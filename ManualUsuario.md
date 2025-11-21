# Manual de Usuario: Portal de Noticias

## 1. Introducción
Bienvenido al Portal de Noticias con IA. Este manual le guiará en el uso de las funcionalidades del sistema, desde la visualización de noticias hasta las herramientas avanzadas de análisis para usuarios Premium.

## 2. Acceso al Sistema
- **URL**: Ingrese a `http://localhost:3000` (o la URL de producción).
- **Registro**: Haga clic en "Registrarse" para crear una cuenta.
- **Login**: Use sus credenciales para acceder.

## 3. Funcionalidades Básicas (Todos los Usuarios)
### 3.1 Página de Inicio
- **Listado de Noticias**: Vea las últimas noticias agregadas.
- **Filtros**: Filtre por categoría (Política, Deportes, etc.) o fuente.
- **Búsqueda**: Use la barra de búsqueda para encontrar temas específicos.
- **Videos**: Seleccione el filtro "Videos" para ver contenido multimedia de YouTube.

### 3.2 Lectura de Noticias
- Haga clic en "Leer más" para ir a la fuente original de la noticia.

## 4. Funcionalidades de Administrador
Acceda al "Panel Admin" desde la barra de navegación.

### 4.1 Dashboard
- Vea estadísticas generales: Total de noticias, usuarios registrados, fuentes activas.

### 4.2 Gestión de Scraping
- **Scraping Manual**: Inicie el proceso de recolección de noticias inmediatamente.
- **Scraping Histórico**: Busque noticias antiguas definiendo una palabra clave y un rango de fechas.

### 4.3 Gestión de RSS
- Agregue nuevas fuentes RSS ingresando el nombre y la URL del feed.
- Elimine fuentes obsoletas.

### 4.4 Exportación
- Descargue la base de datos de noticias en formatos CSV, JSON o Excel.

## 5. Funcionalidades Premium (Admin/Premium)
Estas funciones requieren una suscripción Premium o rol de Administrador.

### 5.1 Panel de Analítica
- **Distribución de Temas**: Gráfico circular que muestra los temas dominantes.
- **Evolución Temporal**: Gráfico de barras que muestra cómo cambian los temas en los últimos 7, 15 o 30 días.
- **Detalle de Clusters**: Vea las palabras clave y ejemplos de noticias para cada tema identificado.

### 5.2 Panel de Predicción IA
- **Analizar Texto**: Pegue el título o contenido de una noticia en el área de texto.
- **Predecir**: Obtenga la categoría sugerida y el potencial de tendencia (Baja, Media, Alta).
- **Entrenar Modelo**: Actualice el modelo de IA con las noticias más recientes de la base de datos.