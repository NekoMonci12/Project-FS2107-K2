package org.yuemi.fsi2107.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Tombol dengan warna background.
 */
public class TombolBerwarna extends JButton {

    private static final int RADIUS = 8;

    private final Color warnaDasar;
    private final Color warnaHover;
    private final Color warnaTekan;

    public TombolBerwarna(String teks, Color warnaDasar) {
        super(teks);
        this.warnaDasar = warnaDasar;
        this.warnaHover = pertegas(warnaDasar, 1.18f);
        this.warnaTekan = peredup(warnaDasar, 0.82f);

        setFont(getFont().deriveFont(Font.BOLD, 13f));
        setForeground(Color.WHITE);
        setContentAreaFilled(false);
        setOpaque(false);
        setBorderPainted(false);
        setFocusPainted(false);
        setRolloverEnabled(true);
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
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
        Dimension ukuran = super.getPreferredSize();
        ukuran.width += 8;
        ukuran.height = Math.max(ukuran.height, 34);
        return ukuran;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color warna = warnaDasar;
        if (!isEnabled()) {
            warna = new Color(0xB0BEC5);
        } else if (getModel().isPressed()) {
            warna = warnaTekan;
        } else if (getModel().isRollover()) {
            warna = warnaHover;
        }

        g2.setColor(warna);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        g2.dispose();

        super.paintComponent(g);
    }
}
