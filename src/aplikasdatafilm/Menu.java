package aplikasdatafilm;

import java.awt.*;
import java.math.BigDecimal;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Cinema Staff & Management Operational Dashboard (1280x720).
 * Displays real-time operational statistics (active films, today's showtimes,
 * tickets sold, and today's revenue) alongside quick-action operational modules.
 */
public class Menu extends JFrame {

    private JLabel lblStatFilm;
    private JLabel lblStatJadwal;
    private JLabel lblStatTiket;
    private JLabel lblStatPendapatan;

    public Menu() {
        CinemaTheme.standardizeFrame(this, "TIKET KU — Cinema Management Dashboard");
        buildUI();
        muatStatistik();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Dashboard Operasional Bioskop", "ADMIN / MANAJER", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Container
        JPanel main = new JPanel();
        main.setLayout(new BoxLayout(main, BoxLayout.Y_AXIS));
        main.setOpaque(false);
        main.setBorder(new EmptyBorder(24, 36, 24, 36));

        // ── SECTION 1: STATS SUMMARY CARDS ──────────────────────────────────
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 18, 0));
        statsRow.setOpaque(false);
        statsRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblStatFilm = new JLabel("0");
        lblStatJadwal = new JLabel("0");
        lblStatTiket = new JLabel("0");
        lblStatPendapatan = new JLabel("Rp 0");

        statsRow.add(createStatCard("🎬 Total Film", lblStatFilm, "Film terdaftar di sistem"));
        statsRow.add(createStatCard("🕒 Jadwal Hari Ini", lblStatJadwal, "Sesi penayangan"));
        statsRow.add(createStatCard("🎟 Tiket Terjual Hari Ini", lblStatTiket, "Tiket lunas"));
        statsRow.add(createStatCard("💰 Pendapatan Hari Ini", lblStatPendapatan, "Total kasir"));

        main.add(statsRow);
        main.add(Box.createVerticalStrut(28));

        // ── SECTION 2: OPERATIONAL MODULE CARDS ─────────────────────────────
        JLabel lblModulTitle = new JLabel("MODUL OPERASIONAL BIOSKOP");
        lblModulTitle.setFont(CinemaTheme.FONT_SUBTITLE);
        lblModulTitle.setForeground(CinemaTheme.CREME);
        lblModulTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        main.add(lblModulTitle);

        main.add(Box.createVerticalStrut(14));

        JPanel moduleGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        moduleGrid.setOpaque(false);

        // 1. Loket Tiket (Cashier ticketing)
        moduleGrid.add(createModuleCard(
                "🎟",
                "Loket Penjualan Tiket",
                "Buka loket kasir untuk melayani pengunjung: pemilihan film, studio, pemilihan kursi interaktif, dan konfirmasi pembayaran QRIS / Cash.",
                "Buka Loket Penjualan Tiket",
                () -> {
                    dispose();
                    new PilihFilm(1).setVisible(true);
                }
        ));

        // 2. Kelola Film & Jadwal
        moduleGrid.add(createModuleCard(
                "🎬",
                "Kelola Film & Jadwal Tayang",
                "Input film baru, atur jam tayang dengan Time Picker interaktif, pilih tanggal tayang dengan kalender, dan pantau status jadwal bioskop.",
                "Kelola Film & Jadwal",
                () -> {
                    dispose();
                    new FormFilmAdmin().setVisible(true);
                }
        ));

        // 3. Kelola Studio
        moduleGrid.add(createModuleCard(
                "🏢",
                "Manajemen Studio",
                "Kelola ruangan studio bioskop, monitor kapasitas kursi, serta pemeliharaan studio penayangan.",
                "Kelola Studio",
                () -> {
                    dispose();
                    new FormTambahStudio().setVisible(true);
                }
        ));

        // 4. Kelola Genre
        moduleGrid.add(createModuleCard(
                "🏷",
                "Manajemen Genre",
                "Pengelompokan kategori genre film untuk mempermudah pencarian dan pengorganisasian katalog film bioskop.",
                "Kelola Genre",
                () -> {
                    dispose();
                    new FormTambahGenre().setVisible(true);
                }
        ));

        main.add(moduleGrid);

        main.add(Box.createVerticalGlue());

        // ── SECTION 3: SYSTEM STATUS FOOTER ─────────────────────────────────
        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CinemaTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 0, 0, 0)
        ));

        JLabel lblDbStatus = new JLabel("● Database: Terhubung (data_film) | Standar Resolusi: 1280 × 720 px");
        lblDbStatus.setFont(CinemaTheme.FONT_SMALL);
        lblDbStatus.setForeground(new Color(0x81, 0xC7, 0x84));

        JButton btnRefresh = CinemaTheme.createSmallButton("🔄 Refresh Data", CinemaTheme.CARD_BG, CinemaTheme.CARD_HOVER);
        btnRefresh.addActionListener(e -> muatStatistik());

        footer.add(lblDbStatus, BorderLayout.WEST);
        footer.add(btnRefresh, BorderLayout.EAST);
        main.add(footer);

        root.add(main, BorderLayout.CENTER);
        getContentPane().add(root);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, String subtitle) {
        JPanel card = CinemaTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel lblT = new JLabel(title);
        lblT.setFont(CinemaTheme.FONT_SECTION);
        lblT.setForeground(CinemaTheme.TEXT_MUTED);

        valueLabel.setFont(CinemaTheme.FONT_TITLE_LARGE);
        valueLabel.setForeground(CinemaTheme.ACCENT_GOLD);

        JLabel lblS = new JLabel(subtitle);
        lblS.setFont(CinemaTheme.FONT_SMALL);
        lblS.setForeground(CinemaTheme.TEXT_HINT);

        card.add(lblT);
        card.add(Box.createVerticalStrut(4));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(2));
        card.add(lblS);
        return card;
    }

    private JPanel createModuleCard(String icon, String title, String description, String buttonText, Runnable action) {
        JPanel card = CinemaTheme.createCard();
        card.setLayout(new BorderLayout(14, 10));

        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerPanel.setOpaque(false);

        JLabel lblIcon = new JLabel(icon);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(CinemaTheme.FONT_SUBTITLE);
        lblTitle.setForeground(CinemaTheme.CREME);

        headerPanel.add(lblIcon);
        headerPanel.add(lblTitle);
        card.add(headerPanel, BorderLayout.NORTH);

        JLabel lblDesc = new JLabel("<html><body style='width: 380px; color: #A89DA2; font-size: 9.5pt; line-height: 1.4;'>"
                + description + "</body></html>");
        card.add(lblDesc, BorderLayout.CENTER);

        JButton btn = CinemaTheme.createPrimaryButton(buttonText + " →");
        btn.setFont(CinemaTheme.FONT_BODY_BOLD);
        btn.addActionListener(e -> action.run());

        JPanel btnWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnWrapper.setOpaque(false);
        btnWrapper.add(btn);
        card.add(btnWrapper, BorderLayout.SOUTH);

        return card;
    }

    private void muatStatistik() {
        new Thread(() -> {
            PemesananService.DashboardStats stats = PemesananService.getDashboardStats();
            SwingUtilities.invokeLater(() -> {
                lblStatFilm.setText(stats.totalFilm() + " Film");
                lblStatJadwal.setText(stats.totalJadwalHariIni() + " Sesi");
                lblStatTiket.setText(stats.totalTiketHariIni() + " Tiket");
                lblStatPendapatan.setText(CinemaTheme.formatRupiah(stats.totalPendapatanHariIni()));
            });
        }).start();
    }
}
