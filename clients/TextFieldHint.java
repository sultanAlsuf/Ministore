package clients;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class TextFieldHint extends JTextField {
    private final String placeholder;
    private boolean showingPlaceholder;

    public TextFieldHint(String placeholder) {
        this.placeholder = placeholder;
        this.showingPlaceholder = true;

        // Apply initial placeholder state and theme
        initPlaceholder();

        this.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (showingPlaceholder) {
                    setText("");
                    showingPlaceholder = false;
                }
                // Update text color based on theme
                setForeground(getForegroundTextColor());
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (getText().isEmpty()) {
                    initPlaceholder();
                } else {
                    // Keep the appropriate text color for non-empty field
                    setForeground(getForegroundTextColor());
                }
            }
        });

        // Apply theme whenever theme changes
        ThemeModeManager.getInstance().applyTheme(this);
    }

    @Override
    public String getText() {
        return showingPlaceholder ? "" : super.getText();
    }

    /**
     * Initializes the placeholder state.
     */
    private void initPlaceholder() {
        showingPlaceholder = true;
        setForeground(getHintColor());
        setText(placeholder);
    }

    /**
     * Gets the appropriate foreground text color based on the theme.
     *
     * @return The text color for the current theme.
     */
    private Color getForegroundTextColor() {
        if (ThemeModeManager.getInstance().isDarkMode()) {
            return ThemeModeManager.getDarkForegroundText();
        } else {
            return ThemeModeManager.getLightForegroundText();
        }
    }

    /**
     * Gets the appropriate placeholder color based on the theme.
     *
     * @return The placeholder color for the current theme.
     */
    private Color getHintColor() {
        if (ThemeModeManager.getInstance().isDarkMode()) {
            return ThemeModeManager.getDarkForegroundHint();
        } else {
            return ThemeModeManager.getLightForegroundHint();
        }
    }
}
