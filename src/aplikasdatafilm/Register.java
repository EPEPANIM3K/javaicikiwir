package aplikasdatafilm;

import Koneksi.Koneksi;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.Arrays;
import java.util.HexFormat;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Cinema Staff Registration Screen (1280x720).
 * Centered card, matching the premium cinema theme.
 */
public class Register extends JFrame {

    private JTextField txtNama;
    private JTextField txtEmail;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirm;

    public Register() {
        CinemaTheme.standardizeFrame(this, "TIKET KU — Registrasi Staff Bioskop");
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        JPanel card = CinemaTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(480, 580));
        card.setMaximumSize(new Dimension(480, 580));
        card.setBorder(new EmptyBorder(28, 40, 28, 40));

        // Header
        JLabel lblIcon = new JLabel("🎬", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 40));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblIcon);

        card.add(Box.createVerticalStrut(6));

        JLabel lblTitle = new JLabel("REGISTRASI STAFF BIOSKOP", SwingConstants.CENTER);
        lblTitle.setFont(CinemaTheme.FONT_TITLE);
        lblTitle.setForeground(CinemaTheme.CREME);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);

        JLabel lblSub = new JLabel("Pendaftaran Akun Petugas Loket / Kasir", SwingConstants.CENTER);
        lblSub.setFont(CinemaTheme.FONT_SMALL);
        lblSub.setForeground(CinemaTheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);

        card.add(Box.createVerticalStrut(20));

        // Nama Staff
        addLabel(card, "Nama Lengkap Petugas:");
        txtNama = new JTextField();
        CinemaTheme.styleTextField(txtNama);
        txtNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(txtNama);

        card.add(Box.createVerticalStrut(10));

        // Email
        addLabel(card, "Email Staff:");
        txtEmail = new JTextField();
        CinemaTheme.styleTextField(txtEmail);
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(txtEmail);

        card.add(Box.createVerticalStrut(10));

        // Password
        addLabel(card, "Password:");
        txtPassword = new JPasswordField();
        CinemaTheme.styleTextField(txtPassword);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        card.add(txtPassword);

        card.add(Box.createVerticalStrut(10));

        // Konfirmasi Password
        addLabel(card, "Konfirmasi Password:");
        txtConfirm = new JPasswordField();
        CinemaTheme.styleTextField(txtConfirm);
        txtConfirm.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        txtConfirm.addActionListener(e -> registerUser());
        card.add(txtConfirm);

        card.add(Box.createVerticalStrut(22));

        // Button: Register
        JButton btnRegister = CinemaTheme.createPrimaryButton("DAFTARKAN AKUN STAFF");
        btnRegister.setFont(CinemaTheme.FONT_SUBTITLE);
        btnRegister.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btnRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnRegister.addActionListener(e -> registerUser());
        card.add(btnRegister);

        card.add(Box.createVerticalStrut(14));

        // Back to Login Link
        JLabel lblBack = new JLabel("Sudah punya akun? Masuk ke Sistem", SwingConstants.CENTER);
        lblBack.setFont(CinemaTheme.FONT_BODY);
        lblBack.setForeground(CinemaTheme.ACCENT_GOLD);
        lblBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblBack.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new Login().setVisible(true);
            }
        });
        card.add(lblBack);

        root.add(card);
        getContentPane().add(root);
    }

    private void addLabel(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(CinemaTheme.FONT_BODY_BOLD);
        lbl.setForeground(CinemaTheme.CREME);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
    }

    private void registerUser() {
        String nama = txtNama.getText().trim();
        String email = txtEmail.getText().trim();
        char[] password = txtPassword.getPassword();
        char[] confirm = txtConfirm.getPassword();

        if (nama.isEmpty() || email.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "Format email tidak valid!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!Arrays.equals(password, confirm)) {
            JOptionPane.showMessageDialog(this, "Konfirmasi password tidak cocok!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.getConnection()) {
            // Check unique email
            try (PreparedStatement checkStmt = conn.prepareStatement("SELECT email FROM pengguna WHERE email = ?")) {
                checkStmt.setString(1, email);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "Email ini sudah terdaftar!", "Peringatan", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                }
            }

            String hash = hashPassword(password);
            String insertSql = "INSERT INTO pengguna (nama, email, password_hash, role) VALUES (?, ?, ?, 'USER')";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, nama);
                insertStmt.setString(2, email);
                insertStmt.setString(3, hash);
                insertStmt.executeUpdate();

                JOptionPane.showMessageDialog(this,
                        "Registrasi akun staff berhasil! Silakan login.",
                        "Sukses", JOptionPane.INFORMATION_MESSAGE);
                dispose();
                new Login().setVisible(true);
            }
        } catch (SQLException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mendaftarkan staff: " + ex.getMessage(),
                    "Kesalahan", JOptionPane.ERROR_MESSAGE);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(confirm, '\0');
        }
    }

    private String hashPassword(char[] password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] bytes = new byte[password.length];
        for (int i = 0; i < password.length; i++) {
            bytes[i] = (byte) password[i];
        }
        byte[] hashed = digest.digest(bytes);
        Arrays.fill(bytes, (byte) 0);
        return HexFormat.of().formatHex(hashed);
    }
}
