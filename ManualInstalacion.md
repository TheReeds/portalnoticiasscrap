# Manual de Instalación - Portal de Noticias

## Requisitos del Sistema

### Backend (Spring Boot)
- **Java**: JDK 17 o superior
- **Maven**: 3.6+ 
- **PostgreSQL**: 12+
- **Memoria RAM**: Mínimo 2GB
- **Espacio en disco**: 500MB

### Frontend (React)
- **Node.js**: 16+ 
- **npm**: 8+ o **yarn**: 1.22+
- **Navegador**: Chrome 90+, Firefox 88+, Safari 14+

## Configuración de la Base de Datos

### 1. Instalación de PostgreSQL

#### Windows
```bash
# Descargar desde https://www.postgresql.org/download/windows/
# O usar Chocolatey
choco install postgresql
```

#### macOS
```bash
# Usando Homebrew
brew install postgresql
brew services start postgresql
```

#### Ubuntu/Debian
```bash
sudo apt update
sudo apt install postgresql postgresql-contrib
sudo systemctl start postgresql
sudo systemctl enable postgresql
```

### 2. Crear Base de Datos
```sql
-- Conectar como superusuario
psql -U postgres

-- Crear base de datos
CREATE DATABASE news_portal_db;

-- Crear usuario (opcional)
CREATE USER news_user WITH ENCRYPTED PASSWORD 'tu_password';
GRANT ALL PRIVILEGES ON DATABASE news_portal_db TO news_user;

-- Salir
\q
```

## Instalación del Backend

### 1. Clonar el Repositorio
```bash
git clone https://github.com/tu-usuario/portal-noticias.git
cd portal-noticias/backend
```

### 2. Configurar application.properties
```properties
# Editar src/main/resources/application.properties
spring.datasource.url=jdbc:postgresql://localhost:5432/news_portal_db
spring.datasource.username=postgres
spring.datasource.password=tu_password
spring.datasource.driver-class-name=org.postgresql.Driver

# Configuración JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Puerto del servidor
server.port=8090
server.servlet.context-path=/api

# CORS (ajustar según necesidad)
cors.allowed-origins=http://localhost:3000
```

### 3. Instalar Dependencias y Ejecutar
```bash
# Instalar dependencias
mvn clean install

# Ejecutar aplicación
mvn spring-boot:run

# O generar JAR y ejecutar
mvn clean package
java -jar target/portalnoticias-0.0.1-SNAPSHOT.jar
```

### 4. Verificar Instalación
- Abrir: http://localhost:8090/api
- Probar endpoint: http://localhost:8090/api/news

## Instalación del Frontend

### 1. Navegar al Directorio Frontend
```bash
cd ../frontend
# O si estás en la raíz del proyecto
cd frontend
```

### 2. Instalar Dependencias
```bash
# Con npm
npm install

# O con yarn
yarn install
```

### 3. Configurar Variables de Entorno
```bash
# Crear archivo .env en la raíz del frontend
echo "REACT_APP_API_BASE_URL=http://localhost:8090/api" > .env
```

### 4. Instalar Tailwind CSS (si no está configurado)
```bash
npm install -D tailwindcss postcss autoprefixer
npx tailwindcss init -p
```

### 5. Ejecutar Aplicación
```bash
# Modo desarrollo
npm start
# O con yarn
yarn start

# La aplicación se abrirá en http://localhost:3000
```

## Configuración de Fuentes Iniciales

### 1. Acceder al Panel de Administración
- Ir a: http://localhost:3000
- Hacer clic en "Gestionar Fuentes"

### 2. Agregar Fuente RSS (Ejemplo: Perú21)
```json
{
  "name": "Perú21",
  "baseUrl": "https://peru21.pe/feed/",
  "newsListSelector": "RSS_FEED",
  "titleSelector": "title",
  "summarySelector": "description",
  "imageSelector": "img",
  "authorSelector": "author",
  "dateSelector": "pubDate",
  "categorySelector": "category",
  "isActive": true,
  "scrapingIntervalMinutes": 30
}
```

### 3. Probar y Guardar Fuente
- Hacer clic en "Probar Configuración"
- Si funciona correctamente, hacer clic en "Crear Fuente"

### 4. Ejecutar Primer Scraping
- Ir a "Dashboard Admin"
- Hacer clic en "Ejecutar Todo el Scraping"

## Verificación de la Instalación

### 1. Comprobar Backend
```bash
# Verificar que el servidor está corriendo
curl http://localhost:8090/api/news/stats

# Debería retornar estadísticas JSON
```

### 2. Comprobar Frontend
- Abrir http://localhost:3000
- Verificar que aparecen noticias
- Probar filtros y búsqueda

### 3. Comprobar Base de Datos
```sql
-- Conectar a PostgreSQL
psql -U postgres -d news_portal_db

-- Verificar tablas creadas
\dt

-- Verificar datos
SELECT COUNT(*) FROM news;
SELECT COUNT(*) FROM news_sources;
```

## Solución de Problemas Comunes

### Backend no inicia
```bash
# Verificar Java
java -version

# Verificar conexión a BD
psql -U postgres -d news_portal_db -c "SELECT 1"

# Verificar logs
tail -f logs/news-portal.log
```

### Frontend no carga
```bash
# Limpiar cache
npm start -- --reset-cache

# Reinstalar dependencias
rm -rf node_modules package-lock.json
npm install
```

### Error de CORS
```properties
# En application.properties, agregar:
cors.allowed-origins=http://localhost:3000,http://127.0.0.1:3000
```

### PostgreSQL no conecta
```bash
# Verificar que PostgreSQL está corriendo
sudo systemctl status postgresql

# Verificar configuración
sudo -u postgres psql -c "SHOW config_file;"
```

## Configuración de Desarrollo

### 1. IDE Recomendados
- **Backend**: IntelliJ IDEA, Eclipse, VS Code
- **Frontend**: VS Code, WebStorm

### 2. Extensiones Útiles (VS Code)
```json
{
  "recommendations": [
    "vscjava.vscode-java-pack",
    "esbenp.prettier-vscode",
    "bradlc.vscode-tailwindcss",
    "ms-vscode.vscode-typescript-next"
  ]
}
```

### 3. Scripts de Desarrollo
```bash
# Crear script start-dev.sh
#!/bin/bash
echo "Iniciando backend..."
cd backend && mvn spring-boot:run &

echo "Esperando 30 segundos..."
sleep 30

echo "Iniciando frontend..."
cd frontend && npm start
```

## Próximos Pasos

1. **Configurar más fuentes de noticias**
2. **Configurar tareas programadas de scraping**
3. **Revisar logs y ajustar configuraciones**
4. **Considerar despliegue en producción**

Para continuar con el despliegue, consultar el **Manual de Despliegue**.