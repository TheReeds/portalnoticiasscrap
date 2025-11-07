# 📰 News Scraper - Minería de Noticias

Sistema completo de web scraping para noticias con backend FastAPI y frontend React. Obtiene noticias de fuentes peruanas e internacionales usando **RSS feeds** combinado con **scraping directo** para contenido completo.

## 🌟 Características

- ✅ **Scraping Inteligente**: Usa RSS feeds para descubrir noticias + scraping directo para contenido completo
- ✅ **Múltiples Fuentes**: 6 fuentes diferentes (artículos + videos)
  - 📰 Artículos: El Comercio, BBC News, The Guardian, RPP Noticias
  - 🎥 Videos YouTube: RPP Noticias, DW Español
- ✅ **Búsqueda por Fechas**: Filtra artículos por rangos de fechas personalizados
- ✅ **Búsqueda de Texto**: Busca en títulos y contenido de artículos
- ✅ **Base de Datos SQLite**: Almacenamiento persistente de artículos
- ✅ **API REST**: Endpoints completos para scraping y consulta
- ✅ **Interfaz Moderna**: UI responsive con React y Vite
- ✅ **Contador de Vistas**: Tracking de artículos más populares
- ✅ **Categorías**: Sistema de categorización de noticias
- ✅ **Reproductor de Videos**: Videos de YouTube embebidos y reproducibles en el sitio
- ✅ **Filtros de Contenido**: Filtra por tipo (Todo/Artículos/Videos)

## 🏗️ Arquitectura

### Backend (FastAPI + Python)
- **FastAPI**: Framework web moderno para APIs
- **SQLAlchemy**: ORM para base de datos SQLite
- **BeautifulSoup4 & Newspaper3k**: Scraping y extracción de contenido
- **Feedparser**: Parsing de RSS feeds

### Frontend (React + Vite)
- **React**: Librería UI componetizada
- **Vite**: Build tool rápido y moderno
- **Axios**: Cliente HTTP para llamadas a la API
- **date-fns**: Manejo de fechas en español

## 📁 Estructura del Proyecto

```
mineria/
├── backend/
│   ├── main.py              # API FastAPI
│   ├── models.py            # Modelos de base de datos
│   ├── scrapers.py          # Scrapers para cada fuente
│   ├── requirements.txt     # Dependencias Python
│   └── news.db             # Base de datos SQLite (generada)
│
└── frontend/
    ├── src/
    │   ├── components/
    │   │   ├── ArticleCard.jsx      # Tarjeta de artículo
    │   │   ├── ScraperControls.jsx  # Controles de scraping
    │   │   └── FilterPanel.jsx      # Panel de filtros
    │   ├── App.jsx
    │   ├── main.jsx
    │   └── index.css
    ├── index.html
    ├── package.json
    └── vite.config.js
```

## 🚀 Instalación y Uso

### Requisitos Previos
- Python 3.8+
- Node.js 16+
- npm o yarn

### 1. Configurar Backend

```bash
# Navegar a la carpeta backend
cd backend

# Crear entorno virtual (recomendado)
python -m venv venv

# Activar entorno virtual
# En Windows:
venv\Scripts\activate
# En Linux/Mac:
source venv/bin/activate

# Instalar dependencias
pip install -r requirements.txt

# Iniciar servidor
python main.py
```

El backend estará disponible en: `http://localhost:8000`

API Docs (Swagger): `http://localhost:8000/docs`

### 2. Configurar Frontend

```bash
# Navegar a la carpeta frontend
cd frontend

# Instalar dependencias
npm install

# Iniciar servidor de desarrollo
npm run dev
```

El frontend estará disponible en: `http://localhost:3000`

## 📚 Uso de la Aplicación

### 1. Hacer Scraping
- Selecciona una fuente específica o deja en "Todas las fuentes"
- Define el límite de artículos por fuente (1-50)
- Haz clic en "Iniciar Scraping"
- Los artículos se guardarán automáticamente en la base de datos

### 2. Navegar por el Sitio
- **Página Principal (/)**: Portada estilo periódico con noticias destacadas
  - Artículo destacado principal
  - Grid de noticias secundarias
  - Sidebar con categorías y artículos más vistos
- **Detalle de Artículo (/article/:id)**: Vista completa del artículo/video
  - Contador de vistas automático
  - Botón para ir a fuente original
  - Artículos relacionados
