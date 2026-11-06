package org.yuemi.fsi2107.storage;

import org.yuemi.fsi2107.model.JenisTransaksi;
import org.yuemi.fsi2107.model.Kategori;
import org.yuemi.fsi2107.model.Transaksi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Basis umum implementasi penyimpanan berbasis JDBC.
 *
 * <p>Kelas ini menangani pemetaan baris hasil query menjadi objek
 * {@link Transaksi} beserta pembacaan nilai enum. Perintah SQL yang berbeda
 * antar basis data diserahkan ke subclass melalui
 * {@link #perintahBuatTabel()}, {@link #perintahInsert()} dan
 * {@link #perintahPilihSemua()}.</p>
 */
abstract class PenyimpananJdbc implements Penyimpanan {

    private final SumberKoneksi sumberKoneksi;
    private Connection koneksi;

    /**
     * Sumber koneksi yang dipakai subclass untuk membuka koneksi baru.
     */
    @FunctionalInterface
    interface SumberKoneksi {
        Connection buka() throws SQLException;
    }

    protected PenyimpananJdbc(SumberKoneksi sumberKoneksi) {
        this.sumberKoneksi = sumberKoneksi;
    }

    /**
     * Mengembalikan koneksi yang dipakai, membukanya bila belum ada.
     */
    protected synchronized Connection koneksi() {
        if (koneksi == null) {
            try {
                koneksi = sumberKoneksi.buka();
            } catch (SQLException ex) {
                throw new DataStorageException("Gagal membuka koneksi ke basis data. " + ex.getMessage(), ex);
            }
        }
        return koneksi;
    }

    /**
     * Perintah SQL untuk membuat tabel transaksi bila belum ada.
     */
    protected abstract String perintahBuatTabel();

    /**
     * Perintah SQL untuk menyisipkan satu transaksi.
     */
    protected abstract String perintahInsert();

    /**
     * Perintah SQL untuk membaca seluruh transaksi.
     */
    protected abstract String perintahPilihSemua();

    /**
     * Memuat driver JDBC bila belum dimuat oleh JVM.
     */
    protected static void muatDriver(String namaDriver) {
        try {
            Class.forName(namaDriver);
        } catch (ClassNotFoundException ex) {
            throw new DataStorageException(
                    "Driver JDBC '" + namaDriver + "' tidak ditemukan di classpath.", ex);
        }
    }

    @Override
    public void siapkan() {
        try (Statement pernyataan = koneksi().createStatement()) {
            pernyataan.executeUpdate(perintahBuatTabel());
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal menyiapkan struktur penyimpanan data. " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Transaksi> muatSemua() {
        List<Transaksi> hasil = new ArrayList<>();
        try (PreparedStatement pernyataan = koneksi().prepareStatement(perintahPilihSemua());
             ResultSet hasilKueri = pernyataan.executeQuery()) {

            while (hasilKueri.next()) {
                hasil.add(bacaTransaksi(hasilKueri));
            }
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal membaca transaksi dari basis data. " + ex.getMessage(), ex);
        }
        return hasil;
    }

    @Override
    public Transaksi simpan(JenisTransaksi jenis, Kategori kategori, String deskripsi,
                            long nominal, LocalDateTime waktu) {
        try (PreparedStatement pernyataan = koneksi().prepareStatement(
                perintahInsert(), Statement.RETURN_GENERATED_KEYS)) {

            pernyataan.setString(1, waktu.toString());
            pernyataan.setString(2, jenis.name());
            pernyataan.setString(3, kategori.name());
            pernyataan.setString(4, deskripsi == null ? "" : deskripsi);
            pernyataan.setLong(5, nominal);
            pernyataan.executeUpdate();

            long id = bacaIdBaru(pernyataan);
            return new Transaksi(id, waktu, jenis, kategori, deskripsi, nominal);
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal menyimpan transaksi ke basis data. " + ex.getMessage(), ex);
        }
    }

    @Override
    public boolean hapus(long id) {
        try (PreparedStatement pernyataan = koneksi().prepareStatement(
                "DELETE FROM transaksi WHERE id = ?")) {

            pernyataan.setLong(1, id);
            return pernyataan.executeUpdate() > 0;
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal menghapus transaksi dari basis data. " + ex.getMessage(), ex);
        }
    }

    @Override
    public synchronized void close() {
        if (koneksi == null) {
            return;
        }
        try {
            koneksi.close();
        } catch (SQLException ex) {
            System.err.println("Peringatan: gagal menutup koneksi basis data. " + ex.getMessage());
        } finally {
            koneksi = null;
        }
    }

    /**
     * Membaca id yang dibuat basis data untuk baris yang baru saja disisipkan.
     */
    private static long bacaIdBaru(PreparedStatement pernyataan) throws SQLException {
        try (ResultSet kunci = pernyataan.getGeneratedKeys()) {
            if (kunci.next()) {
                return kunci.getLong(1);
            }
        }
        throw new DataStorageException("Basis data tidak mengembalikan id transaksi yang baru disimpan.");
    }

    /**
     * Memetaikan satu baris hasil query menjadi objek transaksi.
     */
    private static Transaksi bacaTransaksi(ResultSet baris) throws SQLException {
        long id = baris.getLong("id");
        LocalDateTime waktu = bacaWaktu(baris.getString("waktu"));
        JenisTransaksi jenis = bacaJenis(baris.getString("jenis"));
        Kategori kategori = bacaKategori(baris.getString("kategori"));
        String deskripsi = baris.getString("deskripsi");
        long nominal = baris.getLong("nominal");
        return new Transaksi(id, waktu, jenis, kategori, deskripsi, nominal);
    }

    /**
     * Membaca kolom waktu. Format ISO-8601 dipakai SQLite, sementara MySQL
     * dapat mengembalikan waktu dalam format bawaan driver.
     */
    private static LocalDateTime bacaWaktu(String teks) {
        if (teks == null || teks.isBlank()) {
            return LocalDateTime.now();
        }
        String bersih = teks.trim();
        try {
            return LocalDateTime.parse(bersih);
        } catch (DateTimeParseException diabaikan) {
            // Lanjut ke pembacaan dengan format bawaan JDBC.
        }
        try {
            return java.sql.Timestamp.valueOf(bersih.replace('T', ' ')).toLocalDateTime();
        } catch (IllegalArgumentException ex) {
            throw new DataStorageException("Format waktu transaksi tidak dikenali: " + teks, ex);
        }
    }

    private static JenisTransaksi bacaJenis(String nilai) {
        try {
            return JenisTransaksi.valueOf(nilai.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DataStorageException("Jenis transaksi tidak dikenali: " + nilai, ex);
        }
    }

    private static Kategori bacaKategori(String nilai) {
        try {
            return Kategori.valueOf(nilai.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new DataStorageException("Kategori transaksi tidak dikenali: " + nilai, ex);
        }
    }
}
