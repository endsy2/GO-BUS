from urllib.parse import  unquote

from dotenv import load_dotenv
from fastapi import HTTPException, Header
from fastapi.responses import JSONResponse
from bakong_khqr import KHQR
import os
import httpx
import logging
from pip._internal.cli import status_codes
from datetime import datetime
from typing import Optional

load_dotenv()  # Load environment variables from .env

# Global variable to store Bakong token
bakong_token = None
email = os.getenv("email")

async def renew_bakong_token(email: str):
    global bakong_token

    if bakong_token is not None:
        return bakong_token

    bakong_url = os.getenv("BakongUrl")

    try:
        async with httpx.AsyncClient() as client:

            url = f"{bakong_url.rstrip('/')}/v1/renew_token"

            payload = {
                "email": email
            }

            headers = {
                "Content-Type": "application/json"
            }

            logging.info(f"Calling Bakong URL: {url}")
            logging.info(f"Payload: {payload}")

            response = await client.post(url, json=payload, headers=headers)

            logging.info(f"Status Code: {response.status_code}")
            logging.info(f"Response Text: {response.text}")

            response.raise_for_status()

            data = response.json()

            logging.info(f"Parsed JSON: {data}")

            bakong_token = data["data"]["token"]

            logging.info(f"Bakong token renewed successfully: {bakong_token}")

            return bakong_token

    except httpx.HTTPStatusError as e:
        logging.error(f"HTTP error: {e.response.status_code}")
        logging.error(f"Response body: {e.response.text}")
        raise HTTPException(
            status_code=500,
            detail=f"Bakong API error: {e.response.text}"
        )

    except Exception as e:
        logging.exception("Unexpected error while renewing token")
        raise HTTPException(
            status_code=500,
            detail=f"Token renewal failed: {str(e)}"
        )

def get_bakong_token():
    """
    Get Bakong token from global variable or environment
    """
    global bakong_token
    
    if bakong_token:
        return bakong_token
    
    # Fallback to environment variable
    env_token = os.getenv("token")
    if env_token:
        bakong_token = env_token
        return bakong_token
    
    return None
        

async def verifyMD5(md5: str):

    try:
        if not md5:
            raise HTTPException(status_code=400, detail="md5 is required")

        # Check if bakong_token exists, if not renew it
        current_bakong_token =await renew_bakong_token(email)
        print (f"Email:{email}")
        print(f"Current Bakong token: {current_bakong_token}")
        if not current_bakong_token:
            logging.info("Bakong token not found, renewing...")
            current_bakong_token = await renew_bakong_token()
        # token="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiYmMwMTRkMTUzMDc4NGQyMiJ9LCJpYXQiOjE3NzkzMDA5MjQsImV4cCI6MTc4NzA3NjkyNH0.zNY--bamXOSxDm_eO2xqDd8UaciEamwNq5R8JFN4ytg"
        # 1️⃣ Check KHQR payment status
        khqr = KHQR(current_bakong_token)
        check_payment = khqr.check_payment(md5)
        logging.info("KHQR status: %s", check_payment)

        if check_payment != "PAID":
            return {"status": check_payment}

        logging.info("Payment is PAID")

        # 2️⃣ Get payment info (async-safe)
        payment_info_result = payment_info(md5)
        if callable(getattr(payment_info_result, "__await__", None)):
            payment_info_result = await payment_info_result

        # 3️⃣ If payment_info still returns JSONResponse, extract dict
        if hasattr(payment_info_result, "body"):
            import json
            payment_info_dict = json.loads(payment_info_result.body.decode())
        else:
            payment_info_dict = payment_info_result  # assume it's already dict

        payment_info_data = payment_info_dict.get("payment_info", {})
        if not payment_info_data:
            raise HTTPException(status_code=500, detail="payment_info is empty")

        # 4️⃣ Logging for debug
        logging.debug("amount: %s", payment_info_data.get("amount"))
        logging.debug("transaction hash: %s", payment_info_data.get("hash"))
        logging.debug("createdDateMs: %s", datetime.utcfromtimestamp(payment_info_data.get("createdDateMs") / 1000).isoformat())

        # 5️⃣ Prepare payment data to return
        payment_data = {
            "amount": payment_info_data.get("amount"),
            "method": "KHQR",
            "transaction_id": payment_info_data.get("hash"),
            "status": "PAID",
            "paidAt": datetime.utcfromtimestamp(payment_info_data.get("createdDateMs") / 1000).isoformat(),
        }

        return payment_data

    except HTTPException:
        raise
    except Exception as e:
        logging.exception("verifyMD5 error")
        raise HTTPException(status_code=500, detail=str(e))

    
def payment_info(md5: str):
    try:
        current_bakong_token = get_bakong_token()
        khqr = KHQR(current_bakong_token)
        payment_info_data = khqr.get_payment(md5)
        return JSONResponse({"payment_info": payment_info_data}, status_code=200)

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    
def generateQR(amount: float, currency: str, merchant_name: str,bank_account:str,number_phone:str):
    try:
        current_bakong_token = get_bakong_token()
        
        # Create an instance of KHQR with Bakong Developer Token
        khqr = KHQR(current_bakong_token)

        # Generate QR code data for a transaction
        qr = khqr.create_qr(
            bank_account=bank_account,  # Your Bakong profile user_name@bank
            merchant_name=unquote(merchant_name) ,
            merchant_city='Phnom Penh',
            amount=amount,  # Use passed parameter
            currency=currency,  # USD or KHR
            store_label='MShop',
            phone_number=number_phone,
            bill_number='TRX01234567',
            terminal_label='Cashier-01',
            static=False  # Static or Dynamic QR code
        )
        md5 = khqr.generate_md5(qr)
        response =({
            "qr":qr,
            "md5":md5,
        })
        return response

    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
    

async def get_token_from_header(authorization: Optional[str] = Header(None)):
    """
    Extract Bearer token from request header
    """
    if not authorization:
        raise HTTPException(status_code=401, detail="Authorization header missing")

    if not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="Invalid authorization header format")

    token = authorization.split(" ")[1]
    return token
