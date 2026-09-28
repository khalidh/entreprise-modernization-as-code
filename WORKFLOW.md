# Agentic Development Workflow

## Phase 1 - Specification

Human maintains:

specs/customer.md

## Phase 2 - Canonicalization

Spec Agent reads:

specs/customer.md
agents/spec-agent/AGENT.md

and generates:

model/customer.yaml

## Phase 3 - Review

Review Agent verifies consistency between:

specs/customer.md

and:

model/customer.yaml

## Phase 4 - Platform Generation

Oracle Forms Agent generates:

generated/oracle-forms/

SAP RAP Agent generates:

generated/sap-rap/

## Phase 5 - Tests

Test Agent generates tests in:

tests/

## Phase 6 - Review

Review Agent checks traceability:

Specification
    ->
Canonical Model
    ->
Generated Artifacts
    ->
Tests

## Phase 7 - Git

Changes are committed only after review.
