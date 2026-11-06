package org.yuemi.fsi2107.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Profil khusus untuk pembukuan usaha, lengkap dengan tarif PPh dan PPN.
 *
 * <p>Tarif disimpan dalam satuan persen, misalnya {@code 0.5} berarti 0,5%.
 * Perkiraan pajak dihitung dari total uang masuk profil, yaitu dasar
 * penyerahan yang menjadi objek PPN dan PPh final UMKM.</p>
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

    /**
     * Perkiraan PPN dari total uang masuk profil.
     */
    public long hitungPpn(long totalMasuk) {
        return hitungPajak(totalMasuk, tarifPpn);
    }

    /**
     * Perkiraan PPh dari total uang masuk profil.
     */
    public long hitungPph(long totalMasuk) {
        return hitungPajak(totalMasuk, tarifPph);
    }

    @Override
    public List<String> infoTambahan(long totalMasuk, long totalKeluar) {
        return List.of(
                "PPN " + formatPersen(tarifPpn) + ": " + Transaksi.formatNominal(hitungPpn(totalMasuk)),
                "PPh " + formatPersen(tarifPph) + ": " + Transaksi.formatNominal(hitungPph(totalMasuk)));
    }

    /**
     * Menghitung nilai pajak dalam rupiah penuh lalu dibulatkan ke rupiah terdekat.
     */
    private static long hitungPajak(long totalMasuk, double tarif) {
        return Math.round(totalMasuk * tarif / 100.0);
    }

    /**
     * Menulis tarif persen dengan pemisah desimal koma, misalnya {@code 11,00}.
     */
    private static String formatPersen(double tarif) {
        return String.format(Locale.of("id", "ID"), "%.2f%%", tarif);
    }
}
