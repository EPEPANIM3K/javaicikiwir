package aplikasdatafilm;

import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Cinema Ticket Booking & Seat Selection Screen (1280x720).
 * Features realistic cinema screen curve, interactive seat matrix (A1 to E4),
 * real-time booking status from database, ticket quantity stepper, and live subtotal calculation.
 */
public class BeliTiket extends JFrame {

    private final long idPengguna;
    private final PilihFilm parentFrame;
    private PemesananService.Jadwal jadwalAktif;
    private List<PemesananService.Jadwal> daftarJadwalFilm = new ArrayList<>();

    private List<PemesananService.Kursi> semuaKursi = new ArrayList<>();
    private final Set<String> kursiTerpilih = new LinkedHashSet<>();
    private int jumlahTiket = 1;

    // Seat buttons map
    private final Map<String, JButton> tombolKursiMap = new HashMap<>();

    // Controls
    private JComboBox<String> cbTanggal;
    private JComboBox<String> cbJam;
    private JComboBox<String> cbStudio;
    private JLabel lblJumlahTiket;
    private JLabel lblKursiTerpilih;
    private JLabel lblHargaSatuan;
    private JLabel lblTotal;
    private boolean isUpdatingDropdown = false;

    private static final DateTimeFormatter FMT_TGL = DateTimeFormatter.ofPattern("dd MMMM yyyy", CinemaTheme.LOCALE_ID);
    private static final DateTimeFormatter FMT_JAM = DateTimeFormatter.ofPattern("HH:mm");

    public BeliTiket() {
        this(1, null, null);
    }

    public BeliTiket(long idPengguna, PemesananService.Jadwal jadwal) {
        this(idPengguna, jadwal, null);
    }

    public BeliTiket(long idPengguna, PemesananService.Jadwal jadwal, PilihFilm parentFrame) {
        this.idPengguna = idPengguna;
        this.jadwalAktif = jadwal;
        this.parentFrame = parentFrame;

        CinemaTheme.standardizeFrame(this, "TIKET KU — Pemilihan Kursi Bioskop");
        buildUI();
        muatDaftarJadwalDanDropdown();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Denah Kursi Studio & Pemesanan", "KASIR OPERASIONAL", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // 2-Column Split
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(16, 28, 16, 28));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ── LEFT: CINEMA SEAT HALL (Weight 0.58) ────────────────────────────
        gbc.gridx = 0;
        gbc.weightx = 0.58;
        gbc.insets = new Insets(0, 0, 0, 18);

        JPanel seatCard = CinemaTheme.createCard();
        seatCard.setLayout(new BorderLayout(0, 16));

        // Screen Banner
        JPanel screenPanel = new JPanel();
        screenPanel.setLayout(new BoxLayout(screenPanel, BoxLayout.Y_AXIS));
        screenPanel.setOpaque(false);

        JLabel lblScreen = new JLabel("═══════════════ LAYAR BIOSKOP / SCREEN ═══════════════", SwingConstants.CENTER);
        lblScreen.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblScreen.setForeground(CinemaTheme.ACCENT_GOLD);
        lblScreen.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel screenBar = new JPanel();
        screenBar.setBackground(CinemaTheme.BURGUNDY);
        screenBar.setPreferredSize(new Dimension(500, 6));
        screenBar.setMaximumSize(new Dimension(500, 6));
        screenBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        screenPanel.add(lblScreen);
        screenPanel.add(Box.createVerticalStrut(4));
        screenPanel.add(screenBar);
        seatCard.add(screenPanel, BorderLayout.NORTH);

        // Seat Grid Center (5 Rows: A-E, 4 Columns: 1-4 with aisle in middle)
        JPanel seatGrid = new JPanel(new GridLayout(5, 5, 12, 12));
        seatGrid.setOpaque(false);
        seatGrid.setBorder(new EmptyBorder(16, 40, 16, 40));

        String[] rows = {"A", "B", "C", "D", "E"};
        for (String r : rows) {
            // Left pair: 1, 2
            seatGrid.add(createSeatButton(r + "1"));
            seatGrid.add(createSeatButton(r + "2"));

            // Aisle spacer
            JLabel lblAisle = new JLabel(r, SwingConstants.CENTER);
            lblAisle.setFont(CinemaTheme.FONT_BODY_BOLD);
            lblAisle.setForeground(CinemaTheme.TEXT_MUTED);
            seatGrid.add(lblAisle);

            // Right pair: 3, 4
            seatGrid.add(createSeatButton(r + "3"));
            seatGrid.add(createSeatButton(r + "4"));
        }
        seatCard.add(seatGrid, BorderLayout.CENTER);

