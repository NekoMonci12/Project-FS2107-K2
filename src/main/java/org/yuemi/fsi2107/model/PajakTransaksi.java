package org.yuemi.fsi2107.model;

/**
 * Hasil perhitungan pajak untuk satu transaksi.
 *
 * <p>Nilai {@code nominalBersih} adalah nominal yang benar-benar berpindah
 * setelah pajak diperhitungkan. Untuk uang masuk nilai bersih lebih kecil
 * karena PPh dipotong, sedangkan untuk uang keluar nilai bersih lebih besar
 * karena PPN menambah pengeluaran.</p>
 *
 * @param pph           PPh yang dipotong dari transaksi, dalam rupiah
 * @param ppn           PPN yang dipotong atau ditambahkan, dalam rupiah
 * @param nominalBersih nominal akhir yang dipakai untuk menghitung saldo
 */
public record PajakTransaksi(long pph, long ppn, long nominalBersih) {

    /**
     * Transaksi tanpa pajak, dipakai profil yang tidak menetapkan tarif.
     */
    public static PajakTransaksi tanpaPajak(long nominal) {
        return new PajakTransaksi(0, 0, nominal);
    }
}
