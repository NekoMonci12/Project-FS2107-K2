package org.yuemi.fsi2107.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;

/**
 * Penyimpanan transaksi pada berkas basis data SQLite.
 *
 * <p>Berkas database memakai lokasi relatif terhadap direktori kerja aplikasi
 * dan foldernya dibuat otomatis bila belum ada.</p>
 */
public class PenyimpananSqlite extends PenyimpananJdbc {

    private static final String BUAT_TABEL =
            "CREATE TABLE IF NOT EXISTS transaksi ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "waktu TEXT NOT NULL, "
                    + "jenis VARCHAR(20) NOT NULL, "
                    + "kategori VARCHAR(50) NOT NULL, "
                    + "deskripsi VARCHAR(255), "
                    + "nominal BIGINT NOT NULL)";

    private static final String INSERT_TRANSAKSI =
            "INSERT INTO transaksi (waktu, jenis, kategori, deskripsi, nominal) VALUES (?, ?, ?, ?, ?)";

    private static final String PILIH_SEMUA =
            "SELECT id, waktu, jenis, kategori, deskripsi, nominal FROM transaksi ORDER BY id";

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
