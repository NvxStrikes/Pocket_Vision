from typing import List, Optional
from pydantic import BaseModel, Field

class SmartScanResponse(BaseModel):
    primary_subject: str = Field(..., description="Main recognized subject or entity")
    specific_type: str = Field(..., description="Likely specific species, breed, model, or classification")
    confidence: float = Field(..., ge=0.0, le=1.0, description="Estimated confidence score between 0.0 and 1.0")
    description: str = Field(..., description="Concise natural language visual description")
    additional_objects: List[str] = Field(default_factory=list, description="Other notable detected objects in the scene")
