import { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import Dashboard from '../components/Dashboard'
import ScrapingPanel from '../components/ScrapingPanel'
import HistoricalScrapingPanel from '../components/HistoricalScrapingPanel'
import ExportPanel from '../components/ExportPanel'
import RSSPanel from '../components/RSSPanel'
import AnalyticsPanel from '../components/AnalyticsPanel'
import PredictionPanel from '../components/PredictionPanel'
import './Admin.css'

function Admin() {
    const { user, isAdmin } = useAuth()
    const [activeTab, setActiveTab] = useState(isAdmin ? 'dashboard' : 'rss')

    // Update active tab if role changes or on mount
    useEffect(() => {
        if (!isAdmin && activeTab === 'dashboard') {
            setActiveTab('rss')
        }
    }, [isAdmin])

    return (
        <div className="admin-page">
            <div className="admin-header-new">
                <div className="admin-title-section">
                    <h1>{isAdmin ? 'Panel de Administración' : 'Panel Premium'}</h1>
                    <p className="admin-welcome">Bienvenido, {user?.full_name || user?.username}</p>
                </div>
                <div className="admin-stats-summary">
                    <div className="stat-item">
                        <svg fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
                        </svg>
                        <span className="stat-label">Usuario:</span>
                        <span className="stat-value">{user?.username}</span>
                    </div>
                    <div className="stat-item">
                        <svg fill="currentColor" viewBox="0 0 20 20">
                            <path d="M8.433 7.418c.155-.103.346-.196.567-.267v1.698a2.305 2.305 0 01-.567-.267C8.07 8.34 8 8.114 8 8c0-.114.07-.34.433-.582zM11 12.849v-1.698c.22.071.412.164.567.267.364.243.433.468.433.582 0 .114-.07.34-.433.582a2.305 2.305 0 01-.567.267z" />
                            <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm1-13a1 1 0 10-2 0v.092a4.535 4.535 0 00-1.676.662C6.602 6.234 6 7.009 6 8c0 .99.602 1.765 1.324 2.246.48.32 1.054.545 1.676.662v1.941c-.391-.127-.68-.317-.843-.504a1 1 0 10-1.51 1.31c.562.649 1.413 1.076 2.353 1.253V15a1 1 0 102 0v-.092a4.535 4.535 0 001.676-.662C13.398 13.766 14 12.991 14 12c0-.99-.602-1.765-1.324-2.246A4.535 4.535 0 0011 9.092V7.151c.391.127.68.317.843.504a1 1 0 101.511-1.31c-.563-.649-1.413-1.076-2.354-1.253V5z" clipRule="evenodd" />
                        </svg>
                        <span className="stat-label">Plan:</span>
                        <span className={`stat-value plan-${user?.subscription_plan || 'free'}`}>
                            {user?.subscription_plan === 'premium' ? 'Premium' : user?.subscription_plan === 'basic' ? 'Basic' : 'Free'}
                        </span>
                    </div>
                </div>
            </div>

            <div className="admin-tabs">
                {isAdmin && (
                    <button
                        className={`tab-button ${activeTab === 'dashboard' ? 'active' : ''}`}
                        onClick={() => setActiveTab('dashboard')}
                    >
                        <svg fill="currentColor" viewBox="0 0 20 20">
                            <path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z" />
                            <path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z" />
                        </svg>
                        Dashboard
                    </button>
                )}

                {(isAdmin || user?.subscription_plan === 'premium') && (
                    <button
                        className={`tab-button ${activeTab === 'rss' ? 'active' : ''}`}
                        onClick={() => setActiveTab('rss')}
                    >
                        <svg fill="currentColor" viewBox="0 0 20 20">
                            <path fillRule="evenodd" d="M5 3a1 1 0 000 2c5.523 0 10 4.477 10 10a1 1 0 102 0C17 8.373 11.627 3 5 3z" clipRule="evenodd" />
                            <path fillRule="evenodd" d="M5 7a1 1 0 000 2 6 6 0 016 6 1 1 0 102 0 8 8 0 00-8-8z" clipRule="evenodd" />
                            <path fillRule="evenodd" d="M5 11a1 1 0 000 2 2 2 0 012 2 1 1 0 102 0 4 4 0 00-4-4z" clipRule="evenodd" />
                            <path d="M5 16a1 1 0 100 2 1 1 0 000-2z" />
                        </svg>
                        Mis Feeds RSS
                    </button>
                )}

                {(isAdmin || user?.subscription_plan === 'premium') && (
                    <button
                        className={`tab-button ${activeTab === 'analytics' ? 'active' : ''}`}
                        onClick={() => setActiveTab('analytics')}
                    >
                        <svg fill="currentColor" viewBox="0 0 20 20">
                            <path d="M2 11a1 1 0 011-1h2a1 1 0 011 1v5a1 1 0 01-1 1H3a1 1 0 01-1-1v-5zM8 7a1 1 0 011-1h2a1 1 0 011 1v9a1 1 0 01-1 1H9a1 1 0 01-1-1V7zM14 4a1 1 0 011-1h2a1 1 0 011 1v12a1 1 0 01-1 1h-2a1 1 0 01-1-1V4z" />
                        </svg>
                        Analítica
                    </button>
                )}

                {(isAdmin || user?.subscription_plan === 'premium') && (
                    <button
                        className={`tab-button ${activeTab === 'prediction' ? 'active' : ''}`}
                        onClick={() => setActiveTab('prediction')}
                    >
                        <svg fill="currentColor" viewBox="0 0 20 20">
                            <path d="M10 3.5a1.5 1.5 0 013 0V4a1 1 0 001 1h3a1 1 0 011 1v3a1 1 0 01-1 1h-.5a1.5 1.5 0 000 3h.5a1 1 0 011 1v3a1 1 0 01-1 1h-3a1 1 0 01-1-1v-.5a1.5 1.5 0 00-3 0v.5a1 1 0 01-1 1H6a1 1 0 01-1-1v-3a1 1 0 00-1-1h-.5a1.5 1.5 0 010-3H4a1 1 0 001-1V6a1 1 0 011-1h3a1 1 0 001-1v-.5z" />
                        </svg>
                        Predicción IA
                    </button>
                )}

                {isAdmin && (
                    <>
                        <button
                            className={`tab-button ${activeTab === 'scraping' ? 'active' : ''}`}
                            onClick={() => setActiveTab('scraping')}
                        >
                            <svg fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M4 2a2 2 0 00-2 2v11a3 3 0 106 0V4a2 2 0 00-2-2H4zm1 14a1 1 0 100-2 1 1 0 000 2zm5-1.757l4.9-4.9a2 2 0 000-2.828L13.485 5.1a2 2 0 00-2.828 0L10 5.757v8.486zM16 18H9.071l6-6H16a2 2 0 012 2v2a2 2 0 01-2 2z" clipRule="evenodd" />
                            </svg>
                            Scraping General
                        </button>
                        <button
                            className={`tab-button ${activeTab === 'historical' ? 'active' : ''}`}
                            onClick={() => setActiveTab('historical')}
                        >
                            <svg fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M6 2a1 1 0 00-1 1v1H4a2 2 0 00-2 2v10a2 2 0 002 2h12a2 2 0 002-2V6a2 2 0 00-2-2h-1V3a1 1 0 10-2 0v1H7V3a1 1 0 00-1-1zm0 5a1 1 0 000 2h8a1 1 0 100-2H6z" clipRule="evenodd" />
                            </svg>
                            Scraping Histórico
                        </button>
                        <button
                            className={`tab-button ${activeTab === 'export' ? 'active' : ''}`}
                            onClick={() => setActiveTab('export')}
                        >
                            <svg fill="currentColor" viewBox="0 0 20 20">
                                <path fillRule="evenodd" d="M3 17a1 1 0 011-1h12a1 1 0 110 2H4a1 1 0 01-1-1zm3.293-7.707a1 1 0 011.414 0L9 10.586V3a1 1 0 112 0v7.586l1.293-1.293a1 1 0 111.414 1.414l-3 3a1 1 0 01-1.414 0l-3-3a1 1 0 010-1.414z" clipRule="evenodd" />
                            </svg>
                            Exportar
                        </button>
                    </>
                )}
            </div>

            <div className="admin-content-new">
                {activeTab === 'dashboard' && isAdmin && <Dashboard />}
                {activeTab === 'rss' && (isAdmin || user?.subscription_plan === 'premium') && <RSSPanel />}
                {activeTab === 'analytics' && (isAdmin || user?.subscription_plan === 'premium') && <AnalyticsPanel />}
                {activeTab === 'prediction' && (isAdmin || user?.subscription_plan === 'premium') && <PredictionPanel />}
                {activeTab === 'scraping' && isAdmin && <ScrapingPanel />}
                {activeTab === 'historical' && isAdmin && <HistoricalScrapingPanel />}
                {activeTab === 'export' && isAdmin && <ExportPanel />}
            </div>
        </div>
    )
}

export default Admin
