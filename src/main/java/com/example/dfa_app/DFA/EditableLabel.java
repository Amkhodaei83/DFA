package com.example.dfa_app.DFA;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;

/**
 * EditableLabel is a UI control that displays a centered label along with an embedded
 * TextField for editing its content. When editing begins the editor is shown;
 * once finished, the TextField is hidden and the updated text appears centered.
 */
public class EditableLabel extends Pane {

    private static final String DEFAULT_FONT_FAMILY = "Arial";
    private static final double DEFAULT_FONT_SIZE = 14;

    private final Label displayLabel = new Label("");
    private final TextField editor = new TextField("");

    /**
     * Constructs an EditableLabel with default styling.
     * The label is visible initially and the editor is hidden.
     */
    public EditableLabel() {
        // Configure the display label and editor.
        displayLabel.setFont(new Font(DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE));
        editor.setFont(new Font(DEFAULT_FONT_FAMILY, DEFAULT_FONT_SIZE));
        editor.setVisible(false);

        getChildren().addAll(displayLabel, editor);

        // When the editor loses focus, automatically finalize editing.
        editor.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                finalizeLabel();
            }
        });
    }
// Inside EditableLabel class:

    // Add this getter:
    public TextField getEditor() {
        return editor;
    }

    /**
     * Sets both the display label and the editor with the given text.
     *
     * @param text the new text
     */
    public void setText(String text) {
        displayLabel.setText(text);
        editor.setText(text);
    }

    /**
     * Returns the current text from the display label.
     *
     * @return the text currently shown
     */
    public String getText() {
        return editor.getText();
    }

    /**
     * Shows the editor and hides the label.
     * The editor auto-selects its current text.
     */
    public void startEditing() {
        editor.setText(displayLabel.getText());
        displayLabel.setVisible(false);
        editor.setVisible(true);
        Platform.runLater(() -> {
            editor.requestFocus();
            editor.selectAll();
        });
    }

    /**
     * Commits any changes from the editor back to the label.
     */
    public void stopEditing() {
        displayLabel.setText(editor.getText());
    }

    /**
     * Finalizes editing by stopping the editing mode and toggling visibility.
     */
    public void finalizeLabel() {
        stopEditing();
        editor.setVisible(false);
        displayLabel.setVisible(true);
    }

    /**
     * Sets the position of the display label within this pane.
     *
     * @param x the x-coordinate for the label
     * @param y the y-coordinate for the label
     */
    public void setLabelPosition(double x, double y) {
        displayLabel.setLayoutX(x);
        displayLabel.setLayoutY(y);
    }

    /**
     * Sets the position of the editor within this pane.
     *
     * @param x the x-coordinate for the editor
     * @param y the y-coordinate for the editor
     */
    public void setEditorPosition(double x, double y) {
        editor.setLayoutX(x);
        editor.setLayoutY(y);
    }

    /**
     * Returns the width of the display label.
     *
     * @return the width based on the label's layout bounds
     */
    public double getLabelWidth() {
        return displayLabel.getBoundsInLocal().getWidth();
    }

    /**
     * Returns the height of the display label.
     *
     * @return the height based on the label's layout bounds
     */
    public double getLabelHeight() {
        return displayLabel.getBoundsInLocal().getHeight();
    }
}
