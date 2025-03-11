package com.example.dfa_app.DFA;

import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;

public class CurvedArrow {
    private Pane pane;

    private QuadCurve curve;
    private Polygon arrowHead;
    private Circle controlPoint;
    private EditableLabel editableLabel;

    private double startX, startY;
    private boolean controlDragging = false;
    private boolean selected = false;

    public CurvedArrow(double startX, double startY, Pane pane) {
        this.startX = startX;
        this.startY = startY;
        this.pane = pane;

        // Initialize the curve.
        curve = new QuadCurve();
        curve.setStartX(startX);
        curve.setStartY(startY);
        curve.setControlX(startX);
        curve.setControlY(startY);
        curve.setEndX(startX);
        curve.setEndY(startY);
        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(2);
        curve.setFill(null);
        curve.setPickOnBounds(true);
        pane.getChildren().add(curve);

        // Initialize the arrow head.
        arrowHead = new Polygon();
        arrowHead.setFill(Color.BLACK);

        // Initialize the editable label.
        editableLabel = new EditableLabel(pane);
        editableLabel.setPosition(startX, startY);

        // Initialize the control point.
        controlPoint = new Circle(5, Color.RED);
        controlPoint.setStroke(Color.BLACK);
        controlPoint.setStrokeWidth(2);
        controlPoint.setVisible(false);

        addEventHandlers();
    }

    private void addEventHandlers() {
        // Allow dragging the control point to adjust the curve.
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

        // Left-click on the curve to deselect if already selected.
        curve.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && selected) {
                deselect();
            }
        });
    }

    /**
     * Updates the end point of the arrow. Also positions the control point
     * at the midpoint between start and end so the curve is initially symmetrical.
     */
    public void updateEndPoint(double x, double y) {
        curve.setEndX(x);
        curve.setEndY(y);

        double midX = (startX + x) / 2;
        double midY = (startY + y) / 2;
        curve.setControlX(midX);
        curve.setControlY(midY);
        controlPoint.setCenterX(midX);
        controlPoint.setCenterY(midY);

        updateArrowHead();
        updateLabel();
    }

    /**
     * Updates the control point, which alters the curvature.
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
     * Computes the position and orientation of the arrow head,
     * then updates the display.
     */
    public void updateArrowHead() {
        pane.getChildren().remove(arrowHead);

        // Use parameter t=0.5 to place the arrow head at the midpoint of the curve.
        double t = 0.5;
        double x = Math.pow(1 - t, 2) * curve.getStartX()
                + 2 * (1 - t) * t * curve.getControlX()
                + Math.pow(t, 2) * curve.getEndX();
        double y = Math.pow(1 - t, 2) * curve.getStartY()
                + 2 * (1 - t) * t * curve.getControlY()
                + Math.pow(t, 2) * curve.getEndY();

        // Calculate derivative to get the angle.
        double dx = 2 * (1 - t) * (curve.getControlX() - curve.getStartX())
                + 2 * t * (curve.getEndX() - curve.getControlX());
        double dy = 2 * (1 - t) * (curve.getControlY() - curve.getStartY())
                + 2 * t * (curve.getEndY() - curve.getControlY());
        double angle = Math.atan2(dy, dx);

        double arrowLength = 15;
        double arrowWidth = 10;
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double x1 = x - arrowLength * cos + arrowWidth * sin;
        double y1 = y - arrowLength * sin - arrowWidth * cos;
        double x2 = x - arrowLength * cos - arrowWidth * sin;
        double y2 = y - arrowLength * sin + arrowWidth * cos;

        arrowHead.getPoints().setAll(x, y, x1, y1, x2, y2);
        pane.getChildren().add(arrowHead);

        // Bring label to the front.
        editableLabel.getLabel().toFront();
        editableLabel.getLabelEditor().toFront();
    }

    /**
     * Positions the label at the midpoint of the curve.
     */
    public void updateLabel() {
        double t = 0.5;
        double x = Math.pow(1 - t, 2) * curve.getStartX()
                + 2 * (1 - t) * t * curve.getControlX()
                + Math.pow(t, 2) * curve.getEndX();
        double y = Math.pow(1 - t, 2) * curve.getStartY()
                + 2 * (1 - t) * t * curve.getControlY()
                + Math.pow(t, 2) * curve.getEndY();
        editableLabel.setCenteredPosition(x, y);
    }

    /**
     * Called when the arrow is complete. Fixes the arrow head and label.
     */
    public void finalizeArrow() {
        updateArrowHead();
        updateLabel();
        editableLabel.finalizeLabel();
    }

    /**
     * Marks this arrow as selected, changes its stroke color, shows the control point,
     * and brings up the label editor.
     */
    public void select() {
        selected = true;
        curve.setStroke(Color.BLUE);
        controlPoint.setVisible(true);
        if (!pane.getChildren().contains(controlPoint)) {
            pane.getChildren().add(controlPoint);
        }
        pane.setCursor(Cursor.HAND);
        curve.toFront();
        arrowHead.toFront();
        controlPoint.toFront();
        editableLabel.getLabel().toFront();
        editableLabel.getLabelEditor().toFront();
        editableLabel.showEditor();
    }

    /**
     * Deselects the arrow, reverting stroke color and hiding the control point and label editor.
     */
    public void deselect() {
        selected = false;
        curve.setStroke(Color.BLACK);
        controlPoint.setVisible(false);
        pane.getChildren().remove(controlPoint);
        pane.setCursor(Cursor.DEFAULT);
        editableLabel.hideEditor();
    }

    /**
     * Returns whether the control point is currently being dragged.
     */
    public boolean isControlDragging() {
        return controlDragging;
    }

    /**
     * Removes the entire arrow (curve, arrow head, control point, and label) from the pane.
     */
    public void remove() {
        pane.getChildren().remove(curve);
        pane.getChildren().remove(arrowHead);
        pane.getChildren().remove(controlPoint);
        editableLabel.getLabel().setVisible(false);
        editableLabel.getLabelEditor().setVisible(false);
    }
}
