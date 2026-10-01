/*
 * Reusable admin menu bar untuk semua tampilan admin.
 * Gunakan AdminMenuBar.buat(frame) di setiap JFrame admin.
 */
package aplikasdatafilm;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFrame;

/**
 * Factory untuk JMenuBar admin yang seragam.
 * Dipasang di FormFilmAdmin, FormTambahGenre, dan FormTambahStudio.
 */
public class AdminMenuBar {

    private AdminMenuBar() {
        // utility class — tidak di-instantiate
    }

    /**
     * Membuat dan memasang JMenuBar admin ke {@code frame} yang diberikan.
     * Menu akan menutup {@code frame} saat berpindah halaman.
     *
     * @param frame JFrame tujuan tempat menu bar dipasang
     */
    public static void buat(JFrame frame) {
        JMenuBar menuBar = new JMenuBar();

        // ── Menu Admin ───────────────────────────────────────────────
        JMenu menuAdmin = new JMenu("Admin");

        JMenuItem itemMenu = new JMenuItem("Menu Utama");
        itemMenu.addActionListener(e -> {
            frame.dispose();
            new Menu().setVisible(true);
        });
        menuAdmin.add(itemMenu);

        JMenuItem itemLogOut = new JMenuItem("Log Out");
        itemLogOut.setBackground(new java.awt.Color(102, 0, 51));
        itemLogOut.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        itemLogOut.setForeground(new java.awt.Color(153, 0, 51));
        itemLogOut.addActionListener(e -> {
            frame.dispose();
            new Login().setVisible(true);
        });
        menuAdmin.add(itemLogOut);

        JMenuItem itemKeluar = new JMenuItem("Keluar");
        itemKeluar.addActionListener(e -> System.exit(0));
        menuAdmin.add(itemKeluar);

        menuBar.add(menuAdmin);

        // ── Menu Data ────────────────────────────────────────────────
        JMenu menuData = new JMenu("Data");

        JMenuItem itemDataFilm = new JMenuItem("Isi Data Film");
        itemDataFilm.addActionListener(e -> {
            frame.dispose();
            new FormFilmAdmin().setVisible(true);
        });
        menuData.add(itemDataFilm);

        JMenuItem itemGenre = new JMenuItem("Tambah Genre");
        itemGenre.addActionListener(e -> {
            frame.dispose();
            new FormTambahGenre().setVisible(true);
        });
        menuData.add(itemGenre);

        JMenuItem itemStudio = new JMenuItem("Tambah Studio");
        itemStudio.addActionListener(e -> {
            frame.dispose();
            new FormTambahStudio().setVisible(true);
        });
        menuData.add(itemStudio);

        menuBar.add(menuData);

        frame.setJMenuBar(menuBar);
    }
}
