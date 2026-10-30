import { DatePipe } from '@angular/common';
import { Component, ElementRef, inject, OnDestroy, signal, ViewChild } from '@angular/core';
import { ReactiveFormsModule } from '@angular/forms';
import { CheckinService } from '../../../services/checkin';
import { MemberService } from '../../../services/member';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { debounceTime, distinctUntilChanged, Observable } from 'rxjs';
import { FormControl } from '@angular/forms';
import { Member } from '../../../models/member';
import { Outcome, CheckInResult, CheckInRow, TodayStats } from '../../../models/checkin';


@Component({
  selector: 'app-checkin',
  standalone: true,
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './checkin.html',
  styleUrl: './checkin.css'
})
export class Checkin implements OnDestroy {
  private svc = inject(CheckinService);
  private members = inject(MemberService);

  @ViewChild('scan') scanEl?: ElementRef<HTMLInputElement>;

  result = signal<CheckInResult | null>(null);
  busy = signal(false);
  cameraOn = signal(false);
  cameraError = signal('');
  today = signal<CheckInRow[]>([]);
  stats = signal<TodayStats>({ entered: 0, denied: 0 });
  found = signal<Member[]>([]);

  search = new FormControl('', { nonNullable: true });

  private scanner: any = null;
  private lastCode = '';
  private lastAt = 0;
  private clearTimer?: ReturnType<typeof setTimeout>;

  constructor() {
    this.refresh();
    this.search.valueChanges.pipe(debounceTime(300), distinctUntilChanged(), takeUntilDestroyed())
      .subscribe(q => {
        if (q.trim().length < 2) { this.found.set([]); return; }
        this.members.list(q.trim(), '', 0, 5).subscribe(p => this.found.set(p.content));
      });
  }

  refresh() {
    this.svc.today().subscribe(r => this.today.set(r));
    this.svc.stats().subscribe(s => this.stats.set(s));
  }

  /** Lector USB: el código llega seguido de Enter. */
  onScanEnter(input: HTMLInputElement) {
    const code = input.value.trim();
    input.value = '';
    if (code) this.handle(() => this.svc.byQr(code));
  }

  checkMember(m: Member) {
    this.found.set([]); this.search.setValue('', { emitEvent: false });
    this.handle(() => this.svc.byMember(m.id));
  }

  private handle(call: () => Observable<CheckInResult>) {
    if (this.busy()) return;
    this.busy.set(true);
    call().subscribe({
      next: r => {
        this.result.set(r); this.busy.set(false); this.refresh(); this.beep(r.outcome);
        clearTimeout(this.clearTimer);
        this.clearTimer = setTimeout(() => { this.result.set(null); this.scanEl?.nativeElement.focus(); }, 5000);
      },
      error: e => {
        this.busy.set(false);
        this.result.set({ outcome: 'DENIED', message: e.error?.message ?? 'Erreur de connexion',
                          expiringSoon: false, at: new Date().toISOString() });
      }
    });
  }

  // ---------- cámara ----------
  async startCamera() {
    this.cameraError.set('');
    try {
      const { Html5Qrcode } = await import('html5-qrcode');
      this.scanner = new Html5Qrcode('reader');
      this.cameraOn.set(true);
      await this.scanner.start({ facingMode: 'environment' }, { fps: 10, qrbox: 240 },
        (code: string) => {
          const now = Date.now();
          if (code === this.lastCode && now - this.lastAt < 4000) return;   // la cámara lee el mismo QR muchas veces
          this.lastCode = code; this.lastAt = now;
          this.handle(() => this.svc.byQr(code));
        }, () => {});
    } catch {
      this.cameraOn.set(false);
      this.cameraError.set("Caméra indisponible. Autorisez l'accès ou utilisez le champ de saisie.");
    }
  }

  async stopCamera() {
    try { await this.scanner?.stop(); this.scanner?.clear(); } catch { /* ya parada */ }
    this.scanner = null; this.cameraOn.set(false);
  }

  private beep(o: Outcome) {
    try {
      const ctx = new AudioContext(); const osc = ctx.createOscillator();
      osc.frequency.value = o === 'ALLOWED' ? 880 : 220;
      osc.connect(ctx.destination); osc.start(); osc.stop(ctx.currentTime + 0.18);
    } catch { /* sin audio */ }
  }

  ngOnDestroy() { clearTimeout(this.clearTimer); this.stopCamera(); }
}
