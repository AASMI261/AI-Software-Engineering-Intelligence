const API_BASE = '/api'

export type Project = {
  id: string
  name: string
  description?: string
  technologies?: string[]
  createdAt?: string
  updatedAt?: string
}

export type Analysis = {
  id: string
  projectId: string
  detectedTechnologies: string[]
  analyzedAt: string
}

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

export type FixPlan = {
  id?: string
  _id?: string
  projectId?: string
  findingId?: string
  aiAnalysisId?: string
  filePath?: string
  lineNumber?: number | null
  problem?: string
  changeDescription?: string
  proposedChanges?: string
  testsToRun?: string
  verificationSteps?: string
  risk?: string
  confidence?: string
  status?: string
  generatedAt?: string
}

export type QualityScoreResponse = {
  projectId: string
  qualityScore: number
  rating: string
  totalFindings: number
  openFindings: number
  resolvedFindings: number
  severityCounts: Record<string, number>
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    const message = await response.text()
    throw new Error(
      message || `Request failed with status ${response.status}`,
    )
  }

  return response.json() as Promise<T>
}

export async function getProjects(): Promise<Project[]> {
  const response = await fetch(`${API_BASE}/projects`)
  return handleResponse<Project[]>(response)
}

export async function createProject(data: {
  name: string
  description: string
  technologies: string[]
}): Promise<Project> {
  const response = await fetch(`${API_BASE}/projects`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  })

  return handleResponse<Project>(response)
}

export async function uploadProject(
  projectId: string,
  file: File,
): Promise<string> {
  const formData = new FormData()
  formData.append('file', file)

  const response = await fetch(
    `${API_BASE}/projects/${encodeURIComponent(projectId)}/upload`,
    {
      method: 'POST',
      body: formData,
    },
  )

  if (!response.ok) {
    throw new Error((await response.text()) || 'ZIP upload failed')
  }

  return response.text()
}

export async function analyzeProject(projectId: string): Promise<Analysis> {
  const response = await fetch(
    `${API_BASE}/projects/${encodeURIComponent(projectId)}/analyze`,
    { method: 'POST' },
  )

  return handleResponse<Analysis>(response)
}

export async function runStaticAnalysis(
  projectId: string,
  analysisId: string,
): Promise<Finding[]> {
  const response = await fetch(
    `${API_BASE}/projects/${encodeURIComponent(projectId)}/analyses/${encodeURIComponent(analysisId)}/static-analysis`,
    { method: 'POST' },
  )

  return handleResponse<Finding[]>(response)
}

export async function getProjectFindings(projectId: string): Promise<Finding[]> {
  const response = await fetch(
    `${API_BASE}/projects/${encodeURIComponent(projectId)}/findings`,
  )

  if (!response.ok) {
    throw new Error(
      (await response.text()) ||
        `Failed to fetch findings: HTTP ${response.status}`,
    )
  }

  const data = await response.json()

  if (Array.isArray(data)) {
    return data as Finding[]
  }

  if (Array.isArray(data.findings)) {
    return data.findings as Finding[]
  }

  return []
}

export async function getQualityScore(
  projectId: string,
): Promise<QualityScoreResponse> {
  const response = await fetch(
    `${API_BASE}/projects/${encodeURIComponent(projectId)}/quality-score`,
  )
  return handleResponse<QualityScoreResponse>(response)
}

export async function generateFixPlan(
  projectId: string,
  findingId: string,
): Promise<FixPlan> {
  const response = await fetch(
    `${API_BASE}/projects/${encodeURIComponent(projectId)}/findings/${encodeURIComponent(findingId)}/fix-plan`,
    {
      method: 'POST',
    },
  )

  return handleResponse<FixPlan>(response)
}

export async function getProjectFixPlans(projectId: string): Promise<FixPlan[]> {
  const response = await fetch(
    `${API_BASE}/projects/${encodeURIComponent(projectId)}/fix-plans`,
  )

  const data = await handleResponse<FixPlan[] | { fixPlans?: FixPlan[]; plans?: FixPlan[] }>(
    response,
  )

  if (Array.isArray(data)) {
    return data
  }
  if (Array.isArray(data.fixPlans)) {
    return data.fixPlans
  }
  if (Array.isArray(data.plans)) {
    return data.plans
  }
  return []
}

export async function approveFixPlan(fixPlanId: string): Promise<FixPlan> {
  const response = await fetch(
    `${API_BASE}/fix-plans/${encodeURIComponent(fixPlanId)}/approve`,
    {
      method: 'POST',
    },
  )

  return handleResponse<FixPlan>(response)
}

export async function rejectFixPlan(fixPlanId: string): Promise<FixPlan> {
  const response = await fetch(
    `${API_BASE}/fix-plans/${encodeURIComponent(fixPlanId)}/reject`,
    {
      method: 'POST',
    },
  )

  return handleResponse<FixPlan>(response)
}

export async function applyFixPlan(fixPlanId: string): Promise<FixPlan> {
  const response = await fetch(
    `${API_BASE}/fix-agent/${encodeURIComponent(fixPlanId)}/apply`,
    {
      method: 'POST',
    },
  )

  return handleResponse<FixPlan>(response)
}