-- Skema basis data SQLite untuk aplikasi pencatatan keuangan.
-- Aplikasi membuat tabel ini secara otomatis saat dijalankan.

CREATE TABLE IF NOT EXISTS profile (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    tipe        VARCHAR(20)  NOT NULL DEFAULT 'UMUM',
    nama        VARCHAR(50)  NOT NULL,
    catatan     VARCHAR(255),
    dibuat_pada TEXT         NOT NULL,
    tarif_pph   DECIMAL(5,2),
    tarif_ppn   DECIMAL(5,2)
);

CREATE TABLE IF NOT EXISTS transaksi (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    profile_id INTEGER       NOT NULL DEFAULT 1,
    waktu     TEXT         NOT NULL,
    jenis     VARCHAR(20)  NOT NULL,
    kategori  VARCHAR(50)  NOT NULL,
    deskripsi VARCHAR(255),
    nominal   BIGINT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transaksi_waktu ON transaksi (waktu);
CREATE INDEX IF NOT EXISTS idx_transaksi_profile ON transaksi (profile_id);
