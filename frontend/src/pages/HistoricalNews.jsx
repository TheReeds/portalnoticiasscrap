import { useState } from 'react'
import HistoricalScrapingPanel from '../components/HistoricalScrapingPanel'

function HistoricalNews() {
    return (
        <div className="historical-page">
            <h1>Búsqueda Histórica de Noticias</h1>
            <p className="description">
                Utiliza esta herramienta para buscar y recuperar noticias antiguas de las fuentes configuradas.
            </p>
            <HistoricalScrapingPanel />
        </div>
    )
}

export default HistoricalNews
