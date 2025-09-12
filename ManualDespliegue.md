# Manual de Despliegue - Portal de Noticias

## Opciones de Despliegue

### 1. Despliegue en Servidor VPS/Cloud
### 2. Containerización con Docker
### 3. Despliegue en Servicios Cloud (AWS, Azure, GCP)

## Preparación para Producción

### Configuración Backend

#### application-prod.properties
```properties
# Configuración de producción
spring.profiles.active=prod

# Base de datos
spring.datasource.url=jdbc:postgresql://tu-servidor:5432/news_portal_prod
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JPA optimizado para producción
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Logging
logging.level.com.webscrap.portalnoticias=INFO
logging.level.root=WARN
logging.file.name=/var/log/news-portal/app.log
logging.file.max-size=10MB
logging.file.max-history=30

# Pool de conexiones optimizado
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000

# Configuración del servidor
server.port=8080
server.servlet.context-path=/api
server.compression.enabled=true
server.compression.mime-types=text/html,text/xml,text/plain,text/css,text/javascript,application/javascript,application/json

# CORS para producción
cors.allowed-origins=https://tu-dominio.com,https://www.tu-dominio.com

# Scraping optimizado
scraping.enabled=true
scraping.interval.minutes=30
scraping.timeout.seconds=45
```

### Configuración Frontend

#### Variables de Entorno (.env.production)
```env
REACT_APP_API_BASE_URL=https://api.tu-dominio.com/api
REACT_APP_ENVIRONMENT=production
GENERATE_SOURCEMAP=false
```

#### Optimización del Build
```bash
# Build optimizado
npm run build

# Verificar tamaño del bundle
npm run build -- --analyze
```

## Opción 1: VPS/Servidor Cloud

### Requisitos del Servidor
- **CPU**: 2 vCPUs mínimo
- **RAM**: 4GB mínimo (8GB recomendado)
- **Storage**: 50GB SSD mínimo
- **SO**: Ubuntu 20.04+ / CentOS 8+
- **Ancho de banda**: Ilimitado

### Preparación del Servidor

#### Actualizar Sistema
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install ufw fail2ban nginx certbot python3-certbot-nginx -y
```

#### Instalar Java 17
```bash
sudo apt install openjdk-17-jdk -y
java -version
```

#### Instalar PostgreSQL
```bash
sudo apt install postgresql postgresql-contrib -y
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Configurar base de datos
sudo -u postgres psql
CREATE DATABASE news_portal_prod;
CREATE USER news_user WITH ENCRYPTED PASSWORD 'password_seguro_aqui';
GRANT ALL PRIVILEGES ON DATABASE news_portal_prod TO news_user;
\q
```

#### Instalar Node.js
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install nodejs -y
node --version
npm --version
```

### Despliegue Backend

#### Crear Usuario de Sistema
```bash
sudo useradd -m -d /opt/news-portal news-portal
sudo mkdir -p /opt/news-portal/backend
sudo chown -R news-portal:news-portal /opt/news-portal
```

#### Subir Aplicación
```bash
# En tu máquina local
mvn clean package -Pprod
scp target/portalnoticias-*.jar usuario@servidor:/opt/news-portal/backend/app.jar

# En el servidor
sudo chown news-portal:news-portal /opt/news-portal/backend/app.jar
sudo chmod +x /opt/news-portal/backend/app.jar
```

#### Crear Servicio Systemd
```bash
sudo nano /etc/systemd/system/news-portal-backend.service
```

```ini
[Unit]
Description=News Portal Backend
After=syslog.target network.target

[Service]
Type=simple
User=news-portal
WorkingDirectory=/opt/news-portal/backend
ExecStart=/usr/bin/java -Xms1g -Xmx2g -jar app.jar --spring.profiles.active=prod
SuccessExitStatus=143
Restart=on-failure
RestartSec=10

Environment=DB_USERNAME=news_user
Environment=DB_PASSWORD=password_seguro_aqui

[Install]
WantedBy=multi-user.target
```

#### Iniciar Servicio
```bash
sudo systemctl daemon-reload
sudo systemctl enable news-portal-backend
sudo systemctl start news-portal-backend
sudo systemctl status news-portal-backend
```

### Despliegue Frontend

#### Build y Deploy
```bash
# En tu máquina local
npm run build

# Subir archivos
scp -r build/* usuario@servidor:/var/www/news-portal/
```

#### Configurar Nginx
```bash
sudo nano /etc/nginx/sites-available/news-portal
```

```nginx
server {
    listen 80;
    server_name tu-dominio.com www.tu-dominio.com;

    # Frontend
    location / {
        root /var/www/news-portal;
        index index.html index.htm;
        try_files $uri $uri/ /index.html;
    }

    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Gzip compression
    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types
        text/plain
        text/css
        text/xml
        text/javascript
        application/javascript
        application/xml+rss
        application/json;
}
```

