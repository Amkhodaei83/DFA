package com.example.dfa;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HelloApplication extends Application {

    private static final Logger LOGGER = Logger.getLogger(HelloApplication.class.getName());

    @Override
    public void init() {
        // Set a default handler for any uncaught exceptions in the application thread.
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.SEVERE, "Uncaught Exception in thread " + thread.getName(), throwable);
        });
    }

    @Override
    public void start(Stage stage) {
        try {
            // Load the FXML file from the classpath
            URL fxmlResource = getClass().getResource("Main_DFA.fxml");
            if (fxmlResource == null) {
                throw new IOException("FXML resource 'Main_DFA.fxml' not found.");
            }
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlResource);
            Parent root = fxmlLoader.load();

            // Create the scene with the specified dimensions
            Scene scene = new Scene(root, 1280, 720);

            // Load CSS styling if available
            URL cssResource = getClass().getResource("styles.css");
            if (cssResource != null) {
                scene.getStylesheets().add(cssResource.toExternalForm());
            } else {
                LOGGER.warning("CSS resource 'styles.css' not found.");
            }

            // Set up the stage with a descriptive title and enable resizing.
            stage.setTitle("Deterministic Finite Automaton");
            stage.setScene(scene);
            stage.setResizable(true);
            stage.show();
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Failed to load FXML file.", ex);
            // Display an error alert and exit the application gracefully.
            showErrorDialog("Initialization Error", "Failed to load application components.", ex.getMessage());
            Platform.exit();
        }
    }

    /**
     * Displays an error dialog with the given parameters.
     *
     * @param title   the title of the alert dialog
     * @param header  the header text in the dialog
     * @param content the detailed error message
     */
    private void showErrorDialog(String title, String header, String content) {
        // Ensure the dialog is shown on the JavaFX Application Thread.
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
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
