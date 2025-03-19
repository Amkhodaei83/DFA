package com.example.dfa_app.DFA;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.QuadCurve;
import javafx.util.Duration;


// Note: The classes State and EditableLabel must be defined elsewhere.
// For example, assume State provides getLayoutX(), getLayoutY(), getMainCircle(), etc.
// And assume EditableLabel provides methods such as:
//    setText(String), getText(), applyCss(), layout(), getLabelWidth(), getLabelHeight(),
//    setLabelPosition(double, double), setEditorPosition(double, double),
//    getEditor(), startEditing(), finalizeLabel(), etc.

public class Transition extends Group {
    // In Transition class (outside any method)
    private static CurvedArrow selectedArrow = null;
    private final State fromState;
    private State toState;
    private final CurvedArrow curvedArrow;
    private final EditableLabel editableLabel;
    private boolean complete = false;

    // Temporary endpoint used during interactive drawing.
    private double tempEndX;
    private double tempEndY;

    // Controls the offset for the curve's control point.
    private static final double CONTROL_OFFSET = 40.0;
    private String symbol;

    /**
     * Constructs a Transition originating from the given state.
     *
     * @param fromState the originating state (must not be null)
     */
    public Transition(State fromState) {
        if (fromState == null) {
            throw new IllegalArgumentException("fromState cannot be null.");
        }
        this.fromState = fromState;
        this.curvedArrow = new CurvedArrow();
        this.editableLabel = new EditableLabel();
        editableLabel.setText("");
        editableLabel.setVisible(false);

        // Ensure the Transition can receive key events.
        this.setFocusTraversable(true);

        // Add both the arrow and label to this group.
        getChildren().addAll(curvedArrow, editableLabel);

        // Add this Transition to the same Pane that contains fromState (if any).
        if (fromState.getParent() instanceof Pane) {
            ((Pane) fromState.getParent()).getChildren().add(this);
        }

        // Listen for movement of the fromState.
        InvalidationListener layoutListener = obs -> updateTransition();
        fromState.layoutXProperty().addListener(layoutListener);
        fromState.layoutYProperty().addListener(layoutListener);




        curvedArrow.select();


        // When the Transition is focused (after selection), pressing ENTER deselects the arrow.
        this.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER && curvedArrow.isSelected()) {
                attemptFinalizeName();
                curvedArrow.deselect();
                e.consume();
            }
        });

        updateTransition();
    }

    /**
     * Updates the geometry of the arrow (start, end, control point) and repositions the label.
     * The label is positioned so that its center aligns with the arrowhead tip.
     */
    public void updateTransition() {
        double fromX = fromState.getLayoutX();
        double fromY = fromState.getLayoutY();
        double startX, startY, endX, endY, controlX, controlY, dx, dy, distance;

        if (complete && toState != null) {
            // Handle cycle (self-loop) transition
            if (fromState == toState) {
                // --- Self-loop geometry ---
                // Use the state's center and radius to define the loop.
                double radius = fromState.getMainCircle().getRadius();
                // Choose two distinct points on the circle.
                // For example, let the arrow leave at the top and re-enter at the right.
                startX = fromX;
                startY = fromY - radius;    // Top of the state
                endX = fromX + radius;
                endY = fromY;               // Right of the state

                // The control point is chosen to create a nice loop.
                // Here, we take the midpoint and offset it upward by an amount equal to the radius.
                double midX = (startX + endX) / 2.0;
                double midY = (startY + endY) / 2.0;
                controlX = midX;
                controlY = midY - radius;    // You can adjust this value for a shallower or steeper loop.

            } else {
                // --- Normal transition between two distinct states ---
                double toX = toState.getLayoutX();
                double toY = toState.getLayoutY();
                dx = toX - fromX;
                dy = toY - fromY;
                distance = Math.hypot(dx, dy);
                if (distance == 0) {
                    distance = 1;
                }
                double fromRadius = fromState.getMainCircle().getRadius();
                startX = fromX + (dx / distance) * fromRadius;
                startY = fromY + (dy / distance) * fromState.getMainCircle().getRadius();

                double toRadius = toState.getMainCircle().getRadius();
                endX = toX - (dx / distance) * toRadius;
                endY = toY - (dy / distance) * toRadius;

                // Compute the control point using the perpendicular offset.
                double midX = (startX + endX) / 2.0;
                double midY = (startY + endY) / 2.0;
                // Determine unit perpendicular via (–dy, dx).
                double norm = Math.hypot(dx, dy);
                double perpX = -dy / norm;
                double perpY = dx / norm;
                // Add a little randomness or extra offset so that multiple arrows vary.
                double extraOffset = (int) (Math.random() * 201) - 100; // random value between -100 and 100
                controlX = midX + (CONTROL_OFFSET + extraOffset) * perpX;
                controlY = midY + (CONTROL_OFFSET + extraOffset) * perpY;
            }
        } else {
            // --- While drawing interactively ---
            dx = tempEndX - fromX;
            dy = tempEndY - fromY;
            distance = Math.hypot(dx, dy);
            if (distance == 0) {
                distance = 1;
            }
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

        // Update arrow geometry.
        curvedArrow.setStart(startX, startY);
        curvedArrow.setEnd(endX, endY);
        curvedArrow.setControl(controlX, controlY);
        editableLabel.startEditing();

        // If the transition is complete, reposition the label to align with the arrow tip.
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


    /**
     * While drawing interactively, updates the temporary endpoint.
     *
     * @param mouseX current mouse X coordinate.
     * @param mouseY current mouse Y coordinate.
     */
    public void updateTemporaryEndpoint(double mouseX, double mouseY) {
        if (!complete) {
            tempEndX = mouseX;
            tempEndY = mouseY;
            updateTransition();
        }
    }

    /**
     * Completes the transition by setting the destination state.
     * Also makes the label visible and plays a fade–in animation.
     *
     * @param targetState the destination state (must not be null)
     */
    public void completeTransition(State targetState) {
        if (targetState == null) {
            throw new IllegalArgumentException("Target state cannot be null.");
        }
        this.toState = targetState;
        this.complete = true;

        InvalidationListener toListener = obs -> updateTransition();
        toState.layoutXProperty().addListener(toListener);
        toState.layoutYProperty().addListener(toListener);

        editableLabel.setVisible(true);

        // Fade transition for the label.
        FadeTransition ft = new FadeTransition(Duration.millis(300), editableLabel);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();

        // Attach the ENTER key handler to attempt finalizing the name.
        editableLabel.getEditor().setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER ) {
                attemptFinalizeName();
                e.consume();
            }
        });
        updateTransition();
    }

    /**
     * Helper method for finalizing the label name.
     * It only requires that the name is not empty.
     * After a successful finalization, a log is printed to indicate a successful transition.
     */
    private void attemptFinalizeName() {
        String proposedName = editableLabel.getText();
        if (proposedName != null && !proposedName.trim().isEmpty()) {
            editableLabel.finalizeLabel();
            setSymbol(proposedName);
            logSuccessfulTransition(proposedName);
            System.out.println("Name set to: " + proposedName);
            // Remove any residual ENTER key handler.
            editableLabel.getEditor().setOnAction(null);
            curvedArrow.deselect();
        } else {
            showAlert("Invalid State Name", "State name cannot be empty. Please enter a valid name.");
            editableLabel.startEditing();
            editableLabel.getEditor().setOnAction(e -> attemptFinalizeName());
        }
    }

    private void setSymbol(String proposedName) {
        this.symbol = proposedName;
    }

    /**
     * Logs a successful transition creation.
     * Modify this method to integrate with your logging infrastructure, if needed.
     *
     * @param symbol the finalized transition name.
     */
    private void logSuccessfulTransition(String symbol) {
        // Here we're simply printing to the console.
        // You might choose to write to a log file or external logging system.
        System.out.println("Successful Transition Created: " + symbol);
    }

    /**
     * Displays an alert with the specified title and message.
     *
     * @param title   the alert title.
     * @param message the alert message.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Object getNextState() {
        return toState;
    }

    public String getSymbol() {
        return symbol;
    }

    // --------------------------------------------------------------------------
    // Inner Class: CurvedArrow
    // --------------------------------------------------------------------------
    /**
     * Draws a quadratic curve between two points with an arrowhead at its midpoint.
     * The right–click event is attached exclusively to the arrowhead.
     * A pulse animation is played on the arrowhead whenever its position is updated.
     * Additionally, if the arrowhead is left–clicked while selected or if the ENTER key
     * is pressed (with the Transition focused), the arrow is deselected.
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
        private boolean controlDragging = false;

        // Constants for the arrowhead's size.
        private static final double ARROW_LENGTH = 15.0;
        private static final double ARROW_WIDTH = 10.0;

        /**
         * Constructs the curved arrow, initializing the curve, arrowhead, and control point.
         * Installs the dedicated mouse event handlers.
         */
        public CurvedArrow() {
            curve = new QuadCurve();
            curve.setStroke(Color.BLACK);
            curve.setStrokeWidth(2);
            curve.setFill(Color.TRANSPARENT);
            curve.setPickOnBounds(true);

            arrowHead = new Polygon();
            arrowHead.setFill(Color.BLACK);

            controlPoint = new Circle(5, Color.RED);
            controlPoint.setStroke(Color.BLACK);
            controlPoint.setStrokeWidth(2);
            controlPoint.setVisible(false);

            getChildren().addAll(curve, arrowHead, controlPoint);
            addEventHandlers();
            curve.setMouseTransparent(true);
        }

        /**
         * Sets the start point of the curve.
         */
        public void setStart(double x, double y) {
            this.startX = x;
            this.startY = y;
            curve.setStartX(x);
            curve.setStartY(y);
            updateArrowHead();
        }

        /**
         * Sets the end point of the curve.
         */
        public void setEnd(double x, double y) {
            this.endX = x;
            this.endY = y;
            curve.setEndX(x);
            curve.setEndY(y);
            updateArrowHead();
        }

        /**
         * Sets the control point of the curve.
         */
        public void setControl(double x, double y) {
            this.controlX = x;
            this.controlY = y;
            curve.setControlX(x);
            curve.setControlY(y);
            controlPoint.setCenterX(x);
            controlPoint.setCenterY(y);
            updateArrowHead();
        }

        /**
         * Computes a point on the quadratic curve at parameter t.
         *
         * @param t a value between 0 and 1.
         * @return an array {x, y} representing the point on the curve.
         */
        private double[] getCurvePoint(double t) {
            double oneMinusT = 1 - t;
            double x = oneMinusT * oneMinusT * startX + 2 * oneMinusT * t * controlX + t * t * endX;
            double y = oneMinusT * oneMinusT * startY + 2 * oneMinusT * t * controlY + t * t * endY;
            return new double[]{x, y};
        }

        /**
         * Computes the derivative (tangent vector) of the curve at parameter t.
         *
         * @param t a value between 0 and 1.
         * @return an array {dx, dy} representing the tangent.
         */
        private double[] getCurveDerivative(double t) {
            double oneMinusT = 1 - t;
            double dx = 2 * oneMinusT * (controlX - startX) + 2 * t * (endX - controlX);
            double dy = 2 * oneMinusT * (controlY - startY) + 2 * t * (endY - controlY);
            return new double[]{dx, dy};
        }

        /**
         * Updates the arrowhead's shape and position based on the curve's midpoint (t = 0.5).
         * Also plays a pulse animation on the arrowhead if the transition is complete.
         */
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

            if (Transition.this.complete) {
                animateArrowHead();
            }
        }

        /**
         * Returns the current arrowhead tip coordinates.
         *
         * @return an array {arrowTipX, arrowTipY}.
         */
        public double[] getArrowTip() {
            return new double[]{arrowTipX, arrowTipY};
        }

        /**
         * Installs mouse event handlers exclusively on the arrowhead.
         * Also allows dragging of the control point with the primary mouse button.
         */
        private void addEventHandlers() {
            // Enable dragging of the control point.
            controlPoint.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    controlDragging = true;
                    e.consume();
                }
            });
            controlPoint.setOnMouseDragged(e -> {
                if (controlDragging && e.getButton() == MouseButton.PRIMARY) {
                    setControl(e.getX(), e.getY());
                    e.consume();
                }
            });
            controlPoint.setOnMouseReleased(e -> {
                controlDragging = false;
                e.consume();
            });

            // Process right-click events on the arrowhead for selection.
            // If the arrowhead is left-clicked while selected, then simply deselect.
            arrowHead.setOnKeyPressed( event -> {
                if (event.getCode() == KeyCode.ENTER && selected) {
                    deselect();
                    event.consume();
                }
            });
            arrowHead.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    toggleSelection();
                    e.consume();
                } else if (e.getButton() == MouseButton.PRIMARY && selected) {
                    deselect();
                    e.consume();
                }
            });
            arrowHead.setOnMouseEntered(e -> setCursor(Cursor.HAND));
            arrowHead.setOnMouseExited(e -> setCursor(Cursor.DEFAULT));

            // Ensure the curve itself is not interactive.
            curve.setOnMouseClicked(null);
        }

        /**
         * Plays a pulse animation on the arrowhead via a scale transition.
         */
        private void animateArrowHead() {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), arrowHead);
            st.setFromX(1);
            st.setFromY(1.0);
            st.setToX(1.7);
            st.setToY(1.7);
            st.setCycleCount(2);
            st.setAutoReverse(true);
            st.play();

        }

        /**
         * Toggles selection of the arrow.
         * When first selected, editing starts and focus is requested;
         * on a subsequent right-click, attemptFinalizeName() is called and,
         * if editing ends successfully, the arrow is deselected.
         */
        // In your CurvedArrow inner class

        private void toggleSelection() {
            // If another arrow is already selected (and it's not this one), ignore the selection attempt.
            if (Transition.selectedArrow != null && Transition.selectedArrow != this) {
                return;
            }

            if (!selected) {
                select();
                // Request focus and start editing label on this transition.
                Transition.this.requestFocus();
                Transition.this.editableLabel.startEditing();
            } else {
                // Deselect this arrow.
                deselect();
            }
        }

        public void select() {
            // Only allow selection if none is currently selected or if it's already this arrow.
            if (Transition.selectedArrow != null && Transition.selectedArrow != this) {
                return;  // Another arrow is selected; do not change.
            }
            // Set up the editor position and make it visible.
            Transition.this.editableLabel.setEditorPosition(arrowTipX + 10, arrowTipY - 20);
            Transition.this.editableLabel.setVisible(true);
            selected = true;
            Transition.selectedArrow = this;
            curve.setStroke(Color.BLUE);
            controlPoint.setVisible(true);
            setCursor(Cursor.HAND);
            toFront();
        }

        public void deselect() {
            // Reposition the label slightly.
            Transition.this.editableLabel.setLabelPosition(arrowTipX + 10, arrowTipY - 20);
            Transition.this.editableLabel.setEditorPosition(arrowTipX + 10, arrowTipY - 20);
            selected = false;
            curve.setStroke(Color.BLACK);
            controlPoint.setVisible(false);
            setCursor(Cursor.DEFAULT);
            arrowHead.setMouseTransparent(false);
            // Clear the global selection if this arrow was selected.
            if (Transition.selectedArrow == this) {
                Transition.selectedArrow = null;
            }
        }


        public boolean isSelected() {
            return selected;
        }
    }
}
