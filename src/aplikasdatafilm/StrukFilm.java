package aplikasdatafilm;

import java.awt.*;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Cinema Receipt & Ticket Printing Screen (1280x720).
 * Displays full transaction breakdown, thermal receipt preview,
 * and one-click actions for Printing Thermal Receipt and Printing Official Cinema Tickets via JasperReports.
 */
public class StrukFilm extends JFrame {

    private final long idPemesanan;
    private final long idPengguna;
    private PemesananService.Struk dataStruk;

    private JLabel lblStatusBadge;
    private JLabel lblNoTransaksi;
    private JLabel lblWaktuTransaksi;
    private JLabel lblJudulFilm;
    private JLabel lblStudio;
    private JLabel lblJadwalTayang;
    private JLabel lblKursi;
    private JLabel lblJumlahTiket;
    private JLabel lblHargaSatuan;
    private JLabel lblMetodeBayar;
    private JLabel lblTotalBayar;

    public StrukFilm() {
        this(0, 0);
    }

    public StrukFilm(long idPemesanan, long idPengguna) {
        this.idPemesanan = idPemesanan;
        this.idPengguna = idPengguna;

        CinemaTheme.standardizeFrame(this, "TIKET KU — Bukti Transaksi & Cetak Tiket");
        buildUI();
        muatDataStruk();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Struk & Cetak Tiket Bioskop", "KASIR OPERASIONAL", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Content (2 Columns)
        JPanel content = new JPanel(new GridLayout(1, 2, 28, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 36, 20, 36));

        // ── LEFT COLUMN: RECEIPT CARD (Thermal Receipt Style Preview) ──────
        JPanel receiptCard = CinemaTheme.createCard();
        receiptCard.setLayout(new BorderLayout(0, 12));

        // Top receipt banner
        JPanel receiptHeader = new JPanel();
        receiptHeader.setLayout(new BoxLayout(receiptHeader, BoxLayout.Y_AXIS));
        receiptHeader.setOpaque(false);

        JLabel brand = new JLabel("TIKET KU PREMIERE CINEMA", SwingConstants.CENTER);
        brand.setFont(CinemaTheme.FONT_TITLE);
        brand.setForeground(CinemaTheme.CREME);
        brand.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel address = new JLabel("Jl. Bioskop Modern No. 21, Jawa Timur", SwingConstants.CENTER);
        address.setFont(CinemaTheme.FONT_SMALL);
        address.setForeground(CinemaTheme.TEXT_MUTED);
        address.setAlignmentX(Component.CENTER_ALIGNMENT);

        receiptHeader.add(brand);
        receiptHeader.add(Box.createVerticalStrut(2));
        receiptHeader.add(address);
        receiptHeader.add(Box.createVerticalStrut(8));

        // Status Badge
        lblStatusBadge = new JLabel(" STATUS: LUNAS / SELESAI ", SwingConstants.CENTER);
        lblStatusBadge.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblStatusBadge.setForeground(CinemaTheme.CREME);
        lblStatusBadge.setBackground(CinemaTheme.SUCCESS);
        lblStatusBadge.setOpaque(true);
        lblStatusBadge.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        lblStatusBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        receiptHeader.add(lblStatusBadge);

        receiptCard.add(receiptHeader, BorderLayout.NORTH);

        // Details Center
        JPanel receiptBody = new JPanel(new GridLayout(9, 2, 8, 8));
        receiptBody.setBackground(CinemaTheme.PANEL_BG);
        receiptBody.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        lblNoTransaksi = addReceiptRow(receiptBody, "No. Transaksi");
        lblWaktuTransaksi = addReceiptRow(receiptBody, "Waktu Transaksi");
        lblJudulFilm = addReceiptRow(receiptBody, "Judul Film");
        lblStudio = addReceiptRow(receiptBody, "Studio");
        lblJadwalTayang = addReceiptRow(receiptBody, "Jadwal Tayang");
        lblKursi = addReceiptRow(receiptBody, "Nomor Kursi");
        lblJumlahTiket = addReceiptRow(receiptBody, "Jumlah Tiket");
        lblHargaSatuan = addReceiptRow(receiptBody, "Harga per Tiket");
        lblMetodeBayar = addReceiptRow(receiptBody, "Metode Pembayaran");

        receiptCard.add(receiptBody, BorderLayout.CENTER);

        // Total Bottom
        JPanel receiptBottom = new JPanel(new BorderLayout());
        receiptBottom.setBackground(CinemaTheme.CARD_BG);
        receiptBottom.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, CinemaTheme.BURGUNDY),
                BorderFactory.createEmptyBorder(10, 8, 6, 8)
        ));

        JLabel lblTotalTag = new JLabel("TOTAL DIBAYAR:");
        lblTotalTag.setFont(CinemaTheme.FONT_SUBTITLE);
        lblTotalTag.setForeground(CinemaTheme.CREME);

        lblTotalBayar = new JLabel("Rp 0");
        lblTotalBayar.setFont(CinemaTheme.FONT_BIG_NUMBER);
        lblTotalBayar.setForeground(CinemaTheme.ACCENT_GOLD);

        receiptBottom.add(lblTotalTag, BorderLayout.WEST);
        receiptBottom.add(lblTotalBayar, BorderLayout.EAST);
        receiptCard.add(receiptBottom, BorderLayout.SOUTH);

        content.add(receiptCard);

        // ── RIGHT COLUMN: PRINT & NAVIGATION ACTIONS ───────────────────────
        JPanel actionCard = CinemaTheme.createCard();
        actionCard.setLayout(new BoxLayout(actionCard, BoxLayout.Y_AXIS));

        JLabel actTitle = new JLabel("OPERASIONAL TIKET & STRUK");
        actTitle.setFont(CinemaTheme.FONT_SUBTITLE);
        actTitle.setForeground(CinemaTheme.CREME);
        actionCard.add(actTitle);

        JLabel actSub = new JLabel("Cetak struk belanja kasir atau tiket masuk studio bioskop.");
        actSub.setFont(CinemaTheme.FONT_SMALL);
        actSub.setForeground(CinemaTheme.TEXT_MUTED);
        actionCard.add(actSub);

        actionCard.add(Box.createVerticalStrut(20));

        // Success banner alert
        JPanel alert = new JPanel(new BorderLayout(10, 0));
        alert.setBackground(new Color(0x1B, 0x33, 0x20));
        alert.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.SUCCESS, 1),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        JLabel alertIcon = new JLabel("✓");
        alertIcon.setFont(new Font("Segoe UI", Font.BOLD, 22));
        alertIcon.setForeground(new Color(0x81, 0xC7, 0x84));
        JLabel alertMsg = new JLabel("<html><b>Transaksi Berhasil Dikonfirmasi!</b><br>"
                + "<span style='color: #C5B8AF; font-size: 9pt;'>Pembayaran telah tervalidasi dan nomor tiket telah tercatat di sistem bioskop.</span></html>");
        alertMsg.setForeground(CinemaTheme.CREME);
        alert.add(alertIcon, BorderLayout.WEST);
        alert.add(alertMsg, BorderLayout.CENTER);
        alert.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        actionCard.add(alert);

        actionCard.add(Box.createVerticalStrut(24));

        // Action Button 1: Cetak Tiket JasperReports
        JButton btnCetakTiket = CinemaTheme.createPrimaryButton("🎟  Cetak Tiket Bioskop (JasperReports)");
        btnCetakTiket.setFont(CinemaTheme.FONT_SUBTITLE);
        btnCetakTiket.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnCetakTiket.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCetakTiket.addActionListener(e -> {
            JasperTicketService.cetakTiket(idPemesanan);
        });
        actionCard.add(btnCetakTiket);

        actionCard.add(Box.createVerticalStrut(14));

        // Action Button 2: Cetak Struk Pembelian
        JButton btnCetakStruk = CinemaTheme.createSecondaryButton("🖨  Cetak Struk Transaksi Kasir");
        btnCetakStruk.setFont(CinemaTheme.FONT_SUBTITLE);
        btnCetakStruk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnCetakStruk.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnCetakStruk.addActionListener(e -> {
            CetakStruk.cetak(idPemesanan);
        });
        actionCard.add(btnCetakStruk);

        actionCard.add(Box.createVerticalStrut(20));
        JSeparator sep = new JSeparator();
        sep.setForeground(CinemaTheme.CARD_BORDER);
        actionCard.add(sep);
        actionCard.add(Box.createVerticalStrut(20));

        // Action Button 3: Transaksi Baru
        JButton btnBaru = CinemaTheme.createSuccessButton("+ Transaksi Tiket Baru");
        btnBaru.setFont(CinemaTheme.FONT_BODY_BOLD);
        btnBaru.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnBaru.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBaru.addActionListener(e -> {
            dispose();
            new PilihFilm(idPengguna).setVisible(true);
        });
        actionCard.add(btnBaru);

        actionCard.add(Box.createVerticalStrut(10));

        // Action Button 4: Menu Utama
        JButton btnMenu = CinemaTheme.createSecondaryButton("🏠 Kembali ke Menu Utama");
        btnMenu.setFont(CinemaTheme.FONT_BODY_BOLD);
        btnMenu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnMenu.addActionListener(e -> {
            dispose();
            new Menu().setVisible(true);
        });
        actionCard.add(btnMenu);

        actionCard.add(Box.createVerticalGlue());
        content.add(actionCard);

        root.add(content, BorderLayout.CENTER);
        getContentPane().add(root);
    }

    private JLabel addReceiptRow(JPanel panel, String label) {
        JLabel lblK = new JLabel(label + ":");
        lblK.setFont(CinemaTheme.FONT_SMALL);
        lblK.setForeground(CinemaTheme.TEXT_MUTED);

        JLabel lblV = new JLabel("-");
        lblV.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblV.setForeground(CinemaTheme.CREME);

        panel.add(lblK);
        panel.add(lblV);
        return lblV;
    }

    private void muatDataStruk() {
        if (idPemesanan <= 0) return;

        try {
            dataStruk = PemesananService.ambilStruk(idPemesanan);
            lblNoTransaksi.setText("#" + dataStruk.idPemesanan());
            lblWaktuTransaksi.setText(CinemaTheme.formatTanggalWaktu(dataStruk.dibuatPada()) + " WIB");
            lblJudulFilm.setText(dataStruk.judul());
            lblStudio.setText(dataStruk.namaStudio());
            lblJadwalTayang.setText(CinemaTheme.formatTanggalWaktu(dataStruk.mulaiTayang()) + " WIB");
            lblKursi.setText(dataStruk.kodeKursi());
            lblJumlahTiket.setText(dataStruk.jumlah() + " Tiket");
            lblHargaSatuan.setText(CinemaTheme.formatRupiah(dataStruk.hargaTiket()));
            lblMetodeBayar.setText(dataStruk.metodePembayaran());
            lblTotalBayar.setText(CinemaTheme.formatRupiah(dataStruk.total()));
            lblStatusBadge.setText(" STATUS: " + dataStruk.statusPembayaran() + " ");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Gagal memuat struk: " + ex.getMessage(),
                    "Kesalahan Database",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
