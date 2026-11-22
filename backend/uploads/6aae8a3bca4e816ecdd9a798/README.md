# DevRecovery Sample Java Project

Sample Java project designed to test DevRecovery's end-to-end pipeline:
- Project & ZIP upload
- Technology & static analysis (`System.out.println`, `TODO` comments, etc.)
- AI root-cause analysis (`Ollama` + `qwen2.5-coder:7b`)
- Fix plan generation & human approval
- Fix agent patch apply, compile/verification, and rollback
