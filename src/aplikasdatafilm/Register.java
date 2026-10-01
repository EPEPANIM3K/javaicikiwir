/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package aplikasdatafilm;

/**
 *
 * @author ASUS
 */
public class Register extends javax.swing.JFrame {

    /**
     * Creates new form Login
     */
    public Register() {
        initComponents();
        PlaceholderSupport.install(jTextField1, "Masukkan email");
        PlaceholderSupport.install(jTextField2, "Masukkan nama");
        PlaceholderSupport.install(jPasswordField1, "Masukkan password");
        PlaceholderSupport.install(jPasswordField2, "Ulangi password");
        JBuutton.setText("REGISTER");
        JBuutton.addActionListener(e -> registerUser());
        jLabel5.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jLabel5.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                dispose();
                new Login().setVisible(true);
            }
        });
    }

    private void registerUser() {
        String nama = PlaceholderSupport.getText(jTextField2).trim();
        String email = PlaceholderSupport.getText(jTextField1).trim();
        char[] password = PlaceholderSupport.getPassword(jPasswordField1);
        char[] confirm = PlaceholderSupport.getPassword(jPasswordField2);
        try {
            if (nama.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Nama harus diisi");
                return;
            }
            if (nama.length() > 100) {
                javax.swing.JOptionPane.showMessageDialog(this, "Nama maksimal 100 karakter.");
                return;
            }
            if (email.isEmpty()) {
                javax.swing.JOptionPane.showMessageDialog(this, "Email harus diisi");
                return;
            }
            if (email.length() > 254 || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                javax.swing.JOptionPane.showMessageDialog(this, "Format email tidak valid.");
                return;
            }
            if (password.length == 0) {
                javax.swing.JOptionPane.showMessageDialog(this, "Password harus diisi");
                return;
            }
            if (!java.util.Arrays.equals(password, confirm)) {
                javax.swing.JOptionPane.showMessageDialog(this, "Konfirmasi password tidak cocok");
                return;
            }

            try (java.sql.Connection conn = Koneksi.Koneksi.getConnection()) {
                try (java.sql.PreparedStatement checkStmt = conn.prepareStatement("SELECT email FROM pengguna WHERE email = ?")) {
                checkStmt.setString(1, email);
                try (java.sql.ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        javax.swing.JOptionPane.showMessageDialog(this, "Email sudah terdaftar!");
                        return;
                    }
                }
            }
                String hash = hashPassword(password);
                try (java.sql.PreparedStatement insertStmt = conn.prepareStatement(
                        "INSERT INTO pengguna (nama, email, password_hash, role) VALUES (?, ?, ?, 'USER')")) {
                    insertStmt.setString(1, nama);
                    insertStmt.setString(2, email);
                    insertStmt.setString(3, hash);
                    insertStmt.executeUpdate();
                    javax.swing.JOptionPane.showMessageDialog(this, "Registrasi berhasil! Silakan login.");
                    dispose();
                    new Login().setVisible(true);
                }
            }
        } catch (java.security.NoSuchAlgorithmException exception) {
            javax.swing.JOptionPane.showMessageDialog(this, "Algoritma hash password tidak tersedia.");
        } catch (java.sql.SQLException exception) {
            java.util.logging.Logger.getLogger(Register.class.getName()).log(
                    java.util.logging.Level.SEVERE, "Registrasi gagal", exception);
            String pesan = "23000".equals(exception.getSQLState())
                    ? "Email sudah terdaftar!" : "Registrasi gagal. Periksa koneksi dan data yang dimasukkan.";
            javax.swing.JOptionPane.showMessageDialog(this, pesan, "Kesalahan registrasi",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
        } finally {
            java.util.Arrays.fill(password, '\0');
            java.util.Arrays.fill(confirm, '\0');
            PlaceholderSupport.reset(jPasswordField1);
            PlaceholderSupport.reset(jPasswordField2);
        }
    }

    private String hashPassword(char[] password) throws java.security.NoSuchAlgorithmException {
        java.nio.ByteBuffer encoded = java.nio.charset.StandardCharsets.UTF_8.encode(java.nio.CharBuffer.wrap(password));
        byte[] bytes = new byte[encoded.remaining()];
        encoded.get(bytes);
        try {
            byte[] hash = java.security.MessageDigest.getInstance("SHA-256").digest(bytes);
            return java.util.HexFormat.of().formatHex(hash);
        } finally {
            java.util.Arrays.fill(bytes, (byte) 0);
        }
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
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        JBuutton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText(" TIKET KU");

        jLabel2.setText("REGISTER USER");

        jLabel3.setText("Email :");

        jTextField1.setText("Masukkan email");
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel4.setText("Password :");

        jPasswordField1.setText("Masukkan password ");

        JBuutton.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        JBuutton.setText("LOGIN");

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Sudah punya akun? Login");

        jTextField2.setText("Masukkan nama ");
        jTextField2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField2ActionPerformed(evt);
            }
        });

        jLabel6.setText("Nama :");

        jLabel7.setText("Konfirmasi Password :");

        jPasswordField2.setText(" Ulangi password");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(108, 108, 108)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 158, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(152, 152, 152)
                        .addComponent(JBuutton))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(139, 139, 139)
                        .addComponent(jLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(147, 147, 147)
                        .addComponent(jLabel2))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel6)
                            .addComponent(jPasswordField1, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jTextField1)
                            .addComponent(jTextField2)
                            .addComponent(jLabel7)
                            .addComponent(jPasswordField2))))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(42, 42, 42)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(37, 37, 37)
                .addComponent(jLabel6)
                .addGap(18, 18, 18)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel7)
                .addGap(18, 18, 18)
                .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 36, Short.MAX_VALUE)
                .addComponent(JBuutton)
                .addGap(18, 18, 18)
                .addComponent(jLabel5)
                .addGap(71, 71, 71))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

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
            java.util.logging.Logger.getLogger(Register.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(Register.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(Register.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(Register.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new Register().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton JBuutton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables
}
