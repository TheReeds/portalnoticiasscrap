import { useState } from 'react'
import './ScraperControls.css'

function ScraperControls({ sources, onScrape, scraping }) {
  const [selectedSource, setSelectedSource] = useState('')
  const [limit, setLimit] = useState(10)

  const handleScrape = () => {
    onScrape(selectedSource, limit)
  }

  return (
    <div className="scraper-controls">
      <h2>🔍 Hacer Scraping</h2>
      <div className="controls-grid">
        <div className="control-group">
          <label htmlFor="source">Fuente:</label>
          <select
            id="source"
            value={selectedSource}
            onChange={(e) => setSelectedSource(e.target.value)}
            disabled={scraping}
          >
            <option value="">Todas las fuentes</option>
            {sources.map(source => (
              <option key={source.name} value={source.name}>
                {source.name} ({source.article_count} artículos)
              </option>
            ))}
          </select>
        </div>

        <div className="control-group">
          <label htmlFor="limit">Límite por fuente:</label>
          <input
            id="limit"
            type="number"
            min="1"
            max="50"
            value={limit}
            onChange={(e) => setLimit(parseInt(e.target.value))}
            disabled={scraping}
          />
        </div>

        <button
          onClick={handleScrape}
          disabled={scraping}
          className="btn-primary scrape-button"
        >
          {scraping ? '⏳ Scrapeando...' : '🚀 Iniciar Scraping'}
        </button>
      </div>

      <p className="scraper-note">
        ℹ️ El scraping obtiene las últimas noticias usando RSS feeds y extrae el contenido completo de cada artículo.
      </p>
    </div>
  )
}

export default ScraperControls
