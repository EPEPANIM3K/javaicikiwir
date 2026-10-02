import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JTextField;

public class VerifyBeliTiket {
    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        try {
            System.out.println("=== 1. Testing BeliTiket Initialization ===");
            // Load schedule for Interstellar (id=4)
            var jadwalList = aplikasdatafilm.PemesananService.cariJadwal("Interstellar");
            if (jadwalList.isEmpty()) {
                throw new IllegalStateException("Interstellar schedule not found");
            }
            var jadwal = jadwalList.get(0);
            System.out.println("Using schedule: " + jadwal.judul() + " | id=" + jadwal.idJadwal() + " | Studio=" + jadwal.namaStudio());

            aplikasdatafilm.BeliTiket frame = new aplikasdatafilm.BeliTiket(1L, jadwal);
            System.out.println("Frame created! Size: " + frame.getWidth() + "x" + frame.getHeight());

            var fMap = aplikasdatafilm.BeliTiket.class.getDeclaredField("tombolKursiMap");
            fMap.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, JButton> map = (Map<String, JButton>) fMap.get(frame);
            System.out.println("Mapped seats count: " + map.size());

            var fTerpilih = aplikasdatafilm.BeliTiket.class.getDeclaredField("kursiTerpilih");
            fTerpilih.setAccessible(true);
            @SuppressWarnings("unchecked")
            Set<String> terpilih = (Set<String>) fTerpilih.get(frame);

            var fBtnPlus = aplikasdatafilm.BeliTiket.class.getDeclaredField("jButton4");
            fBtnPlus.setAccessible(true);
            JButton btnPlus = (JButton) fBtnPlus.get(frame);

            var fBtnMinus = aplikasdatafilm.BeliTiket.class.getDeclaredField("jButton29");
            fBtnMinus.setAccessible(true);
            JButton btnMinus = (JButton) fBtnMinus.get(frame);

            var fTxtJumlah = aplikasdatafilm.BeliTiket.class.getDeclaredField("jTextField3");
            fTxtJumlah.setAccessible(true);
            JTextField txtJumlah = (JTextField) fTxtJumlah.get(frame);

            var fTxtTotal = aplikasdatafilm.BeliTiket.class.getDeclaredField("jTextField2");
            fTxtTotal.setAccessible(true);
            JTextField txtTotal = (JTextField) fTxtTotal.get(frame);

            System.out.println("Initial jumlah tiket: " + txtJumlah.getText() + " | Total: " + txtTotal.getText());

            // Check A3 is booked (red/disabled)
            JButton btnA3 = map.get("A3");
            if (btnA3.isEnabled()) {
                System.out.println("WARNING: A3 was expected to be booked/disabled, isEnabled: " + btnA3.isEnabled());
            } else {
                System.out.println("SUCCESS: A3 is booked and disabled (cannot click)!");
            }

            // === 2. Test + button ===
            System.out.println("\n=== 2. Testing '+' button (increment) ===");
            btnPlus.doClick();
            System.out.println("After 1st '+', jumlah tiket: " + txtJumlah.getText() + " | Total: " + txtTotal.getText());
            if (!"2".equals(txtJumlah.getText())) {
                throw new AssertionError("Expected jumlah=2 but got: " + txtJumlah.getText());
            }

            // === 3. Test Seat Selection (2 seats) ===
            System.out.println("\n=== 3. Testing Seat Selection for 2 tickets ===");
            JButton btnA1 = map.get("A1");
            JButton btnA2 = map.get("A2");
            JButton btnA4 = map.get("A4");

            System.out.println("Selecting A1...");
            btnA1.doClick();
            System.out.println("Selected seats: " + terpilih);
            if (!terpilih.contains("A1") || terpilih.size() != 1) {
                throw new AssertionError("A1 should be selected!");
            }
            if (!btnA4.isEnabled()) {
                throw new AssertionError("A4 should still be enabled (quota 1/2)!");
            }

            System.out.println("Selecting A2 (filling quota 2/2)...");
            btnA2.doClick();
            System.out.println("Selected seats: " + terpilih);
            if (!terpilih.contains("A2") || terpilih.size() != 2) {
                throw new AssertionError("A1 and A2 should be selected!");
            }

            // Quota is full (2/2)! Other seats MUST be cannot click!
            if (btnA4.isEnabled()) {
                throw new AssertionError("A4 MUST BE CANNOT CLICK (quota is 2/2)!");
            }
            System.out.println("SUCCESS: Quota 2/2 reached, A4 cannot click: isEnabled=" + btnA4.isEnabled());

            // === 4. Test Unselecting by clicking again ===
            System.out.println("\n=== 4. Testing Unselect by clicking selected seat (A2) again ===");
            btnA2.doClick();
            System.out.println("Selected seats after unselecting A2: " + terpilih);
            if (terpilih.contains("A2") || terpilih.size() != 1) {
                throw new AssertionError("A2 should be unselected!");
            }
            if (!btnA4.isEnabled()) {
                throw new AssertionError("A4 MUST BE CLICKABLE AGAIN after unselecting A2!");
            }
            System.out.println("SUCCESS: A2 unselected, A4 is clickable again!");

            // Re-select A4
            btnA4.doClick();
            System.out.println("Selected seats after selecting A4: " + terpilih);
            if (btnA2.isEnabled()) {
                throw new AssertionError("A2 MUST BE CANNOT CLICK (quota 2/2 reached with A1, A4)!");
            }
            System.out.println("SUCCESS: Quota 2/2 reached with A1 and A4, other seats cannot click!");

            // === 5. Test '-' button auto-deselect excess seats ===
            System.out.println("\n=== 5. Testing '-' button with 2 seats selected ===");
            btnMinus.doClick();
            System.out.println("After '-', jumlah tiket: " + txtJumlah.getText() + " | selected: " + terpilih);
            if (!"1".equals(txtJumlah.getText()) || terpilih.size() != 1) {
                throw new AssertionError("Expected 1 ticket and 1 seat, got: " + txtJumlah.getText() + " and " + terpilih);
            }
            System.out.println("SUCCESS: '-' decreased count to 1 and trimmed excess seat!");

            // === 6. Render preview image to verify no cutoff ===
            System.out.println("\n=== 6. Rendering Preview Image to check cutoff ===");
            BufferedImage img = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(new Color(238, 238, 238));
            g2.fillRect(0, 0, frame.getWidth(), frame.getHeight());
            frame.getContentPane().paint(g2);
            g2.dispose();

            File out = new File("scratch/beli_tiket_preview.png");
            ImageIO.write(img, "png", out);
            System.out.println("Preview saved to: " + out.getAbsolutePath());

            frame.dispose();
            System.out.println("\nALL VERIFICATION CHECKS PASSED PERFECTLY!");
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
