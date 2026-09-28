# Customer Management

## 1. Purpose

Provide an application for managing customers.

The application must allow users to:

- Create a customer
- View a customer
- Modify a customer
- Delete a customer
- Search for customers
- Display a list of customers

---

## 2. Entity: Customer

A customer represents a person or organization registered in the system.

### Fields

| Field | Type | Required | Description |
|---|---|---|---|
| customer_id | Integer | Yes | Unique customer identifier |
| name | String(100) | Yes | Customer name |
| email | String(150) | Yes | Customer email |
| phone | String(30) | No | Phone number |
| city | String(80) | No | City |
| country | String(80) | No | Country |
| active | Boolean | Yes | Customer status |

---

## 3. Business Rules

### BR-001
customer_id must be unique.

### BR-002
name is mandatory.

### BR-003
email is mandatory.

### BR-004
email must be unique.

### BR-005
active defaults to true.

### BR-006
customer_id cannot be modified after creation.

---

## 4. Operations

The application supports:

- CREATE
- READ
- UPDATE
- DELETE
- SEARCH

---

## 5. Search

Customers can be searched by:

- customer_id
- name
- email
- city
- country

Partial search must be supported for:

- name
- email
- city

---

## 6. List View

Display:

- customer_id
- name
- email
- city
- country
- active

Default sorting:

name ascending.

---

## 7. Happy Paths

### HP-001 - Create customer

Given the user opens Customer Management

When the user enters:

- Name: Jean Dupont
- Email: jean.dupont@example.com
- City: Nantes
- Country: France

Then a new customer is created.

And active defaults to true.

### HP-002 - Update customer

Given an existing customer

When the user changes the city

Then the customer is updated.

### HP-003 - Search customer

Given existing customers

When the user searches by name

Then matching customers are displayed.

---

## 8. Edge Cases

### EC-001 - Missing name

When name is empty

Then creation must fail.

### EC-002 - Missing email

When email is empty

Then creation must fail.

### EC-003 - Duplicate email

When another customer already has the email

Then creation must fail.

### EC-004 - Invalid email

When email format is invalid

Then creation must fail.

### EC-005 - Unknown customer

When a requested customer_id does not exist

Then the application must report that the customer was not found.

---

## 9. Acceptance Criteria

The feature is complete when:

- customers can be created
- customers can be displayed
- customers can be updated
- customers can be deleted
- customers can be searched
- mandatory fields are validated
- duplicate emails are rejected
- invalid emails are rejected
- happy paths pass
- edge cases pass
