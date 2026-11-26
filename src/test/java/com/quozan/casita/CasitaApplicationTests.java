package com.quozan.casita;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.quozan.casita.service.AffittoService;
import com.quozan.casita.service.SpesaService;

@SpringBootTest(properties = "spring.ai.openai.api-key=test-key")
@Transactional
class CasitaApplicationTests {
    @Autowired AffittoService affittoService;
    @Autowired SpesaService spesaService;

    @Test
    void caricaIlDatasetDemo() {
        assertThat(affittoService.immobili()).hasSize(3);
        assertThat(affittoService.inquilini()).hasSize(3);
        assertThat(affittoService.contratti()).hasSize(3);
        assertThat(spesaService.spese(null, null, null, null)).hasSize(4);
    }

    @Test
    void individuaIlContrattoInScadenza() {
        assertThat(affittoService.contrattiInScadenza(90))
            .extracting(AffittoService.ContrattoView::stato)
            .containsExactly("ATTIVO");
    }

    @Test
    void impedisceContrattiSovrapposti() {
        var active = affittoService.contrattiInScadenza(90).getFirst();
        assertThatThrownBy(() -> affittoService.creaContratto(
            active.immobileId(), active.inquilinoId(), active.dataInizio().plusDays(2), active.dataFine().minusDays(2),
            new BigDecimal("900"), new BigDecimal("1800"), "Sovrapposto"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("sovrappone");
    }

    @Test
    void aggregaLeSpeseDelPeriodo() {
        var result = spesaService.riepilogo(LocalDate.now().minusDays(60), LocalDate.now(), null, null);
        assertThat(result.numeroSpese()).isEqualTo(2);
        assertThat(result.totale()).isEqualByComparingTo("770.00");
    }

    @Test
    void trovaLeSpeseInArrivo() {
        spesaService.creaSpesa(1L, "Intervento futuro", "MANUTENZIONE", new BigDecimal("100.00"),
            LocalDate.now().plusDays(10), null);

        assertThat(spesaService.speseInArrivo(15))
            .extracting(SpesaService.SpesaView::descrizione)
            .containsExactly("Intervento futuro");
    }

    @Test
    void calcolaIlTotaleSpesePerImmobile() {
        var result = spesaService.totalePerImmobile(1L);

        assertThat(result.numeroSpese()).isEqualTo(2);
        assertThat(result.totale()).isEqualByComparingTo("770.00");
    }

    @Test
    void applicaLaCancellazioneLogica() {
        Long id = spesaService.spese(null, null, null, null).getFirst().id();
        spesaService.eliminaSpesa(id);
        assertThat(spesaService.spese(null, null, null, null)).hasSize(3);
    }
}
