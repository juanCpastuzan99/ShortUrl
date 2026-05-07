import { Link, useLocation } from 'react-router-dom'

export default function Navbar() {
  const { pathname } = useLocation()

  return (
    <nav className="navbar navbar-expand-lg bg-dark border-bottom border-secondary sticky-top">
      <div className="container">
        <Link className="navbar-brand text-info" to="/">
          <i className="bi bi-link-45deg"></i> Acortador de Enlaces
        </Link>
        <div className="d-flex gap-2">
          <Link
            to="/"
            className={`btn btn-sm ${pathname === '/' ? 'btn-primary' : 'btn-outline-light'}`}
          >
            <i className="bi bi-scissors"></i> Acortar
          </Link>
          <Link
            to="/report"
            className={`btn btn-sm ${pathname === '/report' ? 'btn-primary' : 'btn-outline-light'}`}
          >
            <i className="bi bi-table"></i> Reporte
          </Link>
        </div>
      </div>
    </nav>
  )
}
