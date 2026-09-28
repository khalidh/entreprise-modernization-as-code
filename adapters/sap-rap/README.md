# SAP RAP Adapter

This adapter transforms the canonical application model into
SAP RAP artifacts.

## Pipeline

model/customer.yaml
        |
        v
SAP RAP Generator
        |
        +-- CDS interface view
        +-- behavior definition
        +-- projection
        +-- projection behavior
        +-- service definition
        +-- metadata extension
        |
        v
OData Service

## Principle

SAP-specific concepts must remain outside the canonical model.
