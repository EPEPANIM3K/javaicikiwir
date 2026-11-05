package aplikasdatafilm;

import Koneksi.Koneksi;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

/**
 * Modern Cinema Film & Showtime Management Screen (1280x720).
 * Features:
 * - Date Picker calendar selection with Indonesian standard formatting.
 * - Dedicated Year Picker (JSpinner) for movie release year.
 * - Interactive Showtime Chip Panel with Time Picker dialog (hours:minutes),
 *   click-to-edit, delete, duplicate prevention, and responsive wrapping.
 * - Studio collision validation to prevent overlapping showtimes in the same studio.
 * - High-resolution 1280x720 layout with movie table and poster preview.
 */
public class FormFilmAdmin extends JFrame {

    private final Map<String, Integer> genreMap = new HashMap<>();
    private final Map<String, Integer> studioMap = new HashMap<>();

    // Form Components
    private JTextField txtId;
    private JTextField txtJudul;
    private JComboBox<String> cbGenre;
    private JSpinner spinTahun;
    private JTextField txtSutradara;
    private JSpinner spinDurasi;
    private JSpinner spinRating;
    private JTextField txtUrlPoster;
    private JLabel lblPosterPreview;

    // Schedule Components
    private JComboBox<String> cbStudio;
    private JTextField txtHargaTiket;
    private LocalDate selectedTanggal = LocalDate.now();
    private JLabel lblTanggalDisplay;
    private ShowtimeChipPanel showtimePanel;

    // Table & Search
    private JTextField txtSearch;
    private JTable tableFilm;
    private JButton btnTambah;
    private JButton btnEdit;
    private JButton btnHapus;
    private JButton btnClear;

    // Tracking state for editing schedules
    private Integer filmGrupJadwalAwal;
    private LocalDate tanggalGrupJadwalAwal;
    private Integer studioGrupJadwalAwal;

    public record JadwalFilm(int idStudio, LocalDateTime mulaiTayang, BigDecimal hargaTiket, boolean memilikiTiket) {}

