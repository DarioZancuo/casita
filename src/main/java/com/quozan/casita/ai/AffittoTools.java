package com.quozan.casita.ai;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import com.quozan.casita.service.AffittoService;
import com.quozan.casita.service.AffittoService.ContrattoView;
import com.quozan.casita.service.AffittoService.ImmobileView;
import com.quozan.casita.service.AffittoService.InquilinoView;
import com.quozan.casita.service.SpesaService;
import com.quozan.casita.service.SpesaService.RiepilogoSpese;
import com.quozan.casita.service.SpesaService.SpesaView;

@Component
@RequiredArgsConstructor
public class AffittoTools {
    private final AffittoService affittoService;
    private final SpesaService spesaService;
    private final AtomicReference<PendingOperation> pending = new AtomicReference<>();

    @Tool(description = "Elenca tutti gli immobili non eliminati con i loro ID e codici")
    public List<ImmobileView> elencaImmobili() { return affittoService.immobili(); }

    @Tool(description = "Elenca tutti gli inquilini non eliminati con i loro ID")
    public List<InquilinoView> elencaInquilini() { return affittoService.inquilini(); }

    @Tool(description = "Restituisce il dettaglio di un inquilino tramite il suo ID")
    public InquilinoView dettaglioInquilino(@ToolParam(description = "ID numerico dell'inquilino") Long id) {
        return affittoService.inquilino(id);
    }

    @Tool(description = "Elenca tutti i contratti non eliminati, inclusi stato e scadenza")
    public List<ContrattoView> elencaContratti() { return affittoService.contratti(); }

    @Tool(description = "Restituisce il dettaglio di un contratto tramite il suo ID")
    public ContrattoView dettaglioContratto(@ToolParam(description = "ID numerico del contratto") Long id) {
        return affittoService.contratto(id);
    }

    @Tool(description = "Trova i contratti che scadono da oggi entro il numero di giorni indicato")
    public List<ContrattoView> contrattiInScadenza(@ToolParam(description = "Orizzonte temporale in giorni, ad esempio 30, 60 o 90") int giorni) {
        return affittoService.contrattiInScadenza(giorni);
    }

    @Tool(description = "Elenca tutte le spese condominiali non eliminate")
    public List<SpesaView> elencaSpese() { return spesaService.spese(null, null, null, null); }

    @Tool(description = "Restituisce il dettaglio di una spesa condominiale tramite il suo ID")
    public SpesaView dettaglioSpesa(@ToolParam(description = "ID numerico della spesa") Long id) {
        return spesaService.spesa(id);
    }

    @Tool(description = "Elenca le spese con data da oggi ai prossimi giorni indicati, estremi inclusi")
    public List<SpesaView> speseInArrivo(
            @ToolParam(description = "Numero di giorni futuri, ad esempio 7, 30 o 90") int giorni) {
        return spesaService.speseInArrivo(giorni);
    }

    @Tool(description = "Calcola il totale di tutte le spese registrate per un immobile")
    public RiepilogoSpese totaleSpesePerImmobile(
            @ToolParam(description = "ID numerico dell'immobile") Long immobileId) {
        return spesaService.totalePerImmobile(immobileId);
    }

    @Tool(description = "Propone la creazione di un inquilino. Non salva nulla prima della conferma")
    public String proponiCreaInquilino(String nome, String cognome, String codiceFiscale, String email, String telefono) {
        affittoService.validaNuovoInquilino(nome, cognome, codiceFiscale);
        return propose("Creare l'inquilino " + nome + " " + cognome,
            () -> affittoService.creaInquilino(nome, cognome, codiceFiscale, email, telefono).toString());
    }

    @Tool(description = "Propone la modifica completa di un inquilino. Non salva prima della conferma")
    public String proponiAggiornaInquilino(Long id, String nome, String cognome, String codiceFiscale, String email, String telefono) {
        affittoService.validaAggiornamentoInquilino(id, nome, cognome, codiceFiscale);
        return propose("Aggiornare l'inquilino " + id + " (" + nome + " " + cognome + ")",
            () -> affittoService.aggiornaInquilino(id, nome, cognome, codiceFiscale, email, telefono).toString());
    }

    @Tool(description = "Propone la cancellazione logica di un inquilino preservando lo storico")
    public String proponiEliminaInquilino(Long id) {
        affittoService.validaEliminazioneInquilino(id);
        return propose("Disattivare logicamente l'inquilino " + id, () -> affittoService.eliminaInquilino(id));
    }

