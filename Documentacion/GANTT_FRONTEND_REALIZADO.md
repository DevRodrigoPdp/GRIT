# GRIT — Frontend realizado hasta hoy
**Período:** 21 Mar → 26 Mar 2026

```mermaid
gantt
    title GRIT Frontend — Lo construido hasta hoy
    dateFormat  YYYY-MM-DD
    axisFormat  %d %b

    section ⚙️ Configuración
    Setup Angular 21 + Tailwind + estructura base   :done, 2026-03-21, 1d
    Proxy config para desarrollo local              :done, 2026-03-26, 1d

    section 🔐 Autenticación
    Login page (formulario + validación)            :done, 2026-03-21, 1d
    AuthService (cookies JWT, signals, redirección) :done, 2026-03-21, 1d
    Página pendiente de revisión                    :done, 2026-03-21, 1d

    section 🏠 Landing
    Landing page (hero, scroll-video, splash)       :done, 2026-03-24, 1d
    Componente verificación de credenciales         :done, 2026-03-24, 1d
    Footer component                                :done, 2026-03-24, 1d

    section 📋 Registro Entrenador
    Formulario datos personales + correo            :done, 2026-03-24, 1d
    Selector titulación entrenamiento (oficial ES)  :done, 2026-03-26, 1d
    Selector titulación nutrición (opcional, oficial ES) :done, 2026-03-26, 1d
    Subida de documentos (drag & drop, PDF/JPG/PNG) :done, 2026-03-24, 1d
    Validación + error banner + scroll to top       :done, 2026-03-26, 1d

    section 📋 Registro Atleta
    Formulario datos personales y físicos           :done, 2026-03-24, 1d
    Selector nivel deportivo                        :done, 2026-03-24, 1d
    Selector servicio (entrenamiento/nutrición/ambos):done, 2026-03-26, 1d
    Objetivo condicional según servicio             :done, 2026-03-26, 1d
    Validación + error banner + scroll to top       :done, 2026-03-26, 1d

    section 📊 Dashboard Atleta
    Placeholders dashboards por rol                 :done, 2026-03-21, 1d
    Layout + perfil strip                           :done, 2026-03-26, 1d
    Tabs dinámicas según servicio contratado        :done, 2026-03-26, 1d
    Spinner de carga animado                        :done, 2026-03-26, 1d
    Conexión formulario → AuthService → dashboard   :done, 2026-03-26, 1d
```

---

## Resumen por día

| Fecha | Qué se hizo |
|---|---|
| 21 Mar | Setup, login, AuthService, dashboards placeholder, página pendiente |
| 24 Mar | Landing, footer, registro entrenador, registro atleta |
| 25 Mar | Reestructuración de archivos, navigation flow |
| 26 Mar | Proxy, campo servicio + objetivo condicional, titulaciones oficiales, scroll to top, dashboard atleta completo |

---

## Componentes y páginas completadas

| Archivo | Estado |
|---|---|
| `pages/landing` | ✅ |
| `pages/login` | ✅ |
| `pages/entrenador` | ✅ |
| `pages/atleta` | ✅ |
| `pages/pendiente` | ✅ |
| `pages/dashboard-atleta` | ✅ estructura + tabs |
| `pages/dashboard-entrenador` | 🔲 placeholder |
| `pages/dashboard-entrenador-nutricion` | 🔲 placeholder |
| `components/footer` | ✅ |
| `components/hero` | ✅ |
| `components/splash` | ✅ |
| `components/scroll-video` | ✅ |
| `components/verification` | ✅ |
| `components/role-selector` | ✅ |
| `components/grit-loader` | ✅ |
| `services/auth.service` | ✅ signals + mock |

---

> **Hoy:** 26 de Marzo de 2026
