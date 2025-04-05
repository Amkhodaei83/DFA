package com.example.dfa_app.DFA;

import com.example.dfa_app.SelectionListener;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

/**
 * Represents a connection (transition) between two states.
 * This class maintains a CurvedArrow (for the visual representation) and an EditableLabel (for naming).
 * It contains no direct event handlers for selection/deselection—the control point's drag events are registered
 * when the Transition is selected and removed when it is deselected.
 */
public class Transition extends Group implements simularity {

    private final State fromState;
    private State toState;
    private final CurvedArrow curvedArrow;
    private final EditableLabel editableLabel;
    private boolean complete = false;

    // Used while drawing interactively.
    private double tempEndX;
    private double tempEndY;

    // Offset for computing a perpendicular control point.
    private static final double CONTROL_OFFSET = 40.0;
    private String symbol;
    private SelectionListener selectionListener;

    public Transition(State fromState) {
        if (fromState == null) {
            throw new IllegalArgumentException("fromState cannot be null.");
        }
        this.fromState = fromState;
        this.curvedArrow = new CurvedArrow();
        this.editableLabel = new EditableLabel();
        editableLabel.setText("");
        editableLabel.setVisible(true);

        // Enable key events.
        this.setFocusTraversable(true);

        // Add arrow and label.
        getChildren().addAll(curvedArrow, editableLabel);

        // If the parent of fromState is a Pane, add this Transition to it.
        if (fromState.getParent() instanceof Pane) {
            ((Pane) fromState.getParent()).getChildren().add(this);
        }

        // Listen for fromState layout changes.
        InvalidationListener layoutListener = obs -> updateTransition();
        fromState.layoutXProperty().addListener(layoutListener);
        fromState.layoutYProperty().addListener(layoutListener);

        updateTransition();
    }

    public CurvedArrow getCurvedArrow() {
        return curvedArrow;
    }

    // Allows updating a temporary endpoint while drawing.
    public void setTempEnd(double x, double y) {
        this.tempEndX = x;
        this.tempEndY = y;
        updateTransition();
    }

    // Updates the geometry of the arrow based on state positions and control point calculations.
    private void updateTransition() {
        double fromX = fromState.getLayoutX();
        double fromY = fromState.getLayoutY();
        double startX, startY, endX, endY, controlX, controlY, dx, dy, distance;

        if (complete && toState != null) {
            // For self-loop transitions.
            if (fromState == toState) {
                double radius = fromState.getMainCircle().getRadius();
                startX = fromX;
                startY = fromY - radius;
                endX = fromX + radius;
                endY = fromY;
                double midX = (startX + endX) / 2.0;
                double midY = (startY + endY) / 2.0;
                controlX = midX;
                controlY = midY - radius;
            } else {
                // For transitions between distinct states.
                double toX = toState.getLayoutX();
                double toY = toState.getLayoutY();
                dx = toX - fromX;
                dy = toY - fromY;
                distance = Math.hypot(dx, dy);
                if (distance == 0) { distance = 1; }
                double fromRadius = fromState.getMainCircle().getRadius();
                startX = fromX + (dx / distance) * fromRadius;
                startY = fromY + (dy / distance) * fromRadius;
                double toRadius = toState.getMainCircle().getRadius();
                endX = toX - (dx / distance) * toRadius;
                endY = toY - (dy / distance) * toRadius;
                double midX = (startX + endX) / 2.0;
                double midY = (startY + endY) / 2.0;
                double norm = Math.hypot(dx, dy);
                double perpX = -dy / norm;
                double perpY = dx / norm;
                double extraOffset = (int) (Math.random() * 201) - 100;
                controlX = midX + (CONTROL_OFFSET + extraOffset) * perpX;
                controlY = midY + (CONTROL_OFFSET + extraOffset) * perpY;
            }
        } else {
            // While drawing interactively.
            dx = tempEndX - fromX;
            dy = tempEndY - fromY;
            distance = Math.hypot(dx, dy);
            if (distance == 0) { distance = 1; }
            double fromRadius = fromState.getMainCircle().getRadius();
            startX = fromX + (dx / distance) * fromRadius;
            startY = fromY + (dy / distance) * fromState.getMainCircle().getRadius();
            endX = tempEndX;
            endY = tempEndY;
            double midX = (startX + endX) / 2.0;
            double midY = (startY + endY) / 2.0;
            double norm = Math.hypot(dx, dy);
            double perpX = -dy / norm;
            double perpY = dx / norm;
            double extraOffset = (int) (Math.random() * 201) - 100;
            controlX = midX + (CONTROL_OFFSET + extraOffset) * perpX;
            controlY = midY + (CONTROL_OFFSET + extraOffset) * perpY;
        }

        curvedArrow.setStart(startX, startY);
        curvedArrow.setEnd(endX, endY);
        curvedArrow.setControl(controlX, controlY);

        if (complete) {
            Platform.runLater(() -> {
                editableLabel.applyCss();
                editableLabel.layout();
                double labelWidth = editableLabel.getLabelWidth();
                double labelHeight = editableLabel.getLabelHeight();
                double[] tip = curvedArrow.getArrowTip();
                double labelX = tip[0] - labelWidth / 2.0;
                double labelY = tip[1] - labelHeight / 2.0;
                editableLabel.setLabelPosition(labelX, labelY);
                editableLabel.setEditorPosition(labelX, labelY);
            });
        }
    }

