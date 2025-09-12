# Manual de Usuario - Portal de Noticias

## Introducción

El Portal de Noticias es una aplicación web que agrega noticias de múltiples fuentes, permitiendo a los usuarios leer, filtrar y buscar información de manera centralizada. Incluye funcionalidades de administración para gestionar las fuentes de noticias y configurar el scraping automático.

## Navegación Principal

### Barra de Navegación
- **📰 Noticias**: Vista principal con todas las noticias
- **📊 Dashboard Admin**: Panel de control administrativo
- **⚙️ Gestionar Fuentes**: Configuración de fuentes de noticias

## Sección Noticias

### Vista Principal
La página principal muestra una grilla de tarjetas de noticias con:
- **Imagen** (si está disponible)
- **Título** de la noticia
- **Resumen** breve
- **Fuente** de origen
- **Fecha** de publicación
- **Categoría**
- **Número de vistas**

### Búsqueda y Filtros

#### Barra de Búsqueda
1. Escribir términos de búsqueda en el campo superior
2. Presionar "Enter" o hacer clic en "Buscar"
3. Los resultados se filtran automáticamente

#### Pestañas de Navegación
- **Todas las Noticias**: Muestra todas las noticias disponibles
- **Más Populares**: Noticias ordenadas por número de vistas
- **Últimas 24h**: Noticias publicadas en las últimas 24 horas

#### Sidebar de Filtros
**Filtros por Fuente:**
- Clic en "Todas las fuentes" para ver todas
- Seleccionar una fuente específica para filtrar
- El indicador verde/rojo muestra si la fuente está activa

**Filtros por Categoría:**
- Clic en "Todas las categorías" para ver todas
- Seleccionar una categoría específica

**Botón "Limpiar filtros":**
- Resetea todos los filtros aplicados

### Visualización de Noticias

#### Tarjetas de Noticias
Cada tarjeta muestra:
- **Hover Effect**: La tarjeta se eleva al pasar el cursor
- **Clic**: Abre la noticia completa en un modal

#### Modal de Noticia Completa
Al hacer clic en una tarjeta se abre un modal con:
- **Título completo**
- **Imagen a tamaño completo**
- **Contenido/Resumen extendido**
- **Información del autor**
- **Fecha de publicación detallada**
- **Botón "Ver artículo completo"**: Redirige a la fuente original

**Cerrar el modal:**
- Clic en la "X"
- Presionar tecla "Escape"
- Clic fuera del modal

### Paginación
- **Botones de navegación**: Primera, Anterior, Siguiente, Última
- **Números de página**: Navegación directa
- **Información**: Muestra página actual y total de resultados

## Dashboard de Administración

### Acceso
- Clic en "📊 Dashboard Admin" en la navegación superior
- No requiere autenticación (para testing)

### Estadísticas Generales
El dashboard muestra:
- **Total Noticias**: Cantidad total en el sistema
- **Noticias Hoy**: Publicadas en el día actual
- **Esta Semana**: Noticias de los últimos 7 días
- **Fuentes Activas**: Cantidad de fuentes funcionando

### Control de Scraping
**Ejecutar Todo el Scraping:**
1. Clic en "Ejecutar Todo el Scraping"
2. Esperar a que complete el proceso
3. Ver resultados en el mensaje de éxito/error

**Estado de las Fuentes:**
- Tabla con información detallada de cada fuente
- **Estado**: Activa/Inactiva
- **Estadísticas**: Scrapes exitosos vs fallidos
- **Último Scraping**: Fecha de la última ejecución

**Acciones por Fuente:**
- **Ejecutar**: Hacer scraping de una fuente específica
- **Activar/Desactivar**: Cambiar estado de la fuente

### Rendimiento por Fuente
- Ranking de las 5 fuentes más productivas
- Cantidad de noticias extraídas por cada una

## Gestión de Fuentes

### Acceso
- Clic en "⚙️ Gestionar Fuentes" en la navegación

### Crear Nueva Fuente

#### Plantillas Rápidas
- **RSS Feed**: Configuración predefinida para fuentes RSS
- **Página HTML**: Configuración base para sitios web regulares

#### Campos Obligatorios
- **Nombre de la Fuente**: Identificador único (ej: "El Comercio")
- **URL Base**: Dirección web de la fuente
- **Selector de Lista**: CSS selector para encontrar las noticias
- **Selector de Título**: CSS selector para extraer títulos

#### Campos Opcionales
- **Selector de Resumen**: Para extraer resúmenes
- **Selector de Imagen**: Para extraer imágenes
- **Selector de Autor**: Para extraer información del autor
- **Selector de Fecha**: Para extraer fechas de publicación
- **Selector de Categoría**: Para extraer categorías
- **Formato de Fecha**: Patrón para interpretar fechas
- **Intervalo de Scraping**: Frecuencia en minutos
- **Fuente Activa**: Checkbox para activar/desactivar

#### Ejemplo de Configuración RSS
```
Nombre: Perú21
URL Base: https://peru21.pe/feed/
Selector de Lista: RSS_FEED
Selector de Título: title
Selector de Resumen: description
Selector de Imagen: img
```

#### Ejemplo de Configuración HTML
```
Nombre: El Comercio
URL Base: https://elcomercio.pe/noticias
Selector de Lista: article.story
Selector de Título: h2 a
Selector de Resumen: .story-summary
Selector de Imagen: .story-image img
```

### Probar Configuración
1. Completar los campos de la fuente
2. Clic en "Probar Configuración"
3. Revisar el resultado:
   - **Noticias encontradas**: Cantidad extraída
   - **Ejemplos**: Muestra de títulos y resúmenes

### Gestionar Fuentes Existentes
- **Lista de fuentes**: Tabla con todas las fuentes configuradas
- **Información mostrada**:
  - Datos básicos (nombre, URL, ID)
  - Configuración (intervalo, selector principal)
  - Estado (activa/inactiva, estadísticas)

**Acciones disponibles:**
- **Editar**: Modificar configuración existente
- **Desactivar**: Marcar fuente como inactiva (soft delete)

## Consejos de Uso

### Búsqueda Efectiva
- Usar palabras clave específicas
- Combinar filtros de fuente y categoría
- Revisar noticias populares para trending topics

### Configuración de Fuentes
- Probar siempre la configuración antes de guardar
- Usar selectores CSS específicos para evitar elementos incorrectos
- Configurar intervalos apropiados (30-60 minutos recomendado)

### Solución de Problemas
- Si no aparecen noticias, verificar que las fuentes estén activas
- Si el scraping falla, revisar la configuración de selectores
- Usar la función de debug para ajustar selectores CSS

## Accesos Rápidos

### Teclado
- **Escape**: Cerrar modal de noticia
- **Enter**: Ejecutar búsqueda (en campo de búsqueda)

### Navegación Móvil
- El sidebar se convierte en menú hamburguesa
- Todas las funciones disponibles en pantallas pequeñas
- Gestos táctiles para navegación

## Limitaciones Conocidas

- El sistema no guarda historial de navegación personal
- No hay sistema de favoritos o marcadores
- Las fuentes RSS mal formateadas pueden causar errores
- Algunas páginas con JavaScript intensivo pueden no funcionar

## Soporte

Para reportar problemas o solicitar nuevas funcionalidades:
1. Revisar logs en el dashboard de administración
2. Verificar configuración de fuentes
3. Contactar al administrador del sistema