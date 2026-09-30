# Oracle APEX Customer Application

`customer.sql` creates the database table and enforces the customer rules from `model/customer.yaml`, aligned with the fields and triggers in the Oracle Forms XML export. `ACTIVE` uses the Forms representation `Y`/`N`.

## Install

1. Open the target APEX workspace and go to **SQL Workshop > SQL Scripts**.
2. Upload and run `customer.sql` in the parsing schema used by the application.
3. In **App Builder**, create a page using **Report with Form** and select the `CUSTOMER` table. This creates the interactive report and the create, edit, and delete form processes.
4. Set the report's default sort to `NAME` ascending. Make `CUSTOMER_ID` read-only on the edit form.
5. Configure `ACTIVE` as a switch or select list with `Y`/`N` return values; default it to `Y` for new records.
6. To match the Forms actions, use the generated form processes for Create, Read, Update, and Delete. The report provides Search; add a Clear button that resets the search items. For model-defined exact and partial search, create the report page items shown below and use this query as the report region source. Replace `XX` with the report page number.

```sql
SELECT CUSTOMER_ID, NAME, EMAIL, CITY, COUNTRY, ACTIVE
FROM CUSTOMER
WHERE (:PXX_CUSTOMER_ID IS NULL OR CUSTOMER_ID = :PXX_CUSTOMER_ID)
	AND (:PXX_NAME_EXACT IS NULL OR UPPER(NAME) = UPPER(:PXX_NAME_EXACT))
	AND (:PXX_EMAIL_EXACT IS NULL OR UPPER(EMAIL) = UPPER(:PXX_EMAIL_EXACT))
	AND (:PXX_CITY_EXACT IS NULL OR UPPER(CITY) = UPPER(:PXX_CITY_EXACT))
	AND (:PXX_COUNTRY_EXACT IS NULL OR UPPER(COUNTRY) = UPPER(:PXX_COUNTRY_EXACT))
	AND (:PXX_NAME_CONTAINS IS NULL OR INSTR(UPPER(NAME), UPPER(:PXX_NAME_CONTAINS)) > 0)
	AND (:PXX_EMAIL_CONTAINS IS NULL OR INSTR(UPPER(EMAIL), UPPER(:PXX_EMAIL_CONTAINS)) > 0)
	AND (:PXX_CITY_CONTAINS IS NULL OR INSTR(UPPER(CITY), UPPER(:PXX_CITY_CONTAINS)) > 0)
ORDER BY NAME ASC
```

The table constraints enforce the required fields, primary key, unique email, and `ACTIVE` values (`Y`/`N`). The triggers enforce the email format from the Forms item validation and prevent changing `CUSTOMER_ID` after creation. The email unique constraint and primary key replace the Forms `PRE-INSERT` duplicate check at the database level.

This is the schema install script for an APEX application, not an APEX application export. A full export depends on the target APEX workspace and application ID.