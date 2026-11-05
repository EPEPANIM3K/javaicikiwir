package scratch;

import Koneksi.Koneksi;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import net.sf.jasperreports.engine.*;

public class VerifyJasperTicket {
    public static void main(String[] args) {
        try {
            System.out.println("Memulai uji coba JasperReports untuk tiket bioskop...");

            File jasperFile = new File("src/reports/tiket_bioskop.jasper");
            if (!jasperFile.exists()) {
                System.out.println("Meng-compile JRXML...");
                JasperCompileManager.compileReportToFile("src/reports/tiket_bioskop.jrxml", "src/reports/tiket_bioskop.jasper");
            }

            JasperReport report;
            try (InputStream is = new FileInputStream("src/reports/tiket_bioskop.jasper")) {
                report = (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(is);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("id_pemesanan", 1L);

            try (Connection conn = Koneksi.getConnection()) {
                JasperPrint print = JasperFillManager.fillReport(report, params, conn);
                System.out.println("Laporan berhasil di-fill!");
                System.out.println("Jumlah tiket/halaman: " + print.getPages().size());

                // Export to PDF in scratch folder
                File outPdf = new File("scratch/tiket_bioskop_order1.pdf");
                JasperExportManager.exportReportToPdfFile(print, outPdf.getAbsolutePath());
                System.out.println("Tiket berhasil diekspor ke: " + outPdf.getAbsolutePath());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

