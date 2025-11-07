"""
Script para iniciar el servidor backend
"""
import uvicorn
from main import app

if __name__ == "__main__":
    print("=" * 50)
    print("🚀 Iniciando News Scraper Backend")
    print("=" * 50)
    print("\n📍 API disponible en: http://localhost:8000")
    print("📚 Documentación: http://localhost:8000/docs")
    print("\n✨ Presiona Ctrl+C para detener el servidor\n")

    uvicorn.run(
        app,
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
