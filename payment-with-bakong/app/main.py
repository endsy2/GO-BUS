from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.controller import BakongController
from pythonjsonlogger import jsonlogger
import logging
import os

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configure JSON logging to stdout
log_level = os.getenv("LOG_LEVEL", "INFO").upper()
handler = logging.StreamHandler()
formatter = jsonlogger.JsonFormatter("%(asctime)s %(levelname)s %(name)s %(message)s")
handler.setFormatter(formatter)

root_logger = logging.getLogger()
root_logger.handlers = [handler]
root_logger.setLevel(log_level)

# Include your router
app.include_router(BakongController.router)
