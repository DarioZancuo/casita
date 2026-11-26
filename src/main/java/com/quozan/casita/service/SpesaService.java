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

import com.quozan.casita.model.Immobile;
import com.quozan.casita.model.SpesaCondominiale;
import com.quozan.casita.repository.ImmobileRepository;
import com.quozan.casita.repository.SpesaRepository;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SpesaService {
    private final SpesaRepository spesaRepository;
    private final ImmobileRepository immobileRepository;
    private final Clock clock;

    public List<SpesaView> spese(LocalDate dal, LocalDate al, Long immobileId, String categoria) {
        LocalDate from = dal == null ? LocalDate.of(2000, 1, 1) : dal;
        LocalDate to = al == null ? LocalDate.of(9999, 12, 31) : al;
        if (to.isBefore(from)) throw new IllegalArgumentException("L'intervallo delle spese non è valido");
        return spesaRepository.findByDeletedAtIsNullAndDataSpesaBetweenOrderByDataSpesaDesc(from, to).stream()
            .filter(x -> immobileId == null || x.getImmobile().getId().equals(immobileId))
            .filter(x -> categoria == null || categoria.isBlank() || x.getCategoria().equalsIgnoreCase(categoria.trim()))
            .map(this::toView).toList();
    }

    public SpesaView spesa(Long id) { return toView(requireSpesa(id)); }

    public List<SpesaView> speseInArrivo(int giorni) {
        if (giorni < 0 || giorni > 3650) {
            throw new IllegalArgumentException("I giorni devono essere compresi tra 0 e 3650");
        }
        LocalDate oggi = LocalDate.now(clock);
        return spese(oggi, oggi.plusDays(giorni), null, null);
    }

    public RiepilogoSpese totalePerImmobile(Long immobileId) {
        requireImmobile(immobileId);
        return riepilogo(null, null, immobileId, null);
    }

    public RiepilogoSpese riepilogo(LocalDate dal, LocalDate al, Long immobileId, String categoria) {
        List<SpesaView> values = spese(dal, al, immobileId, categoria);
        BigDecimal totale = values.stream().map(SpesaView::importo).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new RiepilogoSpese(values.size(), totale, dal, al, immobileId, categoria);
    }

    public void validaSpesa(Long spesaId, Long immobileId, String descrizione, String categoria,
                            BigDecimal importo, LocalDate dataSpesa) {
        if (spesaId != null) requireSpesa(spesaId);
        requireImmobile(immobileId);
        requireText(descrizione, "descrizione"); requireText(categoria, "categoria");
        if (importo == null || importo.signum() <= 0) throw new IllegalArgumentException("L'importo deve essere positivo");
        if (dataSpesa == null) throw new IllegalArgumentException("La data della spesa è obbligatoria");
    }

    @Transactional
    public SpesaView creaSpesa(Long immobileId, String descrizione, String categoria, BigDecimal importo,
                               LocalDate dataSpesa, String note) {
        validaSpesa(null, immobileId, descrizione, categoria, importo, dataSpesa);
        SpesaCondominiale value = new SpesaCondominiale();
        setSpesa(value, immobileId, descrizione, categoria, importo, dataSpesa, note);
        return toView(spesaRepository.save(value));
    }

    @Transactional
    public SpesaView aggiornaSpesa(Long id, Long immobileId, String descrizione, String categoria,
                                   BigDecimal importo, LocalDate dataSpesa, String note) {
        validaSpesa(id, immobileId, descrizione, categoria, importo, dataSpesa);
        SpesaCondominiale value = requireSpesa(id);
        setSpesa(value, immobileId, descrizione, categoria, importo, dataSpesa, note);
        return toView(value);
    }

    @Transactional
    public String eliminaSpesa(Long id) {
        requireSpesa(id).setDeletedAt(LocalDateTime.now(clock));
        return "Spesa " + id + " disattivata";
    }

    private SpesaCondominiale requireSpesa(Long id) {
        if (id == null) throw new IllegalArgumentException("L'id della spesa è obbligatorio");
        return spesaRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new EntityNotFoundException("Spesa non trovata: " + id));
    }

    private Immobile requireImmobile(Long id) {
        if (id == null) throw new IllegalArgumentException("L'id dell'immobile è obbligatorio");
        return immobileRepository.findByIdAndDeletedAtIsNull(id).orElseThrow(() -> new EntityNotFoundException("Immobile non trovato: " + id));
    }

    private void setSpesa(SpesaCondominiale value, Long immobileId, String descrizione, String categoria,
                          BigDecimal importo, LocalDate dataSpesa, String note) {
        value.setImmobile(requireImmobile(immobileId));
        value.setDescrizione(descrizione.trim());
        value.setCategoria(categoria.trim().toUpperCase());
        value.setImporto(importo);
        value.setDataSpesa(dataSpesa);
        value.setNote(clean(note));
    }

    private SpesaView toView(SpesaCondominiale x) {
        return new SpesaView(x.getId(), x.getImmobile().getId(), x.getImmobile().getCodice(), x.getDescrizione(),
            x.getCategoria(), x.getImporto(), x.getDataSpesa(), x.getNote());
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("Il campo " + field + " è obbligatorio");
    }
    private static String clean(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record SpesaView(Long id, Long immobileId, String codiceImmobile, String descrizione, String categoria,
                            BigDecimal importo, LocalDate dataSpesa, String note) {}
    public record RiepilogoSpese(int numeroSpese, BigDecimal totale, LocalDate dal, LocalDate al,
                                 Long immobileId, String categoria) {}
}
