-- V2: carga inicial de resgates
-- Coerente com a carga do ms-ofertas: oferta 3 = RESERVADA, oferta 4 = RETIRADA,
-- oferta 1 voltou a DISPONIVEL porque o resgate dela foi CANCELADO.
INSERT INTO tb_resgate (oferta_id, instituicao_nome, responsavel_nome, telefone, data_reserva, data_retirada, status) VALUES
(3, 'ONG Mãos Solidárias',             'Maria Souza',   '11987654321', '2026-11-17 18:05:00', NULL,                  'RESERVADO'),
(4, 'Cozinha Solidária da Bela Vista', 'João Pereira',  '11912345678', '2026-11-17 16:10:00', '2026-11-17 16:40:00', 'RETIRADO'),
(1, 'Associação Comunitária Bixiga',   'Ana Lima',      '1133224455',  '2026-11-17 18:20:00', NULL,                  'CANCELADO');
