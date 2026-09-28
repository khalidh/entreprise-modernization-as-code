# Enterprise Modernization as Code

Experimental project combining:

- Spec-Driven Development
- Everything as Code
- Agentic Coding
- Legacy Modernization
- Oracle Forms
- SAP RAP
- CDS
- OData

## Core Idea

Use a technology-independent canonical application model between
functional specifications and implementation technologies.

## Pipeline

specs/customer.md
        |
        v
    Spec Agent
        |
        v
model/customer.yaml
        |
    +---+----------------+
    |                    |
    v                    v
Forms Agent           RAP Agent
    |                    |
    v                    v
Forms XML           CDS / RAP / OData

## Directories

specs/
Functional specifications.

model/
Canonical technology-independent application models.

agents/
Instructions for specialized AI agents.

adapters/
Platform-specific transformations.

generated/
Generated platform artifacts.

tests/
Generated and manual tests.

docs/
Architecture and project documentation.

## First Experiment

1. Read specs/customer.md
2. Use agents/spec-agent/AGENT.md
3. Generate model/customer.yaml
4. Review the model
5. Generate Oracle Forms XML
6. Generate SAP RAP artifacts
7. Compare both implementations
