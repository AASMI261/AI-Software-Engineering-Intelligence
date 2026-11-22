import { useEffect, useState } from 'react'
import {
  AlertTriangle,
  CheckCircle2,
  ClipboardCheck,
  FileCode2,
  Play,
  RefreshCw,
  ShieldAlert,
  XCircle,
} from 'lucide-react'
import {
  applyFixPlan,
  approveFixPlan,
  getProjectFixPlans,
  rejectFixPlan,
  type FixPlan,
} from '../api'
import './FixReviewPage.css'

type FixReviewPageProps = {
  projectId: string
}

export default function FixReviewPage({ projectId }: FixReviewPageProps) {
  const [plans, setPlans] = useState<FixPlan[]>([])
  const [selectedPlanId, setSelectedPlanId] = useState('')
  const [loading, setLoading] = useState(false)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState('')
  const [notice, setNotice] = useState('')

  async function loadPlans() {
    setLoading(true)
    setError('')
    setNotice('')

    try {
      const list = await getProjectFixPlans(projectId)
      setPlans(list)

      if (list.length > 0) {
        const firstId = String(list[0].id ?? list[0]._id ?? '')
        setSelectedPlanId((current) =>
          list.some((plan) => String(plan.id ?? plan._id ?? '') === current)
            ? current
            : firstId,
        )
      } else {
        setSelectedPlanId('')
        setNotice('Abhi is project ke liye koi Fix Plan nahi mila.')
      }
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Fix Plans load nahi ho paye.',
      )
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    void loadPlans()
  }, [projectId])

  const selectedPlan = plans.find(
    (plan) => String(plan.id ?? plan._id ?? '') === selectedPlanId,
  )

  const currentStatus = (selectedPlan?.status ?? 'PENDING').toUpperCase()

  async function handleApprove() {
    if (!selectedPlan) return
    const planId = String(selectedPlan.id ?? selectedPlan._id ?? '')
    if (!planId) {
      setError('Selected Fix Plan ki ID nahi mili.')
      return
    }

    setActionLoading(true)
    setError('')
    setNotice('')

    try {
      const updated = await approveFixPlan(planId)
      setPlans((current) =>
        current.map((plan) =>
          String(plan.id ?? plan._id) === planId
            ? { ...plan, ...updated, status: updated.status ?? 'APPROVED' }
            : plan,
        ),
      )
      setNotice('Fix Plan approved. Ab aap Apply Fix se patch apply kar sakti ho.')
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Fix Plan approve nahi ho paya.',
      )
    } finally {
      setActionLoading(false)
    }
  }

  async function handleReject() {
    if (!selectedPlan) return
    const planId = String(selectedPlan.id ?? selectedPlan._id ?? '')
    if (!planId) {
      setError('Selected Fix Plan ki ID nahi mili.')
      return
    }

    setActionLoading(true)
    setError('')
    setNotice('')

    try {
      const updated = await rejectFixPlan(planId)
      setPlans((current) =>
        current.map((plan) =>
          String(plan.id ?? plan._id) === planId
            ? { ...plan, ...updated, status: updated.status ?? 'REJECTED' }
            : plan,
        ),
      )
      setNotice('Fix Plan rejected. Source code modify nahi hua.')
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Fix Plan reject nahi ho paya.',
      )
    } finally {
      setActionLoading(false)
    }
  }

  async function handleApplyFix() {
    if (!selectedPlan) return
    const planId = String(selectedPlan.id ?? selectedPlan._id ?? '')
    if (!planId) {
      setError('Selected Fix Plan ki ID nahi mili.')
      return
    }

    setActionLoading(true)
    setError('')
    setNotice('')

    try {
      const updated = await applyFixPlan(planId)
      setPlans((current) =>
        current.map((plan) =>
          String(plan.id ?? plan._id) === planId ? { ...plan, ...updated } : plan,
        ),
      )
      await loadPlans()
      setNotice(
        `Fix Agent run complete. Latest plan status: ${updated.status ?? 'UPDATED'}.`,
      )
    } catch (err) {
      setError(
        err instanceof Error ? err.message : 'Fix apply nahi ho paya.',
      )
    } finally {
      setActionLoading(false)
    }
  }

  return (
    <div className="fix-review-page">
      <section className="fr-heading">
        <div>
          <span className="fr-eyebrow">
            <ClipboardCheck size={15} />
            HUMAN-IN-THE-LOOP REVIEW
          </span>
          <h1>Fix Review</h1>
          <p>
            AI-generated plans ko review karo. Pehle approve karo, uske baad
            controlled Fix Agent apply aur verify karega.
          </p>
        </div>

        <button
          className="fr-refresh"
          onClick={() => void loadPlans()}
          disabled={loading || actionLoading}
        >
          <RefreshCw size={16} />
          {loading ? 'Loading...' : 'Refresh Plans'}
        </button>
      </section>

      <section className="fr-summary">
        <article>
          <span>Total Plans</span>
          <strong>{plans.length}</strong>
        </article>
        <article>
          <span>Pending Review</span>
          <strong>
            {
              plans.filter(
                (plan) => (plan.status ?? 'PENDING').toUpperCase() === 'PENDING',
              ).length
            }
          </strong>
        </article>
        <article>
          <span>Approved</span>
          <strong>
            {
              plans.filter(
                (plan) => plan.status?.toUpperCase() === 'APPROVED',
              ).length
            }
          </strong>
        </article>
        <article>
          <span>Rejected / Rolled Back</span>
          <strong>
            {
              plans.filter((plan) => {
                const st = (plan.status ?? '').toUpperCase()
                return st === 'REJECTED' || st === 'ROLLED_BACK'
              }).length
            }
          </strong>
        </article>
      </section>

      {error && (
        <div className="fr-message fr-error">
          <AlertTriangle size={18} />
          <span>{error}</span>
        </div>
      )}

      {notice && (
        <div className="fr-message fr-notice">
          <CheckCircle2 size={18} />
          <span>{notice}</span>
        </div>
      )}

      <section className="fr-layout">
        <aside className="fr-plan-list">
          <div className="fr-section-title">
            <div>
              <h2>Generated Plans</h2>
              <p>Select a plan to review</p>
            </div>
          </div>

          {plans.length === 0 ? (
            <div className="fr-empty">
              <FileCode2 size={28} />
              <strong>No plans available</strong>
              <p>AI Fix Plans page se pehle ek plan generate karo.</p>
            </div>
          ) : (
            plans.map((plan, index) => {
              const id = String(plan.id ?? plan._id ?? index)
              const isSelected = id === selectedPlanId

              return (
                <button
                  key={id}
                  className={`fr-plan-item ${isSelected ? 'selected' : ''}`}
                  onClick={() => setSelectedPlanId(id)}
                >
                  <span className="fr-plan-icon">
                    <FileCode2 size={18} />
                  </span>
                  <span className="fr-plan-copy">
                    <strong>{plan.problem || 'Fix Plan'}</strong>
                    <small>{plan.filePath || 'Source file not specified'}</small>
                    <span
                      className={`fr-status fr-status-${(
                        plan.status ?? 'PENDING'
                      ).toLowerCase()}`}
                    >
                      {plan.status ?? 'PENDING'}
                    </span>
                  </span>
                </button>
              )
            })
          )}
        </aside>

        <div className="fr-detail">
          {!selectedPlan ? (
            <div className="fr-empty fr-detail-empty">
              <ClipboardCheck size={34} />
              <h2>Choose a Fix Plan</h2>
              <p>Review karne ke liye left side se ek plan select karo.</p>
            </div>
          ) : (
            <>
              <div className="fr-detail-heading">
                <div>
                  <span className="fr-eyebrow">PLAN DETAILS</span>
                  <h2>{selectedPlan.problem || 'Fix Plan Review'}</h2>
                  <p>
                    {selectedPlan.filePath || 'File path unavailable'}
                    {selectedPlan.lineNumber
                      ? ` · Line ${selectedPlan.lineNumber}`
                      : ''}
                  </p>
                </div>
                <span
                  className={`fr-status fr-status-${currentStatus.toLowerCase()}`}
                >
                  {currentStatus}
                </span>
              </div>

              <div className="fr-risk-row">
                <div>
                  <ShieldAlert size={18} />
                  <span>Risk</span>
                  <strong>{selectedPlan.risk || 'Not specified'}</strong>
                </div>
                <div>
                  <CheckCircle2 size={18} />
                  <span>AI Confidence</span>
                  <strong>{selectedPlan.confidence || 'Not specified'}</strong>
                </div>
              </div>

              <div className="fr-information">
                <h3>Change Description</h3>
                <p>{selectedPlan.changeDescription || 'Not provided.'}</p>
              </div>

              <div className="fr-information">
                <h3>Proposed Changes</h3>
                <p>{selectedPlan.proposedChanges || 'Not provided.'}</p>
              </div>

              <div className="fr-information">
                <h3>Tests & Verification Steps</h3>
                <p>
                  {selectedPlan.testsToRun
                    ? `Tests: ${selectedPlan.testsToRun}`
                    : 'No specific tests listed.'}
                </p>
                <p>{selectedPlan.verificationSteps || ''}</p>
              </div>

              <div className="fr-actions">
                <button
                  type="button"
                  disabled={currentStatus !== 'PENDING' || actionLoading}
                  onClick={() => void handleApprove()}
                >
                  <CheckCircle2 size={16} />
                  Approve Fix
                </button>

                <button
                  type="button"
                  disabled={currentStatus !== 'PENDING' || actionLoading}
                  onClick={() => void handleReject()}
                >
                  <XCircle size={16} />
                  Reject Fix
                </button>

                <button
                  type="button"
                  disabled={currentStatus !== 'APPROVED' || actionLoading}
                  onClick={() => void handleApplyFix()}
                >
                  <Play size={16} />
                  {actionLoading ? 'Applying...' : 'Apply Fix'}
                </button>
              </div>
            </>
          )}
        </div>
      </section>
    </div>
  )
}