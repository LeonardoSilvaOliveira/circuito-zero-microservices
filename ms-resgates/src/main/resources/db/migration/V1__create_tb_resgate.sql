-- V1: estrutura da tabela de resgates
-- oferta_id referencia uma oferta do ms-ofertas, que tem banco próprio:
-- por isso NÃO existe chave estrangeira entre os bancos.
CREATE TABLE tb_resgate (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    oferta_id         BIGINT       NOT NULL,
    instituicao_nome  VARCHAR(120) NOT NULL,
    responsavel_nome  VARCHAR(100) NOT NULL,
    telefone          VARCHAR(11)  NOT NULL,
    data_reserva      DATETIME(6)  NOT NULL,
    data_retirada     DATETIME(6)  NULL,
    status            VARCHAR(25)  NOT NULL,
    PRIMARY KEY (id)
);
