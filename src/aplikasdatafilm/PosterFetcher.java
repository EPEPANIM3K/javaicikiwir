package aplikasdatafilm;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.swing.*;

/**
 * Utility class to load and render movie posters proportionally without distortion.
 * Supports both HTTP/HTTPS remote URLs and local file system paths.
 */
public class PosterFetcher {

    public static final int POSTER_W = 200;
    public static final int POSTER_H = 260;

    private PosterFetcher() {}

    public static void muat(JLabel label, String urlOrPath) {
        muat(label, urlOrPath, POSTER_W, POSTER_H);
    }

    public static void muat(JLabel label, String urlOrPath, int targetWidth, int targetHeight) {
        label.setIcon(null);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);

        if (urlOrPath == null || urlOrPath.isBlank()) {
            label.setText("<html><center>🎬<br>POSTER FILM</center></html>");
            return;
        }

        label.setText("Memuat...");

        final int tw = targetWidth > 0 ? targetWidth : POSTER_W;
        final int th = targetHeight > 0 ? targetHeight : POSTER_H;

        Thread thread = new Thread(() -> {
            try {
                BufferedImage raw = null;
                String path = urlOrPath.trim();

                // Check local file first
                File localFile = new File(path);
                if (localFile.exists()) {
                    raw = ImageIO.read(localFile);
                } else if (path.startsWith("http://") || path.startsWith("https://")) {
                    URI uri = URI.create(path);
                    URLConnection conn = uri.toURL().openConnection();
                    conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                    conn.setConnectTimeout(6000);
                    conn.setReadTimeout(6000);
                    try (InputStream in = conn.getInputStream()) {
                        raw = ImageIO.read(in);
                    }
                }

                if (raw == null) {
                    SwingUtilities.invokeLater(() -> label.setText("<html><center>🎬<br>POSTER FILM</center></html>"));
                    return;
                }

                // Proportional Scaling (Fit to container without distortion)
                int origW = raw.getWidth();
                int origH = raw.getHeight();
                double scale = Math.min((double) tw / origW, (double) th / origH);
                int scaledW = (int) Math.round(origW * scale);
                int scaledH = (int) Math.round(origH * scale);

                Image scaledImage = raw.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);

                // Create buffered image with background fill for consistent card aspect
                BufferedImage finalImg = new BufferedImage(tw, th, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = finalImg.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Center the image inside the target container
                int x = (tw - scaledW) / 2;
                int y = (th - scaledH) / 2;
                g2.drawImage(scaledImage, x, y, null);
                g2.dispose();

                ImageIcon icon = new ImageIcon(finalImg);
                SwingUtilities.invokeLater(() -> {
                    label.setIcon(icon);
                    label.setText("");
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> label.setText("<html><center>🎬<br>POSTER FILM</center></html>"));
            }
        }, "poster-fetch");
        thread.setDaemon(true);
        thread.start();
    }
}
