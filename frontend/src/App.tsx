import { useEffect, useState } from 'react'
import {
  Activity,
  AlertTriangle,
  CheckCircle2,
  CircleDot,
  ClipboardCheck,
  Code2,
  FolderGit2,
  Gauge,
  RefreshCw,
  ShieldCheck,
  Terminal,
  Wrench,
  XCircle,
} from 'lucide-react'

import ProjectsPanel from './ProjectsPanel'
import './App.css'
import FindingsPage from './pages/FindingsPage'
import FixPlansPage from './pages/FixPlansPage'
import FixReviewPage from './pages/FixReviewPage'
import RepositoryAnalyzer from './pages/RepositoryAnalyzer'

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

type ProjectOption = {
  id: string
  name: string
  technologies?: string[]
}

const DEFAULT_PROJECT_ID = '6ad36dfc5f4d4046483cb64a'

function App() {
  const [projectId, setProjectId] = useState(DEFAULT_PROJECT_ID)
  const [projects, setProjects] = useState<ProjectOption[]>([])
  const [quality, setQuality] = useState<QualityData | null>(null)
  const [findings, setFindings] = useState<Finding[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activePage, setActivePage] = useState('Dashboard')

  async function loadProjects() {
    try {
      const res = await fetch('/api/projects')
      if (!res.ok) return
      const data = await res.json()
      const list = Array.isArray(data)
        ? data
        : Array.isArray(data.projects)
          ? data.projects
          : []
      setProjects(list)
    } catch {
      // Keep dashboard working even if projects list is unavailable
    }
  }

  async function loadDashboard() {
    if (!projectId) {
      setLoading(false)
      return
    }

    setLoading(true)
    setError('')

    try {
      const [qualityResponse, findingsResponse] = await Promise.all([
        fetch(`/api/projects/${encodeURIComponent(projectId)}/quality-score`),
        fetch(`/api/projects/${encodeURIComponent(projectId)}/findings`),
        loadProjects(),
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

  const activeProject = projects.find((p) => p.id === projectId)
  const activeProjectName =
    activeProject?.name || 'AI Software Engineering Intelligence'
  const activeProjectStack =
    activeProject?.technologies && activeProject.technologies.length > 0
      ? activeProject.technologies.join(' · ')
      : 'Java · Spring Boot · MongoDB'

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

        <button
          className={`nav-item ${activePage === 'Dashboard' ? 'active' : ''}`}
          onClick={() => setActivePage('Dashboard')}
        >
          <Gauge size={18} />
          <span>Dashboard</span>
        </button>

        <button
          className={`nav-item ${
            activePage === 'Projects & Upload' ? 'active' : ''
          }`}
          onClick={() => setActivePage('Projects & Upload')}
        >
          <FolderGit2 size={18} />
          <span>Projects &amp; Upload</span>
        </button>

        <button
          className={`nav-item ${activePage === 'Findings' ? 'active' : ''}`}
          onClick={() => setActivePage('Findings')}
        >
          <AlertTriangle size={18} />
          <span>Findings</span>
        </button>

        <button
          className={`nav-item ${activePage === 'Code Health' ? 'active' : ''}`}
          onClick={() => setActivePage('Code Health')}
        >
          <ShieldCheck size={18} />
          <span>Code Health</span>
        </button>

        <button
          className={`nav-item ${activePage === 'AI Fix Plans' ? 'active' : ''}`}
          onClick={() => setActivePage('AI Fix Plans')}
        >
          <Wrench size={18} />
          <span>AI Fix Plans</span>
        </button>

        <button
          className={`nav-item ${activePage === 'Fix Review' ? 'active' : ''}`}
          onClick={() => setActivePage('Fix Review')}
        >
          <ClipboardCheck size={18} />
          <span>Fix Review</span>
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
            <strong>
              {activePage === 'Dashboard' ? 'Project Overview' : activePage}
            </strong>
          </div>

          <div className="topbar-actions">
            {projects.length > 0 && (
              <select
                aria-label="Select Project"
                className="project-select"
                value={projectId}
                onChange={(e) => setProjectId(e.target.value)}
              >
                {projects.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.name}
                  </option>
                ))}
              </select>
            )}

            <button
              className="refresh-button"
              onClick={() => void loadDashboard()}
              disabled={loading}
            >
              <RefreshCw size={15} className={loading ? 'spinning' : ''} />
              {loading ? 'Loading...' : 'Refresh Data'}
            </button>
          </div>
        </header>

        {activePage === 'Projects & Upload' ? (
          <>
            <section className="welcome-section">
              <div>
                <div className="eyebrow">
                  <FolderGit2 size={14} />
                  PROJECT &amp; ZIP UPLOAD WORKSPACE
                </div>
                <h1>Projects &amp; ZIP Upload</h1>
                <p>
                  Project select ya create karo, source ZIP upload karke static
                  analysis chalao, ya repository scan karo.
                </p>
              </div>

              <div className="project-badge">
                <span className="project-badge-dot" />
                Active: {activeProjectName}
              </div>
            </section>

            <ProjectsPanel
              projectId={projectId}
              onProjectChange={(newId) => {
                setProjectId(newId)
                void loadProjects()
              }}
            />

            <section style={{ marginTop: '24px' }}>
              <RepositoryAnalyzer />
            </section>
          </>
        ) : activePage === 'Findings' ? (
          <FindingsPage projectId={projectId} />
        ) : activePage === 'Code Health' ? (
          <>
            <section className="welcome-section">
              <div>
                <div className="eyebrow">
                  <ShieldCheck size={14} />
                  QUALITY METRICS
                </div>
                <h1>Code Health</h1>
                <p>
                  Monitor {activeProjectName}&apos;s code quality and severity
                  distribution.
                </p>
              </div>

              <div className="project-badge">
                <span className="project-badge-dot" />
                Project Connected
              </div>
            </section>

            <section className="stats-grid">
              <article className="stat-card">
                <div className="stat-top">
                  <span>Quality Score</span>
                </div>
                <strong className="stat-number">{score}%</strong>
                <small>{quality?.rating ?? 'Awaiting data'}</small>
              </article>

              <article className="stat-card">
                <div className="stat-top">
                  <span>Total Findings</span>
                </div>
                <strong className="stat-number">
                  {quality?.totalFindings ?? findings.length}
                </strong>
                <small>All recorded issues</small>
              </article>

              <article className="stat-card">
                <div className="stat-top">
                  <span>Open Findings</span>
                </div>
                <strong className="stat-number">{openCount}</strong>
                <small>Pending fix &amp; verification</small>
              </article>

              <article className="stat-card">
                <div className="stat-top">
                  <span>Resolved Findings</span>
                </div>
                <strong className="stat-number">{resolvedCount}</strong>
                <small>Verified resolved issues</small>
              </article>
            </section>
          </>
        ) : activePage === 'AI Fix Plans' ? (
          <FixPlansPage projectId={projectId} />
        ) : activePage === 'Fix Review' ? (
          <FixReviewPage projectId={projectId} />
        ) : (
          <>
            <section className="welcome-section">
              <div>
                <div className="eyebrow">
                  <Activity size={14} />
                  SOFTWARE INTELLIGENCE PLATFORM
                </div>
                <h1>Project Dashboard</h1>
                <p>
                  Monitor code quality, detect issues and track recovery
                  progress.
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
                    Check karo ki Spring Boot backend port 8080 par chal raha
                    hai.
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
                    <Gauge size={19} />
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
                    <CircleDot size={19} />
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
                    <AlertTriangle size={19} />
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
                    <CheckCircle2 size={19} />
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
                    <strong>{activeProjectName}</strong>
                  </div>
                </div>

                <div className="info-item">
                  <span className="info-icon">
                    <Terminal size={18} />
                  </span>
                  <div>
                    <small>Stack</small>
                    <strong>{activeProjectStack}</strong>
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
                  <p>Issues returned by your project&apos;s findings API</p>
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

            <footer className="footer">
              <span>DevRecovery · Engineering Intelligence Platform</span>
              <span>AI-assisted analysis · Human-approved fixes</span>
            </footer>
          </>
        )}
      </main>
    </div>
  )
}

export default App