package com.example.dfa_app.DFA;

public class DFAMinimizerOOP {

    public static void main(String[] args) {
        DFA dfa = new DFA();
        dfa.inputDFA();
        dfa.removeUnreachableStates();
        dfa.minimizeDFA();
        dfa.printMinimizedDFA();
    }
}

