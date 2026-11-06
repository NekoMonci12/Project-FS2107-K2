-- Skema basis data MySQL untuk aplikasi pencatatan keuangan.
-- Database dan tabel dibuat secara otomatis saat aplikasi dijalankan.

CREATE DATABASE IF NOT EXISTS `keuangan`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE `keuangan`;

CREATE TABLE IF NOT EXISTS profile (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    tipe        VARCHAR(20)  NOT NULL DEFAULT 'UMUM',
    nama        VARCHAR(50)  NOT NULL,
    catatan     VARCHAR(255),
    dibuat_pada VARCHAR(32)  NOT NULL,
    tarif_pph   DECIMAL(5,2),
    tarif_ppn   DECIMAL(5,2),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS transaksi (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    profile_id BIGINT      NOT NULL DEFAULT 1,
    waktu     VARCHAR(32)  NOT NULL,
    jenis     VARCHAR(20)  NOT NULL,
    kategori  VARCHAR(50)  NOT NULL,
    deskripsi VARCHAR(255),
    nominal   BIGINT       NOT NULL,
    pph       BIGINT       NOT NULL DEFAULT 0,
    ppn       BIGINT       NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    INDEX idx_transaksi_waktu (waktu),
    INDEX idx_transaksi_profile (profile_id),
    CONSTRAINT fk_transaksi_profile FOREIGN KEY (profile_id)
        REFERENCES profile (id) ON DELETE CASCADE
);
