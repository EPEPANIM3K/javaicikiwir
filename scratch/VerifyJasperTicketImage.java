package scratch;

import Koneksi.Koneksi;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import net.sf.jasperreports.engine.*;

public class VerifyJasperTicketImage {
    public static void main(String[] args) {
        try {
            System.out.println("Memulai render tiket bioskop ke PNG...");

            JasperReport report;
            try (InputStream is = new FileInputStream("src/reports/tiket_bioskop.jasper")) {
                report = (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(is);
            }

            Map<String, Object> params = new HashMap<>();
            params.put("id_pemesanan", 1L);

            try (Connection conn = Koneksi.getConnection()) {
                JasperPrint print = JasperFillManager.fillReport(report, params, conn);
                System.out.println("Laporan berhasil di-fill! Jumlah tiket: " + print.getPages().size());

                for (int i = 0; i < print.getPages().size(); i++) {
                    BufferedImage image = (BufferedImage) JasperPrintManager.printPageToImage(print, i, 2.0f);
                    File outFile = new File("scratch/tiket_stub_" + (i + 1) + ".png");
                    ImageIO.write(image, "png", outFile);
                    System.out.println("Tiket " + (i + 1) + " disimpan ke: " + outFile.getAbsolutePath());
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}

