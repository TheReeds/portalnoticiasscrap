# Manual de Instalación: Portal de Noticias

## 1. Prerrequisitos
- **Python**: 3.9 o superior.
- **Node.js**: 16 o superior.
- **Git**: Para clonar el repositorio.
- **Base de Datos**: SQLite (incluido) o PostgreSQL (opcional).

## 2. Instalación del Backend

### 2.1 Clonar Repositorio
```bash
git clone <url-del-repo>
cd WebScraping/backend
```

### 2.2 Configurar Entorno Virtual
```bash
python -m venv venv
# Windows
venv\Scripts\activate
# Linux/Mac
source venv/bin/activate
```

### 2.3 Instalar Dependencias
```bash
pip install -r requirements.txt
```
*Nota: Asegúrese de que `scikit-learn`, `pandas`, `numpy`, `fastapi`, `uvicorn`, `sqlalchemy` estén en requirements.txt.*

### 2.4 Inicializar Base de Datos
El sistema creará automáticamente `news.db` (SQLite) al iniciar.

### 2.5 Ejecutar Servidor
```bash
uvicorn main:app --reload --port 8000
```
El backend estará disponible en `http://localhost:8000`.

## 3. Instalación del Frontend

### 3.1 Navegar al Directorio
```bash
cd ../frontend
```

### 3.2 Instalar Dependencias
```bash
npm install
```

### 3.3 Configurar Variables de Entorno
Cree un archivo `.env` en la raíz de `frontend/`:
```
REACT_APP_API_URL=http://localhost:8000
```

### 3.4 Ejecutar Cliente
```bash
npm start
```
La aplicación se abrirá en `http://localhost:3000`.

## 4. Verificación
1. Abra el navegador en `http://localhost:3000`.
2. Verifique que la página de inicio cargue.
3. Intente registrar un usuario y acceder al Panel Admin.