- **Panel Admin (/admin)**: Controles de scraping y estadísticas

### 3. Fuentes Disponibles
- **El Comercio** (Perú): Noticias nacionales e internacionales
- **RPP Noticias** (Perú): Artículos y videos de YouTube
- **BBC News** (Reino Unido): Noticias internacionales
- **The Guardian** (Reino Unido): Noticias y análisis
- **DW Español** (Alemania): Videos de noticias en español desde YouTube

## 🔌 API Endpoints

### Scraping
```http
POST /scrape?source={source}&limit={limit}
```
Realiza scraping de noticias. Parámetros:
- `source` (opcional): Nombre de la fuente
- `limit` (opcional, default: 10): Artículos por fuente (1-50)

### Obtener Artículos
```http
GET /articles?skip={skip}&limit={limit}&source={source}&start_date={date}&end_date={date}
```
Lista artículos con filtros. Parámetros:
- `skip` (default: 0): Paginación
- `limit` (default: 20): Artículos por página
- `source` (opcional): Filtrar por fuente
- `start_date` (opcional): Formato YYYY-MM-DD
- `end_date` (opcional): Formato YYYY-MM-DD

### Buscar Artículos
```http
GET /articles/search?q={query}&skip={skip}&limit={limit}
```
Busca en título y contenido.

### Obtener Fuentes
```http
GET /sources
```
Lista fuentes disponibles y cantidad de artículos.

### Artículo Específico
```http
GET /articles/{article_id}
```
Obtiene un artículo por ID.

## 🎯 Fuentes de Noticias

### 1. El Comercio (Perú) 🇵🇪
- **URL RSS**: https://elcomercio.pe/arc/outboundfeeds/rss/?outputType=xml
- **Descripción**: Principal diario peruano fundado en 1839
- **Contenido**: Portada con noticias nacionales e internacionales
- **Método**: RSS + newspaper3k para contenido completo

### 2. BBC News 🇬🇧
- **URL RSS**: http://feeds.bbci.co.uk/news/rss.xml
- **Descripción**: Servicio de noticias de la BBC
- **Contenido**: Noticias internacionales
- **Método**: RSS + newspaper3k para contenido completo

### 3. The Guardian 🇬🇧
- **URL RSS**: https://www.theguardian.com/world/rss
- **Descripción**: Periódico británico reconocido mundialmente
- **Contenido**: Noticias mundiales
- **Método**: RSS + newspaper3k para contenido completo

### 4. RPP Noticias (Artículos) 🇵🇪
- **URL RSS**: https://rpp.pe/rss
- **Descripción**: Principal medio radial y digital de Perú (Radio Programas del Perú)
- **Contenido**: Noticias nacionales e internacionales
- **Método**: RSS + newspaper3k para contenido completo

