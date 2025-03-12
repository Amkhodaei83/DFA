//package com.example.dfa_app.Arrow;// CurvedArrowDrawer.java
//
//import javafx.application.Application;
//import javafx.scene.Scene;
//import javafx.scene.input.KeyCode;
//import javafx.scene.input.MouseButton;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.layout.Pane;
//import javafx.stage.Stage;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CurvedArrowDrawer extends Application {
//
//    private Pane pane;
//    private Arrow currentArrow;
//    private List<Arrow> arrows = new ArrayList<>();
//
//    @Override
//    public void start(Stage primaryStage) {
//        pane = new Pane();
//
//        // Set up mouse event handlers
//        pane.setOnMousePressed(this::onMousePressed);
//        pane.setOnMouseDragged(this::onMouseDragged);
//        pane.setOnMouseReleased(this::onMouseReleased);
//
//        Scene scene = new Scene(pane, 800, 600);
//
//        // Set up key event handler for deletion
//        scene.setOnKeyPressed(event -> {
//            if (event.getCode() == KeyCode.DELETE && currentArrow != null) {
//                deleteCurrentArrow();
//            }
//        });
//
//        primaryStage.setScene(scene);
//        primaryStage.setTitle("Draw Selectable Curved Arrows with Unique Labels");
//        primaryStage.show();
//    }
//
//    private void onMousePressed(MouseEvent event) {
//        if (event.getButton() == MouseButton.PRIMARY) {
//            // Left-click: Start drawing a new arrow
//            deselectAllArrows();
//            currentArrow = new Arrow(event.getX(), event.getY(), pane, this);
//            arrows.add(currentArrow);
//        } else if (event.getButton() == MouseButton.SECONDARY) {
//            // Right-click: Select the closest arrow
//            double minDistance = Double.MAX_VALUE;
//            Arrow closestArrow = null;
//            for (Arrow arrow : arrows) {
//                double distance = arrow.distanceToPoint(event.getX(), event.getY());
//                if (distance < minDistance) {
//                    minDistance = distance;
//                    closestArrow = arrow;
//                }
//            }
//            // Set a maximum selection threshold (e.g., 20 pixels)
//            if (minDistance <= 20 && closestArrow != null) {
//                deselectAllArrows();
//                closestArrow.select();
//                currentArrow = closestArrow;
//            } else {
//                // Clicked on empty space, deselect all arrows
//                deselectAllArrows();
//            }
//        }
//    }
//
//    private void onMouseDragged(MouseEvent event) {
//        if (event.getButton() == MouseButton.PRIMARY) {
//            if (currentArrow != null && !currentArrow.isControlPointDragging()) {
//                // Update the end point of the current arrow
//                currentArrow.updateEndPoint(event.getX(), event.getY());
//            }
//        }
//    }
//
//    private void onMouseReleased(MouseEvent event) {
//        if (event.getButton() == MouseButton.PRIMARY && currentArrow != null) {
//            currentArrow.stopDraggingControlPoint();
//            currentArrow.finalizeArrow();
//        }
//    }
//
//    public void deselectAllArrows() {
//        for (Arrow arrow : arrows) {
//            arrow.deselect();
//        }
//        currentArrow = null;
//    }
//
//    public void setCurrentArrow(Arrow arrow) {
//        this.currentArrow = arrow;
//    }
//
//    private void deleteCurrentArrow() {
//        if (currentArrow != null) {
//            currentArrow.removeFromPane();
//            arrows.remove(currentArrow);
//            currentArrow = null;
//        }
//    }
//
//
//}
