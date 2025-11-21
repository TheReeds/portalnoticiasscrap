# Documentación General del Proyecto: Portal de Noticias con IA

## 1. Descripción General
Este proyecto es un portal de noticias avanzado que utiliza técnicas de Web Scraping para recolectar información de diversas fuentes (El Comercio, RPP, etc.), almacena los datos en una base de datos relacional y los presenta a través de una interfaz web moderna. Además, incorpora características de Inteligencia Artificial para el análisis de tendencias y predicción de categorías de noticias.

## 2. Arquitectura del Sistema

### 2.1 Backend (Python/FastAPI)
El backend está construido con FastAPI, ofreciendo una API RESTful de alto rendimiento.
- **Framework**: FastAPI
- **Base de Datos**: SQLite (Desarrollo) / PostgreSQL (Producción)
- **ORM**: SQLAlchemy
- **Autenticación**: JWT (JSON Web Tokens)
- **Scraping**: BeautifulSoup4, Requests
- **ML/Analytics**: Scikit-learn, Pandas, Numpy

### 2.2 Frontend (React)
El frontend es una Single Page Application (SPA) construida con React.
- **Framework**: React 18
- **Estilos**: CSS Modules / Vanilla CSS
- **Visualización de Datos**: Recharts
- **Cliente HTTP**: Axios
- **Enrutamiento**: React Router

## 3. Módulos Principales

### 3.1 Módulo de Scraping
- **Scrapers**: Clases especializadas para cada fuente de noticias.
- **Gestor de Tareas**: Ejecución programada o manual de scraping.
- **Scraping Histórico**: Búsqueda de noticias pasadas por rango de fechas.

### 3.2 Módulo de Gestión de Noticias
- **Almacenamiento**: Guardado de títulos, resúmenes, enlaces, autores y fechas.
- **API**: Endpoints para listar, filtrar y buscar noticias.
- **RSS**: Soporte para ingestión de noticias vía feeds RSS.

### 3.3 Módulo de Analítica Avanzada (Premium)
- **Clustering**: Agrupamiento automático de noticias por temas usando K-Means y TF-IDF.
- **Tendencias**: Visualización de la evolución de temas en el tiempo.
- **Dashboard**: Gráficos interactivos (Pie, Bar) para administradores.

### 3.4 Módulo de Predicción IA (Premium)
- **Clasificación**: Modelo Naive Bayes para categorizar noticias automáticamente.
- **Predicción de Tendencias**: Heurística para predecir el potencial viral de una noticia.
- **Entrenamiento**: Capacidad de reentrenar el modelo con nuevos datos.

## 4. Seguridad
- **Roles**: Usuarios (Lectura), Admin (Gestión Total), Premium (Acceso a IA/Analítica).
- **Protección**: Endpoints protegidos con dependencias de seguridad OAuth2.

## 5. Despliegue
- **Contenedores**: Docker y Docker Compose para orquestación.
- **Servidor Web**: Nginx como proxy reverso.
- **Servidor de Aplicaciones**: Gunicorn para el backend Python.