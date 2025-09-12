# Manual General - Portal de Noticias

## Descripción del Sistema

El Portal de Noticias es una aplicación web completa desarrollada con **Spring Boot** (backend) y **React** (frontend) que automatiza la recolección, procesamiento y presentación de noticias de múltiples fuentes web. El sistema incluye capacidades de web scraping, gestión administrativa y una interfaz intuitiva para la consulta de información.

## Arquitectura del Sistema

### Componentes Principales

#### Backend (Spring Boot)
- **Framework**: Spring Boot 3.x con Java 17
- **Base de Datos**: PostgreSQL con JPA/Hibernate
- **Web Scraping**: JSoup para HTML y Rome para RSS
- **APIs REST**: Endpoints completos para noticias y administración
- **Programación**: Tareas automáticas de scraping con Spring Scheduler

#### Frontend (React)
- **Framework**: React 18 con hooks modernos
- **Estilos**: Tailwind CSS para diseño responsivo
- **Estado**: React state management nativo
- **Navegación**: SPA (Single Page Application)
- **Componentes**: Arquitectura modular reutilizable

#### Base de Datos
- **Motor**: PostgreSQL
- **Tablas Principales**:
  - `news`: Almacena las noticias extraídas
  - `news_sources`: Configuración de fuentes de scraping
- **Índices**: Optimizados para búsquedas y filtros
- **Integridad**: Constraints y validaciones

## Funcionalidades Principales

### Para Usuarios Finales

#### Portal de Noticias
- **Visualización**: Grid responsivo de tarjetas de noticias
- **Búsqueda**: Por palabras clave en título, contenido y resumen
- **Filtros**: Por fuente, categoría y fecha
- **Categorización**: Noticias populares, recientes y todas
- **Modal Detalle**: Vista completa de cada noticia
- **Paginación**: Navegación eficiente entre resultados

#### Diversidad de Fuentes
- **Algoritmo Inteligente**: Mezcla automática de diferentes fuentes
- **Evita Monopolio**: Impide que una sola fuente domine los resultados
- **Balanceado**: Distribución equitativa por popularidad y fecha

### Para Administradores

#### Dashboard Administrativo
- **Estadísticas en Tiempo Real**: Total de noticias, diarias, semanales
- **Control de Scraping**: Ejecución manual y automática
- **Monitoreo**: Estado de fuentes y rendimiento
- **Métricas**: Top fuentes por productividad

#### Gestión de Fuentes
- **CRUD Completo**: Crear, leer, actualizar, eliminar fuentes
- **Tipos Soportados**: RSS feeds y páginas HTML
- **Configuración Avanzada**: Selectores CSS personalizados
- **Testing**: Prueba de configuraciones antes de guardar
- **Debug**: Herramientas para ajustar selectores

## Tecnologías y Dependencias

### Backend
```xml
<!-- Principales dependencias -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
</dependency>
<dependency>
    <groupId>org.jsoup</groupId>
    <artifactId>jsoup</artifactId>
</dependency>
<dependency>
    <groupId>com.rometools</groupId>
    <artifactId>rome</artifactId>
</dependency>
```

### Frontend
```json
{
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "tailwindcss": "^3.3.0",
    "@headlessui/react": "^1.7.0"
  }
}
```

## Flujo de Trabajo del Sistema

### Proceso de Scraping
1. **Configuración**: Admin configura fuentes con selectores CSS
2. **Programación**: Sistema ejecuta scraping cada X minutos
3. **Extracción**: JSoup/Rome extrae datos según configuración
4. **Limpieza**: Validación y normalización de datos
5. **Almacenamiento**: Guardar en BD evitando duplicados
6. **Indexación**: Actualización de índices para búsquedas

### Flujo de Usuario
1. **Acceso**: Usuario ingresa al portal
2. **Filtrado**: Aplica filtros según necesidad
3. **Búsqueda**: Utiliza barra de búsqueda si es necesario
4. **Navegación**: Explora resultados con paginación
5. **Lectura**: Ve detalles en modal o visita fuente original

## Configuración y Personalización

### Configuración de Fuentes

#### RSS Feeds
```json
{
  "name": "Fuente RSS",
  "baseUrl": "https://sitio.com/feed.xml",
  "newsListSelector": "RSS_FEED",
  "titleSelector": "title",
  "summarySelector": "description",
  "imageSelector": "img",
  "authorSelector": "author",
  "dateSelector": "pubDate",
  "categorySelector": "category"
}
```

#### Sitios HTML
```json
{
  "name": "Sitio Web",
  "baseUrl": "https://sitio.com/noticias",
  "newsListSelector": "article.noticia",
  "titleSelector": "h2 a",
  "summarySelector": ".resumen",
  "imageSelector": ".imagen img",
  "authorSelector": ".autor",
  "dateSelector": ".fecha",
  "categorySelector": ".categoria"
}
```

### Parámetros del Sistema

