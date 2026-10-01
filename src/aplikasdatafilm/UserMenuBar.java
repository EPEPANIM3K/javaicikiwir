/*
 * Reusable user menu bar untuk semua tampilan user.
 * Gunakan UserMenuBar.buat(frame, idPengguna) di setiap JFrame user.
 */
package aplikasdatafilm;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JFrame;

/**
 * Factory untuk JMenuBar user yang seragam.
 * Dipasang di PilihFilm, BeliTiket, CheckOut, dan StrukFilm.
 */
public class UserMenuBar {

    private UserMenuBar() {
        // utility class — tidak di-instantiate
    }

    /**
     * Membuat dan memasang JMenuBar user ke {@code frame} yang diberikan.
     *
     * @param frame       JFrame tujuan tempat menu bar dipasang
     * @param idPengguna  ID pengguna yang sedang login (diteruskan ke form berikutnya)
     */
    public static void buat(JFrame frame, long idPengguna) {
        JMenuBar menuBar = new JMenuBar();

        // ── Menu User ────────────────────────────────────────────────
        JMenu menuUser = new JMenu("User");

        JMenuItem itemLogOut = new JMenuItem("Log Out");
        itemLogOut.setBackground(new java.awt.Color(102, 0, 51));
        itemLogOut.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        itemLogOut.setForeground(new java.awt.Color(153, 0, 51));
        itemLogOut.addActionListener(e -> {
            frame.dispose();
            new Login().setVisible(true);
        });
        menuUser.add(itemLogOut);

        JMenuItem itemKeluar = new JMenuItem("Keluar");
        itemKeluar.addActionListener(e -> System.exit(0));
        menuUser.add(itemKeluar);

        menuBar.add(menuUser);

        // ── Menu Action ──────────────────────────────────────────────
        JMenu menuAction = new JMenu("Action");

        JMenuItem itemPilihFilm = new JMenuItem("Check Film");
        itemPilihFilm.addActionListener(e -> {
            frame.dispose();
            new PilihFilm(idPengguna).setVisible(true);
        });
        menuAction.add(itemPilihFilm);

        menuBar.add(menuAction);

        frame.setJMenuBar(menuBar);
    }
}
