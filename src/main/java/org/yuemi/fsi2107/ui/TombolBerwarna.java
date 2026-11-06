package org.yuemi.fsi2107.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Tombol dengan warna background dan ukuran tetap agar sejajar dengan tombol lain.
 */
public class TombolBerwarna extends JButton {

    public enum Gaya {
        TERISI,
        GARIS
    }

    private static final int RADIUS = 6;
    private static final int TINGGI_TOMBOL = 30;
    private static final int PAD_X = 14;
    private static final int LEBAR_MINIMUM = 72;

    private final Color warnaDasar;
    private final Color warnaHover;
    private final Color warnaTekan;
    private final Gaya gaya;

    public TombolBerwarna(String teks, Color warnaDasar) {
        this(teks, warnaDasar, Gaya.TERISI);
    }

    public TombolBerwarna(String teks, Color warnaDasar, Gaya gaya) {
        super(teks);
        this.warnaDasar = warnaDasar;
        this.warnaHover = pertegas(warnaDasar, 1.18f);
        this.warnaTekan = peredup(warnaDasar, 0.82f);
        this.gaya = gaya;

        setFont(getFont().deriveFont(Font.BOLD, 12f));
        setForeground(gaya == Gaya.TERISI ? Color.WHITE : warnaDasar);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(0, PAD_X, 0, PAD_X));
    }

    @Override
    public void setEnabled(boolean aktif) {
        super.setEnabled(aktif);
        setForeground(gaya == Gaya.TERISI ? Color.WHITE : (aktif ? warnaDasar : new Color(0xB0BEC5)));
    }

    private static Color pertegas(Color warna, float faktor) {
        return new Color(
                Math.min(255, Math.round(warna.getRed() * faktor)),
                Math.min(255, Math.round(warna.getGreen() * faktor)),
                Math.min(255, Math.round(warna.getBlue() * faktor)));
    }

    private static Color peredup(Color warna, float faktor) {
        return new Color(
                Math.round(warna.getRed() * faktor),
                Math.round(warna.getGreen() * faktor),
                Math.round(warna.getBlue() * faktor));
    }

    @Override
    public Dimension getPreferredSize() {
        return hitungUkuran();
    }

    @Override
    public Dimension getMinimumSize() {
        return hitungUkuran();
    }

    @Override
    public Dimension getMaximumSize() {
        return hitungUkuran();
    }

    private Dimension hitungUkuran() {
        FontMetrics metrik = getFontMetrics(getFont());
        int lebarTeks = metrik.stringWidth(getText() == null ? "" : getText());
        int lebar = Math.max(LEBAR_MINIMUM, lebarTeks + PAD_X * 2);
        return new Dimension(lebar, TINGGI_TOMBOL);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean aktif = isEnabled();
        Color warna = warnaDasar;
        if (!aktif) {
            warna = new Color(0xB0BEC5);
        } else if (getModel().isPressed()) {
            warna = warnaTekan;
        } else if (getModel().isRollover()) {
            warna = warnaHover;
        }


        int lebar = getWidth();
        int tinggi = getHeight();

        if (gaya == Gaya.TERISI) {
            g2.setColor(warna);
            g2.fillRoundRect(0, 0, lebar, tinggi, RADIUS, RADIUS);
        } else {
            g2.setColor(aktif ? new Color(warna.getRed(), warna.getGreen(), warna.getBlue(), 28)
                    : new Color(0xECEFF1));
            g2.fillRoundRect(0, 0, lebar, tinggi, RADIUS, RADIUS);
            g2.setColor(warna);
            g2.drawRoundRect(0, 0, lebar - 1, tinggi - 1, RADIUS, RADIUS);
        }

        g2.dispose();

        super.paintComponent(g);
    }
}
