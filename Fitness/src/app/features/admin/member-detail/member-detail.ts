import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, computed, inject, OnDestroy, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MemberService } from '../../../data/members/service/member';
import { Plan, SubscriptionRow, PaymentMethod } from '../../../data/billing/billing';
import { Coach } from '../../../data/coaching/coaching';
import { CoachingService } from '../../../data/coaching/service/coaching';
import { BillingService } from '../../../data/billing/service/billing';
import { Member } from '../../../data/members/member';
import { AuthService } from '../../../core/auth/auth-service';
import { FaceCamera } from '../../../shared/face-camera/face-camera';
import { FaceService, FaceStatus } from '../../../data/face/service/face';
import { waLink, waMessages } from '../../../core/whatsapp';
import { addDays } from '../../../core/dates';
import { FREEZE_MAX, FREEZE_MIN, FreezeRow } from '../../../data/freeze/freeze';
import { FreezeApiService } from '../../../data/freeze/service/freeze';

@Component({
  selector: 'app-member-detail',
  standalone: true,
  imports: [RouterLink, DatePipe, CurrencyPipe, FaceCamera],
  templateUrl: './member-detail.html',
  styleUrl: './member-detail.css',
})
export class MemberDetailComponent implements OnDestroy {
  private svc = inject(MemberService);
  private route = inject(ActivatedRoute);
  auth = inject(AuthService);
  private billing = inject(BillingService);
  private coaching = inject(CoachingService);

  coaches = signal<Coach[]>([]);
  linkEmail = signal('');
  msg = signal('');
  plans = signal<Plan[]>([]);
  history = signal<SubscriptionRow[]>([]);
  selling = signal(false);
  saleError = signal('');
  planId = signal<number | null>(null);
  method = signal<PaymentMethod>('CASH');

  

  member = signal<Member | null>(null);
  qrUrl = signal<string | null>(null);
  barcodeUrl = signal<string | null>(null);
  private id = Number(this.route.snapshot.paramMap.get('id'));

    private faceSvc = inject(FaceService);
  face = signal<FaceStatus | null>(null);
  faceConsent = signal(false);
  faceCam = signal(false);
  faceBusy = signal(false);
  faceMsg = signal<{ ok: boolean; text: string } | null>(null);

    private freezeApi = inject(FreezeApiService);
  freezeMin = FREEZE_MIN; freezeMax = FREEZE_MAX;
  presets = [7, 14, 30, 60];
  freezeDays = signal(14);
  freezeReason = signal('');
  freezes = signal<FreezeRow[]>([]);
  freezeBusy = signal(false);
  freezeMsg = signal<{ ok: boolean; text: string } | null>(null);

  activeFreeze = computed(() => this.freezes().find(f => f.status === 'ACTIVE') ?? null);
  resumeOn = computed(() => addDays(new Date(), this.freezeDays()));
  daysValid = computed(() => { const d = this.freezeDays(); return Number.isInteger(d) && d >= FREEZE_MIN && d <= FREEZE_MAX; });

  loadFreezes() { this.freezeApi.history(this.id).subscribe({ next: f => this.freezes.set(f), error: () => {} }); }
  setDays(v: string) { this.freezeDays.set(Math.floor(+v) || 0); }

  freezeNow() {
    if (!this.daysValid() || this.freezeBusy()) return;
    const d = this.freezeDays();
    if (!confirm(`Geler l'abonnement pendant ${d} jours ?\nLes réservations de cette période seront annulées et le membre sera prévenu par email.`)) return;
    this.freezeBusy.set(true); this.freezeMsg.set(null);
    this.freezeApi.freeze(this.id, d, this.freezeReason()).subscribe({
      next: r => {
        this.freezeBusy.set(false); this.freezeReason.set('');
        const end = r.newEndDate ? new Date(r.newEndDate + 'T00:00:00').toLocaleDateString('fr-FR') : '';
        this.freezeMsg.set({ ok: true, text: `Abonnement gelé. Nouvelle fin : ${end}.` +
          (r.cancelledBookings ? ` ${r.cancelledBookings} réservation(s) annulée(s).` : '') });
        this.afterFreezeChange();
      },
      error: e => { this.freezeBusy.set(false); this.freezeMsg.set({ ok: false, text: e.error?.message ?? 'Gel impossible.' }); }
    });
  }

