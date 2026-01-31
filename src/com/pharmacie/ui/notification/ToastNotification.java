package com.pharmacie.ui.notification;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;

/**
 * Modern toast notification system.
 * Position: FIXED BOTTOM-LEFT via Manual Translation to bypass parent layouts.
 * Style: High Contrast (White Background).
 */
public class ToastNotification {

    public enum Type {
        SUCCESS("SUCCÈS", "✓", "#10B981", "#ECFDF5"),
        ERROR("ERREUR", "✕", "#EF4444", "#FEF2F2"),
        INFO("INFO", "ℹ", "#3B82F6", "#EFF6FF"),
        WARNING("AVERTISSEMENT", "⚠", "#F59E0B", "#FFFBEB");

        private final String title;
        private final String icon;
        private final String color;
        private final String bgColor;

        Type(String title, String icon, String color, String bgColor) {
            this.title = title;
            this.icon = icon;
            this.color = color;
            this.bgColor = bgColor;
        }

        public String getTitle() {
            return title;
        }

        public String getIcon() {
            return icon;
        }

        public String getColor() {
            return color;
        }

        public String getBgColor() {
            return bgColor;
        }
    }

    private static final double TOAST_WIDTH = 360;
    private static final Duration SLIDE_DURATION = Duration.millis(400);
    private static final Duration FADE_OUT_DURATION = Duration.millis(300);
    private static final Duration AUTO_DISMISS_DELAY = Duration.seconds(5);

    public static void show(Pane rootPane, String message, Type type) {
        if (rootPane == null)
            return;

        Platform.runLater(() -> {
            try {
                VBox container = findOrCreateContainer(rootPane);
                HBox toast = createToast(message, type);

                // Ensure container is visible
                container.setVisible(true);
                container.toFront();

                container.getChildren().add(toast);
                animateEntrance(toast);
                setupAutoDismiss(toast, container);

            } catch (Exception e) {
                System.err.println("Toast Error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static VBox findOrCreateContainer(Pane rootPane) {
        // Reuse existing container if it exists
        for (Node node : rootPane.getChildren()) {
            if (node instanceof VBox && "toastContainer".equals(node.getId())) {
                return (VBox) node;
            }
        }

        // Create new container
        VBox container = new VBox(10);
        container.setId("toastContainer");
        // We use MANUAL translation for margins, so 0 padding here
        container.setPadding(new Insets(0));
        container.setPickOnBounds(false); // Clicks pass through empty space
        container.setMouseTransparent(false); // Children (Toasts) catch clicks

        // Ensure container accommodates children
        container.setMinWidth(Region.USE_PREF_SIZE);
        container.setMinHeight(Region.USE_PREF_SIZE);

        rootPane.getChildren().add(container);
        container.toFront();

        // MANUAL POSITIONING STRATEGY
        Runnable updatePosition = () -> {
            if (rootPane.getScene() == null)
                return;

            Platform.runLater(() -> {
                double margin = 90.0;
                double paneHeight = rootPane.getHeight();
                double containerHeight = container.getHeight();

                // Fixed X margin (Left)
                container.setTranslateX(margin);

                // Dynamic Y position: Total Height - Container Height - Margin
                if (paneHeight > 0) {
                    double yPos = paneHeight - containerHeight - margin;
                    // Prevent negative positioning (top clip)
                    container.setTranslateY(Math.max(0, yPos));
                }
            });
        };

        // Listeners for robust positioning
        rootPane.heightProperty().addListener((obs, o, n) -> updatePosition.run());
        container.heightProperty().addListener((obs, o, n) -> updatePosition.run());
        container.getChildren().addListener((ListChangeListener<Node>) c -> updatePosition.run());

        // Initial positioning
        Platform.runLater(updatePosition);

        return container;
    }

    private static HBox createToast(String message, Type type) {
        HBox toast = new HBox(12);
        toast.setAlignment(Pos.CENTER_LEFT);

        // FORCE MINIMUM WIDTH to prevent "..." collapsing
        toast.setMinWidth(340);
        toast.setPrefWidth(TOAST_WIDTH);
        toast.setMaxWidth(TOAST_WIDTH);

        toast.setPadding(new Insets(15));

        // High Contrast Styling: White Background
        String style = String.format(
                "-fx-background-color: white; " +
                        "-fx-background-radius: 4; " +
                        "-fx-border-color: %s; " +
                        "-fx-border-width: 0 0 0 4; " + // Left border accent only
                        "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);", // Subtle shadow
                type.getColor());
        toast.setStyle(style);

        // Icon Circle
        StackPane iconContainer = new StackPane();
        iconContainer.setStyle(String.format(
                "-fx-background-color: %s; -fx-background-radius: 50; -fx-min-width: 32; -fx-min-height: 32;",
                type.getBgColor()));
        Label icon = new Label(type.getIcon());
        icon.setStyle(String.format("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: %s;", type.getColor()));
        iconContainer.getChildren().add(icon);

        // Text Content
        VBox textContent = new VBox(2);
        textContent.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textContent, Priority.ALWAYS);

        Label titleLabel = new Label(type.getTitle());
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #333333;");

        Label messageLabel = new Label(message);
        messageLabel.setWrapText(true);
        messageLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555555;");

        textContent.getChildren().addAll(titleLabel, messageLabel);

        // Close Button
        Label closeBtn = new Label("✕");
        closeBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999; -fx-cursor: hand;");
        closeBtn.setOnMouseEntered(
                e -> closeBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-cursor: hand;"));
        closeBtn.setOnMouseExited(
                e -> closeBtn.setStyle("-fx-font-size: 14px; -fx-text-fill: #999999; -fx-cursor: hand;"));

        toast.getChildren().addAll(iconContainer, textContent, closeBtn);

        closeBtn.setOnMouseClicked(e -> dismissToast(toast, (VBox) toast.getParent()));

        return toast;
    }

    private static void animateEntrance(Node toast) {
        toast.setTranslateX(-50); // Start from left
        toast.setOpacity(0);

        TranslateTransition slide = new TranslateTransition(SLIDE_DURATION, toast);
        slide.setToX(0); // Slide to neutral (0 relative to container)
        slide.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition fade = new FadeTransition(SLIDE_DURATION, toast);
        fade.setToValue(1);

        new ParallelTransition(slide, fade).play();
    }

    private static void setupAutoDismiss(Node toast, VBox container) {
        PauseTransition delay = new PauseTransition(AUTO_DISMISS_DELAY);
        delay.setOnFinished(e -> dismissToast(toast, container));
        delay.play();

        toast.setOnMouseEntered(e -> delay.pause());
        toast.setOnMouseExited(e -> delay.play());
    }

    private static void dismissToast(Node toast, VBox container) {
        if (container == null || !container.getChildren().contains(toast))
            return;

        TranslateTransition slide = new TranslateTransition(FADE_OUT_DURATION, toast);
        slide.setToX(-TOAST_WIDTH); // Slide out left

        FadeTransition fade = new FadeTransition(FADE_OUT_DURATION, toast);
        fade.setToValue(0);

        ParallelTransition out = new ParallelTransition(slide, fade);
        out.setOnFinished(e -> container.getChildren().remove(toast));
        out.play();
    }

    public static void showSuccess(Pane root, String message) {
        show(root, message, Type.SUCCESS);
    }

    public static void showError(Pane root, String message) {
        show(root, message, Type.ERROR);
    }

    public static void showInfo(Pane root, String message) {
        show(root, message, Type.INFO);
    }

    public static void showWarning(Pane root, String message) {
        show(root, message, Type.WARNING);
    }
}
