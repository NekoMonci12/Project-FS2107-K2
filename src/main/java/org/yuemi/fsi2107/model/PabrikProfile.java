package org.yuemi.fsi2107.model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Pabrik pembuatan objek profil dari tipe profil yang tersimpan.
 *
 * <p>Pabrik ini menjadi titik pendaftaran tipe profil. Menambah profil khusus
 * cukup dengan membuat subclass {@link Profile} lalu mendaftarkan tipenya di
 * sini, tanpa mengubah lapisan penyimpanan maupun antarmuka pengguna.</p>
 */
public final class PabrikProfile {

    private static final Map<String, Function<DataProfile, Profile>> PEMBUAT = new LinkedHashMap<>();

    static {
        daftar(ProfileUmum.TIPE, data -> new ProfileUmum(
                data.id(), data.nama(), data.catatan(), data.dibuatPada()));
        daftar(ProfileUsaha.TIPE, data -> new ProfileUsaha(
                data.id(), data.nama(), data.catatan(), data.dibuatPada(),
                tarifWajib(data.tarifPph(), "PPh"), tarifWajib(data.tarifPpn(), "PPN")));
    }

    private PabrikProfile() {
    }

    /**
     * Mendaftarkan pembuat profil untuk suatu tipe.
     *
     * @param tipe     diskriminator tipe profil
     * @param pembuat  fungsi membentuk profil dari data mentah
     */
    public static synchronized void daftar(String tipe, Function<DataProfile, Profile> pembuat) {
        if (tipe == null || tipe.isBlank()) {
            throw new IllegalArgumentException("Tipe profil tidak boleh kosong.");
        }
        if (pembuat == null) {
            throw new IllegalArgumentException("Pembuat profil tidak boleh null.");
        }
        PEMBUAT.put(tipe.trim().toUpperCase(java.util.Locale.ROOT), pembuat);
    }

    /**
     * Membentuk profil sesuai tipenya.
     *
     * @throws IllegalArgumentException bila tipenya tidak terdaftar
     */
    public static Profile buat(DataProfile data) {
        if (data == null) {
            throw new IllegalArgumentException("Data profil tidak boleh null.");
        }
        String tipe = data.tipe() == null ? "" : data.tipe().trim().toUpperCase(java.util.Locale.ROOT);
        Function<DataProfile, Profile> pembuat = PEMBUAT.get(tipe);
        if (pembuat == null) {
            throw new IllegalArgumentException(
                    "Tipe profil '" + data.tipe() + "' tidak dikenali. Tipe tersedia: " + tipeTersedia());
        }
        return pembuat.apply(data);
    }

    /**
     * Membuat profil umum dari parameter biasa.
     */
    public static Profile buatUmum(long id, String nama, String catatan, LocalDateTime dibuatPada) {
        return buat(new DataProfile(ProfileUmum.TIPE, id, nama, catatan, dibuatPada, null, null));
    }

    /**
     * Daftar tipe profil yang terdaftar di pabrik ini.
     */
    public static synchronized List<String> tipeTersedia() {
        return List.copyOf(PEMBUAT.keySet());
    }

    /**
     * Memastikan tarif pajak tersedia untuk tipe profil yang mewajibkannya.
     */
    private static double tarifWajib(Double tarif, String namaPajak) {
        if (tarif == null) {
            throw new IllegalArgumentException("Tarif " + namaPajak + " wajib diisi untuk profil usaha.");
        }
        return tarif;
    }
}
