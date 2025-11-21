import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import axios from 'axios'

function ArticleDetail() {
    const { id } = useParams()
    const [article, setArticle] = useState(null)
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        const fetchArticle = async () => {
            try {
                const response = await axios.get(`/api/articles/${id}`)
                setArticle(response.data)
            } catch (error) {
                console.error('Error fetching article:', error)
            } finally {
                setLoading(false)
            }
        }
        fetchArticle()
    }, [id])

    if (loading) return <div>Cargando...</div>
    if (!article) return <div>Noticia no encontrada</div>

    return (
        <div className="article-detail">
            <h1>{article.title}</h1>
            <div className="article-meta">
                <span>{article.source}</span>
                <span>{new Date(article.published_date).toLocaleDateString()}</span>
            </div>
            {article.image_url && (
                <img src={article.image_url} alt={article.title} className="article-image" />
            )}
            <div className="article-content">
                <p>{article.summary}</p>
                <div dangerouslySetInnerHTML={{ __html: article.content }} />
            </div>
            <a href={article.url} target="_blank" rel="noopener noreferrer" className="read-more-btn">
                Leer noticia completa en la fuente original
            </a>
        </div>
    )
}

export default ArticleDetail
