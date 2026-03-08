# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copia solo il pom.xml prima del codice sorgente per sfruttare il layer cache
# di Docker: le dipendenze Maven vengono riscaricate solo se pom.xml cambia.
COPY pom.xml .
COPY .maven /root/.m2/wrapper

# Scarica le dipendenze offline (layer separato per cache)
RUN --mount=type=cache,target=/root/.m2 \
    true  # le deps vengono risolte al build successivo con il sorgente

# Copia sorgenti ed esegui il build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -q package -DskipTests 2>/dev/null || \
    (apk add --no-cache maven && mvn -q package -DskipTests)

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

# Sicurezza: utente non-root
RUN addgroup -S aia && adduser -S aia -G aia

WORKDIR /app

# Copia il jar prodotto dallo stage di build
COPY --from=build /app/target/aia-management-*.jar app.jar

# Copia la cartella uploads (verrà sovramontata da un volume in produzione)
RUN mkdir -p uploads && chown -R aia:aia /app

USER aia

# Porta dell'applicazione Spring Boot
EXPOSE 8080

# Profilo Spring attivo: prod (usa application-prod.properties)
# Le variabili sensibili vengono iniettate via docker-compose / .env
ENV SPRING_PROFILES_ACTIVE=prod

# Health check: Spring Boot Actuator o endpoint di base
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/api/auth/health 2>/dev/null || \
        wget -qO- http://localhost:8080/actuator/health 2>/dev/null || \
        exit 1

ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", "app.jar"]