#### Scraping
```properties
# Configuración de scraping
scraping.enabled=true
scraping.interval.minutes=30
scraping.timeout.seconds=30
scraping.max.retries=3
scraping.user-agent=Mozilla/5.0...

# Diversidad de fuentes
news.diversity.enabled=true
news.sources.max.per.source=10
```

#### Rendimiento
```properties
# Pool de conexiones
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5

# Cache
spring.cache.type=simple
spring.cache.cache-names=news,sources,categories
```

## Mantenimiento y Administración

### Tareas Regulares

#### Diarias
- Verificar logs de scraping
- Revisar fuentes con errores
- Comprobar estadísticas de rendimiento

#### Semanales
- Limpiar logs antiguos
- Revisar uso de disco y memoria
- Actualizar configuraciones de fuentes problemáticas

#### Mensuales
- Backup completo de base de datos
- Análisis de tendencias de tráfico
- Evaluación de nuevas fuentes de noticias

### Monitoreo

#### Métricas Clave
- **Uptime**: Disponibilidad del sistema
- **Response Time**: Tiempo de respuesta de APIs
- **Scraping Success Rate**: Porcentaje de scraping exitoso
- **Database Size**: Crecimiento de la base de datos
- **Error Rate**: Frecuencia de errores

#### Alertas Recomendadas
- Caída del servicio por más de 5 minutos
- Tasa de error superior al 5%
- Fuentes sin scraping exitoso por más de 2 horas
- Uso de disco superior al 80%

## Seguridad

### Medidas Implementadas

#### Backend
- Validación de entrada en todos los endpoints
- Sanitización de datos de scraping
- Rate limiting para APIs
- Headers de seguridad HTTP

#### Base de Datos
- Conexiones encriptadas
- Usuario con permisos mínimos
- Backups encriptados
- Logs de acceso

#### Frontend
- Validación del lado cliente
- Sanitización de HTML
- HTTPS obligatorio en producción
- CSP (Content Security Policy)

### Consideraciones de Seguridad

#### Web Scraping
- Respeto por robots.txt
- User-agent identificable
- Rate limiting para no sobrecargar sitios
- Manejo responsable de errores

## Escalabilidad y Rendimiento

### Optimizaciones Actuales

#### Base de Datos
- Índices optimizados para consultas frecuentes
- Paginación eficiente
- Queries optimizadas con JPA

#### Backend
- Pool de conexiones configurado
- Cache de consultas frecuentes
- Compresión GZIP
- Async processing para scraping

#### Frontend
- Lazy loading de imágenes
- Componentes optimizados
- Bundle splitting
- CDN para assets estáticos

### Escalabilidad Futura

#### Horizontal
- Load balancers para múltiples instancias
- Database clustering
- CDN para distribución global
- Microservicios especializados

#### Vertical
- Aumentar recursos del servidor
- Optimización de memoria JVM
- SSD para base de datos
- Cache distribuido (Redis)

## Casos de Uso

### Medios de Comunicación
- Agregador de noticias de competencia
- Monitoreo de cobertura mediática
- Análisis de tendencias informativas

### Empresas
- Seguimiento de noticias del sector
- Monitoreo de reputación
- Intelligence competitivo

### Investigación
- Análisis de contenido mediático
- Estudios de comunicación
- Seguimiento de temas específicos

### Organizaciones
- Monitoreo de noticias relevantes
- Alertas temáticas
- Archivo digital de información

## Limitaciones y Consideraciones

### Técnicas
- Dependiente de la estructura HTML de fuentes
- Sitios con JavaScript intensivo pueden fallar
- Rate limiting puede afectar frecuencia de scraping
- Sitios con anti-bot pueden bloquear acceso

### Legales
- Respetar términos de uso de sitios web
- Considerar derechos de autor
- Cumplir con regulaciones de datos
- Uso ético del web scraping

### Operacionales
- Mantenimiento constante de selectores CSS
- Monitoreo de fuentes que cambian estructura
- Gestión de crecimiento de base de datos
- Backup y recuperación de datos

## Roadmap de Desarrollo

### Versión Actual (v2.0)
- Portal de noticias completo
- Gestión de fuentes
- Dashboard administrativo
- Web scraping automatizado

### Próximas Versiones

#### v2.1
- Sistema de notificaciones
- API pública con documentación
- Filtros avanzados por fecha
- Exportación de datos

#### v2.2
- Análisis de sentimientos
- Detección de noticias duplicadas
- Sistema de trending topics
- Mobile app (React Native)

#### v3.0
- Machine Learning para categorización
- Microservicios architecture
- Real-time notifications
- Advanced analytics dashboard

## Soporte y Mantenimiento

### Documentación
- Manual de instalación
- Manual de usuario
- Manual de despliegue
- API documentation

### Soporte Técnico
- Logs detallados para debugging
- Sistema de monitoreo integrado
- Scripts de mantenimiento
- Procedimientos de backup/restore

### Comunidad
- Repository en GitHub
- Issues tracking
- Contribution guidelines
- Wiki técnica