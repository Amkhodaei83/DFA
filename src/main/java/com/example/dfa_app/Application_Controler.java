package com.example.dfa_app;

import javafx.fxml.FXML;
import javafx.scene.Camera;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;

public class Application_Controler {


//        dfaminimizeroop
//        dfa.inputDFA();
//        dfa.removeUnreachableStates();
//        dfa.minimizeDFA();
//        dfa.printMinimizedDFA();
//



    @FXML
    private BorderPane BorderPane;
    private TextField stateNameTextField ;
    private CheckBox startStateCheck , acceptingStateCheck ;
    private ComboBox fromStateCombo , toStateCombo , transitionNameCombo ;
    private TableView dfaTransitionTable ;
    private TableColumn stateColumn ;
    private TableColumn transitionsParentColumn ; //transitionColumn1
    private AnchorPane centerAnchor ;  // under canvas
    private Canvas centerCanvas ;
    private Button startProcessButton ;
    private TextArea logTextArea ;
    private Button openButton ;
    private Button saveButton ;
    private Button newPageButton ;
    private Button newStateButton ;
    private Button newTransitionButton ;
    private Button undoButton ;
    private Button redoButton ;


    @FXML
    public void initialize() {













        BorderPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
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
                        System.out.println("ctrl+n");//new state
                        event.consume();
                    }
                    break;

            }
        });


    }
}
