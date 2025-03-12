package com.example.dfa_app.DFA;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a single DFA state with a visual JavaFX representation.
 */
public class State extends Group {
    // Unique identifier generator for states.
    private static long idCounter = 0;
    // Global tracker for unique (nonempty) state names.
    private static final Set<String> stateNames = new HashSet<>();

    private final long id;
    private String name;
    private boolean accepting;
    private final List<Transition> transitions;

    // Visual components.
    private final Circle mainCircle;
    private Circle acceptingIndicator;
    private final EditableLabel editableLabel;

    /**
     * Constructs a new State at the specified center with the given radius and color.
     *
     * @param centerX the x-coordinate of the state's center.
     * @param centerY the y-coordinate of the state's center.
     * @param radius  the radius of the state.
     * @param color   the fill color of the state.
     */
    public State(double centerX, double centerY, double radius, Color color) {
        this.id = idCounter++;
        setLayoutX(centerX);
        setLayoutY(centerY);
        transitions = new ArrayList<>();

        // Main circle that represents the state.
        mainCircle = new Circle(0, 0, radius);
        mainCircle.setFill(color);
        mainCircle.setStroke(Color.BLACK);
        mainCircle.setUserData(this);

        // Editable label for state naming.
        editableLabel = new EditableLabel();
        editableLabel.setEditorPosition(radius, -1.5 * radius);

        getChildren().addAll(mainCircle, editableLabel);

        // Initialize state properties.
        this.accepting = false;
        this.name = "";
    }

    /**
     * Constructs a new State with an initial name.
     *
     * @param centerX the x-coordinate of the state's center.
     * @param centerY the y-coordinate of the state's center.
     * @param radius  the radius of the state.
     * @param color   the fill color of the state.
     * @param name    the initial name for the state.
     */
    public State(double centerX, double centerY, double radius, Color color, String name) {
        this(centerX, centerY, radius, color);
        setName(name);
    }

