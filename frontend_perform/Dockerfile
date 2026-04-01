# ── Stage 1: Build Angular ────────────────────────────────────────────────────
FROM node:22-alpine AS build

WORKDIR /app

# Copia package files prima del sorgente per sfruttare il layer cache
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --prefer-offline --no-audit

# Copia sorgenti e compila in modalità produzione
COPY . .
RUN npm run build -- --configuration production

# ── Stage 2: Serve con Nginx (solo static files) ───────────────────────────────
# Nota: in produzione il frontend viene servito dal container Nginx principale
# (nginx/), non da questo. Questo stage è utile per test standalone del frontend.
FROM nginx:1.27-alpine AS runtime

COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

# Config minimale per SPA (Angular router): redirige 404 → index.html
RUN printf 'server {\n\
    listen 80;\n\
    root /usr/share/nginx/html;\n\
    index index.html;\n\
    location / {\n\
        try_files $uri $uri/ /index.html;\n\
    }\n\
}\n' > /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost/ || exit 1
