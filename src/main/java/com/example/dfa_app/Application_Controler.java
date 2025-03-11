package com.example.dfa_app;

import com.example.dfa_app.DFA.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ApplicationController {

    // FXML nodes—names now match the fx:id values in the FXML file.
    @FXML
    private BorderPane centerPane;

    @FXML
    private TextField stateNameTextField;
    @FXML
    private CheckBox startStateCheck, acceptingStateCheck;
    @FXML
    private ComboBox fromStateCombo, toStateCombo, transitionNameCombo;
    @FXML
    private TableView dfaTransitionTable;
    @FXML
    private TableColumn stateColumn;
    @FXML
    private TableColumn transitionsParentColumn;
    @FXML
    private AnchorPane centerAnchor; // This is under the canvas.
    @FXML
    private StackPane mainStackPane;
    @FXML
    private Pane centerCanvas1;
    @FXML
    private Pane centerCanvas2;
    @FXML
    private Button startProcessButton;
    @FXML
    private TextArea logTextArea;
    @FXML
    private Button openButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button newPageButton;
    @FXML
    private Button newStateButton;
    @FXML
    private Button newTransitionButton;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;

    @FXML
    public void initialize() {
        // Ensure centerCanvas2 does not intercept mouse events.
        centerCanvas2.setMouseTransparent(true);

        // Make the centerPane focusable and request focus so it can capture key events.
        centerPane.setFocusTraversable(true);
        centerPane.requestFocus();

        // ----------------------------------------------------------------
        // Transition Creation on centerAnchor:
        centerAnchor.setOnMouseClicked(mouseClickEvent -> {
            Node nodeUnderMouse;
            if (mouseClickEvent.isControlDown() &&
                    ((nodeUnderMouse = mouseClickEvent.getPickResult().getIntersectedNode()) != null) &&
                    (nodeUnderMouse instanceof Circle)) {

                System.out.println("A Circle was clicked (first state).");
                Transition transition = new Transition((State) nodeUnderMouse.getUserData(), centerCanvas1);

                // Temporary click listener for the second state.
                centerAnchor.setOnMouseClicked(mouseClickEvent2 -> {
                    Node nodeUnderMouse2;
                    if (((nodeUnderMouse2 = mouseClickEvent2.getPickResult().getIntersectedNode()) != null) &&
                            (nodeUnderMouse2 instanceof Circle)) {
                        System.out.println("A Circle was clicked (second state).");
                        transition.setNextState((State) nodeUnderMouse2.getUserData());
                    }
                    // Clear the temporary handler.
                    centerAnchor.setOnMouseClicked(null);
                    mouseClickEvent2.consume();
                });
            }
            mouseClickEvent.consume();
        });
        // ----------------------------------------------------------------

        // ----------------------------------------------------------------
        // Key event handling on the centerPane:
        centerPane.setOnKeyPressed(event -> {
            if (event.isControlDown()) {
                switch (event.getCode()) {
                    case D:
                        System.out.println("ctrl+d -> Delete");
                        event.consume();
                        break;
                    case Z:
                        System.out.println("ctrl+z -> Undo");
                        event.consume();
                        break;
                    case Y:
                        System.out.println("ctrl+y -> Redo");
                        event.consume();
                        break;
                    case S:
                        System.out.println("ctrl+s -> Save file");
                        event.consume();
                        break;
                    case O:
                        System.out.println("ctrl+o -> Open file");
                        event.consume();
                        break;
                    case R:
                        System.out.println("ctrl+R -> Start minimizing");
                        event.consume();
                        break;
                    case K:
                        System.out.println("ctrl+k -> Clear");
                        event.consume();
                        break;
                    case N:
                        System.out.println("ctrl+n -> New state command");
                        // Create a new state on centerCanvas2.
                        State state = new State(-30, -30, 30, Color.WHITE, centerCanvas2);
                        state.select(); // Highlight and show label editor immediately.

                        // Temporary listener for state movement.
                        mainStackPane.setOnMouseMoved(mouseMoveEvent -> {
                            state.moveCircle(mouseMoveEvent.getX(), mouseMoveEvent.getY());
                            mouseMoveEvent.consume();
                        });

                        // Listener for finalizing the state position.
                        mainStackPane.setOnMouseClicked(mouseClickEvent -> {
                            System.out.println("Mouse clicked -> new state finalized.");
                            mainStackPane.setOnMouseMoved(null);
                            mainStackPane.setOnMouseClicked(null);

                            // Optional: Final click handler for resetting state selection.
                            mainStackPane.setOnMouseClicked(finalClickEvent -> {
                                System.out.println("Mouse clicked -> state set.");
                                state.deselect();
                                mainStackPane.setOnMouseClicked(null);
                                finalClickEvent.consume();
                            });
                            mouseClickEvent.consume();
                        });
                        event.consume();
                        break;
                    default:
                        break;
                }
            }
        });
        // ----------------------------------------------------------------
    }

}
