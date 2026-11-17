FROM python:3.12-slim
RUN apt-get update && apt-get install -y --no-install-recommends curl libglib2.0-0 \
 && rm -rf /var/lib/apt/lists/* && useradd -r -m app
WORKDIR /srv
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Models (OpenCV Zoo). The size checks catch a Git-LFS pointer file downloaded by mistake.
RUN mkdir /models && cd /models \
 && curl -fL -o face_detection_yunet_2023mar.onnx https://github.com/opencv/opencv_zoo/raw/main/models/face_detection_yunet/face_detection_yunet_2023mar.onnx \
 && curl -fL -o face_recognition_sface_2021dec.onnx https://github.com/opencv/opencv_zoo/raw/main/models/face_recognition_sface/face_recognition_sface_2021dec.onnx \
 && test "$(stat -c%s face_detection_yunet_2023mar.onnx)" -gt 100000 \
 && test "$(stat -c%s face_recognition_sface_2021dec.onnx)" -gt 1000000

COPY app ./app
USER app
EXPOSE 8001
HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=5 \
  CMD curl -fs http://localhost:8001/health || exit 1
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8001"]