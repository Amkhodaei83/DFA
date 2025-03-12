package com.example.dfa_app.DFA;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;

public class State /* extends Lable */ {
    private String name;
    private boolean isAccepting;
    private List<Transition> transitions; // List of transitions
    private Circle circle;
    private EditableLabel editableLabel;

    public State(double centerX, double centerY, double radius, Color color, Pane pane) {
        this.isAccepting = false;
        this.transitions = new ArrayList<>();

        circle = new Circle(centerX, centerY, radius);
        circle.setFill(color);
        circle.setStroke(Color.BLACK);
        pane.getChildren().add(circle);

        circle.setUserData(this);

        editableLabel = new EditableLabel(pane);
        // Optionally position the label above the circle:
        editableLabel.createAndEdit(centerX, centerY - radius);
        this.editableLabel.getLabel().toFront();
    }

    public State() {
        // Default constructor if needed
    }

    public String getName() {
        return name;
    }

    public void setAccepting(boolean accepting) {
        isAccepting = accepting;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public void addTransition(String symbol, State nextState,Pane pane) {
        transitions.add(new Transition(this,pane));
    }

    public State getTransition(String symbol) {
        for (Transition t : transitions) {
            if (t.getSymbol().equals(symbol)) {
                return t.getNextState();
            }
        }
        return null;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return name;
    }

    public Circle getCircle() {
        return circle;
    }

    public void setName(String name) {
        setLabelText(name);
        this.name = name;
    }

    public EditableLabel getEditableLabel() {
        return editableLabel;
    }

    public void setLabelText(String text) {
        editableLabel.getLabel().setText(text);
    }

    public void moveCircle(double newX, double newY) {
        circle.setCenterX(newX);
        circle.setCenterY(newY);

        // Update label position
        editableLabel.setPosition(newX, newY - circle.getRadius());
    }

    public void select() {
        circle.setStroke(Color.BLUE); // Highlight circle
        editableLabel.showEditor();     // Allow label editing
    }

    public void deselect() {
        circle.setStroke(Color.BLACK);  // Reset stroke
        editableLabel.hideEditor();       // Close label editor
        this.name = editableLabel.getLabel().getText();
        System.out.println(editableLabel.getLabel().getText());


        // Center the label within the circle.
        // This assumes you've implemented setCenteredPosition() in EditableLabel.
        editableLabel.setCenteredPosition(circle.getCenterX(), circle.getCenterY());

    }
}
