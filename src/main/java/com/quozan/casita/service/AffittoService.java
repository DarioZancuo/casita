package com.quozan.casita.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quozan.casita.model.Contratto;
import com.quozan.casita.model.Immobile;
import com.quozan.casita.model.Inquilino;
import com.quozan.casita.repository.ContrattoRepository;
import com.quozan.casita.repository.ImmobileRepository;
import com.quozan.casita.repository.InquilinoRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AffittoService {
    private final ImmobileRepository immobileRepository;
    private final InquilinoRepository inquilinoRepository;
    private final ContrattoRepository contrattoRepository;
    private final Clock clock;

    public List<ImmobileView> immobili() {
        return immobileRepository.findByDeletedAtIsNullOrderByCodiceAsc().stream().map(this::toView).toList();
    }

    public ImmobileView immobile(Long id) { return toView(requireImmobile(id)); }

    public List<InquilinoView> inquilini() {
        return inquilinoRepository.findByDeletedAtIsNullOrderByCognomeAscNomeAsc().stream().map(this::toView).toList();
    }

    public InquilinoView inquilino(Long id) { return toView(requireInquilino(id)); }

    public List<ContrattoView> contratti() {
        return contrattoRepository.findByDeletedAtIsNullOrderByDataFineAsc().stream().map(this::toView).toList();
    }

    public ContrattoView contratto(Long id) { return toView(requireContratto(id)); }

    public List<ContrattoView> contrattiInScadenza(int giorni) {
        if (giorni < 0 || giorni > 3650) throw new IllegalArgumentException("I giorni devono essere compresi tra 0 e 3650");
        LocalDate oggi = LocalDate.now(clock);
        return contrattoRepository.findInScadenza(oggi, oggi.plusDays(giorni)).stream().map(this::toView).toList();
    }

    public void validaNuovoInquilino(String nome, String cognome, String codiceFiscale) {
        requireText(nome, "nome"); requireText(cognome, "cognome"); requireText(codiceFiscale, "codice fiscale");
        if (inquilinoRepository.existsByCodiceFiscaleIgnoreCaseAndDeletedAtIsNull(codiceFiscale.trim()))
            throw new IllegalStateException("Esiste già un inquilino con questo codice fiscale");
    }

    @Transactional
    public InquilinoView creaInquilino(String nome, String cognome, String codiceFiscale, String email, String telefono) {
        validaNuovoInquilino(nome, cognome, codiceFiscale);
        Inquilino value = new Inquilino();
        setInquilino(value, nome, cognome, codiceFiscale, email, telefono);
        return toView(inquilinoRepository.save(value));
    }

    public void validaAggiornamentoInquilino(Long id, String nome, String cognome, String codiceFiscale) {
        Inquilino current = requireInquilino(id);
        requireText(nome, "nome"); requireText(cognome, "cognome"); requireText(codiceFiscale, "codice fiscale");
        inquilinoRepository.findByCodiceFiscaleIgnoreCaseAndDeletedAtIsNull(codiceFiscale.trim())
            .filter(found -> !found.getId().equals(current.getId()))
            .ifPresent(found -> { throw new IllegalStateException("Esiste già un inquilino con questo codice fiscale"); });
    }

    @Transactional
    public InquilinoView aggiornaInquilino(Long id, String nome, String cognome, String codiceFiscale, String email, String telefono) {
        validaAggiornamentoInquilino(id, nome, cognome, codiceFiscale);
        Inquilino value = requireInquilino(id);
        setInquilino(value, nome, cognome, codiceFiscale, email, telefono);
        return toView(value);
    }

    public void validaEliminazioneInquilino(Long id) {
        requireInquilino(id);
        if (contrattoRepository.countAttiviOFuturiPerInquilino(id, LocalDate.now(clock)) > 0)
            throw new IllegalStateException("L'inquilino ha contratti attivi o futuri");
    }

    @Transactional
    public String eliminaInquilino(Long id) {
        validaEliminazioneInquilino(id);
        requireInquilino(id).setDeletedAt(LocalDateTime.now(clock));
        return "Inquilino " + id + " disattivato";
    }

    public void validaContratto(Long contrattoId, Long immobileId, Long inquilinoId, LocalDate inizio, LocalDate fine,
                                BigDecimal canone, BigDecimal deposito) {
        if (contrattoId != null) requireContratto(contrattoId);
        requireImmobile(immobileId); requireInquilino(inquilinoId);
        if (inizio == null || fine == null || fine.isBefore(inizio)) throw new IllegalArgumentException("Intervallo del contratto non valido");
        requirePositive(canone, "canone mensile");
        if (deposito == null || deposito.signum() < 0) throw new IllegalArgumentException("Il deposito non può essere negativo");
        if (contrattoRepository.countSovrapposti(immobileId, contrattoId, inizio, fine) > 0)
            throw new IllegalStateException("Il contratto si sovrappone a un altro contratto dell'immobile");
    }

    @Transactional
    public ContrattoView creaContratto(Long immobileId, Long inquilinoId, LocalDate inizio, LocalDate fine,
                                       BigDecimal canone, BigDecimal deposito, String note) {
        validaContratto(null, immobileId, inquilinoId, inizio, fine, canone, deposito);
        Contratto value = new Contratto();
        setContratto(value, immobileId, inquilinoId, inizio, fine, canone, deposito, note);
        return toView(contrattoRepository.save(value));
    }

    @Transactional
    public ContrattoView aggiornaContratto(Long id, Long immobileId, Long inquilinoId, LocalDate inizio, LocalDate fine,
                                           BigDecimal canone, BigDecimal deposito, String note) {
        validaContratto(id, immobileId, inquilinoId, inizio, fine, canone, deposito);
        Contratto value = requireContratto(id);
        setContratto(value, immobileId, inquilinoId, inizio, fine, canone, deposito, note);
        return toView(value);
    }

    @Transactional
    public String eliminaContratto(Long id) {
        requireContratto(id).setDeletedAt(LocalDateTime.now(clock));
        return "Contratto " + id + " disattivato";
    }

    private Immobile requireImmobile(Long id) {
        if (id == null) throw new IllegalArgumentException("L'id dell'immobile è obbligatorio");
        return immobileRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new EntityNotFoundException("Immobile non trovato: " + id));
    }

    private Inquilino requireInquilino(Long id) {
        if (id == null) throw new IllegalArgumentException("L'id dell'inquilino è obbligatorio");
        return inquilinoRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new EntityNotFoundException("Inquilino non trovato: " + id));
    }

    private Contratto requireContratto(Long id) {
        if (id == null) throw new IllegalArgumentException("L'id del contratto è obbligatorio");
        return contrattoRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new EntityNotFoundException("Contratto non trovato: " + id));
    }

    private static void setInquilino(Inquilino value, String nome, String cognome, String codiceFiscale,
                                     String email, String telefono) {
        value.setNome(nome.trim());
        value.setCognome(cognome.trim());
        value.setCodiceFiscale(codiceFiscale.trim().toUpperCase());
        value.setEmail(clean(email));
        value.setTelefono(clean(telefono));
    }

    private void setContratto(Contratto value, Long immobileId, Long inquilinoId, LocalDate inizio, LocalDate fine,
                              BigDecimal canone, BigDecimal deposito, String note) {
        value.setImmobile(requireImmobile(immobileId));
        value.setInquilino(requireInquilino(inquilinoId));
        value.setDataInizio(inizio);
        value.setDataFine(fine);
        value.setCanoneMensile(canone);
        value.setDeposito(deposito);
        value.setNote(clean(note));
    }

    private ImmobileView toView(Immobile x) { return new ImmobileView(x.getId(), x.getCodice(), x.getDescrizione(), x.getIndirizzo(), x.getCitta()); }
    private InquilinoView toView(Inquilino x) { return new InquilinoView(x.getId(), x.getNome(), x.getCognome(), x.getCodiceFiscale(), x.getEmail(), x.getTelefono()); }
    private ContrattoView toView(Contratto x) {
        return new ContrattoView(x.getId(), x.getImmobile().getId(), x.getImmobile().getCodice(), x.getInquilino().getId(),
            x.getInquilino().getNome() + " " + x.getInquilino().getCognome(), x.getDataInizio(), x.getDataFine(),
            x.getCanoneMensile(), x.getDeposito(), stato(x), x.getNote());
    }

    private String stato(Contratto x) {
        LocalDate oggi = LocalDate.now(clock);
        if (oggi.isBefore(x.getDataInizio())) return "FUTURO";
        if (oggi.isAfter(x.getDataFine())) return "SCADUTO";
        return "ATTIVO";
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Il campo " + field + " è obbligatorio");
    }
    private static void requirePositive(BigDecimal value, String field) {
        if (value == null || value.signum() <= 0) throw new IllegalArgumentException("Il campo " + field + " deve essere positivo");
    }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record ImmobileView(Long id, String codice, String descrizione, String indirizzo, String citta) {}
    public record InquilinoView(Long id, String nome, String cognome, String codiceFiscale, String email, String telefono) {}
    public record ContrattoView(Long id, Long immobileId, String codiceImmobile, Long inquilinoId, String inquilino,
                               LocalDate dataInizio, LocalDate dataFine, BigDecimal canoneMensile,
                               BigDecimal deposito, String stato, String note) {}
}
