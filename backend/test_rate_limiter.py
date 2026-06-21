#!/usr/bin/env python3
"""
Rate-Limiter Test Script
========================
Hammers each gateway route tier and reports how many requests succeeded (2xx/4xx-non-429)
vs were rate-limited (429).

Usage
-----
    # unauthenticated (auth + IP-limited routes only)
    python test_rate_limiter.py

    # with a real JWT token (tests user-keyed routes too)
    python test_rate_limiter.py --token <your_access_token>

    # custom gateway host/port
    python test_rate_limiter.py --host http://localhost:8080 --token <token>
"""

import argparse
import time
import sys
import urllib.request
import urllib.error
import concurrent.futures
from dataclasses import dataclass, field
from typing import Optional

# ── ANSI colours ───────────────────────────────────────────────────────────────
GREEN  = "\033[92m"
YELLOW = "\033[93m"
RED    = "\033[91m"
CYAN   = "\033[96m"
RESET  = "\033[0m"
BOLD   = "\033[1m"

def c(colour: str, text: str) -> str:
    return f"{colour}{text}{RESET}"

# ── Test-case definition ───────────────────────────────────────────────────────
@dataclass
class RateLimitTest:
    name:         str
    path:         str
    method:       str          = "GET"
    burst:        int          = 30        # requests fired in one burst
    limit:        int          = 0         # expected req/s limit (for display)
    needs_auth:   bool         = False     # requires Authorization header
    body:         Optional[str] = None     # JSON body for POST requests

TESTS = [
    RateLimitTest(
        name       = "Auth route  (IP-limited, 10 req/s burst 20)",
        path       = "/api/auth/login",
        method     = "POST",
        burst      = 30,              # > burst-of-20 → should see 429s
        limit      = 10,
        needs_auth = False,
        body       = '{"email":"test@test.com","password":"wrong"}',
    ),
    RateLimitTest(
        name       = "User route   (user-limited, 30 req/s burst 60)",
        path       = "/api/users/me",
        method     = "GET",
        burst      = 80,              # > burst-of-60 → should see 429s
        limit      = 30,
        needs_auth = True,
    ),
    RateLimitTest(
        name       = "Bus route    (user-limited, 30 req/s burst 60)",
        path       = "/api/buses",
        method     = "GET",
        burst      = 80,
        limit      = 30,
        needs_auth = True,
    ),
    RateLimitTest(
        name       = "Booking route (user-limited, 20 req/s burst 40)",
        path       = "/api/bookings",
        method     = "GET",
        burst      = 60,              # > burst-of-40 → should see 429s
        limit      = 20,
        needs_auth = True,
    ),
]

