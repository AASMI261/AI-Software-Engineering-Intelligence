import { useEffect, useState } from 'react'
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  CircleDot,
  Code2,
  FolderGit2,
  Gauge,
  RefreshCw,
  ShieldCheck,
  Terminal,
  XCircle,
} from 'lucide-react'

import ProjectsPanel from './ProjectsPanel'
import FindingsPanel from './components/FindingsPanel'
import './App.css'

type QualityData = {
  projectId?: string
  qualityScore?: number
  rating?: string
  totalFindings?: number
  openFindings?: number
  resolvedFindings?: number
  severityCounts?: Record<string, number>
}

type Finding = {
  id?: string
  _id?: string
  title?: string
  message?: string
  description?: string
  ruleId?: string
  rule?: string
  severity?: string
  status?: string
  filePath?: string
  createdAt?: string
}

function App() {
   const [projectId, setProjectId] = useState(
    '6ad36dfc5f4d4046483cb64a'
  )
  const [quality, setQuality] = useState<QualityData | null>(null)
  const [findings, setFindings] = useState<Finding[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  async function loadDashboard() {
    setLoading(true)
    setError('')

    try {
      const [qualityResponse, findingsResponse] = await Promise.all([
        fetch(`/api/projects/${projectId}/quality-score`),
        fetch(`/api/projects/${projectId}/findings`),
      ])

      if (!qualityResponse.ok) {
        throw new Error(
          `Quality Score API failed: HTTP ${qualityResponse.status}`,
        )
      }

      if (!findingsResponse.ok) {
        throw new Error(
          `Findings API failed: HTTP ${findingsResponse.status}`,
        )
      }

      const qualityData: QualityData = await qualityResponse.json()
      const findingsData = await findingsResponse.json()

      setQuality(qualityData)

      // Supports APIs returning either a list or an object containing findings.
      const list = Array.isArray(findingsData)
        ? findingsData
        : Array.isArray(findingsData.findings)
          ? findingsData.findings
          : []

      setFindings(list)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Dashboard data load nahi ho paya.',
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
  void loadDashboard()
}, [projectId])

  const openCount =
    quality?.openFindings ??
    findings.filter((item) => item.status?.toUpperCase() === 'OPEN').length

  const resolvedCount =
    quality?.resolvedFindings ??
    findings.filter((item) => item.status?.toUpperCase() === 'RESOLVED').length

  const severityCounts = quality?.severityCounts ?? {}

  const severityTotal = Object.values(severityCounts).reduce(
    (sum, count) => sum + count,
    0,
  )

  const severityValue = (name: string) =>
    Object.entries(severityCounts).find(
      ([key]) => key.toLowerCase() === name.toLowerCase(),
    )?.[1] ?? 0

  const score = quality?.qualityScore ?? 0

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">
            <Code2 size={24} />
          </div>
          <div>
            <h2>DevRecovery</h2>
            <span>ENGINEERING INTELLIGENCE</span>
          </div>
        </div>

        <div className="workspace-label">WORKSPACE</div>

        <button className="nav-item active">
          <Gauge size={18} />
          <span>Dashboard</span>
        </button>

        <button
          className="nav-item"
          onClick={() =>
            document
              .getElementById('findings-section')
              ?.scrollIntoView({ behavior: 'smooth' })
          }
        >
          <AlertTriangle size={18} />
          <span>Findings</span>
        </button>

        <button
          className="nav-item"
          onClick={() =>
            document
              .getElementById('health-section')
              ?.scrollIntoView({ behavior: 'smooth' })
          }
        >
          <ShieldCheck size={18} />
          <span>Code Health</span>
        </button>

        <div className="sidebar-bottom">
          <div className="connection-indicator">
            <span className="status-dot" />
            <div>
              <strong>Backend connection</strong>
              <small>{error ? 'Check connection' : 'Spring Boot API'}</small>
            </div>
          </div>
          <div className="sidebar-version">PROJECT MONITOR · V1.0</div>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">
            <FolderGit2 size={17} />
            <span>Workspace</span>
            <span className="breadcrumb-divider">/</span>
            <strong>Project Overview</strong>
          </div>

          <button
            className="refresh-button"
            onClick={() => void loadDashboard()}
            disabled={loading}
          >
            <RefreshCw size={16} className={loading ? 'spinning' : ''} />
            {loading ? 'Loading...' : 'Refresh Data'}
          </button>
        </header>

        <section className="welcome-section">
          <div>
            <div className="eyebrow">
              <Activity size={15} />
              SOFTWARE INTELLIGENCE PLATFORM
            </div>
            <h1>Project Dashboard</h1>
            <p>
              Monitor code quality, detect issues and track recovery progress.
            </p>
          </div>

          <div className="project-badge">
            <span className="project-badge-dot" />
            Project Connected
          </div>
        </section>

        {error && (
          <div className="error-banner">
            <XCircle size={19} />
            <div>
              <strong>Data load nahi ho paya</strong>
              <p>{error}</p>
              <small>
                Check karo ki Spring Boot backend port 8080 par chal raha hai.
              </small>
            </div>
            <button onClick={() => void loadDashboard()}>Retry</button>
          </div>
        )}

        <section className="stats-grid">
          <article className="stat-card score-card">
            <div className="stat-top">
              <span>Code Quality Score</span>
              <div className="stat-icon purple">
                <Gauge size={20} />
              </div>
            </div>
            <div className="score-row">
              <strong>{loading ? '—' : `${score}%`}</strong>
              <span className="score-rating">
                {quality?.rating ?? 'Awaiting data'}
              </span>
            </div>
            <div className="progress-track">
              <div
                className="progress-fill"
                style={{ width: `${Math.min(100, Math.max(0, score))}%` }}
              />
            </div>
            <small>Based on the backend quality score</small>
          </article>

          <article className="stat-card">
            <div className="stat-top">
              <span>Total Findings</span>
              <div className="stat-icon blue">
                <CircleDot size={20} />
              </div>
            </div>
            <strong className="stat-number">
              {loading ? '—' : (quality?.totalFindings ?? findings.length)}
            </strong>
            <small>Issues recorded for this project</small>
          </article>

          <article className="stat-card">
            <div className="stat-top">
              <span>Open Findings</span>
              <div className="stat-icon orange">
                <AlertTriangle size={20} />
              </div>
            </div>
            <strong className="stat-number">
              {loading ? '—' : openCount}
            </strong>
            <small>Issues awaiting resolution</small>
          </article>

          <article className="stat-card">
            <div className="stat-top">
              <span>Resolved Findings</span>
              <div className="stat-icon green">
                <CheckCircle2 size={20} />
              </div>
            </div>
            <strong className="stat-number">
              {loading ? '—' : resolvedCount}
            </strong>
            <small>Issues marked as resolved</small>
          </article>
        </section>

        <section className="content-grid" id="health-section">
          <article className="panel severity-panel">
            <div className="panel-heading">
              <div>
                <h2>Severity Overview</h2>
                <p>Breakdown reported by the quality API</p>
              </div>
              <ShieldCheck size={20} className="panel-heading-icon" />
            </div>

            <div className="severity-total">
              <strong>{loading ? '—' : severityTotal}</strong>
              <span>severity-counted findings</span>
            </div>

            {[
              { name: 'Critical', color: 'critical' },
              { name: 'High', color: 'high' },
              { name: 'Medium', color: 'medium' },
              { name: 'Low', color: 'low' },
            ].map((item) => {
              const count = severityValue(item.name)
              const percentage =
                severityTotal > 0 ? (count / severityTotal) * 100 : 0

              return (
                <div className="severity-row" key={item.name}>
                  <div className="severity-label">
                    <span className={`severity-dot ${item.color}`} />
                    <span>{item.name}</span>
                    <strong>{loading ? '—' : count}</strong>
                  </div>
                  <div className="severity-track">
                    <div
                      className={`severity-fill ${item.color}`}
                      style={{ width: `${percentage}%` }}
                    />
                  </div>
                </div>
              )
            })}

            <p className="panel-note">
              Severity counts reflect the categories returned by your backend.
            </p>
          </article>

          <article className="panel project-panel">
            <div className="panel-heading">
              <div>
                <h2>Project Information</h2>
                <p>Current project configuration</p>
              </div>
              <FolderGit2 size={20} className="panel-heading-icon" />
            </div>

            <div className="info-item">
              <span className="info-icon">
                <Code2 size={18} />
              </span>
              <div>
                <small>Project</small>
                <strong>AI Software Engineering Intelligence</strong>
              </div>
            </div>

            <div className="info-item">
              <span className="info-icon">
                <Terminal size={18} />
              </span>
              <div>
                <small>Backend</small>
                <strong>Java · Spring Boot</strong>
              </div>
            </div>

            <div className="info-item">
              <span className="info-icon">
                <Activity size={18} />
              </span>
              <div>
                <small>Database / AI</small>
                <strong>MongoDB · Ollama integration</strong>
              </div>
            </div>

            <div className="project-id">
              <span>Project ID</span>
              <code>{projectId}</code>
            </div>
          </article>
        </section>

        <section className="panel findings-panel" id="findings-section">
          <div className="panel-heading findings-heading">
            <div>
              <h2>Recent Findings</h2>
              <p>Issues returned by your project's findings API</p>
            </div>
            <span className="table-count">
              {loading ? 'Loading...' : `${findings.length} records`}
            </span>
          </div>

          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>RULE / ISSUE</th>
                  <th>FILE PATH</th>
                  <th>SEVERITY</th>
                  <th>STATUS</th>
                </tr>
              </thead>
              <tbody>
                {loading ? (
                  <tr>
                    <td colSpan={4} className="table-message">
                      Loading findings from backend...
                    </td>
                  </tr>
                ) : findings.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="table-message">
                      {error
                        ? 'Findings load nahi hue. Backend connection check karo.'
                        : 'No findings returned by the API.'}
                    </td>
                  </tr>
                ) : (
                  findings.map((finding, index) => {
                    const severity = (
                      finding.severity ?? 'UNKNOWN'
                    ).toLowerCase()
                    const status = (
                      finding.status ?? 'UNKNOWN'
                    ).toLowerCase()

                    return (
                      <tr key={finding.id ?? finding._id ?? index}>
                        <td>
                          <div className="finding-title">
                            <strong>
                              {finding.ruleId ??
                                finding.rule ??
                                finding.title ??
                                'Code finding'}
                            </strong>
                            <small>
                              {finding.message ??
                                finding.description ??
                                'No description provided'}
                            </small>
                          </div>
                        </td>
                        <td>
                          <code className="file-path">
                            {finding.filePath ?? 'Not specified'}
                          </code>
                        </td>
                        <td>
                          <span className={`pill severity-pill ${severity}`}>
                            {finding.severity ?? 'Unknown'}
                          </span>
                        </td>
                        <td>
                          <span className={`pill status-pill ${status}`}>
                            {finding.status ?? 'Unknown'}
                          </span>
                        </td>
                      </tr>
                    )
                  })
                )}
              </tbody>
            </table>
          </div>

          <div className="table-footer">
            <span>
              <span className="status-dot" />
              Data fetched from Spring Boot
            </span>
            <button
              className="text-button"
              onClick={() => void loadDashboard()}
              disabled={loading}
            >
              <RefreshCw size={14} />
              Refresh findings
            </button>
          </div>
        </section>
      <section id="ai-findings-section">
  <FindingsPanel projectId={projectId} />
</section>
        <footer className="footer">
          <span>DevRecovery · Engineering Intelligence Platform</span>
          <span>AI-assisted analysis · Human-approved fixes</span>
        </footer>
            <ProjectsPanel
  projectId={projectId}
  onProjectChange={setProjectId}
/>
      </main>
    </div>
  )
}

export default App