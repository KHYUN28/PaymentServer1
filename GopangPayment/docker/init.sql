CREATE DATABASE IF NOT EXISTS paydb;
USE paydb;

CREATE TABLE IF NOT EXISTS Cancel (
    merchant_uid VARCHAR(255) NULL,
    cancel_amount INT NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(255) NOT NULL,
    time DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Card (
    merchant_uid VARCHAR(255) NULL,
    amount INT NOT NULL,
    card_number VARCHAR(255) NULL,
    expiry VARCHAR(7) NULL,
    birth VARCHAR(6) NULL,
    pwd_2digit VARCHAR(2) NULL,
    cvc VARCHAR(3) NULL,
    successStatus VARCHAR(10) NULL,
    time DATETIME DEFAULT CURRENT_TIMESTAMP
);
