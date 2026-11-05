package aplikasdatafilm;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern QRIS Payment Screen for Cinema Staff & Cashiers (1280x720).
 * Displays dummy QRIS image from user upload, order summary, total amount,
 * and "Cek Pembayaran" simulation button to confirm transaction and generate tickets.
 */
public class PaymentQRISFrame extends JFrame {

    private final long idPengguna;
    private final PemesananService.Jadwal jadwal;
    private final List<PemesananService.Kursi> kursi;
    private final BigDecimal totalBayar;
    private final JFrame parentFrame;

    public PaymentQRISFrame(long idPengguna, PemesananService.Jadwal jadwal,
                            List<PemesananService.Kursi> kursi, JFrame parentFrame) {
        this.idPengguna = idPengguna;
        this.jadwal = jadwal;
        this.kursi = kursi;
        this.parentFrame = parentFrame;
        this.totalBayar = jadwal.hargaTiket().multiply(BigDecimal.valueOf(kursi.size()));

        CinemaTheme.standardizeFrame(this, "TIKET KU — Pembayaran QRIS");
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Pembayaran QRIS", "KASIR BIOSKOP", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Content (2-Column Card Layout)
        JPanel content = new JPanel(new GridLayout(1, 2, 24, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 36, 24, 36));

        // ── LEFT COLUMN: QRIS DISPLAY ──────────────────────────────────────
        JPanel qrisCard = CinemaTheme.createCard();
        qrisCard.setLayout(new BoxLayout(qrisCard, BoxLayout.Y_AXIS));

        JLabel qrisTitle = new JLabel("PEMBAYARAN QRIS DINAMIS", SwingConstants.CENTER);
        qrisTitle.setFont(CinemaTheme.FONT_SUBTITLE);
        qrisTitle.setForeground(CinemaTheme.CREME);
        qrisTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrisCard.add(qrisTitle);

        JLabel qrisSub = new JLabel("Scan kode QR menggunakan aplikasi e-Wallet atau Mobile Banking", SwingConstants.CENTER);
        qrisSub.setFont(CinemaTheme.FONT_SMALL);
        qrisSub.setForeground(CinemaTheme.TEXT_MUTED);
        qrisSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrisCard.add(qrisSub);

        qrisCard.add(Box.createVerticalStrut(16));

        // White container card for QR code to ensure high scan contrast
        JPanel qrWhiteBox = new JPanel(new BorderLayout());
        qrWhiteBox.setBackground(Color.WHITE);
        qrWhiteBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 2),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));
        qrWhiteBox.setMaximumSize(new Dimension(280, 280));
        qrWhiteBox.setPreferredSize(new Dimension(280, 280));
        qrWhiteBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel qrImageLabel = new JLabel();
        qrImageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        muatGambarQris(qrImageLabel, 250, 250);
        qrWhiteBox.add(qrImageLabel, BorderLayout.CENTER);
        qrisCard.add(qrWhiteBox);

        qrisCard.add(Box.createVerticalStrut(14));

        JLabel nmidLabel = new JLabel("NMID: ID102026889901 | TIKET KU PREMIERE", SwingConstants.CENTER);
        nmidLabel.setFont(CinemaTheme.FONT_SMALL);
        nmidLabel.setForeground(CinemaTheme.TEXT_MUTED);
        nmidLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrisCard.add(nmidLabel);

        qrisCard.add(Box.createVerticalGlue());

        // Simulation Instruction
        JLabel simHint = new JLabel("ℹ Simulasi: Tekan tombol 'Cek Pembayaran' untuk konfirmasi", SwingConstants.CENTER);
        simHint.setFont(CinemaTheme.FONT_SMALL);
        simHint.setForeground(CinemaTheme.ACCENT_GOLD);
        simHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        qrisCard.add(simHint);

        content.add(qrisCard);

        // ── RIGHT COLUMN: SUMMARY & ACTION ─────────────────────────────────
        JPanel summaryCard = CinemaTheme.createCard();
        summaryCard.setLayout(new BoxLayout(summaryCard, BoxLayout.Y_AXIS));

        JLabel sumTitle = new JLabel("RINGKASAN PEMBELIAN TIKET");
        sumTitle.setFont(CinemaTheme.FONT_SUBTITLE);
        sumTitle.setForeground(CinemaTheme.CREME);
        summaryCard.add(sumTitle);

        summaryCard.add(Box.createVerticalStrut(16));

        // Details Grid
        JPanel grid = new JPanel(new GridLayout(6, 2, 8, 12));
        grid.setOpaque(false);

        addDetailRow(grid, "Judul Film", jadwal.judul());
        addDetailRow(grid, "Studio", jadwal.namaStudio());
        addDetailRow(grid, "Jadwal Tayang", CinemaTheme.formatTanggalWaktu(jadwal.mulaiTayang()) + " WIB");
        addDetailRow(grid, "Jumlah Tiket", kursi.size() + " Kursi");

        String kodeKursiStr = String.join(", ", kursi.stream().map(PemesananService.Kursi::kodeKursi).toList());
        addDetailRow(grid, "Nomor Kursi", kodeKursiStr);
        addDetailRow(grid, "Harga Satuan", CinemaTheme.formatRupiah(jadwal.hargaTiket()));

        summaryCard.add(grid);

        summaryCard.add(Box.createVerticalStrut(18));

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(CinemaTheme.CARD_BORDER);
        summaryCard.add(sep);

        summaryCard.add(Box.createVerticalStrut(14));

        // Total Price Box
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(CinemaTheme.PANEL_BG);
        totalBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 1),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        totalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lblTotalTag = new JLabel("TOTAL TAGIHAN");
        lblTotalTag.setFont(CinemaTheme.FONT_SECTION);
        lblTotalTag.setForeground(CinemaTheme.TEXT_MUTED);

        JLabel lblTotalVal = new JLabel(CinemaTheme.formatRupiah(totalBayar));
        lblTotalVal.setFont(CinemaTheme.FONT_PRICE_HUGE);
        lblTotalVal.setForeground(CinemaTheme.ACCENT_GOLD);

        totalBox.add(lblTotalTag, BorderLayout.NORTH);
        totalBox.add(lblTotalVal, BorderLayout.CENTER);
        summaryCard.add(totalBox);

        summaryCard.add(Box.createVerticalGlue());

        // Action Buttons: Cek Pembayaran & Batal
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnKembali = CinemaTheme.createSecondaryButton("← Batal / Ganti Metode");
        btnKembali.addActionListener(e -> {
            dispose();
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            }
        });

        JButton btnCekBayar = CinemaTheme.createPrimaryButton("✓ Cek Pembayaran");
        btnCekBayar.setFont(CinemaTheme.FONT_SUBTITLE);
        btnCekBayar.addActionListener(e -> konfirmasiPembayaranQris());

        actions.add(btnKembali);
        actions.add(btnCekBayar);
        summaryCard.add(actions);

        content.add(summaryCard);
        root.add(content, BorderLayout.CENTER);

        getContentPane().add(root);
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

    private void muatGambarQris(JLabel label, int width, int height) {
        try {
            BufferedImage img = null;
            // 1. Try file from resources
            File f = new File("src/resources/images/qris_dummy.png");
            if (f.exists()) {
                img = ImageIO.read(f);
            }
            if (img == null) {
                InputStream is = getClass().getResourceAsStream("/resources/images/qris_dummy.png");
                if (is != null) {
                    img = ImageIO.read(is);
                }
            }
            if (img != null) {
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
                return;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fallback placeholder text if image fails
        label.setText("QRIS DUMMY");
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        label.setForeground(Color.DARK_GRAY);
    }

    private void konfirmasiPembayaranQris() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Simulasi Pembayaran QRIS:\nTotal: " + CinemaTheme.formatRupiah(totalBayar)
                        + "\n\nKonfirmasi bahwa dana telah diterima?",
                "Konfirmasi Pembayaran QRIS",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            List<Integer> idKursiList = kursi.stream().map(PemesananService.Kursi::idKursi).toList();
            long idPemesanan = PemesananService.konfirmasiPembayaranDanBuatTiket(
                    idPengguna, jadwal.idJadwal(), idKursiList, "QRIS");

            JOptionPane.showMessageDialog(this,
                    "Pembayaran QRIS Berhasil Dikonfirmasi!\nNomor Transaksi #" + idPemesanan + "\nTiket berhasil dicetak.",
                    "Pembayaran Sukses",
                    JOptionPane.INFORMATION_MESSAGE);

            dispose();
            if (parentFrame != null) {
                parentFrame.dispose();
            }
            new StrukFilm(idPemesanan, idPengguna).setVisible(true);

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Gagal memproses transaksi: " + ex.getMessage(),
                    "Kesalahan Transaksi",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}
