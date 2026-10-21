import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import {
  FolderKanban,
  Plus,
  Upload,
  Play,
  RefreshCw,
  AlertCircle,
  CheckCircle2,
  LoaderCircle,
  FileCode2,
  Activity,
  X,
} from 'lucide-react'

import {
  getProjects,
  getProjectFindings,
  createProject,
  uploadProject,
  analyzeProject,
  runStaticAnalysis,
} from './api'

import type { Project, Finding } from './api'
import './ProjectsPanel.css'

type StepStatus = 'idle' | 'running' | 'success' | 'error'

type ProjectsPanelProps = {
  projectId: string
  onProjectChange: (projectId: string) => void
}

export default function ProjectsPanel({
  projectId,
  onProjectChange,
}: ProjectsPanelProps) {
  const [projects, setProjects] = useState<Project[]>([])
  const [selectedProjectId, setSelectedProjectId] = useState('')

  const [projectName, setProjectName] = useState('')
  const [description, setDescription] = useState('')
  const [technologies, setTechnologies] = useState('Java, Spring Boot')

  const [showCreateForm, setShowCreateForm] = useState(false)

  const [zipFile, setZipFile] = useState<File | null>(null)
  const [findings, setFindings] = useState<Finding[]>([])

  const [loadingProjects, setLoadingProjects] = useState(false)
  const [creating, setCreating] = useState(false)
  const [runningAnalysis, setRunningAnalysis] = useState(false)

  const [status, setStatus] = useState<StepStatus>('idle')
  const [statusMessage, setStatusMessage] = useState('')
  const [error, setError] = useState('')

  const selectedProject = projects.find(
    (project) => project.id === selectedProjectId,
  )

  async function loadProjects() {
    setLoadingProjects(true)
    setError('')

    try {
      const data = await getProjects()

      setProjects(data)

      setSelectedProjectId((currentId) => {
        if (currentId && data.some((project) => project.id === currentId)) {
          return currentId
        }

        return data[0]?.id ?? ''
      })
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Projects load nahi ho paaye.',
      )
    } finally {
      setLoadingProjects(false)
    }
  }

