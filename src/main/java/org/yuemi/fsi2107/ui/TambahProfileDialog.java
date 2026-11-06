package org.yuemi.fsi2107.ui;

import org.yuemi.fsi2107.model.Profile;
import org.yuemi.fsi2107.service.ProfileService;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Dialog untuk menambah profil baru, beserta pilihan tipenya.
 *
 * <p>Jenis profil menentukan isian tambahan yang tampil. Profil usaha
 * menampilkan tarif PPh dan PPN, profil Biasa tidak.</p>
 */
public class TambahProfileDialog extends JDialog {

    private static final String LABEL_UMUM = "Profil Biasa";
    private static final String LABEL_USAHA = "Profil Usaha";

    private final ProfileService profileService;
    private final JComboBox<String> pilihanTipe = new JComboBox<>();
    private final JTextField isianNama = new JTextField();
    private final JTextField isianCatatan = new JTextField();
    private final JTextField isianTarifPph = new JTextField();
    private final JTextField isianTarifPpn = new JTextField();
    private final JPanel panelTarif = new JPanel(new GridBagLayout());

    public TambahProfileDialog(Window induk, ProfileService profileService) {
        super(induk, "Tambah Profil", ModalityType.APPLICATION_MODAL);
        this.profileService = profileService;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        pilihanTipe.setModel(new DefaultComboBoxModel<>(new String[]{LABEL_UMUM, LABEL_USAHA}));
        pilihanTipe.addActionListener(e -> perbaruiTampilTarif());

        JPanel isi = new JPanel(new GridBagLayout());
        isi.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        baris(isi, gbc, 0, "Tipe", pilihanTipe);
        baris(isi, gbc, 1, "Nama", isianNama);
        baris(isi, gbc, 2, "Catatan", isianCatatan);

        GridBagConstraints gbcTarif = new GridBagConstraints();
        gbcTarif.insets = new Insets(5, 5, 5, 5);
        gbcTarif.anchor = GridBagConstraints.WEST;
        gbcTarif.fill = GridBagConstraints.HORIZONTAL;
        baris(panelTarif, gbcTarif, 0, "Tarif PPh (%)", isianTarifPph);
        baris(panelTarif, gbcTarif, 1, "Tarif PPN (%)", isianTarifPpn);
        panelTarif.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        isi.add(panelTarif, gbc);

        JButton tombolSimpan = new TombolBerwarna("Simpan", new Color(0x2E7D32));
        tombolSimpan.addActionListener(e -> simpan());

        JButton tombolBatal = new TombolBerwarna("Batal", new Color(0x607D8B),
                TombolBerwarna.Gaya.GARIS);
        tombolBatal.addActionListener(e -> dispose());

        JPanel panelTombol = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        panelTombol.setOpaque(false);
        panelTombol.add(tombolBatal);
        panelTombol.add(tombolSimpan);

        setLayout(new BorderLayout());
        add(isi, BorderLayout.CENTER);
        add(panelTombol, BorderLayout.SOUTH);

        perbaruiTampilTarif();
        pack();
        setLocationRelativeTo(induk);
    }

    /**
     * Menambahkan satu baris berlabel dan komponen isian ke panel.
     */
    private static void baris(JPanel panel, GridBagConstraints gbc, int baris,
                               String label, java.awt.Component komponen) {
        JLabel labelKomponen = new JLabel(label);
        labelKomponen.setPreferredSize(new Dimension(120, 24));

        gbc.gridy = baris;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        panel.add(labelKomponen, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(komponen, gbc);
        gbc.weightx = 0;
    }

    /**
     * Menampilkan atau menyembunyikan isian tarif sesuai tipe profil terpilih.
     */
    private void perbaruiTampilTarif() {
        boolean tipeUsaha = LABEL_USAHA.equals(pilihanTipe.getSelectedItem());
        panelTarif.setVisible(tipeUsaha);
        if (tipeUsaha) {
            if (isianTarifPph.getText().isBlank()) {
                isianTarifPph.setText("0.5");
            }
            if (isianTarifPpn.getText().isBlank()) {
                isianTarifPpn.setText("11");
            }
        }
        pack();
        setLocationRelativeTo(getOwner());
    }

    /**
     * Menyimpan profil baru lalu menutup dialog.
     */
    private void simpan() {
        try {
            if (LABEL_USAHA.equals(pilihanTipe.getSelectedItem())) {
                profileService.tambahProfileUsaha(
                        isianNama.getText(), isianCatatan.getText(),
                        bacaTarif(isianTarifPph.getText(), "PPh"),
                        bacaTarif(isianTarifPpn.getText(), "PPN"));
            } else {
                profileService.tambahProfileUmum(isianNama.getText(), isianCatatan.getText());
            }
            dispose();
        } catch (NumberFormatException ex) {
            tampilkanPeringatan("Tarif pajak harus berupa angka, contoh: 11 atau 0.5");
        } catch (IllegalArgumentException ex) {
            tampilkanPeringatan(ex.getMessage());
        }
    }

    /**
     * Membaca nilai tarif pajak dari isian pengguna.
     */
    private static double bacaTarif(String teks, String namaPajak) {
        String bersih = teks == null ? "" : teks.trim().replace(",", ".");
        if (bersih.isEmpty()) {
            throw new IllegalArgumentException("Tarif " + namaPajak + " tidak boleh kosong.");
        }
        return Double.parseDouble(bersih);
    }

    private void tampilkanPeringatan(String pesan) {
        JOptionPane.showMessageDialog(this, pesan, "Data Belum Lengkap", JOptionPane.WARNING_MESSAGE);
    }
}
