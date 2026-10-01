package aplikasdatafilm;

import java.awt.Color;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public final class PlaceholderSupport {

    private static final String PLACEHOLDER_KEY = PlaceholderSupport.class.getName() + ".placeholder";
    private static final String NORMAL_COLOR_KEY = PlaceholderSupport.class.getName() + ".normalColor";
    private static final String PLACEHOLDER_COLOR_KEY = PlaceholderSupport.class.getName() + ".placeholderColor";
    private static final String PASSWORD_ECHO_KEY = PlaceholderSupport.class.getName() + ".passwordEcho";
    private static final Color PLACEHOLDER_COLOR = new Color(130, 130, 130);

    private PlaceholderSupport() {
    }

    public static void install(JTextField field, String placeholder) {
        Color normalColor = field.getForeground();
        configure(field, placeholder, normalColor);
        showPlaceholder(field);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (isShowing(field)) {
                    field.setText("");
                    field.setForeground(normalColor);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (field.getText().isEmpty()) {
                    showPlaceholder(field);
                }
            }
        });
    }

    public static void install(JPasswordField field, String placeholder) {
        Color normalColor = field.getForeground();
        char echoCharacter = field.getEchoChar();
        configure(field, placeholder, normalColor);
        field.putClientProperty(PASSWORD_ECHO_KEY, echoCharacter);
        showPlaceholder(field);
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (isShowing(field)) {
                    field.setText("");
                    field.setForeground(normalColor);
                    field.setEchoChar(echoCharacter);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (field.getPassword().length == 0) {
                    showPlaceholder(field);
                }
            }
        });
    }

    public static String getText(JTextField field) {
        return isShowing(field) ? "" : field.getText();
    }

    public static char[] getPassword(JPasswordField field) {
        return isShowing(field) ? new char[0] : field.getPassword();
    }

    public static void setText(JTextField field, String value) {
        field.setForeground(normalColor(field));
        field.setText(value);
    }

    public static void reset(JTextField field) {
        showPlaceholder(field);
    }

    public static void reset(JPasswordField field) {
        showPlaceholder(field);
    }

    private static void configure(JTextField field, String placeholder, Color normalColor) {
        field.putClientProperty(PLACEHOLDER_KEY, placeholder);
        field.putClientProperty(NORMAL_COLOR_KEY, normalColor);
        field.putClientProperty(PLACEHOLDER_COLOR_KEY, PLACEHOLDER_COLOR);
    }

    private static boolean isShowing(JTextField field) {
        Object placeholder = field.getClientProperty(PLACEHOLDER_KEY);
        Object placeholderColor = field.getClientProperty(PLACEHOLDER_COLOR_KEY);
        return placeholder instanceof String && placeholder.equals(field.getText())
                && placeholderColor instanceof Color && placeholderColor.equals(field.getForeground());
    }

    private static Color normalColor(JTextField field) {
        Object color = field.getClientProperty(NORMAL_COLOR_KEY);
        return color instanceof Color ? (Color) color : field.getForeground();
    }

    private static void showPlaceholder(JTextField field) {
        field.setForeground(PLACEHOLDER_COLOR);
        field.setText((String) field.getClientProperty(PLACEHOLDER_KEY));
        if (field instanceof JPasswordField passwordField) {
            Object echoCharacter = field.getClientProperty(PASSWORD_ECHO_KEY);
            if (echoCharacter instanceof Character character) {
                passwordField.setEchoChar((char) 0);
            }
        }
    }
}