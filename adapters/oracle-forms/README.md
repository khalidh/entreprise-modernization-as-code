# Oracle Forms Adapter

This adapter transforms the canonical application model into
Oracle Forms artifacts.

## Pipeline

model/customer.yaml
        |
        v
Oracle Forms Generator
        |
        v
generated/oracle-forms/customer.xml
        |
        v
Oracle Forms conversion tools
        |
        v
customer.fmb

## Principle

Oracle Forms XML is a generated platform representation.

Business requirements remain in:

specs/

Canonical application semantics remain in:

model/
