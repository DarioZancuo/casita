# Casita

Casita is a proof of concept for a **chat-only property rental management
application**. It uses an OpenAI model with function calling to let users manage
rental data through natural-language messages instead of traditional forms or
dedicated CRUD endpoints.

The chat can help manage:

- tenants;
- rental contracts;
- condominium and property-related expenses;
- contracts approaching their expiration date;
- upcoming expenses;
- expense totals by property.

Properties are currently a read-only reference catalog. They can be listed and
associated with contracts and expenses, but they cannot be created, updated, or
deleted through the chat.

## Available chat operations

All application features are exposed through natural-language requests sent to
the chat endpoint. The model interprets the request and uses the corresponding
application function; users do not invoke function names directly.

### Properties

- List all active properties, including their identifiers, codes, descriptions,
  addresses, and cities.
- Use an existing property when creating or updating a contract.
- Use an existing property when creating or updating an expense.
- Calculate the total number and value of expenses recorded for a property.

Property creation, update, and deletion are intentionally not available in this
PoC. A property must already exist in the reference catalog before it can be
associated with a contract or expense.

Example requests:

- `Elenca gli immobili disponibili.`
- `Calcola il totale delle spese per l'immobile AS-29.`

### Tenants

- List all active tenants.
- Retrieve the details of a tenant by identifier.
- Create a tenant with name, surname, tax code, email, and telephone number.
- Completely update an existing tenant.
- Logically delete a tenant while preserving historical data.

Name, surname, and tax code are required when creating or updating a tenant. An
active tax code must be unique. A tenant with an active or future contract
cannot be deleted.

Example requests:

- `Elenca tutti gli inquilini.`
- `Mostra i dettagli dell'inquilino 2.`
- `Crea l'inquilino Mario Rossi con codice fiscale ..., email ... e telefono ...`
- `Modifica l'email dell'inquilino 2.`
- `Elimina l'inquilino 2.`

### Rental contracts

- List all active contracts, including their calculated status and expiration
  date.
- Retrieve the details of a contract by identifier.
- Find contracts expiring between today and a requested number of days.
- Create a contract that associates an existing property with an existing
  tenant.
- Completely update an existing contract.
- Logically delete a contract while preserving historical data.

A contract requires a property, tenant, start date, end date, positive monthly
rent, and a non-negative deposit. Dates use ISO format (`yyyy-MM-dd`) internally.
The end date cannot precede the start date, and contracts for the same property
cannot overlap. Contract status is calculated as `FUTURO`, `ATTIVO`, or
`SCADUTO` according to the current date.

Example requests:

- `Elenca tutti i contratti.`
- `Quali contratti scadono nei prossimi 90 giorni?`
- `Mostra il contratto 1.`
- `Crea un contratto per l'immobile AS-29 e Mario Rossi dal 2027-02-01 al 2028-01-31, con canone di 950 euro e deposito di 1900 euro.`
- `Modifica il canone del contratto 1.`
- `Elimina il contratto 1.`

### Expenses

- List all active condominium or property-related expenses.
- Retrieve the details of an expense by identifier.
- Find expenses dated between today and a requested number of days.
- Create an expense associated with an existing property.
- Completely update an existing expense.
- Logically delete an expense while preserving historical data.
- Calculate the total number and value of expenses for a property.

An expense requires an existing property, description, category, positive
amount, and date. Notes are optional. Dates use ISO format (`yyyy-MM-dd`)
internally.

Example requests:

- `Elenca tutte le spese.`
- `Quali spese sono in arrivo nei prossimi 30 giorni?`
- `Mostra la spesa 3.`
- `Registra una spesa di 250 euro per l'immobile AS-29, categoria manutenzione, in data 2027-01-15.`
- `Modifica l'importo della spesa 3.`
- `Elimina la spesa 3.`

### Unsupported operations

The assistant does not manage property CRUD operations, rent payments, property
availability, authentication, authorization, or user accounts.

## Technology

- Java 21
- Spring Boot
- Spring AI with OpenAI function calling
- Spring Data JPA
- In-memory H2 database

The H2 database is recreated and populated with sample data every time the
application starts.

## Requirements

- JDK 21
- An OpenAI API key with available API credit

An API key is separate from a ChatGPT subscription. Make sure the OpenAI API
project associated with the key has billing or sufficient credit enabled.

## OpenAI API key

The application reads the key from the `OPENAI_API_KEY` environment variable.
The key must start with a valid OpenAI key prefix, such as `sk-` or `sk-proj-`.

Keep the key exclusively in an environment variable. Never place it in
`application.properties`, source code, or any version-controlled file. The
`.idea/` and `.vscode/` directories, `.env` files, logs, and build output are
excluded through `.gitignore`.

### IntelliJ IDEA

1. Open the Run Configuration for `CasitaApplication`.
2. Add the environment variable `OPENAI_API_KEY` with your API key as its value.
3. Run `CasitaApplication`.

After changing the key, stop and restart the application so that the new value
is loaded.

### PowerShell

```powershell
$env:OPENAI_API_KEY = "sk-..."
.\mvnw.cmd spring-boot:run
```

An alternative model can be selected through the optional `OPENAI_MODEL`
environment variable. The default model is configured in
`application.properties`.

## Using the chat API

The service listens on `http://127.0.0.1:8080`. Its only application endpoint is:

```http
POST /api/chat
Content-Type: application/json

{"message":"Which contracts will expire in the next 90 days?"}
```

Example response:

```json
{
  "success": true,
  "data": {
    "reply": "..."
  },
  "error": null
}
```

The model selects and invokes the appropriate application functions according
to the user's request. Read operations are executed immediately. Creating,
updating, and logically deleting tenants, contracts, or expenses uses a
two-step confirmation flow:

1. Send a natural-language request describing the change.
2. Send exactly `conferma` to apply it, or `annulla` to discard it.

The confirmation commands are currently in Italian because the assistant is
configured to interact in Italian.

Only one change can be pending at a time. A second write request is rejected
until the current proposal is confirmed or cancelled. Confirmation commands are
matched after trimming whitespace and ignoring letter case, but the message must
otherwise contain only `conferma` or `annulla`.

Logical deletion sets a deletion timestamp instead of physically removing the
record. Normal list and detail operations ignore logically deleted records,
while historical database relationships remain preserved.

## Proof-of-concept limitations

- There is no traditional graphical user interface; all supported operations
  go through `POST /api/chat`.
- Properties are read-only.
- The database is in memory and is reset at startup.
- Conversation history is not persisted between chat requests. Users should
  include all required information in the same request when possible.
- The pending confirmation state is application-wide rather than isolated per
  user, so this implementation is suitable only for a single-user PoC.
- This project is intended to demonstrate a chat-driven workflow and function
  calling, not production-ready authentication, authorization, persistence, or
  multi-user isolation.