    @Tool(description = "Propone la creazione di un contratto. Le date sono ISO yyyy-MM-dd. Non salva prima della conferma")
    public String proponiCreaContratto(Long immobileId, Long inquilinoId, LocalDate dataInizio, LocalDate dataFine,
                                      BigDecimal canoneMensile, BigDecimal deposito, String note) {
        affittoService.validaContratto(null, immobileId, inquilinoId, dataInizio, dataFine, canoneMensile, deposito);
        return propose("Creare il contratto per immobile " + immobileId + " e inquilino " + inquilinoId
                + " dal " + dataInizio + " al " + dataFine,
            () -> affittoService.creaContratto(immobileId, inquilinoId, dataInizio, dataFine, canoneMensile, deposito, note).toString());
    }

    @Tool(description = "Propone la modifica completa di un contratto. Non salva prima della conferma")
    public String proponiAggiornaContratto(Long id, Long immobileId, Long inquilinoId, LocalDate dataInizio,
                                           LocalDate dataFine, BigDecimal canoneMensile, BigDecimal deposito, String note) {
        affittoService.validaContratto(id, immobileId, inquilinoId, dataInizio, dataFine, canoneMensile, deposito);
        return propose("Aggiornare il contratto " + id,
            () -> affittoService.aggiornaContratto(id, immobileId, inquilinoId, dataInizio, dataFine, canoneMensile, deposito, note).toString());
    }

    @Tool(description = "Propone la cancellazione logica di un contratto preservando lo storico")
    public String proponiEliminaContratto(Long id) {
        affittoService.contratto(id);
        return propose("Disattivare logicamente il contratto " + id, () -> affittoService.eliminaContratto(id));
    }

    @Tool(description = "Propone la registrazione di una spesa condominiale. La data è ISO yyyy-MM-dd. Non salva prima della conferma")
    public String proponiCreaSpesa(Long immobileId, String descrizione, String categoria, BigDecimal importo,
                                   LocalDate dataSpesa, String note) {
        spesaService.validaSpesa(null, immobileId, descrizione, categoria, importo, dataSpesa);
        return propose("Registrare una spesa di " + importo + " euro per l'immobile " + immobileId + ": " + descrizione,
            () -> spesaService.creaSpesa(immobileId, descrizione, categoria, importo, dataSpesa, note).toString());
    }

    @Tool(description = "Propone la modifica completa di una spesa. Non salva prima della conferma")
    public String proponiAggiornaSpesa(Long id, Long immobileId, String descrizione, String categoria,
                                       BigDecimal importo, LocalDate dataSpesa, String note) {
        spesaService.validaSpesa(id, immobileId, descrizione, categoria, importo, dataSpesa);
        return propose("Aggiornare la spesa " + id + " a " + importo + " euro",
            () -> spesaService.aggiornaSpesa(id, immobileId, descrizione, categoria, importo, dataSpesa, note).toString());
    }

    @Tool(description = "Propone la cancellazione logica di una spesa preservando lo storico")
    public String proponiEliminaSpesa(Long id) {
        spesaService.spesa(id);
        return propose("Disattivare logicamente la spesa " + id, () -> spesaService.eliminaSpesa(id));
    }

    public boolean isConfirmCommand(String message) { return normalize(message).equals("conferma"); }
    public boolean isCancelCommand(String message) { return normalize(message).equals("annulla"); }

    public String confirmPending() {
        PendingOperation operation = pending.getAndSet(null);
        if (operation == null) return "Non c'è alcuna operazione in attesa di conferma.";
        try {
            return "Operazione completata: " + operation.action().get();
        } catch (RuntimeException ex) {
            return "Operazione non eseguita: " + ex.getMessage();
        }
    }

    public String cancelPending() {
        PendingOperation operation = pending.getAndSet(null);
        return operation == null ? "Non c'è alcuna operazione da annullare."
            : "Operazione annullata: " + operation.summary();
    }

    public boolean hasPendingOperation() { return pending.get() != null; }

    private String propose(String summary, Supplier<String> action) {
        PendingOperation operation = new PendingOperation(summary, action);
        if (!pending.compareAndSet(null, operation)) {
            return "Esiste già un'operazione in attesa. Rispondi 'conferma' o 'annulla' prima di proporne un'altra.";
        }
        return "Operazione proposta: " + summary + ". Rispondi esattamente 'conferma' per eseguirla oppure 'annulla'.";
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ITALIAN);
    }

    private record PendingOperation(String summary, Supplier<String> action) {}
}
