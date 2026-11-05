package aplikasdatafilm;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Cash Payment Confirmation Screen for Cinema Staff & Cashiers (1280x720).
 * Prominently displays: "Silahkan memberikan uang sesuai nominal kepada kasir",
 * large legible total price, complete purchase details, cashier change calculator,
 * and "Konfirmasi Pembayaran Tunai" button.
 */
public class PaymentCashFrame extends JFrame {

    private final long idPengguna;
    private final PemesananService.Jadwal jadwal;
    private final List<PemesananService.Kursi> kursi;
    private final BigDecimal totalBayar;
    private final JFrame parentFrame;

    private JTextField txtUangDiterima;
    private JLabel lblKembalian;

    public PaymentCashFrame(long idPengguna, PemesananService.Jadwal jadwal,
                            List<PemesananService.Kursi> kursi, JFrame parentFrame) {
        this.idPengguna = idPengguna;
        this.jadwal = jadwal;
        this.kursi = kursi;
        this.parentFrame = parentFrame;
        this.totalBayar = jadwal.hargaTiket().multiply(BigDecimal.valueOf(kursi.size()));

        CinemaTheme.standardizeFrame(this, "TIKET KU — Pembayaran Tunai (Cash)");
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Pembayaran Tunai (Cash)", "KASIR BIOSKOP", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Content (2-Column Card Layout)
        JPanel content = new JPanel(new GridLayout(1, 2, 28, 0));
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(24, 36, 24, 36));

        // ── LEFT COLUMN: CASH INSTRUCTION & CHANGE CALCULATOR ───────────────
        JPanel cashCard = CinemaTheme.createCard();
        cashCard.setLayout(new BoxLayout(cashCard, BoxLayout.Y_AXIS));

        // Cash Icon & Instruction Banner
        JLabel iconCash = new JLabel("💵", SwingConstants.CENTER);
        iconCash.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        iconCash.setAlignmentX(Component.CENTER_ALIGNMENT);
        cashCard.add(iconCash);

        cashCard.add(Box.createVerticalStrut(10));

        // Main Required Notice
        JLabel lblNotice = new JLabel("<html><div style='text-align: center; width: 380px;'>"
                + "<span style='font-size: 15pt; font-weight: bold; color: #FAF4EE;'>Silahkan memberikan uang sesuai nominal kepada kasir</span>"
                + "</div></html>", SwingConstants.CENTER);
        lblNotice.setAlignmentX(Component.CENTER_ALIGNMENT);
        cashCard.add(lblNotice);

        cashCard.add(Box.createVerticalStrut(20));

