package aplikasdatafilm;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Cinema Ticket Counter Film Selection Screen (1280x720).
 * Displays movie cards with high-quality scaled posters, showtimes, prices,
 * and direct seat-selection launcher for cinema cashiers.
 */
public class PilihFilm extends JFrame {

    private final long idPengguna;
    private final List<PemesananService.Jadwal> daftarJadwal = new ArrayList<>();
    private final JPanel panelGrid = new JPanel(new GridLayout(0, 4, 20, 20));
    private JTextField txtSearch;
    private JScrollPane scrollFilm;

    public PilihFilm() {
        this(1);
    }

    public PilihFilm(long idPengguna) {
        this.idPengguna = idPengguna;
        CinemaTheme.standardizeFrame(this, "TIKET KU — Loket Kasir Pilih Film");
        buildUI();
        muatJadwal();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Loket Penjualan Tiket", "KASIR OPERASIONAL", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Subheader: Search bar & Dashboard shortcut
        JPanel subHeader = new JPanel(new BorderLayout(14, 0));
        subHeader.setOpaque(false);
        subHeader.setBorder(new EmptyBorder(14, 32, 10, 32));

        JPanel searchBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchBox.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Cari Film / Genre:");
        lblSearch.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblSearch.setForeground(CinemaTheme.CREME);

        txtSearch = new JTextField();
        CinemaTheme.styleTextField(txtSearch);
        txtSearch.setPreferredSize(new Dimension(320, 36));
        txtSearch.addActionListener(e -> muatJadwal());

        JButton btnSearch = CinemaTheme.createPrimaryButton("Cari");
        btnSearch.addActionListener(e -> muatJadwal());

        JButton btnSemua = CinemaTheme.createSecondaryButton("Semua Film");
        btnSemua.addActionListener(e -> {
            txtSearch.setText("");
            muatJadwal();
        });

        searchBox.add(lblSearch);
        searchBox.add(txtSearch);
        searchBox.add(btnSearch);
        searchBox.add(btnSemua);
        subHeader.add(searchBox, BorderLayout.WEST);

        // Shortcut back to dashboard
        JButton btnDashboard = CinemaTheme.createSecondaryButton("🏠 Dashboard Utama");
        btnDashboard.addActionListener(e -> {
            dispose();
            new Menu().setVisible(true);
        });
        subHeader.add(btnDashboard, BorderLayout.EAST);

        // Center: Film Grid with JScrollPane
        panelGrid.setBackground(CinemaTheme.BG_DARK);
        panelGrid.setBorder(new EmptyBorder(10, 32, 24, 32));

        scrollFilm = new JScrollPane(panelGrid);
        scrollFilm.setBorder(BorderFactory.createEmptyBorder());
        scrollFilm.getViewport().setBackground(CinemaTheme.BG_DARK);
        scrollFilm.getVerticalScrollBar().setUnitIncrement(18);

        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.add(subHeader, BorderLayout.NORTH);
        centerWrapper.add(scrollFilm, BorderLayout.CENTER);

        root.add(centerWrapper, BorderLayout.CENTER);
        getContentPane().add(root);
    }

    private void muatJadwal() {
        String keyword = txtSearch != null ? txtSearch.getText().trim() : "";
        try {
            panelGrid.removeAll();
            daftarJadwal.clear();

            List<PemesananService.Jadwal> list = PemesananService.cariJadwal(keyword);

            // Group by film id so each distinct movie shows its available showtimes
            Set<Integer> seenFilmIds = new HashSet<>();
            for (PemesananService.Jadwal j : list) {
                if (seenFilmIds.add(j.idFilm())) {
                    daftarJadwal.add(j);
                }
            }

            if (daftarJadwal.isEmpty()) {
                JPanel emptyPanel = new JPanel();
                emptyPanel.setLayout(new BoxLayout(emptyPanel, BoxLayout.Y_AXIS));
                emptyPanel.setOpaque(false);

                JLabel lblEmpty = new JLabel("Tidak ada film atau jadwal tayang yang aktif saat ini.", SwingConstants.CENTER);
                lblEmpty.setFont(CinemaTheme.FONT_SUBTITLE);
                lblEmpty.setForeground(CinemaTheme.TEXT_MUTED);
                lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);

                JLabel lblHint = new JLabel("Gunakan menu Manajemen Film di Dashboard untuk menambahkan film dan jadwal.", SwingConstants.CENTER);
                lblHint.setFont(CinemaTheme.FONT_BODY);
                lblHint.setForeground(CinemaTheme.TEXT_HINT);
                lblHint.setAlignmentX(Component.CENTER_ALIGNMENT);

                emptyPanel.add(Box.createVerticalStrut(60));
                emptyPanel.add(lblEmpty);
                emptyPanel.add(Box.createVerticalStrut(8));
                emptyPanel.add(lblHint);
                panelGrid.add(emptyPanel);
            } else {
                for (int i = 0; i < daftarJadwal.size(); i++) {
                    panelGrid.add(createFilmCard(daftarJadwal.get(i), i));
                }
            }

            panelGrid.revalidate();
            panelGrid.repaint();
            scrollFilm.getVerticalScrollBar().setValue(0);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal memuat jadwal film: " + ex.getMessage(),
                    "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createFilmCard(PemesananService.Jadwal jadwal, int index) {
        JPanel card = CinemaTheme.createCard();
        card.setLayout(new BorderLayout(0, 10));
        card.setPreferredSize(new Dimension(260, 440));

        // Poster container
        JLabel lblPoster = new JLabel();
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setVerticalAlignment(SwingConstants.CENTER);
        lblPoster.setBackground(CinemaTheme.PANEL_BG);
        lblPoster.setOpaque(true);
        lblPoster.setPreferredSize(new Dimension(240, 240));
        lblPoster.setBorder(BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1));

