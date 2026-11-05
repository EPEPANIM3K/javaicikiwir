package aplikasdatafilm;

import java.awt.*;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Indonesian Date Picker Dialog.
 * Provides calendar day selection with month and dedicated year controls.
 */
public class ModernDatePickerDialog extends JDialog {

    private LocalDate selectedDate;
    private YearMonth currentYearMonth;
    private boolean saved = false;

    private final JComboBox<String> monthCombo;
    private final JSpinner yearSpinner;
    private final JPanel daysGrid;
    private final JLabel lblSelectedInfo;

    private static final String[] HARI_INDO = {"Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min"};

    public ModernDatePickerDialog(Window owner, LocalDate initialDate) {
        super(owner, "Pilih Tanggal Tayang", ModalityType.APPLICATION_MODAL);
        this.selectedDate = initialDate != null ? initialDate : LocalDate.now();
        this.currentYearMonth = YearMonth.from(selectedDate);

        setResizable(false);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBackground(new Color(0x1F, 0x1B, 0x1F));
        content.setBorder(new EmptyBorder(16, 18, 16, 18));

        // ── TOP: Month & Year Selector ─────────────────────────────────────
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        navPanel.setOpaque(false);

        JButton btnPrev = CinemaTheme.createSmallButton("◀", CinemaTheme.CARD_BG, CinemaTheme.CARD_HOVER);
        btnPrev.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            syncNav();
            renderCalendar();
        });

        monthCombo = new JComboBox<>();
        monthCombo.setFont(CinemaTheme.FONT_BODY_BOLD);
        for (int m = 1; m <= 12; m++) {
            Month month = Month.of(m);
            monthCombo.addItem(month.getDisplayName(TextStyle.FULL, CinemaTheme.LOCALE_ID));
        }
        monthCombo.setSelectedIndex(currentYearMonth.getMonthValue() - 1);
        monthCombo.addActionListener(e -> {
            int newMonth = monthCombo.getSelectedIndex() + 1;
            currentYearMonth = YearMonth.of(currentYearMonth.getYear(), newMonth);
            renderCalendar();
        });

        // Dedicated Year Picker (Spinner)
        int minYear = 2000;
        int maxYear = 2099;
        SpinnerNumberModel yearModel = new SpinnerNumberModel(currentYearMonth.getYear(), minYear, maxYear, 1);
        yearSpinner = new JSpinner(yearModel);
        yearSpinner.setFont(CinemaTheme.FONT_BODY_BOLD);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(yearSpinner, "#");
        yearSpinner.setEditor(editor);
        editor.getTextField().setColumns(4);
        editor.getTextField().setFont(CinemaTheme.FONT_BODY_BOLD);
        yearSpinner.addChangeListener(e -> {
            int newYear = (Integer) yearSpinner.getValue();
            currentYearMonth = YearMonth.of(newYear, currentYearMonth.getMonthValue());
            renderCalendar();
        });

        JButton btnNext = CinemaTheme.createSmallButton("▶", CinemaTheme.CARD_BG, CinemaTheme.CARD_HOVER);
        btnNext.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            syncNav();
            renderCalendar();
        });

        navPanel.add(btnPrev);
        navPanel.add(monthCombo);
        navPanel.add(yearSpinner);
        navPanel.add(btnNext);
        content.add(navPanel, BorderLayout.NORTH);

        // ── CENTER: Calendar Grid (Headers + Days) ──────────────────────────
        JPanel calendarContainer = new JPanel(new BorderLayout(0, 6));
        calendarContainer.setOpaque(false);

        JPanel headerPanel = new JPanel(new GridLayout(1, 7, 4, 4));
        headerPanel.setOpaque(false);
        for (String hari : HARI_INDO) {
            JLabel lblHari = new JLabel(hari, SwingConstants.CENTER);
            lblHari.setFont(CinemaTheme.FONT_BODY_BOLD);
            lblHari.setForeground(hari.equals("Min") ? new Color(0xE5, 0x73, 0x73) : CinemaTheme.CREME);
            headerPanel.add(lblHari);
        }
        calendarContainer.add(headerPanel, BorderLayout.NORTH);

        daysGrid = new JPanel(new GridLayout(6, 7, 4, 4));
        daysGrid.setOpaque(false);
        calendarContainer.add(daysGrid, BorderLayout.CENTER);

        content.add(calendarContainer, BorderLayout.CENTER);

        // ── BOTTOM: Selected Info & Actions ────────────────────────────────
        JPanel bottomPanel = new JPanel(new BorderLayout(0, 10));
        bottomPanel.setOpaque(false);

        lblSelectedInfo = new JLabel("Terpilih: " + CinemaTheme.formatTanggal(selectedDate), SwingConstants.CENTER);
        lblSelectedInfo.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblSelectedInfo.setForeground(CinemaTheme.ACCENT_GOLD);
        bottomPanel.add(lblSelectedInfo, BorderLayout.NORTH);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionPanel.setOpaque(false);

        JButton btnHariIni = CinemaTheme.createSmallButton("Hari Ini", CinemaTheme.CARD_BG, CinemaTheme.CARD_HOVER);
        btnHariIni.addActionListener(e -> {
            selectedDate = LocalDate.now();
            currentYearMonth = YearMonth.from(selectedDate);
            syncNav();
            renderCalendar();
        });

        JButton btnBatal = CinemaTheme.createSmallButton("Batal", CinemaTheme.CARD_BG, CinemaTheme.CARD_HOVER);
        btnBatal.addActionListener(e -> {
            saved = false;
            dispose();
        });

        JButton btnPilih = CinemaTheme.createSmallButton("Pilih Tanggal", CinemaTheme.BURGUNDY, CinemaTheme.BURGUNDY_HOVER);
        btnPilih.addActionListener(e -> {
            saved = true;
            dispose();
        });

        actionPanel.add(btnHariIni);
        actionPanel.add(btnBatal);
        actionPanel.add(btnPilih);
        bottomPanel.add(actionPanel, BorderLayout.SOUTH);

        content.add(bottomPanel, BorderLayout.SOUTH);

        getContentPane().add(content);
        pack();
        setLocationRelativeTo(owner);
        renderCalendar();
    }

    private void syncNav() {
        monthCombo.setSelectedIndex(currentYearMonth.getMonthValue() - 1);
        yearSpinner.setValue(currentYearMonth.getYear());
    }

    private void renderCalendar() {
        daysGrid.removeAll();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        int dayOfWeek = firstOfMonth.getDayOfWeek().getValue(); // 1=Mon, ..., 7=Sun
        int leadingBlanks = dayOfWeek - 1;

        for (int i = 0; i < leadingBlanks; i++) {
            daysGrid.add(new JLabel(""));
        }

        int lengthOfMonth = currentYearMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int day = 1; day <= lengthOfMonth; day++) {
            LocalDate date = currentYearMonth.atDay(day);
            JButton btnDay = new JButton(String.valueOf(day));
            btnDay.setFont(CinemaTheme.FONT_BODY);
            btnDay.setFocusPainted(false);
            btnDay.setMargin(new Insets(4, 4, 4, 4));

            boolean isSelected = date.equals(selectedDate);
            boolean isToday = date.equals(today);

            if (isSelected) {
                btnDay.setBackground(CinemaTheme.BURGUNDY);
                btnDay.setForeground(CinemaTheme.CREME);
                btnDay.setFont(CinemaTheme.FONT_BODY_BOLD);
                btnDay.setBorder(BorderFactory.createLineBorder(CinemaTheme.ACCENT_GOLD, 1));
            } else if (isToday) {
                btnDay.setBackground(new Color(0x35, 0x2C, 0x35));
                btnDay.setForeground(CinemaTheme.ACCENT_GOLD);
                btnDay.setBorder(BorderFactory.createLineBorder(CinemaTheme.ACCENT_GOLD, 1));
            } else {
                btnDay.setBackground(CinemaTheme.CARD_BG);
                btnDay.setForeground(CinemaTheme.CREME);
                btnDay.setBorder(BorderFactory.createLineBorder(CinemaTheme.CARD_BORDER, 1));
            }

            btnDay.addActionListener(e -> {
                selectedDate = date;
                lblSelectedInfo.setText("Terpilih: " + CinemaTheme.formatTanggal(selectedDate));
                renderCalendar();
            });

            daysGrid.add(btnDay);
        }

        // Fill remaining grid slots to maintain 6 rows
        int totalCells = leadingBlanks + lengthOfMonth;
        for (int i = totalCells; i < 42; i++) {
            daysGrid.add(new JLabel(""));
        }

        daysGrid.revalidate();
        daysGrid.repaint();
    }

    public boolean isSaved() {
        return saved;
    }

    public LocalDate getSelectedDate() {
        return selectedDate;
    }

    public static LocalDate showDialog(Window owner, LocalDate initialDate) {
        ModernDatePickerDialog dialog = new ModernDatePickerDialog(owner, initialDate);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            return dialog.getSelectedDate();
        }
        return null;
    }
}
