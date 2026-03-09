-- Crear base de datos con codificación UTF-8
CREATE DATABASE IF NOT EXISTS url_shortener CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE url_shortener;

-- Tabla de URLs
CREATE TABLE IF NOT EXISTS urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY, -- ID único para cada URL auto incremento
    codigo_corto VARCHAR(10) NOT NULL UNIQUE, -- Extracción de codigo corto del hash
    hash_completo VARCHAR(64) UNIQUE, -- Hash completo de la URL de usuarios anónimos, únicos y tamaño de 64 para SHA-256
    url_original TEXT NOT NULL, -- URL original, tamaño TEXT para soportar URLs largas
    tipo ENUM('ANONYMOUS', 'REGISTERED') NOT NULL, -- Tipo de usuario que creo la URL
    estado ENUM( -- Estado de la URL
        'ACTIVE',
        'EXPIRED',
        'DELETED'
    ) NOT NULL DEFAULT 'ACTIVE', -- Estado inicial por defecto es ACTIVE
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Fecha de creacion con valor por defecto de la fecha actual
    fecha_ultima_activacion TIMESTAMP NULL, -- Fecha de la ultima activacion de la URL, para usuarios Anónimos
    fecha_expiracion TIMESTAMP NULL, -- Fecha de expiracion, para usuarios Anónimos
    usuario_id BIGINT NULL, -- ID para usuarios registrados
    contador_clicks_total INT NOT NULL DEFAULT 0, -- Contador total de clicks en una url acortada
    contador_clicks_sesion INT NOT NULL DEFAULT 0, -- Contador de clicks en la sesion de un usuario registrado
    veces_reactivada INT NOT NULL DEFAULT 0, -- Contador de veces que se a reactivado una URL de usuarios Anónimos
    es_personalizada BOOLEAN NOT NULL DEFAULT FALSE, --Indica si se puede personalizar una URL, solo permitido para usuarios registrados
    INDEX idx_codigo_corto (codigo_corto), --Indices para mejorar el rendimiento de las consultas mas realizadas
    INDEX idx_hash_completo (hash_completo),
    INDEX idx_tipo_estado (tipo, estado),
    INDEX idx_fecha_expiracion (fecha_expiracion),
    INDEX idx_usuario_id (usuario_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios ( -- Próximamente, solo modelado para el futuro
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_email (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- Foreign key
ALTER TABLE urls
ADD CONSTRAINT fk_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE;