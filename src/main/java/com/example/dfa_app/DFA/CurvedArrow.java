package com.example.dfa_app.DFA;

import javafx.animation.ScaleTransition;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.util.Duration;

/**
 * A visual component that draws a curved arrow with an arrowhead and a control point.
 * • The arrow head is marked with a style class but has no events attached.
 * • The control point is exposed (via getControlPoint) for external drag handling.
 */
public class CurvedArrow extends Group {

    private final QuadCurve curve;
    private final Polygon arrowHead;
    private final Circle controlPoint;

    private double startX, startY;
    private double controlX, controlY;
    private double endX, endY;
    private double arrowTipX, arrowTipY;

    private boolean selected = false;
    private boolean complete = false;

    // Constants for arrowhead dimensions.
    private static final double ARROW_LENGTH = 15.0;
    private static final double ARROW_WIDTH = 10.0;

    public CurvedArrow() {
        // Create the curve.
        curve = new QuadCurve();
        curve.setStroke(Color.BLACK);
        curve.setStrokeWidth(2);
        curve.setFill(Color.TRANSPARENT);
        curve.setPickOnBounds(true);

        // Create the arrow head and mark it so the controller later can detect clicks on it.
        arrowHead = new Polygon();
        arrowHead.setFill(Color.BLACK);
        // Mark the arrow head with a style class and/or user data:
        arrowHead.getStyleClass().add("arrow-head");
        // DO NOT attach any event handlers here.

        // Create the control point.
        controlPoint = new Circle(5, Color.RED);
        controlPoint.setStroke(Color.BLACK);
        controlPoint.setStrokeWidth(2);
        controlPoint.setVisible(false); // hidden by default; shown when needed

        // Add all components to this group.
        getChildren().addAll(curve, arrowHead, controlPoint);

        // The curve should not capture mouse events.
        curve.setMouseTransparent(true);
    }

    // Expose the control point (for attaching drag events externally).
    public Circle getControlPoint() {
        return controlPoint;
    }

    // Expose the arrow head (so that higher‑level event handlers can detect clicks on it).
    public Polygon getArrowHead() {
        return arrowHead;
    }

    // Setters for the start, end, and control positions.
    public void setStart(double x, double y) {
        this.startX = x;
        this.startY = y;
        curve.setStartX(x);
        curve.setStartY(y);
        updateArrowHead();
    }

    public void setEnd(double x, double y) {
        this.endX = x;
        this.endY = y;
        curve.setEndX(x);
        curve.setEndY(y);
        updateArrowHead();
    }

    public void setControl(double x, double y) {
        this.controlX = x;
        this.controlY = y;
        curve.setControlX(x);
        curve.setControlY(y);
        controlPoint.setCenterX(x);
        controlPoint.setCenterY(y);
        updateArrowHead();
    }

    // Given a parameter t, computes the point on the quadratic curve.
    private double[] getCurvePoint(double t) {
        double oneMinusT = 1 - t;
        double x = oneMinusT * oneMinusT * startX + 2 * oneMinusT * t * controlX + t * t * endX;
        double y = oneMinusT * oneMinusT * startY + 2 * oneMinusT * t * controlY + t * t * endY;
        return new double[]{x, y};
    }

    // Computes the derivative at parameter t (for the tangent).
    private double[] getCurveDerivative(double t) {
        double oneMinusT = 1 - t;
        double dx = 2 * oneMinusT * (controlX - startX) + 2 * t * (endX - controlX);
        double dy = 2 * oneMinusT * (controlY - startY) + 2 * t * (endY - controlY);
        return new double[]{dx, dy};
    }

    // Updates the arrow head’s geometry based on the curve’s midpoint.
    public void updateArrowHead() {
        double t = 0.5;
        double[] midPoint = getCurvePoint(t);
        arrowTipX = midPoint[0];
        arrowTipY = midPoint[1];

        double[] derivative = getCurveDerivative(t);
        double angle = Math.atan2(derivative[1], derivative[0]);

        double baseX = arrowTipX - ARROW_LENGTH * Math.cos(angle);
        double baseY = arrowTipY - ARROW_LENGTH * Math.sin(angle);
        double leftX = baseX + ARROW_WIDTH * Math.sin(angle);
        double leftY = baseY - ARROW_WIDTH * Math.cos(angle);
        double rightX = baseX - ARROW_WIDTH * Math.sin(angle);
        double rightY = baseY + ARROW_WIDTH * Math.cos(angle);
        arrowHead.getPoints().setAll(arrowTipX, arrowTipY, leftX, leftY, rightX, rightY);

        if (complete) {
            animateArrowHead();
        }
    }

    // Plays a pulse animation on the arrow head when finalized.
    private void animateArrowHead() {
        ScaleTransition st = new ScaleTransition(Duration.millis(200), arrowHead);
        st.setFromX(1);
        st.setFromY(1);
        st.setToX(1.7);
        st.setToY(1.7);
        st.setCycleCount(2);
        st.setAutoReverse(true);
        st.play();
    }

    // Returns the arrow tip coordinates.
    public double[] getArrowTip() {
        return new double[]{arrowTipX, arrowTipY};
    }

    // Called externally to mark selection.
    public void select() {
        selected = true;
        curve.setStroke(Color.BLUE);
        controlPoint.setVisible(true); // show control point when selected
        setArrow(Cursor.HAND);
        toFront();
    }

    // Called externally to deselect.
    public void deselect() {
        selected = false;
        curve.setStroke(Color.BLACK);
        controlPoint.setVisible(false);
        setArrow(Cursor.DEFAULT);
    }

    public boolean isSelected() {
        return selected;
    }

    // Sets the cursor on this group and its relevant child nodes.
    private void setArrow(Cursor cursor) {
        this.setArrow(cursor);
        curve.setCursor(cursor);
        controlPoint.setCursor(cursor);
        // Do not change the arrow head’s cursor so that it remains purely decorative.
    }

    // Mark the arrow as complete (to trigger animation, etc.).
    public void setComplete(boolean complete) {
        this.complete = complete;
        updateArrowHead();
    }
}
