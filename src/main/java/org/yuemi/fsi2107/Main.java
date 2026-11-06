package org.yuemi.fsi2107;

import org.yuemi.fsi2107.service.BukuKas;
import org.yuemi.fsi2107.service.ProfileService;
import org.yuemi.fsi2107.storage.KonfigurasiAplikasi;
import org.yuemi.fsi2107.storage.PabrikPenyimpanan;
import org.yuemi.fsi2107.storage.Penyimpanan;
import org.yuemi.fsi2107.ui.MainFrame;

/**
 * Titik masuk aplikasi pencatatan keuangan.
 *
 * <p>Data transaksi dan profil disimpan pada basis data SQLite secara bawaan,
 * atau pada MySQL bila dipilih melalui {@code config.properties}.</p>
 */
public class Main {

    public static void main(String[] args) {
        KonfigurasiAplikasi konfigurasi = KonfigurasiAplikasi.muat();
        Penyimpanan penyimpanan = PabrikPenyimpanan.create(konfigurasi);
        BukuKas bukuKas = new BukuKas(penyimpanan);
        ProfileService profileService = new ProfileService(penyimpanan);

        Runtime.getRuntime().addShutdownHook(new Thread(bukuKas::tutup, "penutup-penyimpanan"));

        MainFrame.jalankan(bukuKas, profileService);
    }
}
