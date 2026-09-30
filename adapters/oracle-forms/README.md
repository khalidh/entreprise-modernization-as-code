# Oracle Forms Adapter

This adapter transforms the canonical application model into an
Oracle Forms representation and keeps the native Forms Builder export
available for comparison.

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

Forms Builder export of the binary module:
customer.fmb -> generated/oracle-forms/customer_fmb.xml

## Principle

`generated/oracle-forms/customer.xml` is the model-derived representation
validated by `generated/oracle-forms/customer.xsd`. It is not the native
Forms Builder XML format.

`generated/oracle-forms/customer_fmb.xml` is the native XML export of the
Forms Builder module `customer.fmb`. Re-export it from Forms Builder whenever
the binary module changes; do not hand-edit it as a substitute for updating
the `.fmb`.

## Test in a Browser with Oracle Database

The local browser preview supports listing, creating, editing, deleting, and
searching customers through Oracle Database.

1. Ensure the `FREEPDB1` service is open and the `CUSTOMER_APP` schema exists.
        On this machine Oracle Database Free is listening at `127.0.0.1:1521`; the
        separate Oracle 19c instance is `ORCLPDB` on port `1522`.
2. Connect to Oracle as `CUSTOMER_APP` and run
        `generated/oracle-apex/customer.sql` once to create the table and triggers.
3. From the repository root, start `generated/oracle-forms/run-customer-preview.ps1`.
        Press Enter to use `jdbc:oracle:thin:@//127.0.0.1:1521/FREEPDB1`, or enter
        the URL shown in your Oracle connection. Then enter the `CUSTOMER_APP`
        password at the secure prompt. Set `CUSTOMER_DB_USER` first if your schema
        has a different name.
4. Open `http://127.0.0.1:8080/` in a browser.

The preview binds only to loopback and uses the Oracle JDBC driver from the
local Oracle Database Free installation. It is a runnable browser client for
the same Oracle schema, not an Oracle APEX application export.

Business requirements remain in:

specs/

Canonical application semantics remain in:

model/
