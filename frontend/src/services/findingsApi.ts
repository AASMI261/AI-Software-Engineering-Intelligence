const API_BASE = '/api'

export type Finding = {
  id: string
  projectId: string
  analysisId: string
  type: string
  severity: string
  filePath: string
  lineNumber: number | null
  message: string
  rule: string
  suggestedFix: string
  status: string
  detectedAt: string
  resolvedAt: string | null
  lastVerifiedAt: string | null
}

export async function getProjectFindings(
  projectId: string,
): Promise<Finding[]> {
  const response = await fetch(
    `${API_BASE}/projects/${projectId}/findings`,
  )

  if (!response.ok) {
    throw new Error(
      (await response.text()) ||
        `Failed to load findings: HTTP ${response.status}`,
    )
  }

  return response.json() as Promise<Finding[]>
}

export async function getOpenFindings(
  projectId: string,
): Promise<Finding[]> {
  const response = await fetch(
    `${API_BASE}/projects/${projectId}/findings/open`,
  )

  if (!response.ok) {
    throw new Error(
      (await response.text()) ||
        `Failed to load open findings: HTTP ${response.status}`,
    )
  }

  return response.json() as Promise<Finding[]>
}


