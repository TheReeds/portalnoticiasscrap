import { useState } from 'react'
import axios from 'axios'

function ExportPanel() {
    const [format, setFormat] = useState('csv')
    const [loading, setLoading] = useState(false)

    const handleExport = async () => {
        setLoading(true)
        try {
            const response = await axios.get(`/api/export?format=${format}`, {
                responseType: 'blob'
            })

            const url = window.URL.createObjectURL(new Blob([response.data]))
            const link = document.createElement('a')
            link.href = url
            link.setAttribute('download', `noticias_export.${format}`)
            document.body.appendChild(link)
            link.click()
            link.remove()
        } catch (error) {
            alert('Error al exportar datos')
        } finally {
            setLoading(false)
        }
    }

    return (
        <div className="panel-content">
            <h2>Exportar Datos</h2>
            <div className="export-controls">
                <select value={format} onChange={e => setFormat(e.target.value)}>
                    <option value="csv">CSV</option>
                    <option value="json">JSON</option>
                    <option value="excel">Excel</option>
                </select>
                <button onClick={handleExport} disabled={loading}>
                    {loading ? 'Exportando...' : 'Descargar Archivo'}
                </button>
            </div>
        </div>
    )
}

export default ExportPanel
