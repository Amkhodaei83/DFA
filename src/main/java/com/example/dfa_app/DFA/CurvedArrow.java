package com.example.dfa_app.DFA;

import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;

/**
 * CurvedArrow visually represents a transition between DFA states.
 * It displays a quadratic curve with an arrow head, an adjustable control point,
 * and an editable label. All visual elements are encapsulated by extending Group,
 * so the arrow can be added directly to a scene graph.
 */
public class CurvedArrow extends Group {

    private final QuadCurve curve;
    private final Polygon arrowHead;
    private final Circle controlPoint;
    private final EditableLabel editableLabel;

    private final double startX, startY;
    private boolean controlDragging = false;
    private boolean selected = false;

    /**
     * Creates a new CurvedArrow starting from the given state's center.
     * All visual components are created and added to this group.
     *
     * @param state the state from which to start the arrow; must not be null.
     * @throws IllegalArgumentException if state is null.
     */
    public CurvedArrow(State state) {
        if (state == null) {
            throw new IllegalArgumentException("State cannot be null.");
        }

        // Capture the starting point based on the state's circle.
        this.startX = state.getCircle().getCenterX();
        this.startY = state.getCircle().getCenterY();

        // Initialize and configure the quadratic curve.
        curve = new QuadCurve();
        curve.setStartX(startX);
        curve.setStartY(startY);
        // Initially set control and end points to the state's center.
        curve.setControlX(startX);
        curve.setControlY(startY);
        curve.setEndX(startX);
        curve.setEndY(startY);
        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(2);
        curve.setFill(null);
        curve.setPickOnBounds(true);
        getChildren().add(curve);

        // Initialize the arrow head.
        arrowHead = new Polygon();
        arrowHead.setFill(Color.BLACK);
        getChildren().add(arrowHead);

        // Initialize the editable label.
        editableLabel = new EditableLabel();
        // Position the label offscreen initially.
        editableLabel.setTranslateX(startX + 5000);
        editableLabel.setTranslateY(startY + 10000);
        getChildren().add(editableLabel);

        // Initialize the control point for curve adjustment.
        controlPoint = new Circle(5, Color.RED);
        controlPoint.setStroke(Color.BLACK);
        controlPoint.setStrokeWidth(2);
        controlPoint.setVisible(false);
        getChildren().add(controlPoint);

        // Set up mouse event handlers.
        addEventHandlers();

        System.out.println("CurvedArrow created.");
    }

