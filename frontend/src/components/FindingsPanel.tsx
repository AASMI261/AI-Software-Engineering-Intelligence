import { useEffect, useState } from 'react'
import { getProjectFindings } from '../services/findingsApi'
import type { Finding } from '../services/findingsApi'
import { analyzeFindingWithAI } from '../services/aiAnalysisApi.ts'
import type { AiAnalysis } from '../services/aiAnalysisApi.ts'
import './FindingsPanel.css'

type Props = {
  projectId: string
}

export default function FindingsPanel({ projectId }: Props) {
  const [findings, setFindings] = useState<Finding[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [selectedFinding, setSelectedFinding] =
    useState<Finding | null>(null)
  const [aiResult, setAiResult] = useState<AiAnalysis | null>(null)
  const [aiLoading, setAiLoading] = useState(false)
  const [aiError, setAiError] = useState('')

  useEffect(() => {
    let cancelled = false

    async function loadFindings() {
      setLoading(true)
      setError('')
      setFindings([])
      setSelectedFinding(null)
      setAiResult(null)

      try {
        const result = await getProjectFindings(projectId)

        if (!cancelled) {
          setFindings(result)
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error
              ? err.message
              : 'Unable to load findings.',
          )
        }
      } finally {
        if (!cancelled) {
          setLoading(false)
        }
      }
    }

    if (projectId) {
      void loadFindings()
    }

    return () => {
      cancelled = true
    }
  }, [projectId])

  async function handleAiAnalysis(finding: Finding) {
    setSelectedFinding(finding)
    setAiResult(null)
    setAiError('')
    setAiLoading(true)

    try {
      const result = await analyzeFindingWithAI(
        finding.projectId,
        finding.id,
      )

      setAiResult(result)
    } catch (err) {
      setAiError(
        err instanceof Error
          ? err.message
          : 'AI analysis failed.',
      )
    } finally {
      setAiLoading(false)
    }
  }

  return (
    <section className="findings-panel">
      <div className="findings-header">
        <div>
          <h2>Static Analysis Findings</h2>
          <p>Code issues detected in the selected project.</p>
        </div>

        <span className="findings-count">
          {findings.length} findings
        </span>
      </div>

      {loading && <p className="findings-message">Loading findings...</p>}

      {error && (
        <p className="findings-error" role="alert">
          {error}
        </p>
      )}

      {!loading && !error && findings.length === 0 && (
        <div className="findings-empty">
          <h3>No findings found</h3>
          <p>
            Upload a project ZIP and run static analysis to detect issues.
          </p>
        </div>
      )}

      {findings.length > 0 && (
        <div className="findings-table-wrapper">
          <table className="findings-table">
            <thead>
              <tr>
                <th>Issue</th>
                <th>Severity</th>
                <th>File</th>
                <th>Line</th>
                <th>Status</th>
                <th>Action</th>
              </tr>
            </thead>

            <tbody>
              {findings.map((finding) => (
                <tr key={finding.id} >
                  <td>
                    <strong>{finding.rule || finding.type}</strong>
                    <p>{finding.message}</p>
                  </td>

                  <td>
                    <span
                      className={`severity-badge ${finding.severity.toLowerCase()}`}
                    >
                      {finding.severity}
                    </span>
                  </td>

                  <td>{finding.filePath}</td>
                  <td>{finding.lineNumber ?? '—'}</td>
                  <td>{finding.status}</td>

                  <td>
                    <button
                      type="button"
                      className="ai-analysis-button"
                      onClick={() => void handleAiAnalysis(finding)}
                    >
                      Analyze with AI
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {selectedFinding && (
        <section className="ai-analysis-panel">
          <div className="ai-analysis-heading">
            <div>
              <h3>AI Root Cause Analysis</h3>
              <p>{selectedFinding.filePath}</p>
            </div>

            <button
              type="button"
              onClick={() => {
                setSelectedFinding(null)
                setAiResult(null)
                setAiError('')
              }}
            >
              Close
            </button>
          </div>

          {aiLoading && (
            <p className="findings-message">
              AI is analyzing the selected finding...
            </p>
          )}

          {aiError && (
            <p className="findings-error" role="alert">
              {aiError}
            </p>
          )}

          {aiResult && (
            <div className="ai-analysis-content">
              <div>
                <h4>Root Cause</h4>
                <p>{aiResult.rootCause}</p>
              </div>

              <div>
                <h4>Technical Explanation</h4>
                <p>{aiResult.explanation}</p>
              </div>

              <div>
                <h4>Impact</h4>
                <p>{aiResult.impact}</p>
              </div>

              <div>
                <h4>Recommended Fix</h4>
                <p>{aiResult.recommendedFix}</p>
              </div>

              <div>
                <h4>Safer Approach</h4>
                <p>{aiResult.saferApproach}</p>
              </div>

              <div>
                <h4>AI Confidence</h4>
                <span className="confidence-badge">
                  {aiResult.confidence}
                </span>
              </div>
            </div>
          )}
        </section>
      )}
    </section>
  )
}