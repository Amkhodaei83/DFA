package com.example.dfa_app.DFA;

import javafx.scene.layout.Pane;

public class Transition {
    private State fromState;
    private String symbol;
    private State nextState;
    private CurvedArrow curvedArrow ;

    public Transition(State fromState , Pane pane) {
        this.fromState = fromState;

        this.curvedArrow = new CurvedArrow(fromState , pane );
        System.out.println("1");
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

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setNextState(State nextState) {
        this.nextState = nextState;
        curvedArrow.updateEndPoint(nextState);
        // Automatically enter the "editing" mode:
        curvedArrow.select();
        System.out.println("3");
    }
    public CurvedArrow getCurvedArrow() {
        return curvedArrow;
    }


}
