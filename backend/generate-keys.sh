#!/bin/bash
# =============================================================================
#  RSA Key Generator for JWT RS256
#  Run once from the project root:
#      chmod +x generate-keys.sh && ./generate-keys.sh
#
#  Outputs two .env snippets — paste them into your environment or
#  directly into the relevant application.yml files.
# =============================================================================
set -e

echo ""
echo "Generating RSA 2048-bit key pair for JWT RS256..."
echo ""

# Generate private key in PKCS8 format (required by Java KeyFactory)
openssl genrsa 2048 2>/dev/null | \
    openssl pkcs8 -topk8 -nocrypt -out private.pem

# Derive public key in X509/SPKI format
openssl rsa -pubout -in private.pem -out public.pem 2>/dev/null

# Strip PEM headers and encode as single-line Base64
PRIVATE_B64=$(grep -v "^-----" private.pem | tr -d '\n')
PUBLIC_B64=$(grep -v "^-----" public.pem | tr -d '\n')

echo "========== COPY THESE INTO YOUR .env FILE ================================"
echo ""
echo "JWT_PRIVATE_KEY=${PRIVATE_B64}"
echo ""
echo "JWT_PUBLIC_KEY=${PUBLIC_B64}"
echo ""
echo "=========================================================================="
echo ""
echo "WARNING: private.pem contains your secret key — do NOT commit it!"
echo "Both .pem files in this directory should be in your .gitignore."
echo ""
echo "user-service needs : JWT_PRIVATE_KEY + JWT_PUBLIC_KEY"
echo "gateway-service needs: JWT_PUBLIC_KEY only"
echo ""

# Optionally delete the raw .pem files after displaying the values
read -r -p "Delete the raw .pem files now? [y/N] " answer
if [[ "$answer" =~ ^[Yy]$ ]]; then
    rm private.pem public.pem
    echo "Deleted private.pem and public.pem."
else
    echo "Keeping .pem files — add them to .gitignore!"
fi
