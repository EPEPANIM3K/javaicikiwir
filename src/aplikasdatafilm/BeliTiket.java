/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasdatafilm;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Form Pemesanan Tiket Bioskop.
 * Menangani pemilihan tanggal, jam tayang, studio, jumlah tiket (+/-),
 * dan pemilihan kursi interaktif dengan status real-time dari database.
 *
 * @author ASUS
 */
public class BeliTiket extends javax.swing.JFrame {

    private final long idPengguna;
    private PemesananService.Jadwal jadwalAwal;
    private PemesananService.Jadwal jadwalAktif;
    private List<PemesananService.Jadwal> daftarJadwalFilm = new ArrayList<>();

    // Status kursi untuk jadwal yang sedang aktif
    private List<PemesananService.Kursi> semuaKursi = new ArrayList<>();
    private final Set<String> kursiTerpilih = new LinkedHashSet<>();
    private int jumlahTiket = 1;

    // Mapping tombol kursi UI (A1 s.d. E4)
    private final Map<String, JButton> tombolKursiMap = new HashMap<>();

    // Flag untuk menghindari cascading event listener saat mengisi dropdown
    private boolean isUpdatingDropdown = false;

    // Warna status kursi
    private static final Color WARNA_NORMAL = new Color(245, 245, 245);
    private static final Color WARNA_TEKS_NORMAL = Color.BLACK;

    private static final Color WARNA_TERPILIH = new Color(45, 52, 58); // Gelap saat dipilih
    private static final Color WARNA_TEKS_TERPILIH = Color.WHITE;

    private static final Color WARNA_SUDAH_DIPESAN = new Color(220, 53, 69); // Merah saat sudah dibeli
    private static final Color WARNA_TEKS_SUDAH_DIPESAN = Color.WHITE;