useEffect(() => {
  if (!projectId) {
    setFindings([])
    return
  }

  let cancelled = false

  async function loadFindings() {
    try {
      const data = await getProjectFindings(projectId)

      if (!cancelled) {
        setFindings(data)
      }
    } catch (error) {
      console.error('Failed to load project findings:', error)

      if (!cancelled) {
        setFindings([])
      }
    }
  }

  void loadFindings()

  return () => {
    cancelled = true
  }
}, [projectId])

  useEffect(() => {
    void loadProjects()
  }, [])

 async function handleCreateProject(
  event: FormEvent<HTMLFormElement>,
) {
    event.preventDefault()
    setError('')
    setStatusMessage('')

    if (!projectName.trim()) {
      setError('Project ka naam enter karo.')
      return
    }

    setCreating(true)

    try {
      const technologyList = technologies
        .split(',')
        .map((technology) => technology.trim())
        .filter(Boolean)

      const project = await createProject({
        name: projectName.trim(),
        description: description.trim(),
        technologies: technologyList,
      })

      setProjects((current) => {
        const alreadyExists = current.some(
          (item) => item.id === project.id,
        )

        return alreadyExists ? current : [project, ...current]
      })

     onProjectChange(project.id)
      setProjectName('')
      setDescription('')
      setTechnologies('Java, Spring Boot')
      setShowCreateForm(false)

      setFindings([])
      setStatus('success')
      setStatusMessage(
        `Project "${project.name}" successfully create ho gaya.`,
      )

      await loadProjects()
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : 'Project create nahi ho paaya.',
      )
    } finally {
      setCreating(false)
    }
  }

  async function handleUploadAndAnalyze() {
    setError('')
    setStatusMessage('')
    setFindings([])

    if (!selectedProjectId) {
      setError('Pehle ek project select karo ya naya project create karo.')
      return
    }

    if (!zipFile) {
      setError('Pehle apne project ki ZIP file select karo.')
      return
    }

    if (!zipFile.name.toLowerCase().endsWith('.zip')) {
      setError('Sirf .zip file upload kar sakte ho.')
      return
    }

    setRunningAnalysis(true)
    setStatus('running')

    try {
      // Step 1: Upload source-code ZIP
      setStatusMessage('Step 1/3: Project ZIP upload ho rahi hai...')

      await uploadProject(selectedProjectId, zipFile)

      // Step 2: Analyze project and detect technologies
      setStatusMessage('Step 2/3: Project analyze ho raha hai...')

      const analysis = await analyzeProject(selectedProjectId)

      // Step 3: Run static analysis
      setStatusMessage('Step 3/3: Static Analysis chal rahi hai...')

      const result = await runStaticAnalysis(
        selectedProjectId,
        analysis.id,
      )

      setFindings(result)
      setZipFile(null)
      setStatus('success')

      setStatusMessage(
        `Analysis complete! ${result.length} findings process hui hain.`,
      )

      await loadProjects()
    } catch (err) {
      setStatus('error')

      setError(
        err instanceof Error
          ? err.message
          : 'Analysis ke dauran error aaya.',
      )

      setStatusMessage(
        'Process complete nahi hua. Error check karke dobara try karo.',
      )
    } finally {
      setRunningAnalysis(false)
    }
  }

  function getSeverityClass(severity: string) {
    switch (severity.toLowerCase()) {
      case 'critical':
        return 'severity-critical'
      case 'high':
        return 'severity-high'
      case 'medium':
        return 'severity-medium'
      case 'low':
        return 'severity-low'
      default:
        return 'severity-default'
    }
  }

  return (
    <section className="projects-panel">
      <div className="projects-header">
        <div>
          <div className="projects-eyebrow">
            <Activity size={15} />
            SOFTWARE INTELLIGENCE
          </div>

          <h2>Project Management</h2>

          <p className="projects-subtitle">
            Create projects, upload source code and run code analysis.
          </p>
        </div>

        <button
          type="button"
          className="projects-primary-button"
          onClick={() => {
            setShowCreateForm((current) => !current)
            setError('')
          }}
        >
          {showCreateForm ? <X size={17} /> : <Plus size={17} />}

          {showCreateForm ? 'Cancel' : 'New Project'}
        </button>
      </div>

      {error && (
        <div className="projects-alert projects-alert-error">
          <AlertCircle size={19} />

          <span>{error}</span>

          <button
            type="button"
            aria-label="Dismiss error"
            onClick={() => setError('')}
          >
            <X size={17} />
          </button>
        </div>
      )}

      {statusMessage && (
        <div
          className={`projects-alert ${
            status === 'error'
              ? 'projects-alert-error'
              : status === 'success'
                ? 'projects-alert-success'
                : 'projects-alert-info'
          }`}
        >
          {status === 'running' ? (
            <LoaderCircle className="projects-spin" size={19} />
          ) : status === 'error' ? (
            <AlertCircle size={19} />
          ) : (
            <CheckCircle2 size={19} />
          )}

          <span>{statusMessage}</span>
        </div>
      )}

      {showCreateForm && (
        <form
          className="projects-card projects-create-form"
          onSubmit={handleCreateProject}
        >
          <h3>
            <Plus size={19} />
            Create New Project
          </h3>

          <label htmlFor="project-name">Project Name *</label>

          <input
            id="project-name"
            type="text"
            placeholder="e.g. Hospital Management System"
            value={projectName}
            onChange={(event) => setProjectName(event.target.value)}
            required
          />

          <label htmlFor="project-description">Description</label>

          <textarea
            id="project-description"
            placeholder="Project ke baare mein likho..."
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            rows={3}
          />

          <label htmlFor="project-technologies">
            Technologies
          </label>

          <input
            id="project-technologies"
            type="text"
            placeholder="Java, Spring Boot, React"
            value={technologies}
            onChange={(event) => setTechnologies(event.target.value)}
          />

          <p className="projects-hint">
            Technologies ko comma se separate karo.
          </p>

          <button
            type="submit"
            className="projects-primary-button"
            disabled={creating}
          >
            {creating ? (
              <LoaderCircle className="projects-spin" size={17} />
            ) : (
              <Plus size={17} />
            )}

            {creating ? 'Creating...' : 'Create Project'}
          </button>
        </form>
      )}

      <div className="projects-stats">
        <div className="projects-stat-card">
          <div className="projects-stat-icon">
            <FolderKanban size={21} />
          </div>

          <div>
            <span>Total Projects</span>
            <strong>{projects.length}</strong>
          </div>
        </div>

        <div className="projects-stat-card">
          <div className="projects-stat-icon">
            <FileCode2 size={21} />
          </div>

          <div>
            <span>Selected Project</span>
            <strong>
              {selectedProject ? '1' : '0'}
            </strong>
          </div>
        </div>

        <div className="projects-stat-card">
          <div className="projects-stat-icon">
            <Activity size={21} />
          </div>

          <div>
            <span>Latest Findings</span>
            <strong>{findings.length}</strong>
          </div>
        </div>
      </div>

      <div className="projects-content-grid">
        <div className="projects-card projects-list-card">
          <div className="projects-card-heading">
            <h3>
              <FolderKanban size={19} />
              Your Projects
            </h3>

            <button
              type="button"
              className="projects-icon-button"
              onClick={() => void loadProjects()}
              disabled={loadingProjects}
              title="Refresh projects"
              aria-label="Refresh projects"
            >
              <RefreshCw
                size={17}
                className={loadingProjects ? 'projects-spin' : ''}
              />
            </button>
          </div>

          {loadingProjects && projects.length === 0 ? (
            <div className="projects-empty">
              <LoaderCircle
                className="projects-spin"
                size={24}
              />

              <p>Projects load ho rahe hain...</p>
            </div>
          ) : projects.length === 0 ? (
            <div className="projects-empty">
              <FolderKanban size={34} />

              <h4>No projects yet</h4>

              <p>
                Start karne ke liye apna pehla project create karo.
              </p>

              <button
                type="button"
                className="projects-secondary-button"
                onClick={() => setShowCreateForm(true)}
              >
                <Plus size={16} />
                Create Project
              </button>
            </div>
          ) : (
            <div className="projects-list">
              {projects.map((project) => (
                <button
                  type="button"
                  key={project.id}
                  className={`projects-list-item ${
                    selectedProjectId === project.id
                      ? 'projects-list-item-active'
                      : ''
                  }`}
                  onClick={() => {
                    setSelectedProjectId(project.id)
                    setFindings([])
                    setStatus('idle')
                    setStatusMessage('')
                    setError('')
                  }}
                >
                  <div className="projects-list-icon">
                    <FolderKanban size={20} />
                  </div>

                  <div className="projects-list-details">
                    <strong>{project.name}</strong>

                    <span>
                      {project.description || 'No description added'}
                    </span>

                    <small>
                      {project.technologies?.length
                        ? project.technologies.join(', ')
                        : 'Technologies not specified'}
                    </small>
                  </div>

                  {selectedProjectId === project.id && (
                    <CheckCircle2
                      size={18}
                      className="projects-selected-icon"
                    />
                  )}
                </button>
              ))}
            </div>
          )}
        </div>

        <div className="projects-card projects-upload-card">
          <div className="projects-card-heading">
            <h3>
              <Upload size={19} />
              Analyze Your Code
            </h3>
          </div>

          {selectedProject ? (
            <>
              <div className="projects-selected-project">
                <div className="projects-list-icon">
                  <FolderKanban size={21} />
                </div>

                <div>
                  <span>Selected project</span>
                  <strong>{selectedProject.name}</strong>
                </div>
              </div>

              <label
                htmlFor="project-zip"
                className="projects-upload-area"
              >
                <Upload size={29} />

                <strong>
                  {zipFile ? zipFile.name : 'Upload project ZIP'}
                </strong>

                <span>
                  {zipFile
                    ? 'File selected successfully'
                    : 'Click here to select your source-code ZIP file'}
                </span>

                <small>Only .zip files are supported</small>

                <input
                  id="project-zip"
                  type="file"
                  accept=".zip,application/zip"
                  disabled={runningAnalysis}
                  onChange={(event) => {
                    const file = event.target.files?.[0] ?? null

                    setZipFile(file)
                    setError('')
                    setStatus('idle')
                    setStatusMessage('')
                  }}
                />
              </label>

              {zipFile && (
                <button
                  type="button"
                  className="projects-remove-file"
                  onClick={() => setZipFile(null)}
                  disabled={runningAnalysis}
                >
                  <X size={15} />
                  Remove selected file
                </button>
              )}

              <div className="projects-process-steps">
                <div>
                  <span className="projects-step-number">1</span>
                  <span>Upload ZIP</span>
                </div>

                <div>
                  <span className="projects-step-number">2</span>
                  <span>Detect Technologies</span>
                </div>

                <div>
                  <span className="projects-step-number">3</span>
                  <span>Static Analysis</span>
                </div>
              </div>

              <button
                type="button"
                className="projects-analyze-button"
                onClick={() => void handleUploadAndAnalyze()}
                disabled={runningAnalysis || !zipFile}
              >
                {runningAnalysis ? (
                  <LoaderCircle
                    className="projects-spin"
                    size={19}
                  />
                ) : (
                  <Play size={19} />
                )}

                {runningAnalysis
                  ? 'Analysis Running...'
                  : 'Upload & Analyze Project'}
              </button>

              <p className="projects-hint">
                Upload karne ke baad project analysis aur static
                analysis automatically sequence mein run honge.
              </p>
            </>
          ) : (
            <div className="projects-empty">
              <Upload size={34} />

              <h4>Select a project first</h4>

              <p>
                ZIP upload karne se pehle list mein project select karo.
              </p>
            </div>
          )}
        </div>
      </div>

      <div className="projects-card projects-findings-card">
        <div className="projects-card-heading">
          <div>
            <h3>
              <Activity size={19} />
              Static Analysis Results
            </h3>

            <p className="projects-subtitle">
              Current session ke latest analysis results.
            </p>
          </div>

          <span className="projects-findings-count">
            {findings.length} findings
          </span>
        </div>

        {findings.length === 0 ? (
          <div className="projects-empty projects-results-empty">
            <FileCode2 size={32} />

            <h4>No new results to display</h4>

            <p>
              Project select karke ZIP upload aur analysis run karo.
            </p>
          </div>
        ) : (
          <div className="projects-table-wrapper">
            <table className="projects-findings-table">
              <thead>
                <tr>
                  <th>Severity</th>
                  <th>Rule</th>
                  <th>File</th>
                  <th>Line</th>
                  <th>Message</th>
                  <th>Status</th>
                </tr>
              </thead>

         <tbody>
  {findings.map((finding, index) => (
    <tr
      key={
        finding.id ||
        `${finding.rule || finding.type || 'finding'}-${index}`
      }
    >
      <td>
        <span
          className={`projects-severity ${getSeverityClass(
            finding.severity || '',
          )}`}
        >
          {finding.severity || 'Unknown'}
        </span>
      </td>

      <td>{finding.rule || finding.type || '—'}</td>

      <td className="projects-file-path">
        {finding.filePath || '—'}
      </td>

      <td>{finding.lineNumber ?? '—'}</td>

      <td>{finding.message || '—'}</td>

      <td>
        <span className="projects-status-badge">
          {finding.status || 'Unknown'}
        </span>
      </td>
    </tr>
  ))}
</tbody>
            </table>
          </div>
        )}
      </div>
    </section>
  )
}