
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DbInspector {
    public static void main(String[] args) {
        String URL = "jdbc:mysql://localhost:3306/data_film";
        String USER = "root";
        String PASSWORD = "";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet fks = metaData.getImportedKeys(null, null, "film");
            while (fks.next()) {
                System.out.println("film FK: " + fks.getString("FKCOLUMN_NAME") + " references " + fks.getString("PKTABLE_NAME") + "." + fks.getString("PKCOLUMN_NAME"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

