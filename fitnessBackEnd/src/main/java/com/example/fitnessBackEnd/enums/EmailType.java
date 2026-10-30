package com.example.fitnessBackEnd.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailType {
    WAITLIST_PROMOTED(false, "Votre place est confirmée",
            "Informe le membre qu'une place s'est libérée et que sa réservation est désormais confirmée.",
            "Bonne nouvelle : une place s'est libérée et votre réservation est maintenant confirmée.",
            "Voir mes cours", "/member/classes"),
    CLASS_CANCELLED(false, "Séance annulée",
            "Informe le membre que la séance a été annulée et présente des excuses sobres.",
            "Nous sommes désolés : cette séance a dû être annulée. Votre réservation a été libérée.",
            "Voir le planning", "/member/classes"),
    CLASS_REMINDER(true, "Rappel : votre séance de demain",
            "Rappelle au membre sa séance de demain et l'encourage à venir.",
            "Petit rappel : vous êtes inscrit(e) à une séance demain. Nous avons hâte de vous voir !",
            "Voir mes cours", "/member/classes"),
    SUBSCRIPTION_EXPIRING(true, "Votre abonnement arrive à échéance",
            "Informe le membre que son abonnement se termine bientôt et l'invite à le renouveler à l'accueil, sans pression.",
            "Votre abonnement arrive bientôt à échéance. Pensez à le renouveler pour ne pas interrompre vos séances.",
            "Mon espace", "/member/home"),
    CHURN_RISK(true, "On ne vous voit plus !",
            "Encourage chaleureusement le membre à reprendre l'entraînement, sans culpabiliser, en rappelant que les coachs peuvent l'aider.",
            "Cela fait un moment que nous ne vous avons pas vu. Nos coachs sont là pour vous aider à reprendre en douceur.",
            "Réserver une séance", "/member/classes");

    private final boolean respectsOptOut;   // true = correo "opcional": se puede dar de baja
    private final String subject, task, fallback, ctaLabel, ctaPath;
}


