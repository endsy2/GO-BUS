
from fastapi import APIRouter, Query, Body, Header, HTTPException
from pydantic import BaseModel
from app.service import BakongService

import logging
from typing import Optional

router = APIRouter(prefix="/bakong", tags=["bakong"])

class VerifyMD5Request(BaseModel):
    md5: str

@router.get("/generateQR")
async def generate_bakong_qr(
    amount: float = Query(...),
    currency: str = Query(...),
    merchant_name: str = Query(...),
    bank_account: str = Query(...),
    number_phone: str = Query(...),
):
    try:
        result = BakongService.generateQR(amount, currency, merchant_name,bank_account,number_phone)

        return {
            "responseCode": 200,
            "responseMessage": "Generate QR successfully",
            "errorCode": None,
            "data": result
        }

    except HTTPException as e:
        return {
            "responseCode": e.status_code,
            "responseMessage": e.detail,
            "errorCode": e.status_code,
            "data": None
        }

    except Exception as e:
        logging.exception("Error in generate_bakong_qr endpoint")

        return {
            "responseCode": 500,
            "responseMessage": str(e),
            "errorCode": 500,
            "data": None
        }


@router.post("/verifyMD5")
async def verify_md5_endpoint(
    request: VerifyMD5Request = Body(...)
):
        # 2️⃣ Call service and pass token
        result = await BakongService.verifyMD5(request.md5)
        
        # 3️⃣ Return success response matching BakongResponse DTO
        return {
            "data": result
        }
    



@router.post("/payment_info")
async def payment_info(md5: str):
    return BakongService.payment_info(md5)

@router.post("/renewToken")
async def renew_token():
    """
    Manually renew Bakong token
    """
    token = await BakongService.renew_bakong_token()
    return {"success": True, "message": "Token renewed successfully", "token": token}
