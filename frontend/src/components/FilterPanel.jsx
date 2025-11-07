import { useState } from 'react'
import './FilterPanel.css'

function FilterPanel({ filters, sources, onFilterChange }) {
  const [localFilters, setLocalFilters] = useState(filters)

  const handleChange = (field, value) => {
    setLocalFilters(prev => ({ ...prev, [field]: value }))
  }

  const handleApply = () => {
    onFilterChange(localFilters)
  }

  const handleReset = () => {
    const resetFilters = {
      source: '',
      startDate: '',
      endDate: '',
      search: ''
    }
    setLocalFilters(resetFilters)
    onFilterChange(resetFilters)
  }

  return (
    <div className="filter-panel">
      <h2>🔎 Filtrar Artículos</h2>
      <div className="filters-grid">
        <div className="filter-group">
          <label htmlFor="filter-source">Fuente:</label>
          <select
            id="filter-source"
            value={localFilters.source}
            onChange={(e) => handleChange('source', e.target.value)}
          >
            <option value="">Todas</option>
            {sources.map(source => (
              <option key={source.name} value={source.name}>
                {source.name}
              </option>
            ))}
          </select>
        </div>

        <div className="filter-group">
          <label htmlFor="start-date">Fecha desde:</label>
          <input
            id="start-date"
            type="date"
            value={localFilters.startDate}
            onChange={(e) => handleChange('startDate', e.target.value)}
          />
        </div>

        <div className="filter-group">
          <label htmlFor="end-date">Fecha hasta:</label>
          <input
            id="end-date"
            type="date"
            value={localFilters.endDate}
            onChange={(e) => handleChange('endDate', e.target.value)}
          />
        </div>

        <div className="filter-group filter-search">
          <label htmlFor="search">Buscar:</label>
          <input
            id="search"
            type="text"
            placeholder="Buscar en título o contenido..."
            value={localFilters.search}
            onChange={(e) => handleChange('search', e.target.value)}
          />
        </div>

        <div className="filter-actions">
          <button onClick={handleApply} className="btn-primary">
            Aplicar Filtros
          </button>
          <button onClick={handleReset} className="btn-secondary">
            Limpiar
          </button>
        </div>
      </div>
    </div>
  )
}

export default FilterPanel
