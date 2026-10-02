package aplikasdatafilm;

import Koneksi.Koneksi;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.*;

/**
 * CetakStruk — cetak nota tiket film gaya struk thermal 80mm.
 *
 * Tidak memerlukan library JasperReports. Seluruh rendering memakai
 * java.awt.Graphics2D sehingga langsung bisa dikirim ke printer apa pun.
 *
 * Penggunaan:
 *   CetakStruk.cetak(idPemesanan);   // tampil preview + dialog print
 */
public class CetakStruk {

    // ── Lebar "kertas thermal" dalam piksel di layar preview ────────────────
    private static final int KERTAS_LEBAR = 320;   // ≈ 80 mm @ 96dpi
    private static final int KERTAS_PADDING = 14;

    // ── Data struk (satu row per kursi) ────────────────────────────────────
    private record BarisTiket(String kodeKursi, BigDecimal harga) {}

    private record DataStruk(
            long idPemesanan,
            LocalDateTime dibuatPada,
            String namaPengguna,
            String judul,
            LocalDateTime mulaiTayang,
            int durasiMenit,
            String namaStudio,
            String metodePembayaran,
            String statusPembayaran,
            BigDecimal biayaLayanan,
            List<BarisTiket> barisTiket
    ) {
        BigDecimal subtotal() {
            return barisTiket.stream()
                    .map(BarisTiket::harga)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        BigDecimal total() {
            return subtotal().add(biayaLayanan);
        }
    }

    // ── Query data struk dari DB (per-kursi untuk detail tabel) ─────────────
    private static DataStruk ambilData(long idPemesanan) throws SQLException {
        String sql = "SELECT p.id_pemesanan, p.dibuat_pada, u.nama AS nama_pengguna, "
                + "f.judul, j.mulai_tayang, f.durasi_menit, s.nama_studio, "
                + "p.metode_pembayaran, p.status_pembayaran, p.biaya_layanan, "
                + "k.kode_kursi, t.harga_saat_beli "
                + "FROM pemesanan p "
                + "JOIN pengguna u ON u.id_pengguna = p.id_pengguna "
                + "JOIN tiket t ON t.id_pemesanan = p.id_pemesanan "
                + "JOIN jadwal_tayang j ON j.id_jadwal = t.id_jadwal "
                + "JOIN film f ON f.id_film = j.id_film "
                + "JOIN studio s ON s.id_studio = j.id_studio "
                + "JOIN kursi k ON k.id_kursi = t.id_kursi "
                + "WHERE p.id_pemesanan = ? ORDER BY k.kode_kursi";

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, idPemesanan);
            try (ResultSet rs = ps.executeQuery()) {
                long id = 0;
                LocalDateTime dibuatPada = null;
                String namaPengguna = "", judul = "", namaStudio = "",
                        metodePembayaran = "", statusPembayaran = "";
                LocalDateTime mulaiTayang = null;
                int durasiMenit = 0;
                BigDecimal biayaLayanan = BigDecimal.ZERO;
                List<BarisTiket> baris = new ArrayList<>();

                while (rs.next()) {
                    if (id == 0) {
                        id = rs.getLong("id_pemesanan");
                        dibuatPada = rs.getTimestamp("dibuat_pada").toLocalDateTime();
                        namaPengguna = rs.getString("nama_pengguna");
                        judul = rs.getString("judul");
                        mulaiTayang = rs.getTimestamp("mulai_tayang").toLocalDateTime();
                        durasiMenit = rs.getInt("durasi_menit");
                        namaStudio = rs.getString("nama_studio");
                        metodePembayaran = rs.getString("metode_pembayaran");
                        statusPembayaran = rs.getString("status_pembayaran");
                        biayaLayanan = rs.getBigDecimal("biaya_layanan");
                    }
                    baris.add(new BarisTiket(rs.getString("kode_kursi"),
                            rs.getBigDecimal("harga_saat_beli")));
                }
                if (id == 0) throw new SQLException("Data struk tidak ditemukan.");

                return new DataStruk(id, dibuatPada, namaPengguna, judul,
                        mulaiTayang, durasiMenit, namaStudio,
                        metodePembayaran, statusPembayaran, biayaLayanan, baris);
            }
        }
    }

    // ── Format Rupiah ────────────────────────────────────────────────────────
    private static String rp(BigDecimal nilai) {
        DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("id", "ID"));
        sym.setGroupingSeparator('.');
        return "Rp " + new DecimalFormat("#,###", sym).format(nilai);
    }

    // ── Durasi dalam format "X Jam Y Menit" ──────────────────────────────────
    private static String durStr(int menit) {
        int j = menit / 60, m = menit % 60;
        return j > 0 ? (j + " Jam" + (m > 0 ? " " + m + " Mnt" : "")) : (m + " Mnt");
    }

    // ── Renderer utama (Printable) ───────────────────────────────────────────
    private static class StrukRenderer implements Printable {

        private final DataStruk data;
        private final boolean preview;         // true = preview di layar (72dpi); false = cetak nyata

        StrukRenderer(DataStruk data, boolean preview) {
            this.data = data;
            this.preview = preview;
        }

        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            if (pageIndex > 0) return NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

            // Untuk print nyata ikuti imageable area printer; untuk preview pakai koordinat langsung
            if (!preview) {
                g2.translate(pf.getImageableX(), pf.getImageableY());
            }

            render(g2, (int) pf.getImageableWidth());
            return PAGE_EXISTS;
        }

        /** Render seluruh konten struk ke konteks g2 dengan lebar w piksel. */
        int render(Graphics2D g2, int w) {
            DateTimeFormatter fmtTgl = DateTimeFormatter.ofPattern("dd-MM-yyyy", new Locale("id","ID"));
            DateTimeFormatter fmtJam = DateTimeFormatter.ofPattern("HH:mm");

            int pad = 10;
            int x = pad;
            int y = 0;
            int contentW = w - pad * 2;

            Font fBold9  = new Font("SansSerif", Font.BOLD,  9);
            Font fPlain8 = new Font("SansSerif", Font.PLAIN, 8);
            Font fBold8  = new Font("SansSerif", Font.BOLD,  8);
            Font fBold10 = new Font("SansSerif", Font.BOLD, 10);
            Font fItal8  = new Font("SansSerif", Font.ITALIC, 8);
            Font fPlain7 = new Font("SansSerif", Font.PLAIN, 7);

            // ── HEADER ──────────────────────────────────────────────────────
            y += 14;
            drawCenter(g2, fBold10, "BIOSKOP TIKET KU", x, y, contentW);
            y += 12;
            drawCenter(g2, fPlain8, "Sistem Tiket Film - RPL Pride", x, y, contentW);
            y += 10;
            y = dashLine(g2, x, y, contentW);
            y += 2;
            drawCenter(g2, fBold9, "NOTA PEMBELIAN TIKET", x, y, contentW);
            y += 2;
            y = dashLine(g2, x, y, contentW);
            y += 6;

            // ── INFO TRANSAKSI ───────────────────────────────────────────────
            y = row2(g2, fPlain8, fBold8, "No. Transaksi", ": #" + data.idPemesanan(), x, y, contentW);
            y = row2(g2, fPlain8, fPlain8, "Tanggal Beli",
                    ": " + fmtTgl.format(data.dibuatPada()), x, y, contentW);
            y = row2(g2, fPlain8, fPlain8, "Nama", ": " + data.namaPengguna(), x, y, contentW);
            y += 2;
            solidLine(g2, x, y, contentW);
            y += 6;

            // ── DETAIL FILM ──────────────────────────────────────────────────
            y = row2(g2, fPlain8, fBold8, "Film", ": " + data.judul(), x, y, contentW);
            y = row2(g2, fPlain8, fPlain8, "Tanggal Tayang",
                    ": " + fmtTgl.format(data.mulaiTayang()), x, y, contentW);
            y = row2(g2, fPlain8, fPlain8, "Jam",
                    ": " + fmtJam.format(data.mulaiTayang()), x, y, contentW);
            y = row2(g2, fPlain8, fPlain8, "Durasi",
                    ": " + durStr(data.durasiMenit()), x, y, contentW);
            y = row2(g2, fPlain8, fPlain8, "Studio", ": " + data.namaStudio(), x, y, contentW);
            y += 2;
            y = dashLine(g2, x, y, contentW);
            y += 4;

            // ── HEADER KOLOM TABEL ───────────────────────────────────────────
            g2.setFont(fBold8);
            int colKursi = (int)(contentW * 0.30);
            int colHarga = (int)(contentW * 0.45);
            int colQty   = contentW - colKursi - colHarga;

            g2.drawString("Kursi", x, y);
            drawRight(g2, fBold8, "Harga", x + colKursi, y, colHarga);
            drawRight(g2, fBold8, "Qty", x + colKursi + colHarga, y, colQty);
            y += 3;
            y = dashLine(g2, x, y, contentW);
            y += 4;

            // ── BARIS TIKET ──────────────────────────────────────────────────
            for (BarisTiket bt : data.barisTiket()) {
                g2.setFont(fPlain8);
                g2.drawString(bt.kodeKursi(), x, y);
                drawRight(g2, fPlain8, rp(bt.harga()), x + colKursi, y, colHarga);
                drawRight(g2, fPlain8, "1", x + colKursi + colHarga, y, colQty);
                y += 12;
            }
            y -= 2;
            y = dashLine(g2, x, y, contentW);
            y += 6;

            // ── SUBTOTAL & BIAYA LAYANAN ─────────────────────────────────────
            y = row2(g2, fPlain8, fPlain8, "Subtotal", rp(data.subtotal()), x, y, contentW);
            y = row2(g2, fPlain8, fPlain8, "Biaya Layanan", rp(data.biayaLayanan()), x, y, contentW);
            y += 2;
            solidLine(g2, x, y, contentW);
            y += 6;

            // ── TOTAL ────────────────────────────────────────────────────────
            g2.setFont(fBold10);
            g2.drawString("TOTAL BAYAR :", x, y);
            drawRight(g2, fBold10, rp(data.total()), x, y, contentW);
            y += 4;
            solidLine(g2, x, y, contentW);
            y += 10;

            // ── METODE & STATUS ──────────────────────────────────────────────
            y = row2(g2, fPlain8, fPlain8, "Metode", ": " + data.metodePembayaran(), x, y, contentW);
            y = row2(g2, fPlain8, fBold8, "Status", ": " + data.statusPembayaran(), x, y, contentW);
            y += 6;

            // ── FOOTER ───────────────────────────────────────────────────────
            y = dashLine(g2, x, y, contentW);
            y += 8;
            drawCenter(g2, fItal8, "-- Terima Kasih --", x, y, contentW);
            y += 12;
            drawCenter(g2, fPlain7, "Tiket tidak dapat ditukar/dikembalikan", x, y, contentW);
            y += 10;

            return y;  // tinggi total konten
        }

        // ── Helper drawing ───────────────────────────────────────────────────
        private void drawCenter(Graphics2D g2, Font f, String text, int x, int y, int w) {
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + (w - fm.stringWidth(text)) / 2;
            g2.drawString(text, tx, y + fm.getAscent());
        }

        private void drawRight(Graphics2D g2, Font f, String text, int x, int y, int w) {
            g2.setFont(f);
            FontMetrics fm = g2.getFontMetrics();
            int tx = x + w - fm.stringWidth(text);
            g2.drawString(text, tx, y + fm.getAscent());
        }

        private int row2(Graphics2D g2, Font fKey, Font fVal,
                         String key, String val, int x, int y, int w) {
            g2.setFont(fKey);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(key, x, y + fm.getAscent());
            int keyW = (int)(w * 0.42);
            g2.setFont(fVal);
            g2.drawString(val, x + keyW, y + fm.getAscent());
            return y + 12;
        }

        private int dashLine(Graphics2D g2, int x, int y, int w) {
            Stroke old = g2.getStroke();
            g2.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_MITER, 1f, new float[]{4, 3}, 0f));
            g2.drawLine(x, y + 4, x + w, y + 4);
            g2.setStroke(old);
            return y + 10;
        }

        private void solidLine(Graphics2D g2, int x, int y, int w) {
            g2.setStroke(new BasicStroke(0.7f));
            g2.drawLine(x, y, x + w, y);
            g2.setStroke(new BasicStroke(1f));
        }
    }

    // ── Jendela Preview ──────────────────────────────────────────────────────
    private static class PreviewFrame extends JFrame {

        private final DataStruk data;
        private final int contentH;

        PreviewFrame(DataStruk data) {
            super("Preview Struk - #" + data.idPemesanan());
            this.data = data;

            // Hitung tinggi konten dulu dengan Graphics dummy
            BufferedImage dummy = new java.awt.image.BufferedImage(KERTAS_LEBAR, 1000,
                    java.awt.image.BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = dummy.createGraphics();
            StrukRenderer r = new StrukRenderer(data, true);
            contentH = r.render(g2d, KERTAS_LEBAR - KERTAS_PADDING * 2) + 20;
            g2d.dispose();

            setDefaultCloseOperation(DISPOSE_ON_CLOSE);
            JPanel panel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(Color.WHITE);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(Color.BLACK);
                    new StrukRenderer(data, true).render(g2, KERTAS_LEBAR - KERTAS_PADDING * 2);
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(KERTAS_LEBAR, contentH);
                }
            };
            panel.setBackground(Color.WHITE);
            panel.setBorder(BorderFactory.createLineBorder(new Color(0xBBBBBB)));

            JScrollPane scroll = new JScrollPane(panel);
            scroll.setBackground(new Color(0xE0E0E0));
            scroll.setBorder(null);

            JButton btnPrint = new JButton("🖨 Print");
            btnPrint.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPrint.addActionListener(e -> cetakKePrinter(data));

            JButton btnTutup = new JButton("Tutup");
            btnTutup.addActionListener(e -> dispose());

            JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 8));
            btnPanel.add(btnPrint);
            btnPanel.add(btnTutup);

            getContentPane().setLayout(new BorderLayout(0, 8));
            getContentPane().setBackground(new Color(0xE0E0E0));
            getContentPane().add(scroll, BorderLayout.CENTER);
            getContentPane().add(btnPanel, BorderLayout.SOUTH);

            setSize(KERTAS_LEBAR + 40, Math.min(contentH + 80, 700));
            setLocationRelativeTo(null);
        }
    }

    // ── Cetak ke printer fisik ───────────────────────────────────────────────
    private static void cetakKePrinter(DataStruk data) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Struk Tiket #" + data.idPemesanan());

        // Setup page format untuk kertas thermal 80mm
        PageFormat pf = job.defaultPage();
        Paper paper = new Paper();
        double mmToPt = 72.0 / 25.4;
        double lebarMm = 80, tinggiMm = 200;
        paper.setSize(lebarMm * mmToPt, tinggiMm * mmToPt);
        paper.setImageableArea(3 * mmToPt, 5 * mmToPt, (lebarMm - 6) * mmToPt, (tinggiMm - 10) * mmToPt);
        pf.setPaper(paper);
        pf.setOrientation(PageFormat.PORTRAIT);

        job.setPrintable(new StrukRenderer(data, false), pf);

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException ex) {
                JOptionPane.showMessageDialog(null,
                        "Gagal mencetak: " + ex.getMessage(), "Error Cetak",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ── Entry point utama ────────────────────────────────────────────────────
    /**
     * Tampilkan preview struk untuk id pemesanan tertentu.
     * Harus dipanggil dari Event Dispatch Thread (EDT).
     *
     * @param idPemesanan ID pemesanan yang akan dicetak struk-nya
     */
    public static void cetak(long idPemesanan) {
        try {
            DataStruk data = ambilData(idPemesanan);
            new PreviewFrame(data).setVisible(true);
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(CetakStruk.class.getName())
                    .log(java.util.logging.Level.SEVERE, "Gagal memuat data struk", ex);
            JOptionPane.showMessageDialog(null,
                    "Gagal memuat data struk: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
