package com.example.dfa_app.Arrow;// Arrow.java

import javafx.beans.value.ChangeListener;
import javafx.scene.Cursor;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.scene.text.Text;

public class Arrow {
    private Pane pane;
    private CurvedArrowDrawer drawer;

    private QuadCurve curve;
    private Polygon arrowHead;
    private Circle controlPoint;
    private Text label;
    private TextField labelEditor;

    private double startX, startY;
    private boolean controlPointDragging = false;
    private boolean selected = false;

    public Arrow(double startX, double startY, Pane pane, CurvedArrowDrawer drawer) {
        this.startX = startX;
        this.startY = startY;
        this.pane = pane;
        this.drawer = drawer;

        // Initialize curve
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
        curve.setPickOnBounds(true); // Allow hit detection

        // **Add the curve to the pane**
        pane.getChildren().add(curve);

        // Initialize arrowhead
        arrowHead = new Polygon();
        arrowHead.setFill(Color.BLACK);

        // Initialize label and label editor
        label = new Text("");
        label.setMouseTransparent(true); // To prevent label from capturing mouse events

        labelEditor = new TextField();
        labelEditor.setVisible(false);
        pane.getChildren().add(labelEditor);

        // Initialize control point
        controlPoint = new Circle(5, Color.RED);
        controlPoint.setStroke(Color.BLACK);
        controlPoint.setStrokeWidth(2);
        controlPoint.setVisible(false); // Initially hidden

        // Add event handlers
        addEventHandlers();
    }

