package com.example.dfa_app;

import com.example.dfa_app.DFA.*;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import org.w3c.dom.ls.LSOutput;


public class Application_Controler {


//        dfaminimizeroop
//        dfa.inputDFA();
//        dfa.removeUnreachableStates();
//        dfa.minimizeDFA();
//        dfa.printMinimizedDFA();
//

    @FXML
    private BorderPane BorderPane;
    @FXML
    private TextField stateNameTextField ;
    @FXML
    private CheckBox startStateCheck , acceptingStateCheck ;
    @FXML
    private ComboBox fromStateCombo , toStateCombo , transitionNameCombo ;
    @FXML
    private TableView dfaTransitionTable ;
    @FXML
    private TableColumn stateColumn ;
    @FXML
    private TableColumn transitionsParentColumn ; //transitionColumn1
    @FXML
    private Pane pane ;
    @FXML
    private Button startProcessButton ;
    @FXML
    private TextArea logTextArea ;
    @FXML
    private Button openButton ;
    @FXML
    private Button saveButton ;
    @FXML
    private Button newPageButton ;
    @FXML
    private Button newStateButton ;
    @FXML
    private Button newTransitionButton ;
    @FXML
    private Button undoButton ;
    @FXML
    private Button redoButton ;

    private boolean waitingForSecondClick = false;
    private Transition transition;

    @FXML
    public void initialize() {
//    var DFA = new DFA();
//                transition.getCurvedArrow().select();



        pane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            Node clickedNode = mouseEvent.getPickResult().getIntersectedNode();

            // If waiting for the second click...
            if (waitingForSecondClick) {
                if (clickedNode instanceof Circle) {
                    System.out.println("Second click on a state");
                    // Retrieve target state.
                    State targetState = (State) clickedNode.getUserData();

                    // Complete the transition (instead of calling setToState).
                    transition.completeTransition(targetState);
                }
                waitingForSecondClick = false;
                // You can keep a reference to the transition if needed.
                // Setting transition = null will not remove it from the pane.
                transition = null;
                mouseEvent.consume();
                return;
            }

            // For the first click: create a new Transition if Ctrl is held.
            if (mouseEvent.isControlDown() && clickedNode instanceof Circle) {
                System.out.println("First click on a state");
                State fromState = (State) clickedNode.getUserData();
                transition = new Transition(fromState);
                transition.updateTemporaryEndpoint(mouseEvent.getX(), mouseEvent.getY());
                waitingForSecondClick = true;
            }

            mouseEvent.consume();
        });










        BorderPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case D:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+d");//Delete
                        event.consume();
                    }
                    break;
                case Z:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+z");//undo
                        event.consume();
                    }
                    break;
                case Y:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+y");//redo
                        event.consume();
                    }
                    break;
                case S:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+s");//save the file
                        event.consume();
                    }
                    break;
                case O:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+o");//open a saved file
                        event.consume();
                    }
                    break;
                case R:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+R");//start minimizing
                        event.consume();
                    }
                    break;
                case K:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+k");//clear
                        event.consume();
                    }
                    break;
                case N:
                    if (event.isControlDown()) {
                        createState();           //new state
                        event.consume();
                    }
                    break;

            }
        });
    }



    //create a state and waits for the second click to place it






    private void createState() {
        // Create and add the new state
        State newState = new State(-30, -30, 30, Color.WHITE);
        pane.getChildren().add(newState);
        newState.select();

        // Define a handler for mouse movement (state follows the cursor)
        javafx.event.EventHandler<MouseEvent> mouseMoveHandler = event -> {
            newState.moveState(event.getX(), event.getY());
            event.consume();
        };
        pane.setOnMouseMoved(mouseMoveHandler);

        // Define a handler for the first click that freezes the state's position.
        javafx.event.EventHandler<MouseEvent> firstClickHandler = event -> {
            // Stop following the mouse.
            pane.setOnMouseMoved(null);

            // Check for overlaps with existing states
            if (isOverlapping(newState)) {
                showOverlapError();
                // Re-enable the movement if placement is invalid.
                pane.setOnMouseMoved(mouseMoveHandler);
                return;
            }
            // Remove the first-click handler.
            pane.setOnMouseClicked(null);

            // Set up a final click handler that verifies the state (e.g., finalizes label editing)
            pane.setOnMouseClicked(finalClickEvent -> {
                newState.deselect(); // This finalizes the name (with validations)
                pane.setOnMouseClicked(null);
                finalClickEvent.consume();
            });
            event.consume();
        };

        pane.setOnMouseClicked(firstClickHandler);
        System.out.print("************** \t \t \t \t  added to DFA state : "); //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    }

    // Helper method to check for state overlap.
    private boolean isOverlapping(State newState) {
        // Coordinates and radius for the new state.
        double x1 = newState.getLayoutX();
        double y1 = newState.getLayoutY();
        double r1 = newState.getMainCircle().getRadius();

        // Iterate over all other states in the canvas.
        for (Node node : pane.getChildren()) {
            if (node instanceof State && node != newState) {
                State otherState = (State) node;
                double x2 = otherState.getLayoutX();
                double y2 = otherState.getLayoutY();
                double r2 = otherState.getMainCircle().getRadius();

                // Compute the Euclidean distance between the two circle centers.
                double dx = x1 - x2;
                double dy = y1 - y2;
                double distance = Math.hypot(dx, dy);

                // If the centers are closer than the sum of the radii, the circles overlap.
                if(distance < (r1 + r2)) {
                    return true;
                }
            }
        }
        return false;
    }


    // Helper method to show an alert when overlapping is detected.
    private void showOverlapError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid State Placement");
        alert.setHeaderText(null);
        alert.setContentText("The state overlaps with an existing state. Please reposition it.");
        alert.showAndWait();
    }
}
