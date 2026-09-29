    stock INTEGER NOT NULL DEFAULT 0 CHECK(stock>=0),
    codigo_barras VARCHAR(100),
    id_categoria INTEGER REFERENCES categorias(id),
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE clientes(
    id SERIAL PRIMARY KEY,
    cedula VARCHAR(10) NOT NULL UNIQUE,
    nombre VARCHAR(150) NOT NULL,
    telefono VARCHAR(10) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    segundo_nombre VARCHAR(100),
    apellido VARCHAR(100) NOT NULL,
    segundo_apellido VARCHAR(100),
    direccion VARCHAR(255) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    CONSTRAINT clientes_cedula_check CHECK(cedula ~ '^[0-9]{10}$'),
    CONSTRAINT clientes_telefono_check CHECK(telefono ~ '^[0-9]{10}$'),
    CONSTRAINT clientes_email_check CHECK(email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

CREATE TABLE configuracion(
    clave VARCHAR(50) PRIMARY KEY,
    valor VARCHAR(255) NOT NULL,
    descripcion VARCHAR(255)
);

CREATE SEQUENCE seq_numero_factura START WITH 1;

CREATE FUNCTION siguiente_numero_factura() RETURNS VARCHAR
LANGUAGE plpgsql AS $$
BEGIN
    RETURN 'FAC-' || LPAD(nextval('seq_numero_factura')::TEXT,6,'0');
END;
$$;

CREATE TABLE ventas(
    id SERIAL PRIMARY KEY,
    numero_factura VARCHAR(20) NOT NULL UNIQUE,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    cliente VARCHAR(150) DEFAULT 'Consumidor Final',
    id_usuario INTEGER NOT NULL REFERENCES usuarios(id),
    subtotal NUMERIC(10,2) NOT NULL DEFAULT 0,
    porcentaje_iva NUMERIC(5,2) NOT NULL DEFAULT 0,
    iva NUMERIC(10,2) NOT NULL DEFAULT 0,
    total NUMERIC(10,2) NOT NULL DEFAULT 0,
    anulada BOOLEAN NOT NULL DEFAULT FALSE,
    id_cliente INTEGER REFERENCES clientes(id)
);

CREATE TABLE detalle_ventas(
    id SERIAL PRIMARY KEY,
    id_venta INTEGER NOT NULL REFERENCES ventas(id) ON DELETE CASCADE,
    id_producto INTEGER NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL CHECK(cantidad>0),
    precio_unitario NUMERIC(10,2) NOT NULL CHECK(precio_unitario>=0),
    subtotal_item NUMERIC(10,2) NOT NULL
);

CREATE INDEX idx_clientes_cedula ON clientes(cedula);
CREATE INDEX idx_productos_barras ON productos(codigo_barras);
CREATE INDEX idx_productos_codigo ON productos(codigo);
CREATE INDEX idx_productos_nombre ON productos(LOWER(nombre));
CREATE INDEX idx_ventas_fecha ON ventas(fecha);
CREATE INDEX idx_ventas_usuario ON ventas(id_usuario);
CREATE INDEX idx_detalle_venta ON detalle_ventas(id_venta);

CREATE VIEW v_stock_bajo AS
SELECT id,codigo,nombre,stock,
 (SELECT valor::INTEGER FROM configuracion WHERE clave='STOCK_MINIMO') AS stock_minimo
FROM productos
WHERE activo=TRUE
AND stock<=(SELECT valor::INTEGER FROM configuracion WHERE clave='STOCK_MINIMO')
ORDER BY stock;

CREATE VIEW v_ventas_detalle AS
SELECT v.id AS id_venta,v.numero_factura,v.fecha,v.cliente,v.id_cliente,
 c.cedula AS cedula_cliente,c.nombre AS nombre_cliente,c.segundo_nombre,c.apellido,
 c.segundo_apellido,c.direccion,c.telefono AS telefono_cliente,c.email,
 u.nombre AS cajero,p.codigo AS codigo_producto,p.nombre AS producto,p.codigo_barras,
 dv.cantidad,dv.precio_unitario,dv.subtotal_item,v.subtotal,v.porcentaje_iva,
 v.iva,v.total,v.anulada
FROM ventas v
JOIN usuarios u ON u.id=v.id_usuario
JOIN detalle_ventas dv ON dv.id_venta=v.id
JOIN productos p ON p.id=dv.id_producto
LEFT JOIN clientes c ON c.id=v.id_cliente;

INSERT INTO roles(nombre) VALUES('ADMIN'),('CAJERO');

INSERT INTO configuracion(clave,valor,descripcion) VALUES
('IVA','15','Porcentaje de IVA aplicado en facturas'),
('STOCK_MINIMO','5','Stock mínimo para alerta de reabastecimiento'),
('EMPRESA_NOMBRE','Comercial Demo','Nombre de la empresa en facturas'),
('EMPRESA_RUC','1799999999001','RUC de demostración'),
('EMPRESA_DIR','Quito','Dirección de demostración');

INSERT INTO categorias(nombre) VALUES('Tecnología'),('Papelería'),('Alimentos');

INSERT INTO productos(codigo,nombre,precio,stock,codigo_barras,id_categoria) VALUES
('P001','Mouse inalámbrico',18.50,25,'786100000101',1),
('P002','Teclado USB',29.90,15,'786100000102',1),
('P003','Cuaderno universitario',2.75,40,'786100000103',2),
('P004','Botella de agua',0.75,30,'786100000104',3);

INSERT INTO clientes(cedula,nombre,segundo_nombre,apellido,segundo_apellido,direccion,telefono,email) VALUES
('1712345678','Daniel','Andrés','Mora','Vega','Quito','0987654321','daniel.demo@example.com'),
('1801234567','María','Elena','Torres','Paz','Guayaqui;','0991234567','maria.demo@example.com');

-- Credenciales de demostración:
-- admin / Admin123
-- cajero / Caja123
INSERT INTO usuarios(nombre,apellido,username,password,id_rol) VALUES
('Administrador','Demo','admin','3b612c75a7b5048a435fb6ec81e52ff92d6d795a8b5a9c17070f6a63c97a53b2',1),
('Cajero','Demo','cajero','3484c59cc17b5bcdf776ab8ac6eacb4cb9fcf548a322f7ab8dca89263d4e94ca',2);

COMMIT;

