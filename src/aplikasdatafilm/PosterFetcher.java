/*
 * Helper untuk mengunduh poster film dari URL secara async
 * dan menampilkannya di JLabel dengan ukuran yang sudah ditentukan.
 */
package aplikasdatafilm;

import java.awt.Image;
import java.io.InputStream;
import java.net.URI;
import java.net.URLConnection;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

/**
 * Utility class untuk fetch dan render poster film dari URL.
 */
public class PosterFetcher {

    /** Lebar poster default dalam piksel. */
    public static final int POSTER_W = 100;
    /** Tinggi poster default dalam piksel. */
    public static final int POSTER_H = 140;

    private PosterFetcher() {
        // utility class — tidak di-instantiate
    }

    /**
     * Mengunduh gambar dari {@code urlStr} di background thread lalu
     * menampilkannya di {@code label} pada EDT. Jika URL null/kosong
     * atau gagal, label akan menampilkan teks "Tidak ada poster".
     *
     * @param label  JLabel yang akan menampilkan gambar
     * @param urlStr URL gambar (http/https)
     */
    public static void muat(JLabel label, String urlStr) {
        label.setIcon(null);
        label.setHorizontalAlignment(JLabel.CENTER);
        label.setVerticalAlignment(JLabel.CENTER);

        if (urlStr == null || urlStr.isBlank()) {
            label.setText("Tidak ada poster");
            return;
        }

        label.setText("Memuat...");

        Thread thread = new Thread(() -> {
            try {
                URI uri = URI.create(urlStr.trim());
                URLConnection connection = uri.toURL().openConnection();
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)");
                connection.setConnectTimeout(6000);
                connection.setReadTimeout(6000);
                
                Image raw;
                try (InputStream in = connection.getInputStream()) {
                    raw = ImageIO.read(in);
                }
                
                if (raw == null) {
                    SwingUtilities.invokeLater(() -> label.setText("Format tidak valid"));
                    return;
                }
                Image scaled = raw.getScaledInstance(POSTER_W, POSTER_H, Image.SCALE_SMOOTH);
                SwingUtilities.invokeLater(() -> {
                    label.setIcon(new ImageIcon(scaled));
                    label.setText("");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> label.setText("Gagal memuat"));
            }
        }, "poster-fetch");
        thread.setDaemon(true);
        thread.start();
    }
}
