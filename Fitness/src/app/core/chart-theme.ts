/** Call right before creating charts: it reads the CURRENT theme variables. */
export function applyChartTheme(Chart: any) {
  const css = getComputedStyle(document.documentElement);
  const tok = (name: string, fallback: string) => css.getPropertyValue(name).trim() || fallback;

  Chart.defaults.font.family = tok('--font-body', 'Inter, sans-serif');
  Chart.defaults.color = tok('--text-muted', '#64748b');
  Chart.defaults.borderColor = tok('--border', '#e5e9f0');       // grid lines

  return { tok, primary: tok('--primary', '#f97316'), grid: tok('--border', '#e5e9f0') };
}