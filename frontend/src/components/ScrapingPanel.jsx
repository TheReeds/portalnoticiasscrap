import { useState } from 'react'
import axios from 'axios'

function ScrapingPanel() {
    const [loading, setLoading] = useState(false)
    const [status, setStatus] = useState(null)

    const handleScrape = async () => {
        setLoading(true)
        try {
            const response = await axios.post('/api/scrape')
            setStatus({ type: 'success', message: response.data.message })
        } catch (error) {
            setStatus({ type: 'error', message: 'Error al iniciar el scraping' })
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="panel-content">
            <h2>Control de Scraping</h2>
            <p>Inicia el proceso de recolección de noticias de todas las fuentes configuradas.</p>

            <button
                className="action-btn"
                onClick={handleScrape}
                disabled={loading}
            >
                {loading ? 'Scraping en progreso...' : 'Iniciar Scraping Manual'}
            </button>

            {status && (
                <div className={`status-message ${status.type}`}>
                    {status.message}
                </div>
            )}
        </div>
    )
}

export default ScrapingPanel
