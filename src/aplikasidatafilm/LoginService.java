package aplikasidatafilm;

import Koneksi.Koneksi;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Locale;

public final class LoginService {

    public enum Role {
        ADMIN,
        PENGGUNA,
        INVALID
    }

    private LoginService() {
    }

    public static Role authenticate(String email, char[] password)
            throws SQLException, NoSuchAlgorithmException {
        String submittedHash = sha256(password);
        Connection connection = Koneksi.getConnection();
        if (connection == null) {
            throw new SQLException("Koneksi ke database gagal.");
        }

        try (connection) {
            String sql = "SELECT password_hash, role FROM pengguna WHERE email = ?";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return Role.INVALID;
                    }

                    String storedHash = result.getString("password_hash");
                    if (!passwordMatches(storedHash, submittedHash)) {
                        return Role.INVALID;
                    }

                    String role = result.getString("role");
                    if ("ADMIN".equals(role)) {
                        return Role.ADMIN;
                    }
                    if ("USER".equals(role)) {
                        return Role.PENGGUNA;
                    }
                    return Role.INVALID;
                }
            }
        }
    }

    private static boolean passwordMatches(String storedHash, String submittedHash) {
        if (storedHash == null) {
            return false;
        }
        byte[] storedBytes = storedHash.trim().toLowerCase(Locale.ROOT).getBytes(StandardCharsets.US_ASCII);
        byte[] submittedBytes = submittedHash.getBytes(StandardCharsets.US_ASCII);
        return MessageDigest.isEqual(storedBytes, submittedBytes);
    }

    private static String sha256(char[] password) throws NoSuchAlgorithmException {
        ByteBuffer encodedPassword = StandardCharsets.UTF_8.encode(CharBuffer.wrap(password));
        byte[] passwordBytes = new byte[encodedPassword.remaining()];
        encodedPassword.get(passwordBytes);
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(passwordBytes);
            return HexFormat.of().formatHex(hash);
        } finally {
            Arrays.fill(passwordBytes, (byte) 0);
        }
    }
}