package org.yuemi.fsi2107.ui;

import org.yuemi.fsi2107.model.Profile;
import org.yuemi.fsi2107.service.ProfileService;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

/**
 * Baris pemilih profil berisi daftar profil dan tombol tambah serta hapus.
 */
public class ProfilPanel extends JPanel {

    private static final int LEBAR_COMBO = 200;

    private final ProfileService profileService;
    private final JComboBox<Profile> pilihanProfile = new JComboBox<>();

    public ProfilPanel(ProfileService profileService) {
        this.profileService = profileService;

        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 4));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 14, 2, 14));

        JLabel label = new JLabel("Profil:");
        label.setFont(label.getFont().deriveFont(Font.BOLD, 13f));

        pilihanProfile.setPreferredSize(new Dimension(LEBAR_COMBO, 28));
        pilihanProfile.setMaximumRowCount(12);

        TombolBerwarna tombolTambah = new TombolBerwarna("Tambah", new Color(0x1565C0));
        tombolTambah.addActionListener(e -> tambahProfile());

        TombolBerwarna tombolHapus = new TombolBerwarna("Hapus", new Color(0xC62828));
        tombolHapus.addActionListener(e -> hapusProfile());

        add(label);
        add(pilihanProfile);
        add(tombolTambah);
        add(tombolHapus);

        pilihanProfile.addActionListener(e -> {
            Profile terpilih = (Profile) pilihanProfile.getSelectedItem();
            if (terpilih != null) {
                profileService.setProfilAktif(terpilih);
            }
        });

        profileService.tambahPendengar(layanan -> segarkanDaftar());
        segarkanDaftar();
    }

    /**
     * Memuat ulang isi combo box dengan profil yang tersedia.
     */
    private void segarkanDaftar() {
        Profile aktif = profileService.getProfilAktif();
        DefaultComboBoxModel<Profile> model = new DefaultComboBoxModel<>();
        for (Profile profile : profileService.getDaftarProfile()) {
            model.addElement(profile);
        }
        pilihanProfile.setModel(model);
        if (aktif != null) {
            for (int i = 0; i < model.getSize(); i++) {
                if (model.getElementAt(i).getId() == aktif.getId()) {
                    pilihanProfile.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    /**
     * Membuka dialog penambahan profil lalu menyegarkan tampilan bila berhasil.
     */
    private void tambahProfile() {
        TambahProfileDialog dialog = new TambahProfileDialog(
                SwingUtilities.getWindowAncestor(this), profileService);
        dialog.setVisible(true);
    }

    /**
     * Menghapus profil yang dipilih beserta seluruh transaksinya.
     */
    private void hapusProfile() {
        Profile terpilih = (Profile) pilihanProfile.getSelectedItem();
        if (terpilih == null) {
            JOptionPane.showMessageDialog(this, "Belum ada profil yang dipilih.",
                    "Belum Ada Pilihan", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int jawaban = JOptionPane.showConfirmDialog(this,
                "Hapus profil \"" + terpilih.getNama() + "\" beserta seluruh transaksinya?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (jawaban != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            profileService.hapusProfile(terpilih.getId());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Gagal Menghapus Profil", JOptionPane.ERROR_MESSAGE);
        }
    }
}
