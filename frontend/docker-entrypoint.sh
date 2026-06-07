#!/bin/sh
set -e

# 1. Borramos la configuración por defecto y el index de bienvenida
rm -f /etc/nginx/conf.d/default.conf
rm -f /usr/share/nginx/html/index.html

# 2. Inyectamos las variables.
# IMPORTANTE: Solo pasamos las variables que queremos sustituir
envsubst '$BACKEND_URL $PORT' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf

echo "--- Configuración final generada ---"
cat /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'
