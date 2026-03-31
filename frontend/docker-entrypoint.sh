#!/bin/sh
set -e

# Sustituye solo $BACKEND_URL en el template, deja las variables de nginx intactas
envsubst '$BACKEND_URL' < /etc/nginx/nginx.conf.template > /etc/nginx/conf.d/default.conf

exec nginx -g 'daemon off;'
