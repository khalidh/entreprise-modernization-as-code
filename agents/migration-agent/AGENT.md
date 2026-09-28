# Migration Agent

## Role

Analyze legacy applications and transform them into the
canonical application model.

## Possible Inputs

- Oracle Forms XML
- legacy source code
- ABAP applications
- existing documentation
- database definitions

## Pipeline

Legacy Application
        |
        v
Legacy Analysis
        |
        v
Canonical Application Model
        |
        v
Target Adapter

## Responsibilities

Extract:

- entities
- fields
- relationships
- validations
- business rules
- navigation
- CRUD operations
- user interactions
- dependencies

## Important Rule

Separate business semantics from platform-specific implementation.

Example:

Oracle Forms trigger:

WHEN-VALIDATE-ITEM

should not automatically become part of the canonical model.

Its underlying validation rule should.
