/*
 * Layanan untuk mengunggah gambar ke Supabase Storage via REST API.
 * Menggunakan java.net.http.HttpClient bawaan standar Java 11+.
 */
package aplikasdatafilm;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

/**
 * Service untuk integrasi upload file ke Supabase Storage.
 */
public class SupabaseStorageService {

    // =========================================================================
    // KONFIGURASI SUPABASE
    // Ganti nilai di bawah ini dengan Project URL & API Key dari dashboard Supabase Anda.
    // Dashboard > Project Settings > API
    // =========================================================================
    public static final String SUPABASE_URL = "https://ybjgdhfiuihchrwbjocb.supabase.co";
    public static final String SUPABASE_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InliamdkaGZpdWloY2hyd2Jqb2NiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTY3NzM0NjYsImV4cCI6MjA3MjM0OTQ2Nn0.IY_aLgBvDnR4juPt_qFrLDjrpTOBGCHPe2ycK_oJBAk";
    public static final String BUCKET_NAME = "image";

    private SupabaseStorageService() {
        // utility class
    }

    /**
     * Memeriksa apakah SUPABASE_URL dan SUPABASE_KEY sudah diisi oleh pengguna.
     *
     * @return true jika sudah dikonfigurasi, false jika masih placeholder
     */
    public static boolean isConfigured() {
        return SUPABASE_URL != null 
                && !SUPABASE_URL.contains("your-project-id") 
                && SUPABASE_KEY != null 
                && !SUPABASE_KEY.contains("your-anon-public-key");
    }

    /**
     * Mengunggah file gambar ke Supabase Storage bucket secara langsung.
     *
     * @param file File gambar lokal (JPG, PNG, WebP, GIF)
     * @return Public URL gambar yang tersimpan di Supabase
     * @throws IOException Jika upload gagal atau respon HTTP bukan 200 OK
     * @throws InterruptedException Jika proses request dibatalkan
     */
    public static String uploadGambar(File file) throws IOException, InterruptedException {
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("File tidak ditemukan: " + file.getAbsolutePath());
        }

        // 1. Tentukan ekstensi dan MIME type
        String namaAsli = file.getName();
        String ekstensi = "";
        int dotIdx = namaAsli.lastIndexOf('.');
        if (dotIdx > 0) {
            ekstensi = namaAsli.substring(dotIdx).toLowerCase();
        }

        String mimeType = switch (ekstensi) {
            case ".png" -> "image/png";
            case ".gif" -> "image/gif";
            case ".webp" -> "image/webp";
            default -> "image/jpeg";
        };

        // 2. Buat nama file unik agar tidak bertabrakan di Supabase
        String namaUnik = "poster_" + System.currentTimeMillis() + "_" 
                + UUID.randomUUID().toString().substring(0, 8) + ekstensi;

        // 3. Endpoint upload Supabase Storage
        String endpoint = SUPABASE_URL.replaceAll("/+$", "") 
                + "/storage/v1/object/" + BUCKET_NAME + "/" + namaUnik;

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .header("apikey", SUPABASE_KEY)
                .header("Authorization", "Bearer " + SUPABASE_KEY)
                .header("Content-Type", mimeType)
                .header("x-upsert", "true")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofFile(file.toPath()))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200 || response.statusCode() == 201) {
            // URL publik permanen Supabase
            return SUPABASE_URL.replaceAll("/+$", "") 
                    + "/storage/v1/object/public/" + BUCKET_NAME + "/" + namaUnik;
        } else {
            throw new IOException("Gagal upload gambar ke Supabase (HTTP " 
                    + response.statusCode() + "): " + response.body());
        }
    }
}
