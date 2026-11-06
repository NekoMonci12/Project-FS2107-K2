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
    private final long profileId;
    private final LocalDateTime waktu;
    private final JenisTransaksi jenis;
    private final Kategori kategori;
    private final String deskripsi;
    private final long nominal;
    private final long pph;
    private final long ppn;

    public Transaksi(long id, long profileId, JenisTransaksi jenis, Kategori kategori,
                     String deskripsi, long nominal) {
        this(id, profileId, LocalDateTime.now(), jenis, kategori, deskripsi, nominal, 0, 0);
    }

    public Transaksi(long id, long profileId, JenisTransaksi jenis, Kategori kategori,
                     String deskripsi, long nominal, long pph, long ppn) {
        this(id, profileId, LocalDateTime.now(), jenis, kategori, deskripsi, nominal, pph, ppn);
    }

    public Transaksi(long id, long profileId, LocalDateTime waktu, JenisTransaksi jenis,
                     Kategori kategori, String deskripsi, long nominal) {
        this(id, profileId, waktu, jenis, kategori, deskripsi, nominal, 0, 0);
    }

    public Transaksi(long id, long profileId, LocalDateTime waktu, JenisTransaksi jenis,
                     Kategori kategori, String deskripsi, long nominal, long pph, long ppn) {
        this.id = id;
        this.profileId = profileId;
        this.waktu = Objects.requireNonNull(waktu, "waktu tidak boleh null");
        this.jenis = Objects.requireNonNull(jenis, "jenis tidak boleh null");
        this.kategori = Objects.requireNonNull(kategori, "kategori tidak boleh null");
        this.deskripsi = deskripsi == null ? "" : deskripsi.trim();
        this.nominal = nominal;
        this.pph = pph;
        this.ppn = ppn;
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

    /**
     * Identitas profil pemilik transaksi ini.
     */
    public long getProfileId() {
        return profileId;
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

    /**
     * PPh yang dipotong dari transaksi ini, dalam rupiah.
     */
    public long getPph() {
        return pph;
    }

    /**
     * PPN yang ditambahkan pada pengeluaran, dalam rupiah.
     */
    public long getPpn() {
        return ppn;
    }

    /**
     * Nominal setelah pajak diperhitungkan. Uang masuk berkurang karena PPh
     * dipotong, uang keluar bertambah karena PPN menambah pengeluaran.
     */
    public long getNominalBersih() {
        return isMasuk() ? nominal - pph : nominal + ppn;
    }

    public boolean isMasuk() {
        return jenis == JenisTransaksi.MASUK;
    }

    /**
     * Nilai nominal bersih yang sudah diberi tanda: positif untuk uang masuk,
     * negatif untuk uang keluar.
     */
    public long getNominalBertanda() {
        return isMasuk() ? getNominalBersih() : -getNominalBersih();
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
