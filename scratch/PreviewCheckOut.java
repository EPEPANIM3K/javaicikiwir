package scratch;

import aplikasdatafilm.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class PreviewCheckOut {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        SwingUtilities.invokeAndWait(() -> {
            try {
                // Buat dummy jadwal
                PemesananService.Jadwal jadwal = new PemesananService.Jadwal(
                    1, 5, "Interstellar", "Sci-Fi", 2014, "Christopher Nolan",
                    169, 1, "Studio 1",
                    LocalDateTime.of(2026, 10, 2, 19, 30),
                    new BigDecimal("50000"),
                    null
                );

                // Buat dummy kursi
                List<PemesananService.Kursi> kursiList = List.of(
                    new PemesananService.Kursi(1, "A1", false),
                    new PemesananService.Kursi(2, "A2", false)
                );

                CheckOut frame = new CheckOut(1, jadwal, kursiList);
                frame.setVisible(true);

                // Screenshot
                Thread.sleep(800);
                Dimension sz = frame.getSize();
                BufferedImage img = new BufferedImage(sz.width, sz.height, BufferedImage.TYPE_INT_RGB);
                frame.paint(img.getGraphics());
                ImageIO.write(img, "png", new File("scratch/checkout_preview.png"));
                System.out.println("Preview saved: scratch/checkout_preview.png");
                frame.dispose();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
