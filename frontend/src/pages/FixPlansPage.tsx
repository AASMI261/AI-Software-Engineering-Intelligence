import { useEffect, useState } from 'react'
import {
  AlertTriangle,
  ArrowLeft,
  BrainCircuit,
  CheckCircle2,
  ClipboardCheck,
  FileCode2,
  LoaderCircle,
  ShieldAlert,
  Sparkles,
  Wrench,
  XCircle,
} from 'lucide-react'
import {
  generateFixPlan as generateFixPlanApi,
  getProjectFindings,
  type Finding,
  type FixPlan,
} from '../api'
import './FixPlansPage.css'

type FixPlansPageProps = {
  projectId: string
}

function getId(item: Finding) {
  return item.id ?? ''
}

export default function FixPlansPage({ projectId }: FixPlansPageProps) {
  const [findings, setFindings] = useState<Finding[]>([])
  const [selectedFindingId, setSelectedFindingId] = useState('')
  const [fixPlan, setFixPlan] = useState<FixPlan | null>(null)
  const [loadingFindings, setLoadingFindings] = useState(false)
  const [generating, setGenerating] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  async function loadFindings() {
    if (!projectId) {
      setFindings([])
      setSelectedFindingId('')
      return
    }

    setLoadingFindings(true)
    setError('')
    setSuccess('')
    setFixPlan(null)

    try {
      const list = await getProjectFindings(projectId)
      setFindings(list)
      setSelectedFindingId((current) =>
        list.some((item) => getId(item) === current) ? current : '',
      )

      if (list.length === 0) {
        setError('Is project ke liye koi finding nahi mili.')
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Findings load nahi ho paayi.',
      )
    } finally {
      setLoadingFindings(false)
    }
  }

  useEffect(() => {
    void loadFindings()
  }, [projectId])

  async function handleGenerateFixPlan() {
    if (!selectedFindingId) {
      setError('Pehle ek finding select karo.')
      return
    }

    setGenerating(true)
    setError('')
    setSuccess('')
    setFixPlan(null)

    try {
      const data = await generateFixPlanApi(projectId, selectedFindingId)
      setFixPlan(data)
      setSuccess('AI Fix Plan successfully generate ho gaya.')
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'AI Fix Plan generate nahi ho paaya.',
      )
    } finally {
      setGenerating(false)
    }
  }

  const selectedFinding = findings.find(
    (finding) => getId(finding) === selectedFindingId,
  )

  return (
    <div className="fixplans-page">
      <header className="fixplans-header">
        <div>
          <div className="fixplans-eyebrow">
            <Sparkles size={15} />
            AI-POWERED RECOVERY
          </div>

          <h1>AI Fix Plans</h1>

          <p>
            Generate a structured, reviewable plan to resolve detected code
            issues.
          </p>
        </div>

        <div className="fixplans-project-badge">
          <span />
          Project connected
        </div>
      </header>

      <section className="fixplans-intro">
        <div className="fixplans-intro-icon">
          <BrainCircuit size={25} />
        </div>

        <div>
          <h2>From issue to recovery plan</h2>
          <p>
            Select a finding, ask the AI to prepare a fix plan, and review the
            proposed changes before modifying any source code.
          </p>
        </div>
      </section>

      <section className="fixplans-workspace">
        <div className="fixplans-panel">
          <div className="fixplans-panel-heading">
            <div className="fixplans-heading-icon">
              <FileCode2 size={19} />
            </div>

            <div>
              <h2>Select a finding</h2>
              <p>Choose an issue from your project analysis.</p>
            </div>
          </div>

          <div className="fixplans-form">
            <label htmlFor="finding-select">Available findings</label>

            <select
              id="finding-select"
              value={selectedFindingId}
              onChange={(event) => {
                setSelectedFindingId(event.target.value)
                setFixPlan(null)
                setError('')
                setSuccess('')
              }}
              disabled={loadingFindings || generating}
            >
              <option value="">Select a finding...</option>

              {findings.map((finding, index) => {
                const id = getId(finding)

                return (
                  <option key={id || index} value={id} disabled={!id}>
                    {finding.rule || finding.type || `Finding ${index + 1}`}
                    {' — '}
                    {finding.message || 'No description'}
                  </option>
                )
              })}
            </select>

            <div className="fixplans-actions">
              <button
                type="button"
                className="fixplans-secondary-button"
                onClick={() => void loadFindings()}
                disabled={loadingFindings || generating}
              >
                {loadingFindings ? (
                  <LoaderCircle className="fixplans-spinner" size={16} />
                ) : (
                  <ArrowLeft size={16} />
                )}

                {loadingFindings ? 'Loading...' : 'Load Findings'}
              </button>

              <button
                type="button"
                className="fixplans-primary-button"
                onClick={() => void handleGenerateFixPlan()}
                disabled={!selectedFindingId || generating}
              >
                {generating ? (
                  <LoaderCircle className="fixplans-spinner" size={17} />
                ) : (
                  <Sparkles size={17} />
                )}

                {generating ? 'Generating...' : 'Generate Fix Plan'}
              </button>
            </div>
          </div>

          {selectedFinding && (
            <div className="fixplans-selected-finding">
              <div className="fixplans-selected-heading">
                <AlertTriangle size={17} />
                Selected issue
              </div>

              <h3>
                {selectedFinding.rule ||
                  selectedFinding.type ||
                  'Code finding'}
              </h3>

              <p>
                {selectedFinding.message || 'No issue description available.'}
              </p>

              <div className="fixplans-finding-meta">
                <span className="fixplans-tag">
                  {selectedFinding.severity || 'Unknown severity'}
                </span>

                <span className="fixplans-tag">
                  {selectedFinding.status || 'Unknown status'}
                </span>
              </div>

              <code className="fixplans-file-path">
                {selectedFinding.filePath || 'File path unavailable'}
                {selectedFinding.lineNumber != null
                  ? `:${selectedFinding.lineNumber}`
                  : ''}
              </code>

              {selectedFinding.suggestedFix && (
                <div className="fixplans-existing-suggestion">
                  <strong>Existing suggestion</strong>
                  <p>{selectedFinding.suggestedFix}</p>
                </div>
              )}
            </div>
          )}
        </div>

        <div className="fixplans-panel fixplans-status-panel">
          <div className="fixplans-panel-heading">
            <div className="fixplans-heading-icon purple">
              <ShieldAlert size={19} />
            </div>

            <div>
              <h2>Plan generation</h2>
              <p>AI-assisted analysis workflow.</p>
            </div>
          </div>

          <div className="fixplans-workflow">
            <div className="fixplans-workflow-step complete">
              <span><CheckCircle2 size={17} /></span>
              <div>
                <strong>Static analysis</strong>
                <small>Identify a code issue.</small>
              </div>
            </div>

            <div className="fixplans-workflow-step">
              <span><BrainCircuit size={17} /></span>
              <div>
                <strong>AI fix planning</strong>
                <small>Generate a structured proposal.</small>
              </div>
            </div>

            <div className="fixplans-workflow-step">
              <span><ClipboardCheck size={17} /></span>
              <div>
                <strong>Human review</strong>
                <small>Review before applying changes.</small>
              </div>
            </div>
          </div>

          <div className="fixplans-safety-note">
            <ShieldAlert size={17} />
            <p>
              The generated plan is a proposal. It does not automatically
              modify your source files.
            </p>
          </div>
        </div>
      </section>

      {error && (
        <div className="fixplans-alert error" role="alert">
          <XCircle size={19} />
          <span>{error}</span>
        </div>
      )}

      {success && (
        <div className="fixplans-alert success" role="status">
          <CheckCircle2 size={19} />
          <span>{success}</span>
        </div>
      )}

      {fixPlan && (
        <section className="fixplans-results">
          <div className="fixplans-results-header">
            <div>
              <div className="fixplans-eyebrow">
                <Sparkles size={15} />
                GENERATED RESULT
              </div>

              <h2>Recommended Fix Plan</h2>

              <p>
                Review the proposed solution before proceeding to implementation.
              </p>
            </div>

            <span
              className={`fixplans-risk-badge ${(fixPlan.risk ?? 'unknown').toLowerCase()}`}
            >
              Risk: {fixPlan.risk ?? 'Not specified'}
            </span>
          </div>

          <div className="fixplans-result-grid">
            <article className="fixplans-result-card full">
              <div className="fixplans-result-title">
                <AlertTriangle size={18} />
                <h3>Problem</h3>
              </div>
              <p>{fixPlan.problem || 'Not provided by the backend.'}</p>
            </article>

            <article className="fixplans-result-card full">
              <div className="fixplans-result-title">
                <Wrench size={18} />
                <h3>Change Description</h3>
              </div>
              <p>
                {fixPlan.changeDescription || 'Not provided by the backend.'}
              </p>
            </article>

            <article className="fixplans-result-card full">
              <div className="fixplans-result-title">
                <FileCode2 size={18} />
                <h3>Proposed Changes</h3>
              </div>
              <pre>
                {fixPlan.proposedChanges || 'Not provided by the backend.'}
              </pre>
            </article>

            <article className="fixplans-result-card">
              <div className="fixplans-result-title">
                <ClipboardCheck size={18} />
                <h3>Tests to Run</h3>
              </div>
              <p>{fixPlan.testsToRun || 'Not provided by the backend.'}</p>
            </article>

            <article className="fixplans-result-card">
              <div className="fixplans-result-title">
                <CheckCircle2 size={18} />
                <h3>Verification Steps</h3>
              </div>
              <p>
                {fixPlan.verificationSteps || 'Not provided by the backend.'}
              </p>
            </article>
          </div>

          <div className="fixplans-result-footer">
            <span>
              Confidence: <strong>{fixPlan.confidence ?? 'Not specified'}</strong>
            </span>

            <span>
              Status: <strong>{fixPlan.status ?? 'Pending review'}</strong>
            </span>
          </div>
        </section>
      )}
    </div>
  )
}