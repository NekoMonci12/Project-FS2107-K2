package org.yuemi.fsi2107.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Profil khusus untuk pembukuan usaha, lengkap dengan tarif PPh dan PPN.
 *
 * <p>Tarif disimpan dalam satuan persen, misalnya {@code 0.5} berarti 0,5%.
 * Setiap transaksi profil ini dipungut pajak: PPh dipotong dari uang masuk
 * sedangkan PPN ditambahkan pada uang keluar.</p>
 */
public final class ProfileUsaha extends Profile {

    /** Nilai diskriminator profil usaha. */
    public static final String TIPE = "USAHA";

    private final double tarifPph;
    private final double tarifPpn;

    /**
     * @param tarifPph tarif PPh dalam persen
     * @param tarifPpn tarif PPN dalam persen
     */
    public ProfileUsaha(long id, String nama, String catatan, LocalDateTime dibuatPada,
                        double tarifPph, double tarifPpn) {
        super(id, nama, catatan, dibuatPada);
        this.tarifPph = tarifPph;
        this.tarifPpn = tarifPpn;
    }

    public double getTarifPph() {
        return tarifPph;
    }

    public double getTarifPpn() {
        return tarifPpn;
    }

    @Override
    public String getTipe() {
        return TIPE;
    }

    @Override
    public boolean punyaPajak() {
        return true;
    }

    /**
     * Menghitung pajak yang berlaku pada transaksi profil usaha.
     *
     * <p>PPh hanya berlaku pada uang masuk dan dipotong dari pemasukan,
     * sedangkan PPN hanya berlaku pada uang keluar dan menambah pengeluaran.</p>
     */
    @Override
    public PajakTransaksi hitungPajak(JenisTransaksi jenis, long nominal) {
        if (jenis == JenisTransaksi.KELUAR) {
            long ppn = hitungPajakNominal(nominal, tarifPpn);
            return new PajakTransaksi(0, ppn, nominal + ppn);
        }
        long pph = hitungPajakNominal(nominal, tarifPph);
        return new PajakTransaksi(pph, 0, nominal - pph);
    }

    @Override
    public String keteranganTarif() {
        return "PPh " + formatPersen(tarifPph) + " (masuk), PPN "
                + formatPersen(tarifPpn) + " (keluar)";
    }

    @Override
    public List<String> infoTambahan() {
        List<String> baris = new ArrayList<>(super.infoTambahan());
        baris.add("PPh " + formatPersen(tarifPph) + " dipotong dari uang masuk, PPN "
                + formatPersen(tarifPpn) + " ditambahkan pada uang keluar.");
        return List.copyOf(baris);
    }

    /**
     * Menghitung nilai pajak dalam rupiah penuh lalu dibulatkan ke rupiah terdekat.
     */
    private static long hitungPajakNominal(long nominal, double tarif) {
        return Math.round(nominal * tarif / 100.0);
    }

    /**
     * Menulis tarif persen dengan pemisah desimal koma, misalnya {@code 11,00}.
     */
    private static String formatPersen(double tarif) {
        return String.format(Locale.of("id", "ID"), "%.2f%%", tarif);
    }
}