#### Activar Sitio
```bash
sudo ln -s /etc/nginx/sites-available/news-portal /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

#### SSL con Let's Encrypt
```bash
sudo certbot --nginx -d tu-dominio.com -d www.tu-dominio.com
```

### Configurar Firewall
```bash
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw enable
```

## Opción 2: Docker

### Dockerfile Backend
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/portalnoticias-*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-jar", "app.jar"]
```

### Dockerfile Frontend
```dockerfile
FROM node:18-alpine as builder

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=builder /app/build /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### docker-compose.yml
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: news_portal_prod
      POSTGRES_USER: news_user
      POSTGRES_PASSWORD: password_seguro
    volumes:
      - postgres_data:/var/lib/postgresql/data
    networks:
      - news-network

  backend:
    build: ./backend
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_USERNAME=news_user
      - DB_PASSWORD=password_seguro
      - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/news_portal_prod
    depends_on:
      - postgres
    networks:
      - news-network
    restart: unless-stopped

  frontend:
    build: ./frontend
    ports:
      - "80:80"
      - "443:443"
    depends_on:
      - backend
    networks:
      - news-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  news-network:
    driver: bridge
```

### Desplegar con Docker
```bash
# Build y run
docker-compose build
docker-compose up -d

# Verificar
docker-compose ps
docker-compose logs -f backend
```

## Opción 3: Cloud Services

### AWS Deployment

#### RDS (PostgreSQL)
```bash
# Crear RDS PostgreSQL instance
aws rds create-db-instance \
    --db-instance-identifier news-portal-db \
    --db-instance-class db.t3.micro \
    --engine postgres \
    --engine-version 15.4 \
    --allocated-storage 20 \
    --db-name news_portal_prod \
    --master-username postgres \
    --master-user-password TuPasswordSeguro
```

#### Elastic Beanstalk (Backend)
```bash
# Inicializar EB
eb init

# Deploy
eb create production --database.engine postgres
eb deploy
```

#### S3 + CloudFront (Frontend)
```bash
# Build frontend
npm run build

# Sync to S3
aws s3 sync build/ s3://tu-bucket-name --delete

# Invalidate CloudFront
aws cloudfront create-invalidation \
    --distribution-id EDFDVBD6EXAMPLE \
    --paths "/*"
```

## Monitoreo y Mantenimiento

### Logs
```bash
# Backend logs
sudo journalctl -u news-portal-backend -f

# Nginx logs
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log

# Application logs
sudo tail -f /var/log/news-portal/app.log
```

### Backup Automatizado
```bash
# Script de backup
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
pg_dump -U news_user -h localhost news_portal_prod | gzip > /backup/news_portal_$DATE.sql.gz

# Crontab
0 2 * * * /opt/scripts/backup.sh
```

### Monitoring con Scripts
```bash
# Script health check
#!/bin/bash
curl -f http://localhost:8080/api/news/stats || systemctl restart news-portal-backend
```

### SSL Renovación Automática
```bash
# Crontab para certbot
0 12 * * * /usr/bin/certbot renew --quiet
```

## Optimizaciones de Rendimiento

### Base de Datos
```sql
-- Índices adicionales para producción
CREATE INDEX CONCURRENTLY idx_news_published_source ON news(published_date, source);
CREATE INDEX CONCURRENTLY idx_news_category_active ON news(category, is_active);

-- Vacuum automático
ALTER TABLE news SET (autovacuum_vacuum_scale_factor = 0.1);
ALTER TABLE news_sources SET (autovacuum_vacuum_scale_factor = 0.1);
```

### Nginx Caching
```nginx
# Cache para assets estáticos
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}

# Cache para API responses
location /api/news/categories {
    proxy_pass http://localhost:8080;
    proxy_cache_valid 200 1h;
    proxy_cache my_cache;
}
```

## Seguridad

### Configuraciones de Seguridad
```properties
# application-prod.properties
# Ocultar información del servidor
server.error.include-stacktrace=never
server.error.include-message=never

# Configurar HTTPS redirect
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
```

### Fail2Ban para API
```ini
# /etc/fail2ban/jail.local
[news-portal-api]
enabled = true
port = http,https
filter = news-portal-api
logpath = /var/log/nginx/access.log
maxretry = 10
bantime = 600
```

## Checklist de Despliegue

### Pre-Deploy
- [ ] Tests unitarios pasando
- [ ] Variables de entorno configuradas
- [ ] Base de datos de producción creada
- [ ] Certificados SSL configurados
- [ ] Backup de datos importante

### Post-Deploy
- [ ] Aplicación accesible desde internet
- [ ] SSL funcionando correctamente
- [ ] API endpoints respondiendo
- [ ] Scraping automático funcionando
- [ ] Logs configurados y rotando
- [ ] Monitoreo activo
- [ ] Backup programado

### Rollback Plan
```bash
# Rollback rápido
sudo systemctl stop news-portal-backend
sudo mv /opt/news-portal/backend/app.jar.backup /opt/news-portal/backend/app.jar
sudo systemctl start news-portal-backend
```