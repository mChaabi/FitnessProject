import { DatePipe } from '@angular/common';
import { Component, HostListener, inject, signal } from '@angular/core';
import { AnalysisReply, Severity } from '../../data/agents/agents';
import { AgentsService } from '../../data/agents/service/agents';

@Component({
  selector: 'app-assistant', standalone: true, imports: [DatePipe],
  templateUrl: './assistant.html', styleUrl: './assistant.css'
})
export class AssistantPanel {
  private svc = inject(AgentsService);

  open = signal(false);
  question = signal('');
  busy = signal(false);
  error = signal('');
  result = signal<AnalysisReply | null>(null);

  quick = [
    { label: 'Analyse globale', q: "Fais une analyse globale de l'activité de la salle." },
    { label: 'Revenus', q: "Analyse l'évolution de mes revenus et ce qui peut l'expliquer." },
    { label: 'Fidélisation', q: "Analyse les renouvellements d'abonnement et les risques d'abandon." },
    { label: 'Cours', q: "Analyse l'occupation des cours et propose des améliorations." },
    { label: 'Priorités', q: 'Quelles sont mes trois priorités cette semaine ?' },
  ];

  labels: Record<Severity, string> = { POSITIVE: 'Positif', INFO: 'Info', WARNING: 'Attention', CRITICAL: 'Critique' };

  ask(text: string = this.question()) {
    if (this.busy()) return;                          // one request at a time
    const q = text.trim();
    this.question.set(q);
    this.busy.set(true); this.error.set(''); this.result.set(null);
    this.svc.analyze(q).subscribe({
      next: r => { this.result.set(r); this.busy.set(false); },
      error: e => { this.busy.set(false);
        this.error.set(e.status === 429 ? (e.error?.message ?? 'Limite quotidienne atteinte.')
                                        : (e.error?.message ?? "L'assistant est indisponible pour le moment.")); }
    });
  }

  @HostListener('document:keydown.escape')
  close() { this.open.set(false); }
}