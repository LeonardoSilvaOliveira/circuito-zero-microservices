-- V2: carga inicial de ofertas
-- As ofertas 3 (RESERVADA) e 4 (RETIRADA) são coerentes com os resgates
-- cadastrados na carga inicial do ms-resgates.
INSERT INTO tb_oferta (comercio_nome, descricao, quantidade, endereco, data_limite_retirada, criado_em, status) VALUES
('Padaria Pão Quente',        '20 pães franceses e 10 sonhos',           30, 'Rua Augusta, 1500 - Consolação, São Paulo/SP',       '2027-12-31 22:00:00', '2026-11-17 18:00:00', 'DISPONIVEL'),
('Restaurante Sabor Caseiro', '5 marmitas de arroz, feijão e frango',     5, 'Rua da Consolação, 2200 - Consolação, São Paulo/SP', '2027-12-31 23:00:00', '2026-11-17 18:30:00', 'DISPONIVEL'),
('Mercadinho Bom Preço',      '12 kg de frutas e legumes',               12, 'Rua Frei Caneca, 800 - Bela Vista, São Paulo/SP',     '2027-12-31 21:00:00', '2026-11-17 17:45:00', 'RESERVADA'),
('Lanchonete da Esquina',     '15 sanduíches naturais',                  15, 'Av. Paulista, 1100 - Bela Vista, São Paulo/SP',       '2027-12-31 20:00:00', '2026-11-17 16:00:00', 'RETIRADA'),
('Confeitaria Doce Vida',     '2 bolos inteiros e 20 fatias de torta',   22, 'Rua Haddock Lobo, 400 - Cerqueira César, São Paulo/SP','2027-12-31 22:30:00', '2026-11-17 19:00:00', 'DISPONIVEL');
