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
export async function getProjectFindings(
  projectId: string,
): Promise<Finding[]> {
  const response = await fetch(
    `${API_BASE}/projects/${projectId}/findings`,
  )

  if (!response.ok) {
    throw new Error(
      (await response.text()) ||
        `Failed to fetch findings: HTTP ${response.status}`,
    )
  }

  const data = await response.json()

  // API array return kare to directly use karo.
  if (Array.isArray(data)) {
    return data as Finding[]
  }

  // API { findings: [...] } return kare to usko handle karo.
  if (Array.isArray(data.findings)) {
    return data.findings as Finding[]
  }

  return []
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
    `${API_BASE}/projects/${projectId}/upload`,
    {
      method: 'POST',
      body: formData,
    },
  )

  if (!response.ok) {
    throw new Error(await response.text() || 'ZIP upload failed')
  }

  return response.text()
}
export async function runStaticAnalysis(
  projectId: string,
  analysisId: string,
): Promise<Finding[]> {
  const response = await fetch(
    `${API_BASE}/projects/${projectId}/analyses/${analysisId}/static-analysis`,
    { method: 'POST' },
  )

  if (!response.ok) {
    throw new Error(
      (await response.text()) ||
        `Static analysis failed: HTTP ${response.status}`,
    )
  }

  return response.json() as Promise<Finding[]>
}
export async function analyzeProject(
  projectId: string,
): Promise<Analysis> {
  const response = await fetch(
    `${API_BASE}/projects/${projectId}/analyze`,
    { method: 'POST' },
  )

  if (!response.ok) {
    throw new Error(
      (await response.text()) || `Analysis failed: HTTP ${response.status}`,
    )
  }

  return response.json() as Promise<Analysis>
}