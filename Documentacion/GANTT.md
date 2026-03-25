# GRIT — Plan de desarrollo TFG
**Período:** 24 Mar → 25 May 2026 · **Entrega:** 25 Mayo 2026

```mermaid
gantt
    title GRIT TFG — Roadmap hasta entrega
    dateFormat  YYYY-MM-DD
    axisFormat  %d %b

    section 🗄️ Backend — Base de datos
    Diseño del modelo de datos          :db1, 2026-03-24, 5d
    Implementación esquema SQL/NoSQL    :db2, after db1, 5d
    Seeds y datos de prueba             :db3, after db2, 3d

    section 🔐 Backend — Autenticación y seguridad
    Endpoints login / registro          :auth1, 2026-03-24, 6d
    Estrategia HttpOnly cookies + JWT   :auth2, after auth1, 4d
    Guards y middleware de roles        :auth3, after auth2, 4d
    Validación de inputs y sanitización :auth4, after auth3, 3d

    section 🔌 Backend — Endpoints
    CRUD atletas                        :api1, 2026-04-06, 5d
    CRUD entrenadores                   :api2, after api1, 4d
    Endpoints nutrición                 :api3, after api2, 5d
    Endpoints ejercicios y programas    :api4, after api3, 5d
    Endpoints biométricos / métricas    :api5, after api4, 4d

    section 🎨 Frontend — Dashboard Atleta
    Layout y estructura del dashboard   :fa1, 2026-04-06, 4d
    Cards de métricas biométricas       :fa2, after fa1, 4d
    Cards de plan nutricional           :fa3, after fa2, 4d
    Cards de ejercicios y programas     :fa4, after fa3, 4d
    Gráficas de progreso                :fa5, after fa4, 3d

    section 🎨 Frontend — Dashboard Entrenador
    Layout y estructura del dashboard   :fe1, 2026-04-13, 4d
    Lista y gestión de atletas          :fe2, after fe1, 4d
    Cards de programas de entrenamiento :fe3, after fe2, 4d
    Dashboard nutrición entrenador      :fe4, after fe3, 4d
    Asignación de planes y seguimiento  :fe5, after fe4, 3d

    section 🔗 Integración
    Conexión frontend ↔ backend (auth)  :int1, 2026-05-04, 4d
    Conexión dashboards ↔ API           :int2, after int1, 4d
    Guards de rutas por rol             :int3, after int2, 3d

    section ✅ Cierre
    Testing end-to-end                  :test1, 2026-05-15, 4d
    Corrección de bugs                  :fix1, after test1, 3d
    Documentación y memoria             :doc1, 2026-05-04, 10d
    Buffer / revisión final             :buf1, 2026-05-22, 3d
```

---

## Resumen por semanas

| Semana | Fechas | Frontend | Backend |
|---|---|---|---|
| 1 | 24 Mar – 30 Mar | — | Diseño DB + Auth endpoints |
| 2 | 31 Mar – 06 Abr | — | Seguridad, cookies, guards |
| 3 | 07 Abr – 13 Abr | Dashboard atleta — layout + métricas | CRUD atletas y entrenadores |
| 4 | 14 Abr – 20 Abr | Dashboard atleta — nutrición + ejercicios | Endpoints nutrición |
| 5 | 21 Abr – 27 Abr | Dashboard entrenador — layout + atletas | Endpoints ejercicios y programas |
| 6 | 28 Abr – 04 May | Dashboard entrenador — programas + nutrición | Endpoints biométricos |
| 7 | 05 May – 11 May | Integración auth + dashboards ↔ API | Integración y ajustes |
| 8 | 12 May – 18 May | Guards de rutas + testing | Testing y seguridad final |
| 9 | 19 May – 25 May | Bug fixes + revisión final | Bug fixes + documentación |

---

## Pendiente Frontend

- [ ] Dashboard atleta — cards métricas biométricas
- [ ] Dashboard atleta — cards plan nutricional
- [ ] Dashboard atleta — cards ejercicios y programas
- [ ] Dashboard atleta — gráficas de progreso
- [ ] Dashboard entrenador — gestión de atletas
- [ ] Dashboard entrenador — asignación de programas
- [ ] Dashboard entrenador nutrición — planes por atleta
- [ ] Guards de rutas por rol (CanActivate)
- [ ] Integración con API (HTTP calls, interceptors)

## Pendiente Backend

- [ ] Modelo de datos (DB schema)
- [ ] Endpoints autenticación (login, register, logout, refresh)
- [ ] Middleware de roles y autenticación
- [ ] CRUD atletas
- [ ] CRUD entrenadores
- [ ] Endpoints nutrición (planes, macros, historial)
- [ ] Endpoints ejercicios y programas de entrenamiento
- [ ] Endpoints métricas biométricas
- [ ] Validación y sanitización de inputs
- [ ] Tests de integración

---

> **Entrega final: 25 de Mayo de 2026**
