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

public class State extends Group {

    private static long idCounter = 0;

    private static final Set<String> stateNames = new HashSet<>();

    private final long id;
    private String name;
    private boolean accepting;
    private final List<Transition> transitions;

    private final Circle mainCircle;
    private Circle acceptingIndicator;
    private final EditableLabel editableLabel;


    public State(double centerX, double centerY, double radius, Color color) {
        this.id = idCounter++;
        setLayoutX(centerX);
        setLayoutY(centerY);
        transitions = new ArrayList<>();


        mainCircle = new Circle(0, 0, radius);
        mainCircle.setFill(color);
        mainCircle.setStroke(Color.BLACK);
        mainCircle.setUserData(this);


        editableLabel = new EditableLabel();
        editableLabel.setEditorPosition(radius, -1.5 * radius);

        getChildren().addAll(mainCircle, editableLabel);


        this.accepting = false;
        this.name = "";
        editableLabel.setMouseTransparent(true);
    }


    public State(double centerX, double centerY, double radius, Color color, String name) {
        this(centerX, centerY, radius, color);
        setName(name);
    }


    public String getName() {
        return name;
    }


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


    public boolean isAccepting() {
        return accepting;
    }


    public void setAccepting(boolean accepting) {
        this.accepting = accepting;
        updateAcceptingIndicator();
    }




    public void removeTransition(Transition transition) {
        if (transition != null) {
            transitions.remove(transition);
        }
    }


    public void removeTransition(String symbol, State nextState) {
        transitions.removeIf(t -> t.getSymbol().equals(symbol) && Objects.equals(t.getNextState(), nextState));
    }


    public List<Transition> getTransitions() {
        return Collections.unmodifiableList(transitions);
    }


    public List<Transition> getTransitions(String symbol) {
        List<Transition> matching = new ArrayList<>();
        for (Transition t : transitions) {
            if (t.getSymbol().equals(symbol)) {
                matching.add(t);
            }
        }
        return matching;
    }


    public void moveState(double newX, double newY) {
        setLayoutX(newX);
        setLayoutY(newY);
    }


    public void animateMoveState(double newX, double newY, double durationMillis) {
        Timeline timeline = new Timeline();
        KeyValue kvX = new KeyValue(layoutXProperty(), newX, Interpolator.EASE_BOTH);
        KeyValue kvY = new KeyValue(layoutYProperty(), newY, Interpolator.EASE_BOTH);
        KeyFrame kf = new KeyFrame(Duration.millis(durationMillis), kvX, kvY);
        timeline.getKeyFrames().add(kf);
        timeline.play();
    }


    public void select() {
        mainCircle.setStroke(Color.BLUE);
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), this);
        scaleUp.setToX(1.1);
        scaleUp.setToY(1.1);
        scaleUp.play();
        editableLabel.startEditing();
    }


    public void deselect() {
        mainCircle.setStroke(Color.BLACK);
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), this);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.play();

        attemptFinalizeName();
    }


    public void deleteState() {

        if (this.name != null && !this.name.trim().isEmpty()) {
            stateNames.remove(this.name);
        }

        transitions.clear();

        if (this.getParent() instanceof Group) {
            ((Group) this.getParent()).getChildren().remove(this);
        }
    }


    public void setLabelText(String text) {
        editableLabel.setText(text);
        Platform.runLater(() -> {
            editableLabel.applyCss();
            editableLabel.layout();
            double labelWidth = editableLabel.getLabelWidth();
            double labelHeight = editableLabel.getLabelHeight();


            editableLabel.setLabelPosition(-labelWidth / 2, -labelHeight / 2);
        });
    }


    private void attemptFinalizeName() {
        String proposedName = editableLabel.getText();
        if (isValidName(proposedName)) {
            commitName(proposedName);
            editableLabel.finalizeLabel();
            setLabelText(proposedName);
            System.out.println(this.name);
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


    private boolean isValidName(String candidate) {
        return candidate != null
                && !candidate.trim().isEmpty()
                && !stateNames.contains(candidate);
    }


    private void commitName(String validName) {
        if (this.name != null && !this.name.trim().isEmpty()) {
            stateNames.remove(this.name);
        }
        this.name = validName;
        stateNames.add(validName);
    }


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


    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }


    @Override
    public String toString() {
        return name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof State))
            return false;
        State other = (State) o;
        return this.id == other.id;
    }


    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public Circle getMainCircle() {
        return mainCircle;
    }


    public Circle getCircle() {
        return mainCircle;
    }
}