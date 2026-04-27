CREATE TABLE order_service.couriers
(
    code   VARCHAR(20)  NOT NULL,
    name   VARCHAR(50)  NOT NULL,
    active BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_couriers PRIMARY KEY (code)
);

INSERT INTO order_service.couriers (code, name, active) VALUES
    ('01', '우체국택배', TRUE),
    ('04', 'CJ대한통운', TRUE),
    ('05', '한진택배', TRUE),
    ('06', '로젠택배', TRUE),
    ('08', '롯데택배', TRUE);
