-- ============================================
-- Script de configuración de bbddgimnasio
-- Ejecutar en MySQL/MariaDB
-- ============================================

-- Añadir columnas que faltan a usuario
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS EMAIL VARCHAR(255) DEFAULT '';
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS TELEFONO VARCHAR(20) DEFAULT '';
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS ADMIN INT DEFAULT 0;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS PREMIUM INT DEFAULT 0;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS FECHA_INI_PREM DATE DEFAULT NULL;
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS FECHA_FIN_PREM DATE DEFAULT NULL;

-- Crear tabla plato_del_dia
CREATE TABLE IF NOT EXISTS plato_del_dia (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NOMBRE VARCHAR(255) NOT NULL,
    CALORIAS VARCHAR(50),
    PROTEINAS VARCHAR(50),
    CARBOHIDRATOS VARCHAR(50),
    GRASAS VARCHAR(50),
    AUTOR VARCHAR(255)
);

-- Crear tabla registro_peso
CREATE TABLE IF NOT EXISTS registro_peso (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_USUARIO INT NOT NULL,
    PESO DOUBLE NOT NULL,
    FECHA DATE NOT NULL,
    UNIQUE KEY uq_usuario_fecha (ID_USUARIO, FECHA)
);

-- Crear tabla pagos
CREATE TABLE IF NOT EXISTS pagos (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    ID_USUARIO INT NOT NULL,
    CONCEPTO VARCHAR(255),
    MONTO DOUBLE,
    FECHA DATE
);

-- Insertar platos de ejemplo
INSERT IGNORE INTO plato_del_dia (NOMBRE, CALORIAS, PROTEINAS, CARBOHIDRATOS, GRASAS, AUTOR) VALUES
('Pechuga de pollo con arroz integral', '450', '35', '55', '8', 'Chef TrainiumGym'),
('Ensalada César con pollo', '380', '28', '20', '18', 'Chef TrainiumGym'),
('Salmón a la plancha con verduras', '520', '40', '15', '30', 'Chef TrainiumGym'),
('Tortilla de espinacas con avena', '350', '22', '40', '12', 'Chef TrainiumGym'),
('Bowl de quinoa con atún', '480', '38', '45', '14', 'Chef TrainiumGym'),
('Pasta integral con pavo y champiñones', '410', '30', '50', '10', 'Chef TrainiumGym'),
('Wrap de pollo con aguacate', '390', '25', '35', '16', 'Chef TrainiumGym');
