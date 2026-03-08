# AIA Management System - Deployment Guide

## Panoramica

L'applicazione supporta due profili di configurazione:
- **dev**: Sviluppo con database H2 in-memory (default)
- **prod**: Produzione con PostgreSQL

## 🚀 Deployment Produzione (PostgreSQL)

### Prerequisiti

- Java 21+ installato
- PostgreSQL 14+ installato e in esecuzione
- Maven 3.8+ (per build)

### 1. Setup Database PostgreSQL

#### Installazione PostgreSQL (Ubuntu/Debian)

```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

#### Creazione Database

```bash
# Accedi come utente postgres
sudo -u postgres psql

# Crea database e utente
CREATE DATABASE aia_management;
CREATE USER aia_user WITH ENCRYPTED PASSWORD 'your_secure_password_here';
GRANT ALL PRIVILEGES ON DATABASE aia_management TO aia_user;
\q
```

#### Inizializzazione Schema

```bash
# Esegui lo script di inizializzazione
psql -U aia_user -d aia_management -f src/main/resources/db/init-postgresql.sql
```

**IMPORTANTE**: Cambia la password dell'utente admin di default dopo il primo accesso!

### 2. Configurazione Applicazione

#### Variabili d'Ambiente (Raccomandato)

Crea un file `.env` o configura le variabili d'ambiente:

```bash
# Database
export DATABASE_URL=jdbc:postgresql://localhost:5432/aia_management
export DATABASE_USERNAME=aia_user
export DATABASE_PASSWORD=your_secure_password_here

# JWT - OBBLIGATORIO cambiare in produzione!
export JWT_SECRET=your-unique-secret-key-minimum-32-characters-long-change-this

# File Storage
export FILE_UPLOAD_DIR=/var/aia-management/uploads

# Email (opzionale)
export NOTIFICATIONS_ENABLED=true
export SMTP_HOST=smtp.gmail.com
export SMTP_PORT=587
export SMTP_USERNAME=your-email@gmail.com
export SMTP_PASSWORD=your-app-password
export SMTP_FROM=noreply@grandimolini.it

# Server
export SERVER_PORT=8080
```

#### Creazione Directory Upload

```bash
sudo mkdir -p /var/aia-management/uploads
sudo chown -R $USER:$USER /var/aia-management
chmod 755 /var/aia-management/uploads
```

### 3. Build Applicazione

```bash
# Build con Maven
mvn clean package -DskipTests

# Il JAR verrà creato in target/aia-management-1.0.0.jar
```

### 4. Avvio Applicazione

#### Modalità Diretta

```bash
# Con profilo prod
java -jar target/aia-management-1.0.0.jar --spring.profiles.active=prod

# Oppure usando variabile d'ambiente
export SPRING_PROFILES_ACTIVE=prod
java -jar target/aia-management-1.0.0.jar
```

#### Modalità Systemd Service (Raccomandato)

Crea file `/etc/systemd/system/aia-management.service`:

```ini
[Unit]
Description=AIA Management System
After=syslog.target network.target postgresql.service

[Service]
User=aia-app
Group=aia-app
WorkingDirectory=/opt/aia-management

# Variabili d'ambiente
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="DATABASE_URL=jdbc:postgresql://localhost:5432/aia_management"
Environment="DATABASE_USERNAME=aia_user"
Environment="DATABASE_PASSWORD=your_password"
Environment="JWT_SECRET=your-jwt-secret-here"
Environment="FILE_UPLOAD_DIR=/var/aia-management/uploads"

ExecStart=/usr/bin/java -jar /opt/aia-management/aia-management-1.0.0.jar

# Restart policy
Restart=always
RestartSec=10

# Logging
StandardOutput=journal
StandardError=journal
SyslogIdentifier=aia-management

