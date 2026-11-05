package aplikasdatafilm;

import java.awt.Color;
import java.awt.Font;
import javax.swing.*;

/**
 * Reusable Admin Menu Bar for Cinema Management.
 */
public class AdminMenuBar {

    private AdminMenuBar() {}

    public static void buat(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(CinemaTheme.PANEL_BG);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CinemaTheme.CARD_BORDER));

        // ── Menu Navigasi ──────────────────────────────────────────────
        JMenu menuNav = new JMenu("Navigasi Operasional");
        menuNav.setFont(CinemaTheme.FONT_BODY_BOLD);
        menuNav.setForeground(CinemaTheme.CREME);

        JMenuItem itemDashboard = new JMenuItem("🏠 Dashboard Operasional");
        itemDashboard.addActionListener(e -> {
            frame.dispose();
            new Menu().setVisible(true);
        });
        menuNav.add(itemDashboard);

        JMenuItem itemLoket = new JMenuItem("🎟 Loket Penjualan Tiket");
        itemLoket.addActionListener(e -> {
            frame.dispose();
            new PilihFilm(1).setVisible(true);
        });
        menuNav.add(itemLoket);

        menuNav.addSeparator();

        JMenuItem itemLogOut = new JMenuItem("Log Out");
        itemLogOut.setForeground(new Color(0xE5, 0x73, 0x73));
        itemLogOut.addActionListener(e -> {
            frame.dispose();
            new Login().setVisible(true);
        });
        menuNav.add(itemLogOut);

        JMenuItem itemKeluar = new JMenuItem("Tutup Aplikasi");
        itemKeluar.addActionListener(e -> System.exit(0));
        menuNav.add(itemKeluar);

        menuBar.add(menuNav);

        // ── Menu Manajemen Data ───────────────────────────────────────
        JMenu menuData = new JMenu("Kelola Data Bioskop");
        menuData.setFont(CinemaTheme.FONT_BODY_BOLD);
        menuData.setForeground(CinemaTheme.CREME);

        JMenuItem itemFilm = new JMenuItem("🎬 Film & Jadwal Tayang");
        itemFilm.addActionListener(e -> {
            frame.dispose();
            new FormFilmAdmin().setVisible(true);
        });
        menuData.add(itemFilm);

        JMenuItem itemGenre = new JMenuItem("🏷 Kategori Genre");
        itemGenre.addActionListener(e -> {
            frame.dispose();
            new FormTambahGenre().setVisible(true);
        });
        menuData.add(itemGenre);

        JMenuItem itemStudio = new JMenuItem("🏢 Ruang Studio");
        itemStudio.addActionListener(e -> {
            frame.dispose();
            new FormTambahStudio().setVisible(true);
        });
        menuData.add(itemStudio);

        menuBar.add(menuData);

        frame.setJMenuBar(menuBar);
    }
}
