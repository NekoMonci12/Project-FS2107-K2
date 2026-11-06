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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(760, 560));
        setLocationRelativeTo(null);

        ringkasanPanel = new RingkasanPanel();
        tabelTransaksiPanel = new TabelTransaksiPanel(bukuKas);
        FormTransaksiPanel formTransaksiPanel = new FormTransaksiPanel(bukuKas);

        JSplitPane pemisah = new JSplitPane(JSplitPane.VERTICAL_SPLIT, formTransaksiPanel, tabelTransaksiPanel);
        pemisah.setResizeWeight(0.0);
        pemisah.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        pemisah.setContinuousLayout(true);

        JPanel isi = new JPanel(new BorderLayout());
        isi.setBackground(new Color(0xF5F7F8));
        isi.add(ringkasanPanel, BorderLayout.NORTH);
        isi.add(pemisah, BorderLayout.CENTER);

        setContentPane(isi);
        setSize(920, 640);

        pemisah.setDividerLocation(Math.max(180, formTransaksiPanel.getPreferredSize().height));

        // Divider dihitung ulang setelah jendela tampil agar tinggi formulir pas dengan isinya.
        SwingUtilities.invokeLater(() -> pemisah.setDividerLocation(
                Math.max(180, formTransaksiPanel.getPreferredSize().height)));

        bukuKas.tambahPendengar(this::segarkanTampilan);
        segarkanTampilan(bukuKas);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent peristiwa) {
                tutupAplikasi();
            }
        });
    }

    private void segarkanTampilan(BukuKas bukuKas) {
        ringkasanPanel.perbarui(bukuKas);
        tabelTransaksiPanel.perbarui(bukuKas);
    }

    /**
     * Menutup jendela, menutup lapisan penyimpanan data, lalu mengakhiri aplikasi.
     */
    private void tutupAplikasi() {
        try {
            bukuKas.tutup();
        } finally {
            dispose();
            System.exit(0);
        }
    }

    /**
     * Menyusun dan menampilkan jendela utama pada event dispatch thread
     * memakai tampilan bawaan sistem.
     */
    public static void jalankan(BukuKas bukuKas) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {
                    // Tampilan bawaan Java tetap dipakai bila tema sistem tidak tersedia.
                }
                new MainFrame(bukuKas).setVisible(true);
            });
        } catch (Exception ex) {
            throw new IllegalStateException("Gagal menjalankan jendela utama aplikasi.", ex);
        }
    }
}