[Install]
WantedBy=multi-user.target
```

Avvia il servizio:

```bash
sudo systemctl daemon-reload
sudo systemctl enable aia-management
sudo systemctl start aia-management
sudo systemctl status aia-management
```

Visualizza i log:

```bash
sudo journalctl -u aia-management -f
```

### 5. Configurazione Reverse Proxy (Nginx)

Esempio configurazione Nginx:

```nginx
server {
    listen 80;
    server_name aia.grandimolini.it;

    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name aia.grandimolini.it;

    # SSL Configuration (usa Let's Encrypt o certificato aziendale)
    ssl_certificate /etc/ssl/certs/aia.grandimolini.it.crt;
    ssl_certificate_key /etc/ssl/private/aia.grandimolini.it.key;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header X-Content-Type-Options "nosniff" always;

    # Max upload size (50MB)
    client_max_body_size 50M;

    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        # Timeouts per upload file
        proxy_connect_timeout 600;
        proxy_send_timeout 600;
        proxy_read_timeout 600;
        send_timeout 600;
    }
}
```

Testa e riavvia Nginx:

```bash
sudo nginx -t
sudo systemctl reload nginx
```

## 🔧 Sviluppo Locale

### Modalità Sviluppo (H2)

```bash
# Avvio automatico con profilo dev (default)
mvn spring-boot:run

# L'applicazione partirà su http://localhost:8080
# H2 Console disponibile su http://localhost:8080/h2-console
```

### Test con PostgreSQL in Locale

```bash
# Usa profilo prod con PostgreSQL locale
export SPRING_PROFILES_ACTIVE=prod
export DATABASE_URL=jdbc:postgresql://localhost:5432/aia_management_dev
export DATABASE_USERNAME=postgres
export DATABASE_PASSWORD=postgres

mvn spring-boot:run
```

## 📊 Monitoraggio

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

### Logs PostgreSQL

```bash
# Ubuntu/Debian
sudo tail -f /var/log/postgresql/postgresql-14-main.log

# Visualizza query lente
psql -U aia_user -d aia_management -c "SELECT * FROM pg_stat_activity WHERE state != 'idle';"
```

## 🔐 Sicurezza

### Checklist Deployment Produzione

- [ ] Cambiare password utente admin di default
- [ ] Configurare JWT_SECRET univoco (min 32 caratteri)
- [ ] Utilizzare HTTPS con certificato valido
- [ ] Configurare firewall (aprire solo porte necessarie)
- [ ] Backup regolare database PostgreSQL
- [ ] Monitoring errori applicazione
- [ ] Limitare accesso database solo da localhost (o IP specifici)
- [ ] Configurare log rotation
- [ ] Disabilitare H2 console in produzione (già fatto)
- [ ] Validare configurazione CORS in SecurityConfig

### Backup Database

```bash
# Backup completo
pg_dump -U aia_user -d aia_management -F c -f aia_backup_$(date +%Y%m%d).dump

# Restore
pg_restore -U aia_user -d aia_management -c aia_backup_20250208.dump
```

## 🐛 Troubleshooting

### Problema: Applicazione non si connette a PostgreSQL

```bash
# Verifica che PostgreSQL sia in esecuzione
sudo systemctl status postgresql

# Verifica connessione
psql -U aia_user -d aia_management -c "SELECT version();"

# Controlla logs applicazione
journalctl -u aia-management -n 100
```

### Problema: Errore "JWT secret too short"

Assicurati che JWT_SECRET sia almeno 32 caratteri:

```bash
export JWT_SECRET=$(openssl rand -base64 32)
```

### Problema: Upload file fallisce

```bash
# Verifica permessi directory
ls -la /var/aia-management/uploads

# Verifica spazio disco
df -h

# Verifica limite Nginx (se usato)
grep client_max_body_size /etc/nginx/nginx.conf
```

## 📞 Supporto

Per assistenza tecnica:
- Email: support@grandimolini.it
- Documentazione API: http://localhost:8080/swagger-ui.html (se abilitato)

---

**Versione**: 1.0.0
**Ultimo aggiornamento**: Febbraio 2025
