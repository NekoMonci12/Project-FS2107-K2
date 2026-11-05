package org.yuemi.fsi2107.ui;

import org.yuemi.fsi2107.service.BukuKas;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

/**
 * Jendela utama aplikasi: menggabungkan ringkasan, formulir input, dan tabel transaksi.
 */
public class MainFrame extends JFrame {

    private final BukuKas bukuKas;
    private final RingkasanPanel ringkasanPanel;
    private final TabelTransaksiPanel tabelTransaksiPanel;

    public MainFrame(BukuKas bukuKas) {
        super("Aplikasi Pencatatan Keuangan - Uang Masuk & Uang Keluar");
        this.bukuKas = bukuKas;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(null);

        ringkasanPanel = new RingkasanPanel();
        tabelTransaksiPanel = new TabelTransaksiPanel(bukuKas);
        FormTransaksiPanel formTransaksiPanel = new FormTransaksiPanel(bukuKas);

        JSplitPane pemisah = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formTransaksiPanel, tabelTransaksiPanel);
        pemisah.setResizeWeight(0.0);
        pemisah.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        pemisah.setDividerLocation(210);
        pemisah.setContinuousLayout(true);

        JPanel isi = new JPanel(new BorderLayout());
        isi.setBackground(new Color(0xF5F7F8));
        isi.add(ringkasanPanel, BorderLayout.NORTH);
        isi.add(pemisah, BorderLayout.CENTER);

        setContentPane(isi);
        setSize(920, 640);

        bukuKas.tambahPendengar(this::segarkanTampilan);
        segarkanTampilan(bukuKas);
    }

    private void segarkanTampilan(BukuKas bukuKas) {
        ringkasanPanel.perbarui(bukuKas);
        tabelTransaksiPanel.perbarui(bukuKas);
    }

    /**
     * Menjalankan aplikasi pada event dispatch thread dengan tampilan bawaan sistem.
     */
    public static void jalankan(BukuKas bukuKas) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
                // Tampilan bawaan Java tetap dipakai bila tema sistem tidak tersedia.
            }
            new MainFrame(bukuKas).setVisible(true);
        });
    }
}
