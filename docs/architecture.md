# Architecture

## Enterprise Modernization as Code

The project separates business intent from platform implementation.

## Architecture

                    Specification
                         |
                         v
                 Canonical Model
                         |
          +--------------+--------------+
          |                             |
          v                             v
   Oracle Forms Adapter          SAP RAP Adapter
          |                             |
          v                             v
      Forms XML                    CDS / Behavior
                                         |
                                         v
                                       OData

## Reverse Modernization Flow

Existing Oracle Forms
        |
        v
Forms XML
        |
        v
Migration Agent
        |
        v
Canonical Model
        |
        v
SAP RAP Adapter
        |
        v
RAP / CDS / OData

## Future Targets

The same canonical model could later target:

- Oracle APEX
- SAP CAP
- React
- Angular
- Flutter
- REST APIs
- Spring Boot
- .NET
