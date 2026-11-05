package aplikasdatafilm;

import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Checkout Screen (1280x720).
 * Features two simplified payment methods: QRIS and CASH.
 * Does NOT generate tickets until payment is explicitly confirmed on the subsequent payment page.
 */
public class CheckOut extends JFrame {

    private final long idPengguna;
    private final PemesananService.Jadwal jadwal;
    private final List<PemesananService.Kursi> kursi;
    private final BigDecimal subtotal;

    private JRadioButton rbQris;
    private JRadioButton rbCash;
    private JPanel cardQris;
    private JPanel cardCash;

    public CheckOut() {
        this(0, null, List.of());
    }

    public CheckOut(long idPengguna, PemesananService.Jadwal jadwal, List<PemesananService.Kursi> kursi) {
        this.idPengguna = idPengguna;
        this.jadwal = jadwal;
        this.kursi = (kursi == null) ? List.of() : List.copyOf(kursi);
        this.subtotal = (jadwal != null && !this.kursi.isEmpty())
                ? jadwal.hargaTiket().multiply(BigDecimal.valueOf(this.kursi.size()))
                : BigDecimal.ZERO;

        CinemaTheme.standardizeFrame(this, "TIKET KU — Kasir Checkout");
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Checkout & Metode Pembayaran", "KASIR BIOSKOP", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Content (2-Column Layout)
        JPanel content = new JPanel(new GridLayout(1, 2, 28, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 36, 24, 36));

        // ── LEFT COLUMN: FILM & TICKET DETAILS ─────────────────────────────
        JPanel leftCard = CinemaTheme.createCard();
        leftCard.setLayout(new BorderLayout(16, 16));

        // Top info with poster thumbnail & basic details
        JPanel moviePanel = new JPanel(new BorderLayout(16, 0));
        moviePanel.setOpaque(false);

        JLabel lblPoster = new JLabel();
        lblPoster.setPreferredSize(new Dimension(140, 200));
        lblPoster.setBackground(CinemaTheme.PANEL_BG);
        lblPoster.setOpaque(true);
        lblPoster.setHorizontalAlignment(SwingConstants.CENTER);
        lblPoster.setBorder(BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 1));
        if (jadwal != null && jadwal.urlPoster() != null && !jadwal.urlPoster().isBlank()) {
            PosterFetcher.muat(lblPoster, jadwal.urlPoster(), 140, 200);
        } else {
            lblPoster.setText("<html><center>POSTER<br>FILM</center></html>");
            lblPoster.setForeground(CinemaTheme.TEXT_MUTED);
        }
        moviePanel.add(lblPoster, BorderLayout.WEST);

        JPanel movieInfo = new JPanel();
        movieInfo.setOpaque(false);
        movieInfo.setLayout(new BoxLayout(movieInfo, BoxLayout.Y_AXIS));

        JLabel lblJudul = new JLabel(jadwal != null ? jadwal.judul() : "-");
        lblJudul.setFont(CinemaTheme.FONT_TITLE);
        lblJudul.setForeground(CinemaTheme.CREME);
        movieInfo.add(lblJudul);

        movieInfo.add(Box.createVerticalStrut(8));

        JLabel lblStudio = new JLabel("🎬 Studio: " + (jadwal != null ? jadwal.namaStudio() : "-"));
        lblStudio.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblStudio.setForeground(CinemaTheme.ACCENT_GOLD);
        movieInfo.add(lblStudio);

        movieInfo.add(Box.createVerticalStrut(4));

        JLabel lblWaktu = new JLabel("🕒 " + (jadwal != null ? CinemaTheme.formatTanggalWaktu(jadwal.mulaiTayang()) + " WIB" : "-"));
        lblWaktu.setFont(CinemaTheme.FONT_BODY);
        lblWaktu.setForeground(CinemaTheme.TEXT_MUTED);
        movieInfo.add(lblWaktu);

        movieInfo.add(Box.createVerticalStrut(4));

