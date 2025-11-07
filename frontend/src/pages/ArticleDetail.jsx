import { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import axios from 'axios'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import './ArticleDetail.css'

const API_URL = '/api'

function ArticleDetail() {
  const { id } = useParams()
  const [article, setArticle] = useState(null)
  const [relatedArticles, setRelatedArticles] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchArticle()
  }, [id])

  const fetchArticle = async () => {
    setLoading(true)
    try {
      const response = await axios.get(`${API_URL}/articles/${id}`)
      setArticle(response.data)
      fetchRelatedArticles(response.data.category)
    } catch (error) {
      console.error('Error fetching article:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchRelatedArticles = async (category) => {
    try {
      const response = await axios.get(`${API_URL}/articles/category/${category}?limit=4`)
      setRelatedArticles(response.data.articles.filter(a => a.id !== parseInt(id)))
    } catch (error) {
      console.error('Error fetching related articles:', error)
    }
  }

  const formatDate = (dateString) => {
    if (!dateString) return ''
    try {
      return format(new Date(dateString), "d 'de' MMMM, yyyy · HH:mm", { locale: es })
    } catch {
      return ''
    }
  }

  const getYouTubeVideoId = (url) => {
    if (!url) return null
    // Extract video ID from YouTube URL
    const regex = /(?:youtube\.com\/(?:[^\/]+\/.+\/|(?:v|e(?:mbed)?)\/|.*[?&]v=)|youtu\.be\/)([^"&?\/\s]{11})/
    const match = url.match(regex)
    return match ? match[1] : null
  }

  const isYouTubeVideo = article && article.source && article.source.includes('YouTube')

  if (loading) {
    return <div className="loading-page">Cargando artículo...</div>
  }

  if (!article) {
    return (
      <div className="error-page">
        <h2>Artículo no encontrado</h2>
        <Link to="/" className="btn-primary">Volver al inicio</Link>
      </div>
    )
  }

  return (
    <div className="article-detail-page">
      <div className="article-header">
        <div className="badges-row">
          <span className="article-category">{article.category}</span>
          {isYouTubeVideo && (
            <span className="youtube-badge">🎥 Video de YouTube</span>
          )}
        </div>
        <h1>{article.title}</h1>

        <div className="article-meta-bar">
          <div className="meta-left">
            <span className="source">{article.source}</span>
            <span className="dot">•</span>
            <span className="author">{article.author}</span>
            <span className="dot">•</span>
            <span className="date">{formatDate(article.published_date)}</span>
          </div>
          <div className="meta-right">
            <span className="views">👁️ {article.views} vistas</span>
          </div>
        </div>
      </div>

      {isYouTubeVideo ? (
        <div className="youtube-player-container">
          <iframe
            src={`https://www.youtube.com/embed/${getYouTubeVideoId(article.url)}`}
            title={article.title}
            frameBorder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
            allowFullScreen
          ></iframe>
        </div>
      ) : (
        article.image_url && (
          <div className="article-hero-image">
            <img src={article.image_url} alt={article.title} />
          </div>
        )
      )}

      {article.summary && (
        <div className="article-summary">
          <p>{article.summary}</p>
        </div>
      )}

      <div className="article-content">
        {article.content ? (
          article.content.split('\n').map((paragraph, index) => (
            paragraph.trim() && <p key={index}>{paragraph}</p>
          ))
        ) : (
          <p>Contenido no disponible.</p>
        )}
      </div>

      <div className="article-footer">
        <a
          href={article.url}
          target="_blank"
          rel="noopener noreferrer"
          className="btn-external"
        >
          {isYouTubeVideo ? (
            <>🎥 Ver en YouTube</>
          ) : (
            <>📰 Leer artículo completo en {article.source}</>
          )}
        </a>
      </div>

      {relatedArticles.length > 0 && (
        <div className="related-articles">
          <h3>Artículos relacionados</h3>
          <div className="related-grid">
            {relatedArticles.slice(0, 3).map(related => (
              <Link
                key={related.id}
                to={`/article/${related.id}`}
                className="related-card"
              >
                {related.image_url && (
                  <img src={related.image_url} alt={related.title} />
                )}
                <div className="related-content">
                  <h4>{related.title}</h4>
                  <span className="related-source">{related.source}</span>
                </div>
              </Link>
            ))}
          </div>
        </div>
      )}

      <div className="back-button">
        <Link to="/" className="btn-secondary">← Volver al inicio</Link>
      </div>
    </div>
  )
}

export default ArticleDetail
