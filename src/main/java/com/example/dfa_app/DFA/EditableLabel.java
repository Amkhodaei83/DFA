package com.example.dfa_app.DFA;

import javafx.beans.value.ChangeListener;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;

public class EditableLabel {
    private Text label;
    private TextField labelEditor;
    private Pane pane;

    // Listeners for keeping the label and editor in sync
    private ChangeListener<String> labelEditorListener = (observable, oldValue, newValue) -> {
        label.setText(newValue);
    };

    public EditableLabel(Pane pane) {
        this.pane = pane;

        // Create and configure the label
        label = new Text("");
        label.setMouseTransparent(true); // Prevent label from capturing mouse events
        label.setVisible(false);

        // Create the label editor (hidden initially)
        labelEditor = new TextField();
        labelEditor.setVisible(false);
        pane.getChildren().add(labelEditor);
    }

    public void setPosition(double x, double y) {
        // Position label and editor with some offset
        label.setX(x + 10);
        label.setY(y - 10);
        labelEditor.setLayoutX(x + 10);
        labelEditor.setLayoutY(y - 10 - label.getBoundsInLocal().getHeight());
    }

    public void finalizeLabel() {
        if (!pane.getChildren().contains(label)) {
            pane.getChildren().add(label);
        }
    }
    public void setCenteredPosition(double centerX, double centerY) {
        // Force CSS and layout pass to ensure the bounds are updated.
        label.applyCss();


        double labelWidth = label.getBoundsInLocal().getWidth();
        double labelHeight = label.getBoundsInLocal().getHeight();

        // Set so that the horizontal center of the label is at centerX.
        label.setX(centerX - labelWidth / 2);
        // Vertical centering can be adjusted since Text's y is the baseline.
        label.setY(centerY + labelHeight / 4);  // Adjust this value if needed
        label.setVisible(true);
        // Also update the label editor position if necessary.
        labelEditor.applyCss();
        labelEditor.layout();
        double editorWidth = labelEditor.getWidth();
        double editorHeight = labelEditor.getHeight();
        labelEditor.setLayoutX(centerX - editorWidth / 2);
        labelEditor.setLayoutY(centerY - editorHeight / 2);
    }


    public void showEditor() {
        labelEditor.setText(label.getText());
        labelEditor.setVisible(true);
        labelEditor.requestFocus();

        // Sync the text and position
        labelEditor.textProperty().addListener(labelEditorListener);
    }

    public void createAndEdit(double x, double y) {
        setPosition(x, y);     // Set label position
        finalizeLabel();       // Add label to the pane if not already added
        showEditor();          // Immediately show the editor for user input
    }

    public void hideEditor() {
        labelEditor.setVisible(false);
        labelEditor.textProperty().removeListener(labelEditorListener);
    }

    public Text getLabel() {
        return label;
    }

    public TextField getLabelEditor() {
        return labelEditor;
    }
}
