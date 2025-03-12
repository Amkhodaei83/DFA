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


        centerAnchor.setOnMouseClicked(mouseClickEvent -> {
            Node nodeUnderMouse ;
            if (mouseClickEvent.isControlDown()&&(( nodeUnderMouse = mouseClickEvent.getPickResult().getIntersectedNode()) != null)&&(nodeUnderMouse instanceof Circle)){
                System.out.println("A Circle was clicked.");
                Transition transition = new Transition( (State) nodeUnderMouse.getUserData() , centerCanvas1 );
transition.getCurvedArrow().setPane(centerCanvas2);
                centerAnchor.setOnMouseClicked(mouseClickEvent2 -> {
                    Node nodeUnderMouse2 ;
                    if ((( nodeUnderMouse2 = mouseClickEvent2.getPickResult().getIntersectedNode()) != null)&&(nodeUnderMouse2 instanceof Circle)){
                        System.out.println("A Circle was clicked.");

                        transition.setNextState((State) nodeUnderMouse2.getUserData());
                        transition.getCurvedArrow().addEventHandlers();
                    }
                    centerAnchor.setOnMouseClicked(null);
                    mouseClickEvent2.consume();
                });
            }
            mouseClickEvent.consume();
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
                        System.out.println("ctrl+n"); // New state command

                        // Create a new state; note: centerCanvas2 should be a Pane.
                        var state = new State(-30, -30, 30, Color.WHITE, centerCanvas2);
                        state.select(); // Highlight the state & show label editor immediately

                        // Add a temporary listener to allow the state to follow the mouse
                        StackPane.setOnMouseMoved(mouseMoveEvent -> {
                            state.moveCircle(mouseMoveEvent.getX(), mouseMoveEvent.getY());
                            mouseMoveEvent.consume();
                        });

                        // Set up a listener to finalize state position on mouse click
                        StackPane.setOnMouseClicked(mouseClickEvent -> {
                            System.out.println("Mouse clicked -> new state.");
                            // Remove the temporary mouse-move listener so the state stops following
                            StackPane.setOnMouseMoved(null);
                            // Remove this click listener (we'll add a finalizing click listener next)
                            StackPane.setOnMouseClicked(null);

                            // Set up a final click listener for setting the state name and deselecting
                            StackPane.setOnMouseClicked(finalClickEvent -> {
                                System.out.println("Mouse clicked -> set the state.");
                                state.deselect();
                                // Now remove the finalizing click listener
                                StackPane.setOnMouseClicked(null);
                                finalClickEvent.consume();
                            });
                            mouseClickEvent.consume();
                        });

                        event.consume();
                    }
                    break;


            }
        });
    }
}
