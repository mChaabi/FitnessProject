import { RenderMode, ServerRoute } from '@angular/ssr';

export const serverRoutes: ServerRoute[] = [
  { path: 'admin/**', renderMode: RenderMode.Client },
  { path: 'member/**', renderMode: RenderMode.Client },
  { path: 'coach/**', renderMode: RenderMode.Client },
  { path: 'reception/**', renderMode: RenderMode.Client },
  { path: 'espace', renderMode: RenderMode.Client },
  { path: 'login', renderMode: RenderMode.Client },
  { path: 'inscription', renderMode: RenderMode.Client },
  { path: '**', renderMode: RenderMode.Prerender },
];