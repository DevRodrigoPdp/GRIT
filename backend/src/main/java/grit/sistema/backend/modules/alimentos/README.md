# API de Alimentos - Módulo Separado

Este módulo contiene la implementación completa de la API de gestión de alimentos para el sistema GRIT.

## Estructura del Módulo

```
api/alimentos/
├── model/
│   └── Alimento.java               # Entidad JPA para la tabla 'alimentos'
├── dto/
│   ├── AlimentoResponseDTO.java    # DTO para respuestas (GET)
│   └── AlimentoCrearRequestDTO.java # DTO para crear alimentos (POST)
├── repository/
│   └── AlimentoRepository.java      # Repositorio con queries personalizadas
├── service/
│   └── AlimentoService.java         # Lógica de negocio
├── controller/
│   └── AlimentoController.java      # Endpoints REST
├── initializer/
│   └── AlimentoSeeder.java          # Seeder de datos iniciales
└── README.md                        # Este archivo
```

## Endpoints

### 1. Buscar alimentos
```
GET /api/v1/alimentos?q=<texto>
```

**Parámetros:**
- `q` (query string): Término a buscar (mínimo 2 caracteres)

**Respuesta 200:**
```json
{
  "ok": true,
  "data": [
    {
      "codigo": "uuid-alimento",
      "nombre": "Pechuga de pollo",
      "marca": "",
      "kcalPor100g": 165,
      "proteinasPor100g": 31.0,
      "carbsPor100g": 0.0,
      "grasasPor100g": 3.6
    }
  ]
}
```

**Comportamiento:**
- Si `q` está vacío o tiene menos de 2 caracteres, devuelve `data: []` sin error
- Busca coincidencias en nombre o marca
- Ordena por relevancia (coincidencias exactas primero)
- Devuelve máximo 15 resultados

### 2. Crear alimento personalizado
```
POST /api/v1/alimentos
```

**Autenticación:** Cookie `access_token` con `rol === 'ENTRENADOR'` y `titulo_nutricion === true`

**Body:**
```json
{
  "nombre": "Tortilla de patata",
  "marca": "",
  "kcalPor100g": 185,
  "proteinasPor100g": 8.5,
  "carbsPor100g": 16.2,
  "grasasPor100g": 9.8
}
```

**Respuesta 201:**
```json
{
  "ok": true,
  "data": {
    "codigo": "uuid-nuevo",
    "nombre": "Tortilla de patata",
    "marca": "",
    "kcalPor100g": 185,
    "proteinasPor100g": 8.5,
    "carbsPor100g": 16.2,
    "grasasPor100g": 9.8
  }
}
```

## Seguridad

- Ambos endpoints requieren autenticación: cookie `access_token` válida
- Solo entrenadores con `titulo_nutricion === true` pueden acceder
- Protegidos con `@PreAuthorize("hasRole('ENTRENADOR') and @auth.tieneTituloNutricion()")`

## Base de Datos

### Tabla: `alimentos`

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | UUID PK | Identificador único |
| `nombre` | VARCHAR(255) | Nombre del alimento en español |
| `marca` | VARCHAR(255) NULLABLE | Marca (null para alimentos genéricos) |
| `kcal_por_100g` | DECIMAL(7,2) | Calorías por cada 100g |
| `proteinas_por_100g` | DECIMAL(7,2) | Proteínas en gramos por 100g |
| `carbs_por_100g` | DECIMAL(7,2) | Carbohidratos en gramos por 100g |
| `grasas_por_100g` | DECIMAL(7,2) | Grasas en gramos por 100g |
| `creado_por` | UUID FK → usuarios | Nutricionista que lo creó (NULL si es del seeder) |
| `creado_en` | TIMESTAMP | Fecha de creación |

### Índices

- `idx_alimentos_nombre_trgm`: Índice GIN usando pg_trgm para búsqueda rápida
- `idx_alimentos_nombre`: Índice B-tree para ILIKE
- `idx_alimentos_marca`: Índice para búsquedas por marca

## Seeder

El componente `AlimentoSeeder` se ejecuta automáticamente al iniciar la aplicación:

1. **Idempotente:** Solo ejecuta si la tabla está vacía
2. **Carga 100+ alimentos base:** Nutrición deportiva completa y diversa
3. **Sin creador:** Los alimentos del seeder tienen `creado_por = NULL`

### Alimentos cargados (Categorías)

**Proteínas Animales (30+)**
- Aves: Pechuga de pollo, muslo, alas, pavo, pato, conejo
- Pescados: Salmón, trucha, bacalao, atún, sardinas, merluza, jurel, dorada
- Mariscos: Gambas, camarones, mejillones, almejas, calamar, pulpo
- Huevos: Entero, clara, yema
- Carnes rojas: Ternera, carne molida, solomillo, cerdo, jamón serrano

