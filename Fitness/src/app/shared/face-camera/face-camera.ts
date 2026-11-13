import { Component, ElementRef, OnDestroy, PLATFORM_ID, afterNextRender, inject, input, output, signal, viewChild } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';

@Component({
  selector: 'app-face-camera', standalone: true,
  templateUrl: './face-camera.html', styleUrl: './face-camera.css'
})
export class FaceCamera implements OnDestroy {
  auto = input(true);                       // start the camera as soon as it appears
  busy = input(false);                      // the parent is uploading: disable the button
  label = input('Prendre la photo');
  captured = output<Blob>();

  video = viewChild.required<ElementRef<HTMLVideoElement>>('v');
  state = signal<'idle' | 'starting' | 'live' | 'error'>('idle');
  error = signal('');

  private stream: MediaStream | null = null;
  private browser = isPlatformBrowser(inject(PLATFORM_ID));

  constructor() { afterNextRender(() => { if (this.auto()) this.start(); }); }

  async start() {
    if (!this.browser || this.state() === 'starting') return;
    if (!window.isSecureContext || !navigator.mediaDevices?.getUserMedia) {
      this.fail("La caméra exige HTTPS (ou l'adresse localhost).");
      return;
    }
    this.state.set('starting'); this.error.set('');
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'user', width: { ideal: 640 }, height: { ideal: 480 } }, audio: false });
      const el = this.video().nativeElement;
      el.srcObject = this.stream;
      await el.play();
      this.state.set('live');
    } catch (e: any) {
      this.stop();
      this.fail(e?.name === 'NotAllowedError' ? "Accès refusé. Autorisez la caméra dans la barre d'adresse du navigateur."
        : e?.name === 'NotFoundError' ? 'Aucune caméra détectée.'
        : e?.name === 'NotReadableError' ? 'Caméra utilisée par une autre application. Fermez-la et réessayez.'
        : 'Caméra indisponible.');
    }
  }

  snap() {
    const el = this.video().nativeElement;
    if (this.state() !== 'live' || !el.videoWidth || this.busy()) return;
    const canvas = document.createElement('canvas');
    canvas.width = el.videoWidth; canvas.height = el.videoHeight;
    canvas.getContext('2d')!.drawImage(el, 0, 0);               // the saved photo is NOT mirrored
    canvas.toBlob(b => { if (b) this.captured.emit(b); }, 'image/jpeg', 0.9);
  }

  stop() {
    this.stream?.getTracks().forEach(t => t.stop());
    this.stream = null;
    if (this.browser) this.video().nativeElement.srcObject = null;
    if (this.state() === 'live' || this.state() === 'starting') this.state.set('idle');
  }

  ngOnDestroy() { this.stream?.getTracks().forEach(t => t.stop()); }

  private fail(msg: string) { this.error.set(msg); this.state.set('error'); }
}