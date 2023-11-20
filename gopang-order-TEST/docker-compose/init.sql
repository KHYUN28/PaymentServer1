create sequence orders_seq start with 1 increment by 50 nocache

create table order_history (
                               history_id bigint not null,
                               item_id bigint,
                               order_amount bigint,
                               order_id bigint,
                               user_id bigint,
                               order_state enum ('CANCEL','COMPLETE','DELIVERYCOMPLETE','DELIVERYREADY','ITEMREADY','PAYCANCEL','PAYCOMPLETE','SELLERCANCEL'),
                               primary key (history_id)
) engine=InnoDB

create table orders (
                        amount bigint,
                        cartitem_id bigint,
                        order_id bigint not null,
                        time datetime(6),
                        user_id bigint,
                        delete_yn varchar(255),
                        reciever_addr varchar(255),
                        reciever_name varchar(255),
                        reciever_phone varchar(255),
                        primary key (order_id)
) engine=InnoDB

alter table order_history
    add constraint UK_a8i5gvsqpx8mgopyw8xffx6hu unique (order_id)

alter table order_history
    add constraint FKnw2ljd8jnpdc9y2ild52e79t2
        foreign key (order_id)
            references orders (order_id)


-- CREATE DATABASE IF NOT EXISTS paydb;
-- USE paydb;

CREATE TABLE IF NOT EXISTS Cancel (
    merchant_uid VARCHAR(255) NULL,
    cancel_amount INT NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(255) NOT NULL,
    time DATETIME DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS Card (
    merchant_uid VARCHAR(255) NULL,
    card_number VARCHAR(255) NULL,
    expiry VARCHAR(7) NULL,  -- Assuming YYYY-MM format
    birth VARCHAR(6) NULL,
    pwd_2digit VARCHAR(2) NULL,
    cvc VARCHAR(3) NULL
    );
