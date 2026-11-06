package org.yuemi.fsi2107.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

/**
 * Penyimpanan transaksi dan profil pada berkas basis data SQLite.
 *
 * <p>Berkas database memakai lokasi relatif terhadap direktori kerja aplikasi
 * dan foldernya dibuat otomatis bila belum ada.</p>
 */
public class PenyimpananSqlite extends PenyimpananJdbc {

    private static final String BUAT_TABEL_TRANSAKSI =
            "CREATE TABLE IF NOT EXISTS transaksi ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "profile_id INTEGER NOT NULL DEFAULT 1, "
                    + "waktu TEXT NOT NULL, "
                    + "jenis VARCHAR(20) NOT NULL, "
                    + "kategori VARCHAR(50) NOT NULL, "
                    + "deskripsi VARCHAR(255), "
                    + "nominal BIGINT NOT NULL, "
                    + "pph BIGINT NOT NULL DEFAULT 0, "
                    + "ppn BIGINT NOT NULL DEFAULT 0)";

    private static final String BUAT_TABEL_PROFILE =
            "CREATE TABLE IF NOT EXISTS profile ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "tipe VARCHAR(20) NOT NULL DEFAULT 'UMUM', "
                    + "nama VARCHAR(50) NOT NULL, "
                    + "catatan VARCHAR(255), "
                    + "dibuat_pada TEXT NOT NULL, "
                    + "tarif_pph DECIMAL(5,2), "
                    + "tarif_ppn DECIMAL(5,2))";

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
            "ALTER TABLE transaksi ADD COLUMN profile_id INTEGER NOT NULL DEFAULT 1";

    private static final String TAMBAH_KOLOM_PPH =
            "ALTER TABLE transaksi ADD COLUMN pph BIGINT NOT NULL DEFAULT 0";

    private static final String TAMBAH_KOLOM_PPN =
            "ALTER TABLE transaksi ADD COLUMN ppn BIGINT NOT NULL DEFAULT 0";

    /**
     * @param driver nama kelas driver JDBC SQLite
     * @param path  lokasi berkas database relatif terhadap direktori kerja
     */
    public PenyimpananSqlite(String driver, String path) {
        super(() -> {
            muatDriver(driver);
            return DriverManager.getConnection(buatUrl(path));
        });
    }

    /**
     * Menyusun URL JDBC sekaligus memastikan folder induk berkas tersedia.
     */
    private static String buatUrl(String path) {
        Path berkas = Path.of(path).toAbsolutePath().normalize();
        Path induk = berkas.getParent();
        if (induk != null && !Files.isDirectory(induk)) {
            try {
                Files.createDirectories(induk);
            } catch (IOException ex) {
                throw new DataStorageException(
                        "Gagal membuat folder penyimpanan data '" + induk + "'. " + ex.getMessage(), ex);
            }
        }
        return "jdbc:sqlite:" + berkas;
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
