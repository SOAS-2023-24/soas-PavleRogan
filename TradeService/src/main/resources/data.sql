-- BTC to USD and EUR
INSERT INTO trade_service (id, currency_from, to_currency, rate) VALUES
(1, 'BTC', 'USD', 30000.00),  -- Example rate for BTC to USD
(2, 'BTC', 'EUR', 27000.00);  -- Example rate for BTC to EUR

-- ETH to USD and EUR
INSERT INTO trade_service (id, currency_from, to_currency, rate) VALUES
(3, 'ETH', 'USD', 2000.00),   -- Example rate for ETH to USD
(4, 'ETH', 'EUR', 1800.00);   -- Example rate for ETH to EUR

-- SOL to USD and EUR
INSERT INTO trade_service (id, currency_from, to_currency, rate) VALUES
(5, 'SOL', 'USD', 50.00),     -- Example rate for SOL to USD
(6, 'SOL', 'EUR', 45.00);     -- Example rate for SOL to EUR

-- USD to BTC, ETH, SOL
INSERT INTO trade_service (id, currency_from, to_currency, rate) VALUES
(7, 'USD', 'BTC', 1 / 30.00), -- Example rate for USD to BTC
(8, 'USD', 'ETH', 1 / 20.00),  -- Example rate for USD to ETH
(9, 'USD', 'SOL', 1 / 50.00);    -- Example rate for USD to SOL

-- EUR to BTC, ETH, SOL
INSERT INTO trade_service (id, currency_from, to_currency, rate) VALUES
(10, 'EUR', 'BTC', 1/ 270.00), -- Example rate for EUR to BTC
(11, 'EUR', 'ETH', 1 / 180.00),  -- Example rate for EUR to ETH
(12, 'EUR', 'SOL', 1 / 45.00);     -- Example rate for EUR to SOL
