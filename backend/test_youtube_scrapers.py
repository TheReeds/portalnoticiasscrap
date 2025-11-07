#!/usr/bin/env python
# -*- coding: utf-8 -*-
"""
Script de prueba para los scrapers de YouTube
Demuestra que ambos scrapers funcionan correctamente
"""

import sys
import io

# Fix encoding for Windows console
if sys.platform == 'win32':
    sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8', errors='replace')

from scrapers import RPPNoticiasYouTubeScraper, DWEspanolYouTubeScraper

def test_scraper(scraper, limit=5):
    """Test individual scraper"""
    print(f"\n{'='*60}")
    print(f"🎥 Probando: {scraper.name}")
    print(f"{'='*60}")
    print(f"RSS URL: {scraper.rss_url}")

    try:
        articles = scraper.fetch_articles(limit=limit)
        print(f"\n✅ Videos obtenidos: {len(articles)}")

        if articles:
            print(f"\n📺 Primeros {min(3, len(articles))} videos:")
            for i, article in enumerate(articles[:3], 1):
                print(f"\n{i}. {article['title'][:70]}...")
                print(f"   URL: {article['url']}")
                print(f"   Fecha: {article['published_date']}")
                print(f"   Thumbnail: {article['image_url'][:60]}...")
                print(f"   Descripción: {article['summary'][:100]}..." if article['summary'] else "   Sin descripción")
        else:
            print("❌ No se obtuvieron videos")

        return len(articles)

    except Exception as e:
        print(f"❌ Error: {e}")
        return 0

def main():
    print("🚀 PRUEBA DE SCRAPERS DE YOUTUBE")
    print("="*60)

    # Test RPP Noticias YouTube
    rpp_scraper = RPPNoticiasYouTubeScraper()
    rpp_count = test_scraper(rpp_scraper, limit=5)

    # Test DW Español YouTube
    dw_scraper = DWEspanolYouTubeScraper()
    dw_count = test_scraper(dw_scraper, limit=5)

    # Resumen
    print(f"\n{'='*60}")
    print("📊 RESUMEN")
    print(f"{'='*60}")
    print(f"RPP Noticias YouTube: {rpp_count} videos")
    print(f"DW Español YouTube: {dw_count} videos")
    print(f"Total: {rpp_count + dw_count} videos")

    if rpp_count > 0 and dw_count > 0:
        print("\n✅ ¡Todos los scrapers funcionan correctamente!")
    else:
        print("\n⚠️ Algunos scrapers no devolvieron resultados")

if __name__ == "__main__":
    main()
