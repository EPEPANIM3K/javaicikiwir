package aplikasdatafilm;

import Koneksi.Koneksi;
import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

/**
 * Modern Cinema Studio Management Screen (1280x720).
 * Standardized layout, clear typography, validation, and table search.
 */
public class FormTambahStudio extends JFrame {

    private JTextField txtNamaStudio;
    private JTextField txtSearch;
    private JTable tableStudio;
    private Integer selectedIdStudio = null;

    private JButton btnTambah;
    private JButton btnEdit;
    private JButton btnHapus;
    private JButton btnClear;

    public FormTambahStudio() {
        CinemaTheme.standardizeFrame(this, "TIKET KU — Manajemen Studio Bioskop");
        buildUI();
        loadDataStudio();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(CinemaTheme.BG_DARK);

        // Header
        JPanel header = CinemaTheme.createStaffHeader(this, "Manajemen Studio Bioskop", "ADMINISTRATOR", () -> {
            dispose();
            new Login().setVisible(true);
        });
        root.add(header, BorderLayout.NORTH);

        // Main Content (2 Columns)
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

        JLabel lblSec = new JLabel("TAMBAH / EDIT STUDIO");
        lblSec.setFont(CinemaTheme.FONT_SUBTITLE);
        lblSec.setForeground(CinemaTheme.CREME);
        formCard.add(lblSec);

        JLabel lblSecSub = new JLabel("Kelola nama studio bioskop dan ketersediaan ruangan.");
        lblSecSub.setFont(CinemaTheme.FONT_SMALL);
        lblSecSub.setForeground(CinemaTheme.TEXT_MUTED);
        formCard.add(lblSecSub);

        formCard.add(Box.createVerticalStrut(24));

        JLabel lblNama = new JLabel("Nama Studio (contoh: Studio 1, IMAX, Premiere):");
        lblNama.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblNama.setForeground(CinemaTheme.CREME);
        formCard.add(lblNama);

        formCard.add(Box.createVerticalStrut(6));

        txtNamaStudio = new JTextField();
        CinemaTheme.styleTextField(txtNamaStudio);
        txtNamaStudio.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        txtNamaStudio.setPreferredSize(new Dimension(300, 40));
        txtNamaStudio.addActionListener(e -> {
            if (selectedIdStudio == null) tambahStudio(); else editStudio();
        });
        formCard.add(txtNamaStudio);

        formCard.add(Box.createVerticalStrut(24));

        // Buttons
        JPanel btnGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        btnGrid.setOpaque(false);
        btnGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        btnTambah = CinemaTheme.createSuccessButton("+ Tambah");
        btnTambah.addActionListener(e -> tambahStudio());

        btnEdit = CinemaTheme.createPrimaryButton("Simpan Edit");
        btnEdit.addActionListener(e -> editStudio());

        btnHapus = CinemaTheme.createDangerButton("Hapus");
        btnHapus.addActionListener(e -> hapusStudio());

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

        JLabel lblCari = new JLabel("🔍 Cari Studio:");
        lblCari.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblCari.setForeground(CinemaTheme.CREME);
        searchBar.add(lblCari, BorderLayout.WEST);

        txtSearch = new JTextField();
        CinemaTheme.styleTextField(txtSearch);
        txtSearch.addActionListener(e -> cariDataStudio());
        searchBar.add(txtSearch, BorderLayout.CENTER);

        JPanel searchActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        searchActions.setOpaque(false);
        JButton btnCari = CinemaTheme.createPrimaryButton("Cari");
        btnCari.addActionListener(e -> cariDataStudio());
        JButton btnAll = CinemaTheme.createSecondaryButton("Semua");
        btnAll.addActionListener(e -> {
            txtSearch.setText("");
            loadDataStudio();
        });
        searchActions.add(btnCari);
        searchActions.add(btnAll);
        searchBar.add(searchActions, BorderLayout.EAST);

        tableCard.add(searchBar, BorderLayout.NORTH);

        // Table
        tableStudio = new JTable();
        CinemaTheme.styleTable(tableStudio);
        tableStudio.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tableStudio.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tableStudio.getSelectedRow() != -1) {
                int row = tableStudio.getSelectedRow();
                selectedIdStudio = (Integer) tableStudio.getValueAt(row, 0);
                txtNamaStudio.setText(String.valueOf(tableStudio.getValueAt(row, 1)));
                updateBtnState(true);
            }
        });

        JScrollPane scroll = new JScrollPane(tableStudio);
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
        txtNamaStudio.setText("");
        selectedIdStudio = null;
        tableStudio.clearSelection();
        updateBtnState(false);
    }

    private void loadDataStudio() {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID Studio", "Nama Studio"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try (Connection conn = Koneksi.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT id_studio, nama_studio FROM studio ORDER BY id_studio ASC")) {
            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("id_studio"), rs.getString("nama_studio")});
            }
            tableStudio.setModel(model);
            clearForm();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void cariDataStudio() {
        String keyword = txtSearch.getText().trim();
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID Studio", "Nama Studio"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT id_studio, nama_studio FROM studio WHERE nama_studio LIKE ? ORDER BY id_studio ASC")) {
            ps.setString(1, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{rs.getInt("id_studio"), rs.getString("nama_studio")});
                }
            }
            tableStudio.setModel(model);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void tambahStudio() {
        String nama = txtNamaStudio.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama studio tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);
            int newStudioId;
            try (PreparedStatement ps = conn.prepareStatement("INSERT INTO studio (nama_studio) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, nama);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Gagal membuat studio baru.");
                    newStudioId = rs.getInt(1);
                }
            }

            // Inisialisasi otomatis 20 kursi bioskop standar (A1 s.d. E4) untuk studio baru
            String insertKursi = "INSERT INTO kursi (id_studio, kode_kursi) VALUES (?, ?)";
            try (PreparedStatement psKursi = conn.prepareStatement(insertKursi)) {
                String[] rows = {"A", "B", "C", "D", "E"};
                for (String r : rows) {
                    for (int c = 1; c <= 4; c++) {
                        psKursi.setInt(1, newStudioId);
                        psKursi.setString(2, r + c);
                        psKursi.addBatch();
                    }
                }
                psKursi.executeBatch();
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Studio \"" + nama + "\" dan 20 kursi standar (A1-E4) berhasil dibuat!",
                    "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadDataStudio();

        } catch (SQLIntegrityConstraintViolationException ex) {
            JOptionPane.showMessageDialog(this, "Studio \"" + nama + "\" sudah ada di database!", "Duplikat", JOptionPane.WARNING_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal menambah studio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void editStudio() {
        if (selectedIdStudio == null) return;
        String nama = txtNamaStudio.getText().trim();
        if (nama.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nama studio tidak boleh kosong!", "Validasi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE studio SET nama_studio = ? WHERE id_studio = ?")) {
            ps.setString(1, nama);
            ps.setInt(2, selectedIdStudio);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Studio berhasil diperbarui!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadDataStudio();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Gagal mengedit studio: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void hapusStudio() {
        if (selectedIdStudio == null) return;
        String nama = txtNamaStudio.getText().trim();
        int confirm = JOptionPane.showConfirmDialog(this,
                "Apakah Anda yakin ingin menghapus studio \"" + nama + "\"?",
                "Konfirmasi Hapus", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm != JOptionPane.YES_OPTION) return;

        try (Connection conn = Koneksi.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM studio WHERE id_studio = ?")) {
            ps.setInt(1, selectedIdStudio);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Studio berhasil dihapus!", "Sukses", JOptionPane.INFORMATION_MESSAGE);
            loadDataStudio();
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Studio tidak dapat dihapus karena masih digunakan dalam jadwal penayangan film.",
                    "Gagal Hapus", JOptionPane.ERROR_MESSAGE);
        }
    }
}