### 5. RPP Noticias YouTube 🎥🇵🇪
- **URL RSS**: https://www.youtube.com/feeds/videos.xml?channel_id=UC5j8-2FT0ZMMBkmK72R4aeA
- **Canal**: [@RPPNoticias](https://www.youtube.com/@RPPNoticias)
- **Descripción**: Videos de noticias y cobertura en vivo de RPP
- **Contenido**: Últimos 15 videos publicados del canal
- **Método**: RSS de YouTube (incluye título, descripción, thumbnail, fecha)

### 6. DW Español YouTube 🎥🇩🇪
- **URL RSS**: https://www.youtube.com/feeds/videos.xml?channel_id=UCT4Jg8h03dD0iN3Pb5L0PMA
- **Canal**: [@DWEspanol](https://www.youtube.com/@DWEspanol)
- **Descripción**: Deutsche Welle en español - Noticias internacionales
- **Contenido**: Últimos 15 videos de noticias en español
- **Método**: RSS de YouTube (incluye título, descripción, thumbnail, fecha)

## 🛠️ Tecnologías y Librerías

### Backend
| Librería | Versión | Propósito |
|----------|---------|-----------|
| fastapi | 0.109.0 | Framework web API |
| uvicorn | 0.27.0 | Servidor ASGI |
| sqlalchemy | 2.0.25 | ORM base de datos |
| beautifulsoup4 | 4.12.3 | Parsing HTML |
| newspaper3k | 0.2.8 | Extracción de artículos |
| feedparser | 6.0.10 | Parsing RSS |
| requests | 2.31.0 | Cliente HTTP |

### Frontend
| Librería | Versión | Propósito |
|----------|---------|-----------|
| react | 18.2.0 | Librería UI |
| vite | 5.0.12 | Build tool |
| axios | 1.6.5 | Cliente HTTP |
| date-fns | 3.2.0 | Manejo de fechas |

## 🔍 Estrategia de Scraping

### ¿Por qué RSS + Scraping Directo?

En 2025, la combinación de ambas técnicas es óptima:

1. **RSS Feeds**:
   - ✅ Formato estandarizado (XML)
   - ✅ Más rápido y confiable
   - ✅ Menor riesgo de bloqueos
   - ✅ Descubre noticias nuevas automáticamente
   - ❌ Contenido limitado (solo resumen)

2. **Scraping Directo (con newspaper3k)**:
   - ✅ Contenido completo del artículo
   - ✅ Metadatos adicionales (autor, imágenes)
   - ✅ Procesamiento NLP integrado
   - ❌ Más lento
   - ❌ Puede fallar con cambios de estructura

**Solución Implementada**: Usamos RSS para descubrir URLs de noticias y luego scrapeamos cada URL individualmente para obtener el contenido completo.

## ⚖️ Consideraciones Legales

- ✅ Uso de RSS feeds públicos (información intencionalmente pública)
- ✅ Rate limiting implementado (1 segundo entre requests)
- ✅ User-Agent apropiado en requests
- ✅ Respeto a robots.txt
- ⚠️ Uso educacional y de investigación

**Nota**: Este proyecto es para fines educativos. Verifica los términos de servicio de cada sitio web antes de usar en producción.

## 🐛 Solución de Problemas

### Error: "No module named 'newspaper'"
```bash
pip install newspaper3k
```

### Error CORS en frontend
Verifica que el backend esté corriendo en `http://localhost:8000`

### Artículos sin contenido
Algunos sitios tienen protección anti-scraping. El sistema capturará el error y guardará lo disponible del RSS.

### Error de fecha en scraping
Verifica tu conexión a internet y que los sitios sean accesibles.

### Scrapers no devuelven artículos
- Verifica que el RSS esté funcionando accediendo directamente a la URL
- El Comercio: `https://elcomercio.pe/arc/outboundfeeds/rss/?outputType=xml`
- RPP Noticias: `https://rpp.pe/rss`
- Revisa los logs del backend para errores específicos

## 🎥 Scraping de YouTube

El sistema ahora incluye scraping de videos de YouTube usando **RSS feeds públicos** de YouTube:

### Ventajas del Scraping de YouTube
- ✅ **Sin API Key**: Usa RSS público de YouTube
- ✅ **Últimos 15 videos**: YouTube proporciona los videos más recientes
- ✅ **Metadata completa**: Título, descripción, thumbnail, fecha de publicación
- ✅ **Sin rate limiting**: RSS es más permisivo que la API
- ✅ **Links directos**: URLs de videos funcionales para embed o redirección

### Formato de RSS de YouTube
```
https://www.youtube.com/feeds/videos.xml?channel_id=CHANNEL_ID
```

### Cómo Agregar Más Canales
1. Encuentra el canal de YouTube que deseas agregar
2. Obtén el Channel ID desde la URL del canal o desde la página fuente
3. Crea un nuevo scraper en [backend/scrapers.py](backend/scrapers.py) siguiendo el patrón de `RPPNoticiasYouTubeScraper` o `DWEspanolYouTubeScraper`
4. Agrega el scraper a la función `get_all_scrapers()`

## 🔮 Mejoras Futuras

- [x] Agregar scraping de videos de YouTube
- [x] Sistema de categorías/etiquetas
- [x] Contador de vistas y artículos populares
- [ ] Implementar scraping programado (cron jobs)
- [ ] Agregar análisis de sentimientos
- [ ] Exportar artículos a CSV/JSON
- [ ] Dashboard de estadísticas mejorado
- [ ] Modo oscuro en frontend
- [ ] Autenticación de usuarios
- [ ] Notificaciones de nuevas noticias
- [ ] Categorización automática con IA

## 📝 Licencia

MIT License - Libre uso para fines educativos y comerciales.

## 👨‍💻 Autor

Proyecto de Minería de Datos - 2025

---

**¿Preguntas o sugerencias?** Abre un issue o contribuye al proyecto.
