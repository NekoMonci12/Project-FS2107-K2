package org.yuemi.fsi2107.service;

import org.yuemi.fsi2107.model.JenisTransaksi;
import org.yuemi.fsi2107.model.Kategori;
import org.yuemi.fsi2107.model.PajakTransaksi;
import org.yuemi.fsi2107.model.Profile;
import org.yuemi.fsi2107.model.Transaksi;
import org.yuemi.fsi2107.storage.Penyimpanan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Buku kas: menyimpan seluruh transaksi selama aplikasi berjalan
 * dan menghitung ringkasan keuangan.
 *
 * <p>Setiap transaksi yang ditambahkan atau dihapus langsung disimpan ke
 * {@link Penyimpanan}, sehingga data tetap tersedia setelah aplikasi
 * ditutup. Daftar di memori dipakai untuk menyusun ringkasan dan tabel.</p>
 */
public class BukuKas {

    private final List<Transaksi> daftarTransaksi = new ArrayList<>();
    private final List<Consumer<BukuKas>> pendengar = new ArrayList<>();
    private final Penyimpanan penyimpanan;

    /** Profil pemilik transaksi yang sedang ditampilkan. */
    private Profile profilAktif;

    /**
     * Menyusun buku kas dan memuat seluruh transaksi yang sudah tersimpan.
     *
     * @param penyimpanan lapisan penyimpanan data
     */
    public BukuKas(Penyimpanan penyimpanan) {
        this.penyimpanan = Objects.requireNonNull(penyimpanan, "penyimpanan tidak boleh null");
        this.penyimpanan.siapkan();
    }

    /**
     * Mengaitkan buku kas dengan profil yang sedang aktif lalu memuat
     * transaksi milik profil tersebut.
     *
     * @param profil profil pemilik transaksi
     */
    public void setProfilAktif(Profile profil) {
        if (profil == null) {
            return;
        }
        if (profilAktif != null && profilAktif.getId() == profil.getId()) {
            return;
        }
        profilAktif = profil;
        muatDariPenyimpanan();
        beriTahuPendengar();
    }

    /**
     * Profil pemilik transaksi yang sedang ditampilkan.
     */
    public Profile getProfilAktif() {
        return profilAktif;
    }

    /**
     * Menambahkan transaksi baru lalu memberi tahu seluruh pendengar.
     */
    public Transaksi tambahTransaksi(JenisTransaksi jenis, Kategori kategori, String deskripsi, long nominal) {
        Objects.requireNonNull(jenis, "jenis tidak boleh null");
        Objects.requireNonNull(kategori, "kategori tidak boleh null");
        if (kategori.getJenis() != jenis) {
            throw new IllegalArgumentException(
                    "Kategori " + kategori.getLabel() + " bukan untuk jenis " + jenis.getLabel());
        }
        if (nominal <= 0) {
            throw new IllegalArgumentException("Nominal harus lebih besar dari nol");
        }

        if (profilAktif == null) {
            throw new IllegalStateException("Belum ada profil aktif yang dipilih.");
        }

        PajakTransaksi pajak = profilAktif.hitungPajak(jenis, nominal);
        Transaksi transaksi = penyimpanan.simpan(profilAktif.getId(), jenis, kategori, deskripsi,
                nominal, LocalDateTime.now(), pajak.pph(), pajak.ppn());
        daftarTransaksi.add(transaksi);
        beriTahuPendengar();
        return transaksi;
    }

    /**
     * Menghapus transaksi berdasarkan id. Mengembalikan {@code true} bila ada yang terhapus.
     */
    public boolean hapusTransaksi(long id) {
        boolean terhapus = penyimpanan.hapus(id);
        if (terhapus) {
            daftarTransaksi.removeIf(transaksi -> transaksi.getId() == id);
            beriTahuPendengar();
        }
        return terhapus;
    }

    /**
     * Mengisi daftar transaksi dari penyimpanan.
     */
    private void muatDariPenyimpanan() {
        daftarTransaksi.clear();
        if (profilAktif != null) {
            daftarTransaksi.addAll(penyimpanan.muatSemua(profilAktif.getId()));
        }
    }

    public List<Transaksi> getDaftarTransaksi() {
        return Collections.unmodifiableList(daftarTransaksi);
    }

    public int getJumlahTransaksi() {
        return daftarTransaksi.size();
    }

    /**
     * Total uang masuk setelah pajak dipotong.
     */
    public long getTotalMasuk() {
        return daftarTransaksi.stream()
                .filter(Transaksi::isMasuk)
                .mapToLong(Transaksi::getNominalBersih)
                .sum();
    }

    /**
     * Total uang keluar setelah PPN ditambahkan ke biaya.
     */
    public long getTotalKeluar() {
        return daftarTransaksi.stream()
                .filter(transaksi -> !transaksi.isMasuk())
                .mapToLong(Transaksi::getNominalBersih)
                .sum();
    }

    /**
     * Total PPh yang dipotong dari seluruh transaksi profil aktif.
     */
    public long getTotalPph() {
        return daftarTransaksi.stream()
                .mapToLong(Transaksi::getPph)
                .sum();
    }

    /**
     * Total PPN dari seluruh transaksi profil aktif, baik yang dipotong dari
     * uang masuk maupun yang ditambahkan pada uang keluar.
     */
    public long getTotalPpn() {
        return daftarTransaksi.stream()
                .mapToLong(Transaksi::getPpn)
                .sum();
    }

    public long getSaldo() {
        return getTotalMasuk() - getTotalKeluar();
    }

    public void tambahPendengar(Consumer<BukuKas> pendengarBaru) {
        pendengar.add(Objects.requireNonNull(pendengarBaru));
    }

    /**
     * Menutup lapisan penyimpanan data.
     */
    public void tutup() {
        penyimpanan.close();
    }

    private void beriTahuPendengar() {
        for (Consumer<BukuKas> satuPendengar : List.copyOf(pendengar)) {
            satuPendengar.accept(this);
        }
    }
}