  unfreezeNow() {
    if (this.freezeBusy() || !confirm('Reprendre maintenant ? Les jours de gel non utilisés seront retirés de la fin de l\'abonnement.')) return;
    this.freezeBusy.set(true); this.freezeMsg.set(null);
    this.freezeApi.unfreeze(this.id).subscribe({
      next: () => { this.freezeBusy.set(false); this.freezeMsg.set({ ok: true, text: 'Abonnement repris.' }); this.afterFreezeChange(); },
      error: e => { this.freezeBusy.set(false); this.freezeMsg.set({ ok: false, text: e.error?.message ?? 'Reprise impossible.' }); }
    });
  }

  private afterFreezeChange() {
    this.svc.get(this.id).subscribe(m => this.member.set(m));
    this.loadHistory();                                   // the subscription table shows the new end dates
    this.loadFreezes();
  }

  loadFace() { this.faceSvc.status(this.id).subscribe({ next: s => this.face.set(s), error: () => this.face.set(null) }); }

  onFacePhoto(photo: Blob) {
    if (!this.faceConsent() || this.faceBusy()) return;
    this.faceBusy.set(true); this.faceMsg.set(null);
    this.faceSvc.enroll(this.id, photo, true).subscribe({
      next: s => { this.face.set(s); this.faceBusy.set(false); this.faceCam.set(false); this.faceConsent.set(false);
                   this.faceMsg.set({ ok: true, text: 'Visage enregistré.' }); },
      error: e => { this.faceBusy.set(false);
                    this.faceMsg.set({ ok: false, text: e.error?.message ?? 'Enregistrement impossible.' }); }   // camera stays open: retry
    });
  }

  removeFace() {
    if (!confirm('Supprimer les données faciales de ce membre ?')) return;
    this.faceSvc.remove(this.id).subscribe(() => { this.loadFace(); this.faceMsg.set({ ok: true, text: 'Données faciales supprimées.' }); });
  }
  
  waHref = computed(() => {
    const m = this.member();
    return m ? waLink(m.phone, waMessages.hello(m.firstName)) : null;
  });


  constructor() {
    this.svc.get(this.id).subscribe((m) => this.member.set(m));
    this.svc.qr(this.id).subscribe((blob) => this.qrUrl.set(URL.createObjectURL(blob)));
    this.billing.getPlans().subscribe((p) => this.plans.set(p));
        this.svc.barcode(this.id).subscribe((b) => this.barcodeUrl.set(URL.createObjectURL(b)));
            if (this.auth.role() === 'ADMIN' || this.auth.role() === 'RECEPTION') this.loadFace();
    if (this.auth.role() === 'ADMIN') this.coaching.coaches().subscribe(c => this.coaches.set(c.filter(x => x.active)));
        const wanted = Number(this.route.snapshot.queryParamMap.get('plan')) || null;   // ?plan=3 from the Inscriptions page
    if (wanted) this.planId.set(wanted);

    this.loadHistory();
        this.loadFreezes();
  }

  assignCoach(v: string) {
  this.coaching.assign(this.id, v ? +v : null).subscribe(() => this.msg.set('Coach mis à jour'));
}
link() {
  this.coaching.linkAccount(this.id, this.linkEmail()).subscribe({
    next: () => { this.msg.set('Compte lié'); this.svc.get(this.id).subscribe(m => this.member.set(m)); },
    error: e => this.msg.set(e.error?.message ?? 'Erreur')
    });}

  loadHistory() {
    this.billing.getSubscriptions(this.id).subscribe((h) => this.history.set(h));
  }
  

  deactivate() {
    if (!confirm('Désactiver ce membre ?')) return;
    this.svc
      .deactivate(this.id)
      .subscribe(() => this.svc.get(this.id).subscribe((m) => this.member.set(m)));
  }

  sell() {
    const plan = this.planId();
    if (!plan || this.selling()) return; // evita el doble clic = doble cobro
    this.selling.set(true);
    this.saleError.set('');
    this.billing.createBilling(this.id, { planId: plan, method: this.method() }).subscribe({
      next: (r) => {
        this.selling.set(false);
        this.loadHistory();
        this.svc.get(this.id).subscribe((m) => this.member.set(m));
        this.billing.downloadReceipt(r.paymentId, r.receiptNumber);
      },
      error: (e) => {
        this.selling.set(false);
        this.saleError.set(e.error?.message ?? 'Erreur');
      },
    });
  }

  download(h: SubscriptionRow) {
    this.billing.downloadReceipt(h.paymentId, h.receiptNumber);
  }

  ngOnDestroy() {
    const u = this.qrUrl();
        const bu = this.barcodeUrl(); if (bu) URL.revokeObjectURL(bu);
    if (u) URL.revokeObjectURL(u);
  } // libera memoria
}
