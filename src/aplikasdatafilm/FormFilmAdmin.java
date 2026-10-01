/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasdatafilm;

import Koneksi.Koneksi;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author WINDOWS 11
 */
public class FormFilmAdmin extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(FormFilmAdmin.class.getName());
    private Map<String, Integer> genreMap = new HashMap<>();
    private final Map<String, Integer> studioMap = new HashMap<>();
    private boolean memperbaruiJadwal;
    private LocalDate tanggalGrupJadwalAwal;
    private Integer studioGrupJadwalAwal;
    private Integer filmGrupJadwalAwal;

    public FormFilmAdmin() {
        initComponents();
        jTextFieldID.setEditable(false);
        PlaceholderSupport.install(jTextField1, "Cari judul atau sutradara");
        PlaceholderSupport.install(jTextFieldJUDULFILM, "Masukkan judul film");
        PlaceholderSupport.install(jTextFieldTAHUN, "Contoh: 2026");
        PlaceholderSupport.install(jTextFieldSUTRADARA, "Masukkan nama sutradara");
        PlaceholderSupport.install(jTextFieldDURASI, "Durasi dalam menit");
        PlaceholderSupport.install(jTextFieldRATING, "Rating 0 sampai 10");
        PlaceholderSupport.install(jTextFieldTanggal, "YYYY-MM-DD");
        PlaceholderSupport.install(jTextFieldTAHUN1, "Contoh: 14.00, 15.00");
        PlaceholderSupport.install(jTextFieldHarga, "Harga tiket");
        jButton3.setVisible(false);
        jButton4.setVisible(false);
        loadComboGenre();
        loadComboStudio();
        jTextFieldTanggal.addActionListener(event -> muatJadwalGrupTerpilih());
        jComboBoxstudio.addActionListener(event -> {
            if (!memperbaruiJadwal) {
                muatJadwalGrupTerpilih();
            }
        });
        loadDataFilm();

        jButton1.addActionListener(e -> tambahFilm());
        jButton3.addActionListener(e -> editFilm());
        jButton4.addActionListener(e -> hapusFilm());
        jButton5.addActionListener(e -> cariDataFilm());
        jButton8.addActionListener(event -> {
            new FormTambahStudio().setVisible(true);
            dispose();
        });

        jButton7.addActionListener(e -> {
            new FormTambahGenre().setVisible(true);
            dispose();
        });

        jTable1.getSelectionModel().addListSelectionListener(event -> {
            if (!event.getValueIsAdjusting() && jTable1.getSelectedRow() != -1) {
                int row = jTable1.getSelectedRow();
                jTextFieldID.setText(jTable1.getValueAt(row, 0).toString());
                PlaceholderSupport.setText(jTextFieldJUDULFILM, jTable1.getValueAt(row, 1).toString());
                jComboBoxGENRE.setSelectedItem(jTable1.getValueAt(row, 2).toString());
                PlaceholderSupport.setText(jTextFieldTAHUN, jTable1.getValueAt(row, 3).toString());
                PlaceholderSupport.setText(jTextFieldSUTRADARA, jTable1.getValueAt(row, 4).toString());
                PlaceholderSupport.setText(jTextFieldDURASI, jTable1.getValueAt(row, 5).toString());
                PlaceholderSupport.setText(jTextFieldRATING, jTable1.getValueAt(row, 6).toString());
                muatJadwalFilm(Integer.parseInt(jTextFieldID.getText()));
            }
        });
        pack();
        setLocationRelativeTo(null);
    }

    private record JadwalFilm(int idStudio, LocalDateTime mulaiTayang,
            java.math.BigDecimal hargaTiket, boolean memilikiTiket) {
    }

    private void updateFilmActionButtons(int jumlahFilm) {
        boolean adaFilm = jumlahFilm > 0;
        jButton3.setVisible(adaFilm);
        jButton4.setVisible(adaFilm);
    }

    private void loadComboStudio() {
        jComboBoxstudio.removeAllItems();
        studioMap.clear();
        try (Connection connection = Koneksi.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT id_studio, nama_studio FROM studio ORDER BY id_studio");
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                String namaStudio = result.getString("nama_studio");
                studioMap.put(namaStudio, result.getInt("id_studio"));
                jComboBoxstudio.addItem(namaStudio);
            }
            jComboBoxstudio.setSelectedIndex(-1);
        } catch (SQLException exception) {
            tampilkanErrorDatabase("Data studio gagal dimuat.", exception);
        }
    }

    private void muatJadwalFilm(int idFilm) {
        String sql = "SELECT j.id_studio, j.mulai_tayang, j.harga_tiket, "
                + "EXISTS (SELECT 1 FROM tiket t WHERE t.id_jadwal = j.id_jadwal) AS memiliki_tiket "
                + "FROM jadwal_tayang j WHERE j.id_film = ? ORDER BY j.mulai_tayang, j.id_studio";
        List<JadwalFilm> jadwal = new ArrayList<>();
        try (Connection connection = Koneksi.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idFilm);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    jadwal.add(new JadwalFilm(result.getInt("id_studio"),
                            result.getTimestamp("mulai_tayang").toLocalDateTime(),
                            result.getBigDecimal("harga_tiket"), result.getBoolean("memiliki_tiket")));
                }
            }
            resetJadwalFields();
            if (jadwal.isEmpty()) {
                setJadwalAwal(null, null, null);
                return;
            }
            JadwalFilm pertama = jadwal.get(0);
            LocalDate tanggal = pertama.mulaiTayang().toLocalDate();
            List<LocalTime> jam = jadwal.stream()
                    .filter(item -> item.idStudio() == pertama.idStudio()
                            && item.mulaiTayang().toLocalDate().equals(tanggal))
                    .map(item -> item.mulaiTayang().toLocalTime())
                    .toList();
            isiGrupJadwal(tanggal, pertama.idStudio(), pertama.hargaTiket(), jam);
            setJadwalAwal(idFilm, tanggal, pertama.idStudio());
        } catch (SQLException exception) {
            tampilkanErrorDatabase("Jadwal tayang film gagal dimuat.", exception);
        }
    }

    private void muatJadwalGrupTerpilih() {
        if (jTextFieldID.getText().isBlank()) {
            return;
        }
        String tanggalText = PlaceholderSupport.getText(jTextFieldTanggal).trim();
        String namaStudio = (String) jComboBoxstudio.getSelectedItem();
        Integer idStudio = namaStudio == null ? null : studioMap.get(namaStudio);
        if (tanggalText.isEmpty() || idStudio == null) {
            return;
        }
        try {
            LocalDate tanggal = LocalDate.parse(tanggalText);
            String sql = "SELECT j.mulai_tayang, j.harga_tiket, "
                    + "EXISTS (SELECT 1 FROM tiket t WHERE t.id_jadwal = j.id_jadwal) AS memiliki_tiket "
                    + "FROM jadwal_tayang j WHERE j.id_film = ? AND j.id_studio = ? "
                    + "AND DATE(j.mulai_tayang) = ? ORDER BY j.mulai_tayang";
            List<JadwalFilm> jadwal = new ArrayList<>();
            try (Connection connection = Koneksi.getConnection();
                 PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, Integer.parseInt(jTextFieldID.getText()));
                statement.setInt(2, idStudio);
                statement.setDate(3, java.sql.Date.valueOf(tanggal));
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        jadwal.add(new JadwalFilm(idStudio,
                                result.getTimestamp("mulai_tayang").toLocalDateTime(),
                                result.getBigDecimal("harga_tiket"), result.getBoolean("memiliki_tiket")));
                    }
                }
            }
            resetJamTayang();
            if (jadwal.isEmpty()) {
                setJadwalAwal(null, null, null);
                return;
            }
            java.math.BigDecimal harga = jadwal.get(0).hargaTiket();
            if (jadwal.stream().anyMatch(item -> item.hargaTiket().compareTo(harga) != 0)) {
                JOptionPane.showMessageDialog(this,
                        "Harga tiket pada grup jadwal ini berbeda. Edit setiap jadwal melalui pemilihan grup yang sesuai.");
                setJadwalAwal(null, null, null);
                return;
            }
                isiGrupJadwal(tanggal, idStudio, harga, jadwal.stream()
                    .map(item -> item.mulaiTayang().toLocalTime()).toList());
                setJadwalAwal(Integer.parseInt(jTextFieldID.getText()), tanggal, idStudio);
        } catch (java.time.DateTimeException | NumberFormatException exception) {
            JOptionPane.showMessageDialog(this, "Tanggal jadwal harus menggunakan format YYYY-MM-DD.");
        } catch (SQLException exception) {
            tampilkanErrorDatabase("Jadwal tayang gagal dimuat.", exception);
        }
    }

    private void isiGrupJadwal(LocalDate tanggal, int idStudio,
            java.math.BigDecimal harga, List<LocalTime> jam) {
        memperbaruiJadwal = true;
        PlaceholderSupport.setText(jTextFieldTanggal, tanggal.toString());
        PlaceholderSupport.setText(jTextFieldHarga, harga.toPlainString());
        String namaStudio = studioMap.entrySet().stream()
                .filter(entry -> entry.getValue() == idStudio)
                .map(Map.Entry::getKey).findFirst().orElse(null);
        jComboBoxstudio.setSelectedItem(namaStudio);
        memperbaruiJadwal = false;
        PlaceholderSupport.setText(jTextFieldTAHUN1, formatJam(jam));
    }

    private void resetJamTayang() {
        PlaceholderSupport.reset(jTextFieldTAHUN1);
    }

    private void resetJadwalFields() {
        PlaceholderSupport.reset(jTextFieldTanggal);
        PlaceholderSupport.reset(jTextFieldHarga);
        PlaceholderSupport.reset(jTextFieldTAHUN1);
        memperbaruiJadwal = true;
        jComboBoxstudio.setSelectedIndex(-1);
        memperbaruiJadwal = false;
    }

    private String formatJam(List<LocalTime> jam) {
        return String.join(", ", jam.stream()
                .map(waktu -> waktu.format(DateTimeFormatter.ofPattern("HH.mm"))).toList());
    }

    private void setJadwalAwal(Integer idFilm, LocalDate tanggal, Integer idStudio) {
        filmGrupJadwalAwal = idFilm;
        tanggalGrupJadwalAwal = tanggal;
        studioGrupJadwalAwal = idStudio;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jLabel9 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jTextFieldID = new javax.swing.JTextField();
        jTextFieldJUDULFILM = new javax.swing.JTextField();
        jTextFieldTAHUN = new javax.swing.JTextField();
        jTextFieldSUTRADARA = new javax.swing.JTextField();
        jTextFieldDURASI = new javax.swing.JTextField();
        jTextFieldRATING = new javax.swing.JTextField();
        jComboBoxGENRE = new javax.swing.JComboBox<>();
        jButton7 = new javax.swing.JButton();
        jTextFieldHarga = new javax.swing.JTextField();
        jTextFieldTanggal = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        jComboBoxstudio = new javax.swing.JComboBox<>();
        jButton8 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();
        jTextFieldTAHUN1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("DATA FILM - ADMIN");

        jLabel2.setText("ID Film       :");

        jLabel3.setText("Judul Film  :");

        jLabel4.setText("Genre         :");

        jLabel5.setText("Tahun        :");

        jLabel6.setText("Sutradara  :");

        jLabel7.setText("Durasi       :");

        jLabel8.setText("Rating       :");

        jButton1.setText("TAMBAH FILM");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setText("EDIT");

        jButton4.setText("HAPUS");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel9.setText("Cari Film :");

        jButton5.setText("CARI");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "ID", "Judul", "Genre", "Tahun", "Sutradara", "Durasi", "Rating"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        jTextFieldID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldIDActionPerformed(evt);
            }
        });

        jComboBoxGENRE.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxGENRE.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxGENREActionPerformed(evt);
            }
        });

        jButton7.setText("Tambah Genre");

        jLabel11.setText("Harga       :");

        jLabel12.setText("Studio         :");
        jLabel14.setText("Tanggal      :");

        jComboBoxstudio.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBoxstudio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBoxstudioActionPerformed(evt);
            }
        });

        jButton8.setText("Tambah Studio");

        jLabel13.setText("Jam Tayang:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel12, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .addComponent(jLabel14, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                    .addComponent(jLabel13, javax.swing.GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextFieldDURASI)
                    .addComponent(jTextFieldSUTRADARA)
                    .addComponent(jTextFieldTAHUN)
                    .addComponent(jComboBoxGENRE, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldJUDULFILM)
                    .addComponent(jTextFieldID, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldRATING, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldHarga, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jComboBoxstudio, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTextFieldTanggal)
                    .addComponent(jTextFieldTAHUN1))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton7)
                    .addComponent(jButton8))
                .addGap(340, 340, 340))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(268, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(319, 319, 319))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addComponent(jButton3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)
                        .addContainerGap())))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButton1)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(268, javax.swing.GroupLayout.PREFERRED_SIZE))
            .addComponent(jScrollPane1)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel1)
                .addGap(52, 52, 52)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextFieldID, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jTextFieldJUDULFILM, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBoxGENRE, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton7))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jComboBoxstudio, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton8))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jTextFieldTanggal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel13)
                    .addComponent(jTextFieldTAHUN1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextFieldTAHUN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jTextFieldSUTRADARA, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jTextFieldDURASI, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(jTextFieldRATING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel11)
                    .addComponent(jTextFieldHarga, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton1)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 289, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(98, 98, 98)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton4)
                    .addComponent(jButton3))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jComboBoxstudioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxstudioActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxstudioActionPerformed

    private void jComboBoxGENREActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBoxGENREActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxGENREActionPerformed

    private void jTextFieldIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldIDActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldIDActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void loadComboGenre() {
        jComboBoxGENRE.removeAllItems();
        genreMap.clear();
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_genre, nama_genre FROM genre");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id_genre");
                String nama = rs.getString("nama_genre");
                genreMap.put(nama, id);
                jComboBoxGENRE.addItem(nama);
            }
        } catch (SQLException ex) {
            tampilkanErrorDatabase("Genre gagal dimuat.", ex);
        }
    }

    private DefaultTableModel modelFilmKosong() {
        return new DefaultTableModel(new String[]{"ID", "Judul", "Genre", "Tahun", "Sutradara", "Durasi", "Rating"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private record FilmInput(String judul, int idGenre, int tahun, String sutradara,
            int durasi, java.math.BigDecimal rating, List<LocalTime> jamTayang,
            boolean jadwalDitentukan, LocalDate tanggalJadwal, Integer idStudio,
            java.math.BigDecimal hargaTiket) {
    }

    private FilmInput bacaInputFilm(boolean modeEdit) {
        String judul = PlaceholderSupport.getText(jTextFieldJUDULFILM).trim();
        String namaGenre = (String) jComboBoxGENRE.getSelectedItem();
        String tahunText = PlaceholderSupport.getText(jTextFieldTAHUN).trim();
        String sutradara = PlaceholderSupport.getText(jTextFieldSUTRADARA).trim();
        String durasiText = PlaceholderSupport.getText(jTextFieldDURASI).trim();
        String ratingText = PlaceholderSupport.getText(jTextFieldRATING).trim().replace(',', '.');
        Integer idGenre = namaGenre == null ? null : genreMap.get(namaGenre);

        if (judul.isEmpty() || idGenre == null || tahunText.isEmpty() || sutradara.isEmpty()
                || durasiText.isEmpty() || ratingText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Semua field harus diisi dan genre harus tersedia.");
            return null;
        }
        if (judul.length() > 200 || sutradara.length() > 120) {
            JOptionPane.showMessageDialog(this, "Judul maksimal 200 karakter dan sutradara maksimal 120 karakter.");
            return null;
        }

        try {
            int tahun = Integer.parseInt(tahunText);
            int durasi = Integer.parseInt(durasiText);
            java.math.BigDecimal rating = new java.math.BigDecimal(ratingText);
            if (tahun < 0 || tahun > 65535 || durasi < 1 || durasi > 65535
                    || rating.compareTo(java.math.BigDecimal.ZERO) < 0
                    || rating.compareTo(new java.math.BigDecimal("10.0")) > 0 || rating.scale() > 1) {
                JOptionPane.showMessageDialog(this,
                        "Tahun/durasi di luar batas database; durasi harus positif dan rating 0 sampai 10 (maksimal 1 desimal).");
                return null;
            }
            List<LocalTime> jamTayang = new ArrayList<>();
            String inputJam = PlaceholderSupport.getText(jTextFieldTAHUN1).trim();
            for (String nilaiJam : inputJam.split(",", -1)) {
                if (nilaiJam.trim().isEmpty()) {
                    continue;
                }
                LocalTime waktu = LocalTime.parse(nilaiJam.trim(), DateTimeFormatter.ofPattern("HH.mm"));
                if (jamTayang.contains(waktu)) {
                    JOptionPane.showMessageDialog(this, "Jam tayang tidak boleh duplikat.");
                    return null;
                }
                jamTayang.add(waktu);
            }
            jamTayang.sort(LocalTime::compareTo);

            String tanggalText = PlaceholderSupport.getText(jTextFieldTanggal).trim();
            String hargaText = PlaceholderSupport.getText(jTextFieldHarga).trim().replace(',', '.');
            boolean jadwalDitentukan = !jamTayang.isEmpty() || !tanggalText.isEmpty()
                    || !hargaText.isEmpty() || (modeEdit && filmGrupJadwalAwal != null);
            LocalDate tanggalJadwal = null;
            Integer idStudio = null;
            java.math.BigDecimal hargaTiket = null;
            if (jadwalDitentukan) {
                String namaStudio = (String) jComboBoxstudio.getSelectedItem();
                Integer studio = namaStudio == null ? null : studioMap.get(namaStudio);
                if (tanggalText.isEmpty() || studio == null || hargaText.isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                            "Isi tanggal, studio, dan harga tiket jika menambahkan atau mengubah jam tayang.");
                    return null;
                }
                if (jamTayang.isEmpty() && (!modeEdit || filmGrupJadwalAwal == null)) {
                    JOptionPane.showMessageDialog(this,
                            "Tambahkan minimal satu jam tayang atau kosongkan semua detail jadwal.");
                    return null;
                }
                tanggalJadwal = LocalDate.parse(tanggalText);
                if (tanggalJadwal.isBefore(LocalDate.now())) {
                    JOptionPane.showMessageDialog(this, "Tanggal tayang tidak boleh di masa lalu.");
                    return null;
                }
                hargaTiket = new java.math.BigDecimal(hargaText);
                if (hargaTiket.compareTo(java.math.BigDecimal.ZERO) <= 0
                    || hargaTiket.scale() > 2 || hargaTiket.precision() > 10) {
                    JOptionPane.showMessageDialog(this,
                            "Harga tiket harus lebih dari 0 dan maksimal 2 angka desimal.");
                    return null;
                }
                idStudio = studio;
                for (LocalTime waktu : jamTayang) {
                    if (LocalDateTime.of(tanggalJadwal, waktu).isBefore(LocalDateTime.now())) {
                        JOptionPane.showMessageDialog(this, "Jam tayang tidak boleh di masa lalu.");
                        return null;
                    }
                }
            }
            return new FilmInput(judul, idGenre, tahun, sutradara, durasi, rating,
                    List.copyOf(jamTayang), jadwalDitentukan, tanggalJadwal, idStudio, hargaTiket);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Tahun dan durasi harus bilangan bulat; rating harus angka valid.");
            return null;
        } catch (java.time.DateTimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Tanggal harus YYYY-MM-DD dan setiap jam tayang harus menggunakan format HH.mm.");
            return null;
        }
    }

    private List<JadwalFilm> ambilGrupJadwal(Connection connection, int idFilm,
            LocalDate tanggal, int idStudio) throws SQLException {
        String sql = "SELECT j.id_studio, j.mulai_tayang, j.harga_tiket, "
                + "EXISTS (SELECT 1 FROM tiket t WHERE t.id_jadwal = j.id_jadwal) AS memiliki_tiket "
                + "FROM jadwal_tayang j WHERE j.id_film = ? AND j.id_studio = ? "
                + "AND DATE(j.mulai_tayang) = ? ORDER BY j.mulai_tayang FOR UPDATE";
        List<JadwalFilm> jadwal = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, idFilm);
            statement.setInt(2, idStudio);
            statement.setDate(3, java.sql.Date.valueOf(tanggal));
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    jadwal.add(new JadwalFilm(result.getInt("id_studio"),
                            result.getTimestamp("mulai_tayang").toLocalDateTime(),
                            result.getBigDecimal("harga_tiket"), result.getBoolean("memiliki_tiket")));
                }
            }
        }
        return jadwal;
    }

    private void simpanJadwalFilm(Connection connection, int idFilm,
            FilmInput input, boolean filmBaru) throws SQLException {
        if (!input.jadwalDitentukan()) {
            return;
        }

        List<JadwalFilm> grupLama = List.of();
        boolean grupLamaAda = !filmBaru && filmGrupJadwalAwal != null
                && filmGrupJadwalAwal == idFilm && tanggalGrupJadwalAwal != null
                && studioGrupJadwalAwal != null;
        if (grupLamaAda) {
            grupLama = ambilGrupJadwal(connection, idFilm,
                    tanggalGrupJadwalAwal, studioGrupJadwalAwal);
        }

        boolean targetSamaDenganLama = grupLamaAda
                && tanggalGrupJadwalAwal.equals(input.tanggalJadwal())
                && studioGrupJadwalAwal.equals(input.idStudio());
        if (grupLama.stream().anyMatch(JadwalFilm::memilikiTiket)) {
            boolean jamSama = grupLama.stream().map(item -> item.mulaiTayang().toLocalTime()).toList()
                    .equals(input.jamTayang());
            boolean hargaSama = grupLama.stream()
                    .allMatch(item -> item.hargaTiket().compareTo(input.hargaTiket()) == 0);
            if (targetSamaDenganLama && jamSama && hargaSama) {
                return;
            }
            throw new SQLException("Jadwal yang sudah memiliki tiket tidak dapat diubah atau dihapus.");
        }

        if (grupLamaAda && !grupLama.isEmpty()) {
            String deleteSql = "DELETE FROM jadwal_tayang WHERE id_film = ? AND id_studio = ? AND DATE(mulai_tayang) = ?";
            try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
                statement.setInt(1, idFilm);
                statement.setInt(2, studioGrupJadwalAwal);
                statement.setDate(3, java.sql.Date.valueOf(tanggalGrupJadwalAwal));
                statement.executeUpdate();
            }
        }

        List<JadwalFilm> grupTujuan = ambilGrupJadwal(connection, idFilm,
                input.tanggalJadwal(), input.idStudio());
        if (!grupTujuan.isEmpty()) {
            throw new SQLException("Grup jadwal tujuan sudah ada. Pilih tanggal dan studio tersebut lalu tekan Enter untuk memuatnya.");
        }

        String insertSql = "INSERT INTO jadwal_tayang (id_film, id_studio, mulai_tayang, harga_tiket) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (LocalTime waktu : input.jamTayang()) {
                statement.setInt(1, idFilm);
                statement.setInt(2, input.idStudio());
                statement.setTimestamp(3, java.sql.Timestamp.valueOf(
                        LocalDateTime.of(input.tanggalJadwal(), waktu)));
                statement.setBigDecimal(4, input.hargaTiket());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void tampilkanErrorDatabase(String pesan, SQLException exception) {
        logger.log(java.util.logging.Level.SEVERE, pesan, exception);
        JOptionPane.showMessageDialog(this, pesan + " Periksa koneksi dan relasi data.",
                "Kesalahan database", JOptionPane.ERROR_MESSAGE);
    }

    private void loadDataFilm() {
        DefaultTableModel model = modelFilmKosong();
        String sql = "SELECT f.id_film, f.judul, g.nama_genre, f.tahun, f.sutradara, f.durasi_menit, f.rating " +
                     "FROM film f JOIN genre g ON f.id_genre = g.id_genre ORDER BY f.id_film ASC";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id_film"), rs.getString("judul"), rs.getString("nama_genre"),
                    rs.getInt("tahun"), rs.getString("sutradara"), rs.getInt("durasi_menit"), rs.getDouble("rating")
                });
            }
            jTable1.setModel(model);
            updateFilmActionButtons(model.getRowCount());
        } catch (SQLException ex) {
            tampilkanErrorDatabase("Data film gagal dimuat.", ex);
        }
    }

    private void cariDataFilm() {
        String keyword = PlaceholderSupport.getText(jTextField1).trim();
        DefaultTableModel model = modelFilmKosong();
        String sql = "SELECT f.id_film, f.judul, g.nama_genre, f.tahun, f.sutradara, f.durasi_menit, f.rating " +
                     "FROM film f JOIN genre g ON f.id_genre = g.id_genre " +
                     "WHERE f.judul LIKE ? OR f.sutradara LIKE ? ORDER BY f.id_film ASC";
        try (Connection conn = Koneksi.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("id_film"), rs.getString("judul"), rs.getString("nama_genre"),
                        rs.getInt("tahun"), rs.getString("sutradara"), rs.getInt("durasi_menit"), rs.getDouble("rating")
                    });
                }
            }
            jTable1.setModel(model);
            updateFilmActionButtons(model.getRowCount());
        } catch (SQLException ex) {
            tampilkanErrorDatabase("Pencarian film gagal.", ex);
        }
    }

    private void clearFields() {
        jTextFieldID.setText("");
        PlaceholderSupport.reset(jTextFieldJUDULFILM);
        PlaceholderSupport.reset(jTextFieldTAHUN);
        PlaceholderSupport.reset(jTextFieldSUTRADARA);
        PlaceholderSupport.reset(jTextFieldDURASI);
        PlaceholderSupport.reset(jTextFieldRATING);
        resetJadwalFields();
        setJadwalAwal(null, null, null);
        if(jComboBoxGENRE.getItemCount() > 0) jComboBoxGENRE.setSelectedIndex(0);
    }

    private void tambahFilm() {
        FilmInput input = bacaInputFilm(false);
        if (input == null) {
            return;
        }
        String sql = "INSERT INTO film (judul, id_genre, tahun, sutradara, durasi_menit, rating) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = Koneksi.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, input.judul());
                stmt.setInt(2, input.idGenre());
                stmt.setInt(3, input.tahun());
                stmt.setString(4, input.sutradara());
                stmt.setInt(5, input.durasi());
                stmt.setBigDecimal(6, input.rating());
                stmt.executeUpdate();
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("ID film baru tidak berhasil dibuat.");
                    }
                    int idFilm = keys.getInt(1);
                    simpanJadwalFilm(conn, idFilm, input, true);
                }
                conn.commit();
                JOptionPane.showMessageDialog(this, "Data film dan jadwal berhasil ditambahkan!");
                clearFields();
                loadDataFilm();
            } catch (SQLException exception) {
                conn.rollback();
                throw exception;
            }
        } catch (SQLException ex) {
            String pesan = "23000".equals(ex.getSQLState())
                    ? "Film atau jadwal gagal disimpan karena data duplikat/relasi tidak valid."
                    : "Film dan jadwal gagal ditambahkan.";
            tampilkanErrorDatabase(pesan, ex);
        }
    }

    private void editFilm() {
        String idStr = jTextFieldID.getText().trim();
        if(idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data dari tabel terlebih dahulu!");
            return;
        }

        FilmInput input = bacaInputFilm(true);
        if (input == null) {
            return;
        }
        try {
            int id = Integer.parseInt(idStr);
            if (id <= 0) {
                throw new NumberFormatException();
            }
            String sql = "UPDATE film SET judul=?, id_genre=?, tahun=?, sutradara=?, durasi_menit=?, rating=? WHERE id_film=?";
            try (Connection conn = Koneksi.getConnection()) {
                conn.setAutoCommit(false);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, input.judul());
                    stmt.setInt(2, input.idGenre());
                    stmt.setInt(3, input.tahun());
                    stmt.setString(4, input.sutradara());
                    stmt.setInt(5, input.durasi());
                    stmt.setBigDecimal(6, input.rating());
                    stmt.setInt(7, id);
                    if (stmt.executeUpdate() == 0) {
                        conn.rollback();
                        JOptionPane.showMessageDialog(this, "Film tidak ditemukan atau sudah dihapus.");
                        return;
                    }
                    simpanJadwalFilm(conn, id, input, false);
                    conn.commit();
                    JOptionPane.showMessageDialog(this, "Data film dan jadwal berhasil diubah!");
                    clearFields();
                    loadDataFilm();
                } catch (SQLException exception) {
                    conn.rollback();
                    throw exception;
                }
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID film harus berupa bilangan bulat yang valid.");
        } catch (SQLException ex) {
            tampilkanErrorDatabase("Film gagal diubah.", ex);
        }
    }

    private void hapusFilm() {
        String idStr = jTextFieldID.getText().trim();
        if(idStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Pilih data dari tabel terlebih dahulu!");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Yakin ingin menghapus film ini?", "Konfirmasi", JOptionPane.YES_NO_OPTION);
        if(confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = Koneksi.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM film WHERE id_film=?")) {
                stmt.setInt(1, Integer.parseInt(idStr));
                if (stmt.executeUpdate() == 0) {
                    JOptionPane.showMessageDialog(this, "Film tidak ditemukan atau sudah dihapus.");
                    return;
                }
                JOptionPane.showMessageDialog(this, "Data film berhasil dihapus!");
                clearFields();
                loadDataFilm();
            } catch(SQLException ex) {
                String pesan = "23000".equals(ex.getSQLState())
                        ? "Film tidak dapat dihapus karena masih digunakan jadwal tayang."
                        : "Film gagal dihapus. Periksa koneksi database.";
                tampilkanErrorDatabase(pesan, ex);
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FormFilmAdmin().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JComboBox<String> jComboBoxGENRE;
    private javax.swing.JComboBox<String> jComboBoxstudio;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextFieldDURASI;
    private javax.swing.JTextField jTextFieldHarga;
    private javax.swing.JTextField jTextFieldID;
    private javax.swing.JTextField jTextFieldJUDULFILM;
    private javax.swing.JTextField jTextFieldRATING;
    private javax.swing.JTextField jTextFieldSUTRADARA;
    private javax.swing.JTextField jTextFieldTAHUN;
    private javax.swing.JTextField jTextFieldTAHUN1;
    private javax.swing.JTextField jTextFieldTanggal;
    // End of variables declaration//GEN-END:variables
}
