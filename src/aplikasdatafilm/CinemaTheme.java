package aplikasdatafilm;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

/**
 * Modern Cinema Theme & Design System.
 * Standardizes resolution to 1280x720, typography hierarchy, colors,
 * buttons, inputs, cards, tables, and Indonesian currency/date formatting.
 */
public final class CinemaTheme {

    public static final int WINDOW_WIDTH = 1280;
    public static final int WINDOW_HEIGHT = 720;
    public static final Dimension STANDARD_SIZE = new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);

    // Color Palette
    public static final Color BG_DARK = new Color(0x13, 0x11, 0x14);         // #131114
    public static final Color PANEL_BG = new Color(0x1A, 0x16, 0x1A);        // #1A161A
    public static final Color CARD_BG = new Color(0x23, 0x1E, 0x23);         // #231E23
    public static final Color CARD_HOVER = new Color(0x2B, 0x24, 0x2A);      // #2B242A
    public static final Color CARD_BORDER = new Color(0x3B, 0x2A, 0x33);     // #3B2A33
    public static final Color INPUT_BG = new Color(0xF5, 0xED, 0xE5);        // Creme for text inputs
    public static final Color INPUT_TEXT = new Color(0x17, 0x14, 0x16);      // Dark text for creme inputs

    public static final Color BURGUNDY = new Color(0x7A, 0x1F, 0x32);        // Primary #7A1F32
    public static final Color BURGUNDY_HOVER = new Color(0x94, 0x28, 0x3F);  // Lighter
    public static final Color BURGUNDY_DARK = new Color(0x56, 0x15, 0x23);   // Darker
    public static final Color CREME = new Color(0xF5, 0xED, 0xE5);           // #F5EDE5
    public static final Color TEXT_MUTED = new Color(0xA8, 0x9D, 0xA2);      // Secondary text
    public static final Color TEXT_HINT = new Color(0x73, 0x67, 0x6D);       // Subtle text
    public static final Color ACCENT_GOLD = new Color(0xE5, 0xB8, 0x5D);     // Rating / Highlights
    public static final Color SUCCESS = new Color(0x2E, 0x7D, 0x32);         // Green
    public static final Color DANGER = new Color(0xC6, 0x28, 0x28);          // Red
    public static final Color DANGER_HOVER = new Color(0xE5, 0x39, 0x35);

    // Typography
    public static final Font FONT_TITLE_LARGE = new Font("Segoe UI", Font.BOLD, 26);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SECTION = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BIG_NUMBER = new Font("Segoe UI", Font.BOLD, 28);
    public static final Font FONT_PRICE_HUGE = new Font("Segoe UI", Font.BOLD, 32);

    // Formatters
    public static final Locale LOCALE_ID = new Locale("id", "ID");
    public static final DateTimeFormatter DATE_FORMAT_ID = DateTimeFormatter.ofPattern("dd MMMM yyyy", LOCALE_ID);
    public static final DateTimeFormatter DATE_TIME_FORMAT_ID = DateTimeFormatter.ofPattern("dd MMMM yyyy HH:mm", LOCALE_ID);
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter TIME_FORMAT_DOT = DateTimeFormatter.ofPattern("HH.mm");

    private static final DecimalFormat RUPIAH_FORMAT = new DecimalFormat("#,###",
            new DecimalFormatSymbols(LOCALE_ID));

    private CinemaTheme() {}

    public static String formatRupiah(BigDecimal amount) {
        if (amount == null) return "Rp 0";
        return "Rp " + RUPIAH_FORMAT.format(amount);
    }

    public static String formatRupiah(double amount) {
        return "Rp " + RUPIAH_FORMAT.format(amount);
    }

    public static String formatTanggal(LocalDate date) {
        if (date == null) return "-";
        return DATE_FORMAT_ID.format(date);
    }

    public static String formatTanggalWaktu(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return DATE_TIME_FORMAT_ID.format(dateTime);
    }

    public static String formatWaktu(LocalDateTime dateTime) {
        if (dateTime == null) return "-";
        return TIME_FORMAT.format(dateTime);
    }

    /**
     * Standardizes any JFrame to 1280x720, centered, dark theme background.
     */
    public static void standardizeFrame(JFrame frame, String title) {
        frame.setTitle(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(STANDARD_SIZE);
        frame.setPreferredSize(STANDARD_SIZE);
        frame.setMinimumSize(new Dimension(1024, 640));
        frame.getContentPane().setBackground(BG_DARK);
        frame.setLocationRelativeTo(null);
    }

    /**
     * Create a standard cinema operational top navigation bar.
     */
    public static JPanel createStaffHeader(JFrame frame, String pageTitle, String staffRole, Runnable onLogout) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, CARD_BORDER),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));

        // Brand & Current Page
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        left.setOpaque(false);

        JLabel brandIcon = new JLabel("🎬");
        brandIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));

        JLabel brandName = new JLabel("TIKET KU");
        brandName.setFont(FONT_TITLE);
        brandName.setForeground(CREME);

        JLabel divider = new JLabel("—");
        divider.setFont(FONT_SUBTITLE);
        divider.setForeground(TEXT_HINT);

        JLabel pageLabel = new JLabel(pageTitle);
        pageLabel.setFont(FONT_SUBTITLE);
        pageLabel.setForeground(ACCENT_GOLD);

        left.add(brandIcon);
        left.add(brandName);
        left.add(divider);
        left.add(pageLabel);

        // Staff Info & Logout
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        right.setOpaque(false);

        JLabel staffBadge = new JLabel(" " + staffRole + " ");
        staffBadge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        staffBadge.setForeground(CREME);
        staffBadge.setBackground(BURGUNDY);
        staffBadge.setOpaque(true);
        staffBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JButton btnLogout = createSmallButton("Keluar", DANGER, DANGER_HOVER);
        btnLogout.addActionListener(e -> {
            if (onLogout != null) {
                onLogout.run();
            } else {
                frame.dispose();
                new Login().setVisible(true);
            }
        });

        right.add(staffBadge);
        right.add(btnLogout);

        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    /**
     * Creates a styled card container panel with rounded borders.
     */
    public static JPanel createCard() {
        JPanel card = new JPanel();
        card.setBackground(CARD_BG);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        return card;
    }

    /**
     * Primary action button (Burgundy).
     */
    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, BURGUNDY, BURGUNDY_HOVER, CREME, FONT_BODY_BOLD, new Insets(10, 20, 10, 20));
        return btn;
    }

    /**
     * Secondary / Outline button.
     */
    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, CARD_BG, CARD_HOVER, CREME, FONT_BODY_BOLD, new Insets(10, 18, 10, 18));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CARD_BORDER, 1),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)
        ));
        return btn;
    }

    /**
     * Success button (Green).
     */
    public static JButton createSuccessButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, SUCCESS, new Color(0x38, 0x8E, 0x3C), CREME, FONT_BODY_BOLD, new Insets(10, 20, 10, 20));
        return btn;
    }

    /**
     * Danger button (Red).
     */
    public static JButton createDangerButton(String text) {
        JButton btn = new JButton(text);
        styleButton(btn, DANGER, DANGER_HOVER, CREME, FONT_BODY_BOLD, new Insets(10, 18, 10, 18));
        return btn;
    }

    public static JButton createSmallButton(String text, Color bg, Color hover) {
        JButton btn = new JButton(text);
        styleButton(btn, bg, hover, CREME, FONT_SMALL, new Insets(6, 12, 6, 12));
        return btn;
    }

    public static void styleButton(JButton btn, Color bg, Color hover, Color fg, Font font, Insets padding) {
        btn.setUI(new BasicButtonUI());
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(font);
        btn.setFocusPainted(false);
        btn.setBorderPainted(true);
        btn.setContentAreaFilled(true);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.darker(), 1),
                BorderFactory.createEmptyBorder(padding.top, padding.left, padding.bottom, padding.right)
        ));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(hover);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (btn.isEnabled()) {
                    btn.setBackground(bg);
                }
            }
        });
    }

    /**
     * Styles a text field for dark/creme cinema theme.
     */
    public static void styleTextField(JTextField tf) {
        tf.setBackground(INPUT_BG);
        tf.setForeground(INPUT_TEXT);
        tf.setCaretColor(INPUT_TEXT);
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BURGUNDY, 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    /**
     * Styles a combo box.
     */
    public static void styleComboBox(JComboBox<?> cb) {
        cb.setBackground(INPUT_BG);
        cb.setForeground(INPUT_TEXT);
        cb.setFont(FONT_BODY);
        cb.setBorder(BorderFactory.createLineBorder(BURGUNDY, 1));
    }

    /**
     * Styles a JTable for professional cinema admin look.
     */
    public static void styleTable(JTable table) {
        table.setBackground(CARD_BG);
        table.setForeground(CREME);
        table.setFont(FONT_BODY);
        table.setRowHeight(34);
        table.setGridColor(CARD_BORDER);
        table.setSelectionBackground(BURGUNDY);
        table.setSelectionForeground(CREME);
        table.setShowGrid(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(PANEL_BG);
        header.setForeground(CREME);
        header.setFont(FONT_BODY_BOLD);
        header.setPreferredSize(new Dimension(header.getWidth(), 38));
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BURGUNDY));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(CARD_BG);
        centerRenderer.setForeground(CREME);

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer();
        leftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        leftRenderer.setBackground(CARD_BG);
        leftRenderer.setForeground(CREME);
    }
}
