import { useState, useEffect } from 'react'
import axios from 'axios'
import {
    PieChart, Pie, Cell, Tooltip, Legend,
    BarChart, Bar, XAxis, YAxis, CartesianGrid, ResponsiveContainer
} from 'recharts'
import './AnalyticsPanel.css'

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884d8'];

function AnalyticsPanel() {
    const [data, setData] = useState(null)
    const [loading, setLoading] = useState(true)
    const [days, setDays] = useState(7)
    const [nClusters, setNClusters] = useState(5)

    useEffect(() => {
        fetchAnalytics()
    }, [days, nClusters])

    const fetchAnalytics = async () => {
        setLoading(true)
        try {
            const response = await axios.get('/api/analytics/clustering', {
                params: { days, n_clusters: nClusters }
            })
            setData(response.data)
        } catch (error) {
            console.error('Error fetching analytics:', error)
        } finally {
            setLoading(false)
        }
    }

    if (loading) return <div className="loading">Cargando análisis...</div>
    if (!data) return <div>No hay datos disponibles</div>

    const pieData = data.clustering.clusters.map(c => ({
        name: `Tema ${c.id + 1}`,
        value: c.size,
        keywords: c.keywords.join(', ')
    }))

    return (
        <div className="analytics-panel">
            <div className="analytics-header">
                <h2>Análisis de Tendencias y Temas</h2>
                <div className="analytics-controls">
                    <select value={days} onChange={e => setDays(Number(e.target.value))}>
                        <option value={7}>Últimos 7 días</option>
                        <option value={15}>Últimos 15 días</option>
                        <option value={30}>Últimos 30 días</option>
                    </select>
                    <select value={nClusters} onChange={e => setNClusters(Number(e.target.value))}>
                        <option value={3}>3 Temas</option>
                        <option value={5}>5 Temas</option>
                        <option value={7}>7 Temas</option>
                    </select>
                </div>
            </div>

            <div className="analytics-grid">
                <div className="chart-card">
                    <h3>Distribución de Temas</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <PieChart>
                            <Pie
                                data={pieData}
                                cx="50%"
                                cy="50%"
                                labelLine={false}
                                outerRadius={100}
                                fill="#8884d8"
                                dataKey="value"
                                label={({ name, percent }) => `${name} ${(percent * 100).toFixed(0)}%`}
                            >
                                {pieData.map((entry, index) => (
                                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                                ))}
                            </Pie>
                            <Tooltip />
                            <Legend />
                        </PieChart>
                    </ResponsiveContainer>
                </div>

                <div className="chart-card">
                    <h3>Evolución Temporal</h3>
                    <ResponsiveContainer width="100%" height={300}>
                        <BarChart data={data.trends}>
                            <CartesianGrid strokeDasharray="3 3" />
                            <XAxis dataKey="date" />
                            <YAxis />
                            <Tooltip />
                            <Legend />
                            {data.clustering.clusters.map((c, i) => (
                                <Bar
                                    key={c.id}
                                    dataKey={`cluster_${c.id}`}
                                    name={`Tema ${c.id + 1}`}
                                    stackId="a"
                                    fill={COLORS[i % COLORS.length]}
                                />
                            ))}
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>

            <div className="clusters-list">
                <h3>Detalle de Temas Identificados</h3>
                <div className="clusters-grid">
                    {data.clustering.clusters.map((c, i) => (
                        <div key={c.id} className="cluster-card" style={{ borderTop: `4px solid ${COLORS[i % COLORS.length]}` }}>
                            <h4>Tema {c.id + 1} ({c.percentage.toFixed(1)}%)</h4>
                            <div className="keywords">
                                <strong>Palabras clave:</strong> {c.keywords.join(', ')}
                            </div>
                            <div className="sample-titles">
                                <strong>Ejemplos:</strong>
                                <ul>
                                    {c.sample_titles.map((title, idx) => (
                                        <li key={idx}>{title}</li>
                                    ))}
                                </ul>
                            </div>
                        </div>
                    ))}
                </div>
            </div>
        </div>
    )
}

export default AnalyticsPanel
