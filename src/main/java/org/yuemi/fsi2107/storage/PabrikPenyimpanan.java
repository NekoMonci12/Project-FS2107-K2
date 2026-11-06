package org.yuemi.fsi2107.storage;

/**
 * Pabrik pembuatan implementasi {@link Penyimpanan} berdasarkan konfigurasi.
 */
public final class PabrikPenyimpanan {

    private PabrikPenyimpanan() {
    }

    /**
     * Membuat penyimpanan sesuai jenis yang dipilih pada konfigurasi.
     *
     * @throws DataStorageException bila jenis penyimpanan tidak dikenali
     */
    public static Penyimpanan create(KonfigurasiAplikasi konfigurasi) {
        String tipe = konfigurasi.getTipePenyimpanan();
        return switch (tipe) {
            case KonfigurasiAplikasi.TIPE_SQLITE -> new PenyimpananSqlite(
                    konfigurasi.getDriverSqlite(), konfigurasi.getPathSqlite());
            case KonfigurasiAplikasi.TIPE_MYSQL -> new PenyimpananMysql(
                    konfigurasi.getDriverMysql(), konfigurasi.getUrlMysql(),
                    konfigurasi.getUsernameMysql(), konfigurasi.getPasswordMysql());
            default -> throw new DataStorageException(
                    "Jenis penyimpanan '" + tipe + "' tidak dikenali.");
        };
    }
}
