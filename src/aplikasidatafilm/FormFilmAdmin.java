package aplikasidatafilm;

import javax.swing.SwingUtilities;

/**
 * Compatibility wrapper for FormFilmAdmin pointing to modern 1280x720 FormFilmAdmin.
 */
public class FormFilmAdmin extends aplikasdatafilm.FormFilmAdmin {

    public FormFilmAdmin() {
        super();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new aplikasdatafilm.FormFilmAdmin().setVisible(true));
    }
}
