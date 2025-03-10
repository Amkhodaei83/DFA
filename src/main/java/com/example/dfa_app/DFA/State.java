package com.example.dfa_app.DFA;

import java.util.ArrayList;
import java.util.List;

class State {
    private String name;
    private boolean isAccepting;
    private List<Transition> transitions; // List of transitions

    public State(String name) {
        this.name = name;
        this.isAccepting = false;
        this.transitions = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setAccepting(boolean accepting) {
        isAccepting = accepting;
    }

    public boolean isAccepting() {
        return isAccepting;
    }

    public void addTransition(String symbol, State nextState) {
        transitions.add(new Transition(this, symbol, nextState));
    }

    public State getTransition(String symbol) {
        for (Transition t : transitions) {
            if (t.getSymbol().equals(symbol)) {
                return t.getNextState();
            }
        }
        return null;
    }

    public List<Transition> getTransitions() {
        return transitions;
    }

    @Override
    public String toString() {
        return name;
    }
}
