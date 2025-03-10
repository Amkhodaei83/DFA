package com.example.dfa_app.DFA;

class Transition {
    private State fromState;
    private String symbol;
    private State nextState;

    public Transition(State fromState, String symbol, State nextState) {
        this.fromState = fromState;
        this.symbol = symbol;
        this.nextState = nextState;
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
}
