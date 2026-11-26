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

## Proof-of-concept limitations

- There is no traditional graphical user interface; all supported operations
  go through `POST /api/chat`.
- Properties are read-only.
- The database is in memory and is reset at startup.
- This project is intended to demonstrate a chat-driven workflow and function
  calling, not production-ready authentication, authorization, persistence, or
  multi-user isolation.
