export interface Probe { minutes: number | null; thumb: Blob | null; }

export function probeVideo(file: File): Promise<Probe> {
  return new Promise(resolve => {
    const url = URL.createObjectURL(file);
    const v = document.createElement('video');
    v.preload = 'metadata'; v.muted = true; v.playsInline = true; v.src = url;
    let done = false;
    const finish = (p: Probe) => {
      if (done) return; done = true; clearTimeout(timer);
      URL.revokeObjectURL(url); v.removeAttribute('src'); v.load(); resolve(p);
    };
    const timer = setTimeout(() => finish({ minutes: null, thumb: null }), 8000);

    v.onloadedmetadata = () => {
      const minutes = isFinite(v.duration) ? Math.max(1, Math.round(v.duration / 60)) : null;
      v.onseeked = () => {
        try {
          const w = 640, h = Math.round((w * (v.videoHeight || 360)) / (v.videoWidth || 640));
          const c = document.createElement('canvas'); c.width = w; c.height = h;
          c.getContext('2d')!.drawImage(v, 0, 0, w, h);
          c.toBlob(b => finish({ minutes, thumb: b }), 'image/jpeg', 0.8);
        } catch { finish({ minutes, thumb: null }); }
      };
      v.currentTime = Math.min(1, (v.duration || 2) / 4);       // a frame near the start
    };
    v.onerror = () => finish({ minutes: null, thumb: null });
  });
}