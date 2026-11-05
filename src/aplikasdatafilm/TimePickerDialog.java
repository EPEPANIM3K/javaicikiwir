package aplikasdatafilm;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.time.LocalTime;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

/**
 * Modern Time Picker Modal Dialog.
 * Styled after vertical drum/wheel time picker (reference: media_1793864161795_53d505a8.png)
 * featuring dual vertical columns for Hours and Minutes, clear prominent active value,
 * subtle adjacent values, with "Simpan" and "Batal" actions.
 */
public class TimePickerDialog extends JDialog {

    private int selectedHour = 14;
    private int selectedMinute = 0;
    private boolean saved = false;

    private final WheelColumnPanel hourWheel;
    private final WheelColumnPanel minuteWheel;
    private final JLabel lblDisplay;

    public TimePickerDialog(Window owner, LocalTime initialTime) {
        super(owner, "Pilih Jam Tayang", ModalityType.APPLICATION_MODAL);
        if (initialTime != null) {
            this.selectedHour = initialTime.getHour();
            this.selectedMinute = initialTime.getMinute();
        }

        setResizable(false);
        setUndecorated(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(new Color(0xF8, 0xF8, 0xF9)); // Light modern minimalist as in reference
        mainPanel.setBorder(new EmptyBorder(20, 24, 20, 24));

        // Title
        JLabel lblTitle = new JLabel("PILIH JAM TAYANG");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(new Color(0x22, 0x22, 0x22));
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitle);

        mainPanel.add(Box.createVerticalStrut(14));

        // Display current time: "14 : 00 WIB"
        lblDisplay = new JLabel(formatTime(selectedHour, selectedMinute));
        lblDisplay.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDisplay.setForeground(CinemaTheme.BURGUNDY);
        lblDisplay.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblDisplay);

        mainPanel.add(Box.createVerticalStrut(16));

