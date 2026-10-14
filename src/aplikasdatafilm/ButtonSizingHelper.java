package aplikasdatafilm;

import javax.swing.JButton;
import java.awt.Dimension;
import java.awt.Insets;

/**
 * Helper class untuk standardisasi ukuran, padding, dan styling tombol di seluruh aplikasi.
 * Memastikan semua tombol memiliki ukuran yang proporsional dan padding yang konsisten.
 */
public class ButtonSizingHelper {

    // Tipe-tipe tombol
    public static final int TYPE_SEAT = 1;        // Tombol kursi (45x42 px)
    public static final int TYPE_SMALL = 2;       // Tombol kecil seperti +/- (38x32 px)
    public static final int TYPE_ACTION = 3;      // Tombol action standar (90-110 px, 34 px height)
    public static final int TYPE_PRIMARY = 4;     // Tombol utama seperti BELI TIKET (120-140 px, 36 px height)
    public static final int TYPE_ICON = 5;        // Tombol dengan icon saja (32x32 px)

    /**
     * Mengatur ukuran standar tombol berdasarkan tipe.
     * Cocok untuk tombol dengan teks pendek/menengah.
     */
    public static void setSizeForButton(JButton button, int type) {
        switch (type) {
            case TYPE_SEAT:
                // Tombol kursi - ukuran tetap untuk grid
                button.setPreferredSize(new Dimension(45, 42));
                button.setMinimumSize(new Dimension(45, 42));
                button.setMaximumSize(new Dimension(45, 42));
                button.setMargin(new Insets(2, 4, 2, 4));
                break;

            case TYPE_SMALL:
                // Tombol +/- dan tombol kecil lainnya
                button.setPreferredSize(new Dimension(38, 32));
                button.setMinimumSize(new Dimension(38, 32));
                button.setMaximumSize(new Dimension(38, 32));
                button.setMargin(new Insets(2, 2, 2, 2));
                break;

            case TYPE_ACTION:
                // Tombol action standar (CARI, EDIT, HAPUS, KEMBALI, dll)
                button.setPreferredSize(new Dimension(100, 34));
                button.setMinimumSize(new Dimension(85, 34));
                button.setMaximumSize(new Dimension(140, 34));
                button.setMargin(new Insets(4, 12, 4, 12));
                break;

            case TYPE_PRIMARY:
                // Tombol utama (BELI TIKET, LOGIN, BAYAR SEKARANG, REGISTER, TAMBAH FILM)
                button.setPreferredSize(new Dimension(130, 36));
                button.setMinimumSize(new Dimension(120, 36));
                button.setMaximumSize(new Dimension(160, 36));
                button.setMargin(new Insets(6, 16, 6, 16));
                break;

            case TYPE_ICON:
                // Tombol icon murni (hanya icon, minimal text)
                button.setPreferredSize(new Dimension(32, 32));
                button.setMinimumSize(new Dimension(32, 32));
                button.setMaximumSize(new Dimension(32, 32));
                button.setMargin(new Insets(2, 2, 2, 2));
                break;
        }
    }

    /**
     * Mengatur ukuran custom untuk tombol dengan teks panjang.
     * Gunakan ini jika teks tombol lebih panjang dari biasanya.
     */
    public static void setSizeCustom(JButton button, int preferredWidth, int preferredHeight,
                                      int marginHorizontal, int marginVertical) {
        button.setPreferredSize(new Dimension(preferredWidth, preferredHeight));
        button.setMinimumSize(new Dimension(preferredWidth - 10, preferredHeight));
        button.setMaximumSize(new Dimension(preferredWidth + 20, preferredHeight));
        button.setMargin(new Insets(marginVertical, marginHorizontal, marginVertical, marginHorizontal));
    }

    /**
     * Mengatur ukuran untuk tombol-tombol kecil yang berdampingan (seperti di form).
     * Untuk tombol dengan lebar yang lebih fleksibel namun tinggi standar.
     */
    public static void setSizeFlexible(JButton button, int minWidth, int preferredWidth, int maxWidth) {
        button.setPreferredSize(new Dimension(preferredWidth, 34));
        button.setMinimumSize(new Dimension(minWidth, 34));
        button.setMaximumSize(new Dimension(maxWidth, 34));
        button.setMargin(new Insets(4, 10, 4, 10));
    }

    /**
     * Menyamakan ukuran beberapa tombol (cocok untuk button group).
     */
    public static void setUniformSize(JButton[] buttons, int type) {
        for (JButton button : buttons) {
            setSizeForButton(button, type);
        }
    }

    /**
     * Menyamakan ukuran beberapa tombol dengan ukuran custom.
     */
    public static void setUniformSizeCustom(JButton[] buttons, int width, int height,
                                             int marginH, int marginV) {
        for (JButton button : buttons) {
            setSizeCustom(button, width, height, marginH, marginV);
        }
    }
}
