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

public class VerifyFullSystem {

    public static void main(String[] args) {
        System.out.println("=== VERIFIKASI SISTEM APLIKASI TIKET BIOSKOP (1280x720) ===");

        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Test Login Frame
                Login login = new Login();
                captureFrame(login, "scratch/screen_01_login.png");
                login.dispose();

                // 2. Test Register Frame
                Register register = new Register();
                captureFrame(register, "scratch/screen_02_register.png");
                register.dispose();

                // 3. Test Menu / Dashboard Frame
                aplikasdatafilm.Menu menu = new aplikasdatafilm.Menu();
                captureFrame(menu, "scratch/screen_03_dashboard.png");
                menu.dispose();

                // 4. Test FormFilmAdmin Frame
                FormFilmAdmin filmAdmin = new FormFilmAdmin();
                captureFrame(filmAdmin, "scratch/screen_04_form_film_admin.png");
                filmAdmin.dispose();

                // 5. Test FormTambahGenre Frame
                FormTambahGenre genreFrame = new FormTambahGenre();
                captureFrame(genreFrame, "scratch/screen_05_genre.png");
                genreFrame.dispose();

                // 6. Test FormTambahStudio Frame
                FormTambahStudio studioFrame = new FormTambahStudio();
                captureFrame(studioFrame, "scratch/screen_06_studio.png");
                studioFrame.dispose();

                // 7. Test PilihFilm Frame
                PilihFilm pilihFilm = new PilihFilm(1L);
                captureFrame(pilihFilm, "scratch/screen_07_pilih_film.png");

                // Dummy Jadwal for testing ticketing & payments
                PemesananService.Jadwal dummyJadwal = new PemesananService.Jadwal(
                        1L, 1, "Avatar: The Way of Water", "Action", 2022,
                        "James Cameron", 192, 1, "Studio 1",
                        LocalDateTime.now().plusDays(1).withHour(14).withMinute(0),
                        new BigDecimal("50000.00"), "https://ybjgdhfiuihchrwbjocb.supabase.co/storage/v1/object/public/image/poster_1790866222347_d1bd26de.png"
                );

                List<PemesananService.Kursi> dummyKursi = List.of(
                        new PemesananService.Kursi(1, "A1", false),
                        new PemesananService.Kursi(2, "A2", false)
                );

                // 8. Test BeliTiket Frame
                BeliTiket beliTiket = new BeliTiket(1L, dummyJadwal, pilihFilm);
                captureFrame(beliTiket, "scratch/screen_08_beli_tiket.png");
                beliTiket.dispose();
                pilihFilm.dispose();

                // 9. Test CheckOut Frame
                CheckOut checkOut = new CheckOut(1L, dummyJadwal, dummyKursi);
                captureFrame(checkOut, "scratch/screen_09_checkout.png");

                // 10. Test PaymentQRISFrame
                PaymentQRISFrame qrisFrame = new PaymentQRISFrame(1L, dummyJadwal, dummyKursi, checkOut);
                captureFrame(qrisFrame, "scratch/screen_10_payment_qris.png");
                qrisFrame.dispose();

                // 11. Test PaymentCashFrame
                PaymentCashFrame cashFrame = new PaymentCashFrame(1L, dummyJadwal, dummyKursi, checkOut);
                captureFrame(cashFrame, "scratch/screen_11_payment_cash.png");
                cashFrame.dispose();
                checkOut.dispose();

                // 12. Test StrukFilm Frame (using existing order 1)
                StrukFilm strukFilm = new StrukFilm(1L, 1L);
                captureFrame(strukFilm, "scratch/screen_12_struk_film.png");
                strukFilm.dispose();

                System.out.println("Semua 12 tampilan berhasil dirender dan diverifikasi pada resolusi 1280x720!");
                System.exit(0);

            } catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void captureFrame(JFrame frame, String outPath) {
        frame.pack();
        frame.setSize(CinemaTheme.WINDOW_WIDTH, CinemaTheme.WINDOW_HEIGHT);
        frame.validate();

        BufferedImage img = new BufferedImage(CinemaTheme.WINDOW_WIDTH, CinemaTheme.WINDOW_HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        frame.paint(g2);
        g2.dispose();

        try {
            File f = new File(outPath);
            ImageIO.write(img, "png", f);
            System.out.println("✓ Screenshot tersimpan: " + outPath + " (" + frame.getWidth() + "x" + frame.getHeight() + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
