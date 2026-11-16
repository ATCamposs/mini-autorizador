CREATE TABLE IF NOT EXISTS cartao (
    numero     VARCHAR(19)   NOT NULL PRIMARY KEY,
    senha_hash VARCHAR(60)   NOT NULL,
    saldo      DECIMAL(19,2) NOT NULL
);
