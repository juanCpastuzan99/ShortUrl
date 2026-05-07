import { useState, useEffect } from 'react'

const URL_REGEX = /^(https?|ftp):\/\/[^\s/$.?#].[^\s]*$/i

export default function Home() {
  // ── Live dashboard ──────────────────────────────────────────────────────────
  const [status, setStatus] = useState(null)
  const [lastUpdate, setLastUpdate] = useState('--:--:--')

  useEffect(() => {
    const refresh = () => {
      fetch('/api/status')
        .then(r => r.json())
        .then(s => {
          setStatus(s)
          setLastUpdate(new Date().toLocaleTimeString())
        })
        .catch(() => {})
    }
    refresh()
    const id = setInterval(refresh, 3000)
    return () => clearInterval(id)
  }, [])

  // ── Form state ──────────────────────────────────────────────────────────────
  const [form, setForm] = useState({ originalUrl: '', imageUrl: '', description: '' })
  const [errors, setErrors] = useState({})
  const [loading, setLoading] = useState(false)
  const [result, setResult] = useState(null)
  const [copied, setCopied] = useState(false)
  const [imagePreview, setImagePreview] = useState('')

  // ── Validation helpers ──────────────────────────────────────────────────────
  function validateUrl(val) {
    if (!val.trim()) return 'Este campo es obligatorio.'
    if (!URL_REGEX.test(val.trim())) return 'URL inválida (debe iniciar con http:// o https://).'
    return ''
  }

  function validateDescription(val) {
    if (!val.trim()) return 'La descripción es obligatoria.'
    const words = val.trim().split(/\s+/).filter(w => w.length > 0)
    if (words.length < 5) return `Mínimo 5 palabras (llevas ${words.length}).`
    if (val.length > 500) return 'Máximo 500 caracteres.'
    return ''
  }

  function handleBlur(field) {
    const val = form[field]
    const err = field === 'description' ? validateDescription(val) : validateUrl(val)
    setErrors(prev => ({ ...prev, [field]: err }))
    if (field === 'imageUrl') {
      setImagePreview(URL_REGEX.test(val.trim()) ? val.trim() : '')
    }
  }

  // ── Submit ──────────────────────────────────────────────────────────────────
  async function handleSubmit(e) {
    e.preventDefault()
    const urlErr  = validateUrl(form.originalUrl)
    const imgErr  = validateUrl(form.imageUrl)
    const descErr = validateDescription(form.description)
    setErrors({ originalUrl: urlErr, imageUrl: imgErr, description: descErr })
    if (urlErr || imgErr || descErr) return

    setLoading(true)
    setResult(null)
    try {
      const resp = await fetch('/api/links', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          originalUrl: form.originalUrl.trim(),
          imageUrl:    form.imageUrl.trim(),
          description: form.description.trim()
        })
      })
      if (!resp.ok) {
        const err = await resp.json()
        alert('Error: ' + Object.values(err).join(' | '))
        return
      }
      const data = await resp.json()
      setResult(data)
      setForm(prev => ({ ...prev, originalUrl: data.shortUrl }))
    } catch {
      alert('Error de conexión con el servidor.')
    } finally {
      setLoading(false)
    }
  }

  // ── Copy to clipboard ───────────────────────────────────────────────────────
  async function handleCopy() {
    try { await navigator.clipboard.writeText(result?.shortUrl || form.originalUrl) } catch { /* ignore */ }
    setCopied(true)
    setTimeout(() => setCopied(false), 2000)
  }

  // ── Dashboard helpers ───────────────────────────────────────────────────────
  const DB_META = [
    { key: 'mysql', label: 'MySQL',   color: 'text-info',    unit: 'enlaces'   },
    { key: 'mongo', label: 'MongoDB', color: 'text-success', unit: 'metadatos' },
    { key: 'redis', label: 'Redis',   color: 'text-warning', unit: 'cacheados' },
  ]

  return (
    <main className="container py-5">
      {/* Hero */}
      <div className="text-center mb-4">
        <h1 className="display-4 fw-bold hero-title">Acorta tus enlaces</h1>
        <p className="lead text-secondary">Transforma URLs largas en enlaces cortos y fáciles de compartir</p>
      </div>

      {/* ── Live dashboard ── */}
      <div className="status-panel p-3 mb-4 mx-auto" style={{ maxWidth: 720 }}>
        <div className="d-flex justify-content-between align-items-center mb-2">
          <small className="text-secondary fw-bold">
            <i className="bi bi-broadcast"></i> ESTADO DEL SISTEMA EN VIVO
          </small>
          <small className="text-secondary">{lastUpdate}</small>
        </div>

        <div className="row g-2 text-center">
          {DB_META.map(({ key, label, color, unit }) => {
            const up    = status?.[key]?.up
            const count = status ? (up ? status[key].count : 'X') : '--'
            return (
              <div className="col-4" key={key}>
                <div className="bd-card p-2 rounded border border-secondary">
                  <div>
                    <span className={`status-dot ${status ? (up ? 'up' : 'down') : 'down'}`}></span>
                    <strong>{label}</strong>
                  </div>
                  <div className={`fs-4 fw-bold ${color}`}>{count}</div>
                  <small className="text-secondary">{unit}</small>
                </div>
              </div>
            )
          })}
        </div>

        <div className="text-center mt-2">
          <small className="text-secondary">
            <i className="bi bi-cpu"></i> Hilos activos JVM:{' '}
            <span className="text-info fw-bold">{status?.threads?.active ?? '--'}</span>
          </small>
        </div>
      </div>

      {/* ── Shorten form ── */}
      <div className="card bg-dark border-secondary shadow-lg mx-auto" style={{ maxWidth: 720 }}>
        <div className="card-body p-4 p-md-5">
          <form onSubmit={handleSubmit} noValidate>

            {/* Original URL */}
            <div className="mb-4">
              <label className="form-label fw-semibold">
                <i className="bi bi-globe text-info"></i> Enlace original
              </label>
              <div className="input-group">
                <input
                  type="text"
                  className={`form-control form-control-lg ${errors.originalUrl ? 'is-invalid' : result ? 'is-valid' : ''}`}
                  placeholder="https://www.ejemplo.com/enlace-largo..."
                  value={form.originalUrl}
                  onChange={e => setForm(p => ({ ...p, originalUrl: e.target.value }))}
                  onBlur={() => handleBlur('originalUrl')}
                  autoComplete="off"
                />
                {result && (
                  <button type="button" className="btn btn-info" onClick={handleCopy}>
                    <i className={`bi ${copied ? 'bi-check-lg' : 'bi-clipboard'}`}></i>{' '}
                    {copied ? 'Copiado' : 'Copiar'}
                  </button>
                )}
              </div>
              {errors.originalUrl && (
                <div className="text-danger small mt-1">{errors.originalUrl}</div>
              )}
              {result && (
                <div className="alert alert-success mt-2 d-flex align-items-center gap-2">
                  <i className="bi bi-check-circle-fill"></i>
                  <span>
                    Enlace acortado:{' '}
                    <a href={result.shortUrl} target="_blank" rel="noreferrer" className="alert-link">
                      {result.shortUrl}
                    </a>
                  </span>
                </div>
              )}
            </div>

            {/* Image URL */}
            <div className="mb-4">
              <label className="form-label fw-semibold">
                <i className="bi bi-image text-warning"></i> Enlace de imagen
              </label>
              <input
                type="text"
                className={`form-control form-control-lg ${errors.imageUrl ? 'is-invalid' : ''}`}
                placeholder="https://www.ejemplo.com/imagen.jpg"
                value={form.imageUrl}
                onChange={e => setForm(p => ({ ...p, imageUrl: e.target.value }))}
                onBlur={() => handleBlur('imageUrl')}
                autoComplete="off"
              />
              {errors.imageUrl && (
                <div className="text-danger small mt-1">{errors.imageUrl}</div>
              )}
              {imagePreview && (
                <div className="mt-2">
                  <img
                    src={imagePreview}
                    alt="preview"
                    className="img-preview"
                    onError={() => setImagePreview('')}
                  />
                </div>
              )}
            </div>

            {/* Description */}
            <div className="mb-4">
              <label className="form-label fw-semibold">
                <i className="bi bi-card-text text-success"></i> Descripción
              </label>
              <textarea
                className={`form-control ${errors.description ? 'is-invalid' : ''}`}
                rows={3}
                placeholder="Mínimo 5 palabras, máximo 500 caracteres..."
                maxLength={500}
                value={form.description}
                onChange={e => setForm(p => ({ ...p, description: e.target.value }))}
                onBlur={() => handleBlur('description')}
              />
              <div className="d-flex justify-content-between mt-1">
                <small className="text-danger">{errors.description}</small>
                <small className="text-secondary">{form.description.length}/500</small>
              </div>
            </div>

            <button type="submit" className="btn btn-primary btn-lg w-100 fw-bold" disabled={loading}>
              {loading ? (
                <><span className="spinner-border spinner-border-sm me-2"></span>Procesando...</>
              ) : (
                <><i className="bi bi-scissors me-1"></i>ACORTAR</>
              )}
            </button>
          </form>
        </div>
      </div>

      {/* Feature badges */}
      <div className="row g-3 mt-4">
        {[
          { icon: 'bi-lightning-charge-fill', color: 'text-warning', title: 'Rápido',    desc: 'Procesamiento concurrente con @Async' },
          { icon: 'bi-database-fill',         color: 'text-info',    title: '3 BD',       desc: 'MySQL · MongoDB · Redis' },
          { icon: 'bi-hexagon-fill',           color: 'text-success', title: 'Hexagonal', desc: 'Domain · Application · Infrastructure' },
        ].map(({ icon, color, title, desc }) => (
          <div className="col-md-4" key={title}>
            <div className="card bg-dark border-secondary text-center h-100">
              <div className="card-body">
                <i className={`bi ${icon} ${color} fs-1`}></i>
                <h6 className="mt-2 fw-bold">{title}</h6>
                <small className="text-secondary">{desc}</small>
              </div>
            </div>
          </div>
        ))}
      </div>
    </main>
  )
}
