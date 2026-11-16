export const COUNTRY_CODE = '212';        // Morocco: used when a number is written locally (06...)
export const GYM_NAME = 'FitForge';

/** "06 12-34-56-78", "+212 6 12 34 56 78", "0612345678" -> "212612345678". Null if it cannot be a real number. */
export function toWaNumber(phone?: string | null): string | null {
  if (!phone) return null;
  let d = phone.replace(/\D/g, '');
  if (!d) return null;
  if (!phone.trim().startsWith('+')) {
    if (d.startsWith('00')) d = d.slice(2);                    // 00212... -> 212...
    else if (d.startsWith('0')) d = COUNTRY_CODE + d.slice(1);  // 06... -> 2126...
    else if (d.length === 9) d = COUNTRY_CODE + d;              // 612345678 -> 212612345678
  }
  if (d.startsWith(COUNTRY_CODE + '0')) d = COUNTRY_CODE + d.slice(COUNTRY_CODE.length + 1);   // "+212 (0) 6..."
  return d.length >= 8 && d.length <= 15 ? d : null;            // international numbers: 8 to 15 digits
}

export function waLink(phone: string | null | undefined, text: string): string | null {
  const n = toWaNumber(phone);
  return n ? `https://wa.me/${n}?text=${encodeURIComponent(text)}` : null;
}

/** Friendly, never guilt-tripping (same tone rule as the emails). */
export const waMessages = {
  renewal: (name: string, plan: string, end: string) =>
    `Bonjour ${name}, ici ${GYM_NAME}. Votre abonnement « ${plan} » se termine le ${end}. ` +
    `Passez à l'accueil pour le renouveler et continuer sans interruption. À bientôt !`,
  comeback: (name: string, days: number | null) =>
    days == null
      ? `Bonjour ${name}, ici ${GYM_NAME}. Nous ne vous avons pas encore vu à la salle. ` +
        `Un coach peut vous accueillir et vous montrer comment bien démarrer. Quand passez-vous ?`
      : `Bonjour ${name}, ici ${GYM_NAME}. Cela fait ${days} jours que nous ne vous avons pas vu. ` +
        `Nos coachs peuvent vous aider à reprendre en douceur. Quand passez-vous ?`,
  hello: (name: string) => `Bonjour ${name}, ici ${GYM_NAME}. `,
};