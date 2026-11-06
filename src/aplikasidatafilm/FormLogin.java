package aplikasidatafilm;

import javax.swing.SwingUtilities;

/**
 * Compatibility wrapper for FormLogin pointing to modern 1280x720 Login.
 */
public class FormLogin extends aplikasdatafilm.Login {

    public FormLogin() {
        super();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new aplikasdatafilm.Login().setVisible(true));
    }
}
