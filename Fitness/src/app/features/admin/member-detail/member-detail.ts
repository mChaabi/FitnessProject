import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, inject, OnDestroy, signal } from '@angular/core';
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
