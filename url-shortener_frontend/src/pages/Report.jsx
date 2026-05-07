import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'

function truncate(str, max) {
  return str.length > max ? str.slice(0, max) + '…' : str
}

export default function Report() {
  const [links, setLinks]   = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError]   = useState('')

  async function loadReport() {
    setLoading(true)
    setError('')
    try {
      const resp = await fetch('/api/links')
      if (!resp.ok) throw new Error('Error al cargar el reporte')
      setLinks(await resp.json())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { loadReport() }, [])

  const withImage = links.filter(l => l.imageUrl).length

  return (
    <main className="container py-5">
      <div className="text-center mb-5">
        <h1 className="display-5 fw-bold hero-title">Reporte de Enlaces</h1>
        <p className="lead text-secondary">Historial completo de enlaces acortados</p>
      </div>

      <div className="card bg-dark border-secondary shadow-lg">
        <div className="card-body p-4">

          {/* Stats + refresh */}
          <div className="d-flex justify-content-between align-items-center flex-wrap gap-3 mb-4">
            <div className="d-flex gap-4">
              <div>
                <div className="stat-num text-info">{links.length}</div>
                <small className="text-secondary">Total enlaces</small>
              </div>
              <div>
                <div className="stat-num text-warning">{withImage}</div>
                <small className="text-secondary">Con imagen</small>
              </div>
            </div>
            <button className="btn btn-outline-info" onClick={loadReport} disabled={loading}>
              <i className={`bi ${loading ? 'bi-hourglass-split' : 'bi-arrow-clockwise'}`}></i> Actualizar
            </button>
          </div>

          {/* Loading */}
          {loading && (
            <div className="text-center py-5 text-secondary">
              <div className="spinner-border text-info" role="status"></div>
              <p className="mt-2">Cargando enlaces...</p>
            </div>
          )}

          {/* Error */}
          {!loading && error && (
            <div className="alert alert-danger">
              <i className="bi bi-exclamation-triangle"></i> {error}
            </div>
          )}

          {/* Empty */}
          {!loading && !error && links.length === 0 && (
            <div className="text-center py-5">
              <i className="bi bi-inbox fs-1 text-secondary"></i>
              <p className="text-secondary mt-3">No hay enlaces registrados aún.</p>
              <Link to="/" className="btn btn-primary">
                <i className="bi bi-scissors"></i> Acortar primer enlace
              </Link>
            </div>
          )}

          {/* Table */}
          {!loading && !error && links.length > 0 && (
            <div className="table-responsive">
              <table className="table table-dark table-hover table-striped align-middle">
                <thead className="text-info">
                  <tr>
                    <th>#</th>
                    <th>Imagen</th>
                    <th>Enlace Original</th>
                    <th>Enlace Acortado</th>
                    <th>Descripción</th>
                    <th>Fecha</th>
                  </tr>
                </thead>
                <tbody>
                  {links.map((link, idx) => {
                    const date = link.createdAt
                      ? new Date(link.createdAt).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' })
                      : 'N/A'
                    return (
                      <tr key={link.shortCode || idx}>
                        <td><span className="badge bg-secondary">{idx + 1}</span></td>
                        <td>
                          {link.imageUrl
                            ? <img src={link.imageUrl} alt="img" className="table-img"
                                onError={e => { e.target.style.display = 'none' }} />
                            : <i className="bi bi-image text-secondary"></i>}
                        </td>
                        <td className="url-cell" title={link.originalUrl}>
                          <a href={link.originalUrl} target="_blank" rel="noreferrer"
                            className="link-info text-decoration-none">
                            {truncate(link.originalUrl, 45)}
                          </a>
                        </td>
                        <td>
                          <a href={link.shortUrl} target="_blank" rel="noreferrer"
                            className="link-warning text-decoration-none fw-bold">
                            {link.shortUrl}
                          </a>
                        </td>
                        <td className="desc-cell text-secondary" title={link.description || ''}>
                          {truncate(link.description || 'Sin descripción', 60)}
                        </td>
                        <td><small className="text-secondary">{date}</small></td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}

        </div>
      </div>
    </main>
  )
}