        // Legend at Bottom
        JPanel legendPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 24, 6));
        legendPanel.setOpaque(false);
        legendPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, CinemaTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(8, 0, 0, 0)
        ));

        legendPanel.add(createLegendItem(CinemaTheme.CREME, CinemaTheme.INPUT_TEXT, "Tersedia"));
        legendPanel.add(createLegendItem(CinemaTheme.BURGUNDY, CinemaTheme.CREME, "Terpilih"));
        legendPanel.add(createLegendItem(new Color(0x33, 0x2C, 0x33), CinemaTheme.TEXT_MUTED, "Sudah Dipesan"));
        seatCard.add(legendPanel, BorderLayout.SOUTH);

        content.add(seatCard, gbc);

        // ── RIGHT: SUMMARY & SESSION CONTROLS (Weight 0.42) ─────────────────
        gbc.gridx = 1;
        gbc.weightx = 0.42;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel rightCard = CinemaTheme.createCard();
        rightCard.setLayout(new BoxLayout(rightCard, BoxLayout.Y_AXIS));

        // Film Title & Basic Info
        JLabel lblFilmTitle = new JLabel(jadwalAktif != null ? jadwalAktif.judul() : "-");
        lblFilmTitle.setFont(CinemaTheme.FONT_TITLE);
        lblFilmTitle.setForeground(CinemaTheme.CREME);
        rightCard.add(lblFilmTitle);

        JLabel lblGenreDurasi = new JLabel(jadwalAktif != null ? (jadwalAktif.genre() + " | Durasi: " + jadwalAktif.durasiMenit() + " Menit") : "-");
        lblGenreDurasi.setFont(CinemaTheme.FONT_SMALL);
        lblGenreDurasi.setForeground(CinemaTheme.TEXT_MUTED);
        rightCard.add(lblGenreDurasi);

        rightCard.add(Box.createVerticalStrut(14));

        // Dropdowns: Tanggal, Jam, Studio
        JPanel ddGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        ddGrid.setOpaque(false);
        ddGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel l1 = new JLabel("Tanggal Tayang:");
        l1.setFont(CinemaTheme.FONT_SMALL);
        l1.setForeground(CinemaTheme.TEXT_MUTED);
        cbTanggal = new JComboBox<>();
        CinemaTheme.styleComboBox(cbTanggal);

        JLabel l2 = new JLabel("Jam Tayang:");
        l2.setFont(CinemaTheme.FONT_SMALL);
        l2.setForeground(CinemaTheme.TEXT_MUTED);
        cbJam = new JComboBox<>();
        CinemaTheme.styleComboBox(cbJam);

        JLabel l3 = new JLabel("Studio:");
        l3.setFont(CinemaTheme.FONT_SMALL);
        l3.setForeground(CinemaTheme.TEXT_MUTED);
        cbStudio = new JComboBox<>();
        CinemaTheme.styleComboBox(cbStudio);

        ddGrid.add(l1); ddGrid.add(cbTanggal);
        ddGrid.add(l2); ddGrid.add(cbJam);
        ddGrid.add(l3); ddGrid.add(cbStudio);
        rightCard.add(ddGrid);

        rightCard.add(Box.createVerticalStrut(14));

        // Ticket Counter Stepper: [ - ] [ 1 Tiket ] [ + ]
        JPanel counterPanel = new JPanel(new BorderLayout(8, 0));
        counterPanel.setOpaque(false);
        counterPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        JLabel lblCountTag = new JLabel("Jumlah Tiket:");
        lblCountTag.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblCountTag.setForeground(CinemaTheme.CREME);

        JPanel stepper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        stepper.setOpaque(false);

        JButton btnMinus = CinemaTheme.createSmallButton("—", CinemaTheme.CARD_BG, CinemaTheme.CARD_HOVER);
        btnMinus.setPreferredSize(new Dimension(36, 32));
        btnMinus.addActionListener(e -> kurangTiket());

        lblJumlahTiket = new JLabel("1", SwingConstants.CENTER);
        lblJumlahTiket.setFont(CinemaTheme.FONT_SUBTITLE);
        lblJumlahTiket.setForeground(CinemaTheme.CREME);
        lblJumlahTiket.setPreferredSize(new Dimension(32, 32));

        JButton btnPlus = CinemaTheme.createSmallButton("+", CinemaTheme.BURGUNDY, CinemaTheme.BURGUNDY_HOVER);
        btnPlus.setPreferredSize(new Dimension(36, 32));
        btnPlus.addActionListener(e -> tambahTiket());

        stepper.add(btnMinus);
        stepper.add(lblJumlahTiket);
        stepper.add(btnPlus);

        counterPanel.add(lblCountTag, BorderLayout.WEST);
        counterPanel.add(stepper, BorderLayout.EAST);
        rightCard.add(counterPanel);

        rightCard.add(Box.createVerticalStrut(12));

        // Details Grid (Kursi Terpilih, Harga Satuan, Total)
        JPanel sumGrid = new JPanel(new GridLayout(3, 2, 8, 8));
        sumGrid.setOpaque(false);
        sumGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, CinemaTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 0, 12, 0)
        ));

        lblKursiTerpilih = addSummaryRow(sumGrid, "Kursi Terpilih", "(Belum ada)");
        lblHargaSatuan = addSummaryRow(sumGrid, "Harga Tiket", CinemaTheme.formatRupiah(jadwalAktif != null ? jadwalAktif.hargaTiket() : BigDecimal.ZERO));
        lblTotal = addSummaryRow(sumGrid, "Total Harga", CinemaTheme.formatRupiah(jadwalAktif != null ? jadwalAktif.hargaTiket() : BigDecimal.ZERO));
        lblTotal.setFont(CinemaTheme.FONT_TITLE);
        lblTotal.setForeground(CinemaTheme.ACCENT_GOLD);
        rightCard.add(sumGrid);

        rightCard.add(Box.createVerticalGlue());

        // Navigation Actions
        JPanel actPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        actPanel.setOpaque(false);
        actPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        JButton btnBack = CinemaTheme.createSecondaryButton("← Katalog Film");
        btnBack.addActionListener(e -> kembaliKePilihFilm());

        JButton btnCheckout = CinemaTheme.createPrimaryButton("Lanjut Checkout →");
        btnCheckout.setFont(CinemaTheme.FONT_BODY_BOLD);
        btnCheckout.addActionListener(e -> prosesCheckout());

        actPanel.add(btnBack);
        actPanel.add(btnCheckout);
        rightCard.add(actPanel);

        content.add(rightCard, gbc);
        root.add(content, BorderLayout.CENTER);

        getContentPane().add(root);
    }

    private JButton createSeatButton(String kode) {
        JButton btn = new JButton(kode);
        btn.setFont(CinemaTheme.FONT_BODY_BOLD);
        btn.setBackground(CinemaTheme.CREME);
        btn.setForeground(CinemaTheme.INPUT_TEXT);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 1));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> onKursiClicked(kode));
        tombolKursiMap.put(kode, btn);
        return btn;
    }

    private JPanel createLegendItem(Color bg, Color fg, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);

        JLabel box = new JLabel("  ");
        box.setOpaque(true);
        box.setBackground(bg);
        box.setBorder(BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1));
        box.setPreferredSize(new Dimension(18, 18));

        JLabel txt = new JLabel(label);
        txt.setFont(CinemaTheme.FONT_SMALL);
        txt.setForeground(CinemaTheme.TEXT_MUTED);

        p.add(box);
        p.add(txt);
        return p;
    }

    private JLabel addSummaryRow(JPanel panel, String label, String value) {
        JLabel lblK = new JLabel(label + ":");
        lblK.setFont(CinemaTheme.FONT_BODY);
        lblK.setForeground(CinemaTheme.TEXT_MUTED);

        JLabel lblV = new JLabel(value);
        lblV.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblV.setForeground(CinemaTheme.CREME);

        panel.add(lblK);
        panel.add(lblV);
        return lblV;
    }

    private void muatDaftarJadwalDanDropdown() {
        if (jadwalAktif == null) return;
        try {
            daftarJadwalFilm = PemesananService.jadwalUntukFilm(jadwalAktif.idFilm());
            if (daftarJadwalFilm.isEmpty()) {
                daftarJadwalFilm = new ArrayList<>(List.of(jadwalAktif));
            }
        } catch (SQLException ex) {
            daftarJadwalFilm = new ArrayList<>(List.of(jadwalAktif));
        }

        cbTanggal.addActionListener(e -> {
            if (!isUpdatingDropdown) updateJamDropdown();
        });
        cbJam.addActionListener(e -> {
            if (!isUpdatingDropdown) updateStudioDropdown();
        });
        cbStudio.addActionListener(e -> {
            if (!isUpdatingDropdown) syncJadwalAktif();
        });

        updateTanggalDropdown();
    }

    private void updateTanggalDropdown() {
        isUpdatingDropdown = true;
        cbTanggal.removeAllItems();
        Set<LocalDate> dateSet = new LinkedHashSet<>();
        for (PemesananService.Jadwal j : daftarJadwalFilm) {
            dateSet.add(j.mulaiTayang().toLocalDate());
        }
        for (LocalDate d : dateSet) {
            cbTanggal.addItem(FMT_TGL.format(d));
        }
        if (jadwalAktif != null) {
            cbTanggal.setSelectedItem(FMT_TGL.format(jadwalAktif.mulaiTayang()));
        }
        isUpdatingDropdown = false;
        updateJamDropdown();
    }

    private void updateJamDropdown() {
        isUpdatingDropdown = true;
        cbJam.removeAllItems();
        String selDate = (String) cbTanggal.getSelectedItem();
        if (selDate != null) {
            Set<LocalTime> times = new LinkedHashSet<>();
            for (PemesananService.Jadwal j : daftarJadwalFilm) {
                if (FMT_TGL.format(j.mulaiTayang()).equals(selDate)) {
                    times.add(j.mulaiTayang().toLocalTime());
                }
            }
            for (LocalTime t : times) {
                cbJam.addItem(FMT_JAM.format(t));
            }
            if (jadwalAktif != null) {
                cbJam.setSelectedItem(FMT_JAM.format(jadwalAktif.mulaiTayang()));
            }
        }
        isUpdatingDropdown = false;
        updateStudioDropdown();
    }

    private void updateStudioDropdown() {
        isUpdatingDropdown = true;
        cbStudio.removeAllItems();
        String selDate = (String) cbTanggal.getSelectedItem();
        String selJam = (String) cbJam.getSelectedItem();
        if (selDate != null && selJam != null) {
            for (PemesananService.Jadwal j : daftarJadwalFilm) {
                if (FMT_TGL.format(j.mulaiTayang()).equals(selDate)
                        && FMT_JAM.format(j.mulaiTayang()).equals(selJam)) {
                    cbStudio.addItem(j.namaStudio());
                }
            }
            if (jadwalAktif != null) {
                cbStudio.setSelectedItem(jadwalAktif.namaStudio());
            }
        }
        isUpdatingDropdown = false;
        syncJadwalAktif();
    }

    private void syncJadwalAktif() {
        String selDate = (String) cbTanggal.getSelectedItem();
        String selJam = (String) cbJam.getSelectedItem();
        String selStudio = (String) cbStudio.getSelectedItem();
        if (selDate == null || selJam == null || selStudio == null) return;

        for (PemesananService.Jadwal j : daftarJadwalFilm) {
            if (FMT_TGL.format(j.mulaiTayang()).equals(selDate)
                    && FMT_JAM.format(j.mulaiTayang()).equals(selJam)
                    && j.namaStudio().equals(selStudio)) {
                jadwalAktif = j;
                break;
            }
        }
        muatKursiStudio();
    }

    private void muatKursiStudio() {
        if (jadwalAktif == null) return;
        try {
            semuaKursi = PemesananService.kursiUntukJadwal(jadwalAktif.idJadwal());
        } catch (SQLException ex) {
            ex.printStackTrace();
            semuaKursi = new ArrayList<>();
        }
        kursiTerpilih.clear();
        updateTampilanKursi();
        updateSummary();
    }

    private boolean isKursiSudahDipesan(String kode) {
        for (PemesananService.Kursi k : semuaKursi) {
            if (k.kodeKursi().equalsIgnoreCase(kode)) {
                return k.sudahDipesan();
            }
        }
        return false;
    }

    private void onKursiClicked(String kode) {
        if (kursiTerpilih.contains(kode)) {
            kursiTerpilih.remove(kode);
        } else {
            if (kursiTerpilih.size() < jumlahTiket) {
                kursiTerpilih.add(kode);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Anda telah memilih " + jumlahTiket + " kursi sesuai jumlah tiket.\n"
                                + "Batalkan salah satu kursi jika ingin mengubah, atau tambah jumlah tiket.",
                        "Batas Kursi Terpilih", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        updateTampilanKursi();
        updateSummary();
    }

    private void updateTampilanKursi() {
        for (Map.Entry<String, JButton> entry : tombolKursiMap.entrySet()) {
            String kode = entry.getKey();
            JButton btn = entry.getValue();
            boolean dipesan = isKursiSudahDipesan(kode);

            if (dipesan) {
                btn.setEnabled(false);
                btn.setBackground(new Color(0x33, 0x2C, 0x33));
                btn.setForeground(CinemaTheme.TEXT_MUTED);
                btn.setText("✕");
                btn.setToolTipText("Kursi " + kode + " sudah dipesan");
            } else if (kursiTerpilih.contains(kode)) {
                btn.setEnabled(true);
                btn.setBackground(CinemaTheme.BURGUNDY);
                btn.setForeground(CinemaTheme.CREME);
                btn.setText(kode);
                btn.setToolTipText("Kursi " + kode + " terpilih");
            } else {
                btn.setEnabled(true);
                btn.setBackground(CinemaTheme.CREME);
                btn.setForeground(CinemaTheme.INPUT_TEXT);
                btn.setText(kode);
                btn.setToolTipText("Klik untuk memilih kursi " + kode);
            }
        }
    }

    private void tambahTiket() {
        int max = 20; // total 20 seats
        if (jumlahTiket < max) {
            jumlahTiket++;
            lblJumlahTiket.setText(String.valueOf(jumlahTiket));
            updateSummary();
        }
    }

    private void kurangTiket() {
        if (jumlahTiket > 1) {
            jumlahTiket--;
            while (kursiTerpilih.size() > jumlahTiket) {
                String last = null;
                for (String k : kursiTerpilih) last = k;
                if (last != null) kursiTerpilih.remove(last);
            }
            lblJumlahTiket.setText(String.valueOf(jumlahTiket));
            updateTampilanKursi();
            updateSummary();
        }
    }

    private void updateSummary() {
        if (jadwalAktif == null) return;
        BigDecimal harga = jadwalAktif.hargaTiket();
        BigDecimal total = harga.multiply(BigDecimal.valueOf(jumlahTiket));

        lblHargaSatuan.setText(CinemaTheme.formatRupiah(harga));
        lblTotal.setText(CinemaTheme.formatRupiah(total));

        if (kursiTerpilih.isEmpty()) {
            lblKursiTerpilih.setText("(Belum dipilih)");
            lblKursiTerpilih.setForeground(CinemaTheme.TEXT_MUTED);
        } else {
            lblKursiTerpilih.setText(String.join(", ", kursiTerpilih));
            lblKursiTerpilih.setForeground(CinemaTheme.ACCENT_GOLD);
        }
    }

    private void kembaliKePilihFilm() {
        dispose();
        if (parentFrame != null) {
            parentFrame.setVisible(true);
        } else {
            new PilihFilm(idPengguna).setVisible(true);
        }
    }

    private void prosesCheckout() {
        if (jadwalAktif == null) return;
        if (kursiTerpilih.size() != jumlahTiket) {
            JOptionPane.showMessageDialog(this,
                    "Silakan pilih " + jumlahTiket + " kursi di denah studio sebelum melanjutkan.\n"
                            + "Saat ini baru dipilih: " + kursiTerpilih.size() + " kursi.",
                    "Pilihan Kursi Belum Lengkap", JOptionPane.WARNING_MESSAGE);
            return;
        }

        List<PemesananService.Kursi> listKursiDipilih = new ArrayList<>();
        for (String kode : kursiTerpilih) {
            for (PemesananService.Kursi k : semuaKursi) {
                if (k.kodeKursi().equalsIgnoreCase(kode)) {
                    listKursiDipilih.add(k);
                    break;
                }
            }
        }

        dispose();
        new CheckOut(idPengguna, jadwalAktif, listKursiDipilih).setVisible(true);
    }
}
