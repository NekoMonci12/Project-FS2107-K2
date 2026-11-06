package org.yuemi.fsi2107.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * Konfigurasi aplikasi dibaca dari {@code config.properties}.
 *
 * <p>Sumber konfigurasi dibaca berurutan dan nilai berikutnya menimpa nilai
 * sebelumnya:</p>
 * <ol>
 *   <li>{@code config.properties} di classpath (bawaan aplikasi),</li>
 *   <li>{@code config.properties} di direktori kerja (opsional, hasil deploy),</li>
 *   <li>system property (misal {@code -Dstorage.type=mysql}),</li>
 *   <li>environment variable (misal {@code STORAGE_TYPE=mysql}).</li>
 * </ol>
 *
 * <p>Setiap kunci memiliki nilai bawaan di dalam kelas ini, sehingga aplikasi
 * tetap berjalan walau berkas konfigurasi tidak ditemukan.</p>
 */
public final class KonfigurasiAplikasi {

    /** Nama berkas konfigurasi yang dicari di classpath dan direktori kerja. */
    public static final String NAMA_BERKAS = "config.properties";

    private static final String KUNCI_TIPE = "storage.type";
    private static final String KUNCI_SQLITE_DRIVER = "storage.sqlite.driver";
    private static final String KUNCI_SQLITE_PATH = "storage.sqlite.path";
    private static final String KUNCI_MYSQL_DRIVER = "storage.mysql.driver";
    private static final String KUNCI_MYSQL_URL = "storage.mysql.url";
    private static final String KUNCI_MYSQL_USERNAME = "storage.mysql.username";
    private static final String KUNCI_MYSQL_PASSWORD = "storage.mysql.password";

    /** Jenis penyimpanan bawaan bila {@code storage.type} tidak dikenali. */
    public static final String TIPE_SQLITE = "sqlite";
    public static final String TIPE_MYSQL = "mysql";

    /** Seluruh kunci yang dikenali aplikasi, dipakai untuk pencocokan sumber luar. */
    private static final List<String> KUNCI_KONFIGURASI = List.of(
            KUNCI_TIPE,
            KUNCI_SQLITE_DRIVER,
            KUNCI_SQLITE_PATH,
            KUNCI_MYSQL_DRIVER,
            KUNCI_MYSQL_URL,
            KUNCI_MYSQL_USERNAME,
            KUNCI_MYSQL_PASSWORD);

    private final Properties properti;

    private KonfigurasiAplikasi(Properties properti) {
        this.properti = properti;
    }

    /**
     * Memuat konfigurasi gabungan dari seluruh sumber yang tersedia.
     */
    public static KonfigurasiAplikasi muat() {
        Properties properti = new Properties();
        muatDariClasspath(properti);
        muatDariBerkasLuar(properti, Path.of(NAMA_BERKAS));
        muatDariSystemProperty(properti);
        muatDariEnvironment(properti);
        return new KonfigurasiAplikasi(properti);
    }

    private static void muatDariClasspath(Properties properti) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = KonfigurasiAplikasi.class.getClassLoader();
        }
        try (InputStream aliran = loader.getResourceAsStream(NAMA_BERKAS)) {
            if (aliran != null) {
                properti.load(aliran);
            }
        } catch (IOException ex) {
            System.err.println("Peringatan: gagal membaca " + NAMA_BERKAS
                    + " dari classpath, memakai nilai bawaan. " + ex.getMessage());
        }
    }

    private static void muatDariBerkasLuar(Properties properti, Path jalur) {
        if (!Files.isRegularFile(jalur)) {
            return;
        }
        try (InputStream aliran = Files.newInputStream(jalur)) {
            properti.load(aliran);
        } catch (IOException ex) {
            System.err.println("Peringatan: gagal membaca " + jalur
                    + ", memakai nilai bawaan. " + ex.getMessage());
        }
    }

    private static void muatDariSystemProperty(Properties properti) {
        for (String kunci : KUNCI_KONFIGURASI) {
            String nilai = System.getProperty(kunci);
            if (nilai != null) {
                properti.setProperty(kunci, nilai);
            }
        }
    }

    private static void muatDariEnvironment(Properties properti) {
        for (String kunci : KUNCI_KONFIGURASI) {
            String nilai = System.getenv(keEnvironment(kunci));
            if (nilai != null && !nilai.isBlank()) {
                properti.setProperty(kunci, nilai);
            }
        }
    }

    /**
     * Mengubah nama kunci konfigurasi menjadi format environment variable.
     */
    private static String keEnvironment(String kunci) {
        return kunci.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    }

    /**
     * Jenis penyimpanan yang dipilih: {@code sqlite} atau {@code mysql}.
     * Nilai yang tidak dikenali jatuh ke {@code sqlite} disertai peringatan.
     */
    public String getTipePenyimpanan() {
        String tipe = ambil(KUNCI_TIPE, TIPE_SQLITE).trim().toLowerCase(Locale.ROOT);
        if (tipe.isEmpty()) {
            return TIPE_SQLITE;
        }
        if (!TIPE_SQLITE.equals(tipe) && !TIPE_MYSQL.equals(tipe)) {
            System.err.println("Peringatan: storage.type '" + tipe
                    + "' tidak dikenali, memakai " + TIPE_SQLITE + ".");
            return TIPE_SQLITE;
        }
        return tipe;
    }

    public String getDriverSqlite() {
        return ambil(KUNCI_SQLITE_DRIVER, "org.sqlite.JDBC");
    }

    /**
     * Lokasi berkas basis data SQLite, relatif terhadap direktori kerja.
     */
    public String getPathSqlite() {
        return ambil(KUNCI_SQLITE_PATH, "data/keuangan.db");
    }

    public String getDriverMysql() {
        return ambil(KUNCI_MYSQL_DRIVER, "com.mysql.cj.jdbc.Driver");
    }

    public String getUrlMysql() {
        return ambil(KUNCI_MYSQL_URL,
                "jdbc:mysql://localhost:3306/keuangan?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
    }

    public String getUsernameMysql() {
        return ambil(KUNCI_MYSQL_USERNAME, "root");
    }

    public String getPasswordMysql() {
        return ambil(KUNCI_MYSQL_PASSWORD, "");
    }

    private String ambil(String kunci, String bawaan) {
        String nilai = properti.getProperty(kunci);
        return nilai == null || nilai.isBlank() ? bawaan : nilai.trim();
    }
}
