package org.yuemi.fsi2107.storage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Penyimpanan transaksi dan profil pada server MySQL.
 *
 * <p>Bila database yang dituju belum ada, database tersebut dibuat otomatis
 * pada koneksi pertama.</p>
 */
public class PenyimpananMysql extends PenyimpananJdbc {

    private static final String BUAT_TABEL_TRANSAKSI =
            "CREATE TABLE IF NOT EXISTS transaksi ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT, "
                    + "profile_id BIGINT NOT NULL DEFAULT 1, "
                    + "waktu VARCHAR(32) NOT NULL, "
                    + "jenis VARCHAR(20) NOT NULL, "
                    + "kategori VARCHAR(50) NOT NULL, "
                    + "deskripsi VARCHAR(255), "
                    + "nominal BIGINT NOT NULL, "
                    + "pph BIGINT NOT NULL DEFAULT 0, "
                    + "ppn BIGINT NOT NULL DEFAULT 0, "
                    + "PRIMARY KEY (id))";

    private static final String BUAT_TABEL_PROFILE =
            "CREATE TABLE IF NOT EXISTS profile ("
                    + "id BIGINT NOT NULL AUTO_INCREMENT, "
                    + "tipe VARCHAR(20) NOT NULL DEFAULT 'UMUM', "
                    + "nama VARCHAR(50) NOT NULL, "
                    + "catatan VARCHAR(255), "
                    + "dibuat_pada VARCHAR(32) NOT NULL, "
                    + "tarif_pph DECIMAL(5,2), "
                    + "tarif_ppn DECIMAL(5,2), "
                    + "PRIMARY KEY (id))";

    private static final String INSERT_TRANSAKSI =
            "INSERT INTO transaksi (profile_id, waktu, jenis, kategori, deskripsi, nominal, pph, ppn) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String INSERT_PROFILE =
            "INSERT INTO profile (tipe, nama, catatan, dibuat_pada, tarif_pph, tarif_ppn) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String INSERT_PROFILE_BAWAAN =
            "INSERT INTO profile (id, tipe, nama, catatan, dibuat_pada) "
                    + "VALUES (1, 'UMUM', ?, ?, ?)";

    private static final String PILIH_SEMUA_TRANSAKSI =
            "SELECT id, profile_id, waktu, jenis, kategori, deskripsi, nominal, pph, ppn "
                    + "FROM transaksi WHERE profile_id = ? ORDER BY id";

    private static final String PILIH_SEMUA_PROFILE =
            "SELECT id, tipe, nama, catatan, dibuat_pada, tarif_pph, tarif_ppn "
                    + "FROM profile ORDER BY id";

    private static final String HAPUS_PROFILE =
            "DELETE FROM profile WHERE id = ?";

    private static final String HAPUS_TRANSAKSI_PROFILE =
            "DELETE FROM transaksi WHERE profile_id = ?";

    private static final String TAMBAH_KOLOM_PROFILE =
            "ALTER TABLE transaksi ADD COLUMN profile_id BIGINT NOT NULL DEFAULT 1";

    private static final String TAMBAH_KOLOM_PPH =
            "ALTER TABLE transaksi ADD COLUMN pph BIGINT NOT NULL DEFAULT 0";

    private static final String TAMBAH_KOLOM_PPN =
            "ALTER TABLE transaksi ADD COLUMN ppn BIGINT NOT NULL DEFAULT 0";

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
        return BUAT_TABEL_TRANSAKSI;
    }

    @Override
    protected String perintahBuatTabelProfile() {
        return BUAT_TABEL_PROFILE;
    }

    @Override
    protected String perintahInsert() {
        return INSERT_TRANSAKSI;
    }

    @Override
    protected String perintahSisipProfile() {
        return INSERT_PROFILE;
    }

    @Override
    protected String perintahSisipProfileBawaan() {
        return INSERT_PROFILE_BAWAAN;
    }

    @Override
    protected String perintahPilihSemua() {
        return PILIH_SEMUA_TRANSAKSI;
    }

    @Override
    protected String perintahPilihSemuaProfile() {
        return PILIH_SEMUA_PROFILE;
    }

    @Override
    protected String perintahHapusProfile() {
        return HAPUS_PROFILE;
    }

    @Override
    protected String perintahHapusTransaksiProfile() {
        return HAPUS_TRANSAKSI_PROFILE;
    }

    @Override
    protected String perintahTambahKolomProfile() {
        return TAMBAH_KOLOM_PROFILE;
    }

    @Override
    protected String perintahTambahKolomPph() {
        return TAMBAH_KOLOM_PPH;
    }

    @Override
    protected String perintahTambahKolomPpn() {
        return TAMBAH_KOLOM_PPN;
    }
}