    // Completes the transition by attaching the target state.
    public void completeTransition(State targetState) {
        if (targetState == null) {
            throw new IllegalArgumentException("Target state cannot be null.");
        }
        this.toState = targetState;
        attemptFinalizeName();
        this.complete = true;
        curvedArrow.setComplete(true);

        InvalidationListener toListener = obs -> updateTransition();
        toState.layoutXProperty().addListener(toListener);
        toState.layoutYProperty().addListener(toListener);

        editableLabel.setVisible(true);
        FadeTransition ft = new FadeTransition(Duration.millis(300), editableLabel);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        updateTransition();
    }

    // Validates and finalizes the transition name.
    public void attemptFinalizeName() {
        String proposedName = editableLabel.getText();
        if (proposedName != null && !proposedName.trim().isEmpty()) {
            editableLabel.finalizeLabel();
            setSymbol(proposedName);
            System.out.println("Successful Transition Created: " + proposedName);
            curvedArrow.deselect();
        } else {
            showAlert("Invalid Transition Name", "Transition name cannot be empty. Please enter a valid name.");
            editableLabel.startEditing();
        }
    }

    public void setSymbol(String proposedName) {
        this.symbol = proposedName;
    }

    public String getSymbol() {
        return symbol;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public State getNextState() {
        return toState;
    }

    /**
     * When a Transition is selected, we want to both update visual style and allow the user
     * to drag the control point to adjust the curve. Therefore, we attach drag event handlers to
     * the control point here.
     */
    @Override
    public void select() {
        curvedArrow.select();
        registerControlPointDrag();
    }

    /**
     * When a Transition is deselected, we remove the drag event handlers from the control point.
     */
    @Override
    public void deselect() {
        curvedArrow.deselect();
        deregisterControlPointDrag();
    }

    public boolean isSelected() {
        return curvedArrow.isSelected();
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    /**
     * Attaches mouse event handlers to the control point so the user can drag it.
     */
    private void registerControlPointDrag() {
        CurvedArrow arrow = this.getCurvedArrow();
        Circle cp = arrow.getControlPoint();

        // Local array to track drag offset.
        final double[] dragDelta = new double[2];

        cp.setOnMousePressed((MouseEvent e) -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Point2D cpScene = cp.localToScene(cp.getCenterX(), cp.getCenterY());
                dragDelta[0] = e.getSceneX() - cpScene.getX();
                dragDelta[1] = e.getSceneY() - cpScene.getY();
                e.consume();
            }
        });

        cp.setOnMouseDragged((MouseEvent e) -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                double newSceneX = e.getSceneX() - dragDelta[0];
                double newSceneY = e.getSceneY() - dragDelta[1];
                // Convert scene coordinates into the arrow's local coordinate system.
                Point2D localPoint = arrow.sceneToLocal(newSceneX, newSceneY);
                arrow.setControl(localPoint.getX(), localPoint.getY());
                e.consume();
            }
        });

        cp.setOnMouseReleased((MouseEvent e) -> {
            e.consume();
        });
    }

    /**
     * Removes the mouse event handlers from the control point.
     */
    private void deregisterControlPointDrag() {
        CurvedArrow arrow = this.getCurvedArrow();
        Circle cp = arrow.getControlPoint();
        cp.setOnMousePressed(null);
        cp.setOnMouseDragged(null);
        cp.setOnMouseReleased(null);
    }
}
