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
# /etc/nginx/templates/*.template al arrancar. GATEWAY_UPSTREAM es el nombre del API
# Gateway compartido en la red buildr_shared. Sin ese enganche, ni /api ni /auth resuelven:
# ver docker-compose.dev.yml.
COPY nginx.conf.template /etc/nginx/templates/default.conf.template
ENV GATEWAY_UPSTREAM=api-gateway:8080

EXPOSE 80