        // Wheel Container
        JPanel wheelContainer = new JPanel(new GridBagLayout());
        wheelContainer.setBackground(new Color(0xF8, 0xF8, 0xF9));
        wheelContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xE5, 0xE5, 0xE7), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // Column Labels
        JLabel lblColHour = new JLabel("JAM", SwingConstants.CENTER);
        lblColHour.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblColHour.setForeground(new Color(0x88, 0x88, 0x88));

        JLabel lblColMin = new JLabel("MENIT", SwingConstants.CENTER);
        lblColMin.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblColMin.setForeground(new Color(0x88, 0x88, 0x88));

        hourWheel = new WheelColumnPanel(24, selectedHour, val -> {
            selectedHour = val;
            updateDisplay();
        });

        minuteWheel = new WheelColumnPanel(60, selectedMinute, val -> {
            selectedMinute = val;
            updateDisplay();
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, 8, 4, 8);
        gbc.gridx = 0; gbc.gridy = 0;
        wheelContainer.add(lblColHour, gbc);

        gbc.gridx = 2; gbc.gridy = 0;
        wheelContainer.add(lblColMin, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        wheelContainer.add(hourWheel, gbc);

        // Vertical divider line between hour and minute
        gbc.gridx = 1; gbc.gridy = 1;
        JPanel div = new JPanel();
        div.setBackground(new Color(0xEE, 0xEE, 0xEE));
        div.setPreferredSize(new Dimension(1, 140));
        wheelContainer.add(div, gbc);

        gbc.gridx = 2; gbc.gridy = 1;
        wheelContainer.add(minuteWheel, gbc);

        wheelContainer.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(wheelContainer);

        mainPanel.add(Box.createVerticalStrut(20));

        // Buttons: Simpan & Batal
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        btnPanel.setOpaque(false);

        JButton btnBatal = new JButton("Batal");
        CinemaTheme.styleButton(btnBatal, new Color(0xE0, 0xE0, 0xE0), new Color(0xD0, 0xD0, 0xD0),
                new Color(0x33, 0x33, 0x33), new Font("Segoe UI", Font.BOLD, 12), new Insets(8, 20, 8, 20));
        btnBatal.addActionListener(e -> {
            saved = false;
            dispose();
        });

        JButton btnSimpan = new JButton("Simpan");
        CinemaTheme.styleButton(btnSimpan, CinemaTheme.BURGUNDY, CinemaTheme.BURGUNDY_HOVER,
                CinemaTheme.CREME, new Font("Segoe UI", Font.BOLD, 12), new Insets(8, 24, 8, 24));
        btnSimpan.addActionListener(e -> {
            saved = true;
            dispose();
        });

        btnPanel.add(btnBatal);
        btnPanel.add(btnSimpan);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(btnPanel);

        getContentPane().add(mainPanel);
        pack();
        setLocationRelativeTo(owner);
    }

    private void updateDisplay() {
        lblDisplay.setText(formatTime(selectedHour, selectedMinute));
    }

    private static String formatTime(int hour, int minute) {
        return String.format("%02d : %02d WIB", hour, minute);
    }

    public boolean isSaved() {
        return saved;
    }

    public LocalTime getSelectedTime() {
        return LocalTime.of(selectedHour, selectedMinute);
    }

    public static LocalTime showDialog(Window owner, LocalTime initialTime) {
        TimePickerDialog dialog = new TimePickerDialog(owner, initialTime);
        dialog.setVisible(true);
        if (dialog.isSaved()) {
            return dialog.getSelectedTime();
        }
        return null;
    }

    /**
     * Vertical Drum / Wheel component showing 5 slots:
     * -2 (faded)
     * -1 (light gray)
     *  0 (selected, big bold, prominent)
     * +1 (light gray)
     * +2 (faded)
     */
    private static class WheelColumnPanel extends JPanel {

        private final int max;
        private int current;
        private final ValueChangeListener listener;

        private final JLabel[] labels = new JLabel[5];

        public interface ValueChangeListener {
            void onChange(int newValue);
        }

        public WheelColumnPanel(int max, int initial, ValueChangeListener listener) {
            this.max = max;
            this.current = (initial % max + max) % max;
            this.listener = listener;

            setLayout(new GridLayout(5, 1, 0, 4));
            setBackground(new Color(0xF8, 0xF8, 0xF9));
            setPreferredSize(new Dimension(80, 160));

            // Fonts & Colors matching the reference image:
            // Center (selected): 28pt bold black
            // Adjacent 1: 20pt light gray
            // Adjacent 2: 15pt very faint gray
            Font fontCenter = new Font("Segoe UI", Font.BOLD, 28);
            Font fontAdj1 = new Font("Segoe UI", Font.PLAIN, 20);
            Font fontAdj2 = new Font("Segoe UI", Font.PLAIN, 15);

            Color colorCenter = new Color(0x11, 0x11, 0x11);
            Color colorAdj1 = new Color(0x9E, 0x9E, 0x9E);
            Color colorAdj2 = new Color(0xDD, 0xDD, 0xDD);

            for (int i = 0; i < 5; i++) {
                labels[i] = new JLabel("", SwingConstants.CENTER);
                labels[i].setCursor(new Cursor(Cursor.HAND_CURSOR));
                final int offset = i - 2;
                labels[i].addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseClicked(java.awt.event.MouseEvent e) {
                        if (offset != 0) {
                            step(offset);
                        }
                    }
                });
                add(labels[i]);
            }

            labels[0].setFont(fontAdj2);
            labels[0].setForeground(colorAdj2);

            labels[1].setFont(fontAdj1);
            labels[1].setForeground(colorAdj1);

            labels[2].setFont(fontCenter);
            labels[2].setForeground(colorCenter);

            labels[3].setFont(fontAdj1);
            labels[3].setForeground(colorAdj1);

            labels[4].setFont(fontAdj2);
            labels[4].setForeground(colorAdj2);

            // Mouse wheel listener for natural scrolling
            addMouseWheelListener((MouseWheelEvent e) -> {
                int rotation = e.getWheelRotation();
                step(rotation > 0 ? 1 : -1);
            });

            refreshLabels();
        }

        private void step(int delta) {
            current = (current + delta % max + max) % max;
            refreshLabels();
            if (listener != null) {
                listener.onChange(current);
            }
        }

        private void refreshLabels() {
            for (int i = 0; i < 5; i++) {
                int offset = i - 2;
                int val = (current + offset % max + max) % max;
                labels[i].setText(String.format("%02d", val));
            }
        }
    }
}
