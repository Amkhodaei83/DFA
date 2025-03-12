package com.example.dfa_app.DFA;

import javafx.scene.Group;

/**
 * The Transition class represents a transition in the DFA.
 * It holds the originating state, a symbol, and the target state.
 * Visually, it is represented by a CurvedArrow that connects states.
 *
 * By extending Group, Transition encapsulates its visual element, so it
 * can be added directly to any parent container without external dependencies.
 */
public class Transition extends Group {
    private final State fromState;
    private String symbol;
    private State nextState;
    private final CurvedArrow curvedArrow;

    /**
     * Constructs a new Transition starting from the given state.
     * The visual component (CurvedArrow) is created internally.
     *
     * @param fromState The state from which the transition originates.
     */
    public Transition(State fromState) {
        this.fromState = fromState;
        // Create the curved arrow based solely on the starting state.
        this.curvedArrow = new CurvedArrow(fromState);
        // Add the curved arrow as a child so it is rendered.
        getChildren().add(curvedArrow);
        System.out.println("Transition created: from state " + fromState);
    }

    /**
     * Overloaded constructor that also accepts the symbol and the target state.
     *
     * @param fromState The state from which the transition originates.
     * @param symbol    The symbol associated with the transition.
     * @param nextState The target state of the transition.
     */
    public Transition(State fromState, String symbol, State nextState) {
        this(fromState);    // Delegate common initialization.
        setSymbol(symbol);
        setNextState(nextState);
    }

    public State getFromState() {
        return fromState;
    }

    public String getSymbol() {
        return symbol;
    }

    public State getNextState() {
        return nextState;
    }

    /**
     * Sets the symbol (label) of this transition.
     *
     * @param symbol the symbol associated with the transition.
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Sets the target state of the transition.
     * Updates the visual representation (curved arrow) to point to the new endpoint.
     * After setting the target, the curved arrow automatically enters editing mode for its label.
     *
     * @param nextState the state where the transition should point.
     */
    public void setNextState(State nextState) {
        this.nextState = nextState;
        curvedArrow.updateEndPoint(nextState);
        curvedArrow.select();  // Enter editing mode on the arrow if needed.
        System.out.println("Transition updated: from state " + fromState + " to " + nextState);
    }

    public CurvedArrow getCurvedArrow() {
        return curvedArrow;
    }
}
