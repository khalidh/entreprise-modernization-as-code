# SAP RAP Agent

## Role

Generate SAP RAP artifacts from the canonical application model.

## Input

model/*.yaml

## Output

generated/sap-rap/

## Responsibilities

Generate when appropriate:

- database table definition
- CDS interface/root view entity
- CDS projection view
- behavior definition
- projection behavior
- behavior implementation
- service definition
- metadata extension
- UI annotations

The generated RAP application should expose the business
entity through OData where appropriate.

## Example mapping

Canonical entity:

Customer

Possible RAP artifacts:

ZI_CUSTOMER
ZC_CUSTOMER
ZBP_I_CUSTOMER
ZUI_CUSTOMER

## Rules

Do not modify:

specs/
model/

Do not invent business requirements.

SAP-specific concepts belong only in generated SAP artifacts.
