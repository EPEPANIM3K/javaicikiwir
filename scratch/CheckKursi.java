import java.sql.*;
public class CheckKursi {
    public static void main(String[] args) throws Exception {
        try (Connection c = Koneksi.Koneksi.getConnection()) {
            var rs = c.createStatement().executeQuery("SELECT id_studio, COUNT(*), MIN(kode_kursi), MAX(kode_kursi) FROM kursi GROUP BY id_studio");
            while (rs.next()) {
                System.out.println("Studio " + rs.getInt(1) + ": " + rs.getInt(2) + " kursi (min=" + rs.getString(3) + ", max=" + rs.getString(4) + ")");
            }
            var rs2 = c.createStatement().executeQuery("SELECT DISTINCT kode_kursi FROM kursi ORDER BY kode_kursi");
            System.out.print("Kode kursi: ");
            while (rs2.next()) {
                System.out.print(rs2.getString(1) + " ");
            }
            System.out.println();
        }
    }
}
