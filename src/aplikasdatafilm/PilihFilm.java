/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasdatafilm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author ASUS
 */
public class PilihFilm extends javax.swing.JFrame {

    private static final Color BURGUNDY = new Color(0x6D1F2B);
    private static final Color CREME = new Color(0xF5EDE5);
    private static final Color DEEP_CHARCOAL = new Color(0x171416);
    private static final String SEARCH_PLACEHOLDER = "Cari judul film....";

    private final long idPengguna;
    private final List<PemesananService.Jadwal> daftarJadwal = new ArrayList<>();
    private final JPanel panelGrid = new JPanel(new GridLayout(0, 5, 18, 18));
    private JScrollPane scrollFilm;
    private boolean searchPlaceholderShowing = true;

    private final DateTimeFormatter formatWaktu = DateTimeFormatter.ofPattern("HH.mm");
    private final DecimalFormat formatRupiah = new DecimalFormat("#,###",
            new DecimalFormatSymbols(Locale.forLanguageTag("id-ID")));

    /**
     * Creates new form Beli
     */
    public PilihFilm() {
        this(0);
    }

    public PilihFilm(long idPengguna) {
        this.idPengguna = idPengguna;
        initComponents();

        setupDynamicLayout();

        jTextField2.setText(SEARCH_PLACEHOLDER);
        jTextField2.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (searchPlaceholderShowing) {
                    jTextField2.setText("");
                    searchPlaceholderShowing = false;
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (jTextField2.getText().isBlank()) {
                    jTextField2.setText(SEARCH_PLACEHOLDER);
                    searchPlaceholderShowing = true;
                }
            }
        });
        jTextField2.addActionListener(event -> muatJadwal());

        jLabel23.setToolTipText("Keluar dari akun");
        jLabel23.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jLabel23.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent event) {
                dispose();
                new aplikasdatafilm.Login().setVisible(true);
            }
        });

        muatJadwal();
    }

    private void setupDynamicLayout() {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        jPanel6.setBackground(DEEP_CHARCOAL);
        jLabel22.setForeground(CREME);
        jLabel23.setForeground(CREME);
        jLabel24.setForeground(CREME);
        jTextField2.setBackground(CREME);
        jTextField2.setForeground(DEEP_CHARCOAL);
        jTextField2.setBorder(BorderFactory.createLineBorder(BURGUNDY));
        
        // Apply styling ke tombol jButton5 (tombol Cari)
        jButton5.setUI(new BasicButtonUI());
        jButton5.setBackground(BURGUNDY);
        jButton5.setForeground(CREME);
        jButton5.setOpaque(true);
        jButton5.setContentAreaFilled(true);
        jButton5.setBorderPainted(true);
        jButton5.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BURGUNDY.darker()),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        jButton5.setFocusPainted(false);
        jButton5.setRolloverEnabled(false);
        jButton5.setEnabled(true);
        jButton5.setCursor(new Cursor(Cursor.HAND_CURSOR));
        jButton5.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        ButtonSizingHelper.setSizeForButton(jButton5, ButtonSizingHelper.TYPE_ACTION);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);
        topBar.setBorder(BorderFactory.createEmptyBorder(12, 20, 0, 20));
        topBar.add(jLabel22, BorderLayout.WEST);
        topBar.add(jLabel23, BorderLayout.EAST);

        jLabel24.setAlignmentX(CENTER_ALIGNMENT);
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 9, 0));
        searchBar.setOpaque(false);
        searchBar.setBorder(BorderFactory.createEmptyBorder(0, 49, 0, 0));
        jTextField2.setPreferredSize(new Dimension(263, 32));
        searchBar.add(jTextField2);
        searchBar.add(jButton5);

        header.add(topBar);
        header.add(Box.createVerticalStrut(42));
        header.add(jLabel24);
        header.add(Box.createVerticalStrut(16));
        header.add(searchBar);
        header.add(Box.createVerticalStrut(20));

        panelGrid.setBackground(DEEP_CHARCOAL);
        panelGrid.setBorder(BorderFactory.createEmptyBorder(8, 49, 24, 33));
        scrollFilm = new JScrollPane(panelGrid);
        scrollFilm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollFilm.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollFilm.getVerticalScrollBar().setUnitIncrement(16);
        scrollFilm.getVerticalScrollBar().setBackground(DEEP_CHARCOAL);
        scrollFilm.getVerticalScrollBar().setForeground(BURGUNDY);
        scrollFilm.setBorder(BorderFactory.createEmptyBorder());
        scrollFilm.setBackground(DEEP_CHARCOAL);
        scrollFilm.getViewport().setBackground(DEEP_CHARCOAL);

        jPanel6.removeAll();
        jPanel6.setLayout(new BorderLayout());
        jPanel6.add(header, BorderLayout.NORTH);
        jPanel6.add(scrollFilm, BorderLayout.CENTER);

        getContentPane().removeAll();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().setBackground(DEEP_CHARCOAL);
        getContentPane().add(jPanel6, BorderLayout.CENTER);
        setSize(new Dimension(1280, 760));
        setLocationRelativeTo(null);
    }

    private JPanel buatCard(PemesananService.Jadwal jadwal, int posisi) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createLineBorder(BURGUNDY, 3));
        card.setBackground(BURGUNDY);
        card.setPreferredSize(new Dimension(200, 425));

        JLabel poster = new JLabel("POSTER FILM", SwingConstants.CENTER);
        poster.setOpaque(true);
        poster.setBackground(BURGUNDY);
        poster.setVerticalAlignment(SwingConstants.CENTER);
        poster.setFont(new Font("Segoe UI", Font.BOLD, 12));
        poster.setForeground(CREME);
        poster.setPreferredSize(new Dimension(194, 240));
        PosterFetcher.muat(poster, jadwal.urlPoster(), 194, 240);
        card.add(poster, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(CREME);
        bottomPanel.setOpaque(true);
        bottomPanel.setPreferredSize(new Dimension(194, 185));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));

        JLabel prefix = new JLabel("Judul : ");
        prefix.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        prefix.setForeground(DEEP_CHARCOAL);
        prefix.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel judul = new JLabel(formatJudulHtml(jadwal.judul()));
        judul.setFont(new Font("Segoe UI", Font.BOLD, 13));
        judul.setForeground(DEEP_CHARCOAL);
        judul.setToolTipText(jadwal.judul() + " (" + jadwal.genre() + ")");
        judul.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel waktu = new JLabel(formatWaktu.format(jadwal.mulaiTayang()));
        waktu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        waktu.setForeground(DEEP_CHARCOAL);
        waktu.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel durasi = new JLabel(formatDurasi(jadwal.durasiMenit()));
        durasi.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        durasi.setForeground(DEEP_CHARCOAL);
        durasi.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JLabel harga = new JLabel("Rp " + formatRupiah.format(jadwal.hargaTiket()));
        harga.setFont(new Font("Segoe UI", Font.BOLD, 12));
        harga.setForeground(BURGUNDY);
        harga.setAlignmentX(JPanel.LEFT_ALIGNMENT);

        JButton beli = new JButton("BELI");
        beli.setUI(new BasicButtonUI());
        beli.setFont(new Font("Segoe UI", Font.BOLD, 12));
        beli.setBackground(BURGUNDY);
        beli.setForeground(CREME);
        beli.setOpaque(true);
        beli.setContentAreaFilled(true);
        beli.setBorderPainted(true);
        beli.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BURGUNDY.darker()),
                BorderFactory.createEmptyBorder(4, 12, 4, 12)));
        beli.setCursor(new Cursor(Cursor.HAND_CURSOR));
        beli.setFocusPainted(false);
        beli.setRolloverEnabled(false);
        ButtonSizingHelper.setSizeCustom(beli, 85, 28, 8, 2);
        beli.setAlignmentX(JPanel.CENTER_ALIGNMENT);
        beli.addActionListener(event -> beliJadwal(posisi));

        bottomPanel.add(prefix);
        bottomPanel.add(Box.createVerticalStrut(2));
        bottomPanel.add(judul);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(waktu);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(durasi);
        bottomPanel.add(Box.createVerticalStrut(4));
        bottomPanel.add(harga);
        bottomPanel.add(Box.createVerticalGlue());
        bottomPanel.add(beli);
        card.add(bottomPanel, BorderLayout.SOUTH);
        return card;
    }

    private void muatJadwal() {
        String kataKunci = searchPlaceholderShowing ? "" : jTextField2.getText().trim();
        boolean isSearch = !kataKunci.isEmpty();

        try {
            panelGrid.removeAll();
            daftarJadwal.clear();
            List<PemesananService.Jadwal> hasilDb = PemesananService.cariJadwal(kataKunci);

            // Group by film id agar setiap card menampilkan film yang berbeda (distinct film)
            Set<Integer> seenFilmIds = new HashSet<>();
            for (PemesananService.Jadwal j : hasilDb) {
                if (seenFilmIds.add(j.idFilm())) {
                    daftarJadwal.add(j);
                }
            }

            for (int index = 0; index < daftarJadwal.size(); index++) {
                panelGrid.add(buatCard(daftarJadwal.get(index), index));
            }

            if (isSearch && daftarJadwal.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Film \"" + kataKunci + "\" tidak ditemukan.",
                        "Pencarian Film",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            panelGrid.revalidate();
            panelGrid.repaint();
            scrollFilm.getVerticalScrollBar().setValue(0);
        } catch (SQLException exception) {
            java.util.logging.Logger.getLogger(PilihFilm.class.getName()).log(
                    java.util.logging.Level.SEVERE, "Gagal memuat jadwal tayang", exception);
            JOptionPane.showMessageDialog(this,
                    "Jadwal film gagal dimuat. Periksa koneksi database.", "Kesalahan database",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void jButton5ActionPerformed(java.awt.event.ActionEvent event) {
        muatJadwal();
    }

    private String formatJudulHtml(String judul) {
        if (judul == null || judul.isEmpty()) {
            return "";
        }
        return "<html><div style='width:160px; font-family:Segoe UI; font-size:11pt; font-weight:bold;'>"
                + judul.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                + "</div></html>";
    }

    private String formatDurasi(int durasiMenit) {
        if (durasiMenit <= 0) {
            return "Durasi : -";
        }
        if (durasiMenit >= 60) {
            int jam = durasiMenit / 60;
            int sisaMenit = durasiMenit % 60;
            if (sisaMenit == 0) {
                return "Durasi : " + jam + " Jam";
            } else {
                return "Durasi : " + jam + " Jam " + sisaMenit + " Menit";
            }
        }
        return "Durasi : " + durasiMenit + " Menit";
    }

    private void beliJadwal(int posisi) {
        if (idPengguna <= 0) {
            JOptionPane.showMessageDialog(this, "Silakan login terlebih dahulu untuk membeli tiket.");
            return;
        }
        if (posisi < 0 || posisi >= daftarJadwal.size()) {
            return;
        }
        setVisible(false);
        new BeliTiket(idPengguna, daftarJadwal.get(posisi), this).setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel6 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton5 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        jLabel27 = new javax.swing.JLabel();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        jLabel30 = new javax.swing.JLabel();
        jButton6 = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        jLabel32 = new javax.swing.JLabel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jButton7 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jButton8 = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jLabel43 = new javax.swing.JLabel();
        jLabel44 = new javax.swing.JLabel();
        jLabel45 = new javax.swing.JLabel();
        jLabel46 = new javax.swing.JLabel();
        jLabel47 = new javax.swing.JLabel();
        jLabel48 = new javax.swing.JLabel();
        jButton9 = new javax.swing.JButton();
        jPanel8 = new javax.swing.JPanel();
        jLabel49 = new javax.swing.JLabel();
        jLabel50 = new javax.swing.JLabel();
        jLabel51 = new javax.swing.JLabel();
        jLabel52 = new javax.swing.JLabel();
        jLabel53 = new javax.swing.JLabel();
        jLabel54 = new javax.swing.JLabel();
        jButton10 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel22.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel22.setText("TIKET KU");

        jLabel23.setText("👤 User  [↪]");

        jLabel24.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel24.setText("PILIH FILM  ");

        jTextField2.setText("Cari judul film....");

        jButton5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton5.setText("Cari");
        jButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton5ActionPerformed(evt);
            }
        });

        jPanel3.setBackground(new java.awt.Color(255, 153, 51));

        jLabel25.setText("POSTER FILM");

        jLabel26.setText("Judul : ");

        jLabel27.setText("Romantic");

        jLabel28.setText("17.00");

        jLabel29.setText("Durasi : 3 Jam");

        jLabel30.setText("Rp 40.000");

        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton6.setText("BELI");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel25))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel26)
                            .addComponent(jLabel27)
                            .addComponent(jLabel29)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton6)
                                    .addComponent(jLabel30)))
                            .addComponent(jLabel28))))
                .addContainerGap(105, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
                .addComponent(jLabel26)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel27)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel28)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel29)
                .addGap(18, 18, 18)
                .addComponent(jLabel30)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6)
                .addContainerGap())
        );

        jPanel4.setBackground(new java.awt.Color(255, 153, 51));

        jLabel31.setText("POSTER FILM");

        jLabel32.setText("Judul : ");

        jLabel33.setText("Romantic");

        jLabel34.setText("17.00");

        jLabel35.setText("Durasi : 3 Jam");

        jLabel36.setText("Rp 40.000");

        jButton7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton7.setText("BELI");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel31))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel32)
                            .addComponent(jLabel33)
                            .addComponent(jLabel35)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton7)
                                    .addComponent(jLabel36)))
                            .addComponent(jLabel34))))
                .addContainerGap(110, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel31)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel32)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel34)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel35)
                .addGap(18, 18, 18)
                .addComponent(jLabel36)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton7)
                .addContainerGap())
        );

        jPanel5.setBackground(new java.awt.Color(255, 153, 51));

        jLabel37.setText("POSTER FILM");

        jLabel38.setText("Judul : ");

        jLabel39.setText("Romantic");

        jLabel40.setText("17.00");

        jLabel41.setText("Durasi : 3 Jam");

        jLabel42.setText("Rp 40.000");

        jButton8.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton8.setText("BELI");

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel37))
                    .addGroup(jPanel5Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel38)
                            .addComponent(jLabel39)
                            .addComponent(jLabel41)
                            .addGroup(jPanel5Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton8)
                                    .addComponent(jLabel42)))
                            .addComponent(jLabel40))))
                .addContainerGap(110, Short.MAX_VALUE))
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel37)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
                .addComponent(jLabel38)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel39)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel41)
                .addGap(18, 18, 18)
                .addComponent(jLabel42)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton8)
                .addContainerGap())
        );

        jPanel7.setBackground(new java.awt.Color(255, 153, 51));

        jLabel43.setText("POSTER FILM");

        jLabel44.setText("Judul : ");

        jLabel45.setText("Romantic");

        jLabel46.setText("17.00");

        jLabel47.setText("Durasi : 3 Jam");

        jLabel48.setText("Rp 40.000");

        jButton9.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton9.setText("BELI");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel43))
                    .addGroup(jPanel7Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel44)
                            .addComponent(jLabel45)
                            .addComponent(jLabel47)
                            .addGroup(jPanel7Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton9)
                                    .addComponent(jLabel48)))
                            .addComponent(jLabel46))))
                .addContainerGap(110, Short.MAX_VALUE))
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel43)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 147, Short.MAX_VALUE)
                .addComponent(jLabel44)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel45)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel46)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel47)
                .addGap(18, 18, 18)
                .addComponent(jLabel48)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton9)
                .addContainerGap())
        );

        jPanel8.setBackground(new java.awt.Color(255, 153, 51));

        jLabel49.setText("POSTER FILM");

        jLabel50.setText("Judul : ");

        jLabel51.setText("Romantic");

        jLabel52.setText("17.00");

        jLabel53.setText("Durasi : 3 Jam");

        jLabel54.setText("Rp 40.000");

        jButton10.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton10.setText("BELI");

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel49))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel50)
                            .addComponent(jLabel51)
                            .addComponent(jLabel53)
                            .addGroup(jPanel8Layout.createSequentialGroup()
                                .addGap(8, 8, 8)
                                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jButton10)
                                    .addComponent(jLabel54)))
                            .addComponent(jLabel52))))
                .addContainerGap(98, Short.MAX_VALUE))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel49)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel50)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel51)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel52)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel53)
                .addGap(18, 18, 18)
                .addComponent(jLabel54)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton10)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 263, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 84, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(33, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel6Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jLabel24)
                .addGap(488, 488, 488))
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addComponent(jLabel22)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel23)
                .addGap(22, 22, 22))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel22))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGap(20, 20, 20)
                        .addComponent(jLabel23)))
                .addGap(48, 48, 48)
                .addComponent(jLabel24)
                .addGap(19, 19, 19)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton5))
                .addGap(27, 27, 27)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jPanel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel7, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(PilihFilm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(PilihFilm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(PilihFilm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(PilihFilm.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new PilihFilm().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel47;
    private javax.swing.JLabel jLabel48;
    private javax.swing.JLabel jLabel49;
    private javax.swing.JLabel jLabel50;
    private javax.swing.JLabel jLabel51;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JLabel jLabel53;
    private javax.swing.JLabel jLabel54;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
