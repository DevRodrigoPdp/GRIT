# GRIT Frontend — Documentación Técnica

## Stack tecnológico

| Tecnología | Versión |
|---|---|
| Node.js | 22.14.0 |
| npm | 10.9.2 |
| Angular | 21.2.x |
| Angular CLI | 21.2.3 |
| TypeScript | 5.9.x |
| Tailwind CSS | 4.2.x |
| RxJS | 7.8.x |

---

## Dependencias

### Producción
| Paquete | Versión |
|---|---|
| `@angular/common` | ^21.2.0 |
| `@angular/compiler` | ^21.2.0 |
| `@angular/core` | ^21.2.0 |
| `@angular/forms` | ^21.2.0 |
| `@angular/platform-browser` | ^21.2.0 |
| `@angular/router` | ^21.2.0 |
| `rxjs` | ~7.8.0 |
| `tslib` | ^2.3.0 |

### Desarrollo
| Paquete | Versión |
|---|---|
| `@angular/build` | ^21.2.3 |
| `@angular/cli` | ^21.2.3 |
| `@angular/compiler-cli` | ^21.2.3 |
| `@tailwindcss/vite` | ^4.2.2 |
| `@tailwindcss/postcss` | ^4.2.2 |
| `tailwindcss` | ^4.2.2 |
| `autoprefixer` | ^10.4.27 |
| `prettier` | ^3.8.1 |
| `typescript` | ~5.9.2 |

---

## Arquitectura

- **Standalone components** — sin NgModules
- **Lazy loading** en todas las rutas con `loadComponent`
- **Signals** para estado reactivo local (`signal`, `computed`)
- **Reactive Forms** (`FormBuilder`, `Validators`) en formularios de registro y login
- **HttpOnly cookies** como estrategia de autenticación (sin tokens en localStorage)

---

## Estructura de rutas

```
/                         → LandingPage
/login                    → LoginPage
/pendiente                → PendientePage
/empezar                  → OnboardingPage (selector de rol)
/empezar/entrenador       → EntrenadorPage (formulario de registro)
/empezar/atleta           → AtletaPage (formulario de registro)
/dashboard/atleta         → DashboardAtletaPage
/dashboard/entrenador     → DashboardEntrenadorPage
/dashboard/entrenador/nutricion → DashboardEntrenadorNutricionPage
```

---

## Componentes principales

### Landing
- **HeaderComponent** — navegación fija con menú móvil, scroll instantáneo a secciones via `scrollIntoView({ behavior: 'instant' })`
- **HeroComponent** — sección principal con headline, stats y CTA
- **VerificationComponent** — grid de 3 pilares del sistema
- **DashboardComponent** — gráfica SVG de rendimiento biométrico con métricas
- **FooterComponent** — CTA con video de fondo en loop + links

### Formularios
- **RoleSelectorComponent** — selector de rol atleta/entrenador (`grid-cols-1 sm:grid-cols-2`)
- **EntrenadorPage** — formulario en 3 secciones: datos personales, credenciales, documentación
- **AtletaPage** — formulario en 3 secciones: datos personales, datos físicos, objetivo

---

## Rendimiento

### Change Detection
Los componentes estáticos o con signals usan `ChangeDetectionStrategy.OnPush` para reducir ciclos de detección innecesarios:

| Componente | Estrategia |
|---|---|
| `HeroComponent` | `OnPush` |
| `VerificationComponent` | `OnPush` |
| `FooterComponent` | `OnPush` |
| `HeaderComponent` | Default (signals — Angular lo optimiza automáticamente) |
| `DashboardComponent` | Default (signals + computed) |

### Video (footer)
- `preload="none"` — el archivo no se descarga hasta que se necesita
- `IntersectionObserver` — el video solo se reproduce cuando el footer es visible en el viewport; se pausa al salir
- `muted` + `playsinline` — cumple la política de autoplay de los navegadores

### Scroll
- Listener de scroll con `{ passive: true }` — no bloquea el hilo principal
- RAF loop (`requestAnimationFrame`) fuera de la zona de Angular con `NgZone.runOutsideAngular`
- `zone.run()` solo cuando hay cambios reales — evita change detection innecesaria

### Routing
- Todas las rutas con `loadComponent` — code splitting automático por ruta
- Navegación por anclas con `scrollIntoView({ behavior: 'instant' })` para evitar scroll suave a través de secciones grandes

---

## Responsive

| Breakpoint | Prefijo Tailwind |
|---|---|
| Mobile | (base) |
| ≥ 640px | `sm:` |
| ≥ 768px | `md:` |
| ≥ 1024px | `lg:` |
| ≥ 1280px | `xl:` |

### Decisiones por componente
- **Hero** — tipografía con `clamp(2rem, 10vw, 9rem)` para escalar fluidamente
- **RoleSelector** — `grid-cols-1` en móvil, `grid-cols-2` en `sm+`
- **Dashboard chart** — Y-axis labels ocultos en móvil (`hidden sm:flex`), sin `ml-6` en móvil
- **EntrenadorPage / AtletaPage** — aside con `sticky top-0 h-screen` en `lg+`, oculto en móvil
- **Header** — menú hamburguesa en móvil, nav horizontal en `md+`

---

## Assets

| Archivo | Ruta | Uso |
|---|---|---|
| `video.mp4` | `public/video.mp4` | Fondo del CTA del footer |
| `favicon.ico` | `public/favicon.ico` | Icono del sitio |

> El directorio `public/` es servido estáticamente por Angular (configurado en `angular.json` con `glob: **/*`).

---

## Scripts

```bash
npm start        # Servidor de desarrollo (ng serve)
npm run build    # Build de producción
npm run watch    # Build en modo watch
npm test         # Tests unitarios
```
