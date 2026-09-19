import os
import urllib.request
import sys

MODELS = {
    "efficientdet_lite0.tflite": "https://storage.googleapis.com/mediapipe-models/object_detector/efficientdet_lite0/float32/1/efficientdet_lite0.tflite",
    "gesture_recognizer.task": "https://storage.googleapis.com/mediapipe-models/gesture_recognizer/gesture_recognizer/float16/1/gesture_recognizer.task",
    "face_landmarker.task": "https://storage.googleapis.com/mediapipe-models/face_landmarker/face_landmarker/float16/1/face_landmarker.task"
}

def main():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    project_root = os.path.dirname(script_dir)
    assets_dir = os.path.join(project_root, "mobile", "app", "src", "main", "assets")
    os.makedirs(assets_dir, exist_ok=True)

    print("==========================================")
    print("Pocket Vision — MediaPipe Model Downloader")
    print("==========================================")
    print(f"Target directory: {assets_dir}\n")

    for filename, url in MODELS.items():
        target_path = os.path.join(assets_dir, filename)
        if os.path.exists(target_path):
            size_mb = os.path.getsize(target_path) / (1024 * 1024)
            print(f"[EXISTS] {filename} ({size_mb:.2f} MB)")
            continue

        print(f"[DOWNLOADING] {filename}...")
        try:
            urllib.request.urlretrieve(url, target_path)
            size_mb = os.path.getsize(target_path) / (1024 * 1024)
            print(f"[OK] {filename} ({size_mb:.2f} MB)")
        except Exception as e:
            print(f"[ERROR] Failed to download {filename}: {e}", file=sys.stderr)
            sys.exit(1)

    print("\nAll models ready.")

if __name__ == "__main__":
    main()
