const API_BASE = '/api'

export type AiAnalysis = {
  id?: string
  projectId: string
  findingId: string
  rootCause: string
  explanation: string
  impact: string
  recommendedFix: string
  saferApproach: string
  confidence: 'HIGH' | 'MEDIUM' | 'LOW' | string
}

export async function analyzeFindingWithAI(
  projectId: string,
  findingId: string,
): Promise<AiAnalysis> {
  const response = await fetch(
    `${API_BASE}/ai/projects/${projectId}/findings/${findingId}`,
    {
      method: 'POST',
    },
  )

  if (!response.ok) {
    const message = await response.text()

    throw new Error(
      message || `AI analysis failed: HTTP ${response.status}`,
    )
  }

  return response.json() as Promise<AiAnalysis>
}