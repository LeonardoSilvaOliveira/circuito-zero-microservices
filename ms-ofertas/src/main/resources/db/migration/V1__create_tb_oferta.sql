-- V1: estrutura da tabela de ofertas (excedentes alimentares)
CREATE TABLE tb_oferta (
    id                    BIGINT       NOT NULL AUTO_INCREMENT,
    comercio_nome         VARCHAR(100) NOT NULL,
    descricao             VARCHAR(255) NOT NULL,
    quantidade            INT          NOT NULL,
    endereco              VARCHAR(255) NOT NULL,
    data_limite_retirada  DATETIME(6)  NOT NULL,
    criado_em             DATETIME(6)  NOT NULL,
    status                VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id)
);
