import { useEffect, useMemo, useState } from 'react'
import {
  AlertTriangle,
  CheckCircle2,
  CircleDot,
  RefreshCw,
  Search,
  ShieldAlert,
} from 'lucide-react'

import { getProjectFindings, type Finding } from '../api'
import FindingsPanel from '../components/FindingsPanel'
import './FindingsPage.css'

type FindingsPageProps = {
  projectId: string
}

export default function FindingsPage({ projectId }: FindingsPageProps) {
  const [findings, setFindings] = useState<Finding[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [severityFilter, setSeverityFilter] = useState('ALL')
  const [statusFilter, setStatusFilter] = useState('ALL')

  async function loadFindings() {
    if (!projectId) {
      setFindings([])
      setLoading(false)
      return
    }

    setLoading(true)
    setError('')

    try {
      const list = await getProjectFindings(projectId)
      setFindings(list)
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Findings load nahi ho paaye.',
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadFindings()
  }, [projectId])

  const filteredFindings = useMemo(() => {
    const query = search.trim().toLowerCase()

    return findings.filter((finding) => {
      const rule = finding.rule ?? ''
      const message = finding.message ?? ''
      const file = finding.filePath ?? ''
      const severity = (finding.severity ?? '').toUpperCase()
      const status = (finding.status ?? '').toUpperCase()

      const matchesSearch =
        !query ||
        rule.toLowerCase().includes(query) ||
        message.toLowerCase().includes(query) ||
        file.toLowerCase().includes(query)

      const matchesSeverity =
        severityFilter === 'ALL' || severity === severityFilter

      const matchesStatus =
        statusFilter === 'ALL' || status === statusFilter

      return matchesSearch && matchesSeverity && matchesStatus
    })
  }, [findings, search, severityFilter, statusFilter])

  const openCount = findings.filter(
    (finding) => (finding.status ?? '').toUpperCase() === 'OPEN',
  ).length

  const resolvedCount = findings.filter(
    (finding) => (finding.status ?? '').toUpperCase() === 'RESOLVED',
  ).length

  const highSeverityCount = findings.filter((finding) =>
    ['CRITICAL', 'HIGH'].includes((finding.severity ?? '').toUpperCase()),
  ).length

  return (
    <div className="findings-page">
      <header className="findings-page-header">
        <div>
          <span className="findings-eyebrow">SOFTWARE INTELLIGENCE</span>
          <h1>Findings</h1>
          <p>
            Search code issues, filter by severity, and inspect findings
            detected in your selected project.
          </p>
        </div>

        <button
          className="findings-refresh-button"
          onClick={() => void loadFindings()}
          disabled={loading}
        >
          <RefreshCw size={16} className={loading ? 'findings-spin' : ''} />
          {loading ? 'Loading...' : 'Refresh'}
        </button>
      </header>

      {error && (
        <div className="findings-error">
          <AlertTriangle size={18} />
          <span>{error}</span>
          <button onClick={() => void loadFindings()}>Retry</button>
        </div>
      )}

      <section className="findings-summary">
        <article className="findings-summary-card">
          <CircleDot size={21} />
          <span>Total Findings</span>
          <strong>{loading ? '—' : findings.length}</strong>
        </article>

        <article className="findings-summary-card">
          <AlertTriangle size={21} />
          <span>Open Findings</span>
          <strong>{loading ? '—' : openCount}</strong>
        </article>

        <article className="findings-summary-card">
          <ShieldAlert size={21} />
          <span>Critical / High</span>
          <strong>{loading ? '—' : highSeverityCount}</strong>
        </article>

        <article className="findings-summary-card">
          <CheckCircle2 size={21} />
          <span>Resolved</span>
          <strong>{loading ? '—' : resolvedCount}</strong>
        </article>
      </section>

      <section className="findings-list-card">
        <div className="findings-list-heading">
          <div>
            <h2>Detected Issues</h2>
            <p>{filteredFindings.length} matching findings</p>
          </div>
        </div>

        <div className="findings-filters">
          <label className="findings-search">
            <Search size={17} />
            <input
              type="search"
              placeholder="Search rule, message or file..."
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
          </label>

          <select
            aria-label="Filter by severity"
            value={severityFilter}
            onChange={(event) => setSeverityFilter(event.target.value)}
          >
            <option value="ALL">All Severities</option>
            <option value="CRITICAL">Critical</option>
            <option value="HIGH">High</option>
            <option value="MEDIUM">Medium</option>
            <option value="LOW">Low</option>
          </select>

          <select
            aria-label="Filter by status"
            value={statusFilter}
            onChange={(event) => setStatusFilter(event.target.value)}
          >
            <option value="ALL">All Statuses</option>
            <option value="OPEN">Open</option>
            <option value="IN_PROGRESS">In Progress</option>
            <option value="RESOLVED">Resolved</option>
          </select>
        </div>

        <div className="findings-table-wrapper">
          <table className="findings-data-table">
            <thead>
              <tr>
                <th>RULE / ISSUE</th>
                <th>FILE</th>
                <th>LINE</th>
                <th>SEVERITY</th>
                <th>STATUS</th>
              </tr>
            </thead>

            <tbody>
              {loading ? (
                <tr>
                  <td colSpan={5} className="findings-empty">
                    Loading findings from Spring Boot...
                  </td>
                </tr>
              ) : filteredFindings.length === 0 ? (
                <tr>
                  <td colSpan={5} className="findings-empty">
                    {findings.length === 0
                      ? 'No findings found for this project.'
                      : 'No findings match the selected filters.'}
                  </td>
                </tr>
              ) : (
                filteredFindings.map((finding, index) => {
                  const severity = (finding.severity ?? 'UNKNOWN').toLowerCase()
                  const status = (finding.status ?? 'UNKNOWN').toLowerCase()

                  return (
                    <tr
                      key={finding.id ?? `${finding.rule}-${index}`}
                    >
                      <td>
                        <div className="findings-issue-cell">
                          <strong>{finding.rule || 'Code Finding'}</strong>
                          <span>
                            {finding.message || 'No description available.'}
                          </span>
                        </div>
                      </td>

                      <td>
                        <code>{finding.filePath || 'Unknown file'}</code>
                      </td>

                      <td>{finding.lineNumber ?? '—'}</td>

                      <td>
                        <span className={`findings-badge severity-${severity}`}>
                          {finding.severity || 'Unknown'}
                        </span>
                      </td>

                      <td>
                        <span className={`findings-badge status-${status}`}>
                          {finding.status || 'Unknown'}
                        </span>
                      </td>
                    </tr>
                  )
                })
              )}
            </tbody>
          </table>
        </div>
      </section>

      <section className="findings-ai-section">
        <FindingsPanel projectId={projectId} />
      </section>
    </div>
  )
}