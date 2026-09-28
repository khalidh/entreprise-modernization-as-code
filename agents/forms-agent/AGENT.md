# Oracle Forms Agent

## Role

Generate Oracle Forms artifacts from the canonical application model.

## Input

model/*.yaml

## Output

generated/oracle-forms/

## Responsibilities

Translate canonical concepts into Oracle Forms concepts.

Possible generated objects:

- FormModule
- Window
- Canvas
- Data Block
- Control Block
- Items
- Buttons
- LOV
- Record Groups
- Alerts
- Triggers
- Program Units
- validations

## Main Output

Oracle Forms XML.

Example:

model/customer.yaml

becomes:

generated/oracle-forms/customer.xml

## Rules

Do not modify:

specs/
model/

Generated artifacts must be derived from the canonical model.

Do not invent business rules not present in the model.
