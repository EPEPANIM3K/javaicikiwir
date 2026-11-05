package aplikasdatafilm;

import java.awt.*;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Cinema Staff Login Portal (1280x720).
 * Centered login card, elegant typography, role-aware navigation (Admin vs Cashier).
 */
public class Login extends JFrame {

    private JTextField txtEmail;
    private JPasswordField txtPassword;

    public Login() {
        CinemaTheme.standardizeFrame(this, "TIKET KU — Cinema Staff & Management Login");
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Center Login Card (width 440)
        JPanel card = CinemaTheme.createCard();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 520));
        card.setMaximumSize(new Dimension(460, 520));
        card.setBorder(new EmptyBorder(32, 40, 32, 40));

        // Cinema Logo & Heading
        JLabel lblIcon = new JLabel("🎬", SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        lblIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblIcon);

        card.add(Box.createVerticalStrut(8));

        JLabel lblTitle = new JLabel("TIKET KU PREMIERE", SwingConstants.CENTER);
        lblTitle.setFont(CinemaTheme.FONT_TITLE_LARGE);
        lblTitle.setForeground(CinemaTheme.CREME);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);

        JLabel lblSub = new JLabel("Cinema Management & Ticketing Portal", SwingConstants.CENTER);
        lblSub.setFont(CinemaTheme.FONT_SMALL);
        lblSub.setForeground(CinemaTheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);

        card.add(Box.createVerticalStrut(28));

        // Email / Username Input
        JLabel lblEmail = new JLabel("Email / Username Staff:");
        lblEmail.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblEmail.setForeground(CinemaTheme.CREME);
        lblEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblEmail);

        card.add(Box.createVerticalStrut(6));

        txtEmail = new JTextField();
        CinemaTheme.styleTextField(txtEmail);
        txtEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtEmail.setPreferredSize(new Dimension(380, 40));
        txtEmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(txtEmail);

        card.add(Box.createVerticalStrut(16));

        // Password Input
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblPassword.setForeground(CinemaTheme.CREME);
        lblPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblPassword);

        card.add(Box.createVerticalStrut(6));

        txtPassword = new JPasswordField();
        CinemaTheme.styleTextField(txtPassword);
        txtPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtPassword.setPreferredSize(new Dimension(380, 40));
        txtPassword.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtPassword.addActionListener(e -> login());
        card.add(txtPassword);

        card.add(Box.createVerticalStrut(24));

        // Login Button
        JButton btnLogin = CinemaTheme.createPrimaryButton("MASUK KE SISTEM BIOSKOP");
        btnLogin.setFont(CinemaTheme.FONT_SUBTITLE);
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> login());
        card.add(btnLogin);

        card.add(Box.createVerticalStrut(18));

        // Register Link
        JLabel lblRegister = new JLabel("Belum punya akun staff? Register Staff Baru", SwingConstants.CENTER);
        lblRegister.setFont(CinemaTheme.FONT_BODY);
        lblRegister.setForeground(CinemaTheme.ACCENT_GOLD);
        lblRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRegister.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblRegister.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                dispose();
                new Register().setVisible(true);
            }
        });
        card.add(lblRegister);

        root.add(card);
        getContentPane().add(root);
    }

    private void login() {
        String identity = txtEmail.getText().trim();
        char[] password = txtPassword.getPassword();

        if (identity.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this,
                    "Username/email dan password wajib diisi.",
                    "Peringatan Login", JOptionPane.WARNING_MESSAGE);
            Arrays.fill(password, '\0');
            return;
        }

        try {
            LoginService.AuthenticatedUser user = LoginService.authenticateUser(identity, password);
            if (user.role() == LoginService.Role.INVALID) {
                JOptionPane.showMessageDialog(this,
                        "Username/email atau password salah.",
                        "Gagal Masuk", JOptionPane.ERROR_MESSAGE);
            } else {
                dispose();
                if (user.role() == LoginService.Role.ADMIN) {
                    new Menu().setVisible(true);
                } else {
                    new PilihFilm(user.idPengguna()).setVisible(true);
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(this, "Algoritma keamanan SHA-256 tidak tersedia.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal koneksi database: " + ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            txtPassword.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Login().setVisible(true));
    }
}
