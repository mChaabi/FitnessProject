import json, subprocess, sys
import numpy as np

KEY = "test123"          # your FACE_API_KEY
URL = "http://127.0.0.1:8001/embed"

def embed(path):
    out = subprocess.run(["curl.exe", "-s", "-H", f"X-Internal-Key: {KEY}", "-F", f"image=@{path}", URL],
                         capture_output=True, text=True).stdout
    r = json.loads(out)
    if not r.get("ok"):
        sys.exit(f"{path}: rejected -> {r}")
    return np.array(r["embedding"])

a, b, c = embed("me1.jpg"), embed("me2.jpg"), embed("other.jpg")
print("same person      :", round(float(a @ b), 3))
print("different person :", round(float(a @ c), 3))