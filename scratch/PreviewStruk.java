package scratch;

import aplikasdatafilm.CetakStruk;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Render preview struk tanpa koneksi DB dengan dummy data.
 */
public class PreviewStruk {

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        // Akses inner classes via reflection karena private
        Class<?> clazzStruk = Class.forName("aplikasdatafilm.CetakStruk");
        Class<?> clazzBarisTiket = null;
        Class<?> clazzDataStruk  = null;
        for (Class<?> inner : clazzStruk.getDeclaredClasses()) {
            if (inner.getSimpleName().equals("BarisTiket"))  clazzBarisTiket = inner;
            if (inner.getSimpleName().equals("DataStruk"))   clazzDataStruk  = inner;
        }

        // Buat BarisTiket records via constructor
        Constructor<?> btCtor = clazzBarisTiket.getDeclaredConstructor(String.class, BigDecimal.class);
        btCtor.setAccessible(true);
        Object bt1 = btCtor.newInstance("A1", new BigDecimal("50000"));
        Object bt2 = btCtor.newInstance("A2", new BigDecimal("50000"));

        // Buat DataStruk record
        Constructor<?> dsCtor = clazzDataStruk.getDeclaredConstructor(
                long.class, LocalDateTime.class, String.class, String.class,
                LocalDateTime.class, int.class, String.class,
                String.class, String.class, BigDecimal.class, List.class);
        dsCtor.setAccessible(true);
        Object dataStruk = dsCtor.newInstance(
                42L,
                LocalDateTime.of(2026, 10, 2, 13, 30),
                "Budi Santoso",
                "Interstellar",
                LocalDateTime.of(2026, 10, 2, 19, 30),
                169,
                "Studio 1",
                "CASH",
                "PENDING",
                new BigDecimal("10000"),
                List.of(bt1, bt2)
        );

        // Buat StrukRenderer
        Class<?> clazzRenderer = null;
        for (Class<?> inner : clazzStruk.getDeclaredClasses()) {
            if (inner.getSimpleName().equals("StrukRenderer")) clazzRenderer = inner;
        }
        Constructor<?> rendCtor = clazzRenderer.getDeclaredConstructor(clazzDataStruk, boolean.class);
        rendCtor.setAccessible(true);
        Object renderer = rendCtor.newInstance(dataStruk, true);

        // Render ke BufferedImage
        int w = 320;
        BufferedImage img = new BufferedImage(w, 800, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, w, 800);
        g2.setColor(Color.BLACK);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        Method renderMethod = clazzRenderer.getDeclaredMethod("render", Graphics2D.class, int.class);
        renderMethod.setAccessible(true);
        int contentH = (int) renderMethod.invoke(renderer, g2, w - 28);
        g2.dispose();

        // Crop
        BufferedImage cropped = img.getSubimage(0, 0, w, contentH + 20);
        ImageIO.write(cropped, "png", new File("scratch/struk_preview.png"));
        System.out.println("Preview saved: scratch/struk_preview.png (height=" + (contentH + 20) + ")");
    }
}
