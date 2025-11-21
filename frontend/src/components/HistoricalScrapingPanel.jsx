import { useState, useEffect } from 'react'
import axios from 'axios'

function HistoricalScrapingPanel() {
    const [keyword, setKeyword] = useState('')
    const [dates, setDates] = useState({ start: '', end: '' })
    const [status, setStatus] = useState(null)

    useEffect(() => {
        const interval = setInterval(fetchStatus, 2000)
        return () => clearInterval(interval)
    }, [])

    const fetchStatus = async () => {
        try {
            const response = await axios.get('/api/historical/status')
            setStatus(response.data)
        } catch (error) {
            console.error('Error fetching status')
        }
    }

    const handleScrape = async (e) => {
        e.preventDefault()
        try {
            await axios.post('/api/historical/scrape', null, {
                params: {
                    keyword,
                    start_date: dates.start,
                    end_date: dates.end
                }
            })
        } catch (error) {
            alert('Error al iniciar scraping histórico')
        }
    }

    return (
        <div className="panel-content">
            <h2>Scraping Histórico</h2>

            <form onSubmit={handleScrape} className="historical-form">
                <input
                    type="text"
                    placeholder="Palabra clave"
                    value={keyword}
                    onChange={e => setKeyword(e.target.value)}
                    required
                />
                <div className="date-inputs">
                    <input
                        type="date"
                        value={dates.start}
                        onChange={e => setDates({ ...dates, start: e.target.value })}
                        required
                    />
                    <input
                        type="date"
                        value={dates.end}
                        onChange={e => setDates({ ...dates, end: e.target.value })}
                        required
                    />
                </div>
                <button type="submit" disabled={status?.is_running}>
                    {status?.is_running ? 'Ejecutando...' : 'Iniciar Búsqueda'}
                </button>
            </form>

            {status && status.is_running && (
                <div className="status-display">
                    <p>Buscando: {status.current_keyword}</p>
                    <p>Encontrados: {status.total_found}</p>
                    <div className="progress-bar">
                        <div
                            className="progress-fill"
                            style={{ width: `${status.progress}%` }}
                        ></div>
                    </div>
                </div>
            )}
        </div>
    )
}

export default HistoricalScrapingPanel
