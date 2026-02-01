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
        return getThemeIcon();
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
     * Get the appropriate theme SVG path for the toggle button
     */
    public static String getThemeIcon() {
        // SVG Moon Path for light mode (to indicate dark mode option)
        String moon = "M12 3c.132 0 .263 0 .393 0a7.5 7.5 0 0 0 7.92 12.446a9 9 0 1 1-8.313-12.454z";
        // SVG Sun Path for dark mode (to indicate light mode option)
        String sun = "M12 7a5 5 0 1 0 0 10 5 5 0 0 0 0-10zM2 11h2v2H2v-2zm18 0h2v2h-2v-2zM11 2h2v2h-2V2zm0 18h2v2h-2v-2zM5.99 4.58l1.41 1.41-1.41 1.41-1.41-1.41 1.41-1.41zm12.02 12.02l1.41 1.41-1.41 1.41-1.41-1.41 1.41-1.41zM5.99 19.42l1.41-1.41 1.41 1.41-1.41 1.41-1.41-1.41zm12.02-12.02l1.41-1.41 1.41 1.41-1.41 1.41-1.41-1.41z";

        return isDarkMode ? sun : moon;
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
