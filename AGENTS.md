# AGENT WORKFLOW & PROTOCOLS

> **IMPORTANT**: This file acts as the primary entry point for AI Agents working on this project.
> You are required to strictly adhere to the file structure and documentation standards defined below.

## 1. Documentation Standards

When writing logs, plans, or reviews in this project, you must follow these style guidelines:

* **No Emojis**: Do not use emojis in any markdown files. Maintain a strictly professional and technical tone.
* **High Readability**:
    * Use **Bold** text to highlight key entities (filenames, function names, critical statuses).
    * Use **Lists** (bullet points) to break down complex information.
    * Ensure proper **Line Breaks** between sections to prevent "walls of text."
    * Use **Code Blocks** for file paths, commands, and code snippets.
* **Atomic Updates**: When updating logs, append new information without deleting past context unless explicitly instructed.

---

## 2. Directory Structure & Responsibilities

The agent workspace is isolated in the `.agents/` directory. Do not store context in the root directory.

| Directory | Access | Purpose |
| :--- | :--- | :--- |
| **`.agents/context/`** | **Read-Only** | Project goals, tech stack, and architectural constraints. |
| **`.agents/plan/`** | **Read/Write** | High-level roadmaps and task backlogs. |
| **`.agents/work/`** | **Write** | Active scratchpad for the current task. One file per task. |
| **`.agents/review/`** | **Write** | Self-review notes, test results, and QA logs before completion. |
| **`.agents/issue/`** | **Write** | detailed analysis of bugs, errors, or blockers encountered. |
| **`.agents/log/`** | **Append** | Chronological history of completed tasks (Source of Truth). |

---

## 3. Standard Operational Procedure

For every assigned objective, execute the following cycle:

### Phase 1: Analysis & Setup
1.  **Read Context**: Review `AGENTS.md` and `.agents/context/` to align with the project scope.
2.  **Select Task**: Identify the next item from `.agents/plan/`.
3.  **Initialize Workspace**:
    * Create a new file in `.agents/work/` (e.g., `.agents/work/task_name.md`).
    * Copy the specific requirements from the plan into this file.
    * Use this file to draft your implementation plan before writing code.

### Phase 2: Execution
1.  **Implement**: Write the code in the project source directories.
2.  **Update Context**: Continuously update your status in the `.agents/work/` file.

### Phase 3: Verification & Closure
1.  **Self-Review**: Create a summary in `.agents/review/` verifying that requirements are met.
2.  **Log History**: Append a summary of the completed task to `.agents/log/history.md`.
    * *Format*: Timestamp, Task Name, Summary of Changes, Modified Files.
3.  **Update Plan**: Mark the task as `[x]` in `.agents/plan/`.
4.  **Cleanup**: (Optional) Move the temporary `.agents/work/` file to an archive if needed.
