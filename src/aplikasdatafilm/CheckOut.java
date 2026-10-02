/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasdatafilm;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

/**
 * Halaman checkout pembelian tiket film.
 *
 * @author ASUS
 */
public class CheckOut extends javax.swing.JFrame {

    // ── Data ────────────────────────────────────────────────────────────────
    private long idPengguna;
    private PemesananService.Jadwal jadwal;
    private List<PemesananService.Kursi> kursi;

    private BigDecimal subtotal = BigDecimal.ZERO;
    // Biaya layanan dihapus sesuai desain baru (total = subtotal saja)

    private final DateTimeFormatter fmtTgl =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("id", "ID"));
    private final DateTimeFormatter fmtJam =
            DateTimeFormatter.ofPattern("HH:mm");

    // ── UI Components ────────────────────────────────────────────────────────
    // Panel kiri (orange)
    private JPanel panelKiri;
    private JLabel lblPoster;
    private JLabel lblJudul;
    private JLabel lblTanggal;
    private JLabel lblJam;
    private JLabel lblDurasi;
    private JLabel lblKursi;
    private JLabel lblStudio;

    // Panel kanan
    private JPanel panelKanan;
    private JPanel panelDetailPembelian;
    private JLabel lblDetailPembelianJudul;
    private JLabel lblHargaTiketKey;
    private JLabel lblHargaTiketVal;
    private JLabel lblSubtotalKey;
    private JLabel lblSubtotalVal;
    private JLabel lblTotalKey;
    private JLabel lblTotalVal;

    // Metode pembayaran
    private JLabel lblMetodePembayaran;
    private JRadioButton rbCash;
    private JRadioButton rbQris;
    private JRadioButton rbTransfer;
    private ButtonGroup bgMetode;

    // Tombol
    private JButton btnBayar;

    // ── Konstruktor ──────────────────────────────────────────────────────────
    public CheckOut() {
        this(0, null, List.of());
    }

    public CheckOut(long idPengguna, PemesananService.Jadwal jadwal,
            List<PemesananService.Kursi> kursi) {
        this.idPengguna = idPengguna;
        this.jadwal = jadwal;
        this.kursi = kursi == null ? List.of() : List.copyOf(kursi);

        initComponents();
        setTitle("TIKET KU – Checkout");
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(760, 620));
        pack();
        setSize(760, 620);
        setLocationRelativeTo(null);

        isiData();
        UserMenuBar.buat(this, idPengguna);
    }

    // ── Format Rupiah ────────────────────────────────────────────────────────
    private String formatRp(BigDecimal nilai) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("id", "ID"));
        sym.setGroupingSeparator('.');
        DecimalFormat df = new DecimalFormat("#,###", sym);
        return "Rp" + df.format(nilai);
    }

    // ── Isi data dari jadwal & kursi ─────────────────────────────────────────
    private void isiData() {
        if (jadwal == null || kursi.isEmpty()) return;

        subtotal = jadwal.hargaTiket().multiply(BigDecimal.valueOf(kursi.size()));

        // Detail Film
        lblJudul.setText("\uD83C\uDFAC " + jadwal.judul());
        lblTanggal.setText("\uD83D\uDCC5 " + fmtTgl.format(jadwal.mulaiTayang()));

        int jam  = jadwal.durasiMenit() / 60;
        int mnt  = jadwal.durasiMenit() % 60;
        String durStr = jam > 0
                ? (jam + " Jam" + (mnt > 0 ? " " + mnt + " Menit" : ""))
                : (mnt + " Menit");

        lblJam.setText("\uD83D\uDD50 " + fmtJam.format(jadwal.mulaiTayang())
                + " / " + durStr);
        lblDurasi.setText("\u23F3 Durasi : " + durStr);
        lblStudio.setText("\uD83C\uDFA5 " + jadwal.namaStudio());

        String kodeKursi = String.join(", ", kursi.stream()
                .map(PemesananService.Kursi::kodeKursi).sorted().toList());
        lblKursi.setText("\uD83D\uDCBA Kursi: " + kodeKursi);

        // Detail Pembelian
        lblHargaTiketVal.setText(kursi.size() + " \u00d7 " + formatRp(jadwal.hargaTiket()));
        lblSubtotalVal.setText(formatRp(subtotal));
        lblTotalVal.setText(formatRp(subtotal));

        // Muat poster
        if (jadwal.urlPoster() != null && !jadwal.urlPoster().isBlank()) {
            PosterFetcher.muat(lblPoster, jadwal.urlPoster(), 180, 240);
        }
    }

    // ── Simpan Pemesanan ─────────────────────────────────────────────────────
    private void simpanPemesanan() {
        if (idPengguna <= 0 || jadwal == null || kursi.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Data pemesanan tidak lengkap. Silakan pilih film dan kursi lagi.");
            return;
        }
        String metode;
        if (rbCash.isSelected())         metode = "CASH";
        else if (rbQris.isSelected())     metode = "QRIS";
        else if (rbTransfer.isSelected()) metode = "TRANSFER_BANK";
        else {
            JOptionPane.showMessageDialog(this,
                    "Pilih metode pembayaran terlebih dahulu.");
            return;
        }

        btnBayar.setEnabled(false);
        try {
            long idPemesanan = PemesananService.buatPemesanan(
                    idPengguna,
                    jadwal.idJadwal(),
                    kursi.stream().map(PemesananService.Kursi::idKursi).toList(),
                    metode);
            dispose();
            new StrukFilm(idPemesanan, idPengguna).setVisible(true);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CheckOut.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Pemesanan gagal", ex);
            JOptionPane.showMessageDialog(this,
                    "Pemesanan gagal disimpan.\n" + ex.getMessage(),
                    "Kesalahan Pemesanan", JOptionPane.ERROR_MESSAGE);
            btnBayar.setEnabled(true);
        }
    }

    // ── Build UI (menggantikan initComponents yang digenerate NetBeans) ───────
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        // ── Warna ───────────────────────────────────────────────────────────
        Color ORANGE     = new Color(0xF5A623);
        Color DARK_PANEL = new Color(0x2D343A);  // tidak dipakai di sini
        Color WHITE      = Color.WHITE;
        Color BG         = new Color(0xF0F0F0);

        Font fontBold14 = new Font("Segoe UI", Font.BOLD, 14);
        Font fontBold12 = new Font("Segoe UI", Font.BOLD, 12);
        Font fontPlain12 = new Font("Segoe UI", Font.PLAIN, 12);
        Font fontBold18 = new Font("Segoe UI", Font.BOLD, 18);
        Font fontTitle  = new Font("Segoe UI", Font.BOLD, 22);

        getContentPane().setBackground(BG);
        getContentPane().setLayout(new BorderLayout());

        // ── Header ─────────────────────────────────────────────────────────
        JPanel panelHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 14));
        panelHeader.setBackground(BG);
        JLabel lblTitle = new JLabel("TIKET KU");
        lblTitle.setFont(fontTitle);
        panelHeader.add(lblTitle);
        getContentPane().add(panelHeader, BorderLayout.NORTH);

        // ── Tengah: kiri + kanan ───────────────────────────────────────────
        JPanel panelTengah = new JPanel(new BorderLayout(20, 0));
        panelTengah.setBackground(BG);
        panelTengah.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));
        getContentPane().add(panelTengah, BorderLayout.CENTER);

        // ── PANEL KIRI (ORANGE) ────────────────────────────────────────────
        panelKiri = new JPanel();
        panelKiri.setBackground(ORANGE);
        panelKiri.setLayout(new BorderLayout());
        panelKiri.setPreferredSize(new Dimension(240, 460));

        // Poster di bagian atas panel orange
        lblPoster = new JLabel("POSTER FILM", SwingConstants.CENTER);
        lblPoster.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPoster.setForeground(WHITE);
        lblPoster.setOpaque(false);
        lblPoster.setPreferredSize(new Dimension(220, 250));
        lblPoster.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Info film di bawah poster
        JPanel panelInfoFilm = new JPanel();
        panelInfoFilm.setOpaque(false);
        panelInfoFilm.setLayout(new BoxLayout(panelInfoFilm, BoxLayout.Y_AXIS));
        panelInfoFilm.setBorder(BorderFactory.createEmptyBorder(10, 14, 14, 10));

        lblJudul   = buatLabelInfo(WHITE, fontBold14, "\uD83C\uDFAC Film");
        lblTanggal = buatLabelInfo(WHITE, fontPlain12, "\uD83D\uDCC5 Tanggal");
        lblJam     = buatLabelInfo(WHITE, fontPlain12, "\uD83D\uDD50 Jam");
        lblDurasi  = buatLabelInfo(WHITE, fontPlain12, "\u23F3 Durasi");
        lblKursi   = buatLabelInfo(WHITE, fontPlain12, "\uD83D\uDCBA Kursi");
        lblStudio  = buatLabelInfo(WHITE, fontPlain12, "\uD83C\uDFA5 Studio");

        for (JLabel lbl : new JLabel[]{lblJudul, lblTanggal, lblJam, lblDurasi, lblKursi, lblStudio}) {
            panelInfoFilm.add(lbl);
            panelInfoFilm.add(Box.createVerticalStrut(6));
        }

        panelKiri.add(lblPoster, BorderLayout.NORTH);
        panelKiri.add(panelInfoFilm, BorderLayout.CENTER);

        panelTengah.add(panelKiri, BorderLayout.WEST);

        // ── PANEL KANAN ────────────────────────────────────────────────────
        panelKanan = new JPanel();
        panelKanan.setBackground(BG);
        panelKanan.setLayout(new BoxLayout(panelKanan, BoxLayout.Y_AXIS));
        panelKanan.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

        // -- Detail Pembelian (kotak merah) --
        panelDetailPembelian = new JPanel();
        panelDetailPembelian.setBackground(new Color(0xDC3545));
        panelDetailPembelian.setLayout(new GridBagLayout());
        panelDetailPembelian.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        panelDetailPembelian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.anchor = GridBagConstraints.WEST;

        lblDetailPembelianJudul = new JLabel("DETAIL PEMBELIAN");
        lblDetailPembelianJudul.setFont(fontBold12);
        lblDetailPembelianJudul.setForeground(WHITE);
        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        panelDetailPembelian.add(lblDetailPembelianJudul, gc);
        gc.gridwidth = 1;

        // Harga tiket
        lblHargaTiketKey = new JLabel("Harga tiket :");
        lblHargaTiketKey.setForeground(WHITE);
        lblHargaTiketKey.setFont(fontPlain12);
        gc.gridx = 0; gc.gridy = 1;
        panelDetailPembelian.add(lblHargaTiketKey, gc);

        lblHargaTiketVal = new JLabel("-");
        lblHargaTiketVal.setForeground(WHITE);
        lblHargaTiketVal.setFont(fontBold12);
        gc.gridx = 1;
        panelDetailPembelian.add(lblHargaTiketVal, gc);

        // Subtotal
        lblSubtotalKey = new JLabel("Subtotal :");
        lblSubtotalKey.setForeground(WHITE);
        lblSubtotalKey.setFont(fontPlain12);
        gc.gridx = 0; gc.gridy = 2;
        panelDetailPembelian.add(lblSubtotalKey, gc);

        lblSubtotalVal = new JLabel("-");
        lblSubtotalVal.setForeground(WHITE);
        lblSubtotalVal.setFont(fontBold12);
        gc.gridx = 1;
        panelDetailPembelian.add(lblSubtotalVal, gc);

        panelKanan.add(Box.createVerticalStrut(10));
        panelKanan.add(panelDetailPembelian);
        panelKanan.add(Box.createVerticalStrut(20));

        // -- Total --
        JPanel panelTotal = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        panelTotal.setBackground(BG);
        panelTotal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        lblTotalKey = new JLabel("TOTAL :  ");
        lblTotalKey.setFont(fontBold14);
        lblTotalVal = new JLabel("-");
        lblTotalVal.setFont(fontBold18);

        panelTotal.add(lblTotalKey);
        panelTotal.add(lblTotalVal);
        panelKanan.add(panelTotal);
        panelKanan.add(Box.createVerticalStrut(20));

        // -- Metode Pembayaran --
        JPanel panelMetode = new JPanel();
        panelMetode.setBackground(BG);
        panelMetode.setLayout(new BoxLayout(panelMetode, BoxLayout.Y_AXIS));
        panelMetode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        lblMetodePembayaran = new JLabel("METODE PEMBAYARAN :");
        lblMetodePembayaran.setFont(fontBold12);
        lblMetodePembayaran.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelMetode.add(lblMetodePembayaran);
        panelMetode.add(Box.createVerticalStrut(8));

        rbCash     = new JRadioButton("Cash");
        rbQris     = new JRadioButton("Qris");
        rbTransfer = new JRadioButton("Transfer Bank");
        for (JRadioButton rb : new JRadioButton[]{rbCash, rbQris, rbTransfer}) {
            rb.setBackground(BG);
            rb.setFont(fontPlain12);
            rb.setAlignmentX(Component.LEFT_ALIGNMENT);
            panelMetode.add(rb);
            panelMetode.add(Box.createVerticalStrut(4));
        }

        bgMetode = new ButtonGroup();
        bgMetode.add(rbCash);
        bgMetode.add(rbQris);
        bgMetode.add(rbTransfer);

        panelKanan.add(panelMetode);
        panelKanan.add(Box.createVerticalStrut(20));

        // -- Tombol BAYAR SEKARANG --
        btnBayar = new JButton("BAYAR SEKARANG");
        btnBayar.setFont(fontBold12);
        btnBayar.setBackground(new Color(0x343A40));
        btnBayar.setForeground(WHITE);
        btnBayar.setFocusPainted(false);
        btnBayar.setOpaque(true);
        btnBayar.setBorderPainted(false);
        btnBayar.setPreferredSize(new Dimension(180, 36));
        btnBayar.setMaximumSize(new Dimension(200, 36));
        btnBayar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBayar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBayar.addActionListener(e -> simpanPemesanan());
        panelKanan.add(btnBayar);

        panelTengah.add(panelKanan, BorderLayout.CENTER);

        // ── Bottom margin ──────────────────────────────────────────────────
        getContentPane().add(Box.createVerticalStrut(20), BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents

    /** Helper buat label info di panel orange. */
    private JLabel buatLabelInfo(Color fg, Font font, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(fg);
        lbl.setFont(font);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CheckOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CheckOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CheckOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CheckOut.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new CheckOut().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // (all variables are declared as fields above — no GEN-BEGIN block needed)
    // End of variables declaration//GEN-END:variables
}