        int durasi = jadwal != null ? jadwal.durasiMenit() : 0;
        int j = durasi / 60, m = durasi % 60;
        String durStr = j > 0 ? (j + " Jam " + (m > 0 ? m + " Menit" : "")) : (m + " Menit");
        JLabel lblDurasi = new JLabel("⏳ Durasi: " + durStr);
        lblDurasi.setFont(CinemaTheme.FONT_BODY);
        lblDurasi.setForeground(CinemaTheme.TEXT_MUTED);
        movieInfo.add(lblDurasi);

        moviePanel.add(movieInfo, BorderLayout.CENTER);
        leftCard.add(moviePanel, BorderLayout.NORTH);

        // Center Details Grid
        JPanel detailGrid = new JPanel(new GridLayout(4, 2, 8, 10));
        detailGrid.setOpaque(false);
        detailGrid.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 0, CinemaTheme.CARD_BORDER),
                BorderFactory.createEmptyBorder(14, 0, 14, 0)
        ));

        String seatCodes = String.join(", ", kursi.stream().map(PemesananService.Kursi::kodeKursi).toList());
        addDetailRow(detailGrid, "Kursi Terpilih", seatCodes.isEmpty() ? "-" : seatCodes);
        addDetailRow(detailGrid, "Jumlah Tiket", kursi.size() + " Tiket");
        addDetailRow(detailGrid, "Harga Tiket", jadwal != null ? CinemaTheme.formatRupiah(jadwal.hargaTiket()) : "-");
        addDetailRow(detailGrid, "Biaya Layanan", "Rp 0 (Kasir)");

        leftCard.add(detailGrid, BorderLayout.CENTER);

        // Bottom Total
        JPanel totalBar = new JPanel(new BorderLayout());
        totalBar.setOpaque(false);

        JLabel lblTotKey = new JLabel("TOTAL TAGIHAN");
        lblTotKey.setFont(CinemaTheme.FONT_SUBTITLE);
        lblTotKey.setForeground(CinemaTheme.CREME);

        JLabel lblTotVal = new JLabel(CinemaTheme.formatRupiah(subtotal));
        lblTotVal.setFont(CinemaTheme.FONT_BIG_NUMBER);
        lblTotVal.setForeground(CinemaTheme.ACCENT_GOLD);

        totalBar.add(lblTotKey, BorderLayout.WEST);
        totalBar.add(lblTotVal, BorderLayout.EAST);
        leftCard.add(totalBar, BorderLayout.SOUTH);

        content.add(leftCard);

        // ── RIGHT COLUMN: SIMPLIFIED PAYMENT METHODS (QRIS & CASH) ──────────
        JPanel rightCard = CinemaTheme.createCard();
        rightCard.setLayout(new BoxLayout(rightCard, BoxLayout.Y_AXIS));

        JLabel lblPilih = new JLabel("PILIH METODE PEMBAYARAN");
        lblPilih.setFont(CinemaTheme.FONT_SUBTITLE);
        lblPilih.setForeground(CinemaTheme.CREME);
        rightCard.add(lblPilih);

        JLabel lblSub = new JLabel("Pilih salah satu metode pembayaran di bawah untuk melanjutkan.");
        lblSub.setFont(CinemaTheme.FONT_SMALL);
        lblSub.setForeground(CinemaTheme.TEXT_MUTED);
        rightCard.add(lblSub);

        rightCard.add(Box.createVerticalStrut(20));

        ButtonGroup bg = new ButtonGroup();

        // 1. QRIS Card
        cardQris = createPaymentMethodOption("📱 QRIS (Quick Response Code Indonesian Standard)",
                "Scan kode QR dummy melalui GoPay, OVO, Dana, BCA, atau Mobile Banking.", true);
        rbQris = (JRadioButton) cardQris.getClientProperty("radio");
        bg.add(rbQris);
        rightCard.add(cardQris);

        rightCard.add(Box.createVerticalStrut(14));

        // 2. CASH Card
        cardCash = createPaymentMethodOption("💵 Tunai (Cash)",
                "Pembayaran langsung dengan uang tunai melalui loket kasir bioskop.", false);
        rbCash = (JRadioButton) cardCash.getClientProperty("radio");
        bg.add(rbCash);
        rightCard.add(cardCash);

        // Radio click sync
        rbQris.addActionListener(e -> updateSelectionUI());
        rbCash.addActionListener(e -> updateSelectionUI());
        updateSelectionUI();

        rightCard.add(Box.createVerticalGlue());

        // Buttons: Kembali & Lanjut
        JPanel actionRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actionRow.setOpaque(false);

        JButton btnKembali = CinemaTheme.createSecondaryButton("← Kembali Pilih Kursi");
        btnKembali.addActionListener(e -> {
            dispose();
            new BeliTiket(idPengguna, jadwal).setVisible(true);
        });

        JButton btnLanjut = CinemaTheme.createPrimaryButton("Lanjut Pembayaran →");
        btnLanjut.setFont(CinemaTheme.FONT_SUBTITLE);
        btnLanjut.addActionListener(e -> prosesLanjutPembayaran());

        actionRow.add(btnKembali);
        actionRow.add(btnLanjut);
        rightCard.add(actionRow);

        content.add(rightCard);
        root.add(content, BorderLayout.CENTER);

        getContentPane().add(root);
    }

    private JPanel createPaymentMethodOption(String title, String desc, boolean selected) {
        JPanel card = new JPanel(new BorderLayout(12, 0));
        card.setBackground(CinemaTheme.PANEL_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JRadioButton rb = new JRadioButton();
        rb.setOpaque(false);
        rb.setSelected(selected);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblTitle.setForeground(CinemaTheme.CREME);

        JLabel lblDesc = new JLabel("<html><body style='width: 320px; color: #A89DA2; font-size: 9pt;'>" + desc + "</body></html>");

        textPanel.add(lblTitle);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(lblDesc);

        card.add(rb, BorderLayout.WEST);
        card.add(textPanel, BorderLayout.CENTER);

        card.putClientProperty("radio", rb);

        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                rb.setSelected(true);
                updateSelectionUI();
            }
        });

        return card;
    }

    private void updateSelectionUI() {
        if (rbQris.isSelected()) {
            cardQris.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 2),
                    BorderFactory.createEmptyBorder(13, 15, 13, 15)
            ));
            cardQris.setBackground(new Color(0x28, 0x1E, 0x24));

            cardCash.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
            cardCash.setBackground(CinemaTheme.PANEL_BG);
        } else {
            cardCash.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 2),
                    BorderFactory.createEmptyBorder(13, 15, 13, 15)
            ));
            cardCash.setBackground(new Color(0x28, 0x1E, 0x24));

            cardQris.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1),
                    BorderFactory.createEmptyBorder(14, 16, 14, 16)
            ));
            cardQris.setBackground(CinemaTheme.PANEL_BG);
        }
    }

    private void addDetailRow(JPanel panel, String label, String value) {
        JLabel lblK = new JLabel(label);
        lblK.setFont(CinemaTheme.FONT_BODY);
        lblK.setForeground(CinemaTheme.TEXT_MUTED);

        JLabel lblV = new JLabel(value);
        lblV.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblV.setForeground(CinemaTheme.CREME);

        panel.add(lblK);
        panel.add(lblV);
    }

    private void prosesLanjutPembayaran() {
        if (idPengguna <= 0 || jadwal == null || kursi.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Data transaksi belum lengkap. Silakan pilih jadwal dan kursi lagi.",
                    "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setVisible(false);
        if (rbQris.isSelected()) {
            new PaymentQRISFrame(idPengguna, jadwal, kursi, this).setVisible(true);
        } else {
            new PaymentCashFrame(idPengguna, jadwal, kursi, this).setVisible(true);
        }
    }
}
