# ── Stage 1: Build ──
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build
# Verificamos la ruta real en los logs (grit-landing)
RUN ls -R /app/dist/grit-landing

# ── Stage 2: Serve ──
FROM nginx:alpine
RUN apk add --no-cache gettext

# 1. Limpieza absoluta
RUN rm -rf /usr/share/nginx/html/*

# 2. Copiamos el contenido de browser
COPY --from=build /app/dist/grit-landing/browser/ /usr/share/nginx/html/

# 3. EL TRUCO: Si el archivo no está, lo traemos con otro nombre y luego lo renombramos
# Esto fuerza a Docker a tratarlo como un archivo nuevo
COPY --from=build /app/dist/grit-landing/browser/index.html /usr/share/nginx/html/archivo_temporal.html
RUN mv /usr/share/nginx/html/archivo_temporal.html /usr/share/nginx/html/index.html

# 4. Verificación de seguridad (El build fallará si no se logra)
RUN ls -la /usr/share/nginx/html/index.html

RUN chmod -R 755 /usr/share/nginx/html


COPY nginx.conf.template /etc/nginx/nginx.conf.template
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

ENV PORT=80
EXPOSE 80

ENTRYPOINT ["/docker-entrypoint.sh"]