        // Large Total Box
        JPanel bigPriceBox = new JPanel();
        bigPriceBox.setLayout(new BoxLayout(bigPriceBox, BoxLayout.Y_AXIS));
        bigPriceBox.setBackground(CinemaTheme.PANEL_BG);
        bigPriceBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 2),
                BorderFactory.createEmptyBorder(16, 20, 16, 20)
        ));
        bigPriceBox.setMaximumSize(new Dimension(420, 95));
        bigPriceBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblTag = new JLabel("TOTAL YANG HARUS DIBAYAR", SwingConstants.CENTER);
        lblTag.setFont(CinemaTheme.FONT_SECTION);
        lblTag.setForeground(CinemaTheme.TEXT_MUTED);
        lblTag.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblBigPrice = new JLabel(CinemaTheme.formatRupiah(totalBayar), SwingConstants.CENTER);
        lblBigPrice.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblBigPrice.setForeground(CinemaTheme.ACCENT_GOLD);
        lblBigPrice.setAlignmentX(Component.CENTER_ALIGNMENT);

        bigPriceBox.add(lblTag);
        bigPriceBox.add(Box.createVerticalStrut(4));
        bigPriceBox.add(lblBigPrice);
        cashCard.add(bigPriceBox);

        cashCard.add(Box.createVerticalStrut(20));

        // Cashier Input: Uang Diterima & Kembalian
        JPanel calcBox = new JPanel(new GridLayout(2, 2, 10, 12));
        calcBox.setOpaque(false);
        calcBox.setMaximumSize(new Dimension(420, 90));
        calcBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblInput = new JLabel("Uang Diterima (Rp):");
        lblInput.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblInput.setForeground(CinemaTheme.CREME);

        txtUangDiterima = new JTextField();
        CinemaTheme.styleTextField(txtUangDiterima);
        txtUangDiterima.setFont(new Font("Segoe UI", Font.BOLD, 15));
        txtUangDiterima.setText(totalBayar.toPlainString());

        JLabel lblKembaliTag = new JLabel("Kembalian:");
        lblKembaliTag.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblKembaliTag.setForeground(CinemaTheme.CREME);

        lblKembalian = new JLabel("Rp 0");
        lblKembalian.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblKembalian.setForeground(new Color(0x81, 0xC7, 0x84)); // Soft green

        txtUangDiterima.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                hitungKembalian();
            }
        });

        calcBox.add(lblInput);
        calcBox.add(txtUangDiterima);
        calcBox.add(lblKembaliTag);
        calcBox.add(lblKembalian);
        cashCard.add(calcBox);

        cashCard.add(Box.createVerticalGlue());
        content.add(cashCard);

        // ── RIGHT COLUMN: DETAIL PEMBELIAN & CONFIRM BUTTON ────────────────
        JPanel detailCard = CinemaTheme.createCard();
        detailCard.setLayout(new BoxLayout(detailCard, BoxLayout.Y_AXIS));

        JLabel detTitle = new JLabel("DETAIL PEMBELIAN TIKET");
        detTitle.setFont(CinemaTheme.FONT_SUBTITLE);
        detTitle.setForeground(CinemaTheme.CREME);
        detailCard.add(detTitle);

        detailCard.add(Box.createVerticalStrut(16));

        // Details Grid
        JPanel grid = new JPanel(new GridLayout(6, 2, 8, 14));
        grid.setOpaque(false);

        addDetailRow(grid, "Judul Film", jadwal.judul());
        addDetailRow(grid, "Studio", jadwal.namaStudio());
        addDetailRow(grid, "Jadwal Tayang", CinemaTheme.formatTanggalWaktu(jadwal.mulaiTayang()) + " WIB");
        addDetailRow(grid, "Jumlah Tiket", kursi.size() + " Tiket");

        String kodeKursiStr = String.join(", ", kursi.stream().map(PemesananService.Kursi::kodeKursi).toList());
        addDetailRow(grid, "Nomor Kursi", kodeKursiStr);
        addDetailRow(grid, "Harga Satuan", CinemaTheme.formatRupiah(jadwal.hargaTiket()));

        detailCard.add(grid);

        detailCard.add(Box.createVerticalStrut(18));

        JSeparator sep = new JSeparator();
        sep.setForeground(CinemaTheme.CARD_BORDER);
        detailCard.add(sep);

        detailCard.add(Box.createVerticalStrut(14));

        // Total row in detail card
        JPanel sumRow = new JPanel(new BorderLayout());
        sumRow.setOpaque(false);
        JLabel lblTotalRowKey = new JLabel("TOTAL PEMBAYARAN");
        lblTotalRowKey.setFont(CinemaTheme.FONT_SECTION);
        lblTotalRowKey.setForeground(CinemaTheme.TEXT_MUTED);

        JLabel lblTotalRowVal = new JLabel(CinemaTheme.formatRupiah(totalBayar));
        lblTotalRowVal.setFont(CinemaTheme.FONT_TITLE_LARGE);
        lblTotalRowVal.setForeground(CinemaTheme.ACCENT_GOLD);

        sumRow.add(lblTotalRowKey, BorderLayout.WEST);
        sumRow.add(lblTotalRowVal, BorderLayout.EAST);
        detailCard.add(sumRow);

        detailCard.add(Box.createVerticalGlue());

        // Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        actions.setOpaque(false);

        JButton btnKembali = CinemaTheme.createSecondaryButton("← Batal / Ganti Metode");
        btnKembali.addActionListener(e -> {
            dispose();
            if (parentFrame != null) {
                parentFrame.setVisible(true);
            }
        });

        JButton btnKonfirmasi = CinemaTheme.createPrimaryButton("✓ Konfirmasi Pembayaran Tunai");
        btnKonfirmasi.setFont(CinemaTheme.FONT_SUBTITLE);
        btnKonfirmasi.addActionListener(e -> konfirmasiPembayaranCash());

        actions.add(btnKembali);
        actions.add(btnKonfirmasi);
        detailCard.add(actions);

        content.add(detailCard);
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

    private void hitungKembalian() {
        try {
            String text = txtUangDiterima.getText().replaceAll("[^0-9]", "");
            if (text.isEmpty()) {
                lblKembalian.setText("Rp 0");
                return;
            }
            BigDecimal bayar = new BigDecimal(text);
            BigDecimal kembalian = bayar.subtract(totalBayar);
            if (kembalian.compareTo(BigDecimal.ZERO) < 0) {
                lblKembalian.setText("Kurang: " + CinemaTheme.formatRupiah(kembalian.abs()));
                lblKembalian.setForeground(new Color(0xE5, 0x73, 0x73));
            } else {
                lblKembalian.setText(CinemaTheme.formatRupiah(kembalian));
                lblKembalian.setForeground(new Color(0x81, 0xC7, 0x84));
            }
        } catch (Exception ex) {
            lblKembalian.setText("Rp 0");
        }
    }

    private void konfirmasiPembayaranCash() {
        String inputUang = txtUangDiterima.getText().replaceAll("[^0-9]", "");
        BigDecimal uangDiterima = inputUang.isEmpty() ? totalBayar : new BigDecimal(inputUang);

        if (uangDiterima.compareTo(totalBayar) < 0) {
            JOptionPane.showMessageDialog(this,
                    "Uang yang diberikan kurang dari total pembayaran!\nTotal: "
                            + CinemaTheme.formatRupiah(totalBayar) + "\nDiberikan: "
                            + CinemaTheme.formatRupiah(uangDiterima),
                    "Uang Tidak Cukup",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal kembalian = uangDiterima.subtract(totalBayar);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Konfirmasi Pembayaran Tunai:\nTotal: " + CinemaTheme.formatRupiah(totalBayar)
                        + "\nUang Diterima: " + CinemaTheme.formatRupiah(uangDiterima)
                        + "\nKembalian: " + CinemaTheme.formatRupiah(kembalian)
                        + "\n\nLanjutkan pencetakan tiket dan struk?",
                "Konfirmasi Pembayaran Kasir",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            List<Integer> idKursiList = kursi.stream().map(PemesananService.Kursi::idKursi).toList();
            long idPemesanan = PemesananService.konfirmasiPembayaranDanBuatTiket(
                    idPengguna, jadwal.idJadwal(), idKursiList, "CASH");

            JOptionPane.showMessageDialog(this,
                    "Pembayaran Tunai Berhasil!\nNomor Transaksi #" + idPemesanan + "\nTiket berhasil dicetak.",
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
