package org.yuemi.fsi2107.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Penyimpanan transaksi pada server MySQL.
 *
 * <p>Bila database yang dituju belum ada, database tersebut dibuat otomatis
 * pada koneksi pertama.</p>
 */
public class PenyimpananMysql extends PenyimpananJdbc {

    private static final String BUAT_TABEL =
            "CREATE TABLE IF NOT EXISTS transaksi ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT, "
                    + "waktu VARCHAR(32) NOT NULL, "
                    + "jenis VARCHAR(20) NOT NULL, "
                    + "kategori VARCHAR(50) NOT NULL, "
                    + "deskripsi VARCHAR(255), "
                    + "nominal BIGINT NOT NULL, "
                    + "PRIMARY KEY (id))";

    private static final String INSERT_TRANSAKSI =
            "INSERT INTO transaksi (waktu, jenis, kategori, deskripsi, nominal) VALUES (?, ?, ?, ?, ?)";

    private static final String PILIH_SEMUA =
            "SELECT id, waktu, jenis, kategori, deskripsi, nominal FROM transaksi ORDER BY id";

    private static final Pattern POLA_NAMA_DATABASE =
            Pattern.compile("jdbc:mysql://[^/]+/([^?/?]+).*", Pattern.CASE_INSENSITIVE);

    /**
     * @param driver   nama kelas driver JDBC MySQL
     * @param url      URL JDBC lengkap, contoh {@code jdbc:mysql://localhost:3306/keuangan}
     * @param username nama pengguna basis data
     * @param password sandi pengguna basis data
     */
    public PenyimpananMysql(String driver, String url, String username, String password) {
        super(() -> {
            muatDriver(driver);
            return bukaKoneksi(url, username, password);
        });
    }

    /**
     * Membuka koneksi ke server MySQL. Bila database belum ada, database
     * dibuat terlebih dahulu lalu koneksi diulang.
     */
    private static Connection bukaKoneksi(String url, String username, String password) throws SQLException {
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException ex) {
            String namaDatabase = namaDatabase(url);
            if (namaDatabase == null || !pesanDatabaseBelumAda(ex)) {
                throw ex;
            }
            buatDatabase(url, namaDatabase, username, password);
            return DriverManager.getConnection(url, username, password);
        }
    }

    /**
     * Menjalankan {@code CREATE DATABASE IF NOT EXISTS} melalui koneksi ke server
     * tanpa nama database.
     */
    private static void buatDatabase(String url, String namaDatabase, String username, String password)
            throws SQLException {
        String urlTanpaDatabase = url.replaceFirst("/" + java.util.regex.Pattern.quote(namaDatabase) + "([?/].*)?$", "/");
        try (Connection koneksi = DriverManager.getConnection(urlTanpaDatabase, username, password);
             Statement pernyataan = koneksi.createStatement()) {
            pernyataan.executeUpdate(
                    "CREATE DATABASE IF NOT EXISTS `" + namaDatabase + "` CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
        }
    }

    /**
     * Mengambil nama database dari URL JDBC.
     */
    private static String namaDatabase(String url) {
        Matcher pencocok = POLA_NAMA_DATABASE.matcher(url);
        if (!pencocok.matches()) {
            return null;
        }
        String nama = pencocok.group(1);
        return nama.isBlank() ? null : nama;
    }

    /**
     * Menentukan apakah kesalahan koneksi disebabkan database yang belum ada.
     */
    private static boolean pesanDatabaseBelumAda(SQLException ex) {
        String pesan = ex.getMessage() == null ? "" : ex.getMessage().toLowerCase(java.util.Locale.ROOT);
        return pesan.contains("unknown database") || pesan.contains("database does not exist")
                || pesan.contains("1049");
    }

    @Override
    protected String perintahBuatTabel() {
        return BUAT_TABEL;
    }

    @Override
    protected String perintahInsert() {
        return INSERT_TRANSAKSI;
    }

    @Override
    protected String perintahPilihSemua() {
        return PILIH_SEMUA;
    }
}
