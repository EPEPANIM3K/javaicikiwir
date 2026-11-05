package aplikasdatafilm;

import Koneksi.Koneksi;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Modern Genre Management Screen (1280x720).
 * Standardized layout, clear typography, validation, and table search.
 */
public class FormTambahGenre extends JFrame {

    private JTextField txtNamaGenre;
    private JTextField txtSearch;
    private JTable tableGenre;
    private Integer selectedIdGenre = null;

    private JButton btnTambah;
    private JButton btnEdit;
    private JButton btnHapus;
    private JButton btnClear;

    public FormTambahGenre() {
        CinemaTheme.standardizeFrame(this, "TIKET KU — Manajemen Genre Film");
        buildUI();
        loadDataGenre();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Manajemen Genre Film", "ADMINISTRATOR", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Content (2-Column Grid)
        JPanel content = new JPanel(new GridBagLayout());
        content.setOpaque(false);
        content.setBorder(new EmptyBorder(20, 36, 20, 36));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;

        // ── LEFT: FORM INPUT CARD ───────────────────────────────────────────
        gbc.gridx = 0;
        gbc.weightx = 0.40;
        gbc.insets = new Insets(0, 0, 0, 20);

        JPanel formCard = CinemaTheme.createCard();
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblSec = new JLabel("TAMBAH / EDIT GENRE");
        lblSec.setFont(CinemaTheme.FONT_SUBTITLE);
        lblSec.setForeground(CinemaTheme.CREME);
        formCard.add(lblSec);

        JLabel lblSecSub = new JLabel("Kelola nama kategori genre untuk katalog film bioskop.");
        lblSecSub.setFont(CinemaTheme.FONT_SMALL);
        lblSecSub.setForeground(CinemaTheme.TEXT_MUTED);
        formCard.add(lblSecSub);

        formCard.add(Box.createVerticalStrut(24));

        JLabel lblNama = new JLabel("Nama Genre:");
        lblNama.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblNama.setForeground(CinemaTheme.CREME);
        formCard.add(lblNama);

        formCard.add(Box.createVerticalStrut(6));

        txtNamaGenre = new JTextField();
        CinemaTheme.styleTextField(txtNamaGenre);
        txtNamaGenre.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtNamaGenre.setPreferredSize(new Dimension(300, 40));
        txtNamaGenre.addActionListener(e -> {
            if (selectedIdGenre == null) tambahGenre(); else editGenre();
        });
        formCard.add(txtNamaGenre);

        formCard.add(Box.createVerticalStrut(24));

        // Buttons
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        btnGrid.setOpaque(false);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        btnTambah = CinemaTheme.createSuccessButton("+ Tambah");
        btnTambah.addActionListener(e -> tambahGenre());

        btnEdit = CinemaTheme.createPrimaryButton("Simpan Edit");
        btnEdit.addActionListener(e -> editGenre());

        btnHapus = CinemaTheme.createDangerButton("Hapus");
        btnHapus.addActionListener(e -> hapusGenre());

        btnClear = CinemaTheme.createSecondaryButton("Reset Form");
        btnClear.addActionListener(e -> clearForm());

        btnGrid.add(btnTambah);
        btnGrid.add(btnEdit);
        btnGrid.add(btnHapus);
        btnGrid.add(btnClear);
        formCard.add(btnGrid);

        formCard.add(Box.createVerticalGlue());

        // Back to Film Admin
        JButton btnBack = CinemaTheme.createSecondaryButton("← Kembali ke Manajemen Film");
        btnBack.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBack.addActionListener(e -> {
            dispose();
            new FormFilmAdmin().setVisible(true);
        });
        formCard.add(btnBack);

        content.add(formCard, gbc);

        // ── RIGHT: TABLE CARD ──────────────────────────────────────────────
        gbc.gridx = 1;
        gbc.weightx = 0.60;
        gbc.insets = new Insets(0, 0, 0, 0);

        JPanel tableCard = CinemaTheme.createCard();
        tableCard.setLayout(new BorderLayout(0, 12));

        // Search Bar
        JPanel searchBar = new JPanel(new BorderLayout(10, 0));
        searchBar.setOpaque(false);

        JLabel lblCari = new JLabel("🔍 Cari Genre:");
        lblCari.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblCari.setForeground(CinemaTheme.CREME);
        searchBar.add(lblCari, BorderLayout.WEST);

        txtSearch = new JTextField();
        CinemaTheme.styleTextField(txtSearch);
        txtSearch.addActionListener(e -> cariDataGenre());
        searchBar.add(txtSearch, BorderLayout.CENTER);

        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchActions.setOpaque(false);
        JButton btnCari = CinemaTheme.createPrimaryButton("Cari");
        btnCari.addActionListener(e -> cariDataGenre());
        JButton btnAll = CinemaTheme.createSecondaryButton("Semua");
        btnAll.addActionListener(e -> {
            txtSearch.setText("");
            loadDataGenre();
        });
        searchActions.add(btnCari);
        searchActions.add(btnAll);
        searchBar.add(searchActions, BorderLayout.EAST);

        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        tableGenre = new JTable();
        CinemaTheme.styleTable(tableGenre);
        tableGenre.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableGenre.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableGenre.getSelectedRow() != -1) {
                int row = tableGenre.getSelectedRow();
                selectedIdGenre = (Integer) tableGenre.getValueAt(row, 0);
                txtNamaGenre.setText(String.valueOf(tableGenre.getValueAt(row, 1)));
                updateBtnState(true);
            }
        });

        JScrollPane scroll = new JScrollPane(tableGenre);
        scroll.setBorder(BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1));
        scroll.getViewport().setBackground(CinemaTheme.CARD_BG);
        tableCard.add(scroll, BorderLayout.CENTER);

        content.add(tableCard, gbc);
        root.add(content, BorderLayout.CENTER);

        AdminMenuBar.buat(this);
        getContentPane().add(root);
        updateBtnState(false);
    }

    private void updateBtnState(boolean rowSelected) {
        btnTambah.setEnabled(!rowSelected);
        btnEdit.setEnabled(rowSelected);
        btnHapus.setEnabled(rowSelected);
    }

    private void clearForm() {
        txtNamaGenre.setText("");
        selectedIdGenre = null;
        tableGenre.clearSelection();
        updateBtnState(false);
    }

    private void loadDataGenre() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID Genre", "Nama Genre"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_genre, nama_genre FROM genre ORDER BY nama_genre ASC")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id_genre"), rs.getString("nama_genre")});
            }
            tableGenre.setModel(model);
            clearForm();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cariDataGenre() {
        String keyword = txtSearch.getText().trim();
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID Genre", "Nama Genre"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id_genre, nama_genre FROM genre WHERE nama_genre LIKE ? ORDER BY nama_genre ASC")) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt("id_genre"), rs.getString("nama_genre")});
                }
            }
            tableGenre.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void tambahGenre() {
        String nama = txtNamaGenre.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama genre tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("INSERT INTO genre (nama_genre) VALUES (?)")) {
            ps.setString(1, nama);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Genre \"" + nama + "\" berhasil ditambahkan!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadDataGenre();
        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Genre \"" + nama + "\" sudah ada di database!", "Duplikat", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menambah genre: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editGenre() {
        if (selectedIdGenre == null) return;
        String nama = txtNamaGenre.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama genre tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE genre SET nama_genre = ? WHERE id_genre = ?")) {
            ps.setString(1, nama);
            ps.setInt(2, selectedIdGenre);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Genre berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadDataGenre();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengedit genre: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusGenre() {
        if (selectedIdGenre == null) return;
        String nama = txtNamaGenre.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus genre \"" + nama + "\"?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM genre WHERE id_genre = ?")) {
            ps.setInt(1, selectedIdGenre);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Genre berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadDataGenre();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Genre tidak dapat dihapus karena masih digunakan oleh film yang ada di katalog.",
                    "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
        }
    }
}
