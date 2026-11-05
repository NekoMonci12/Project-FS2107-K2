package org.yuemi.fsi2107.ui;

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

/**
 * Panel ringkasan berisi tiga kartu: saldo, total uang masuk, dan total uang keluar.
 */
public class RingkasanPanel extends JPanel {

    private static final Color WARNA_MASUK = new Color(0x2E7D32);
    private static final Color WARNA_KELUAR = new Color(0xC62828);
    private static final Color WARNA_NETRAL = new Color(0x37474F);

    private final JLabel nilaiSaldo = buatNilaiLabel();
    private final JLabel nilaiMasuk = buatNilaiLabel();
    private final JLabel nilaiKeluar = buatNilaiLabel();
    private final JLabel infoJumlah = new JLabel("Belum ada transaksi");

    public RingkasanPanel() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 6, 14));
        setOpaque(false);

        JPanel barisKartu = new JPanel(new GridLayout(1, 3, 12, 0));
        barisKartu.setOpaque(false);
        barisKartu.add(buatKartu("SALDO SAAT INI", nilaiSaldo, WARNA_NETRAL));
        barisKartu.add(buatKartu("TOTAL UANG MASUK", nilaiMasuk, WARNA_MASUK));
        barisKartu.add(buatKartu("TOTAL UANG KELUAR", nilaiKeluar, WARNA_KELUAR));

        infoJumlah.setFont(infoJumlah.getFont().deriveFont(Font.PLAIN, 12f));
        infoJumlah.setForeground(new Color(0x607D8B));
        infoJumlah.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));

        add(barisKartu, BorderLayout.CENTER);
        add(infoJumlah, BorderLayout.SOUTH);
    }

    private static JLabel buatNilaiLabel() {
        JLabel label = new JLabel("Rp0");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 24f));
        label.setForeground(WARNA_NETRAL);
        return label;
    }

    private static JPanel buatKartu(String judul, JLabel nilai, Color warnaJudul) {
        JPanel kartu = new JPanel();
        kartu.setLayout(new BoxLayout(kartu, BoxLayout.Y_AXIS));
        kartu.setBackground(Color.WHITE);
        kartu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xCFD8DC)),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)));

        JLabel labelJudul = new JLabel(judul);
        labelJudul.setFont(labelJudul.getFont().deriveFont(Font.BOLD, 11f));
        labelJudul.setForeground(warnaJudul);

        kartu.add(labelJudul);
        kartu.add(Box.createRigidArea(new Dimension(0, 6)));
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

        int jumlah = bukuKas.getJumlahTransaksi();
        infoJumlah.setText(jumlah == 0
                ? "Belum ada transaksi"
                : "Total " + jumlah + " transaksi tercatat");
    }
}
