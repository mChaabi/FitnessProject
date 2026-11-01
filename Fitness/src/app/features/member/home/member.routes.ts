import { Routes } from '@angular/router';
import { Home } from './home'; // Ajusta la ruta o el nombre de tu componente home si es necesario

export const MEMBER_ROUTES: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'home' },
  { path: 'home', component: Home }, // O usa loadComponent si es standalone
];