package clients;

import javax.swing.*;
import java.awt.*;

public class ThemeModeManager {
    private static final String LIGHT_ICON = "☀";
    private static final String DARK_ICON = "☾";

    // Light theme colors
    private static final Color LIGHT_BACKGROUND = new Color(238, 238, 238);
    private static final Color LIGHT_FOREGROUND_TEXT = new Color(33, 33, 33);
    private static final Color LIGHT_FOREGROUND_HINT = Color.GRAY;
    private static final Color LIGHT = Color.WHITE;
    private static final Color LIGHT_BUTTON = new Color(218, 232, 245);

    // Dark theme colors
    private static final Color DARK_BACKGROUND = new Color(55, 55, 55);
    private static final Color DARK_FOREGROUND_TEXT = new Color(238, 238, 238);
    private static final Color DARK_FOREGROUND_HINT = Color.LIGHT_GRAY;
    private static final Color DARK_BUTTON = new Color(89, 149, 204);

    private boolean isDarkMode = false;
    private JButton themeToggleButton;

    // Singleton instance
    private static ThemeModeManager instance;

    private ThemeModeManager() {
        // Private constructor to prevent direct instantiation
    }

    public static ThemeModeManager getInstance() {
        if (instance == null) {
            instance = new ThemeModeManager();
        }
        return instance;
    }

    public JButton createThemeToggleButton() {
        themeToggleButton = new JButton(LIGHT_ICON);
        themeToggleButton.setFont(new Font("Dialog", Font.PLAIN, 12)); // Increase font size for the icon
        themeToggleButton.setFocusPainted(false); // Remove focus border
        themeToggleButton.addActionListener(e -> toggleTheme());

        // Make the button more compact and circular
        themeToggleButton.setPreferredSize(new Dimension(40, 40));
        themeToggleButton.setMargin(new Insets(2, 2, 2, 2));

        return themeToggleButton;
    }

    public void toggleTheme() {
        isDarkMode = !isDarkMode;
        themeToggleButton.setText(isDarkMode ? DARK_ICON : LIGHT_ICON);

        // Apply the theme to all visible windows
        for (Window window : Window.getWindows()) {
            if (window.isDisplayable()) {
                applyTheme(window);
            }
        }
    }

    public void applyTheme(Component component) {
        if (component instanceof JPanel || component instanceof JFrame) {
            component.setBackground(isDarkMode ? DARK_BACKGROUND : LIGHT_BACKGROUND);
        }

        if (component instanceof JButton) {
            component.setBackground(isDarkMode ? DARK_BUTTON : LIGHT_BUTTON);
            component.setForeground(isDarkMode ? DARK_FOREGROUND_TEXT : LIGHT_FOREGROUND_TEXT);
        }

        if (component instanceof JLabel) {
            component.setForeground(isDarkMode ? DARK_FOREGROUND_TEXT : LIGHT_FOREGROUND_TEXT);
        }

        if (component instanceof JTextField) {
            component.setBackground(isDarkMode ? DARK_BACKGROUND : LIGHT);
            component.setForeground(isDarkMode ? DARK_FOREGROUND_TEXT : LIGHT_FOREGROUND_TEXT);
            ((JTextField) component).setCaretColor(isDarkMode ? DARK_FOREGROUND_HINT : LIGHT_FOREGROUND_HINT);
        }

        if (component instanceof JTextArea) {
            component.setBackground(isDarkMode ? DARK_BACKGROUND : LIGHT);
            component.setForeground(isDarkMode ? DARK_FOREGROUND_HINT : LIGHT_FOREGROUND_TEXT);
        }

        // Recursively apply theme to all child components
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyTheme(child);
            }
        }

        // Repaint the component
        component.repaint();
    }

    public boolean isDarkMode() {
        return isDarkMode;
    }

    public static Color getLightBackground() {
        return LIGHT_BACKGROUND;
    }

    public static Color getDarkBackground() {
        return DARK_BACKGROUND;
    }

    public static Color getLightForegroundText() {
        return LIGHT_FOREGROUND_TEXT;
    }

    public static Color getDarkForegroundText() {
        return DARK_FOREGROUND_TEXT;
    }

    public static Color getLightForegroundHint() {
        return LIGHT_FOREGROUND_HINT;
    }

    public static Color getDarkForegroundHint() {
        return DARK_FOREGROUND_HINT;
    }
}