    /**
     * @return the current state name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the state's name after validating that it is non-null, nonempty, and unique.
     * If invalid, an alert is displayed and name editing is re-enabled.
     *
     * @param newName new name for the state.
     */
    public void setName(String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            showAlert("Invalid State Name", "State name cannot be empty. Please choose a unique name.");
            Platform.runLater(editableLabel::startEditing);
            return;
        }
        if (newName.equals(this.name)) {
            return;
        }
        if (stateNames.contains(newName)) {
            showAlert("Duplicate State Name", "A state with the name '" + newName
                    + "' already exists. Please choose a unique name.");
            Platform.runLater(editableLabel::startEditing);
            return;
        }
        // Remove previous name from tracking if one exists.
        if (this.name != null && !this.name.trim().isEmpty()) {
            stateNames.remove(this.name);
        }
        this.name = newName;
        stateNames.add(newName);
        setLabelText(newName);
    }

    /**
     * @return true if this is an accepting (final) state.
     */
    public boolean isAccepting() {
        return accepting;
    }

    /**
     * Marks or unmarks this state as an accepting state.
     *
     * @param accepting true if the state should be accepting.
     */
    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
        updateAcceptingIndicator();
    }

    /**
     * Adds a new transition from this state using the given symbol to the specified target state.
     *
     * @param symbol    the symbol triggering the transition.
     * @param nextState the target state.
     * @throws IllegalArgumentException if either symbol or nextState is null.
     */
    public void addTransition(String symbol, State nextState) {
        if (symbol == null || nextState == null) {
            throw new IllegalArgumentException("Transition symbol and next state cannot be null.");
        }
        transitions.add(new Transition(this, symbol, nextState));
    }

    /**
     * Removes a specific transition from this state.
     *
     * @param transition the transition to remove.
     */
    public void removeTransition(Transition transition) {
        if (transition != null) {
            transitions.remove(transition);
        }
    }

    /**
     * Removes all transitions that match the given symbol and target state.
     *
     * @param symbol    the symbol of the transitions to remove.
     * @param nextState the target state for transitions to remove.
     */
    public void removeTransition(String symbol, State nextState) {
        transitions.removeIf(t -> t.getSymbol().equals(symbol) && Objects.equals(t.getNextState(), nextState));
    }

    /**
     * @return an unmodifiable list of all outgoing transitions from this state.
     */
    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }

    /**
     * Filters and retrieves transitions that have the specified symbol.
     *
     * @param symbol the symbol to filter transitions.
     * @return a list of transitions matching the symbol.
     */
    public List<Transition> getTransitions(String symbol) {
        List<Transition> matching = new ArrayList<>();
        for (Transition t : transitions) {
            if (t.getSymbol().equals(symbol)) {
                matching.add(t);
            }
        }
        return matching;
    }

    /**
     * Moves this state instantly to a new position.
     *
     * @param newX new x-coordinate.
     * @param newY new y-coordinate.
     */
    public void moveState(double newX, double newY) {
        setLayoutX(newX);
        setLayoutY(newY);
    }

    /**
     * Smoothly animates the state moving to a new position over the given duration.
     *
     * @param newX           target x-coordinate.
     * @param newY           target y-coordinate.
     * @param durationMillis duration of the animation in milliseconds.
     */
    public void animateMoveState(double newX, double newY, double durationMillis) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(layoutXProperty(), newX, Interpolator.EASE_BOTH);
        KeyValue kvY = new KeyValue(layoutYProperty(), newY, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(Duration.millis(durationMillis), kvX, kvY);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }

    /**
     * Visually selects this state, highlighting it and enabling name editing.
     */
    public void select() {
        mainCircle.setStroke(Color.BLUE);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), this);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);
        scaleUp.play();
        editableLabel.startEditing();
    }

    /**
     * Deselects this state and attempts to finalize the name that was edited.
     */
    public void deselect() {
        mainCircle.setStroke(Color.BLACK);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), this);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();

        // Validate and commit the name.
        attemptFinalizeName();
    }

    /**
     * Deletes this state by cleaning up resources:
     * - Removes the state's name from the global tracker.
     * - Clears all outgoing transitions.
     * - Removes the state from its parent's scene graph to clear it from view.
     */
    public void deleteState() {
        // Remove state's name from tracker if one exists.
        if (this.name != null && !this.name.trim().isEmpty()) {
            stateNames.remove(this.name);
        }
        // Clear any transitions.
        transitions.clear();
        // Remove from parent's children list if it exists.
        if (this.getParent() instanceof Group) {
            ((Group) this.getParent()).getChildren().remove(this);
        }
    }

    /**
     * Sets the label text and centers the label within the state's main circle.
     *
     * @param text the new label text.
     */
    public void setLabelText(String text) {
        editableLabel.setText(text);
        Platform.runLater(() -> {
            editableLabel.applyCss();
            editableLabel.layout();
            double labelWidth = editableLabel.getLabelWidth();
            double labelHeight = editableLabel.getLabelHeight();

            // Center the label within the circle.
            editableLabel.setLabelPosition(-labelWidth / 2, -labelHeight / 2);
        });
    }

    /**
     * Checks the candidate name from the editable label. If valid, commits it;
     * otherwise, prompts the user to correct it.
     */
    private void attemptFinalizeName() {
        String proposedName = editableLabel.getText();
        if (isValidName(proposedName)) {
            commitName(proposedName);
            editableLabel.finalizeLabel();
            setLabelText(proposedName);
            System.out.println("Updated state label: " + this.name);
            // Remove the ENTER key handler.
            editableLabel.getEditor().setOnAction(null);
        } else {
            if (proposedName == null || proposedName.trim().isEmpty()) {
                showAlert("Invalid State Name", "State name cannot be empty. Please enter a valid name.");
            } else if (stateNames.contains(proposedName)) {
                showAlert("Duplicate State Name", "A state with the name '" + proposedName
                        + "' already exists. Please choose a unique name.");
            }
            // Re-enable editing if the name is invalid.
            editableLabel.startEditing();
            editableLabel.getEditor().setOnAction(e -> attemptFinalizeName());
        }
    }

    /**
     * Validates the candidate name.
     *
     * @param candidate the name to validate.
     * @return true if the candidate is non-null, nonempty, and unique.
     */
    private boolean isValidName(String candidate) {
        return candidate != null
                && !candidate.trim().isEmpty()
                && !stateNames.contains(candidate);
    }

    /**
     * Commits a valid name by updating both the state and the global tracker.
     *
     * @param validName the name that has been validated.
     */
    private void commitName(String validName) {
        if (this.name != null && !this.name.trim().isEmpty()) {
            stateNames.remove(this.name);
        }
        this.name = validName;
        stateNames.add(validName);
    }

    /**
     * Updates the visual indicator that shows if this state is accepting.
     * Adds a dashed circle when accepting and removes it when not.
     */
    private void updateAcceptingIndicator() {
        if (accepting) {
            if (acceptingIndicator == null) {
                acceptingIndicator = new Circle(0, 0, mainCircle.getRadius() + 4);
                acceptingIndicator.setFill(Color.TRANSPARENT);
                acceptingIndicator.setStroke(Color.GREEN);
                acceptingIndicator.getStrokeDashArray().addAll(4.0, 4.0);
                // Add indicator below the main circle.
                getChildren().add(0, acceptingIndicator);
            }
        } else if (acceptingIndicator != null) {
            getChildren().remove(acceptingIndicator);
            acceptingIndicator = null;
        }
    }

    /**
     * Displays an alert dialog for error messages.
     *
     * @param title   the title for the alert.
     * @param content the error message content.
     */
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Returns the state's name.
     *
     * @return the name of the state.
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Determines equality based on the state's unique ID.
     *
     * @param o the other object.
     * @return true if both states have the same ID.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof State))
            return false;
        State other = (State) o;
        return this.id == other.id;
    }

    /**
     * @return the hash code generated from the state's unique ID.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * @return the main circle representing the state.
     */
    public Circle getMainCircle() {
        return mainCircle;
    }

    /**
     * Alias for getMainCircle().
     *
     * @return the primary visual circle for this state.
     */
    public Circle getCircle() {
        return mainCircle;
    }
}