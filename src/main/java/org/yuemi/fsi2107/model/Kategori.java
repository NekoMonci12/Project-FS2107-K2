package org.yuemi.fsi2107.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Kategori transaksi. Setiap kategori terikat pada satu jenis transaksi
 * sehingga pilihan kategori dapat disaring sesuai jenis yang dipilih pengguna.
 */
public enum Kategori {

    GAJI("Gaji", JenisTransaksi.MASUK),
    BONUS("Bonus", JenisTransaksi.MASUK),
    PENJUALAN("Penjualan", JenisTransaksi.MASUK),
    BUNGA("Bunga", JenisTransaksi.MASUK),
    MAKAN("Makan", JenisTransaksi.KELUAR),
    TRANSPORT("Transport", JenisTransaksi.KELUAR),
    BELANJA("Belanja", JenisTransaksi.KELUAR),
    TAGIHAN("Tagihan", JenisTransaksi.KELUAR),
    HIBURAN("Hiburan", JenisTransaksi.KELUAR),
    LAINNYA("Lainnya", JenisTransaksi.KELUAR);

    private final String label;
    private final JenisTransaksi jenis;

    Kategori(String label, JenisTransaksi jenis) {
        this.label = label;
        this.jenis = jenis;
    }

    public String getLabel() {
        return label;
    }

    public JenisTransaksi getJenis() {
        return jenis;
    }

    /**
     * Mengambil seluruh kategori yang sesuai dengan jenis transaksi tertentu.
     */
    public static List<Kategori> berdasarkanJenis(JenisTransaksi jenis) {
        List<Kategori> hasil = new ArrayList<>();
        for (Kategori kategori : values()) {
            if (kategori.jenis == jenis) {
                hasil.add(kategori);
            }
        }
        return hasil;
    }

    @Override
    public String toString() {
        return label;
    }
}
