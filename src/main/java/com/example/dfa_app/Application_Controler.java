package com.example.dfa_app;

import com.example.dfa_app.DFA.*;

import javafx.fxml.FXML;
import javafx.scene.Camera;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jdk.swing.interop.SwingInterOpUtils;


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
    private AnchorPane centerAnchor ;  // under canvas
    @FXML
    private StackPane StackPane ;
    @FXML
    private Pane centerCanvas1 ;
    @FXML
    private Pane centerCanvas2 ;
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


    @FXML
    public void initialize() {
//    var DFA = new DFA();
//                transition.getCurvedArrow().select();


        centerAnchor.setOnMouseClicked(firstClick -> {
            Node clickedNode = firstClick.getPickResult().getIntersectedNode();
            if (firstClick.isControlDown() && clickedNode instanceof Circle) {
                System.out.println("A Circle was clicked.");
                // Retrieve the originating state from the clicked node's user data.
                State fromState = (State) clickedNode.getUserData();

                // Create the transition from the originating state.
                Transition transition = new Transition(fromState);
                // If needed, configure the curved arrow (for example, set its pane) here:
                // transition.getCurvedArrow().setPane(centerCanvas2);

                // Install a one-time event handler for the second click.
                centerAnchor.setOnMouseClicked(secondClick -> {
                    Node secondClickedNode = secondClick.getPickResult().getIntersectedNode();
                    if (secondClickedNode instanceof Circle) {
                        System.out.println("A Circle was clicked.");
                        // Retrieve the target state from the clicked node's user data.
                        State targetState = (State) secondClickedNode.getUserData();
                        // Set the target state.
                        transition.setNextState(targetState);
                        // Optionally, add event handlers to the curved arrow after linking.
                        transition.getCurvedArrow().addEventHandlers();
                    }

                    // Remove the temporary second-click handler.
                    centerAnchor.setOnMouseClicked(null);
                    secondClick.consume();
                });
            }
            firstClick.consume();
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
        System.out.println("Creating new state");

        // Create and add the new state
        State newState = new State(-30, -30, 30, Color.WHITE);
        centerCanvas2.getChildren().add(newState);
        newState.select();

        // Define a handler for mouse movement (state follows the cursor)
        javafx.event.EventHandler<MouseEvent> mouseMoveHandler = event -> {
            newState.moveState(event.getX(), event.getY());
            event.consume();
        };
        centerAnchor.setOnMouseMoved(mouseMoveHandler);

        // Define a handler for the first click that freezes the state's position.
        javafx.event.EventHandler<MouseEvent> firstClickHandler = event -> {
            // Stop following the mouse.
            centerAnchor.setOnMouseMoved(null);

            // Check for overlaps with existing states
            if (isOverlapping(newState)) {
                showOverlapError();
                // Re-enable the movement if placement is invalid.
                centerAnchor.setOnMouseMoved(mouseMoveHandler);
                return;
            }
            // Remove the first-click handler.
            centerAnchor.setOnMouseClicked(null);

            // Set up a final click handler that verifies the state (e.g., finalizes label editing)
            centerAnchor.setOnMouseClicked(finalClickEvent -> {
                newState.deselect(); // This finalizes the name (with validations)
                centerAnchor.setOnMouseClicked(null);
                finalClickEvent.consume();
            });
            event.consume();
        };

        centerAnchor.setOnMouseClicked(firstClickHandler);
        System.out.println("state :"+newState.getName()+" added to DFA"); //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }

    // Helper method to check for state overlap.
    private boolean isOverlapping(State newState) {
        // Coordinates and radius for the new state.
        double x1 = newState.getLayoutX();
        double y1 = newState.getLayoutY();
        double r1 = newState.getMainCircle().getRadius();

        // Iterate over all other states in the canvas.
        for (Node node : centerCanvas2.getChildren()) {
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
