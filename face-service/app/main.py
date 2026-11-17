import hmac
import os
import threading

import cv2
import numpy as np
from fastapi import FastAPI, File, Header, HTTPException, UploadFile

# 1. Configurar correctamente la ruta apuntando a la carpeta 'models' al mismo nivel que 'app'
MODELS = os.path.join(os.path.dirname(__file__), "..", "models")

API_KEY = os.environ["FACE_API_KEY"]        # required: the service refuses to start without it
MAX_BYTES = 2 * 1024 * 1024                 # 2 MB per photo
MIN_FACE_PX = 90                            # face narrower than this is too small
MIN_SHARPNESS = 40.0                        # Laplacian variance: below = blurry (tune with real photos)

# 2. Inicializar detector y recognizer usando la ruta corregida
detector = cv2.FaceDetectorYN.create(
    os.path.join(MODELS, "face_detection_yunet_2023mar.onnx"), "", (320, 320), 0.9, 0.3, 5000)
recognizer = cv2.FaceRecognizerSF.create(
    os.path.join(MODELS, "face_recognition_sface_2021dec.onnx"), "")

lock = threading.Lock()                     # OpenCV detectors keep state: one image at a time

# no /docs and no /openapi.json: nothing to browse
app = FastAPI(title="FitForge face service", docs_url=None, redoc_url=None, openapi_url=None)


def check_key(key: str | None):
    if not key or not hmac.compare_digest(key.encode(), API_KEY.encode()):
        raise HTTPException(status_code=401, detail="unauthorized")


def reject(reason: str):
    return {"ok": False, "reason": reason}   # business outcome: HTTP 200, the caller decides what to show


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/embed")
def embed(image: UploadFile = File(...), x_internal_key: str | None = Header(default=None)):
    check_key(x_internal_key)

    data = image.file.read(MAX_BYTES + 1)
    if len(data) > MAX_BYTES:
        raise HTTPException(status_code=413, detail="image_too_large")
    img = cv2.imdecode(np.frombuffer(data, np.uint8), cv2.IMREAD_COLOR)
    if img is None:
        raise HTTPException(status_code=400, detail="invalid_image")

    h, w = img.shape[:2]
    if max(h, w) > 1920:                     # shrink very large photos
        s = 1920 / max(h, w)
        img = cv2.resize(img, (int(w * s), int(h * s)))
        h, w = img.shape[:2]

    with lock:
        detector.setInputSize((w, h))
        _, faces = detector.detect(img)
        if faces is None or len(faces) == 0:
            return reject("no_face")
        if len(faces) > 1:
            return reject("several_faces")   # never guess between two people

        face = faces[0]
        x, y, bw, bh = [max(0, int(v)) for v in face[:4]]
        if bw < MIN_FACE_PX:
            return reject("face_too_small")
        crop = img[y:y + bh, x:x + bw]
        if crop.size == 0:
            return reject("no_face")
        sharpness = float(cv2.Laplacian(cv2.cvtColor(crop, cv2.COLOR_BGR2GRAY), cv2.CV_64F).var())
        if sharpness < MIN_SHARPNESS:
            return reject("too_blurry")

        aligned = recognizer.alignCrop(img, face)
        feat = recognizer.feature(aligned).flatten().astype(np.float64)

    feat /= np.linalg.norm(feat)             # unit length: cosine similarity = a simple dot product
    return {
        "ok": True,
        "embedding": [round(float(v), 6) for v in feat],
        "quality": {"detection": round(float(face[14]), 3), "facePx": bw, "sharpness": round(sharpness, 1)},
    }