**Carbohidratos - Cereales (19+)**
- Arroces: Blanco, integral, basmati, salvaje
- Pastas: Blanca, integral
- Panes: Blanco, integral, centeno, molde (blanco/integral)
- Cereales: Avena, copos, harina, muesli, cuscús, polenta, maíz

**Carbohidratos - Tubérculos y Legumbres (12+)**
- Tubérculos: Patata (cocida, horno, frita), boniato, ñame, plátano verde
- Legumbres: Garbanzos, lentejas, judías (blancas, rojas), habas, guisantes

**Verduras (31+)**
- Crucíferas: Brócoli, coliflor, col, col rizada
- Hojas verdes: Espinacas, acelgas, lechuga (romana, iceberg)
- Solanáceas: Tomate (cherry), pepino, berenjena
- Raíces: Zanahoria, remolacha, nabo, rádanos
- Otras: Pimiento (rojo, verde, amarillo), cebolla, ajo, puerro, champión, espárragos, alcachofas, judías verdes

**Frutas (28+)**
- Cítricas: Naranja, mandarina, limón, pomelo
- Manzanas y peras: Manzana roja, verde, pera
- Berries: Fresas, arándanos, frambuesas, moras
- Melones: Melón, sandía
- Tropicales: Piña, mango, papaya, kiwi
- Uvas y secas: Uva blanca, roja, higo, cereza, melocotón, albaricoque, ciruela, caqui, granada, dátil, coco

**Lácteos (23+)**
- Leches: Entera, semidesnatada, desnatada, cabra, oveja, almendras, soja, coco, avena
- Yogures: Natural, griego, desnatado, kéfir
- Quesos: Cottage, fresco, cabra, cheddar, mozzarella, feta, parmesano
- Otros: Requesón, ricotta, mantequilla, crema agria

**Grasas y Frutos Secos (21+)**
- Aguacate
- Aceites: Oliva, girasol, coco, canola
- Frutos secos: Almendras, nueces, avellanas, pistachos, cacahuetes, semillas (girasol, lino, chía, calabaza)
- Mantequillas: Cacahuete, almendras
- Coco desecado

**Complementos y Suplementos (10+)**
- Proteínas: Whey, caseína, vegana
- Energéticos: Tortitas de arroz, barritas, miel, melaza
- Nutrientes: Levadura de cerveza, germen de trigo, salvado de avena

**Condimentos y Salsas (5+)**
- Salsa de tomate, soja, mostaza, mayonesa, salsa de yogur

## Migraciones

Migración: `V7__create_alimentos_and_ejercicios_table.sql`

Crea la tabla y los índices necesarios:
```sql
CREATE TABLE alimentos (...)
CREATE INDEX idx_alimentos_nombre_trgm ON alimentos USING GIN (...)
CREATE INDEX idx_alimentos_nombre ON alimentos (nombre)
CREATE INDEX idx_alimentos_marca ON alimentos (marca)
```

## Notas de Implementación

1. **Campos en camelCase:** Los DTOs usan `@JsonProperty` para serializar correctamente
2. **UUID como código:** El campo `id` se serializa como `codigo` en las respuestas
3. **Búsqueda eficiente:** Usa índices pg_trgm para búsquedas rápidas en tablas grandes
4. **Validación:** Todos los campos tienen validaciones con `@NotNull` y `@DecimalMin`
5. **Transacciones:** Operaciones críticas protegidas con `@Transactional`

## Integración con el resto de la aplicación

Este módulo es **completamente independiente** y no depende de la lógica existente en `grit/sistema/backend/`. 

Sin embargo:
- Usa `ApiResponseDTO` de GRIT para mantener consistencia
- Usa `UserPrincipal` de GRIT para la autenticación
- Los valores de los alimentos se referencian por UUID en otras entidades

## Testing

Para probar los endpoints:

```bash
# Buscar alimentos (requiere autenticación)
curl -X GET "http://localhost:8080/api/v1/alimentos?q=pollo" \
  -H "Cookie: access_token=<token>"

# Crear alimento (requiere autenticación y título en nutrición)
curl -X POST "http://localhost:8080/api/v1/alimentos" \
  -H "Content-Type: application/json" \
  -H "Cookie: access_token=<token>" \
  -d '{
    "nombre": "Tortilla de patata",
    "marca": "",
    "kcalPor100g": 185,
    "proteinasPor100g": 8.5,
    "carbsPor100g": 16.2,
    "grasasPor100g": 9.8
  }'
```
