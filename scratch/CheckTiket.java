import java.sql.*;
public class CheckTiket {
    public static void main(String[] args) throws Exception {
        try (Connection c = Koneksi.Koneksi.getConnection()) {
            var rs = c.createStatement().executeQuery("SELECT t.id_tiket, t.id_jadwal, k.kode_kursi, f.judul, j.mulai_tayang FROM tiket t JOIN jadwal_tayang j ON j.id_jadwal = t.id_jadwal JOIN film f ON f.id_film = j.id_film JOIN kursi k ON k.id_kursi = t.id_kursi");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("Tiket " + rs.getInt(1) + ": jadwal=" + rs.getInt(2) + ", kursi=" + rs.getString(3) + ", film=" + rs.getString(4) + ", jam=" + rs.getString(5));
            }
            System.out.println("Total tiket di database: " + count);
        }
    }
}