    // Formatters
    private final DecimalFormat formatRupiah = new DecimalFormat("#,###",
            new DecimalFormatSymbols(Locale.forLanguageTag("id-ID")));
    private final DateTimeFormatter formatTanggal = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.forLanguageTag("id-ID"));
    private final DateTimeFormatter formatJam = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Creates new form BeliTiket
     */
    public BeliTiket() {
        this(0, null);
    }

    public BeliTiket(long idPengguna, PemesananService.Jadwal jadwal) {
        this.idPengguna = idPengguna;
        this.jadwalAwal = jadwal;
        this.jadwalAktif = jadwal;

        initComponents();
        setupComponents();
        UserMenuBar.buat(this, idPengguna);
    }

    private void setupComponents() {
        setTitle("Pemesanan Tiket Bioskop");
        pack();
        setSize(840, 640);
        setLocationRelativeTo(null);
        setResizable(false);

        // Map semua 20 tombol kursi di grid UI
        tombolKursiMap.put("A1", jButton10);
        tombolKursiMap.put("A2", jButton11);
        tombolKursiMap.put("A3", jButton8);
        tombolKursiMap.put("A4", jButton7);

        tombolKursiMap.put("B1", jButton13);
        tombolKursiMap.put("B2", jButton17);
        tombolKursiMap.put("B3", jButton18);
        tombolKursiMap.put("B4", jButton19);

        tombolKursiMap.put("C1", jButton6);
        tombolKursiMap.put("C2", jButton22);
        tombolKursiMap.put("C3", jButton25);
        tombolKursiMap.put("C4", jButton9);

        tombolKursiMap.put("D1", jButton12);
        tombolKursiMap.put("D2", jButton23);
        tombolKursiMap.put("D3", jButton26);
        tombolKursiMap.put("D4", jButton20);

        tombolKursiMap.put("E1", jButton16);
        tombolKursiMap.put("E2", jButton24);
        tombolKursiMap.put("E3", jButton27);
        tombolKursiMap.put("E4", jButton21);

        for (Map.Entry<String, JButton> entry : tombolKursiMap.entrySet()) {
            final String kode = entry.getKey();
            JButton btn = entry.getValue();
            btn.setText(kode);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setFocusPainted(false);
            btn.setOpaque(true);
            btn.setContentAreaFilled(true);
            // Bersihkan action listener bawaan form
            for (var al : btn.getActionListeners()) {
                btn.removeActionListener(al);
            }
            btn.addActionListener(e -> onKursiClicked(kode));
        }

        // Konfigurasi TextField
        jTextFieldJudulFilm.setEditable(false);
        jTextField1.setEditable(false); // Harga/Tiket
        jTextField2.setEditable(false); // Total
        jTextField3.setEditable(false); // Jumlah Tiket
        jTextField3.setHorizontalAlignment(JTextField.CENTER);
        jTextField3.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jTextField3.setText(String.valueOf(jumlahTiket));

        // Tombol + dan -
        jButton4.setText("+");
        jButton4.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jButton4.setCursor(new Cursor(Cursor.HAND_CURSOR));
        for (var al : jButton4.getActionListeners()) {
            jButton4.removeActionListener(al);
        }
        jButton4.addActionListener(e -> tambahTiket());

        jButton29.setText("-");
        jButton29.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jButton29.setCursor(new Cursor(Cursor.HAND_CURSOR));
        for (var al : jButton29.getActionListeners()) {
            jButton29.removeActionListener(al);
        }
        jButton29.addActionListener(e -> kurangTiket());

        // Tombol BELI TIKET & PILIH KURSI (keduanya memproses pemesanan)
        jButton2.setText("BELI TIKET");
        jButton2.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jButton2.setCursor(new Cursor(Cursor.HAND_CURSOR));
        for (var al : jButton2.getActionListeners()) {
            jButton2.removeActionListener(al);
        }
        jButton2.addActionListener(e -> prosesBeliTiket());

        jButton28.setText("CHECKOUT");
        jButton28.setFont(new Font("Segoe UI", Font.BOLD, 12));
        jButton28.setCursor(new Cursor(Cursor.HAND_CURSOR));
        for (var al : jButton28.getActionListeners()) {
            jButton28.removeActionListener(al);
        }
        jButton28.addActionListener(e -> prosesBeliTiket());

        // Jika jadwalAwal null (misal dipanggil langsung dari main), ambil jadwal pertama dari database
        if (jadwalAwal == null) {
            try {
                List<PemesananService.Jadwal> jList = PemesananService.cariJadwal("");
                if (!jList.isEmpty()) {
                    jadwalAwal = jList.get(0);
                    jadwalAktif = jadwalAwal;
                }
            } catch (SQLException ex) {
                // Abaikan
            }
        }

        if (jadwalAktif != null) {
            jTextFieldJudulFilm.setText(jadwalAktif.judul());
            muatDaftarJadwalDanDropdown();
        }
    }

    private void muatDaftarJadwalDanDropdown() {
        if (jadwalAktif == null) return;
        try {
            daftarJadwalFilm = PemesananService.jadwalUntukFilm(jadwalAktif.idFilm());
            if (daftarJadwalFilm.isEmpty()) {
                daftarJadwalFilm = new ArrayList<>(List.of(jadwalAktif));
            }
        } catch (SQLException ex) {
            daftarJadwalFilm = new ArrayList<>(List.of(jadwalAktif));
        }

        // Listener untuk dropdown
        for (var al : jComboBox1.getActionListeners()) {
            jComboBox1.removeActionListener(al);
        }
        for (var al : jComboBox2.getActionListeners()) {
            jComboBox2.removeActionListener(al);
        }
        for (var al : jComboBox3.getActionListeners()) {
            jComboBox3.removeActionListener(al);
        }

        jComboBox1.addActionListener(e -> {
            if (!isUpdatingDropdown) {
                updateJamDropdown();
            }
        });
        jComboBox2.addActionListener(e -> {
            if (!isUpdatingDropdown) {
                updateStudioDropdown();
            }
        });
        jComboBox3.addActionListener(e -> {
            if (!isUpdatingDropdown) {
                pilihJadwalSesuaiDropdown();
            }
        });

        updateTanggalDropdown();
    }

    private void updateTanggalDropdown() {
        isUpdatingDropdown = true;
        jComboBox1.removeAllItems();
        Set<LocalDate> dateSet = new LinkedHashSet<>();
        for (PemesananService.Jadwal j : daftarJadwalFilm) {
            dateSet.add(j.mulaiTayang().toLocalDate());
        }
        for (LocalDate d : dateSet) {
            jComboBox1.addItem(formatTanggal.format(d));
        }
        if (jadwalAktif != null) {
            String targetDate = formatTanggal.format(jadwalAktif.mulaiTayang());
            jComboBox1.setSelectedItem(targetDate);
        }
        isUpdatingDropdown = false;
        updateJamDropdown();
    }

    private void updateJamDropdown() {
        isUpdatingDropdown = true;
        jComboBox2.removeAllItems();
        String selectedDateStr = (String) jComboBox1.getSelectedItem();
        if (selectedDateStr != null) {
            Set<LocalTime> timeSet = new LinkedHashSet<>();
            for (PemesananService.Jadwal j : daftarJadwalFilm) {
                if (formatTanggal.format(j.mulaiTayang()).equals(selectedDateStr)) {
                    timeSet.add(j.mulaiTayang().toLocalTime());
                }
            }
            for (LocalTime t : timeSet) {
                jComboBox2.addItem(formatJam.format(t));
            }
            if (jadwalAktif != null) {
                String targetTime = formatJam.format(jadwalAktif.mulaiTayang());
                jComboBox2.setSelectedItem(targetTime);
            }
        }
        isUpdatingDropdown = false;
        updateStudioDropdown();
    }

    private void updateStudioDropdown() {
        isUpdatingDropdown = true;
        jComboBox3.removeAllItems();
        String selectedDateStr = (String) jComboBox1.getSelectedItem();
        String selectedTimeStr = (String) jComboBox2.getSelectedItem();
        if (selectedDateStr != null && selectedTimeStr != null) {
            for (PemesananService.Jadwal j : daftarJadwalFilm) {
                if (formatTanggal.format(j.mulaiTayang()).equals(selectedDateStr)
                        && formatJam.format(j.mulaiTayang()).equals(selectedTimeStr)) {
                    jComboBox3.addItem(j.namaStudio());
                }
            }
            if (jadwalAktif != null) {
                jComboBox3.setSelectedItem(jadwalAktif.namaStudio());
            }
        }
        isUpdatingDropdown = false;
        pilihJadwalSesuaiDropdown();
    }

    private void pilihJadwalSesuaiDropdown() {
        if (isUpdatingDropdown) return;
        String selectedDateStr = (String) jComboBox1.getSelectedItem();
        String selectedTimeStr = (String) jComboBox2.getSelectedItem();
        String selectedStudio = (String) jComboBox3.getSelectedItem();
        if (selectedDateStr == null || selectedTimeStr == null || selectedStudio == null) return;

        for (PemesananService.Jadwal j : daftarJadwalFilm) {
            if (formatTanggal.format(j.mulaiTayang()).equals(selectedDateStr)
                    && formatJam.format(j.mulaiTayang()).equals(selectedTimeStr)
                    && j.namaStudio().equals(selectedStudio)) {
                jadwalAktif = j;
                break;
            }
        }
        muatKursiStudio();
    }

    private void muatKursiStudio() {
        if (jadwalAktif == null) return;
        try {
            semuaKursi = PemesananService.kursiUntukJadwal(jadwalAktif.idJadwal());
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Gagal memuat status kursi: " + ex.getMessage(),
                    "Kesalahan Database", JOptionPane.ERROR_MESSAGE);
            semuaKursi = new ArrayList<>();
        }
        kursiTerpilih.clear();
        updateHargaDanTotal();
        updateTampilanKursi();
    }

    private int hitungKursiTersedia() {
        int count = 0;
        for (Map.Entry<String, JButton> entry : tombolKursiMap.entrySet()) {
            if (!isKursiSudahDipesan(entry.getKey())) {
                count++;
            }
        }
        return Math.max(count, 1);
    }

    private boolean isKursiSudahDipesan(String kode) {
        for (PemesananService.Kursi k : semuaKursi) {
            if (k.kodeKursi().equalsIgnoreCase(kode)) {
                return k.sudahDipesan();
            }
        }
        return false;
    }

    private void tambahTiket() {
        int maxTersedia = hitungKursiTersedia();
        if (jumlahTiket < maxTersedia) {
            jumlahTiket++;
            jTextField3.setText(String.valueOf(jumlahTiket));
            updateHargaDanTotal();
            updateTampilanKursi();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Jumlah tiket maksimal adalah " + maxTersedia + " (sesuai kursi yang tersedia di studio ini).",
                    "Batas Maksimal Kursi",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void kurangTiket() {
        if (jumlahTiket > 1) {
            jumlahTiket--;
            // Jika kursi yang sudah dipilih lebih banyak dari kuota baru, lepas kursi yang terakhir dipilih
            while (kursiTerpilih.size() > jumlahTiket) {
                String last = null;
                for (String k : kursiTerpilih) {
                    last = k;
                }
                if (last != null) {
                    kursiTerpilih.remove(last);
                }
            }
            jTextField3.setText(String.valueOf(jumlahTiket));
            updateHargaDanTotal();
            updateTampilanKursi();
        }
    }

    private void onKursiClicked(String kode) {
        if (kursiTerpilih.contains(kode)) {
            // User menekan kembali kursi yang sudah dipilih -> hilangkan pemilihannya (unselect)
            kursiTerpilih.remove(kode);
        } else {
            // User memilih kursi baru jika kuota belum penuh
            if (kursiTerpilih.size() < jumlahTiket) {
                kursiTerpilih.add(kode);
            } else {
                return;
            }
        }
        updateTampilanKursi();
    }

    private void updateTampilanKursi() {
        boolean kuotaPenuh = kursiTerpilih.size() >= jumlahTiket;

        for (Map.Entry<String, JButton> entry : tombolKursiMap.entrySet()) {
            String kode = entry.getKey();
            JButton btn = entry.getValue();
            boolean sudahDipesan = isKursiSudahDipesan(kode);

            if (sudahDipesan) {
                // Kursi yang sudah dibeli / dipesan di studio & jadwal ini
                btn.setEnabled(false);
                btn.setBackground(WARNA_SUDAH_DIPESAN);
                btn.setForeground(WARNA_TEKS_SUDAH_DIPESAN);
                btn.setToolTipText("Kursi " + kode + " sudah dipesan");
                btn.setCursor(Cursor.getDefaultCursor());
            } else if (kursiTerpilih.contains(kode)) {
                // Kursi yang dipilih oleh user: MENJADI GELAP
                btn.setEnabled(true);
                btn.setBackground(WARNA_TERPILIH);
                btn.setForeground(WARNA_TEKS_TERPILIH);
                btn.setToolTipText("Kursi " + kode + " terpilih (klik lagi untuk membatalkan)");
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            } else {
                // Kursi tersedia (belum dipilih)
                if (kuotaPenuh) {
                    // Pemesanan tiket sudah sesuai dengan jumlah kursinya -> kursi lain cannot click
                    btn.setEnabled(false);
                    btn.setBackground(WARNA_NORMAL);
                    btn.setForeground(Color.LIGHT_GRAY);
                    btn.setToolTipText("Pemesanan tiket sudah sesuai (" + jumlahTiket + " tiket). Batalkan kursi lain untuk mengubah.");
                    btn.setCursor(Cursor.getDefaultCursor());
                } else {
                    // Masih bisa memilih kursi
                    btn.setEnabled(true);
                    btn.setBackground(WARNA_NORMAL);
                    btn.setForeground(WARNA_TEKS_NORMAL);
                    btn.setToolTipText("Pilih kursi " + kode);
                    btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
            }
        }
    }

    private void updateHargaDanTotal() {
        if (jadwalAktif == null) return;
        BigDecimal harga = jadwalAktif.hargaTiket();
        BigDecimal total = harga.multiply(BigDecimal.valueOf(jumlahTiket));
        jTextField1.setText("Rp " + formatRupiah.format(harga));
        jTextField2.setText("Rp " + formatRupiah.format(total));
    }

    private void prosesBeliTiket() {
        if (idPengguna <= 0) {
            JOptionPane.showMessageDialog(this, "Silakan login terlebih dahulu untuk memesan tiket.");
            return;
        }
        if (jadwalAktif == null) {
            JOptionPane.showMessageDialog(this, "Pilih jadwal film terlebih dahulu.");
            return;
        }
        if (kursiTerpilih.size() < jumlahTiket) {
            JOptionPane.showMessageDialog(this,
                    "Silakan pilih " + jumlahTiket + " kursi terlebih dahulu (baru memilih "
                    + kursiTerpilih.size() + " kursi).",
                    "Pilihan Kursi Belum Lengkap",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Ambil objek Kursi dari database yang sesuai dengan kode yang dipilih
        List<PemesananService.Kursi> listKursiDipilih = new ArrayList<>();
        for (String kode : kursiTerpilih) {
            for (PemesananService.Kursi k : semuaKursi) {
                if (k.kodeKursi().equalsIgnoreCase(kode)) {
                    listKursiDipilih.add(k);
                    break;
                }
            }
        }

        if (listKursiDipilih.size() != jumlahTiket) {
            JOptionPane.showMessageDialog(this, "Terjadi kesalahan saat memproses data kursi.");
            return;
        }

        // Buka form CheckOut
        dispose();
        new CheckOut(idPengguna, jadwalAktif, listKursiDipilih).setVisible(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton47 = new javax.swing.JButton();
        jButton34 = new javax.swing.JButton();
        jButton42 = new javax.swing.JButton();
        jButton46 = new javax.swing.JButton();
        jButton30 = new javax.swing.JButton();
        jButton37 = new javax.swing.JButton();
        jButton33 = new javax.swing.JButton();
        jButton48 = new javax.swing.JButton();
        jButton55 = new javax.swing.JButton();
        jButton39 = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jButton31 = new javax.swing.JButton();
        jButton43 = new javax.swing.JButton();
        jButton50 = new javax.swing.JButton();
        jButton53 = new javax.swing.JButton();
        jButton40 = new javax.swing.JButton();
        jButton38 = new javax.swing.JButton();
        jButton35 = new javax.swing.JButton();
        jButton32 = new javax.swing.JButton();
        jButton51 = new javax.swing.JButton();
        jButton44 = new javax.swing.JButton();
        jButton54 = new javax.swing.JButton();
        jButton41 = new javax.swing.JButton();
        jButton45 = new javax.swing.JButton();
        jButton49 = new javax.swing.JButton();
        jButton52 = new javax.swing.JButton();
        jLabel8 = new javax.swing.JLabel();
        jButton36 = new javax.swing.JButton();
        jButton58 = new javax.swing.JButton();
        jButton61 = new javax.swing.JButton();
        jLabel14 = new javax.swing.JLabel();
        jButton77 = new javax.swing.JButton();
        jButton78 = new javax.swing.JButton();
        jButton70 = new javax.swing.JButton();
        jButton66 = new javax.swing.JButton();
        jButton60 = new javax.swing.JButton();
        jButton79 = new javax.swing.JButton();
        jButton71 = new javax.swing.JButton();
        jButton72 = new javax.swing.JButton();
        jButton59 = new javax.swing.JButton();
        jButton69 = new javax.swing.JButton();
        jButton80 = new javax.swing.JButton();
        jButton63 = new javax.swing.JButton();
        jButton81 = new javax.swing.JButton();
        jButton62 = new javax.swing.JButton();
        jButton64 = new javax.swing.JButton();
        jButton75 = new javax.swing.JButton();
        jButton67 = new javax.swing.JButton();
        jButton74 = new javax.swing.JButton();
        jButton57 = new javax.swing.JButton();
        jButton65 = new javax.swing.JButton();
        jButton73 = new javax.swing.JButton();
        jButton56 = new javax.swing.JButton();
        jButton68 = new javax.swing.JButton();
        jButton76 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        jLabel13 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton29 = new javax.swing.JButton();
        jTextField3 = new javax.swing.JTextField();
        jButton19 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        jButton24 = new javax.swing.JButton();
        jButton21 = new javax.swing.JButton();
        jButton12 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton28 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton20 = new javax.swing.JButton();
        jButton17 = new javax.swing.JButton();
        jButton25 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jButton26 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton23 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jTextFieldJudulFilm = new javax.swing.JTextField();

        jButton47.setText("A2");

        jButton34.setText("E2");

        jButton42.setText("A4");

        jButton46.setText("A1");

        jButton30.setText("D4");

        jButton37.setText("A5");

        jButton33.setText("D2");

        jButton48.setText("D1");

        jButton55.setText("B4");

        jButton39.setText("B5");

        jLabel12.setText("Pilih Kursi : ");

        jButton31.setText("E4");

        jButton43.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton43.setText("PILIH");

        jButton50.setText("D5");

        jButton53.setText("B2");

        jButton40.setText("E3");

        jButton38.setText("D3");

        jButton35.setText("C5");

        jButton32.setText("C2");

        jButton51.setText("E5");

        jButton44.setText("A3");

        jButton54.setText("B3");

        jButton41.setText("C1");

        jButton45.setText("C4");

        jButton49.setText("B1");

        jButton52.setText("E1");

        jLabel8.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel8.setText("LAYAR BIOSKOP");

        jButton36.setText("C3");

        jButton58.setText("C2");

        jButton61.setText("C3");

        jLabel14.setText("Pilih Kursi : ");

        jButton77.setText("E5");

        jButton78.setText("E1");

        jButton70.setText("A3");

        jButton66.setText("A5");

        jButton60.setText("E2");

        jButton79.setText("B2");

        jButton71.setText("C4");

        jButton72.setText("A1");

        jButton59.setText("D2");

        jButton69.setText("A4");

        jButton80.setText("B3");

        jButton63.setText("E3");

        jButton81.setText("B4");

        jButton62.setText("D3");

        jButton64.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton64.setText("PILIH");

        jButton75.setText("B1");

        jButton67.setText("B5");

        jButton74.setText("D1");

        jButton57.setText("E4");

        jButton65.setText("C5");

        jButton73.setText("A2");

        jButton56.setText("D4");

        jButton68.setText("C1");

        jButton76.setText("D5");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("PILIH FILM  ");

        jLabel2.setText("🎬 Judul Film  : ");

        jLabel5.setText("Tanggal :");

        jLabel6.setText("Jam Tayang :");

        jLabel7.setText("Studio :");

        jLabel3.setText("Jumlah Tiket :");

        jLabel4.setFont(new java.awt.Font("Segoe UI", 3, 14)); // NOI18N
        jLabel4.setText("LAYAR BIOSKOP");

        jLabel11.setText("Harga/Tiket :");

        jLabel13.setText("Total :");

        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton2.setText("BELI TIKET");

        jButton4.setText("+");

        jButton29.setText("-");

        jTextField3.setText("1");

        jButton19.setText("B4");

        jButton27.setText("E3");

        jButton10.setText("A1");

        jButton18.setText("B3");

        jButton24.setText("E2");

        jButton21.setText("E4");

        jButton12.setText("D1");

        jButton11.setText("A2");

        jButton8.setText("A3");

        jButton13.setText("B1");

        jButton28.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jButton28.setText("CHECKOUT");

        jButton6.setText("C1");

        jButton20.setText("D4");

        jButton17.setText("B2");

        jButton25.setText("C3");

        jLabel10.setText("Pilih Kursi : ");

        jButton26.setText("D3");

        jButton7.setText("A4");

        jButton16.setText("E1");

        jButton9.setText("C4");

        jButton23.setText("D2");

        jButton22.setText("C2");

        jTextFieldJudulFilm.setText("film");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel11)
                                    .addComponent(jLabel13))
                                .addGap(18, 18, 18)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                    .addComponent(jTextField2)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5)
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel7)
                                    .addComponent(jLabel3))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jTextFieldJudulFilm, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton29, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                        .addGap(60, 60, 60)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jLabel10)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel4)
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                .addGap(18, 18, 18)
                                .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 52, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(60, 60, 60)
                                .addComponent(jButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(252, 252, 252)
                        .addComponent(jLabel1)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addGap(30, 30, 30)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextFieldJudulFilm, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jButton4)
                            .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton29))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel11))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel13))
                        .addGap(25, 25, 25)
                        .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton10, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton11, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton19, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton18, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton17, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton13, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton25, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton12, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton23, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton26, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton24, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton27, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 32, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(25, 25, 25)
                        .addComponent(jButton28, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
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
            java.util.logging.Logger.getLogger(BeliTiket.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(BeliTiket.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(BeliTiket.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(BeliTiket.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new BeliTiket().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton28;
    private javax.swing.JButton jButton29;
    private javax.swing.JButton jButton30;
    private javax.swing.JButton jButton31;
    private javax.swing.JButton jButton32;
    private javax.swing.JButton jButton33;
    private javax.swing.JButton jButton34;
    private javax.swing.JButton jButton35;
    private javax.swing.JButton jButton36;
    private javax.swing.JButton jButton37;
    private javax.swing.JButton jButton38;
    private javax.swing.JButton jButton39;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton40;
    private javax.swing.JButton jButton41;
    private javax.swing.JButton jButton42;
    private javax.swing.JButton jButton43;
    private javax.swing.JButton jButton44;
    private javax.swing.JButton jButton45;
    private javax.swing.JButton jButton46;
    private javax.swing.JButton jButton47;
    private javax.swing.JButton jButton48;
    private javax.swing.JButton jButton49;
    private javax.swing.JButton jButton50;
    private javax.swing.JButton jButton51;
    private javax.swing.JButton jButton52;
    private javax.swing.JButton jButton53;
    private javax.swing.JButton jButton54;
    private javax.swing.JButton jButton55;
    private javax.swing.JButton jButton56;
    private javax.swing.JButton jButton57;
    private javax.swing.JButton jButton58;
    private javax.swing.JButton jButton59;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton60;
    private javax.swing.JButton jButton61;
    private javax.swing.JButton jButton62;
    private javax.swing.JButton jButton63;
    private javax.swing.JButton jButton64;
    private javax.swing.JButton jButton65;
    private javax.swing.JButton jButton66;
    private javax.swing.JButton jButton67;
    private javax.swing.JButton jButton68;
    private javax.swing.JButton jButton69;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton70;
    private javax.swing.JButton jButton71;
    private javax.swing.JButton jButton72;
    private javax.swing.JButton jButton73;
    private javax.swing.JButton jButton74;
    private javax.swing.JButton jButton75;
    private javax.swing.JButton jButton76;
    private javax.swing.JButton jButton77;
    private javax.swing.JButton jButton78;
    private javax.swing.JButton jButton79;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton80;
    private javax.swing.JButton jButton81;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
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
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextFieldJudulFilm;
    // End of variables declaration//GEN-END:variables
}