    private void addEventHandlers() {
        // Handle control point dragging
        controlPoint.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                controlPointDragging = true;
                e.consume();
            }
        });

        controlPoint.setOnMouseDragged(e -> {
            if (controlPointDragging && e.getButton() == MouseButton.PRIMARY) {
                updateControlPoint(e.getX(), e.getY());
                e.consume();
            }
        });

        controlPoint.setOnMouseReleased(e -> {
            controlPointDragging = false;
            e.consume();
        });

        // Handle curve selection
        curve.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                drawer.deselectAllArrows();
                select();
                drawer.setCurrentArrow(this);
                e.consume();
            }
        });
    }

    public void updateEndPoint(double x, double y) {
        curve.setEndX(x);
        curve.setEndY(y);

        // Update control point to midpoint
        double midX = (startX + x) / 2;
        double midY = (startY + y) / 2;
        curve.setControlX(midX);
        curve.setControlY(midY);
        controlPoint.setCenterX(midX);
        controlPoint.setCenterY(midY);

        updateArrowHead();
        updateLabelPosition();
    }

    public void updateControlPoint(double x, double y) {
        curve.setControlX(x);
        curve.setControlY(y);
        controlPoint.setCenterX(x);
        controlPoint.setCenterY(y);

        updateArrowHead();
        updateLabelPosition();
    }

    public void updateArrowHead() {
        // Remove previous arrowhead
        pane.getChildren().remove(arrowHead);

        // Calculate position along the curve
        double t = 0.5; // Midpoint of the curve
        double x = Math.pow(1 - t, 2) * curve.getStartX()
                + 2 * (1 - t) * t * curve.getControlX()
                + Math.pow(t, 2) * curve.getEndX();
        double y = Math.pow(1 - t, 2) * curve.getStartY()
                + 2 * (1 - t) * t * curve.getControlY()
                + Math.pow(t, 2) * curve.getEndY();

        // Calculate derivative at point t for angle
        double dx = 2 * (1 - t) * (curve.getControlX() - curve.getStartX())
                + 2 * t * (curve.getEndX() - curve.getControlX());
        double dy = 2 * (1 - t) * (curve.getControlY() - curve.getStartY())
                + 2 * t * (curve.getEndY() - curve.getControlY());
        double angle = Math.atan2(dy, dx);

        // Define arrowhead size
        double arrowLength = 15;
        double arrowWidth = 10;

        // Calculate points for arrowhead
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        double x1 = x - arrowLength * cos + arrowWidth * sin;
        double y1 = y - arrowLength * sin - arrowWidth * cos;

        double x2 = x - arrowLength * cos - arrowWidth * sin;
        double y2 = y - arrowLength * sin + arrowWidth * cos;

        // Update arrowhead polygon
        arrowHead.getPoints().setAll(
                x, y,
                x1, y1,
                x2, y2
        );

        pane.getChildren().add(arrowHead);

        // Bring label and editor to front
        label.toFront();
        labelEditor.toFront();
    }

    public void updateLabelPosition() {
        // Position label at the midpoint of the curve
        double t = 0.5; // Midpoint
        double x = Math.pow(1 - t, 2) * curve.getStartX()
                + 2 * (1 - t) * t * curve.getControlX()
                + Math.pow(t, 2) * curve.getEndX();
        double y = Math.pow(1 - t, 2) * curve.getStartY()
                + 2 * (1 - t) * t * curve.getControlY()
                + Math.pow(t, 2) * curve.getEndY();

        label.setX(x + 10); // Offset label slightly from the curve
        label.setY(y - 10);

        labelEditor.setLayoutX(x + 10);
        labelEditor.setLayoutY(y - 10 - label.getBoundsInLocal().getHeight());
    }

    public void finalizeArrow() {
        updateArrowHead();
        updateLabelPosition();
        // Add label to pane
        pane.getChildren().add(label);
    }

    public void select() {
        selected = true;
        curve.setStroke(Color.BLUE); // Highlight selected curve
        controlPoint.setVisible(true);
        pane.getChildren().add(controlPoint);

        // Change cursor to indicate editability
        pane.setCursor(Cursor.HAND);

        // Bring elements to front
        curve.toFront();
        arrowHead.toFront();
        controlPoint.toFront();
        label.toFront();
        labelEditor.toFront();

        // Show label editor
        showLabelEditor();
    }

    public void deselect() {
        selected = false;
        curve.setStroke(Color.BLACK);
        controlPoint.setVisible(false);
        pane.getChildren().remove(controlPoint);

        // Reset cursor
        pane.setCursor(Cursor.DEFAULT);

        // Hide label editor
        hideLabelEditor();
    }

    public void stopDraggingControlPoint() {
        controlPointDragging = false;
    }

    public boolean isControlPointDragging() {
        return controlPointDragging;
    }

    public boolean isSelected() {
        return selected;
    }

    public void removeFromPane() {
        pane.getChildren().removeAll(curve, arrowHead, label, labelEditor, controlPoint);
    }

    public double distanceToPoint(double x, double y) {
        int samplingPoints = 100;
        double minDist = Double.MAX_VALUE;
        for (int i = 0; i <= samplingPoints; i++) {
            double t = i / (double) samplingPoints;
            double curveX = Math.pow(1 - t, 2) * curve.getStartX()
                    + 2 * (1 - t) * t * curve.getControlX()
                    + Math.pow(t, 2) * curve.getEndX();
            double curveY = Math.pow(1 - t, 2) * curve.getStartY()
                    + 2 * (1 - t) * t * curve.getControlY()
                    + Math.pow(t, 2) * curve.getEndY();
            double dist = Math.hypot(curveX - x, curveY - y);
            if (dist < minDist) {
                minDist = dist;
            }
        }
        return minDist;
    }

    public void showLabelEditor() {
        labelEditor.setText(label.getText());
        labelEditor.setLayoutX(label.getX());
        labelEditor.setLayoutY(label.getY() - label.getBoundsInLocal().getHeight());
        labelEditor.setVisible(true);
        labelEditor.requestFocus();

        // Update the label text as the user types
        labelEditor.textProperty().addListener(labelEditorListener);

        // Move the label editor if the label position changes
        label.xProperty().addListener(labelXListener);
        label.yProperty().addListener(labelYListener);
    }

    public void hideLabelEditor() {
        labelEditor.setVisible(false);

        // Remove listeners to prevent memory leaks
        labelEditor.textProperty().removeListener(labelEditorListener);
        label.xProperty().removeListener(labelXListener);
        label.yProperty().removeListener(labelYListener);
    }

    // Listeners for label editor and label position
    private ChangeListener<String> labelEditorListener = (observable, oldValue, newValue) -> {
        label.setText(newValue);
    };

    private ChangeListener<Number> labelXListener = (obs, oldVal, newVal) -> {
        labelEditor.setLayoutX(newVal.doubleValue());
    };
    private ChangeListener<Number> labelYListener = (obs, oldVal, newVal) -> {
        labelEditor.setLayoutY(newVal.doubleValue() - label.getBoundsInLocal().getHeight());
    };

    public QuadCurve getCurve() {
        return curve;
    }

    public boolean containsPoint(double x, double y) {
        return curve.contains(x, y);
    }
}
