import { Link } from 'react-router-dom'
import { useState } from 'react'
import './Navbar.css'

function Navbar() {
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  const toggleMenu = () => {
    setIsMenuOpen(!isMenuOpen)
  }

  const getCurrentDate = () => {
    return new Date().toLocaleDateString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  }

  return (
    <>
      {/* Barra superior roja */}
      <div className="top-bar"></div>

      <header className="navbar">
        <div className="navbar-container">
          {/* Logo y marca */}
          <div className="navbar-brand">
            <Link to="/" className="brand-logo">
              <div className="logo-icon">
                <svg fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M2 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 002 2H4a2 2 0 01-2-2V5zm3 1h6v4H5V6zm6 6H5v2h6v-2z" clipRule="evenodd"/>
                </svg>
              </div>
              <div className="brand-text">
                <h1 className="brand-title">RED NOTICIAS</h1>
                <p className="brand-subtitle">Portal Informativo</p>
              </div>
            </Link>
          </div>

          {/* Información de fecha - Desktop */}
          <div className="navbar-date">
            <div className="date-text">{getCurrentDate()}</div>
            <div className="date-subtitle">Edición Digital</div>
          </div>

          {/* Navegación - Desktop */}
          <nav className="navbar-nav">
            <Link to="/" className="nav-link">
              <svg className="nav-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M2 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 002 2H4a2 2 0 01-2-2V5zm3 1h6v4H5V6zm6 6H5v2h6v-2z" clipRule="evenodd"/>
              </svg>
              <span>Portada</span>
            </Link>
            <Link to="/admin" className="nav-link admin-link">
              <svg className="nav-icon" fill="currentColor" viewBox="0 0 20 20">
                <path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z"/>
                <path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z"/>
              </svg>
              <span>Centro de Control</span>
            </Link>
          </nav>

          {/* Botón menú móvil */}
          <button className="mobile-menu-btn" onClick={toggleMenu}>
            <span className={`hamburger ${isMenuOpen ? 'open' : ''}`}>
              <span></span>
              <span></span>
              <span></span>
            </span>
          </button>
        </div>

        {/* Menú móvil */}
        <div className={`mobile-menu ${isMenuOpen ? 'open' : ''}`}>
          <nav className="mobile-nav">
            <Link to="/" className="mobile-nav-link" onClick={() => setIsMenuOpen(false)}>
              <svg className="nav-icon" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M2 5a2 2 0 012-2h8a2 2 0 012 2v10a2 2 0 002 2H4a2 2 0 01-2-2V5zm3 1h6v4H5V6zm6 6H5v2h6v-2z" clipRule="evenodd"/>
              </svg>
              Portada
            </Link>
            <Link to="/admin" className="mobile-nav-link admin-link" onClick={() => setIsMenuOpen(false)}>
              <svg className="nav-icon" fill="currentColor" viewBox="0 0 20 20">
                <path d="M2 10a8 8 0 018-8v8h8a8 8 0 11-16 0z"/>
                <path d="M12 2.252A8.014 8.014 0 0117.748 8H12V2.252z"/>
              </svg>
              Centro de Control
            </Link>
          </nav>

          <div className="mobile-menu-footer">
            <div className="mobile-date">{getCurrentDate()}</div>
            <div className="mobile-subtitle">Edición Digital • Portal Informativo</div>
          </div>
        </div>
      </header>

      {/* Barra de noticias destacadas */}
      <div className="breaking-news-bar">
        <div className="breaking-news-content">
          <span className="breaking-badge">ÚLTIMA HORA</span>
          <div className="breaking-text">
            <span>Noticias actualizadas en tiempo real desde múltiples fuentes</span>
            <span className="separator">•</span>
            <span>Cobertura nacional e internacional</span>
            <span className="separator">•</span>
            <span>Videos y artículos verificados</span>
          </div>
        </div>
      </div>
    </>
  )
}

export default Navbar
