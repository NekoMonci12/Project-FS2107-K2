package org.yuemi.fsi2107.ui;

import org.yuemi.fsi2107.model.Transaksi;
import org.yuemi.fsi2107.service.BukuKas;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel tabel yang menampilkan seluruh transaksi beserta tombol penghapusan.
 */
public class TabelTransaksiPanel extends JPanel {

    private static final String[] JUDUL_KOLOM =
            {"No", "Waktu", "Jenis", "Kategori", "Deskripsi", "Nominal"};

    private static final Color WARNA_BARIS_MASUK = new Color(0xE8F5E9);
    private static final Color WARNA_BARIS_KELUAR = new Color(0xFFEBEE);
    private static final Color WARNA_TEKS_MASUK = new Color(0x1B5E20);
    private static final Color WARNA_TEKS_KELUAR = new Color(0xB71C1C);

    private final BukuKas bukuKas;
    private final DefaultTableModel modelTabel;
    private final JTable tabel;

    /** Daftar transaksi yang sedang ditampilkan, sejajar indeks dengan baris tabel. */
    private List<Transaksi> transaksiTampil = new ArrayList<>();

    public TabelTransaksiPanel(BukuKas bukuKas) {
        this.bukuKas = bukuKas;

        setLayout(new BorderLayout(0, 6));
        setBackground(Color.WHITE);
        setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(0xCFD8DC)),
                " Daftar Transaksi "));

        modelTabel = new DefaultTableModel(JUDUL_KOLOM, 0) {
            @Override
            public boolean isCellEditable(int baris, int kolom) {
                return false;
            }
        };

        tabel = new JTable(modelTabel);
        tabel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabel.setRowHeight(26);
        tabel.setFillsViewportHeight(true);
        tabel.setShowGrid(true);
        tabel.setGridColor(new Color(0xE0E0E0));
        tabel.getTableHeader().setReorderingAllowed(false);
        tabel.setDefaultRenderer(Object.class, new PerenderBaris());

        tabel.getColumnModel().getColumn(0).setPreferredWidth(40);
        tabel.getColumnModel().getColumn(1).setPreferredWidth(120);
        tabel.getColumnModel().getColumn(2).setPreferredWidth(100);
        tabel.getColumnModel().getColumn(3).setPreferredWidth(100);
        tabel.getColumnModel().getColumn(4).setPreferredWidth(260);
        tabel.getColumnModel().getColumn(5).setPreferredWidth(130);

        JButton tombolHapus = new JButton("Hapus Transaksi Terpilih");
        tombolHapus.addActionListener(e -> hapusTransaksiTerpilih());

        JPanel panelBawah = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        panelBawah.setOpaque(false);
        panelBawah.add(tombolHapus);

        add(new JScrollPane(tabel), BorderLayout.CENTER);
        add(panelBawah, BorderLayout.SOUTH);
    }

    /**
     * Menyusun ulang isi tabel berdasarkan data terbaru dari buku kas.
     */
    public void perbarui(BukuKas bukuKas) {
        long idTerpilih = -1;
        int barisTerpilih = tabel.getSelectedRow();
        if (barisTerpilih >= 0 && barisTerpilih < transaksiTampil.size()) {
            idTerpilih = transaksiTampil.get(barisTerpilih).getId();
        }

        modelTabel.setRowCount(0);
        transaksiTampil = new ArrayList<>(bukuKas.getDaftarTransaksi());

        int nomor = 1;
        for (Transaksi transaksi : transaksiTampil) {
            modelTabel.addRow(new Object[]{
                    nomor++,
                    transaksi.getWaktuTerformat(),
                    transaksi.getJenis().getLabel(),
                    transaksi.getKategori().getLabel(),
                    transaksi.getDeskripsi(),
                    transaksi.getNominalTerformat()
            });
        }

        if (idTerpilih > 0) {
            for (int i = 0; i < transaksiTampil.size(); i++) {
                if (transaksiTampil.get(i).getId() == idTerpilih) {
                    tabel.setRowSelectionInterval(i, i);
                    break;
                }
            }
        }
    }

    private void hapusTransaksiTerpilih() {
        int baris = tabel.getSelectedRow();
        if (baris < 0) {
            JOptionPane.showMessageDialog(this,
                    "Pilih salah satu baris transaksi yang ingin dihapus.",
                    "Belum Ada Pilihan", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        if (baris >= transaksiTampil.size()) {
            return;
        }
        Transaksi transaksi = transaksiTampil.get(baris);

        int jawaban = JOptionPane.showConfirmDialog(this,
                "Hapus transaksi \"" + transaksi.getDeskripsi() + "\" sebesar "
                        + transaksi.getNominalTerformat() + "?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

        if (jawaban == JOptionPane.YES_OPTION) {
            bukuKas.hapusTransaksi(transaksi.getId());
        }
    }

    /**
     * Mewarnai baris sesuai jenis transaksi: hijau untuk uang masuk, merah untuk uang keluar.
     */
    private static class PerenderBaris extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable tabel, Object nilai, boolean terpilih,
                                                       boolean punyaFokus, int baris, int kolom) {
            Component komponen = super.getTableCellRendererComponent(
                    tabel, nilai, terpilih, punyaFokus, baris, kolom);

            Object jenis = tabel.getValueAt(baris, 2);
            boolean masuk = jenis != null && jenis.toString().equalsIgnoreCase("Uang Masuk");

            if (terpilih) {
                komponen.setBackground(tabel.getSelectionBackground());
                komponen.setForeground(tabel.getSelectionForeground());
            } else {
                komponen.setBackground(masuk ? WARNA_BARIS_MASUK : WARNA_BARIS_KELUAR);
                komponen.setForeground(masuk ? WARNA_TEKS_MASUK : WARNA_TEKS_KELUAR);
            }

            if (kolom == 0 || kolom == 5) {
                setHorizontalAlignment(RIGHT);
            } else {
                setHorizontalAlignment(LEFT);
            }
            return komponen;
        }
    }
}
