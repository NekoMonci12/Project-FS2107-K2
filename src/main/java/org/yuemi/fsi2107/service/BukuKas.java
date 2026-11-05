package org.yuemi.fsi2107.service;

import org.yuemi.fsi2107.model.JenisTransaksi;
import org.yuemi.fsi2107.model.Kategori;
import org.yuemi.fsi2107.model.Transaksi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Buku kas: menyimpan seluruh transaksi selama aplikasi berjalan
 * dan menghitung ringkasan keuangan.
 */
public class BukuKas {

    private final List<Transaksi> daftarTransaksi = new ArrayList<>();
    private final List<Consumer<BukuKas>> pendengar = new ArrayList<>();
    private long idBerikutnya = 1L;

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

        Transaksi transaksi = new Transaksi(idBerikutnya++, jenis, kategori, deskripsi, nominal);
        daftarTransaksi.add(transaksi);
        beriTahuPendengar();
        return transaksi;
    }

    /**
     * Menghapus transaksi berdasarkan id. Mengembalikan {@code true} bila ada yang terhapus.
     */
    public boolean hapusTransaksi(long id) {
        boolean terhapus = daftarTransaksi.removeIf(transaksi -> transaksi.getId() == id);
        if (terhapus) {
            beriTahuPendengar();
        }
        return terhapus;
    }

    public List<Transaksi> getDaftarTransaksi() {
        return Collections.unmodifiableList(daftarTransaksi);
    }

    public int getJumlahTransaksi() {
        return daftarTransaksi.size();
    }

    public long getTotalMasuk() {
        return daftarTransaksi.stream()
                .filter(Transaksi::isMasuk)
                .mapToLong(Transaksi::getNominal)
                .sum();
    }

    public long getTotalKeluar() {
        return daftarTransaksi.stream()
                .filter(transaksi -> !transaksi.isMasuk())
                .mapToLong(Transaksi::getNominal)
                .sum();
    }

    public long getSaldo() {
        return getTotalMasuk() - getTotalKeluar();
    }

    public void tambahPendengar(Consumer<BukuKas> pendengarBaru) {
        pendengar.add(Objects.requireNonNull(pendengarBaru));
    }

    private void beriTahuPendengar() {
        for (Consumer<BukuKas> satuPendengar : List.copyOf(pendengar)) {
            satuPendengar.accept(this);
        }
    }
}
