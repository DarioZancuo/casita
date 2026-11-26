INSERT INTO immobili (id, codice, descrizione, indirizzo, citta, deleted_at) VALUES
    (1, 'AS-29', 'Bilocale centro', 'Via Aselli 29', 'Milano', NULL),
    (2, 'MI-01', 'Trilocale con balcone', 'Via Inama 17', 'Milano', NULL),
    (3, 'BO-01', 'Monolocale universitario', 'Via Lecchi 10', 'Milano', NULL);

INSERT INTO inquilini (id, nome, cognome, codice_fiscale, email, telefono, deleted_at) VALUES
    (1, 'Mario', 'Rossi', 'RSSMRA80A01H501U', 'mario.rossi@example.com', '+39 333 1111111', NULL),
    (2, 'Giulia', 'Bianchi', 'BNCGLI90B41F205X', 'giulia.bianchi@example.com', '+39 333 2222222', NULL),
    (3, 'Luca', 'Verdi', 'VRDLCU95C10A944K', 'luca.verdi@example.com', '+39 333 3333333', NULL);

INSERT INTO contratti
    (id, immobile_id, inquilino_id, data_inizio, data_fine, canone_mensile, deposito, note, deleted_at)
VALUES
    (1, 1, 1, DATEADD('DAY', -180, CURRENT_DATE), DATEADD('DAY', 60, CURRENT_DATE), 950.00, 1900.00, 'Contratto attivo in prossima scadenza', NULL),
    (2, 2, 2, DATEADD('DAY', -500, CURRENT_DATE), DATEADD('DAY', -30, CURRENT_DATE), 1200.00, 2400.00, 'Contratto storico scaduto', NULL),
    (3, 3, 3, DATEADD('DAY', 120, CURRENT_DATE), DATEADD('DAY', 485, CURRENT_DATE), 650.00, 1300.00, 'Contratto futuro', NULL);

INSERT INTO spese_condominiali
    (id, immobile_id, descrizione, categoria, importo, data_spesa, note, deleted_at)
VALUES
    (1, 1, 'Riparazione caldaia', 'MANUTENZIONE', 320.00, DATEADD('DAY', -15, CURRENT_DATE), 'Intervento tecnico', NULL),
    (2, 1, 'Quota condominiale trimestrale', 'CONDOMINIO', 450.00, DATEADD('DAY', -45, CURRENT_DATE), NULL, NULL),
    (3, 2, 'Assicurazione immobile', 'ASSICURAZIONE', 280.00, DATEADD('DAY', -80, CURRENT_DATE), NULL, NULL),
    (4, 3, 'Tassa rifiuti', 'TASSE', 190.00, DATEADD('DAY', -120, CURRENT_DATE), NULL, NULL);

ALTER TABLE immobili ALTER COLUMN id RESTART WITH 100;
ALTER TABLE inquilini ALTER COLUMN id RESTART WITH 100;
ALTER TABLE contratti ALTER COLUMN id RESTART WITH 100;
ALTER TABLE spese_condominiali ALTER COLUMN id RESTART WITH 100;
