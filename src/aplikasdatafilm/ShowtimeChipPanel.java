package aplikasdatafilm;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;

/**
 * Interactive Showtime Chip Management Panel.
 * Contains "+ Tambahkan Jam Tayang" button, responsive wrapping container for time chips,
 * supports editing via TimePickerDialog, deleting, duplicate prevention, and validation.
 */
public class ShowtimeChipPanel extends JPanel {

    private final List<LocalTime> showtimes = new ArrayList<>();
    private final JPanel chipContainer;
    private final JButton btnAdd;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter fmtDot = DateTimeFormatter.ofPattern("HH.mm");

    public ShowtimeChipPanel() {
        setLayout(new BorderLayout(8, 4));
        setOpaque(false);

        // Header / Action row
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topBar.setOpaque(false);

        btnAdd = CinemaTheme.createPrimaryButton("+ Tambah Jam Tayang");
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAdd.setMargin(new Insets(6, 12, 6, 12));
        btnAdd.addActionListener(e -> bukaDialogTambah());

        topBar.add(btnAdd);
        add(topBar, BorderLayout.WEST);

        // Responsive Chip Container
        chipContainer = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        chipContainer.setOpaque(false);
        add(chipContainer, BorderLayout.CENTER);
    }

    private void bukaDialogTambah() {
        Window window = SwingUtilities.getWindowAncestor(this);
        LocalTime defaultTime = showtimes.isEmpty() ? LocalTime.of(14, 0) : showtimes.get(showtimes.size() - 1).plusHours(2);
        LocalTime chosen = TimePickerDialog.showDialog(window, defaultTime);
        if (chosen != null) {
            tambahJam(chosen);
        }
    }

    public boolean tambahJam(LocalTime time) {
        if (showtimes.contains(time)) {
            JOptionPane.showMessageDialog(this,
                    "Jam tayang " + fmt.format(time) + " sudah ada dalam daftar.",
                    "Jam Tayang Duplikat",
                    JOptionPane.WARNING_MESSAGE);
            return false;
        }

        showtimes.add(time);
        Collections.sort(showtimes);
        renderChips();
        return true;
    }

    public void setJamList(List<LocalTime> times) {
        showtimes.clear();
        if (times != null) {
            for (LocalTime t : times) {
                if (!showtimes.contains(t)) {
                    showtimes.add(t);
                }
            }
            Collections.sort(showtimes);
        }
        renderChips();
    }

    public List<LocalTime> getJamList() {
        return Collections.unmodifiableList(showtimes);
    }

    public void clear() {
        showtimes.clear();
        renderChips();
    }

    public String getFormattedJamString() {
        List<String> list = showtimes.stream().map(fmtDot::format).toList();
        return String.join(", ", list);
    }

    private void renderChips() {
        chipContainer.removeAll();

        if (showtimes.isEmpty()) {
            JLabel lblKosong = new JLabel("(Belum ada jam tayang)");
            lblKosong.setFont(CinemaTheme.FONT_SMALL);
            lblKosong.setForeground(CinemaTheme.TEXT_MUTED);
            chipContainer.add(lblKosong);
        } else {
            for (LocalTime time : showtimes) {
                chipContainer.add(createChip(time));
            }
        }

        chipContainer.revalidate();
        chipContainer.repaint();
        revalidate();
        repaint();
    }

    private JPanel createChip(LocalTime time) {
        JPanel chip = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        chip.setBackground(CinemaTheme.CARD_BG);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(CinemaTheme.BURGUNDY, 1),
                BorderFactory.createEmptyBorder(2, 6, 2, 4)
        ));

        // Time text clickable to edit
        JLabel lblTime = new JLabel("🕒 " + fmt.format(time));
        lblTime.setFont(CinemaTheme.FONT_BODY_BOLD);
        lblTime.setForeground(CinemaTheme.CREME);
        lblTime.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblTime.setToolTipText("Klik untuk ubah jam tayang");
        lblTime.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                Window window = SwingUtilities.getWindowAncestor(ShowtimeChipPanel.this);
                LocalTime edited = TimePickerDialog.showDialog(window, time);
                if (edited != null && !edited.equals(time)) {
                    if (showtimes.contains(edited)) {
                        JOptionPane.showMessageDialog(ShowtimeChipPanel.this,
                                "Jam tayang " + fmt.format(edited) + " sudah ada dalam daftar.",
                                "Jam Tayang Duplikat",
                                JOptionPane.WARNING_MESSAGE);
                    } else {
                        showtimes.remove(time);
                        showtimes.add(edited);
                        Collections.sort(showtimes);
                        renderChips();
                    }
                }
            }
        });

        // Delete button
        JLabel btnDelete = new JLabel("✕");
        btnDelete.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnDelete.setForeground(new Color(0xE5, 0x73, 0x73));
        btnDelete.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDelete.setToolTipText("Hapus jam tayang ini");
        btnDelete.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showtimes.remove(time);
                renderChips();
            }
        });

        chip.add(lblTime);
        chip.add(btnDelete);
        return chip;
    }
}