    public FormFilmAdmin() {
        CinemaTheme.standardizeFrame(this, "TIKET KU — Manajemen Film & Jadwal Tayang");
        buildUI();
        loadComboGenre();
        loadComboStudio();
        loadDataFilm();
        updateButtonStates(false);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Manajemen Film & Jadwal Tayang", "ADMINISTRATOR", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Container: 2 Columns
        // Left Column: Form Controls (width 540)
        // Right Column: Film Table & Search (width remaining)
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        mainPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ── LEFT: FORM PANEL ───────────────────────────────────────────────
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.52;
        gbc.insets = new Insets(0, 0, 0, 14);

        JPanel formCard = CinemaTheme.createCard();
        formCard.setLayout(new BorderLayout(0, 10));

        JScrollPane formScroll = new JScrollPane(createFormInnerPanel());
        formScroll.setBorder(BorderFactory.createEmptyBorder());
        formScroll.setOpaque(false);
        formScroll.getViewport().setOpaque(false);
        formScroll.getVerticalScrollBar().setUnitIncrement(14);
        formCard.add(formScroll, BorderLayout.CENTER);

        // Buttons row
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnBar.setOpaque(false);

        btnClear = CinemaTheme.createSecondaryButton("Reset Form");
        btnClear.addActionListener(e -> clearFields());

        btnHapus = CinemaTheme.createDangerButton("Hapus Film");
        btnHapus.addActionListener(e -> hapusFilm());

        btnEdit = CinemaTheme.createPrimaryButton("Simpan Edit");
        btnEdit.addActionListener(e -> editFilm());

        btnTambah = CinemaTheme.createSuccessButton("+ Tambah Film");
        btnTambah.addActionListener(e -> tambahFilm());

        btnBar.add(btnClear);
        btnBar.add(btnHapus);
        btnBar.add(btnEdit);
        btnBar.add(btnTambah);
        formCard.add(btnBar, BorderLayout.SOUTH);

        mainPanel.add(formCard, gbc);

        // ── RIGHT: TABLE & PREVIEW PANEL ───────────────────────────────────
        gbc.gridx = 1;
        gbc.weightx = 0.48;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel tableCard = CinemaTheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 10));

        // Search bar
        JPanel searchBar = new JPanel(new BorderLayout(8, 0));
        searchBar.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍 Cari Film:");
        lblSearch.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblSearch.setForeground(CinemaTheme.CREME);
        searchBar.add(lblSearch, BorderLayout.WEST);

        txtSearch = new JTextField();
        CinemaTheme.styleTextField(txtSearch);
        txtSearch.addActionListener(e -> cariDataFilm());
        searchBar.add(txtSearch, BorderLayout.CENTER);

        JPanel searchBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchBtns.setOpaque(false);
        JButton btnCari = CinemaTheme.createPrimaryButton("Cari");
        btnCari.addActionListener(e -> cariDataFilm());
        JButton btnResetSearch = CinemaTheme.createSecondaryButton("Semua");
        btnResetSearch.addActionListener(e -> {
            txtSearch.setText("");
            loadDataFilm();
        });
        searchBtns.add(btnCari);
        searchBtns.add(btnResetSearch);
        searchBar.add(searchBtns, BorderLayout.EAST);

        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        tableFilm = new JTable();
        CinemaTheme.styleTable(tableFilm);
        tableFilm.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableFilm.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                tampilkanFilmTerpilih();
            }
        });

        JScrollPane tableScroll = new JScrollPane(tableFilm);
        tableScroll.setBorder(BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1));
        tableScroll.getViewport().setBackground(CinemaTheme.CARD_BG);
        tableCard.add(tableScroll, BorderLayout.CENTER);

        // Bottom: Poster thumbnail preview & quick stats
        JPanel bottomPreview = new JPanel(new BorderLayout(12, 0));
        bottomPreview.setOpaque(false);
        bottomPreview.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CinemaTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)
        ));

        lblPosterPreview = new JLabel("POSTER", SwingConstants.CENTER);
        lblPosterPreview.setPreferredSize(new Dimension(70, 95));
        lblPosterPreview.setBackground(CinemaTheme.PANEL_BG);
        lblPosterPreview.setOpaque(true);
        lblPosterPreview.setBorder(BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 1));
        lblPosterPreview.setForeground(CinemaTheme.TEXT_MUTED);

        JLabel lblHelp = new JLabel("<html><body style='color:#A89DA2; font-size:9pt; line-height:1.4;'>"
                + "<b>Petunjuk:</b><br>"
                + "• Pilih baris tabel untuk melihat/mengedit film & jadwal tayangnya.<br>"
                + "• Jam tayang dapat ditambah banyak sesi per tanggal & diedit langsung.<br>"
                + "• Sistem otomatis memeriksa bentrok jam tayang pada studio yang sama.</body></html>");

        bottomPreview.add(lblPosterPreview, BorderLayout.WEST);
        bottomPreview.add(lblHelp, BorderLayout.CENTER);
        tableCard.add(bottomPreview, BorderLayout.SOUTH);

        mainPanel.add(tableCard, gbc);
        root.add(mainPanel, BorderLayout.CENTER);

        AdminMenuBar.buat(this);
        getContentPane().add(root);
    }

    private JPanel createFormInnerPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);

        // ── SECTION A: DATA FILM ───────────────────────────────────────────
        JLabel sec1 = new JLabel("INFORMASI FILM");
        sec1.setFont(CinemaTheme.FONT_SECTION);
        sec1.setForeground(CinemaTheme.ACCENT_GOLD);
        p.add(sec1);
        p.add(Box.createVerticalStrut(10));

        // Row 1: ID Film & Judul Film
        JPanel r1 = new JPanel(new BorderLayout(10, 0));
        r1.setOpaque(false);

        JPanel pId = new JPanel(new BorderLayout(4, 0));
        pId.setOpaque(false);
        pId.setPreferredSize(new Dimension(80, 40));
        JLabel lblId = new JLabel("ID:");
        lblId.setFont(CinemaTheme.FONT_SMALL);
        lblId.setForeground(CinemaTheme.TEXT_MUTED);
        txtId = new JTextField();
        txtId.setEditable(false);
        CinemaTheme.styleTextField(txtId);
        pId.add(lblId, BorderLayout.NORTH);
        pId.add(txtId, BorderLayout.CENTER);

        JPanel pJudul = new JPanel(new BorderLayout(4, 0));
        pJudul.setOpaque(false);
        JLabel lblJudul = new JLabel("Judul Film:");
        lblJudul.setFont(CinemaTheme.FONT_SMALL);
        lblJudul.setForeground(CinemaTheme.TEXT_MUTED);
        txtJudul = new JTextField();
        CinemaTheme.styleTextField(txtJudul);
        pJudul.add(lblJudul, BorderLayout.NORTH);
        pJudul.add(txtJudul, BorderLayout.CENTER);

        r1.add(pId, BorderLayout.WEST);
        r1.add(pJudul, BorderLayout.CENTER);
        p.add(r1);
        p.add(Box.createVerticalStrut(8));

        // Row 2: Genre (+ button) & Year Picker (JSpinner)
        JPanel r2 = new JPanel(new GridLayout(1, 2, 12, 0));
        r2.setOpaque(false);

        JPanel pGenre = new JPanel(new BorderLayout(4, 0));
        pGenre.setOpaque(false);
        JLabel lblGenre = new JLabel("Genre:");
        lblGenre.setFont(CinemaTheme.FONT_SMALL);
        lblGenre.setForeground(CinemaTheme.TEXT_MUTED);
        cbGenre = new JComboBox<>();
        CinemaTheme.styleComboBox(cbGenre);

        JButton btnAddGenre = CinemaTheme.createSmallButton("+", CinemaTheme.BURGUNDY, CinemaTheme.BURGUNDY_HOVER);
        btnAddGenre.setToolTipText("Tambah Genre Baru");
        btnAddGenre.addActionListener(e -> {
            new FormTambahGenre().setVisible(true);
            dispose();
        });

        JPanel genreInputBox = new JPanel(new BorderLayout(4, 0));
        genreInputBox.setOpaque(false);
        genreInputBox.add(cbGenre, BorderLayout.CENTER);
        genreInputBox.add(btnAddGenre, BorderLayout.EAST);

        pGenre.add(lblGenre, BorderLayout.NORTH);
        pGenre.add(genreInputBox, BorderLayout.CENTER);

        // Dedicated Year Picker (Spinner) - Requirement 5A
        JPanel pTahun = new JPanel(new BorderLayout(4, 0));
        pTahun.setOpaque(false);
        JLabel lblTahun = new JLabel("Tahun Rilis (Year Picker):");
        lblTahun.setFont(CinemaTheme.FONT_SMALL);
        lblTahun.setForeground(CinemaTheme.TEXT_MUTED);

        spinTahun = new JSpinner(new SpinnerNumberModel(2026, 1900, 2099, 1));
        JSpinner.NumberEditor yearEditor = new JSpinner.NumberEditor(spinTahun, "#");
        spinTahun.setEditor(yearEditor);
        yearEditor.getTextField().setFont(CinemaTheme.FONT_BODY);
        pTahun.add(lblTahun, BorderLayout.NORTH);
        pTahun.add(spinTahun, BorderLayout.CENTER);

        r2.add(pGenre);
        r2.add(pTahun);
        p.add(r2);
        p.add(Box.createVerticalStrut(8));

        // Row 3: Sutradara, Durasi, Rating
        JPanel r3 = new JPanel(new GridLayout(1, 3, 10, 0));
        r3.setOpaque(false);

        JPanel pSutradara = new JPanel(new BorderLayout(4, 0));
        pSutradara.setOpaque(false);
        JLabel lblSutradara = new JLabel("Sutradara:");
        lblSutradara.setFont(CinemaTheme.FONT_SMALL);
        lblSutradara.setForeground(CinemaTheme.TEXT_MUTED);
        txtSutradara = new JTextField();
        CinemaTheme.styleTextField(txtSutradara);
        pSutradara.add(lblSutradara, BorderLayout.NORTH);
        pSutradara.add(txtSutradara, BorderLayout.CENTER);

        JPanel pDurasi = new JPanel(new BorderLayout(4, 0));
        pDurasi.setOpaque(false);
        JLabel lblDurasi = new JLabel("Durasi (Menit):");
        lblDurasi.setFont(CinemaTheme.FONT_SMALL);
        lblDurasi.setForeground(CinemaTheme.TEXT_MUTED);
        spinDurasi = new JSpinner(new SpinnerNumberModel(120, 1, 600, 5));
        spinDurasi.setFont(CinemaTheme.FONT_BODY);
        pDurasi.add(lblDurasi, BorderLayout.NORTH);
        pDurasi.add(spinDurasi, BorderLayout.CENTER);

        JPanel pRating = new JPanel(new BorderLayout(4, 0));
        pRating.setOpaque(false);
        JLabel lblRating = new JLabel("Rating (0.0 - 10.0):");
        lblRating.setFont(CinemaTheme.FONT_SMALL);
        lblRating.setForeground(CinemaTheme.TEXT_MUTED);
        spinRating = new JSpinner(new SpinnerNumberModel(8.0, 0.0, 10.0, 0.1));
        spinRating.setFont(CinemaTheme.FONT_BODY);
        pRating.add(lblRating, BorderLayout.NORTH);
        pRating.add(spinRating, BorderLayout.CENTER);

        r3.add(pSutradara);
        r3.add(pDurasi);
        r3.add(pRating);
        p.add(r3);
        p.add(Box.createVerticalStrut(8));

        // Row 4: URL Poster & Upload button
        JPanel r4 = new JPanel(new BorderLayout(8, 0));
        r4.setOpaque(false);
        JLabel lblPoster = new JLabel("URL Poster Film:");
        lblPoster.setFont(CinemaTheme.FONT_SMALL);
        lblPoster.setForeground(CinemaTheme.TEXT_MUTED);
        txtUrlPoster = new JTextField();
        CinemaTheme.styleTextField(txtUrlPoster);

        JButton btnUpload = CinemaTheme.createSecondaryButton("📁 Unggah");
        btnUpload.setToolTipText("Unggah poster lokal");
        btnUpload.addActionListener(e -> pilihDanUploadPoster());

        r4.add(lblPoster, BorderLayout.NORTH);
        r4.add(txtUrlPoster, BorderLayout.CENTER);
        r4.add(btnUpload, BorderLayout.EAST);
        p.add(r4);

        p.add(Box.createVerticalStrut(16));

        // ── SECTION B: PENGATURAN JADWAL TAYANG ─────────────────────────────
        JLabel sec2 = new JLabel("PENGATURAN JADWAL TAYANG");
        sec2.setFont(CinemaTheme.FONT_SECTION);
        sec2.setForeground(CinemaTheme.ACCENT_GOLD);
        p.add(sec2);
        p.add(Box.createVerticalStrut(8));

        // Row 5: Studio (+ button) & Harga Tiket
        JPanel r5 = new JPanel(new GridLayout(1, 2, 12, 0));
        r5.setOpaque(false);

        JPanel pStudio = new JPanel(new BorderLayout(4, 0));
        pStudio.setOpaque(false);
        JLabel lblStudio = new JLabel("Studio Bioskop:");
        lblStudio.setFont(CinemaTheme.FONT_SMALL);
        lblStudio.setForeground(CinemaTheme.TEXT_MUTED);
        cbStudio = new JComboBox<>();
        CinemaTheme.styleComboBox(cbStudio);

        JButton btnAddStudio = CinemaTheme.createSmallButton("+", CinemaTheme.BURGUNDY, CinemaTheme.BURGUNDY_HOVER);
        btnAddStudio.setToolTipText("Tambah Studio Baru");
        btnAddStudio.addActionListener(e -> {
            new FormTambahStudio().setVisible(true);
            dispose();
        });

        JPanel studioInputBox = new JPanel(new BorderLayout(4, 0));
        studioInputBox.setOpaque(false);
        studioInputBox.add(cbStudio, BorderLayout.CENTER);
        studioInputBox.add(btnAddStudio, BorderLayout.EAST);

        pStudio.add(lblStudio, BorderLayout.NORTH);
        pStudio.add(studioInputBox, BorderLayout.CENTER);

        JPanel pHarga = new JPanel(new BorderLayout(4, 0));
        pHarga.setOpaque(false);
        JLabel lblHarga = new JLabel("Harga Tiket (Rp):");
        lblHarga.setFont(CinemaTheme.FONT_SMALL);
        lblHarga.setForeground(CinemaTheme.TEXT_MUTED);
        txtHargaTiket = new JTextField("45000");
        CinemaTheme.styleTextField(txtHargaTiket);
        pHarga.add(lblHarga, BorderLayout.NORTH);
        pHarga.add(txtHargaTiket, BorderLayout.CENTER);

        r5.add(pStudio);
        r5.add(pHarga);
        p.add(r5);
        p.add(Box.createVerticalStrut(10));

        // Row 6: Tanggal Tayang (Date Picker with Indonesian format) - Requirement 5A
        JPanel r6 = new JPanel(new BorderLayout(8, 0));
        r6.setOpaque(false);

        JLabel lblTglTag = new JLabel("Tanggal Tayang (Date Picker):");
        lblTglTag.setFont(CinemaTheme.FONT_SMALL);
        lblTglTag.setForeground(CinemaTheme.TEXT_MUTED);

        JPanel dateBox = new JPanel(new BorderLayout(8, 0));
        dateBox.setOpaque(false);

        lblTanggalDisplay = new JLabel(CinemaTheme.formatTanggal(selectedTanggal));
        lblTanggalDisplay.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblTanggalDisplay.setForeground(CinemaTheme.CREME);
        lblTanggalDisplay.setBackground(CinemaTheme.PANEL_BG);
        lblTanggalDisplay.setOpaque(true);
        lblTanggalDisplay.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        JButton btnPickDate = CinemaTheme.createPrimaryButton("📅 Pilih Tanggal");
        btnPickDate.addActionListener(e -> {
            LocalDate picked = ModernDatePickerDialog.showDialog(this, selectedTanggal);
            if (picked != null) {
                selectedTanggal = picked;
                lblTanggalDisplay.setText(CinemaTheme.formatTanggal(selectedTanggal));
                if (!txtId.getText().isBlank()) {
                    muatJadwalGrupTerpilih();
                }
            }
        });

        dateBox.add(lblTanggalDisplay, BorderLayout.CENTER);
        dateBox.add(btnPickDate, BorderLayout.EAST);

        r6.add(lblTglTag, BorderLayout.NORTH);
        r6.add(dateBox, BorderLayout.CENTER);
        p.add(r6);
        p.add(Box.createVerticalStrut(12));

        // Row 7: Jam Tayang (ShowtimeChipPanel) - Requirement 5B
        JLabel lblJamTag = new JLabel("Jam Tayang (Time Picker & Chips):");
        lblJamTag.setFont(CinemaTheme.FONT_SMALL);
        lblJamTag.setForeground(CinemaTheme.TEXT_MUTED);
        p.add(lblJamTag);
        p.add(Box.createVerticalStrut(4));

        showtimePanel = new ShowtimeChipPanel();
        p.add(showtimePanel);

        return p;
    }

    private void loadComboGenre() {
        cbGenre.removeAllItems();
        genreMap.clear();
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_genre, nama_genre FROM genre ORDER BY nama_genre")) {
            while (rs.next()) {
                int id = rs.getInt("id_genre");
                String nama = rs.getString("nama_genre");
                genreMap.put(nama, id);
                cbGenre.addItem(nama);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadComboStudio() {
        cbStudio.removeAllItems();
        studioMap.clear();
        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_studio, nama_studio FROM studio ORDER BY id_studio")) {
            while (rs.next()) {
                int id = rs.getInt("id_studio");
                String nama = rs.getString("nama_studio");
                studioMap.put(nama, id);
                cbStudio.addItem(nama);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadDataFilm() {
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Judul Film", "Genre", "Tahun", "Sutradara", "Durasi", "Rating", "URL Poster"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        String sql = "SELECT f.id_film, f.judul, g.nama_genre, f.tahun, f.sutradara, f.durasi_menit, f.rating, f.url_poster "
                + "FROM film f JOIN genre g ON g.id_genre = f.id_genre ORDER BY f.id_film DESC";

        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id_film"),
                        rs.getString("judul"),
                        rs.getString("nama_genre"),
                        rs.getInt("tahun"),
                        rs.getString("sutradara"),
                        rs.getInt("durasi_menit") + " mnt",
                        rs.getDouble("rating"),
                        rs.getString("url_poster")
                });
            }
            tableFilm.setModel(model);
            updateButtonStates(false);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cariDataFilm() {
        String keyword = txtSearch.getText().trim();
        DefaultTableModel model = new DefaultTableModel(
                new String[]{"ID", "Judul Film", "Genre", "Tahun", "Sutradara", "Durasi", "Rating", "URL Poster"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        String sql = "SELECT f.id_film, f.judul, g.nama_genre, f.tahun, f.sutradara, f.durasi_menit, f.rating, f.url_poster "
                + "FROM film f JOIN genre g ON g.id_genre = f.id_genre "
                + "WHERE f.judul LIKE ? OR f.sutradara LIKE ? ORDER BY f.id_film DESC";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("id_film"),
                            rs.getString("judul"),
                            rs.getString("nama_genre"),
                            rs.getInt("tahun"),
                            rs.getString("sutradara"),
                            rs.getInt("durasi_menit") + " mnt",
                            rs.getDouble("rating"),
                            rs.getString("url_poster")
                    });
                }
            }
            tableFilm.setModel(model);
            updateButtonStates(false);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void tampilkanFilmTerpilih() {
        int row = tableFilm.getSelectedRow();
        if (row == -1) {
            clearFields();
            return;
        }

        int modelRow = tableFilm.convertRowIndexToModel(row);
        DefaultTableModel m = (DefaultTableModel) tableFilm.getModel();

        int idFilm = (Integer) m.getValueAt(modelRow, 0);
        txtId.setText(String.valueOf(idFilm));
        txtJudul.setText(String.valueOf(m.getValueAt(modelRow, 1)));
        cbGenre.setSelectedItem(m.getValueAt(modelRow, 2));
        spinTahun.setValue((Integer) m.getValueAt(modelRow, 3));
        txtSutradara.setText(String.valueOf(m.getValueAt(modelRow, 4)));

        String durasiStr = String.valueOf(m.getValueAt(modelRow, 5)).replace(" mnt", "").trim();
        try {
            spinDurasi.setValue(Integer.parseInt(durasiStr));
        } catch (Exception ignored) {}

        spinRating.setValue(m.getValueAt(modelRow, 6));
        Object posterObj = m.getValueAt(modelRow, 7);
        String posterUrl = posterObj != null ? posterObj.toString() : "";
        txtUrlPoster.setText(posterUrl);

        if (!posterUrl.isBlank()) {
            PosterFetcher.muat(lblPosterPreview, posterUrl, 70, 95);
        } else {
            lblPosterPreview.setIcon(null);
            lblPosterPreview.setText("NO POSTER");
        }

        muatJadwalFilm(idFilm);
        updateButtonStates(true);
    }

    private void muatJadwalFilm(int idFilm) {
        String sql = "SELECT j.id_studio, j.mulai_tayang, j.harga_tiket "
                + "FROM jadwal_tayang j WHERE j.id_film = ? ORDER BY j.mulai_tayang ASC";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFilm);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int idStudio = rs.getInt("id_studio");
                    Timestamp ts = rs.getTimestamp("mulai_tayang");
                    BigDecimal harga = rs.getBigDecimal("harga_tiket");

                    selectedTanggal = ts.toLocalDateTime().toLocalDate();
                    lblTanggalDisplay.setText(CinemaTheme.formatTanggal(selectedTanggal));
                    txtHargaTiket.setText(harga.toPlainString());

                    // Find studio name
                    for (Map.Entry<String, Integer> entry : studioMap.entrySet()) {
                        if (entry.getValue() == idStudio) {
                            cbStudio.setSelectedItem(entry.getKey());
                            break;
                        }
                    }

                    // Load all showtimes for this film on that date & studio
                    muatJadwalGrupTerpilih();
                } else {
                    showtimePanel.clear();
                    filmGrupJadwalAwal = null;
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void muatJadwalGrupTerpilih() {
        if (txtId.getText().isBlank()) return;
        int idFilm = Integer.parseInt(txtId.getText());
        String namaStudio = (String) cbStudio.getSelectedItem();
        Integer idStudio = namaStudio == null ? null : studioMap.get(namaStudio);
        if (idStudio == null || selectedTanggal == null) return;

        String sql = "SELECT j.mulai_tayang, j.harga_tiket FROM jadwal_tayang j "
                + "WHERE j.id_film = ? AND j.id_studio = ? AND DATE(j.mulai_tayang) = ? "
                + "ORDER BY j.mulai_tayang ASC";

        List<LocalTime> times = new ArrayList<>();
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idFilm);
            ps.setInt(2, idStudio);
            ps.setDate(3, java.sql.Date.valueOf(selectedTanggal));
            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal harga = null;
                while (rs.next()) {
                    times.add(rs.getTimestamp("mulai_tayang").toLocalDateTime().toLocalTime());
                    if (harga == null) {
                        harga = rs.getBigDecimal("harga_tiket");
                    }
                }
                if (harga != null) {
                    txtHargaTiket.setText(harga.toPlainString());
                }
            }

            showtimePanel.setJamList(times);
            filmGrupJadwalAwal = idFilm;
            tanggalGrupJadwalAwal = selectedTanggal;
            studioGrupJadwalAwal = idStudio;

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateButtonStates(boolean rowSelected) {
        btnTambah.setEnabled(!rowSelected);
        btnEdit.setEnabled(rowSelected);
        btnHapus.setEnabled(rowSelected);
    }

    private void clearFields() {
        txtId.setText("");
        txtJudul.setText("");
        if (cbGenre.getItemCount() > 0) cbGenre.setSelectedIndex(0);
        spinTahun.setValue(2026);
        txtSutradara.setText("");
        spinDurasi.setValue(120);
        spinRating.setValue(8.0);
        txtUrlPoster.setText("");
        lblPosterPreview.setIcon(null);
        lblPosterPreview.setText("POSTER");

        if (cbStudio.getItemCount() > 0) cbStudio.setSelectedIndex(0);
        txtHargaTiket.setText("45000");
        selectedTanggal = LocalDate.now();
        lblTanggalDisplay.setText(CinemaTheme.formatTanggal(selectedTanggal));
        showtimePanel.clear();

        tableFilm.clearSelection();
        updateButtonStates(false);
        filmGrupJadwalAwal = null;
        tanggalGrupJadwalAwal = null;
        studioGrupJadwalAwal = null;
    }

    private void tambahFilm() {
        String judul = txtJudul.getText().trim();
        String sutradara = txtSutradara.getText().trim();
        String namaGenre = (String) cbGenre.getSelectedItem();
        Integer idGenre = namaGenre != null ? genreMap.get(namaGenre) : null;
        int tahun = (Integer) spinTahun.getValue();
        int durasi = (Integer) spinDurasi.getValue();
        double rating = ((Number) spinRating.getValue()).doubleValue();
        String urlPoster = txtUrlPoster.getText().trim();

        if (judul.isEmpty() || sutradara.isEmpty() || idGenre == null) {
            JOptionPane.showMessageDialog(this, "Judul, Sutradara, dan Genre wajib diisi!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validate schedule if showtimes provided
        List<LocalTime> jamList = showtimePanel.getJamList();
        String namaStudio = (String) cbStudio.getSelectedItem();
        Integer idStudio = namaStudio != null ? studioMap.get(namaStudio) : null;
        BigDecimal hargaTiket;
        try {
            hargaTiket = new BigDecimal(txtHargaTiket.getText().trim().replace(',', '.'));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Harga tiket tidak valid!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);
            int newFilmId;

            // 1. Insert Film
            String sqlFilm = "INSERT INTO film (judul, id_genre, tahun, sutradara, durasi_menit, rating, url_poster) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlFilm, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, judul);
                ps.setInt(2, idGenre);
                ps.setInt(3, tahun);
                ps.setString(4, sutradara);
                ps.setInt(5, durasi);
                ps.setDouble(6, rating);
                ps.setString(7, urlPoster.isEmpty() ? null : urlPoster);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Gagal membuat film baru.");
                    newFilmId = rs.getInt(1);
                }
            }

            // 2. Insert Showtimes with Conflict Validation
            if (!jamList.isEmpty() && idStudio != null) {
                String sqlJadwal = "INSERT INTO jadwal_tayang (id_film, id_studio, mulai_tayang, harga_tiket) VALUES (?, ?, ?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sqlJadwal)) {
                    for (LocalTime t : jamList) {
                        LocalDateTime mulai = LocalDateTime.of(selectedTanggal, t);

                        // Check collision with other movies in same studio
                        if (PemesananService.cekBentrokJadwal(idStudio, mulai, durasi, null)) {
                            conn.rollback();
                            JOptionPane.showMessageDialog(this,
                                    "Jadwal tayang " + CinemaTheme.TIME_FORMAT.format(t)
                                            + " bertabrakan dengan jadwal film lain di studio yang sama!",
                                    "Jadwal Bertabrakan", JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        ps.setInt(1, newFilmId);
                        ps.setInt(2, idStudio);
                        ps.setTimestamp(3, Timestamp.valueOf(mulai));
                        ps.setBigDecimal(4, hargaTiket);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Film \"" + judul + "\" berhasil ditambahkan beserta jadwal tayang!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadDataFilm();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menyimpan data film: " + ex.getMessage(),
                    "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editFilm() {
        if (txtId.getText().isBlank()) return;
        int idFilm = Integer.parseInt(txtId.getText());
        String judul = txtJudul.getText().trim();
        String sutradara = txtSutradara.getText().trim();
        String namaGenre = (String) cbGenre.getSelectedItem();
        Integer idGenre = namaGenre != null ? genreMap.get(namaGenre) : null;
        int tahun = (Integer) spinTahun.getValue();
        int durasi = (Integer) spinDurasi.getValue();
        double rating = ((Number) spinRating.getValue()).doubleValue();
        String urlPoster = txtUrlPoster.getText().trim();

        List<LocalTime> jamList = showtimePanel.getJamList();
        String namaStudio = (String) cbStudio.getSelectedItem();
        Integer idStudio = namaStudio != null ? studioMap.get(namaStudio) : null;
        BigDecimal hargaTiket;
        try {
            hargaTiket = new BigDecimal(txtHargaTiket.getText().trim().replace(',', '.'));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Harga tiket tidak valid!", "Validasi Form", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);

            // 1. Update Film Info
            String sqlFilm = "UPDATE film SET judul = ?, id_genre = ?, tahun = ?, sutradara = ?, durasi_menit = ?, "
                    + "rating = ?, url_poster = ? WHERE id_film = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlFilm)) {
                ps.setString(1, judul);
                ps.setInt(2, idGenre);
                ps.setInt(3, tahun);
                ps.setString(4, sutradara);
                ps.setInt(5, durasi);
                ps.setDouble(6, rating);
                ps.setString(7, urlPoster.isEmpty() ? null : urlPoster);
                ps.setInt(8, idFilm);
                ps.executeUpdate();
            }

            // 2. Update Showtimes: Delete old un-booked showtimes for this date & re-insert
            if (idStudio != null) {
                // Delete existing schedules for this film, studio, and date that do NOT have booked tickets
                String delSql = "DELETE FROM jadwal_tayang WHERE id_film = ? AND id_studio = ? "
                        + "AND DATE(mulai_tayang) = ? AND id_jadwal NOT IN (SELECT DISTINCT id_jadwal FROM tiket)";
                try (PreparedStatement ps = conn.prepareStatement(delSql)) {
                    ps.setInt(1, idFilm);
                    ps.setInt(2, idStudio);
                    ps.setDate(3, java.sql.Date.valueOf(selectedTanggal));
                    ps.executeUpdate();
                }

                // Insert updated showtimes
                if (!jamList.isEmpty()) {
                    String insertSql = "INSERT IGNORE INTO jadwal_tayang (id_film, id_studio, mulai_tayang, harga_tiket) VALUES (?, ?, ?, ?)";
                    try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                        for (LocalTime t : jamList) {
                            LocalDateTime mulai = LocalDateTime.of(selectedTanggal, t);
                            ps.setInt(1, idFilm);
                            ps.setInt(2, idStudio);
                            ps.setTimestamp(3, Timestamp.valueOf(mulai));
                            ps.setBigDecimal(4, hargaTiket);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                    }
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Data film dan jadwal tayang berhasil diperbarui!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadDataFilm();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengedit film: " + ex.getMessage(),
                    "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusFilm() {
        if (txtId.getText().isBlank()) return;
        int idFilm = Integer.parseInt(txtId.getText());
        String judul = txtJudul.getText();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus film \"" + judul + "\" beserta seluruh jadwal tayangnya?",
                "Konfirmasi Hapus Film",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Koneksi.getConnection()) {
            // First delete related showtimes that don't have tickets
            String delJadwal = "DELETE FROM jadwal_tayang WHERE id_film = ?";
            try (PreparedStatement ps = conn.prepareStatement(delJadwal)) {
                ps.setInt(1, idFilm);
                ps.executeUpdate();
            }

            String delFilm = "DELETE FROM film WHERE id_film = ?";
            try (PreparedStatement ps = conn.prepareStatement(delFilm)) {
                ps.setInt(1, idFilm);
                ps.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Film berhasil dihapus.", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            clearFields();
            loadDataFilm();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Film tidak dapat dihapus karena sudah memiliki tiket transaksi tersimpan.\n" + ex.getMessage(),
                    "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void pilihDanUploadPoster() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Pilih Berkas Poster Film");
        chooser.setFileFilter(new FileNameExtensionFilter("Gambar (JPG, PNG, WEBP)", "jpg", "jpeg", "png", "webp"));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            txtUrlPoster.setText(selectedFile.getAbsolutePath());
            PosterFetcher.muat(lblPosterPreview, selectedFile.getAbsolutePath(), 70, 95);
        }
    }
}
