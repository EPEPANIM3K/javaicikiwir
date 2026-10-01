package Koneksi;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Koneksi {

    private static final String URL = "jdbc:mysql://localhost:3306/data_film";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void main(String[] args) {
        try (Connection conn = getConnection()) {
            System.out.println("DATABASE: data_film");
        } catch (SQLException exception) {
            System.err.println("Koneksi database gagal: " + exception.getMessage());
            exception.printStackTrace();
        }
    }
}