import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import './Admin.css'

const API_URL = '/api'

function Admin() {
  const [sources, setSources] = useState([])
  const [selectedSource, setSelectedSource] = useState('')
  const [limit, setLimit] = useState(10)
  const [scraping, setScraping] = useState(false)
  const [message, setMessage] = useState(null)
  const [stats, setStats] = useState({ total: 0, bySource: [] })

  useEffect(() => {
    fetchSources()
    fetchStats()
  }, [])

  const fetchSources = async () => {
    try {
      const response = await axios.get(`${API_URL}/sources`)
      setSources(response.data.sources)
    } catch (error) {
      console.error('Error fetching sources:', error)
    }
  }

  const fetchStats = async () => {
    try {
      const response = await axios.get(`${API_URL}/articles?limit=1`)
      const sourcesResp = await axios.get(`${API_URL}/sources`)
      setStats({
        total: response.data.total,
        bySource: sourcesResp.data.sources
      })
    } catch (error) {
      console.error('Error fetching stats:', error)
    }
  }

  const handleScrape = async () => {
    setScraping(true)
    setMessage(null)
    try {
      const params = { limit }
      if (selectedSource) params.source = selectedSource

      const response = await axios.post(`${API_URL}/scrape`, null, { params })
      setMessage({
        type: 'success',
        text: `✓ Scraping exitoso! ${response.data.scraped} nuevos artículos agregados`
      })
      fetchStats()
    } catch (error) {
      setMessage({
        type: 'error',
        text: '✗ Error al hacer scraping. Revisa la consola del backend.'
      })
    } finally {
      setScraping(false)
    }
  }

  return (
    <div className="admin-page">
      <div className="admin-header">
        <h1>🔧 Panel de Administración</h1>
        <Link to="/" className="btn-secondary">← Volver al inicio</Link>
      </div>

      <div className="admin-content">
        {/* Stats Section */}
        <div className="stats-section">
          <h2>📊 Estadísticas</h2>
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-number">{stats.total}</div>
              <div className="stat-label">Total de Artículos</div>
            </div>
            {stats.bySource.map(source => (
              <div key={source.name} className={`stat-card ${source.name.includes('YouTube') ? 'youtube-source' : ''}`}>
                <div className="stat-number">{source.article_count}</div>
                <div className="stat-label">
                  {source.name.includes('YouTube') && '🎥 '}
                  {source.name}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Scraping Section */}
        <div className="scraping-section">
          <h2>🔍 Web Scraping</h2>

          {message && (
            <div className={`message ${message.type}`}>
              {message.text}
            </div>
          )}

          <div className="scraping-form">
            <div className="form-group">
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
                    {source.name.includes('YouTube') ? '🎥 ' : ''}
                    {source.name} ({source.article_count} artículos)
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
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
              className="btn-scrape"
            >
              {scraping ? '⏳ Scrapeando...' : '🚀 Iniciar Scraping'}
            </button>
          </div>

          <div className="scraping-info">
            <h3>ℹ️ Información</h3>
            <ul>
              <li>El scraping obtiene las últimas noticias usando RSS feeds</li>
              <li>Extrae el contenido completo de cada artículo</li>
              <li>Los artículos duplicados (por URL) son ignorados automáticamente</li>
              <li>El proceso puede tardar 15-60 segundos dependiendo de la cantidad</li>
            </ul>
          </div>
        </div>

        {/* Export Section */}
        <div className="export-section">
          <h2>📥 Exportar Datos</h2>
          <p className="export-description">Descarga todos tus datos en formato JSON o CSV</p>

          <div className="export-grid">
            {/* Export All */}
            <div className="export-card">
              <div className="export-card-header">
                <h3>📰 Todos los Datos</h3>
                <span className="export-badge">Artículos + Videos</span>
              </div>
              <p>Exporta todos los artículos y videos de todas las fuentes</p>
              <div className="export-buttons">
                <a
                  href={`${API_URL}/export/articles?format=json&content_type=all`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn-export json"
                >
                  📄 Descargar JSON
                </a>
                <a
                  href={`${API_URL}/export/articles?format=csv&content_type=all`}
                  download
                  className="btn-export csv"
                >
                  📊 Descargar CSV
                </a>
              </div>
            </div>

            {/* Export Articles Only */}
            <div className="export-card">
              <div className="export-card-header">
                <h3>📄 Solo Artículos</h3>
                <span className="export-badge articles">RSS Feeds</span>
              </div>
              <p>Exporta solo artículos de noticias tradicionales</p>
              <div className="export-buttons">
                <a
                  href={`${API_URL}/export/articles?format=json&content_type=articles`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn-export json"
                >
                  📄 Descargar JSON
                </a>
                <a
                  href={`${API_URL}/export/articles?format=csv&content_type=articles`}
                  download
                  className="btn-export csv"
                >
                  📊 Descargar CSV
                </a>
              </div>
            </div>

            {/* Export Videos Only */}
            <div className="export-card">
              <div className="export-card-header">
                <h3>🎥 Solo Videos</h3>
                <span className="export-badge videos">YouTube</span>
              </div>
              <p>Exporta solo videos de YouTube</p>
              <div className="export-buttons">
                <a
                  href={`${API_URL}/export/articles?format=json&content_type=videos`}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="btn-export json"
                >
                  📄 Descargar JSON
                </a>
                <a
                  href={`${API_URL}/export/articles?format=csv&content_type=videos`}
                  download
                  className="btn-export csv"
                >
                  📊 Descargar CSV
                </a>
              </div>
            </div>
          </div>

          <div className="export-info">
            <h4>ℹ️ Información sobre exportación</h4>
            <ul>
              <li><strong>JSON</strong>: Formato completo con toda la información (título, contenido, autor, fecha, URL, etc.)</li>
              <li><strong>CSV</strong>: Formato compatible con Excel con campos principales (título, fuente, fecha, resumen)</li>
              <li>Los archivos se generan en tiempo real con los datos actuales de la base de datos</li>
              <li>Puedes importar estos datos en otras herramientas de análisis</li>
            </ul>
          </div>
        </div>

        {/* Search Filters Section */}
        <div className="filters-section">
          <h2>🔎 Búsqueda y Filtros</h2>
          <p>Los filtros están disponibles en la página principal para los usuarios.</p>
          <Link to="/" className="btn-primary">Ir a Portada</Link>
        </div>
      </div>
    </div>
  )
}

export default Admin
