package org.yuemi.fsi2107.ui;

import org.yuemi.fsi2107.model.JenisTransaksi;
import org.yuemi.fsi2107.model.Kategori;
import org.yuemi.fsi2107.service.BukuKas;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

/**
 * Formulir untuk menambahkan transaksi baru ke buku kas.
 */
public class FormTransaksiPanel extends JPanel {

    private final BukuKas bukuKas;

    private final JComboBox<JenisTransaksi> pilihanJenis = new JComboBox<>(JenisTransaksi.values());
    private final JComboBox<Kategori> pilihanKategori = new JComboBox<>();
    private final JTextField isianNominal = new JTextField();
    private final JTextField isianDeskripsi = new JTextField();

    public FormTransaksiPanel(BukuKas bukuKas) {
        this.bukuKas = bukuKas;

        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(0xCFD8DC)),
                        " Tambah Transaksi "),
                BorderFactory.createEmptyBorder(8, 12, 12, 12)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int baris = 0;
        tambahBaris(gbc, baris++, "Jenis", pilihanJenis, 1.0);
        tambahBaris(gbc, baris++, "Kategori", pilihanKategori, 1.0);
        tambahBaris(gbc, baris++, "Nominal (Rp)", isianNominal, 1.0);
        tambahBaris(gbc, baris++, "Deskripsi", isianDeskripsi, 1.0);

        JButton tombolTambah = new JButton("Tambah Transaksi");
        tombolTambah.setFont(tombolTambah.getFont().deriveFont(Font.BOLD));
        tombolTambah.setBackground(new Color(0x2E7D32));
        tombolTambah.setForeground(Color.WHITE);
        tombolTambah.setFocusPainted(false);
        tombolTambah.addActionListener(e -> simpanTransaksi());

        JButton tombolReset = new JButton("Reset");
        tombolReset.addActionListener(e -> bersihkanForm());

        JPanel panelTombol = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 8, 0));
        panelTombol.setOpaque(false);
        panelTombol.add(tombolTambah);
        panelTombol.add(tombolReset);

        gbc.gridx = 0;
        gbc.gridy = baris;
        gbc.gridwidth = 3;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        add(panelTombol, gbc);

        pilihanJenis.addActionListener(e -> perbaruiPilihanKategori());
        perbaruiPilihanKategori();

        isianNominal.addActionListener(e -> simpanTransaksi());
        isianDeskripsi.addActionListener(e -> simpanTransaksi());
    }

    private void tambahBaris(GridBagConstraints gbc, int baris, String label, java.awt.Component komponen,
                             double bobotX) {
        JLabel labelKomponen = new JLabel(label);
        labelKomponen.setPreferredSize(new java.awt.Dimension(110, 24));

        gbc.gridy = baris;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        add(labelKomponen, gbc);

        gbc.gridx = 1;
        gbc.weightx = bobotX;
        add(komponen, gbc);

        gbc.weightx = 0;
    }

    /**
     * Menyaring daftar kategori agar sesuai dengan jenis transaksi yang sedang dipilih.
     */
    private void perbaruiPilihanKategori() {
        JenisTransaksi jenisTerpilih = (JenisTransaksi) pilihanJenis.getSelectedItem();
        pilihanKategori.removeAllItems();
        if (jenisTerpilih == null) {
            return;
        }
        List<Kategori> daftarKategori = Kategori.berdasarkanJenis(jenisTerpilih);
        for (Kategori kategori : daftarKategori) {
            pilihanKategori.addItem(kategori);
        }
    }

    private void simpanTransaksi() {
        JenisTransaksi jenis = (JenisTransaksi) pilihanJenis.getSelectedItem();
        Kategori kategori = (Kategori) pilihanKategori.getSelectedItem();

        if (jenis == null) {
            tampilkanPeringatan("Silakan pilih jenis transaksi terlebih dahulu.");
            return;
        }
        if (kategori == null) {
            tampilkanPeringatan("Silakan pilih kategori transaksi terlebih dahulu.");
            return;
        }

        long nominal;
        try {
            nominal = bacaNominal(isianNominal.getText());
        } catch (IllegalArgumentException ex) {
            tampilkanPeringatan(ex.getMessage());
            isianNominal.requestFocusInWindow();
            isianNominal.selectAll();
            return;
        }

        String deskripsi = isianDeskripsi.getText().trim();
        if (deskripsi.isEmpty()) {
            deskripsi = kategori.getLabel();
        }

        bukuKas.tambahTransaksi(jenis, kategori, deskripsi, nominal);
        bersihkanForm();
    }

    /**
     * Membaca nominal dari teks yang diisi pengguna. Titik dibuang sebagai pemisah ribuan,
     * koma diterima sebagai pemisah desimal lalu dibulatkan ke bawah.
     */
    private static long bacaNominal(String teks) {
        String bersih = teks == null ? "" : teks.trim()
                .replace("Rp", "")
                .replace("rp", "")
                .replace(".", "")
                .replace(" ", "");
        if (bersih.isEmpty()) {
            throw new IllegalArgumentException("Nominal belum diisi. Masukkan angka lebih besar dari nol.");
        }
        if (bersih.startsWith("-")) {
            throw new IllegalArgumentException("Nominal tidak boleh bernilai negatif.");
        }
        if (bersih.contains(",")) {
            int posisiKoma = bersih.indexOf(',');
            bersih = bersih.substring(0, posisiKoma);
        }
        if (bersih.isEmpty()) {
            throw new IllegalArgumentException("Nominal harus lebih besar dari nol.");
        }
        try {
            long nilai = Long.parseLong(bersih);
            if (nilai <= 0) {
                throw new IllegalArgumentException("Nominal harus lebih besar dari nol.");
            }
            return nilai;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Nominal harus berupa angka, contoh: 250000 atau 250.000");
        }
    }

    private void bersihkanForm() {
        isianNominal.setText("");
        isianDeskripsi.setText("");
        isianNominal.requestFocusInWindow();
    }

    private void tampilkanPeringatan(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Data Belum Lengkap", JOptionPane.WARNING_MESSAGE);
    }
}
