package com.example.dfa_app;

import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.event.ActionEvent;

public class Application_Controler {
    @FXML
    private BorderPane BorderPane;




    @FXML
    public void initialize() {


        BorderPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case Z:
                    if (event.isControlDown()) {
                        event.consume();
                    }
                    break;
                case S:
                    if (event.isControlDown()) {
                        event.consume();
                    }
                    break;

            }
        });


    }
//
//    @FXML
//    private void handleButtonClick(ActionEvent event) {
//
//    }
}
