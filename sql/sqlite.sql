-- Skema basis data SQLite untuk aplikasi pencatatan keuangan.
-- Aplikasi membuat tabel ini secara otomatis saat dijalankan.

CREATE TABLE IF NOT EXISTS transaksi (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    waktu     TEXT         NOT NULL,
    jenis     VARCHAR(20)  NOT NULL,
    kategori  VARCHAR(50)  NOT NULL,
    deskripsi VARCHAR(255),
    nominal   BIGINT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transaksi_waktu ON transaksi (waktu);
