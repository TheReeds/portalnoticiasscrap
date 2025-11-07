"""
Script de prueba para verificar que los scrapers funcionan correctamente
"""
from scrapers import get_all_scrapers

def test_scrapers():
    """Prueba cada scraper obteniendo 2 artículos"""
    print("🧪 Probando Scrapers de Noticias\n")
    print("=" * 60)

    scrapers = get_all_scrapers()

    for scraper in scrapers:
        print(f"\n📰 Probando: {scraper.name}")
        print("-" * 60)

        try:
            articles = scraper.fetch_articles(limit=2)

            if articles:
                print(f"✅ {len(articles)} artículos obtenidos")

                for i, article in enumerate(articles, 1):
                    print(f"\n  Artículo {i}:")
                    print(f"  Título: {article['title'][:60]}...")
                    print(f"  URL: {article['url']}")
                    print(f"  Autor: {article.get('author', 'N/A')}")
                    print(f"  Fecha: {article.get('published_date', 'N/A')}")
                    content_preview = article.get('content', '')[:100]
                    print(f"  Contenido: {content_preview}...")
            else:
                print("⚠️  No se obtuvieron artículos")

        except Exception as e:
            print(f"❌ Error: {str(e)}")

    print("\n" + "=" * 60)
    print("✨ Prueba completada\n")

if __name__ == "__main__":
    test_scrapers()
