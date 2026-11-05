package aplikasdatafilm;

import java.awt.Color;
import javax.swing.*;

/**
 * Reusable Cashier / Staff Menu Bar for Cinema Management.
 */
public class UserMenuBar {

    private UserMenuBar() {}

    public static void buat(JFrame frame, long idPengguna) {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(CinemaTheme.PANEL_BG);
        menuBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, CinemaTheme.CARD_BORDER));

        JMenu menuNav = new JMenu("Kasir");
        menuNav.setFont(CinemaTheme.FONT_BODY_BOLD);
        menuNav.setForeground(CinemaTheme.CREME);

        JMenuItem itemLoket = new JMenuItem("🎟 Loket Penjualan Tiket");
        itemLoket.addActionListener(e -> {
            frame.dispose();
            new PilihFilm(idPengguna).setVisible(true);
        });
        menuNav.add(itemLoket);

        JMenuItem itemLogOut = new JMenuItem("Log Out");
        itemLogOut.setForeground(new Color(0xE5, 0x73, 0x73));
        itemLogOut.addActionListener(e -> {
            frame.dispose();
            new Login().setVisible(true);
        });
        menuNav.add(itemLogOut);

        menuBar.add(menuNav);
        frame.setJMenuBar(menuBar);
    }
}
