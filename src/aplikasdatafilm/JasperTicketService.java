package aplikasdatafilm;

import Koneksi.Koneksi;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.view.JasperViewer;

/**
 * Service to handle compiling, filling, viewing, and printing cinema tickets via JasperReports.
 * Includes both native JasperViewer and custom Cinema Ticket Modal Viewer.
 */
public class JasperTicketService {

    private static final String JRXML_PATH = "src/reports/tiket_bioskop.jrxml";
    private static final String JASPER_PATH = "src/reports/tiket_bioskop.jasper";
    private static final String CLASSPATH_JASPER = "/reports/tiket_bioskop.jasper";
    private static final String CLASSPATH_JRXML = "/reports/tiket_bioskop.jrxml";

    /**
     * Compiles JRXML to JASPER file.
     */
    public static void compileReport() throws Exception {
        File jrxmlFile = new File(JRXML_PATH);
        if (!jrxmlFile.exists()) {
            throw new IllegalStateException("File template " + JRXML_PATH + " tidak ditemukan.");
        }
        JasperCompileManager.compileReportToFile(JRXML_PATH, JASPER_PATH);
        System.out.println("Berhasil meng-compile " + JRXML_PATH + " ke " + JASPER_PATH);
    }

    /**
     * Prints or previews tickets for a given booking ID.
     */
    public static void cetakTiket(long idPemesanan) {
        try {
            JasperReport jasperReport = getJasperReport();
            if (jasperReport == null) {
                JOptionPane.showMessageDialog(null,
                        "Template tiket bioskop (.jasper / .jrxml) tidak ditemukan!",
                        "Error Template Tiket",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            Map<String, Object> params = new HashMap<>();
            params.put("id_pemesanan", idPemesanan);

            try (Connection conn = Koneksi.getConnection()) {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

                if (jasperPrint.getPages().isEmpty()) {
                    JOptionPane.showMessageDialog(null,
                            "Tidak ada tiket untuk nomor transaksi #" + idPemesanan,
                            "Informasi", JOptionPane.INFORMATION_MESSAGE);
                    return;
                }

                // Show custom cinema modal viewer for seamless embedded preview and print
                showTicketViewer(jasperPrint, idPemesanan);
            }
        } catch (NoClassDefFoundError e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Library JasperReports belum lengkap: " + e.getMessage(),
                    "Missing Library", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Gagal mencetak tiket: " + e.getMessage(),
                    "Error Cetak Tiket", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JasperReport getJasperReport() throws Exception {
        // 1. Try precompiled .jasper from classpath
        InputStream jasperStream = JasperTicketService.class.getResourceAsStream(CLASSPATH_JASPER);
        if (jasperStream == null) {
            File f = new File(JASPER_PATH);
            if (f.exists()) {
                jasperStream = new FileInputStream(f);
            }
        }

        if (jasperStream != null) {
            try {
                return (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(jasperStream);
            } finally {
                jasperStream.close();
            }
        }

        // 2. Fallback: compile from .jrxml
        InputStream jrxmlStream = JasperTicketService.class.getResourceAsStream(CLASSPATH_JRXML);
        if (jrxmlStream == null) {
            File f = new File(JRXML_PATH);
            if (f.exists()) {
                jrxmlStream = new FileInputStream(f);
            }
        }

        if (jrxmlStream != null) {
            try {
                return JasperCompileManager.compileReport(jrxmlStream);
            } finally {
                jrxmlStream.close();
            }
        }

        return null;
    }

    /**
     * Beautiful modal dialog to preview and print JasperReports cinema ticket stubs.
     */
    public static void showTicketViewer(JasperPrint print, long idPemesanan) {
        JDialog dialog = new JDialog((Frame) null, "Pratinjau Tiket Bioskop #" + idPemesanan, true);
        dialog.setSize(620, 780);
        dialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 10));
        mainPanel.setBackground(CinemaTheme.BG_DARK);
        mainPanel.setBorder(new EmptyBorder(14, 16, 14, 16));

        // Header
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JLabel lblTitle = new JLabel("🎟 PRATINJAU TIKET MASUK BIOSKOP (JASPERREPORTS)");
        lblTitle.setFont(CinemaTheme.FONT_SUBTITLE);
        lblTitle.setForeground(CinemaTheme.CREME);

        JLabel lblCount = new JLabel("Total Tiket: " + print.getPages().size() + " Lembar");
        lblCount.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblCount.setForeground(CinemaTheme.ACCENT_GOLD);

        topPanel.add(lblTitle, BorderLayout.WEST);
        topPanel.add(lblCount, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Center: Scrollable preview of all ticket stubs rendered from JasperPrint
        JPanel ticketsContainer = new JPanel();
        ticketsContainer.setLayout(new BoxLayout(ticketsContainer, BoxLayout.Y_AXIS));
        ticketsContainer.setBackground(CinemaTheme.BG_DARK);
        ticketsContainer.setBorder(new EmptyBorder(10, 10, 10, 10));

        try {
            for (int i = 0; i < print.getPages().size(); i++) {
                BufferedImage pageImg = (BufferedImage) JasperPrintManager.printPageToImage(print, i, 1.4f);
                JLabel ticketLabel = new JLabel(new ImageIcon(pageImg));
                ticketLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                ticketLabel.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 2),
                        BorderFactory.createEmptyBorder(6, 6, 6, 6)
                ));

                JLabel lblPageNum = new JLabel("— Tiket " + (i + 1) + " dari " + print.getPages().size() + " —", SwingConstants.CENTER);
                lblPageNum.setFont(CinemaTheme.FONT_SMALL);
                lblPageNum.setForeground(CinemaTheme.TEXT_MUTED);
                lblPageNum.setAlignmentX(Component.CENTER_ALIGNMENT);

                ticketsContainer.add(lblPageNum);
                ticketsContainer.add(Box.createVerticalStrut(4));
                ticketsContainer.add(ticketLabel);
                ticketsContainer.add(Box.createVerticalStrut(18));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        JScrollPane scroll = new JScrollPane(ticketsContainer);
        scroll.setBorder(BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1));
        scroll.getViewport().setBackground(CinemaTheme.BG_DARK);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        mainPanel.add(scroll, BorderLayout.CENTER);

        // Footer Actions
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        footer.setOpaque(false);

        JButton btnTutup = CinemaTheme.createSecondaryButton("Tutup");
        btnTutup.addActionListener(e -> dialog.dispose());

        JButton btnJasperViewer = CinemaTheme.createSecondaryButton("Buka JasperViewer");
        btnJasperViewer.addActionListener(e -> {
            JasperViewer viewer = new JasperViewer(print, false);
            viewer.setTitle("JasperViewer — Tiket Bioskop #" + idPemesanan);
            viewer.setVisible(true);
        });

        JButton btnPrint = CinemaTheme.createPrimaryButton("🖨 Cetak ke Printer");
        btnPrint.setFont(CinemaTheme.FONT_BODY_BOLD);
        btnPrint.addActionListener(e -> {
            try {
                boolean printed = JasperPrintManager.printReport(print, true);
                if (printed) {
                    JOptionPane.showMessageDialog(dialog, "Dokumen tiket telah dikirim ke printer.", "Cetak Sukses", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (JRException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Gagal mencetak: " + ex.getMessage(), "Error Cetak", JOptionPane.ERROR_MESSAGE);
            }
        });

        footer.add(btnTutup);
        footer.add(btnJasperViewer);
        footer.add(btnPrint);
        mainPanel.add(footer, BorderLayout.SOUTH);

        dialog.getContentPane().add(mainPanel);
        dialog.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            compileReport();
            cetakTiket(1L);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
