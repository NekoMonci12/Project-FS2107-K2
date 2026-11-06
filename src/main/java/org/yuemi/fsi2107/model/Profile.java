package org.yuemi.fsi2107.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Profil pengguna.Dibuat sebagai abstract class agar dapat diturunkan menjadi
 * profil khusus, misalnya profil usaha dengan setelan PPh dan PPN.
 *
 * <p>Bagian yang dimiliki seluruh profil — identitas, nama, catatan, dan waktu
 * pembuatan — disimpan di kelas ini. Tiap turunan menambah state dan perilaku
 * khususnya sendiri.</p>
 */
public abstract class Profile {

    private final long id;
    private final String nama;
    private final String catatan;
    private final LocalDateTime dibuatPada;

    protected Profile(long id, String nama, String catatan, LocalDateTime dibuatPada) {
        this.id = id;
        this.nama = Objects.requireNonNull(nama, "nama profil tidak boleh null");
        this.catatan = catatan == null ? "" : catatan.trim();
        this.dibuatPada = Objects.requireNonNull(dibuatPada, "waktu pembuatan profil tidak boleh null");
    }

    /**
     * Diskriminator profil yang disimpan di basis data. Nilai ini dipakai
     * {@link PabrikProfile} untuk membentuk subclass yang sesuai saat profil
     * dibaca kembali dari penyimpanan.
     */
    public abstract String getTipe();

    /**
     * Baris informasi tambahan yang ditampilkan pada ringkasan, misalnya
     * catatan profil dan setelan pajak yang berlaku. Profil yang tidak punya
     * informasi khusus tidak menambahkan apa pun.
     *
     * @return daftar baris informasi, kosong bila tidak ada
     */
    public List<String> infoTambahan() {
        if (catatan.isEmpty()) {
            return List.of();
        }
        return List.of("Catatan: " + catatan);
    }

    /**
     * Keterangan singkat setelan pajak profil, dipakai pada formulir transaksi.
     * Profil tanpa setelan pajak mengembalikan teks kosong.
     */
    public String keteranganTarif() {
        return "";
    }

    /**
     * Menandai apakah profil ini memungut pajak pada transaksinya. Tampilan
     * memakai penanda ini untuk menyembunyikan kolom serta kartu pajak yang
     * tidak berlaku bagi profil tersebut.
     *
     * @return {@code true} bila profil memungut PPh/PPN
     */
    public boolean punyaPajak() {
        return false;
    }

    /**
     * Menghitung pajak yang berlaku pada satu transaksi.
     *
     * <p>Profil dasar tidak menetapkan tarif sehingga setiap transaksinya
     * bebas pajak. Profil khusus menimpa method ini untuk memotong PPh dari
     * uang masuk serta menambah PPN pada uang keluar.</p>
     *
     * @param jenis   jenis transaksi
     * @param nominal nominal sebelum pajak
     * @return nilai pajak dan nominal bersih transaksi
     */
    public PajakTransaksi hitungPajak(JenisTransaksi jenis, long nominal) {
        return PajakTransaksi.tanpaPajak(nominal);
    }

    public long getId() {
        return id;
    }

    public String getNama() {
        return nama;
    }

    public String getCatatan() {
        return catatan;
    }

    public LocalDateTime getDibuatPada() {
        return dibuatPada;
    }

    /**
     * Dua profil dianggap sama bila berasal dari kelas yang sama dan memiliki
     * id yang sama. Profil yang belum tersimpan (id belum diperoleh) memakai
     * identitas objek.
     */
    @Override
    public boolean equals(Object objekLain) {
        if (this == objekLain) {
            return true;
        }
        if (objekLain == null || getClass() != objekLain.getClass()) {
            return false;
        }
        Profile lain = (Profile) objekLain;
        if (id <= 0 || lain.id <= 0) {
            return false;
        }
        return id == lain.id;
    }

    @Override
    public int hashCode() {
        return id > 0 ? Long.hashCode(id) : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return nama;
    }
}
