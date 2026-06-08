-- ---------- Catalogo: roles ----------
INSERT INTO roles (id, name) VALUES (1, 'ADMIN')    ON CONFLICT (id) DO NOTHING;;
INSERT INTO roles (id, name) VALUES (2, 'CUSTOMER') ON CONFLICT (id) DO NOTHING;;
INSERT INTO roles (id, name) VALUES (3, 'MERCHANT') ON CONFLICT (id) DO NOTHING;;

SELECT setval(
    pg_get_serial_sequence('roles', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM roles)
);;

-- ---------- Catalogo: proveedores de servicios (billing) ----------
INSERT INTO service_providers (id, name, category, active, created_at) VALUES
  (1, 'Sedapal',     'UTILITIES', true, now()),
  (2, 'Luz del Sur', 'UTILITIES', true, now()),
  (3, 'Movistar',    'TELECOM',   true, now()),
  (4, 'Claro',       'TELECOM',   true, now()),
  (5, 'Win',         'INTERNET',  true, now())
ON CONFLICT (id) DO NOTHING;;

SELECT setval(
    pg_get_serial_sequence('service_providers', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM service_providers)
);;

-- ---------- Demo: usuarios (password de todos: "password123") ----------
INSERT INTO users (id, email, password, verified, role_id) VALUES
  (1, 'admin@pagoya.com', '$2a$10$7KpeW.POyhPPusuSoMafpuDIoUWqrbjpzD/3qsyoc6a17KVgUv/Si', true, 1),
  (2, 'ana@pagoya.com',   '$2a$10$7KpeW.POyhPPusuSoMafpuDIoUWqrbjpzD/3qsyoc6a17KVgUv/Si', true, 2),
  (3, 'luis@pagoya.com',  '$2a$10$7KpeW.POyhPPusuSoMafpuDIoUWqrbjpzD/3qsyoc6a17KVgUv/Si', true, 2)
ON CONFLICT (id) DO NOTHING;;

SELECT setval(pg_get_serial_sequence('users', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM users));;

-- ---------- Demo: clientes ----------
INSERT INTO customers (id, full_name, dni, phone, user_id, deleted) VALUES
  (1, 'Ana Torres',  '12345678', '999111222', 2, false),
  (2, 'Luis Quispe', '87654321', '999333444', 3, false)
ON CONFLICT (id) DO NOTHING;;

SELECT setval(pg_get_serial_sequence('customers', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM customers));;

-- ---------- Demo: cuentas con saldo ----------
INSERT INTO accounts (id, account_number, balance, status, type, customer_id) VALUES
  (1, 'ACC-ANA-001',  1000.00, 'ACTIVE', 'SAVINGS',  1),
  (2, 'ACC-LUIS-001',  500.00, 'ACTIVE', 'SAVINGS',  2)
ON CONFLICT (id) DO NOTHING;;

SELECT setval(pg_get_serial_sequence('accounts', 'id'),
    (SELECT COALESCE(MAX(id), 1) FROM accounts));;


-- Funciones de reporte

-- 1. Total transferido por moneda (solo COMPLETED)
CREATE OR REPLACE FUNCTION fn_transfer_report_by_currency()
RETURNS TABLE(currency VARCHAR, total_transfers BIGINT, total_amount NUMERIC) AS $$
BEGIN
    RETURN QUERY
    SELECT t.currency::VARCHAR, COUNT(*)::BIGINT, SUM(t.amount)::NUMERIC
    FROM transfers t
    WHERE t.status = 'COMPLETED'
    GROUP BY t.currency
    ORDER BY SUM(t.amount) DESC;
END;
$$ LANGUAGE plpgsql;;

-- 2. Transferencias por dia en rango
CREATE OR REPLACE FUNCTION fn_transfer_report_by_day(p_from DATE, p_to DATE)
RETURNS TABLE(day DATE, total_transfers BIGINT, total_amount NUMERIC) AS $$
BEGIN
    RETURN QUERY
    SELECT DATE(t.created_at), COUNT(*)::BIGINT, SUM(t.amount)::NUMERIC
    FROM transfers t
    WHERE DATE(t.created_at) BETWEEN p_from AND p_to
    GROUP BY DATE(t.created_at)
    ORDER BY DATE(t.created_at);
END;
$$ LANGUAGE plpgsql;;

-- 3. Distribucion por estado
CREATE OR REPLACE FUNCTION fn_transfer_report_by_status()
RETURNS TABLE(status VARCHAR, total BIGINT) AS $$
BEGIN
    RETURN QUERY
    SELECT t.status::VARCHAR, COUNT(*)::BIGINT
    FROM transfers t
    GROUP BY t.status;
END;
$$ LANGUAGE plpgsql;;

-- 4. Cuentas por tipo y estado
CREATE OR REPLACE FUNCTION fn_account_report_summary()
RETURNS TABLE(type VARCHAR, status VARCHAR, total BIGINT, total_balance NUMERIC) AS $$
BEGIN
    RETURN QUERY
    SELECT a.type::VARCHAR, a.status::VARCHAR,
           COUNT(*)::BIGINT, COALESCE(SUM(a.balance), 0)::NUMERIC
    FROM accounts a
    GROUP BY a.type, a.status
    ORDER BY a.type, a.status;
END;
$$ LANGUAGE plpgsql;;

-- 5. Pagos del cliente agrupados por categoria de proveedor (US-B04)
CREATE OR REPLACE FUNCTION sp_payments_by_category(p_customer_id BIGINT)
RETURNS TABLE(category VARCHAR, total_count BIGINT, total_amount NUMERIC) AS $$
BEGIN
    RETURN QUERY
    SELECT sp.category::VARCHAR,
           COUNT(bp.id)::BIGINT,
           COALESCE(SUM(bp.amount), 0)::NUMERIC
    FROM bill_payments bp
    JOIN service_providers sp ON sp.id = bp.provider_id
    WHERE bp.customer_id = p_customer_id
    GROUP BY sp.category
    ORDER BY total_amount DESC;
END;
$$ LANGUAGE plpgsql;;
