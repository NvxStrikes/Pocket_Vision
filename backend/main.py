import os
from dotenv import load_dotenv
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from vision.schemas import SmartScanResponse
from vision.analyzer import VisionAnalyzer

load_dotenv()

app = FastAPI(
    title="Pocket Vision Smart Scan API",
    description="Multimodal deep vision analysis service for the Pocket Vision mobile application",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

analyzer = VisionAnalyzer()

@app.get("/")
def read_root():
    return {
        "app": "Pocket Vision Smart Scan Backend",
        "status": "online",
        "endpoints": {
            "health": "/health",
            "analyze": "POST /api/v1/analyze"
        }
    }

@app.get("/health")
def health_check():
    return {
        "status": "ok",
        "gemini_configured": bool(os.getenv("GEMINI_API_KEY"))
    }

@app.post("/api/v1/analyze", response_model=SmartScanResponse)
async def analyze_frame(image: UploadFile = File(...)):
    if not image.content_type.startswith("image/"):
        raise HTTPException(status_code=400, detail="Uploaded file must be an image.")
    contents = await image.read()
    if len(contents) == 0:
        raise HTTPException(status_code=400, detail="Image file is empty.")
    result = await analyzer.analyze_image(contents)
    return result

if __name__ == "__main__":
    import uvicorn
    port = int(os.getenv("PORT", 8000))
    host = os.getenv("HOST", "0.0.0.0")
    uvicorn.run("main:app", host=host, port=port, reload=True)
