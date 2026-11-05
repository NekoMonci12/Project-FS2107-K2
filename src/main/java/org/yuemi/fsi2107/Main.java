package org.yuemi.fsi2107;

import org.yuemi.fsi2107.service.BukuKas;
import org.yuemi.fsi2107.ui.MainFrame;

/**
 * Titik masuk aplikasi pencatatan keuangan.
 *
 * <p>Seluruh data hanya disimpan di memori selama aplikasi berjalan; tidak ada
 * penyimpanan ke basis data maupun berkas.</p>
 */
public class Main {

    public static void main(String[] args) {
        BukuKas bukuKas = new BukuKas();
        MainFrame.jalankan(bukuKas);
    }
}
