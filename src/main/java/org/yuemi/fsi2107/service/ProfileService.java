package org.yuemi.fsi2107.service;

import org.yuemi.fsi2107.model.Profile;
import org.yuemi.fsi2107.model.ProfileUmum;
import org.yuemi.fsi2107.model.ProfileUsaha;
import org.yuemi.fsi2107.storage.Penyimpanan;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Layanan profil: menambah, menghapus, dan memilih profil yang sedang aktif.
 *
 * <p>Setiap profil memiliki catatan transaksinya sendiri, sehingga memilih
 * profil lain berarti seluruh tampilan aplikasi menyegarkan datanya.</p>
 */
public class ProfileService {

    private static final int PANJANG_NAMA_MAKSIMAL = 50;

    private final List<Profile> daftarProfile = new ArrayList<>();
    private final List<Consumer<ProfileService>> pendengar = new ArrayList<>();
    private final Penyimpanan penyimpanan;

    private Profile profilAktif;

    /**
     * Menyusun layanan profil dan memuat seluruh profil yang tersimpan.
     *
     * @param penyimpanan lapisan penyimpanan data
     */
    public ProfileService(Penyimpanan penyimpanan) {
        this.penyimpanan = Objects.requireNonNull(penyimpanan, "penyimpanan tidak boleh null");
        this.penyimpanan.siapkan();
        muatDariPenyimpanan();
        if (profilAktif == null && !daftarProfile.isEmpty()) {
            profilAktif = daftarProfile.get(0);
        }
    }

    /**
     * Menambah profil biasa.
     *
     * @param nama    nama profil
     * @param catatan catatan bebas profil
     * @return profil yang tersimpan
     */
    public Profile tambahProfileUmum(String nama, String catatan) {
        String namaBersih = periksaNama(nama);
        Profile profile = new ProfileUmum(0, namaBersih, catatan == null ? "" : catatan, LocalDateTime.now());
        Profile tersimpan = penyimpanan.simpanProfile(profile);
        daftarProfile.add(tersimpan);
        if (profilAktif == null) {
            profilAktif = tersimpan;
        }
        beriTahuPendengar();
        return tersimpan;
    }

    /**
     * Menambah profil usaha lengkap dengan tarif PPh dan PPN.
     *
     * @param nama      nama profil
     * @param catatan   catatan bebas profil
     * @param tarifPph  tarif PPh dalam persen
     * @param tarifPpn  tarif PPN dalam persen
     * @return profil yang tersimpan
     */
    public Profile tambahProfileUsaha(String nama, String catatan, double tarifPph, double tarifPpn) {
        String namaBersih = periksaNama(nama);
        periksaTarif(tarifPph, "PPh");
        periksaTarif(tarifPpn, "PPN");
        Profile profile = new ProfileUsaha(0, namaBersih, catatan == null ? "" : catatan,
                LocalDateTime.now(), tarifPph, tarifPpn);
        Profile tersimpan = penyimpanan.simpanProfile(profile);
        daftarProfile.add(tersimpan);
        if (profilAktif == null) {
            profilAktif = tersimpan;
        }
        beriTahuPendengar();
        return tersimpan;
    }

    /**
     * Menghapus profil beserta seluruh transaksinya.
     *
     * @param id identitas profil
     * @return {@code true} bila profil terhapus
     */
    public boolean hapusProfile(long id) {
        if (!penyimpanan.hapusProfile(id)) {
            return false;
        }
        daftarProfile.removeIf(profile -> profile.getId() == id);

        if (profilAktif != null && profilAktif.getId() == id) {
            profilAktif = daftarProfile.isEmpty() ? buatProfileBawaan() : daftarProfile.get(0);
        }
        beriTahuPendengar();
        return true;
    }

    /**
     * Memilih profil yang sedang aktif.
     */
    public void setProfilAktif(Profile profile) {
        if (profile == null) {
            return;
        }
        if (profilAktif != null && profilAktif.getId() == profile.getId()) {
            return;
        }
        profilAktif = profile;
        beriTahuPendengar();
    }

    public Profile getProfilAktif() {
        return profilAktif;
    }

    public List<Profile> getDaftarProfile() {
        return Collections.unmodifiableList(daftarProfile);
    }

    /**
     * Mendaftarkan pendengar yang diberi tahu setiap kali daftar profil
     * atau profil aktif berubah.
     */
    public void tambahPendengar(Consumer<ProfileService> pendengarBaru) {
        pendengar.add(Objects.requireNonNull(pendengarBaru));
    }

    /**
     * Memuat daftar profil dari penyimpanan.
     */
    private void muatDariPenyimpanan() {
        daftarProfile.clear();
        daftarProfile.addAll(penyimpanan.muatSemuaProfile());
    }

    /**
     * Membuat profil bawaan ketika seluruh profil telah dihapus.
     */
    private Profile buatProfileBawaan() {
        Profile profile = new ProfileUmum(0, "Default", "", LocalDateTime.now());
        Profile tersimpan = penyimpanan.simpanProfile(profile);
        daftarProfile.add(tersimpan);
        return tersimpan;
    }

    /**
     * Memeriksa nama profil: wajib diisi, panjangnya terbatas, dan belum dipakai.
     */
    private String periksaNama(String nama) {
        String bersih = nama == null ? "" : nama.trim();
        if (bersih.isEmpty()) {
            throw new IllegalArgumentException("Nama profil tidak boleh kosong.");
        }
        if (bersih.length() > PANJANG_NAMA_MAKSIMAL) {
            throw new IllegalArgumentException(
                    "Nama profil maksimal " + PANJANG_NAMA_MAKSIMAL + " karakter.");
        }
        boolean sudahAda = daftarProfile.stream().anyMatch(profile ->
                profile.getNama().toLowerCase(Locale.ROOT).equals(bersih.toLowerCase(Locale.ROOT)));
        if (sudahAda) {
            throw new IllegalArgumentException("Profil dengan nama '" + bersih + "' sudah ada.");
        }
        return bersih;
    }

    /**
     * Memeriksa nilai tarif pajak berada di rentang yang wajar.
     */
    private static void periksaTarif(double tarif, String namaPajak) {
        if (Double.isNaN(tarif) || tarif < 0 || tarif > 100) {
            throw new IllegalArgumentException(
                    "Tarif " + namaPajak + " harus berupa angka antara 0 sampai 100.");
        }
    }

    private void beriTahuPendengar() {
        for (Consumer<ProfileService> satuPendengar : List.copyOf(pendengar)) {
            satuPendengar.accept(this);
        }
    }
}
