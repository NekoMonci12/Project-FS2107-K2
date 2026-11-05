package org.yuemi.fsi2107.model;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Objects;

/**
 * Satu catatan transaksi keuangan: uang masuk atau uang keluar.
 */
public class Transaksi {

    private static final Locale LOKAL = Locale.of("id", "ID");
    private static final DateTimeFormatter FORMAT_WAKTU =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", LOKAL);
    private static final DecimalFormat FORMAT_UANG = buatFormatUang();

    private final long id;
    private final LocalDateTime waktu;
    private final JenisTransaksi jenis;
    private final Kategori kategori;
    private final String deskripsi;
    private final long nominal;

    public Transaksi(long id, JenisTransaksi jenis, Kategori kategori, String deskripsi, long nominal) {
        this(id, LocalDateTime.now(), jenis, kategori, deskripsi, nominal);
    }

    public Transaksi(long id, LocalDateTime waktu, JenisTransaksi jenis,
                     Kategori kategori, String deskripsi, long nominal) {
        this.id = id;
        this.waktu = Objects.requireNonNull(waktu, "waktu tidak boleh null");
        this.jenis = Objects.requireNonNull(jenis, "jenis tidak boleh null");
        this.kategori = Objects.requireNonNull(kategori, "kategori tidak boleh null");
        this.deskripsi = deskripsi == null ? "" : deskripsi.trim();
        this.nominal = nominal;
    }

    private static DecimalFormat buatFormatUang() {
        DecimalFormatSymbols simbol = new DecimalFormatSymbols(LOKAL);
        simbol.setGroupingSeparator('.');
        simbol.setDecimalSeparator(',');
        DecimalFormat format = new DecimalFormat("#,##0", simbol);
        format.setPositivePrefix("Rp");
        format.setNegativePrefix("-Rp");
        format.setNegativeSuffix("");
        return format;
    }

    public long getId() {
        return id;
    }

    public LocalDateTime getWaktu() {
        return waktu;
    }

    public JenisTransaksi getJenis() {
        return jenis;
    }

    public Kategori getKategori() {
        return kategori;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public long getNominal() {
        return nominal;
    }

    public boolean isMasuk() {
        return jenis == JenisTransaksi.MASUK;
    }

    /**
     * Nilai nominal yang sudah diberi tanda: positif untuk uang masuk,
     * negatif untuk uang keluar.
     */
    public long getNominalBertanda() {
        return isMasuk() ? nominal : -nominal;
    }

    public String getWaktuTerformat() {
        return waktu.format(FORMAT_WAKTU);
    }

    public String getNominalTerformat() {
        return formatNominal(nominal);
    }

    public static String formatNominal(long nominal) {
        synchronized (FORMAT_UANG) {
            return FORMAT_UANG.format(nominal);
        }
    }

    @Override
    public String toString() {
        return "%s - %s (%s)".formatted(jenis.getLabel(), kategori.getLabel(), getNominalTerformat());
    }
}
