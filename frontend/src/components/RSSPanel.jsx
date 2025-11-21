import { useState, useEffect } from 'react'
import axios from 'axios'

function RSSPanel() {
    const [feeds, setFeeds] = useState([])
    const [newFeed, setNewFeed] = useState({ name: '', url: '' })

    useEffect(() => {
        fetchFeeds()
    }, [])

    const fetchFeeds = async () => {
        try {
            const response = await axios.get('/api/rss')
            setFeeds(response.data)
        } catch (error) {
            console.error('Error fetching feeds:', error)
        }
    }

    const handleSubmit = async (e) => {
        e.preventDefault()
        try {
            await axios.post('/api/rss', newFeed)
            setNewFeed({ name: '', url: '' })
            fetchFeeds()
        } catch (error) {
            alert('Error al guardar el feed')
        }
    }

    const handleDelete = async (id) => {
        if (!window.confirm('¿Estás seguro?')) return
        try {
            await axios.delete(`/api/rss/${id}`)
            fetchFeeds()
        } catch (error) {
            alert('Error al eliminar el feed')
        }
    }

    return (
        <div className="panel-content">
            <h2>Gestión de Fuentes RSS</h2>

            <form onSubmit={handleSubmit} className="rss-form">
                <input
                    type="text"
                    placeholder="Nombre de la fuente"
                    value={newFeed.name}
                    onChange={e => setNewFeed({ ...newFeed, name: e.target.value })}
                    required
                />
                <input
                    type="url"
                    placeholder="URL del Feed RSS"
                    value={newFeed.url}
                    onChange={e => setNewFeed({ ...newFeed, url: e.target.value })}
                    required
                />
                <button type="submit">Agregar Fuente</button>
            </form>

            <div className="feeds-list">
                {feeds.map(feed => (
                    <div key={feed.id} className="feed-item">
                        <div className="feed-info">
                            <strong>{feed.name}</strong>
                            <span>{feed.url}</span>
                        </div>
                        <button onClick={() => handleDelete(feed.id)} className="delete-btn">
                            Eliminar
                        </button>
                    </div>
                ))}
            </div>
        </div>
    )
}

export default RSSPanel
