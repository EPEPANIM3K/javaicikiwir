package aplikasidatafilm;

import javax.swing.SwingUtilities;

/**
 * Compatibility wrapper for FormTambahGenre pointing to modern 1280x720 FormTambahGenre.
 */
public class FormTambahGenre extends aplikasdatafilm.FormTambahGenre {

    public FormTambahGenre() {
        super();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new aplikasdatafilm.FormTambahGenre().setVisible(true));
    }
}
