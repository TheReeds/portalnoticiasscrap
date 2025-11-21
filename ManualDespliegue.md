# Manual de Despliegue: Portal de Noticias

## 1. Introducción
Este documento detalla los pasos para desplegar el Portal de Noticias en un servidor VPS (Ubuntu 20.04+) usando Gunicorn, Nginx y Docker.

## 2. Opción A: Despliegue Tradicional (VPS)

### 2.1 Preparar el Servidor
```bash
sudo apt update && sudo apt upgrade -y
sudo apt install python3-pip python3-venv nodejs npm nginx -y
```

### 2.2 Backend (Gunicorn + Systemd)
1. **Configurar entorno**:
   ```bash
   cd /var/www/portal/backend
   python3 -m venv venv
   source venv/bin/activate
   pip install -r requirements.txt gunicorn
   ```

2. **Crear servicio Systemd**:
   `/etc/systemd/system/portal-backend.service`
   ```ini
   [Unit]
   Description=Gunicorn instance to serve Portal Noticias
   After=network.target

   [Service]
   User=www-data
   Group=www-data
   WorkingDirectory=/var/www/portal/backend
   Environment="PATH=/var/www/portal/backend/venv/bin"
   ExecStart=/var/www/portal/backend/venv/bin/gunicorn -w 4 -k uvicorn.workers.UvicornWorker main:app --bind 0.0.0.0:8000

   [Install]
   WantedBy=multi-user.target
   ```

3. **Iniciar servicio**:
   ```bash
   sudo systemctl start portal-backend
   sudo systemctl enable portal-backend
   ```

### 2.3 Frontend (Build + Nginx)
1. **Construir proyecto**:
   ```bash
   cd /var/www/portal/frontend
   npm install
   npm run build
   ```

2. **Configurar Nginx**:
   `/etc/nginx/sites-available/portal`
   ```nginx
   server {
       listen 80;
       server_name midominio.com;

       location / {
           root /var/www/portal/frontend/build;
           index index.html index.htm;
           try_files $uri $uri/ /index.html;
       }

       location /api {
           proxy_pass http://127.0.0.1:8000;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
       }
   }
   ```

3. **Activar sitio**:
   ```bash
   sudo ln -s /etc/nginx/sites-available/portal /etc/nginx/sites-enabled
   sudo nginx -t
   sudo systemctl restart nginx
   ```

## 3. Opción B: Despliegue con Docker

### 3.1 Dockerfile Backend
```dockerfile
FROM python:3.9-slim
WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt
COPY . .
CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
```

### 3.2 Dockerfile Frontend
```dockerfile
FROM node:16 as build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 3.3 Docker Compose
```yaml
version: '3.8'
services:
  backend:
    build: ./backend
    ports:
      - "8000:8000"
    volumes:
      - ./backend/news.db:/app/news.db

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
```

### 3.4 Ejecutar
```bash
docker-compose up -d --build
```