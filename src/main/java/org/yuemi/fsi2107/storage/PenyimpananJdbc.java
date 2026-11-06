package org.yuemi.fsi2107.storage;

import org.yuemi.fsi2107.model.DataProfile;
import org.yuemi.fsi2107.model.JenisTransaksi;
import org.yuemi.fsi2107.model.Kategori;
import org.yuemi.fsi2107.model.PabrikProfile;
import org.yuemi.fsi2107.model.Profile;
import org.yuemi.fsi2107.model.ProfileUsaha;
import org.yuemi.fsi2107.model.Transaksi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
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
 * {@link Transaksi} dan {@link Profile} beserta pembacaan nilai enum.
 * Perintah SQL yang berbeda antar basis data diserahkan ke subclass melalui
 * method abstract di bawah.</p>
 */
abstract class PenyimpananJdbc implements Penyimpanan {

    /** Nama profil bawaan yang dibuat otomatis saat profil belum ada. */
    protected static final String NAMA_PROFILE_BAWAAN = "Default";

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
     * Perintah SQL untuk membuat tabel profil bila belum ada.
     */
    protected abstract String perintahBuatTabelProfile();

    /**
     * Perintah SQL untuk menyisipkan satu profil.
     */
    protected abstract String perintahSisipProfile();

    /**
     * Perintah SQL untuk menyisipkan profil bawaan dengan id tetap.
     */
    protected abstract String perintahSisipProfileBawaan();

    /**
     * Perintah SQL untuk membaca seluruh profil.
     */
    protected abstract String perintahPilihSemuaProfile();

    /**
     * Perintah SQL untuk menyisipkan satu transaksi.
     */
    protected abstract String perintahInsert();

    /**
     * Perintah SQL untuk membaca transaksi sebuah profil.
     */
    protected abstract String perintahPilihSemua();

    /**
     * Perintah SQL untuk menghapus satu profil.
     */
    protected abstract String perintahHapusProfile();

    /**
     * Perintah SQL untuk menghapus seluruh transaksi sebuah profil.
     */
    protected abstract String perintahHapusTransaksiProfile();

