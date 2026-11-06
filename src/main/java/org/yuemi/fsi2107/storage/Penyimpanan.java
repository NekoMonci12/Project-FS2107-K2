package org.yuemi.fsi2107.storage;

import org.yuemi.fsi2107.model.JenisTransaksi;
import org.yuemi.fsi2107.model.Kategori;
import org.yuemi.fsi2107.model.Transaksi;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Kontrak lapisan penyimpanan data transaksi.
 *
 * <p>Perintah SQL berbeda antar basis data, namun seluruh aplikasi
 * hanya mengenal antarmuka ini sehingga berganti backend tidak menyentuh
 * lapisan layanan maupun antarmuka pengguna.</p>
 */
public interface Penyimpanan extends AutoCloseable {

    /**
     * Menyiapkan struktur penyimpanan, misalnya membuat tabel bila belum ada.
     */
    void siapkan();

    /**
     * Membaca seluruh transaksi yang tersimpan.
     */
    List<Transaksi> muatSemua();

    /**
     * Menyimpan satu transaksi baru dan mengembalikan objek transaksi
     * beserta id yang diberikan basis data.
     */
    Transaksi simpan(JenisTransaksi jenis, Kategori kategori, String deskripsi,
                     long nominal, LocalDateTime waktu);

    /**
     * Menghapus transaksi berdasarkan id. Mengembalikan {@code true} bila
     * ada transaksi yang terhapus.
     */
    boolean hapus(long id);

    /**
     * Melepaskan seluruh sumber daya yang dipakai penyimpanan.
     */
    @Override
    void close();
}
