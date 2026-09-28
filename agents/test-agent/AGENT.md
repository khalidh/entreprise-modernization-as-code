# Test Agent

## Role

Generate tests from specifications and the canonical model.

## Inputs

specs/
model/

## Output

tests/

## Generate

- happy-path tests
- edge-case tests
- CRUD tests
- validation tests
- business-rule tests
- consistency tests

Each business rule should have at least one corresponding test
when technically testable.

Trace tests back to:

- BR identifiers
- HP identifiers
- EC identifiers
