package com.example.dfa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());

    // Default (normal) window dimensions.
    private static final double DEFAULT_WIDTH = 1280;
    private static final double DEFAULT_HEIGHT = 720;

    // Variables for maximize toggle.
    private double previousX, previousY, previousWidth, previousHeight;

    @Override
    public void start(Stage stage) {

        try {
            // Load the FXML file containing the complete interface, including the custom tool bar.
            URL fxmlResource = getClass().getResource("Main_DFA.fxml");
            if (fxmlResource == null) {
                throw new IOException("FXML resource 'Main_DFA.fxml' not found.");
            }
            Parent root = FXMLLoader.load(fxmlResource);
            Scene scene = new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT);

            // Optionally load CSS if available.
            URL cssResource = getClass().getResource("styles.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            }

            // Retrieve tool bar controls via fx:id.
            // Note: Since your FXML defines the tool bar as a ToolBar, cast it accordingly.
            ToolBar toolBar = (ToolBar) scene.lookup("#titleBar");
            Button minimizeButton = (Button) scene.lookup("#minimizeButton");
            Button maximizeButton = (Button) scene.lookup("#maximizeButton");
            Button closeButton = (Button) scene.lookup("#closeButton");

            // Set up the tool bar button actions.
            if (minimizeButton != null) {
                minimizeButton.setOnAction(e -> stage.setIconified(true));
            }
            if (closeButton != null) {
                closeButton.setOnAction(e -> Platform.exit());
            }
            if (maximizeButton != null) {
                maximizeButton.setOnAction(e -> toggleMaximized(stage));
            }

            // Because we are keeping the native OS window decorations,
            // there is no custom dragging or snapping logic here.
            // The tool bar is simply an application-specific control bar.
            // The native OS title bar remains active and provides native functionality.

            stage.setScene(scene);
            stage.show();

        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML file.", ex);
            showErrorDialog("Initialization Error", "Failed to load application components.", ex.getMessage());
            Platform.exit();
        }
    }

    /**
     * Toggle between maximized and restored states.
     * When maximizing, store the current bounds; when restoring, revert to those bounds.
     */
    private void toggleMaximized(Stage stage) {
        if (!stage.isMaximized()) {
            // Store current bounds.
            previousX = stage.getX();
            previousY = stage.getY();
            previousWidth = stage.getWidth();
            previousHeight = stage.getHeight();
            stage.setMaximized(true);
        } else {
            stage.setMaximized(false);
            // Restore the stored bounds.
            stage.setX(previousX);
            stage.setY(previousY);
            stage.setWidth(previousWidth);
            stage.setHeight(previousHeight);
        }
    }

    private void showErrorDialog(String title, String header, String content) {
        Platform.runLater(() -> {
            javafx.scene.control.Alert alert =
                    new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
