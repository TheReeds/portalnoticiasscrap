import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { format } from 'date-fns'
import { es } from 'date-fns/locale'
import {
    IconNewspaper,
    IconVideo,
    IconTrending,
    IconEye,
    IconChevronLeft,
    IconChevronRight,
    IconClock
} from '../components/Icons'
import './Home.css'

const API_URL = '/api'

function Home() {
    const [articles, setArticles] = useState([])
    const [popularArticles, setPopularArticles] = useState([])
    const [categories, setCategories] = useState([])
    const [selectedCategory, setSelectedCategory] = useState('all')
    const [contentType, setContentType] = useState('all') // 'all', 'articles', 'videos'
    const [loading, setLoading] = useState(true)
    const [currentPage, setCurrentPage] = useState(1)
    const [totalArticles, setTotalArticles] = useState(0)
    const articlesPerPage = 20

    useEffect(() => {
        fetchArticles()
        fetchPopularArticles()
        fetchCategories()
    }, [selectedCategory, contentType, currentPage])

    const fetchArticles = async () => {
        setLoading(true)
        try {
            const skip = (currentPage - 1) * articlesPerPage
            const params = {
                limit: articlesPerPage,
                skip: skip
            }

            // Filter by content type using backend source parameter
            if (contentType === 'videos') {
                // Use backend filtering for YouTube sources
                // We'll make multiple requests for each YouTube source and combine
                const youtubeSourcesPromises = [
                    axios.get(`${API_URL}/articles`, {
                        params: { ...params, source: 'RPP Noticias YouTube' }
                    }),
                    axios.get(`${API_URL}/articles`, {
                        params: { ...params, source: 'DW Español YouTube' }
                    })
                ]

                const responses = await Promise.all(youtubeSourcesPromises)
                const allVideos = responses.flatMap(r => r.data.articles)

                // Sort by date and take the requested amount
                const sortedVideos = allVideos.sort((a, b) =>
                    new Date(b.published_date) - new Date(a.published_date)
                )

                setArticles(sortedVideos.slice(0, articlesPerPage))
                setTotalArticles(responses.reduce((sum, r) => sum + r.data.total, 0))
            } else {
                // For regular articles or all content
                let url = `${API_URL}/articles`

                if (selectedCategory !== 'all') {
                    url = `${API_URL}/articles/category/${selectedCategory}`
                }

                const response = await axios.get(url, { params })
                let filteredArticles = response.data.articles

                // If showing only articles (not videos), filter out YouTube sources
                if (contentType === 'articles') {
                    filteredArticles = filteredArticles.filter(article =>
                        !article.source || (
                            !article.source.toLowerCase().includes('youtube')
                        )
                    )
                }

                setArticles(filteredArticles)
                setTotalArticles(response.data.total || filteredArticles.length)
            }
        } catch (error) {
            console.error('Error fetching articles:', error)
        } finally {
            setLoading(false)
        }
    }

    const fetchPopularArticles = async () => {
        try {
            const response = await axios.get(`${API_URL}/articles/popular/top?limit=5`)
            setPopularArticles(response.data.articles)
        } catch (error) {
            console.error('Error fetching popular articles:', error)
        }
    }

    const fetchCategories = async () => {
        try {
            const response = await axios.get(`${API_URL}/categories`)
            setCategories(response.data.categories)
        } catch (error) {
            console.error('Error fetching categories:', error)
        }
    }

    const formatDate = (dateString) => {
        if (!dateString) return ''
        try {
            return format(new Date(dateString), "d MMM, yyyy · HH:mm", { locale: es })
        } catch {
            return ''
        }
    }

    const truncateText = (text, maxLength) => {
        if (!text) return ''
        if (text.length <= maxLength) return text
        return text.substring(0, maxLength) + '...'
    }

    const featuredArticle = articles[0]
    const secondaryArticles = articles.slice(1, 4)
    const regularArticles = articles.slice(4)

    return (
        <div className="home-page">
            {/* Sidebar */}
            <aside className="sidebar">
                <div className="sidebar-section">
                    <h3>Explorar</h3>
                    <div className="content-type-filters">
                        <button
                            className={`filter-btn ${contentType === 'all' ? 'active' : ''}`}
                            onClick={() => setContentType('all')}
                        >
                            <IconNewspaper className="icon" /> Todo
                        </button>
                        <button
                            className={`filter-btn ${contentType === 'articles' ? 'active' : ''}`}
                            onClick={() => setContentType('articles')}
                        >
                            <IconNewspaper className="icon" /> Artículos
                        </button>
                        <button
                            className={`filter-btn ${contentType === 'videos' ? 'active' : ''}`}
                            onClick={() => setContentType('videos')}
                        >
                            <IconVideo className="icon" /> Videos
                        </button>
                    </div>
                </div>

                <div className="sidebar-section">
                    <h3>Categorías</h3>
                    <ul className="category-list">
                        <li
                            className={selectedCategory === 'all' ? 'active' : ''}
                            onClick={() => setSelectedCategory('all')}
                        >
                            Todas las noticias
                        </li>
                        {categories.map(cat => (
                            <li
                                key={cat.name}
                                className={selectedCategory === cat.name ? 'active' : ''}
                                onClick={() => setSelectedCategory(cat.name)}
                            >
                                {cat.name} <span className="count">{cat.count}</span>
                            </li>
                        ))}
                    </ul>
                </div>

                <div className="sidebar-section">
                    <h3><IconTrending className="section-icon" /> Tendencias</h3>
                    <div className="popular-list">
                        {popularArticles.map((article, index) => (
                            <Link
                                key={article.id}
                                to={`/article/${article.id}`}
                                className="popular-item"
                            >
                                <span className="popular-number">{index + 1}</span>
                                <div className="popular-content">
                                    <h4>{truncateText(article.title, 60)}</h4>
                                    <span className="popular-views">
                                        <IconEye className="icon-small" /> {article.views}
                                    </span>
                                </div>
                            </Link>
                        ))}
                    </div>
                </div>
            </aside>

            {/* Main Content */}
            <main className="main-content">
                {loading ? (
                    <div className="loading">
                        <div className="spinner"></div>
                        <p>Cargando noticias...</p>
                    </div>
                ) : articles.length === 0 ? (
                    <div className="no-articles">
                        <p>No hay noticias disponibles.</p>
                        <Link to="/admin" className="btn-primary">Ir al Panel Admin</Link>
                    </div>
                ) : (
                    <>
                        {/* Featured Article */}
                        {featuredArticle && (
                            <Link to={`/article/${featuredArticle.id}`} className="featured-article">
                                {featuredArticle.image_url && (
                                    <div className="featured-image">
                                        <img src={featuredArticle.image_url} alt={featuredArticle.title} />
                                        <div className="featured-overlay"></div>
                                    </div>
                                )}
                                <div className="featured-content">
                                    <div className="badges-row">
                                        <span className="article-category">{featuredArticle.category}</span>
                                        {featuredArticle.source.includes('YouTube') && (
                                            <span className="youtube-badge"><IconVideo className="icon-xs" /> Video</span>
                                        )}
                                    </div>
                                    <h2>{featuredArticle.title}</h2>
                                    <p className="article-summary">{truncateText(featuredArticle.summary, 180)}</p>
                                    <div className="article-meta">
                                        <span className="source">{featuredArticle.source}</span>
                                        <span className="dot">•</span>
                                        <span className="date"><IconClock className="icon-xs" /> {formatDate(featuredArticle.published_date)}</span>
                                    </div>
                                </div>
                            </Link>
                        )}

                        {/* Secondary Articles */}
                        {secondaryArticles.length > 0 && (
                            <div className="secondary-articles">
                                {secondaryArticles.map(article => (
                                    <Link
                                        key={article.id}
                                        to={`/article/${article.id}`}
                                        className="secondary-article"
                                    >
                                        {article.image_url && (
                                            <div className="secondary-image-wrapper">
                                                <img src={article.image_url} alt={article.title} />
                                            </div>
                                        )}
                                        <div className="secondary-content">
                                            <div className="badges-row">
                                                <span className="article-category">{article.category}</span>
                                                {article.source.includes('YouTube') && (
                                                    <span className="youtube-badge"><IconVideo className="icon-xs" /></span>
                                                )}
                                            </div>
                                            <h3>{truncateText(article.title, 80)}</h3>
                                            <div className="article-meta">
                                                <span className="source">{article.source}</span>
                                                <span className="date">{formatDate(article.published_date)}</span>
                                            </div>
                                        </div>
                                    </Link>
                                ))}
                            </div>
                        )}

                        {/* Regular Articles Grid */}
                        <div className="section-header">
                            <h3>Más Noticias</h3>
                            <div className="divider"></div>
                        </div>

                        <div className="articles-grid">
                            {regularArticles.map(article => (
                                <Link
                                    key={article.id}
                                    to={`/article/${article.id}`}
                                    className="article-card"
                                >
                                    {article.image_url && (
                                        <div className="card-image-wrapper">
                                            <img src={article.image_url} alt={article.title} />
                                        </div>
                                    )}
                                    <div className="card-content">
                                        <div className="badges-row">
                                            <span className="article-category">{article.category}</span>
                                            {article.source.includes('YouTube') && (
                                                <span className="youtube-badge"><IconVideo className="icon-xs" /></span>
                                            )}
                                        </div>
                                        <h4>{truncateText(article.title, 70)}</h4>
                                        <div className="article-meta">
                                            <span className="source">{article.source}</span>
                                            <span className="views"><IconEye className="icon-xs" /> {article.views}</span>
                                        </div>
                                    </div>
                                </Link>
                            ))}
                        </div>

                        {/* Paginación */}
                        {totalArticles > articlesPerPage && (
                            <div className="pagination">
                                <button
                                    className="pagination-btn"
                                    onClick={() => {
                                        setCurrentPage(prev => Math.max(1, prev - 1))
                                        window.scrollTo({ top: 0, behavior: 'smooth' })
                                    }}
                                    disabled={currentPage === 1}
                                >
                                    <IconChevronLeft className="icon" /> Anterior
                                </button>

                                <div className="pagination-info">
                                    <span className="page-number">Página {currentPage}</span>
                                    <span className="page-divider">de</span>
                                    <span className="page-number">{Math.ceil(totalArticles / articlesPerPage)}</span>
                                </div>

                                <button
                                    className="pagination-btn"
                                    onClick={() => {
                                        setCurrentPage(prev => prev + 1)
                                        window.scrollTo({ top: 0, behavior: 'smooth' })
                                    }}
                                    disabled={currentPage >= Math.ceil(totalArticles / articlesPerPage)}
                                >
                                    Siguiente <IconChevronRight className="icon" />
                                </button>
                            </div>
                        )}
                    </>
                )}
            </main>
        </div>
    )
}

export default Home
