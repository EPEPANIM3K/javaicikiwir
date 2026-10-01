package aplikasidatafilm;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.Arrays;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class FormLogin extends JFrame {

    private final JTextField identityField = new JTextField(24);
    private final JPasswordField passwordField = new JPasswordField(24);
    private final JButton loginButton = new JButton("LOGIN");

    public FormLogin() {
        super("Login Aplikasi Data Film");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginButton.addActionListener(event -> login());
        setContentPane(createLoginPanel());
        getRootPane().setDefaultButton(loginButton);
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(7, 7, 7, 7);
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 2;
        panel.add(new JLabel("Login Admin atau Pengguna"), constraints);

        constraints.gridwidth = 1;
        constraints.gridy++;
        panel.add(new JLabel("Email:"), constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        panel.add(identityField, constraints);

        constraints.gridx = 0;
        constraints.gridy++;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        panel.add(new JLabel("Password:"), constraints);
        constraints.gridx = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        panel.add(passwordField, constraints);

        constraints.gridx = 1;
        constraints.gridy++;
        constraints.fill = GridBagConstraints.NONE;
        constraints.weightx = 0;
        panel.add(loginButton, constraints);
        return panel;
    }

    private void login() {
        String identity = identityField.getText().trim();
        char[] password = passwordField.getPassword();

        if (identity.isEmpty() || password.length == 0) {
            JOptionPane.showMessageDialog(this, "Username/email dan password wajib diisi.");
            Arrays.fill(password, '\0');
            return;
        }

        try {
            LoginService.Role role = LoginService.authenticate(identity, password);
            if (role == LoginService.Role.INVALID) {
                JOptionPane.showMessageDialog(this, "Username/email atau password salah.");
            } else {
                dispose();
                if (role == LoginService.Role.ADMIN) {
                    new Menu().setVisible(true);
                } else {
                    new aplikasdatafilm.PilihFilm().setVisible(true);
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            JOptionPane.showMessageDialog(this, "Algoritma SHA-256 tidak tersedia.");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memeriksa login: " + ex.getMessage());
        } finally {
            Arrays.fill(password, '\0');
            passwordField.setText("");
        }
    }

    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(() -> new FormLogin().setVisible(true));
    }
}