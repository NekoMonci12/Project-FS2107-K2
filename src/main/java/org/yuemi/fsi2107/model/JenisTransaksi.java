package org.yuemi.fsi2107.model;

/**
 * Jenis transaksi keuangan yang dicatat oleh aplikasi.
 */
public enum JenisTransaksi {

    MASUK("Uang Masuk"),
    KELUAR("Uang Keluar");

    private final String label;

    JenisTransaksi(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        return label;
    }
}
