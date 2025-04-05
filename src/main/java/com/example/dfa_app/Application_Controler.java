package com.example.dfa_app;

import com.example.dfa_app.DFA.*;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.event.EventHandler;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Application_Controler  implements SelectionListener  {
    @FXML
    private TabPane TabPane;
    @FXML
    private Tab transitionSettingsTab , stateSettingsTab;
    @FXML
    private BorderPane BorderPane;
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
    private Pane pane;
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




    // Hold a reference to the DFA instance.
    private DFA dfa;
    private Transition currentTransition;
    private boolean waitingForSecondClick = false;

    @FXML
    public void initialize() {
        // Initialize the DFA model.
        dfa = new DFA();


        Timeline dfaUpdater = new Timeline(
                new KeyFrame(Duration.millis(100), event -> {
                    logTextArea.setText(dfa.getDFAData());
                })
        );
        dfaUpdater.setCycleCount(Timeline.INDEFINITE);
        dfaUpdater.play();
        // Global mouse click handler for creating transitions.
        pane.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEvent -> {
            Node clickedNode = mouseEvent.getPickResult().getIntersectedNode();
            // If the clicked node is a state (Circle), handle state selection/deselection.
            if (clickedNode.getUserData() instanceof State) {
                State state = (State) clickedNode.getUserData();

                // Right-click or secondary button toggles selection.
                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
                    if (state.isSelected()) {
                        state.deselect();
                    } else {
                        state.select();
                    }
                    mouseEvent.consume();
                    return;
                }
                // Left-click (primary button) on an already selected state deselects it.
                else if (mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (state.isSelected()) {
                        state.deselect();
                        mouseEvent.consume();
                        return;
                    }
                }
            }



//            if (clickedNode.getUserData() instanceof State) {
//                Transition transition = (Transition) clickedNode.getUserData();
//
//                // Right-click or secondary button toggles selection.
//                if (mouseEvent.getButton() == MouseButton.SECONDARY) {
//                    if (state.isSelected()) {
//                        state.deselect();
//                    } else {
//                        state.select();
//                    }
//                    mouseEvent.consume();
//                    return;
//                }
//                // Left-click (primary button) on an already selected state deselects it.
//                else if (mouseEvent.getButton() == MouseButton.PRIMARY) {
//                    if (state.isSelected()) {
//                        state.deselect();
//                        mouseEvent.consume();
//                        return;
//                    }
//                }
//            }

            // If a transition is already in creation mode, then this is the second click.
            if (waitingForSecondClick) {
                if (clickedNode instanceof Circle) {
                    State targetState = (State) clickedNode.getUserData();
                    if (currentTransition != null) {
                        System.out.println("Second click on a state. Completing transition...");
                        currentTransition.completeTransition(targetState);
                        // Optionally, set a selection listener if you need additional logic.
                        currentTransition.setSelectionListener(this);
                    }
                }
                waitingForSecondClick = false;
                currentTransition = null;
                mouseEvent.consume();
                return;
            }

            // For the first click: if Ctrl is held and a state (Circle) is clicked, create a Transition.
            if (mouseEvent.isControlDown() && clickedNode instanceof Circle) {
                System.out.println("CTRL + click on a state. Initiating transition creation...");
                State fromState = (State) clickedNode.getUserData();
                currentTransition = new Transition(fromState);
                // Attach this controller as the selection listener if needed.
                currentTransition.setSelectionListener(this);
                waitingForSecondClick = true;
                mouseEvent.consume();
                return;
            }
            mouseEvent.consume();
        });



        // When process button is clicked, first build the DFA from current UI elements.
        startProcessButton.setOnAction(actionEvent -> {
            buildDFAFromPane();
            dfa.removeUnreachableStates();
            dfa.minimizeDFA();
            dfa.printMinimizedDFA();
        });

        // Global key handlers.
        BorderPane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case D:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+d"); // Delete
                        event.consume();
                    }
                    break;
                case Z:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+z"); // Undo
                        event.consume();
                    }
                    break;
                case Y:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+y"); // Redo
                        event.consume();
                    }
                    break;
                case S:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+s"); // Save file
                        event.consume();
                    }
                    break;
                case O:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+o"); // Open file
                        event.consume();
                    }
                    break;
                case R:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+R"); // Start minimization
                        event.consume();
                    }
                    break;
                case K:
                    if (event.isControlDown()) {
                        System.out.println("ctrl+k"); // Clear
                        event.consume();
                    }
                    break;
                case N:
                    if (event.isControlDown()) {
                        createState(); // Create a new state interactively.
                        event.consume();
                    }
                    break;
            }
        });
    }

    /**
     * Builds the DFA configuration from the states and transitions present in the pane.
     * This method collects all State objects, gathers their transitions, builds the alphabet,
     * and determines the set of accepting states as well as an initial state.
     */
    private void buildDFAFromPane() {
        List<State> stateList = new ArrayList<>();
        Set<String> alphabet = new HashSet<>();
        Set<State> acceptingStates = new HashSet<>();
        Map<State, Map<String, State>> transitionsMap = new HashMap<>();
        State initialState = null;

        // Iterate through the pane's children, filtering for State instances.
        for (Node node : pane.getChildren()) {
            if (node instanceof State) {
                State s = (State) node;
                stateList.add(s);
                if (s.isAccepting()) {
                    acceptingStates.add(s);
                }
                // For the initial state, we simply take the first one.
                if (initialState == null) {
                    initialState = s;
                }
                // Build the transitions mapping for this state.
                Map<String, State> transMap = new HashMap<>();
                for (Transition t : s.getTransitions()) {
                    if (t.getSymbol() != null && t.getNextState() != null) {
                        transMap.put(t.getSymbol(), (State) t.getNextState());
                        alphabet.add(t.getSymbol());
                    }
                }
                transitionsMap.put(s, transMap);
            }
        }
        // Pass the assembled data to the DFA’s configuration method.
        dfa.configureDFA(stateList, alphabet, initialState, acceptingStates, transitionsMap);
    }

    /**
     * Creates a new state and allows the user to place it on the pane via mouse movement.
     * A temporary mouse handler lets the state follow the cursor until its position is finalized.
     */
    private void createState() {
        // Create a new state using the four-parameter constructor (name to be finalized later).
        State newState = new State(-30, -30, 30, Color.WHITE);
        newState.setSelectionListener(this);
        pane.getChildren().add(newState);
        newState.select();

        // Handler to have the state follow the mouse.
        EventHandler<MouseEvent> mouseMoveHandler = event -> {
            newState.moveState(event.getX(), event.getY());
            event.consume();
        };
        pane.setOnMouseMoved(mouseMoveHandler);

        // First-click freezes the state's position and checks for overlap.
        EventHandler<MouseEvent> firstClickHandler = event -> {
            pane.setOnMouseMoved(null); // Stop moving with the mouse.
            if (isOverlapping(newState)) {
                showOverlapError();
                // If the placement is invalid, re-enable movement.
                pane.setOnMouseMoved(mouseMoveHandler);
                return;
            }
            pane.setOnMouseClicked(null); // Remove this temporary handler.
            // Final click handler to finalize the state (e.g., complete label editing).
            pane.setOnMouseClicked(finalClickEvent -> {
                newState.deselect(); // Finalizes the label with validations.
                pane.setOnMouseClicked(null);
                finalClickEvent.consume();
            });
            event.consume();
        };

        pane.setOnMouseClicked(firstClickHandler);
        System.out.println("New state added to the pane.");
    }

    /**
     * Checks whether the provided newState overlaps with any existing states on the pane.
     */
    private boolean isOverlapping(State newState) {
        // Get the new state's position and radius.
        double x1 = newState.getLayoutX();
        double y1 = newState.getLayoutY();
        double r1 = newState.getMainCircle().getRadius();

        for (Node node : pane.getChildren()) {
            if (node instanceof State && node != newState) {
                State otherState = (State) node;
                double x2 = otherState.getLayoutX();
                double y2 = otherState.getLayoutY();
                double r2 = otherState.getMainCircle().getRadius();
                double distance = Math.hypot(x1 - x2, y1 - y2);
                if (distance < (r1 + r2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Shows an error alert when a new state overlaps with an existing state.
     */
    private void showOverlapError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Invalid State Placement");
        alert.setHeaderText(null);
        alert.setContentText("The state overlaps with an existing state. Please reposition it.");
        alert.showAndWait();
    }

    public void onSelected(Object obj) {
        if (obj instanceof State) {
            // Update state-related UI components here if needed.
            // Then switch to the State Settings tab.
            TabPane.getSelectionModel().select(stateSettingsTab);
        } else if (obj instanceof CurvedArrow) {

            // Update transition-related UI components here if needed.
            // Then switch to the Transition Settings tab.
            TabPane.getSelectionModel().select(transitionSettingsTab);
        }
    }

    @Override
    public void onDeselected(Object obj) {
        if (obj instanceof State) {
//            ((State) obj).setName(stateNameTextField.getText());
        }
        else if (obj instanceof CurvedArrow) {

        }

    }
}