# ── Single request helper ──────────────────────────────────────────────────────
def fire(url: str, method: str, headers: dict, body: Optional[str]) -> tuple[int, str]:
    """
    Returns (status_code, label).
    Never raises — connection errors are reported as status 0.
    """
    data = body.encode() if body else None
    req  = urllib.request.Request(url, data=data, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            return resp.status, "OK"
    except urllib.error.HTTPError as e:
        return e.code, e.reason
    except Exception as e:
        return 0, str(e)

# ── Run one test ───────────────────────────────────────────────────────────────
def run_test(test: RateLimitTest, base_url: str, token: Optional[str]) -> None:
    url = base_url.rstrip("/") + test.path

    headers: dict = {"Content-Type": "application/json"}
    if test.needs_auth:
        if not token:
            print(c(YELLOW, f"  ⚠  Skipping '{test.name}' — needs --token\n"))
            return
        headers["Authorization"] = f"Bearer {token}"

    print(c(BOLD, f"\n{'─'*60}"))
    print(c(CYAN, f"  {test.name}"))
    print(f"  URL   : {test.method} {url}")
    print(f"  Burst : {test.burst} concurrent requests")
    print(c(BOLD, f"{'─'*60}"))

    results: dict[int, int] = {}

    # Fire all requests in parallel to maximise burst intensity
    with concurrent.futures.ThreadPoolExecutor(max_workers=test.burst) as pool:
        futures = [
            pool.submit(fire, url, test.method, dict(headers), test.body)
            for _ in range(test.burst)
        ]
        for f in concurrent.futures.as_completed(futures):
            code, _ = f.result()
            results[code] = results.get(code, 0) + 1

    # Print per-status breakdown
    total    = sum(results.values())
    limited  = results.get(429, 0)
    ok_codes = {c for c in results if c not in (429, 0)}
    ok_total = sum(results[c] for c in ok_codes)
    errors   = results.get(0, 0)

    for code in sorted(results):
        count = results[code]
        pct   = count * 100 // total
        bar   = "█" * (pct // 5)
        if   code == 429:  colour = RED
        elif code == 0:    colour = YELLOW
        elif code < 400:   colour = GREEN
        else:              colour = YELLOW
        print(f"    HTTP {code if code else 'ERR':>3}  {c(colour, bar):<30}  {count:>3} / {total}  ({pct}%)")

    # Verdict
    print()
    if limited > 0:
        print(c(GREEN, f"  ✓ Rate limiter is ACTIVE — {limited}/{total} requests were throttled (429)"))
    else:
        print(c(YELLOW, f"  ✗ No 429s seen (burst may be within the allowed window, or limiter not reachable)"))

    if errors:
        print(c(YELLOW, f"  ⚠  {errors} request(s) failed with a connection error (is the gateway running?)"))

# ── Recovery test ──────────────────────────────────────────────────────────────
def run_recovery_test(base_url: str, token: Optional[str]) -> None:
    """
    After a burst, wait 2 seconds and confirm the limiter has refilled.
    Uses the auth route (IP-limited, easiest to trigger without a token).
    """
    print(c(BOLD, f"\n{'─'*60}"))
    print(c(CYAN, "  Recovery test — waiting 2 s after burst, then re-testing auth route"))
    print(c(BOLD, f"{'─'*60}"))

    url     = base_url.rstrip("/") + "/api/auth/login"
    headers = {"Content-Type": "application/json"}
    body    = '{"email":"test@test.com","password":"wrong"}'

    # 1. Exhaust the bucket
    print("  Step 1: exhaust bucket (30 rapid requests)…")
    with concurrent.futures.ThreadPoolExecutor(max_workers=30) as pool:
        futs = [pool.submit(fire, url, "POST", dict(headers), body) for _ in range(30)]
        pre  = {}
        for f in concurrent.futures.as_completed(futs):
            code, _ = f.result()
            pre[code] = pre.get(code, 0) + 1
    limited_before = pre.get(429, 0)
    print(f"    → 429s during burst: {limited_before}")

    # 2. Wait for the token bucket to refill
    print("  Step 2: sleeping 2 seconds…")
    time.sleep(2)

    # 3. Try 5 requests — all should succeed now
    print("  Step 3: sending 5 requests after cooldown…")
    post  = {}
    for _ in range(5):
        code, _ = fire(url, "POST", dict(headers), body)
        post[code] = post.get(code, 0) + 1

    limited_after = post.get(429, 0)
    if limited_after == 0:
        print(c(GREEN, f"  ✓ Bucket refilled — 0/5 requests throttled after cooldown"))
    else:
        print(c(YELLOW, f"  ⚠  Still seeing {limited_after}/5 throttled after cooldown"))

# ── Entry point ────────────────────────────────────────────────────────────────
def main() -> None:
    parser = argparse.ArgumentParser(description="Rate-limiter smoke test for the API Gateway")
    parser.add_argument("--host",  default="http://localhost:8080", help="Gateway base URL")
    parser.add_argument("--token", default=None,                    help="Bearer token for authenticated routes")
    args = parser.parse_args()

    print(c(BOLD, f"\n{'═'*60}"))
    print(c(BOLD,  "  Gateway Rate-Limiter Test"))
    print(c(BOLD, f"  Target : {args.host}"))
    print(c(BOLD, f"  Token  : {'(provided)' if args.token else '(none — authenticated tests skipped)'}"))
    print(c(BOLD, f"{'═'*60}"))

    for test in TESTS:
        run_test(test, args.host, args.token)

    run_recovery_test(args.host, args.token)

    print(c(BOLD, f"\n{'═'*60}\n  Done\n{'═'*60}\n"))

if __name__ == "__main__":
    main()
