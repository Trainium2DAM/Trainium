-- ============================================
-- Script de configuración de bbddgimnasio
-- Ejecutar en MySQL/MariaDB
-- ============================================

CREATE TABLE maquinas (
    nombre TEXT NOT NULL,
    foto TEXT,
    id BIGINT NOT NULL,
    estado SMALLINT NOT NULL,
    operativa BOOLEAN NOT NULL,
    mantenimiento_desde TIMESTAMP WITHOUT TIME ZONE,
    mantenimiento_hasta TIMESTAMP WITHOUT TIME ZONE,
    descripcion TEXT,
    tipo TEXT
);

CREATE TABLE pagos (
    id_usuario BIGINT,
    id BIGINT NOT NULL,
    monto DOUBLE PRECISION,
    fecha_pago DATE,
    metodo_pago TEXT,
    tipo TEXT
);

CREATE TABLE peso_usuario (
    fecha DATE,
    id_usuario BIGINT,
    peso DOUBLE PRECISION NOT NULL,
    id BIGINT NOT NULL
);

CREATE TABLE platos (
    fecha_subida DATE,
    visibilidad BOOLEAN,
    aceptado BOOLEAN,
    id_usuario BIGINT,
    id BIGINT NOT NULL,
    imagen_url TEXT,
    tiempo TEXT,
    descripcion TEXT,
    nombre TEXT NOT NULL,
    calorias DOUBLE PRECISION
);

CREATE TABLE recomendacion_diaria (
    id BIGINT NOT NULL,
    id_plato_fk BIGINT,
    fecha DATE
);

CREATE TABLE reservas (
    id BIGINT NOT NULL,
    estado BOOLEAN,
    hora_fin TIME WITHOUT TIME ZONE,
    hora_inicio TIME WITHOUT TIME ZONE,
    fecha DATE,
    id_maquina BIGINT,
    id_usuario BIGINT
);

CREATE TABLE usuarios (
    dni TEXT NOT NULL,
    email TEXT,
    foto TEXT,
    contraseniaHash TEXT NOT NULL,
    telefono TEXT,
    fecha_fin_prem DATE,
    fecha_reg DATE,
    admin INTEGER NOT NULL,
    fecha_ini_prem DATE,
    premium BOOLEAN,
    nombre TEXT NOT NULL,
    id BIGINT NOT NULL
);