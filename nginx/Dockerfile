# ── Stage 1: Build Angular ────────────────────────────────────────────────────
FROM node:22-alpine AS frontend-build

WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci --prefer-offline --no-audit

COPY frontend/ .
RUN npm run build -- --configuration production

# ── Stage 2: Nginx con frontend + config ──────────────────────────────────────
FROM nginx:1.27-alpine

# Rimuove la config default di Nginx
RUN rm /etc/nginx/conf.d/default.conf

# Copia la config personalizzata (include strip X-Internal-Token)
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# Copia il frontend compilato
COPY --from=frontend-build /app/frontend/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80 443

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost/nginx-health || exit 1
