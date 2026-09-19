# Pocket Vision — Backend Service

Lightweight FastAPI backend powering the **Smart Scan** feature for the Pocket Vision Android app.

## Features
* Fast, asynchronous image processing endpoint (`POST /api/v1/analyze`).
* Integrates Google Gemini 2.5 Flash for deep scene recognition, species/breed identification, and natural language descriptions.
* Built-in fallback mock mode: runs smoothly even when offline or without an API key.

## Quick Start
1. Create a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate # or venv\Scripts\activate on Windows
   ```
2. Install dependencies:
   ```bash
   pip install -r requirements.txt
   ```
3. Copy environment configuration:
   ```bash
   cp .env.example .env
   ```
4. Run the server:
   ```bash
   uvicorn main:app --reload --host 0.0.0.0 --port 8000
   ```
