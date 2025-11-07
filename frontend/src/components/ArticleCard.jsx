import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import './ArticleCard.css'

function ArticleCard({ article }) {
  const formatDate = (dateString) => {
    if (!dateString) return 'Fecha desconocida'
    try {
      return format(new Date(dateString), "d 'de' MMMM, yyyy", { locale: es })
    } catch (error) {
      return 'Fecha desconocida'
    }
  }

  const truncateText = (text, maxLength) => {
    if (!text) return ''
    if (text.length <= maxLength) return text
    return text.substring(0, maxLength) + '...'
  }

  const getSourceColor = (source) => {
    const colors = {
      'El Comercio': '#e11d48',
      'BBC News': '#dc2626',
      'The Guardian': '#0284c7',
      'RPP Noticias': '#ff6b00'  // RPP orange
    }
    return colors[source] || '#64748b'
  }

  return (
    <div className="article-card">
      {article.image_url && (
        <div className="article-image">
          <img src={article.image_url} alt={article.title} />
        </div>
      )}

      <div className="article-content">
        <div className="article-meta">
          <span
            className="article-source"
            style={{ backgroundColor: getSourceColor(article.source) }}
          >
            {article.source}
          </span>
          <span className="article-date">
            {formatDate(article.published_date)}
          </span>
        </div>

        <h3 className="article-title">
          {article.title}
        </h3>

        {article.author && (
          <p className="article-author">
            Por: {article.author}
          </p>
        )}

        {article.summary && (
          <p className="article-summary">
            {truncateText(article.summary, 150)}
          </p>
        )}

        {article.content && (
          <p className="article-excerpt">
            {truncateText(article.content, 200)}
          </p>
        )}

        <a
          href={article.url}
          target="_blank"
          rel="noopener noreferrer"
          className="article-link"
        >
          Leer artículo completo →
        </a>
      </div>
    </div>
  )
}

export default ArticleCard
