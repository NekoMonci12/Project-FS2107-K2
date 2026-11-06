-- Skema basis data MySQL untuk aplikasi pencatatan keuangan.
-- Database dan tabel dibuat secara otomatis saat aplikasi dijalankan.

CREATE DATABASE IF NOT EXISTS `keuangan`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `keuangan`;

CREATE TABLE IF NOT EXISTS transaksi (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    waktu     VARCHAR(32)  NOT NULL,
    jenis     VARCHAR(20)  NOT NULL,
    kategori  VARCHAR(50)  NOT NULL,
    deskripsi VARCHAR(255),
    nominal   BIGINT       NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_transaksi_waktu (waktu)
);
