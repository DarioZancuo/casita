package com.quozan.casita.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.quozan.casita.service.AffittoService;
import com.quozan.casita.service.SpesaService;

@ExtendWith(MockitoExtension.class)
class AffittoToolsTest {
    @Mock AffittoService affittoService;
    @Mock SpesaService spesaService;
    private AffittoTools tools;

    @BeforeEach
    void setUp() {
        tools = new AffittoTools(affittoService, spesaService);
    }

    @Test
    void nonScrivePrimaDellaConferma() {
        when(affittoService.creaInquilino("Mario", "Rossi", "RSSMRA80A01H501U", null, null))
            .thenReturn(new AffittoService.InquilinoView(99L, "Mario", "Rossi", "RSSMRA80A01H501U", null, null));

        String proposal = tools.proponiCreaInquilino("Mario", "Rossi", "RSSMRA80A01H501U", null, null);

        assertThat(proposal).contains("conferma");
        verify(affittoService, never()).creaInquilino("Mario", "Rossi", "RSSMRA80A01H501U", null, null);

        String result = tools.confirmPending();
        assertThat(result).contains("Operazione completata");
        verify(affittoService).creaInquilino("Mario", "Rossi", "RSSMRA80A01H501U", null, null);
    }

    @Test
    void annullaSenzaEseguire() {
        tools.proponiEliminaContratto(10L);
        String result = tools.cancelPending();
        assertThat(result).contains("annullata");
        verify(affittoService, never()).eliminaContratto(10L);
    }

    @Test
    void accettaUnaSolaOperazionePendente() {
        tools.proponiEliminaContratto(10L);
        String second = tools.proponiEliminaContratto(11L);
        assertThat(second).contains("già un'operazione");
    }
}