    /**
     * Sets up mouse event handlers for curve adjustments and selection.
     */
    public void addEventHandlers() {
        // Handle dragging of the control point.
        controlPoint.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                controlDragging = true;
                e.consume();
            }
        });

        controlPoint.setOnMouseDragged(e -> {
            if (controlDragging && e.getButton() == MouseButton.PRIMARY) {
                updateControlPoint(e.getX(), e.getY());
                e.consume();
            }
        });

        controlPoint.setOnMouseReleased(e -> {
            controlDragging = false;
            e.consume();
        });

        // Right-click on the curve selects the arrow.
        curve.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                select();
                e.consume();
            }
        });

        // Left-click on the curve deselects it if already selected.
        curve.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && selected) {
                deselect();
            }
        });
    }

    /**
     * Updates the end point of the arrow to the center of the provided state.
     * Also resets the control point to the midpoint between start and end.
     *
     * @param state the state that defines the arrow's endpoint.
     */
    public void updateEndPoint(State state) {
        double endX = state.getCircle().getCenterX();
        double endY = state.getCircle().getCenterY();
        curve.setEndX(endX);
        curve.setEndY(endY);

        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;
        curve.setControlX(midX);
        curve.setControlY(midY);
        controlPoint.setCenterX(midX);
        controlPoint.setCenterY(midY);

        updateArrowHead();
        updateLabel();
        System.out.println("Arrow end point updated.");
    }

    /**
     * Updates the control point to the specified coordinates,
     * adjusting the curve's shape accordingly.
     *
     * @param x the new X-coordinate for the control point.
     * @param y the new Y-coordinate for the control point.
     */
    public void updateControlPoint(double x, double y) {
        curve.setControlX(x);
        curve.setControlY(y);
        controlPoint.setCenterX(x);
        controlPoint.setCenterY(y);

        updateArrowHead();
        updateLabel();
    }

    /**
     * Recalculates the position and orientation of the arrow head based on the curve.
     */
    public void updateArrowHead() {
        // Remove the current arrow head from the group.
        getChildren().remove(arrowHead);

        double t = 0.5; // Use the midpoint on the curve.
        double[] point = getCurvePoint(t);
        double[] derivative = getCurveDerivative(t);

        double angle = Math.atan2(derivative[1], derivative[0]);
        double arrowLength = 15;
        double arrowWidth = 10;

        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double x1 = point[0] - arrowLength * cos + arrowWidth * sin;
        double y1 = point[1] - arrowLength * sin - arrowWidth * cos;
        double x2 = point[0] - arrowLength * cos - arrowWidth * sin;
        double y2 = point[1] - arrowLength * sin + arrowWidth * cos;

        arrowHead.getPoints().setAll(point[0], point[1], x1, y1, x2, y2);
        getChildren().add(arrowHead);

        // Ensure the editable label is rendered on top.
        editableLabel.toFront();
    }

    /**
     * Positions the editable label near the midpoint of the curve.
     */
    public void updateLabel() {
        double t = 0.5;
        double[] point = getCurvePoint(t);

        // Offsets can be adjusted as needed.
        double offsetX = 20;
        double offsetY = -20;

        editableLabel.setTranslateX(point[0] + offsetX);
        editableLabel.setTranslateY(point[1] + offsetY);
    }

    /**
     * Finalizes the arrow by updating the arrow head, label, and finalizing the label's display.
     */
    public void finalizeArrow() {
        updateArrowHead();
        updateLabel();
        editableLabel.finalizeLabel();
    }

    /**
     * Marks this arrow as selected: changes stroke color, shows the control point,
     * brings it to the front, and enters label editing mode.
     */
    public void select() {
        selected = true;
        curve.setStroke(Color.BLUE);
        controlPoint.setVisible(true);
        if (!getChildren().contains(controlPoint)) {
            getChildren().add(controlPoint);
        }
        setCursor(Cursor.HAND);
        // Bring components to the front.
        curve.toFront();
        arrowHead.toFront();
        controlPoint.toFront();
        editableLabel.toFront();
        editableLabel.startEditing();
    }

    /**
     * Deselects the arrow, reverting its stroke and hiding the control point and label editor.
     */
    public void deselect() {
        selected = false;
        curve.setStroke(Color.BLACK);
        controlPoint.setVisible(false);
        getChildren().remove(controlPoint);
        setCursor(Cursor.DEFAULT);
        editableLabel.stopEditing();
    }

    /**
     * Returns whether the control point is currently being dragged.
     *
     * @return true if the control point is being dragged; false otherwise.
     */
    public boolean isControlDragging() {
        return controlDragging;
    }

    /**
     * Removes all visual elements of the arrow from this group.
     * After removal, the CurvedArrow should not be used further.
     */
    public void remove() {
        getChildren().removeAll(curve, arrowHead, controlPoint);
        editableLabel.setVisible(false);
    }

    // ----------------------------------------------------------------------
    // Private helper methods for curve calculations.
    // ----------------------------------------------------------------------

    /**
     * Computes and returns the point [x,y] on the quadratic curve at the given parameter t.
     *
     * @param t the parameter (between 0 and 1); for t=0.5 this returns the midpoint.
     * @return an array with two elements: {x, y}.
     */
    private double[] getCurvePoint(double t) {
        double x = Math.pow(1 - t, 2) * curve.getStartX()
                + 2 * (1 - t) * t * curve.getControlX()
                + Math.pow(t, 2) * curve.getEndX();
        double y = Math.pow(1 - t, 2) * curve.getStartY()
                + 2 * (1 - t) * t * curve.getControlY()
                + Math.pow(t, 2) * curve.getEndY();
        return new double[] { x, y };
    }

    /**
     * Computes and returns the derivative [dx,dy] of the quadratic curve at the given parameter t.
     *
     * @param t the parameter (between 0 and 1).
     * @return an array with two elements: {dx, dy}.
     */
    private double[] getCurveDerivative(double t) {
        double dx = 2 * (1 - t) * (curve.getControlX() - curve.getStartX())
                + 2 * t * (curve.getEndX() - curve.getControlX());
        double dy = 2 * (1 - t) * (curve.getControlY() - curve.getStartY())
                + 2 * t * (curve.getEndY() - curve.getControlY());
        return new double[] { dx, dy };
    }
}
