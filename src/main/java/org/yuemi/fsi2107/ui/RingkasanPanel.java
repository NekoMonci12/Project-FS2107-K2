package org.yuemi.fsi2107.ui;

import org.yuemi.fsi2107.model.Profile;
import org.yuemi.fsi2107.model.Transaksi;
import org.yuemi.fsi2107.service.BukuKas;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

/**
 * Panel ringkasan berisi kartu saldo, total uang masuk, total uang keluar,
 * serta total PPN dan PPh yang terkumpul pada profil aktif.
 */
public class RingkasanPanel extends JPanel {

    private static final Color WARNA_MASUK = new Color(0x2E7D32);
    private static final Color WARNA_KELUAR = new Color(0xC62828);
    private static final Color WARNA_NETRAL = new Color(0x37474F);
    private static final Color WARNA_PAJAK = new Color(0xEF6C00);

    private final JLabel nilaiSaldo = buatNilaiLabel();
    private final JLabel nilaiMasuk = buatNilaiLabel();
    private final JLabel nilaiKeluar = buatNilaiLabel();
    private final JLabel nilaiPpn = buatNilaiLabel();
    private final JLabel nilaiPph = buatNilaiLabel();
    private final JLabel infoJumlah = new JLabel("Belum ada transaksi");
    private final JLabel infoProfil = new JLabel(" ");

    public RingkasanPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));
        setOpaque(false);

        JPanel barisKartu = new JPanel(new GridLayout(2, 3, 12, 8));
        barisKartu.setOpaque(false);
        barisKartu.add(buatKartu("SALDO SAAT INI", nilaiSaldo, WARNA_NETRAL));
        barisKartu.add(buatKartu("TOTAL UANG MASUK", nilaiMasuk, WARNA_MASUK));
        barisKartu.add(buatKartu("TOTAL UANG KELUAR", nilaiKeluar, WARNA_KELUAR));
        barisKartu.add(buatKartu("TOTAL PPN TERKUMPUL", nilaiPpn, WARNA_PAJAK));
        barisKartu.add(buatKartu("TOTAL PPH TERKUMPUL", nilaiPph, WARNA_PAJAK));

        infoJumlah.setFont(infoJumlah.getFont().deriveFont(Font.PLAIN, 12f));
        infoJumlah.setForeground(new Color(0x607D8B));

        infoProfil.setFont(infoProfil.getFont().deriveFont(Font.PLAIN, 12f));
        infoProfil.setForeground(new Color(0x37474F));

        // Sel kosong pada baris kedua dipakai untuk keterangan jumlah transaksi dan profil.
        JPanel selKeterangan = new JPanel(new BorderLayout(0, 2));
        selKeterangan.setOpaque(false);
        selKeterangan.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        selKeterangan.add(infoJumlah, BorderLayout.NORTH);
        selKeterangan.add(infoProfil, BorderLayout.SOUTH);
        barisKartu.add(selKeterangan);

        add(barisKartu, BorderLayout.CENTER);
    }

    private static JLabel buatNilaiLabel() {
        JLabel label = new JLabel("Rp0");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 18f));
        label.setForeground(WARNA_NETRAL);
        return label;
    }

    private static JPanel buatKartu(String judul, JLabel nilai, Color warnaJudul) {
        JPanel kartu = new JPanel();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCFD8DC)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)));

        JLabel labelJudul = new JLabel(judul);
        labelJudul.setFont(labelJudul.getFont().deriveFont(Font.BOLD, 11f));
        labelJudul.setForeground(warnaJudul);

        kartu.add(labelJudul);
        kartu.add(Box.createRigidArea(new Dimension(0, 4)));
        kartu.add(nilai);
        return kartu;
    }

    /**
     * Menyesuaikan seluruh tampilan ringkasan dengan kondisi buku kas terbaru.
     */
    public void perbarui(BukuKas bukuKas) {
        long saldo = bukuKas.getSaldo();
        nilaiSaldo.setText(Transaksi.formatNominal(saldo));
        nilaiSaldo.setForeground(saldo < 0 ? WARNA_KELUAR : WARNA_MASUK);

        nilaiMasuk.setText(Transaksi.formatNominal(bukuKas.getTotalMasuk()));
        nilaiKeluar.setText(Transaksi.formatNominal(bukuKas.getTotalKeluar()));

        nilaiPpn.setText(Transaksi.formatNominal(bukuKas.getTotalPpn()));
        nilaiPph.setText(Transaksi.formatNominal(bukuKas.getTotalPph()));

        int jumlah = bukuKas.getJumlahTransaksi();
        infoJumlah.setText(jumlah == 0
                ? "Belum ada transaksi"
                : "Total " + jumlah + " transaksi tercatat");

        perbaruiInfoProfil(bukuKas.getProfilAktif());
    }

    /**
     * Menampilkan informasi tambahan milik profil aktif, misalnya catatan profil
     * dan setelan pajak yang berlaku. Profil tanpa informasi khusus menampilkan
     * baris kosong.
     */
    private void perbaruiInfoProfil(Profile profil) {
        if (profil == null) {
            infoProfil.setText(" ");
            return;
        }
        List<String> baris = profil.infoTambahan();
        infoProfil.setText(baris.isEmpty() ? " " : String.join("   |   ", baris));
    }
}
