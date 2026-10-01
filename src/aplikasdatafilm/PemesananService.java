package aplikasdatafilm;

import Koneksi.Koneksi;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PemesananService {

    public record Jadwal(long idJadwal, int idFilm, String judul, String genre, int tahun,
            String sutradara, int durasiMenit, int idStudio, String namaStudio,
            LocalDateTime mulaiTayang, BigDecimal hargaTiket) {
    }

    public record Kursi(int idKursi, String kodeKursi, boolean sudahDipesan) {
    }

    public record Struk(long idPemesanan, LocalDateTime dibuatPada, String metodePembayaran,
            BigDecimal biayaLayanan, String statusPembayaran, String judul, int durasiMenit,
            LocalDateTime mulaiTayang, String namaStudio, String kodeKursi, int jumlah,
            BigDecimal hargaTiket, BigDecimal subtotal) {
        public BigDecimal total() {
            return subtotal.add(biayaLayanan);
        }
    }

    private PemesananService() {
    }

    public static List<Jadwal> cariJadwal(String kataKunci) throws SQLException {
        String sql = "SELECT j.id_jadwal, f.id_film, f.judul, g.nama_genre, f.tahun, "
                + "f.sutradara, f.durasi_menit, j.id_studio, s.nama_studio, "
                + "j.mulai_tayang, j.harga_tiket "
                + "FROM jadwal_tayang j JOIN film f ON f.id_film = j.id_film "
                + "JOIN genre g ON g.id_genre = f.id_genre "
                + "JOIN studio s ON s.id_studio = j.id_studio "
                + "WHERE j.mulai_tayang >= CURRENT_TIMESTAMP "
                + "AND (f.judul LIKE ? OR g.nama_genre LIKE ?) "
                + "ORDER BY j.mulai_tayang, f.judul";
        String pattern = "%" + kataKunci.trim() + "%";
        List<Jadwal> jadwal = new ArrayList<>();
        try (Connection connection = Koneksi.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    jadwal.add(readJadwal(result));
                }
            }
        }
        return jadwal;
    }

    public static List<Jadwal> jadwalUntukFilm(int idFilm) throws SQLException {
        String sql = "SELECT j.id_jadwal, f.id_film, f.judul, g.nama_genre, f.tahun, "
                + "f.sutradara, f.durasi_menit, j.id_studio, s.nama_studio, "
                + "j.mulai_tayang, j.harga_tiket "
                + "FROM jadwal_tayang j JOIN film f ON f.id_film = j.id_film "
                + "JOIN genre g ON g.id_genre = f.id_genre "
                + "JOIN studio s ON s.id_studio = j.id_studio "
                + "WHERE j.id_film = ? AND j.mulai_tayang >= CURRENT_TIMESTAMP "
                + "ORDER BY j.mulai_tayang";
        List<Jadwal> jadwal = new ArrayList<>();
        try (Connection connection = Koneksi.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idFilm);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    jadwal.add(readJadwal(result));
                }
            }
        }
        return jadwal;
    }

    public static List<Kursi> kursiUntukJadwal(long idJadwal) throws SQLException {
        String sql = "SELECT k.id_kursi, k.kode_kursi, t.id_tiket "
                + "FROM jadwal_tayang j JOIN kursi k ON k.id_studio = j.id_studio "
                + "LEFT JOIN tiket t ON t.id_jadwal = j.id_jadwal AND t.id_kursi = k.id_kursi "
                + "WHERE j.id_jadwal = ? ORDER BY k.kode_kursi";
        List<Kursi> kursi = new ArrayList<>();
        try (Connection connection = Koneksi.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idJadwal);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    kursi.add(new Kursi(result.getInt("id_kursi"), result.getString("kode_kursi"),
                            result.getObject("id_tiket") != null));
                }
            }
        }
        return kursi;
    }

    public static long buatPemesanan(long idPengguna, long idJadwal, List<Integer> idKursi,
            String metodePembayaran) throws SQLException {
        if (idPengguna <= 0 || idJadwal <= 0 || idKursi == null || idKursi.isEmpty()) {
            throw new SQLException("Data pemesanan belum lengkap.");
        }
        if (!Set.of("CASH", "QRIS", "TRANSFER_BANK").contains(metodePembayaran)) {
            throw new SQLException("Metode pembayaran tidak valid.");
        }
        Set<Integer> kursiDipilih = new HashSet<>(idKursi);
        if (kursiDipilih.size() != idKursi.size() || kursiDipilih.contains(null)) {
            throw new SQLException("Pilihan kursi tidak valid.");
        }

        try (Connection connection = Koneksi.getConnection()) {
            connection.setAutoCommit(false);
            try {
                BigDecimal hargaTiket;
                String jadwalSql = "SELECT harga_tiket FROM jadwal_tayang "
                        + "WHERE id_jadwal = ? AND mulai_tayang >= CURRENT_TIMESTAMP FOR UPDATE";
                try (PreparedStatement statement = connection.prepareStatement(jadwalSql)) {
                    statement.setLong(1, idJadwal);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Jadwal tidak ditemukan atau waktu tayangnya sudah lewat.");
                        }
                        hargaTiket = result.getBigDecimal("harga_tiket");
                    }
                }

                String placeholders = String.join(", ", java.util.Collections.nCopies(kursiDipilih.size(), "?"));
                String seatSql = "SELECT k.id_kursi, t.id_tiket FROM jadwal_tayang j "
                        + "JOIN kursi k ON k.id_studio = j.id_studio "
                        + "LEFT JOIN tiket t ON t.id_jadwal = j.id_jadwal AND t.id_kursi = k.id_kursi "
                        + "WHERE j.id_jadwal = ? AND k.id_kursi IN (" + placeholders + ") FOR UPDATE";
                Set<Integer> validIds = new HashSet<>();
                try (PreparedStatement statement = connection.prepareStatement(seatSql)) {
                    statement.setLong(1, idJadwal);
                    int index = 2;
                    for (int idKursiDipilih : kursiDipilih) {
                        statement.setInt(index++, idKursiDipilih);
                    }
                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            validIds.add(result.getInt("id_kursi"));
                            if (result.getObject("id_tiket") != null) {
                                throw new SQLException("Salah satu kursi baru saja dipesan pengguna lain.");
                            }
                        }
                    }
                }
                if (!validIds.equals(kursiDipilih)) {
                    throw new SQLException("Kursi yang dipilih tidak tersedia di studio untuk jadwal ini.");
                }

                BigDecimal subtotal = hargaTiket.multiply(BigDecimal.valueOf(kursiDipilih.size()));
                BigDecimal biayaLayanan = subtotal.multiply(new BigDecimal("0.10"))
                        .setScale(2, RoundingMode.HALF_UP);
                long idPemesanan;
                String bookingSql = "INSERT INTO pemesanan "
                        + "(id_pengguna, metode_pembayaran, biaya_layanan, status_pembayaran) "
                        + "VALUES (?, ?, ?, 'PENDING')";
                try (PreparedStatement statement = connection.prepareStatement(bookingSql,
                        PreparedStatement.RETURN_GENERATED_KEYS)) {
                    statement.setLong(1, idPengguna);
                    statement.setString(2, metodePembayaran);
                    statement.setBigDecimal(3, biayaLayanan);
                    statement.executeUpdate();
                    try (ResultSet keys = statement.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("Nomor pemesanan tidak berhasil dibuat.");
                        }
                        idPemesanan = keys.getLong(1);
                    }
                }

                String ticketSql = "INSERT INTO tiket "
                        + "(id_pemesanan, id_jadwal, id_kursi, harga_saat_beli) VALUES (?, ?, ?, ?)";
                try (PreparedStatement statement = connection.prepareStatement(ticketSql)) {
                    for (int idKursiDipilih : kursiDipilih) {
                        statement.setLong(1, idPemesanan);
                        statement.setLong(2, idJadwal);
                        statement.setInt(3, idKursiDipilih);
                        statement.setBigDecimal(4, hargaTiket);
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }
                connection.commit();
                return idPemesanan;
            } catch (SQLException exception) {
                connection.rollback();
                if ("23000".equals(exception.getSQLState())) {
                    throw new SQLException("Kursi sudah dipesan atau data terkait tidak lagi tersedia.",
                            exception.getSQLState(), exception.getErrorCode(), exception);
                }
                throw exception;
            }
        }
    }

    public static Struk ambilStruk(long idPemesanan) throws SQLException {
        String sql = "SELECT p.id_pemesanan, p.dibuat_pada, p.metode_pembayaran, "
                + "p.biaya_layanan, p.status_pembayaran, f.judul, f.durasi_menit, "
                + "j.mulai_tayang, s.nama_studio, GROUP_CONCAT(k.kode_kursi "
                + "ORDER BY k.kode_kursi SEPARATOR ', ') AS kode_kursi, COUNT(t.id_tiket) AS jumlah, "
                + "MIN(t.harga_saat_beli) AS harga_tiket, SUM(t.harga_saat_beli) AS subtotal "
                + "FROM pemesanan p JOIN tiket t ON t.id_pemesanan = p.id_pemesanan "
                + "JOIN jadwal_tayang j ON j.id_jadwal = t.id_jadwal "
                + "JOIN film f ON f.id_film = j.id_film "
                + "JOIN studio s ON s.id_studio = j.id_studio "
                + "JOIN kursi k ON k.id_kursi = t.id_kursi "
                + "WHERE p.id_pemesanan = ? "
                + "GROUP BY p.id_pemesanan, p.dibuat_pada, p.metode_pembayaran, "
                + "p.biaya_layanan, p.status_pembayaran, f.judul, f.durasi_menit, "
                + "j.mulai_tayang, s.nama_studio";
        try (Connection connection = Koneksi.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, idPemesanan);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Data struk tidak ditemukan.");
                }
                return new Struk(result.getLong("id_pemesanan"),
                        result.getTimestamp("dibuat_pada").toLocalDateTime(),
                        result.getString("metode_pembayaran"), result.getBigDecimal("biaya_layanan"),
                        result.getString("status_pembayaran"), result.getString("judul"),
                        result.getInt("durasi_menit"), result.getTimestamp("mulai_tayang").toLocalDateTime(),
                        result.getString("nama_studio"), result.getString("kode_kursi"),
                        result.getInt("jumlah"), result.getBigDecimal("harga_tiket"),
                        result.getBigDecimal("subtotal"));
            }
        }
    }

    private static Jadwal readJadwal(ResultSet result) throws SQLException {
        Timestamp mulaiTayang = result.getTimestamp("mulai_tayang");
        return new Jadwal(result.getLong("id_jadwal"), result.getInt("id_film"),
                result.getString("judul"), result.getString("nama_genre"), result.getInt("tahun"),
                result.getString("sutradara"), result.getInt("durasi_menit"), result.getInt("id_studio"),
                result.getString("nama_studio"), mulaiTayang.toLocalDateTime(),
                result.getBigDecimal("harga_tiket"));
    }
}