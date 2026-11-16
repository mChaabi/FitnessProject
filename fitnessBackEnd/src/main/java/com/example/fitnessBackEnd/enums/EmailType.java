package com.example.fitnessBackEnd.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailType {
    WELCOME(false, "Bienvenue !",
            "Accueille le nouvel utilisateur et lui souhaite la bienvenue dans le club.",
            "Bienvenue dans notre club ! Nous sommes ravis de vous compter parmi nous.",
            "Commencer", "/member/home"), // <-- Ajouté ici avec une virgule
    PAYMENT_CONFIRMED(false, "Paiement confirmé",
            "Confirme au membre que son paiement est enregistré et que son abonnement est actif, sur un ton sobre et chaleureux.",
            "Merci ! Votre paiement a bien été enregistré et votre abonnement est maintenant actif.",
            "Réserver un cours", "/member/book"),
    PAYMENT_REMINDER(true, "Votre inscription est presque terminée",
            "Rappelle au membre, sans pression, qu'il lui reste à régler son abonnement à l'accueil pour pouvoir réserver ses cours.",
            "Votre compte est prêt ! Il ne vous reste qu'à régler votre abonnement à l'accueil pour réserver vos cours.",
            "Mon espace", "/member/home"),
    MEMBERSHIP_FROZEN(false, "Votre abonnement est gelé",
            "Confirme au membre que son abonnement est gelé, que la durée du gel sera ajoutée à la fin de son abonnement et qu'il pourra reprendre ses séances à la date de reprise.",
            "Votre abonnement est gelé. La durée du gel est ajoutée à la fin de votre abonnement : vous ne perdez aucun jour.",
            "Mon espace", "/member/home"),
    MEMBERSHIP_RESUMED(false, "Bon retour !",
            "Souhaite un bon retour au membre dont l'abonnement reprend aujourd'hui et l'invite à réserver sa prochaine séance.",
            "Votre abonnement reprend aujourd'hui. Nous avons hâte de vous revoir à la salle !",
            "Réserver un cours", "/member/book"),
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


