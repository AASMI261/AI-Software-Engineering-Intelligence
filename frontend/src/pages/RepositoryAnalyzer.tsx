
import { useEffect, useState } from 'react'
import {
  GitBranch,
  Search,
  CheckCircle2,
  AlertCircle,
  Loader2,
  FileCode2,
  ShieldAlert,
  RefreshCw,
} from 'lucide-react'
import './RepositoryAnalyzer.css'

const PROJECT_ID = '6ad36dfc5f4d4046483cb64a'

interface RepositoryResult {
  valid: boolean
  repositoryUrl?: string
  message: string
}

interface RepositoryAnalysis {
  id: string
  projectId: string
  repositoryUrl: string
  repositoryName: string
  status: string
  message: string
  filesAnalyzed: number
  findingsCount: number
  createdAt?: string
  completedAt?: string
}

interface Finding {
  id: string
  type: string
  severity: string
  filePath: string
  lineNumber: number | null
  message: string
  rule: string
  suggestedFix: string
  status: string
}

export default function RepositoryAnalyzer() {
  const [repositoryUrl, setRepositoryUrl] = useState('')
  const [result, setResult] = useState<RepositoryResult | null>(null)
  const [loading, setLoading] = useState(false)
  const [analysis, setAnalysis] = useState<RepositoryAnalysis | null>(null)
  const [findings, setFindings] = useState<Finding[]>([])
  const [history, setHistory] = useState<RepositoryAnalysis[]>([])
  const [error, setError] = useState('')
  const [historyLoading, setHistoryLoading] = useState(false)

  async function readResponse(response: Response) {
    const data = await response.json().catch(() => ({}))

    if (!response.ok) {
      throw new Error(
        data.message || `Request failed with status ${response.status}`,
      )
    }

    return data
  }

  async function loadHistory() {
    setHistoryLoading(true)

    try {
      const response = await fetch(
        `/api/repository-analyses/project/${PROJECT_ID}`,
      )
      const data = await readResponse(response)

      setHistory(Array.isArray(data) ? data : [])
    } catch (err) {
      console.error('Could not load repository analysis history:', err)
    } finally {
      setHistoryLoading(false)
    }
  }

  async function loadFindings(
    projectId: string,
    analysisId: string,
  ) {
    const response = await fetch(
      `/api/projects/${encodeURIComponent(projectId)}/analyses/${encodeURIComponent(analysisId)}/findings`,
    )

    const data = await readResponse(response)
    setFindings(Array.isArray(data) ? data : [])
  }

  async function validateRepository(): Promise<boolean> {
    setResult(null)
    setError('')

    if (!repositoryUrl.trim()) {
      setResult({
        valid: false,
        message: 'Please enter a GitHub repository URL.',
      })
      return false
    }

    try {
      const response = await fetch('/api/repositories/validate', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          repositoryUrl: repositoryUrl.trim(),
        }),
      })

      const data = await readResponse(response)

      const valid = data.valid === true

      setResult({
        valid,
        repositoryUrl: data.repositoryUrl,
        message: data.message || 'Unable to validate repository.',
      })

      return valid
    } catch (err) {
      setResult({
        valid: false,
        message:
          err instanceof Error
            ? err.message
            : 'Could not connect to the backend.',
      })
      return false
    }
  }

  async function analyzeRepository() {
    setLoading(true)
    setError('')
    setAnalysis(null)
    setFindings([])

    try {
      const valid = await validateRepository()

      if (!valid) {
        return
      }

      const response = await fetch('/api/repository-analyses', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          projectId: PROJECT_ID,
          repositoryUrl: repositoryUrl.trim(),
        }),
      })

      const data = (await readResponse(response)) as RepositoryAnalysis

      setAnalysis(data)

      if (data.id && data.status === 'COMPLETED') {
        await loadFindings(data.projectId || PROJECT_ID, data.id)
      }

      await loadHistory()
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Repository analysis failed.',
      )
    } finally {
      setLoading(false)
    }
  }

  async function openAnalysis(item: RepositoryAnalysis) {
    setError('')
    setAnalysis(item)
    setFindings([])

    try {
      const response = await fetch(
        `/api/repository-analyses/${encodeURIComponent(item.id)}`,
      )
      const data = (await readResponse(response)) as RepositoryAnalysis

      setAnalysis(data)

      if (data.status === 'COMPLETED') {
        await loadFindings(data.projectId || PROJECT_ID, data.id)
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Could not load this analysis.',
      )
    }
  }

  useEffect(() => {
    void loadHistory()
  }, [])

  const highCount = findings.filter(
    (finding) => finding.severity?.toUpperCase() === 'HIGH',
  ).length

  const mediumCount = findings.filter(
    (finding) => finding.severity?.toUpperCase() === 'MEDIUM',
  ).length

  const lowCount = findings.filter(
    (finding) => finding.severity?.toUpperCase() === 'LOW',
  ).length

  return (
    <div className="repository-analyzer">
      <div className="repository-heading">
        <div className="repository-heading-icon">
          <GitBranch size={25} />
        </div>

        <div>
          <h1>Repository Analyzer</h1>
          <p>
            Connect a public GitHub repository to scan source code for
            potential issues.
          </p>
        </div>
      </div>

      <section className="repository-card">
        <div className="repository-card-title">
          <GitBranch size={21} />
          <h2>Connect Repository</h2>
        </div>

        <p className="repository-description">
          Enter the HTTPS URL of a public GitHub repository.
        </p>

        <label htmlFor="repository-url">GitHub Repository URL</label>

        <input
          id="repository-url"
          type="url"
          placeholder="https://github.com/owner/repository"
          value={repositoryUrl}
          disabled={loading}
          onChange={(event) => {
            setRepositoryUrl(event.target.value)
            setResult(null)
            setError('')
          }}
          onKeyDown={(event) => {
            if (event.key === 'Enter' && !loading) {
              void analyzeRepository()
            }
          }}
        />

        <div className="repository-action-row">
          <button
            className="repository-validate-button"
            onClick={() => void validateRepository()}
            disabled={loading}
          >
            <Search size={18} />
            Validate URL
          </button>

          <button
            className="repository-validate-button"
            onClick={() => void analyzeRepository()}
            disabled={loading}
          >
            {loading ? (
              <>
                <Loader2 size={18} className="repository-spinner" />
                Analyzing...
              </>
            ) : (
              <>
                <FileCode2 size={18} />
                Analyze Repository
              </>
            )}
          </button>
        </div>

        {result && (
          <div
            className={`repository-result ${
              result.valid
                ? 'repository-result-success'
                : 'repository-result-error'
            }`}
            role="status"
          >
            {result.valid ? (
              <CheckCircle2 size={20} />
            ) : (
              <AlertCircle size={20} />
            )}

            <div>
              <strong>
                {result.valid ? 'Validation Successful' : 'Validation Failed'}
              </strong>
              <p>{result.message}</p>

              {result.valid && result.repositoryUrl && (
                <a
                  href={result.repositoryUrl}
                  target="_blank"
                  rel="noreferrer"
                >
                  Open GitHub Repository
                </a>
              )}
            </div>
          </div>
        )}

        {error && (
          <div className="repository-result repository-result-error" role="alert">
            <AlertCircle size={20} />
            <div>
              <strong>Request Failed</strong>
              <p>{error}</p>
            </div>
          </div>
        )}
      </section>

      {analysis && (
        <section className="repository-info-card">
          <div className="repository-card-title">
            <FileCode2 size={21} />
            <h2>Analysis Report</h2>
          </div>

          <p>
            <strong>Repository:</strong> {analysis.repositoryName}
          </p>
          <p>
            <strong>Status:</strong>{' '}
            <span className={`analysis-status status-${analysis.status?.toLowerCase()}`}>
              {analysis.status}
            </span>
          </p>
          <p>{analysis.message}</p>

          <div className="repository-stats">
            <div className="repository-stat">
              <FileCode2 size={20} />
              <span>Files Analyzed</span>
              <strong>{analysis.filesAnalyzed ?? 0}</strong>
            </div>

            <div className="repository-stat">
              <ShieldAlert size={20} />
              <span>Total Findings</span>
              <strong>{analysis.findingsCount ?? findings.length}</strong>
            </div>

            <div className="repository-stat">
              <AlertCircle size={20} />
              <span>High Severity</span>
              <strong>{highCount}</strong>
            </div>
          </div>

          {analysis.status === 'COMPLETED' && (
            <>
              <h3>Severity Summary</h3>
              <p>High: {highCount} · Medium: {mediumCount} · Low: {lowCount}</p>

              <h3>Detected Findings</h3>

              {findings.length === 0 ? (
                <p>
                  No findings were returned for this analysis. This does not
                  guarantee that the repository is free of issues.
                </p>
              ) : (
                <div className="repository-findings-list">
                  {findings.map((finding) => (
                    <article
                      className="repository-finding"
                      key={finding.id}
                    >
                      <div className="repository-finding-heading">
                        <strong>{finding.rule || finding.type || 'Finding'}</strong>
                        <span
                          className={`finding-severity severity-${finding.severity?.toLowerCase()}`}
                        >
                          {finding.severity || 'UNKNOWN'}
                        </span>
                      </div>

                      <p>{finding.message}</p>

                      <p className="repository-finding-location">
                        {finding.filePath || 'Unknown file'}
                        {finding.lineNumber != null
                          ? ` : line ${finding.lineNumber}`
                          : ''}
                      </p>

                      {finding.suggestedFix && (
                        <div className="repository-suggested-fix">
                          <strong>Suggested action</strong>
                          <p>{finding.suggestedFix}</p>
                        </div>
                      )}
                    </article>
                  ))}
                </div>
              )}
            </>
          )}
        </section>
      )}

      <section className="repository-info-card">
        <div className="repository-history-heading">
          <h3>Recent Analyses</h3>
          <button
            className="repository-refresh-button"
            onClick={() => void loadHistory()}
            disabled={historyLoading}
            aria-label="Refresh analysis history"
          >
            {historyLoading ? (
              <Loader2 size={17} className="repository-spinner" />
            ) : (
              <RefreshCw size={17} />
            )}
            Refresh
          </button>
        </div>

        {history.length === 0 ? (
          <p>No repository analyses found for this project yet.</p>
        ) : (
          <div className="repository-history-list">
            {history.map((item) => (
              <button
                className="repository-history-item"
                key={item.id}
                onClick={() => void openAnalysis(item)}
              >
                <span>
                  <strong>{item.repositoryName}</strong>
                  <small>{item.repositoryUrl}</small>
                </span>
                <span className={`analysis-status status-${item.status?.toLowerCase()}`}>
                  {item.status}
                </span>
              </button>
            ))}
          </div>
        )}
      </section>

      <section className="repository-info-card">
        <h3>Analysis Workflow</h3>

        <div className="repository-next-step">
          <span>01</span>
          <div>
            <strong>Validate Repository</strong>
            <p>Check the GitHub repository URL format.</p>
          </div>
        </div>

        <div className="repository-next-step">
          <span>02</span>
          <div>
            <strong>Retrieve Source Code</strong>
            <p>Fetch supported files from a public GitHub repository.</p>
          </div>
        </div>

        <div className="repository-next-step">
          <span>03</span>
          <div>
            <strong>Analyze Code</strong>
            <p>Run basic rule-based checks and review potential findings.</p>
          </div>
        </div>
      </section>
    </div>
  )
}
