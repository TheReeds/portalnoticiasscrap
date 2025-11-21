import { useState, useEffect } from 'react'
import axios from 'axios'
import { IconNewspaper, IconDatabase, IconChart } from './Icons'

function Dashboard() {
    const [stats, setStats] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        fetchStats()
    }, [])

    const fetchStats = async () => {
        try {
            const response = await axios.get('/api/admin/stats')
            setStats(response.data)
        } catch (error) {
            console.error('Error fetching stats:', error)
        } finally {
            setLoading(false)
        }
    }

    if (loading) return <div>Cargando estadísticas...</div>
    if (!stats) return <div>No hay datos disponibles</div>

    return (
        <div className="dashboard-grid">
            <div className="stat-card">
                <div className="stat-icon">
                    <IconNewspaper size={32} />
                </div>
                <div className="stat-info">
                    <h3>Total Noticias</h3>
                    <p className="stat-value">{stats.total_articles}</p>
                </div>
            </div>

            <div className="stat-card">
                <div className="stat-icon">
                    <IconDatabase size={32} />
                </div>
                <div className="stat-info">
                    <h3>Fuentes Activas</h3>
                    <p className="stat-value">{stats.total_sources}</p>
                </div>
            </div>

            <div className="stat-card">
                <div className="stat-icon">
                    <IconChart size={32} />
                </div>
                <div className="stat-info">
                    <h3>Usuarios</h3>
                    <p className="stat-value">{stats.total_users}</p>
                </div>
            </div>
        </div>
    )
}

export default Dashboard
