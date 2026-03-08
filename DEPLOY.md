# Deploy in produzione — AIA Management System

## Architettura

```
git push → GitHub Actions → ghcr.io (registry) → VPS Hetzner → Docker Compose
                                                        ↓
                                               Nginx (:80/:443)
                                               ├── Angular (static)
                                               └── /api/ → Spring Boot (:8080)
```

---

## 1. Crea il VPS (una tantum)

Consigliato: **Hetzner CX22** (~4€/mese, datacenter EU)
- 2 vCPU, 4GB RAM, 40GB SSD
- OS: Ubuntu 24.04 LTS
- Abilita firewall: apri solo porte 22, 80, 443

---

## 2. Configura il VPS (una tantum)

SSH sul server e installa Docker:

```bash
# Aggiorna il sistema
apt update && apt upgrade -y

# Installa Docker
curl -fsSL https://get.docker.com | sh

# Aggiungi il tuo utente al gruppo docker (evita sudo ogni volta)
usermod -aG docker $USER

# Crea la cartella del progetto
mkdir -p /opt/aia

# Crea il file .env con i segreti reali
nano /opt/aia/.env
```

Contenuto di `/opt/aia/.env` (valori reali, non di esempio):
```bash
JWT_SECRET=$(openssl rand -hex 64)
AIA_INTERNAL_SECRET=$(openssl rand -hex 32)
DB_PASSWORD=$(openssl rand -hex 16)
DB_USERNAME=aia
HTTP_PORT=80
HTTPS_PORT=443
NOTIFICATIONS_ENABLED=false
```

---

## 3. Configura GitHub (una tantum)

### 3a. Rendi il repository compatibile con ghcr.io

Nel repository GitHub → **Settings → Packages** → assicurati che
"Improve container support" sia abilitato.

### 3b. Aggiungi i Secrets del repository

**Settings → Secrets and variables → Actions → New repository secret:**

| Nome | Valore |
|------|--------|
| `VPS_HOST` | IP del tuo VPS (es. `65.21.xxx.xxx`) |
| `VPS_USER` | Utente SSH (es. `root` o `ubuntu`) |
| `VPS_SSH_KEY` | Chiave privata SSH (contenuto di `~/.ssh/id_rsa`) |

> **Come generare la chiave SSH per GitHub Actions:**
> ```bash
> ssh-keygen -t ed25519 -C "github-actions-aia" -f ~/.ssh/aia_deploy
> # Copia la chiave pubblica sul VPS:
> ssh-copy-id -i ~/.ssh/aia_deploy.pub root@IP_VPS
> # Incolla il contenuto di ~/.ssh/aia_deploy (privata) nel secret VPS_SSH_KEY
> ```

### 3c. Abilita GitHub Container Registry

Il `GITHUB_TOKEN` automatico ha già i permessi per push su `ghcr.io`
grazie a `permissions: packages: write` nel workflow. Non serve configurare nulla.

---

## 4. Primo deploy

```bash
git add .
git commit -m "setup: docker + CI/CD"
git push origin main
```

GitHub Actions partirà automaticamente. Puoi seguirlo in:
**repository → Actions → Build & Deploy**

Durata stimata primo build: ~5-8 minuti (dipendenze Maven + npm).
Build successivi: ~2-3 minuti (grazie alla cache dei layer Docker).

---

## 5. SSL con Let's Encrypt (consigliato)

Sul VPS, dopo il primo deploy:

```bash
# Installa certbot
apt install -y certbot python3-certbot-nginx

# Ottieni il certificato (sostituire con il tuo dominio)
certbot --nginx -d tuodominio.it -d www.tuodominio.it

# Rinnovo automatico (certbot lo configura da solo con systemd)
systemctl status certbot.timer
```

Poi decommenta il blocco HTTPS in `nginx/nginx.conf` e fai un nuovo push.

---

## 6. Comandi utili sul VPS

```bash
# Stato dei container
docker compose -f /opt/aia/docker-compose.yml ps

# Log in tempo reale
docker compose -f /opt/aia/docker-compose.yml logs -f

# Log solo backend
docker compose -f /opt/aia/docker-compose.yml logs -f backend

# Riavvio manuale
docker compose -f /opt/aia/docker-compose.yml restart

# Backup del database H2
docker run --rm \
  -v aia_aia-db-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/aia-db-$(date +%Y%m%d).tar.gz /data

# Rollback a una versione precedente (usa il git SHA del commit)
IMAGE_BACKEND=ghcr.io/TUOUSERNAME/aia-backend:SHA_COMMIT \
IMAGE_NGINX=ghcr.io/TUOUSERNAME/aia-nginx:SHA_COMMIT \
docker compose -f /opt/aia/docker-compose.yml up -d
```

---

## 7. Costi riepilogo

| Voce | Costo |
|------|-------|
| VPS Hetzner CX22 | ~4€/mese |
| Dominio .it | ~1€/mese |
| SSL (Let's Encrypt) | Gratuito |
| GitHub Actions | Gratuito (2000 min/mese) |
| GitHub Container Registry | Gratuito |
| **Totale** | **~5€/mese** |
