# Casita

Gestionale minimale per inquilini, contratti e spese, utilizzabile solo tramite
chat OpenAI con function calling. Gli immobili sono un'anagrafica di supporto
in sola lettura.

## Avvio

Requisiti: JDK 21 e una chiave OpenAI.

### IntelliJ IDEA

1. Configurare la variabile di ambiente `OPENAI_API_KEY` nella Run Configuration
   di `CasitaApplication` oppure nel proprio account Windows.
2. Aprire `CasitaApplication` e premere **Run**.

### PowerShell

```powershell
$env:OPENAI_API_KEY = "sk-..."
.\mvnw.cmd spring-boot:run
```

La chiave deve essere conservata esclusivamente in una variabile d'ambiente.
Non inserirla in `application.properties`, nel codice sorgente o in file
versionati. Le directory `.idea/` e `.vscode/`, i file `.env` e i log sono
esclusi dalla repository tramite `.gitignore`.

Il servizio ascolta su `http://127.0.0.1:8080`. Il database H2 viene ricreato
e popolato a ogni avvio.

```http
POST /api/chat
Content-Type: application/json

{"message":"Quali contratti scadono nei prossimi 90 giorni?"}
```

La chat supporta:

- CRUD di inquilini, contratti e spese;
- contratti in scadenza nei prossimi X giorni;
- spese in arrivo nei prossimi X giorni;
- totale delle spese per immobile.

Creazioni, modifiche e cancellazioni logiche richiedono sempre un secondo
messaggio contenente esattamente `conferma`; usare `annulla` per scartare
l'operazione. L'unico endpoint applicativo è `POST /api/chat`.