    /**
     * Perintah SQL untuk menambahkan kolom profil ke tabel transaksi lama.
     */
    protected abstract String perintahTambahKolomProfile();

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
            pernyataan.executeUpdate(perintahBuatTabelProfile());
            pernyataan.executeUpdate(perintahBuatTabel());
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal menyiapkan struktur penyimpanan data. " + ex.getMessage(), ex);
        }

        sisipkanProfileBawaanBilaPerlu();
        tambahKolomProfileBilaBelumAda();
    }

    /**
     * Menyisipkan profil bawaan "Default" bila tabel profil masih kosong.
     */
    private void sisipkanProfileBawaanBilaPerlu() {
        try {
            if (jumlahProfile() > 0) {
                return;
            }
            try (PreparedStatement pernyataan = koneksi().prepareStatement(perintahSisipProfileBawaan())) {
                pernyataan.setString(1, NAMA_PROFILE_BAWAAN);
                pernyataan.setString(2, "");
                pernyataan.setString(3, LocalDateTime.now().toString());
                pernyataan.executeUpdate();
            }
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal membuat profil bawaan. " + ex.getMessage(), ex);
        }
    }

    /**
     * Menambahkan kolom profil ke tabel transaksi lama yang belum memilikinya.
     * Transaksi lama otomatis menunjuk profil bawaan dengan id satu.
     */
    private void tambahKolomProfileBilaBelumAda() {
        if (kolomProfileSudahAda()) {
            return;
        }
        try (Statement pernyataan = koneksi().createStatement()) {
            pernyataan.executeUpdate(perintahTambahKolomProfile());
        } catch (SQLException ex) {
            throw new DataStorageException(
                    "Gagal menambahkan kolom profil pada tabel transaksi. " + ex.getMessage(), ex);
        }
    }

    /**
     * Mengecek apakah tabel transaksi sudah memiliki kolom profil.
     */
    private boolean kolomProfileSudahAda() {
        try {
            DatabaseMetaData metaData = koneksi().getMetaData();
            try (ResultSet kolom = metaData.getColumns(null, null, "transaksi", null)) {
                while (kolom.next()) {
                    String nama = kolom.getString("COLUMN_NAME");
                    if (nama != null && nama.equalsIgnoreCase("profile_id")) {
                        return true;
                    }
                }
            }
            return false;
        } catch (SQLException ex) {
            throw new DataStorageException(
                    "Gagal memeriksa struktur tabel transaksi. " + ex.getMessage(), ex);
        }
    }

    /**
     * Menghitung jumlah profil yang tersimpan.
     */
    private int jumlahProfile() {
        try (PreparedStatement pernyataan = koneksi().prepareStatement(
                "SELECT COUNT(*) FROM profile");
             ResultSet hasil = pernyataan.executeQuery()) {

            if (hasil.next()) {
                return hasil.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal menghitung jumlah profil. " + ex.getMessage(), ex);
        }
    }

    @Override
    public List<Transaksi> muatSemua(long profileId) {
        List<Transaksi> hasil = new ArrayList<>();
        try (PreparedStatement pernyataan = koneksi().prepareStatement(perintahPilihSemua())) {

            pernyataan.setLong(1, profileId);
            try (ResultSet hasilKueri = pernyataan.executeQuery()) {
                while (hasilKueri.next()) {
                    hasil.add(bacaTransaksi(hasilKueri));
                }
            }
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal membaca transaksi dari basis data. " + ex.getMessage(), ex);
        }
        return hasil;
    }

    @Override
    public List<Profile> muatSemuaProfile() {
        List<Profile> hasil = new ArrayList<>();
        try (PreparedStatement pernyataan = koneksi().prepareStatement(perintahPilihSemuaProfile());
             ResultSet hasilKueri = pernyataan.executeQuery()) {

            while (hasilKueri.next()) {
                hasil.add(PabrikProfile.buat(bacaDataProfile(hasilKueri)));
            }
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal membaca profil dari basis data. " + ex.getMessage(), ex);
        }
        return hasil;
    }

    @Override
    public Transaksi simpan(long profileId, JenisTransaksi jenis, Kategori kategori, String deskripsi,
                            long nominal, LocalDateTime waktu) {
        try (PreparedStatement pernyataan = koneksi().prepareStatement(
                perintahInsert(), Statement.RETURN_GENERATED_KEYS)) {

            pernyataan.setLong(1, profileId);
            pernyataan.setString(2, waktu.toString());
            pernyataan.setString(3, jenis.name());
            pernyataan.setString(4, kategori.name());
            pernyataan.setString(5, deskripsi == null ? "" : deskripsi);
            pernyataan.setLong(6, nominal);
            pernyataan.executeUpdate();

            long id = bacaIdBaru(pernyataan);
            return new Transaksi(id, profileId, waktu, jenis, kategori, deskripsi, nominal);
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal menyimpan transaksi ke basis data. " + ex.getMessage(), ex);
        }
    }

    @Override
    public Profile simpanProfile(Profile profile) {
        Double tarifPph = null;
        Double tarifPpn = null;
        if (profile instanceof ProfileUsaha usaha) {
            tarifPph = usaha.getTarifPph();
            tarifPpn = usaha.getTarifPpn();
        }

        try (PreparedStatement pernyataan = koneksi().prepareStatement(
                perintahSisipProfile(), Statement.RETURN_GENERATED_KEYS)) {

            pernyataan.setString(1, profile.getTipe());
            pernyataan.setString(2, profile.getNama());
            pernyataan.setString(3, profile.getCatatan());
            pernyataan.setString(4, profile.getDibuatPada().toString());
            setTarif(pernyataan, 5, tarifPph);
            setTarif(pernyataan, 6, tarifPpn);
            pernyataan.executeUpdate();

            long id = bacaIdBaru(pernyataan);
            return PabrikProfile.buat(new DataProfile(profile.getTipe(), id, profile.getNama(),
                    profile.getCatatan(), profile.getDibuatPada(), tarifPph, tarifPpn));
        } catch (SQLException ex) {
            throw new DataStorageException("Gagal menyimpan profil ke basis data. " + ex.getMessage(), ex);
        }
    }

    /**
     * Menulis nilai tarif pajak, atau {@code null} bila profil tidak memakainya.
     */
    private static void setTarif(PreparedStatement pernyataan, int indeks, Double tarif)
            throws SQLException {
        if (tarif == null) {
            pernyataan.setNull(indeks, java.sql.Types.DECIMAL);
        } else {
            pernyataan.setDouble(indeks, tarif);
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
    public boolean hapusProfile(long id) {
        Connection koneksiAktif = koneksi();
        boolean autoCommitAwal = true;
        try {
            autoCommitAwal = koneksiAktif.getAutoCommit();
            koneksiAktif.setAutoCommit(false);

            try (PreparedStatement hapusTransaksi = koneksiAktif.prepareStatement(
                    perintahHapusTransaksiProfile())) {
                hapusTransaksi.setLong(1, id);
                hapusTransaksi.executeUpdate();
            }

            int jumlahTerhapus;
            try (PreparedStatement hapusProfil = koneksiAktif.prepareStatement(perintahHapusProfile())) {
                hapusProfil.setLong(1, id);
                jumlahTerhapus = hapusProfil.executeUpdate();
            }

            koneksiAktif.commit();
            return jumlahTerhapus > 0;
        } catch (SQLException ex) {
            batalkan(koneksiAktif);
            throw new DataStorageException("Gagal menghapus profil dari basis data. " + ex.getMessage(), ex);
        } finally {
            kembalikanAutoCommit(koneksiAktif, autoCommitAwal);
        }
    }

    /**
     * Membatalkan transaksi basis data yang gagal.
     */
    private static void batalkan(Connection koneksiAktif) {
        try {
            koneksiAktif.rollback();
        } catch (SQLException ex) {
            System.err.println("Peringatan: gagal membatalkan transaksi basis data. " + ex.getMessage());
        }
    }

    /**
     * Mengembalikan pengaturan auto commit seperti semula.
     */
    private static void kembalikanAutoCommit(Connection koneksiAktif, boolean autoCommitAwal) {
        try {
            koneksiAktif.setAutoCommit(autoCommitAwal);
        } catch (SQLException ex) {
            System.err.println("Peringatan: gagal mengembalikan pengaturan auto commit. " + ex.getMessage());
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
        throw new DataStorageException("Basis data tidak mengembalikan id data yang baru disimpan.");
    }

    /**
     * Memetaikan satu baris hasil query menjadi objek transaksi.
     */
    private static Transaksi bacaTransaksi(ResultSet baris) throws SQLException {
        long id = baris.getLong("id");
        long profileId = baris.getLong("profile_id");
        LocalDateTime waktu = bacaWaktu(baris.getString("waktu"));
        JenisTransaksi jenis = bacaJenis(baris.getString("jenis"));
        Kategori kategori = bacaKategori(baris.getString("kategori"));
        String deskripsi = baris.getString("deskripsi");
        long nominal = baris.getLong("nominal");
        return new Transaksi(id, profileId, waktu, jenis, kategori, deskripsi, nominal);
    }

    /**
     * Memetaikan satu baris hasil query menjadi data mentah profil.
     */
    private static DataProfile bacaDataProfile(ResultSet baris) throws SQLException {
        long id = baris.getLong("id");
        String tipe = baris.getString("tipe");
        String nama = baris.getString("nama");
        String catatan = baris.getString("catatan");
        LocalDateTime dibuatPada = bacaWaktu(baris.getString("dibuat_pada"));
        Double tarifPph = bacaTarif(baris, "tarif_pph");
        Double tarifPpn = bacaTarif(baris, "tarif_ppn");
        return new DataProfile(tipe, id, nama, catatan, dibuatPada, tarifPph, tarifPpn);
    }

    /**
     * Membaca kolom tarif pajak, mengembalikan {@code null} bila kosong.
     */
    private static Double bacaTarif(ResultSet baris, String kolom) throws SQLException {
        double nilai = baris.getDouble(kolom);
        if (baris.wasNull()) {
            return null;
        }
        return nilai;
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
