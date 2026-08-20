# ---- Etapa 1: Build ----
FROM node:20 AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install --legacy-peer-deps
COPY . .
RUN npm run build

# ---- Etapa 2: Servidor (Nginx) ----
FROM nginx:alpine
RUN rm -rf /usr/share/nginx/html/*
COPY --from=builder /app/dist/frezco/browser /usr/share/nginx/html

# Template en vez de config fija: el entrypoint oficial de nginx corre envsubst sobre
# /etc/nginx/templates/*.template al arrancar. BACKEND_UPSTREAM se puede sobreescribir
# por variable de entorno cuando el nombre del servicio backend no es "backend"
# (por ejemplo, al mezclar este frontend en el compose de otro stack).
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
ENV BACKEND_UPSTREAM=backend:8090

EXPOSE 80
