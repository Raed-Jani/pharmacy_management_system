package com.pharmacie.util;

import javafx.scene.Parent;
import java.util.prefs.Preferences;

/**
 * Manages application theme (Light/Dark mode)
 * Persists user preference using Java Preferences API
 */
public class ThemeManager {

    private static final String PREF_KEY_THEME = "app_theme";
    private static final String THEME_LIGHT = "light";
    private static final String THEME_DARK = "dark";
    private static final String DARK_MODE_CLASS = "dark-mode";

    private static final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);
    private static boolean isDarkMode = false;

    /**
     * Initialize theme manager and load saved preference
     */
    public static void initialize() {
        String savedTheme = prefs.get(PREF_KEY_THEME, THEME_LIGHT);
        isDarkMode = THEME_DARK.equals(savedTheme);
    }

    /**
     * Check if dark mode is currently active
     * 
     * @return true if dark mode is active
     */
    public static boolean isDarkMode() {
        return isDarkMode;
    }

    /**
     * Toggle between light and dark mode
     * 
     * @param root The root node to apply theme to
     * @return The icon to display (moon for light mode, sun for dark mode)
     */
    public static String toggleTheme(Parent root) {
        isDarkMode = !isDarkMode;
        applyTheme(root);
        savePreference();
        return isDarkMode ? "☀️" : "🌙";
    }

    /**
     * Apply the current theme to a root node
     * 
     * @param root The root node to apply theme to
     */
    public static void applyTheme(Parent root) {
        if (root == null)
            return;

        if (isDarkMode) {
            if (!root.getStyleClass().contains(DARK_MODE_CLASS)) {
                root.getStyleClass().add(DARK_MODE_CLASS);
            }
        } else {
            root.getStyleClass().remove(DARK_MODE_CLASS);
        }
    }

    /**
     * Get the appropriate theme icon for the toggle button
     * 
     * @return Moon icon for light mode, sun icon for dark mode
     */
    public static String getThemeIcon() {
        return isDarkMode ? "☀️" : "🌙";
    }

    /**
     * Save current theme preference
     */
    private static void savePreference() {
        String theme = isDarkMode ? THEME_DARK : THEME_LIGHT;
        prefs.put(PREF_KEY_THEME, theme);
    }

    /**
     * Set theme explicitly
     * 
     * @param darkMode true for dark mode, false for light mode
     * @param root     The root node to apply theme to
     */
    public static void setDarkMode(boolean darkMode, Parent root) {
        isDarkMode = darkMode;
        applyTheme(root);
        savePreference();
    }
}
