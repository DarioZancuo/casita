package com.quozan.casita.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.quozan.casita.ai.AffittoTools;
import com.quozan.casita.dto.ApiResponse;
import com.quozan.casita.dto.ChatRequest;
import com.quozan.casita.dto.ChatResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private static final String SYSTEM_PROMPT = """
        Sei l'assistente del gestionale personale Casita. Rispondi sempre in italiano, in modo chiaro e conciso.
        Gestisci esclusivamente inquilini, contratti e spese. Gli immobili sono consultabili solo per identificare
        l'immobile collegato a contratti e spese. Devi sempre usare gli strumenti disponibili:
        non inventare mai record, ID, importi o date.
        Le letture possono essere eseguite subito. Per creare, aggiornare o cancellare usa esclusivamente un tool
        che inizia con 'proponi'. Questi tool non modificano subito i dati: comunica chiaramente all'utente che
        deve inviare 'conferma' oppure 'annulla'. Non affermare mai che una modifica è avvenuta prima della conferma.
        Per "spese in arrivo" usa il relativo strumento con il numero di giorni richiesto.
        Per il totale spese di un immobile usa il relativo strumento e non calcolare il totale a memoria.
        Non gestire immobili, pagamenti dei canoni o disponibilità: non fanno parte del sistema.
        """;

    private final ChatClient chatClient;
    private final AffittoTools affittoTools;

    public ChatController(ChatClient.Builder builder, AffittoTools affittoTools) {
        this.affittoTools = affittoTools;
        this.chatClient = builder.defaultSystem(SYSTEM_PROMPT).defaultTools(affittoTools).build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChatResponse>> chat(@Valid @RequestBody ChatRequest request) {
        if (affittoTools.isConfirmCommand(request.message())) {
            return ResponseEntity.ok(ApiResponse.ok(new ChatResponse(affittoTools.confirmPending())));
        }
        if (affittoTools.isCancelCommand(request.message())) {
            return ResponseEntity.ok(ApiResponse.ok(new ChatResponse(affittoTools.cancelPending())));
        }
        try {
            String reply = chatClient.prompt().user(request.message()).call().content();
            return ResponseEntity.ok(ApiResponse.ok(new ChatResponse(reply)));
        } catch (RuntimeException ex) {
            log.error("Chiamata al servizio OpenAI non riuscita", ex);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("Servizio AI non disponibile. Verifica OPENAI_API_KEY e riprova."));
        }
    }
}