        if (jadwal.urlPoster() != null && !jadwal.urlPoster().isBlank()) {
            PosterFetcher.muat(lblPoster, jadwal.urlPoster(), 240, 240);
        } else {
            lblPoster.setText("<html><center>🎬<br>POSTER FILM</center></html>");
            lblPoster.setFont(CinemaTheme.FONT_BODY_BOLD);
            lblPoster.setForeground(CinemaTheme.TEXT_MUTED);
        }
        card.add(lblPoster, BorderLayout.NORTH);

        // Movie info body
        JPanel body = new JPanel();
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setOpaque(false);

        JLabel lblJudul = new JLabel(jadwal.judul());
        lblJudul.setFont(CinemaTheme.FONT_SUBTITLE);
        lblJudul.setForeground(CinemaTheme.CREME);
        lblJudul.setToolTipText(jadwal.judul());
        body.add(lblJudul);

        body.add(Box.createVerticalStrut(4));

        // Genre & Studio row
        JPanel tagRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        tagRow.setOpaque(false);

        JLabel lblGenre = new JLabel(" " + jadwal.genre() + " ");
        lblGenre.setFont(CinemaTheme.FONT_SMALL);
        lblGenre.setForeground(CinemaTheme.CREME);
        lblGenre.setBackground(new Color(0x3B, 0x2A, 0x33));
        lblGenre.setOpaque(true);
        lblGenre.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        JLabel lblStudio = new JLabel(" " + jadwal.namaStudio() + " ");
        lblStudio.setFont(CinemaTheme.FONT_SMALL);
        lblStudio.setForeground(CinemaTheme.CREME);
        lblStudio.setBackground(CinemaTheme.BURGUNDY);
        lblStudio.setOpaque(true);
        lblStudio.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));

        tagRow.add(lblGenre);
        tagRow.add(lblStudio);
        body.add(tagRow);

        body.add(Box.createVerticalStrut(6));

        // Showtime & Duration
        JLabel lblWaktu = new JLabel("🕒 " + CinemaTheme.formatWaktu(jadwal.mulaiTayang()) + " WIB | ⏳ " + jadwal.durasiMenit() + " Menit");
        lblWaktu.setFont(CinemaTheme.FONT_SMALL);
        lblWaktu.setForeground(CinemaTheme.TEXT_MUTED);
        body.add(lblWaktu);

        body.add(Box.createVerticalStrut(6));

        // Price
        JLabel lblHarga = new JLabel(CinemaTheme.formatRupiah(jadwal.hargaTiket()));
        lblHarga.setFont(CinemaTheme.FONT_SUBTITLE);
        lblHarga.setForeground(CinemaTheme.ACCENT_GOLD);
        body.add(lblHarga);

        card.add(body, BorderLayout.CENTER);

        // Button: Pilih Kursi & Beli
        JButton btnBeli = CinemaTheme.createPrimaryButton("🎟  Pilih Kursi & Beli");
        btnBeli.setFont(CinemaTheme.FONT_BODY_BOLD);
        btnBeli.addActionListener(e -> {
            setVisible(false);
            new BeliTiket(idPengguna, jadwal, this).setVisible(true);
        });
        card.add(btnBeli, BorderLayout.SOUTH);

        return card;
    }
}
