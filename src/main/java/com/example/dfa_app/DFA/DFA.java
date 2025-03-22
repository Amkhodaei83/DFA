package com.example.dfa_app.DFA;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.util.*;

public class DFA {
    private Set<State> states;
    private Set<String> alphabet;
    private State initialState;
    private Set<State> acceptingStates;
    private Pane pane;

    public DFA() {
        states = new HashSet<>();
        alphabet = new HashSet<>();
        acceptingStates = new HashSet<>();
    }

    /**
     * Configures the DFA using already created State objects.
     *
     * @param stateList       List of State objects (as constructed/positioned by your UI).
     * @param alphabet        Set of alphabet symbols.
     * @param initialState    The initial state.
     * @param acceptingStates The set of accepting states.
     * @param transitionsMap  Mapping from a State to another mapping of symbol → next State.
     */
    /**
     * Returns a string summarizing the current DFA data including:
     * - All state names (indicating the initial and accepting ones)
     * - The alphabet symbols
     * - The complete transition function.
     */
    public String getDFAData() {
        StringBuilder sb = new StringBuilder();

        sb.append("----- DFA Data -----\n");
        sb.append("States:\n");
        for (State state : states) {
            sb.append("  ");
            sb.append(state.getName());
            if (state.equals(initialState)) {
                sb.append(" [initial]");
            }
            if (state.isAccepting()) {
                sb.append(" [accepting]");
            }
            sb.append("\n");
        }

        sb.append("\nAlphabet: ").append(alphabet).append("\n\n");

        sb.append("Transitions:\n");
        for (State state : states) {
            for (Transition t : state.getTransitions()) {
                // Check to avoid null pointer exceptions if transitions are incomplete.
                if (t.getSymbol() != null && t.getNextState() != null) {
                    sb.append(String.format("  δ(%s, %s) = %s\n", state.getName(), t.getSymbol(), t.getNextState().getName()));
                }
            }
        }
        sb.append("--------------------");
        return sb.toString();
    }

    public void configureDFA(List<State> stateList,
                             Set<String> alphabet,
                             State initialState,
                             Set<State> acceptingStates,
                             Map<State, Map<String, State>> transitionsMap) {
        this.states = new HashSet<>(stateList);
        this.alphabet = new HashSet<>(alphabet);
        this.initialState = initialState;
        this.acceptingStates = new HashSet<>(acceptingStates);

        for (Map.Entry<State, Map<String, State>> entry : transitionsMap.entrySet()) {
            State state = entry.getKey();
            Map<String, State> stateTransitions = entry.getValue();
            for (Map.Entry<String, State> t : stateTransitions.entrySet()) {
                String symbol = t.getKey();
                State nextState = t.getValue();
                // Calls a helper method (which you add to your State class)
                state.addTransitionDirect(symbol, nextState);
            }
        }
    }

    /**
     * Alternative configuration method that creates states and transitions from names.
     *
     * @param stateNames           List of state names.
     * @param alphabet             Set of alphabet symbols.
     * @param initialStateName     Name of the initial state.
     * @param acceptingStateNames  List of names for accepting states.
     * @param transitionsData      Mapping from a state name to a mapping of symbol → next state name.
     */
    public void configureDFA(List<String> stateNames,
                             Set<String> alphabet,
                             String initialStateName,
                             List<String> acceptingStateNames,
                             Map<String, Map<String, String>> transitionsData) {
        Map<String, State> stateMap = new HashMap<>();
        for (String name : stateNames) {
            // Create states using your parameterized constructor.
            // Defaults: center at (50,50), radius 15, color LIGHTGRAY—adjust as needed.
            State s = new State(50, 50, 15, Color.LIGHTGRAY, name);
            stateMap.put(name, s);
            states.add(s);
        }
        this.alphabet.addAll(alphabet);
        this.initialState = stateMap.get(initialStateName);
        for (String name : acceptingStateNames) {
            State s = stateMap.get(name);
            if (s != null) {
                s.setAccepting(true);
                acceptingStates.add(s);
            }
        }
        // Add transitions using the helper method on State.
        for (Map.Entry<String, Map<String, String>> entry : transitionsData.entrySet()) {
            String stateName = entry.getKey();
            State fromState = stateMap.get(stateName);
            if (fromState == null) continue;
            Map<String, String> transForState = entry.getValue();
            for (Map.Entry<String, String> t : transForState.entrySet()) {
                String symbol = t.getKey();
                String nextStateName = t.getValue();
                State nextState = stateMap.get(nextStateName);
                if (nextState != null) {
                    fromState.addTransitionDirect(symbol, nextState);
                }
            }
        }
    }

    /**
     * Removes unreachable states from the DFA.
     */
    public void removeUnreachableStates() {
        System.out.println("\n*** Step 1: Removing Unreachable States ***");
        Set<State> reachableStates = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        reachableStates.add(initialState);
        queue.add(initialState);

        while (!queue.isEmpty()) {
            State current = queue.poll();
            for (Transition t : current.getTransitions()) {
                State nextState = (State) t.getNextState();
                if (nextState != null && reachableStates.add(nextState)) {
                    queue.add(nextState);
                }
            }
        }

        states.retainAll(reachableStates);
        acceptingStates.retainAll(reachableStates);

        System.out.print("Reachable States: ");
        for (State state : reachableStates) {
            System.out.print(state.getName() + " ");
        }
        System.out.println();
    }

    /**
     * Minimizes the DFA using the partitioning method.
     */
    public void minimizeDFA() {
        System.out.println("\n*** Step 2: Minimizing DFA using Partitioning Method ***");

        List<Set<State>> partitions = new ArrayList<>();
        Set<State> acceptingPartition = new HashSet<>();
        Set<State> nonAcceptingPartition = new HashSet<>();

        for (State state : states) {
            if (state.isAccepting()) {
                acceptingPartition.add(state);
            } else {
                nonAcceptingPartition.add(state);
            }
        }

        if (!nonAcceptingPartition.isEmpty()) partitions.add(nonAcceptingPartition);
        if (!acceptingPartition.isEmpty()) partitions.add(acceptingPartition);

        printPartitions(partitions);

        boolean partitionsChanged;
        int iteration = 0;
        do {
            partitionsChanged = false;
            List<Set<State>> newPartitions = new ArrayList<>();

            System.out.println("\nIteration " + iteration + ":");
            for (Set<State> partition : partitions) {
                Map<String, Set<State>> blockMap = new HashMap<>();
                for (State state : partition) {
                    StringBuilder signature = new StringBuilder();
                    for (String symbol : alphabet) {
                        // Assumes you have implemented getTransition(symbol) in State.
                        Transition transition = state.getTransition(symbol);
                        State nextState = (transition != null) ? (State) transition.getNextState() : null;
                        int index = getPartitionIndex(partitions, nextState);
                        signature.append(symbol).append("-P").append(index).append(";");
                    }
                    String sig = signature.toString();
                    blockMap.computeIfAbsent(sig, k -> new HashSet<>()).add(state);
                }
                if (blockMap.size() > 1) {
                    partitionsChanged = true;
                }
                newPartitions.addAll(blockMap.values());
            }
            partitions = newPartitions;
            printPartitions(partitions);
            iteration++;
        } while (partitionsChanged);

        rebuildDFA(partitions);
    }

    /**
     * Rebuilds the DFA from the partitions produced by the minimization algorithm.
     */
    private void rebuildDFA(List<Set<State>> partitions) {
        Map<State, State> stateMapping = new HashMap<>();
        Set<State> newStates = new HashSet<>();
        Set<State> newAcceptingStates = new HashSet<>();
        State newInitialState = null;
        int index = 0;

        for (Set<State> partition : partitions) {
            State representative = partition.iterator().next();
            // Create a new state based on a representative’s visual attributes.
            State newState = new State(
                    representative.getLayoutX(),
                    representative.getLayoutY(),
                    representative.getMainCircle().getRadius(),
                    (Color) representative.getMainCircle().getFill(),
                    "P" + index
            );
            if (partition.contains(initialState)) {
                newInitialState = newState;
            }
            if (representative.isAccepting()) {
                newState.setAccepting(true);
                newAcceptingStates.add(newState);
            }
            for (State oldState : partition) {
                stateMapping.put(oldState, newState);
            }
            newStates.add(newState);
            index++;
        }

        // Reestablish transitions for the new states.
        for (State newState : newStates) {
            State oldRepresentative = null;
            for (Map.Entry<State, State> entry : stateMapping.entrySet()) {
                if (entry.getValue().equals(newState)) {
                    oldRepresentative = entry.getKey();
                    break;
                }
            }
            for (String symbol : alphabet) {
                Transition oldTransition = oldRepresentative.getTransition(symbol);
                if (oldTransition != null) {
                    State oldTarget = (State) oldTransition.getNextState();
                    if (oldTarget != null) {
                        State newTarget = stateMapping.get(oldTarget);
                        newState.addTransitionDirect(symbol, newTarget);
                    }
                }
            }
        }

        states = newStates;
        acceptingStates = newAcceptingStates;
        initialState = newInitialState;
    }

    /**
     * Helper method to get the partition index for a given state.
     */
    private int getPartitionIndex(List<Set<State>> partitions, State state) {
        for (int i = 0; i < partitions.size(); i++) {
            if (partitions.get(i).contains(state)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Prints the current partitions to the console.
     */
    private void printPartitions(List<Set<State>> partitions) {
        System.out.println("Current Partitions:");
        int index = 0;
        for (Set<State> partition : partitions) {
            System.out.print("P" + index + ": ");
            for (State state : partition) {
                System.out.print(state.getName() + " ");
            }
            System.out.println();
            index++;
        }
    }

    /**
     * Prints the minimized DFA details to the console.
     */
    public void printMinimizedDFA() {
        System.out.println("\n*** Minimized DFA ***");
        System.out.print("States: ");
        for (State state : states) {
            System.out.print(state.getName() + " ");
        }
        System.out.println();

        System.out.println("Alphabet: " + alphabet);

        System.out.println("Initial State: " + initialState.getName());

        System.out.print("Accepting States: ");
        for (State state : acceptingStates) {
            System.out.print(state.getName() + " ");
        }
        System.out.println();

        System.out.println("Transition Function:");
        for (State state : states) {
            for (Transition t : state.getTransitions()) {
                System.out.println("δ(" + state.getName() + ", " + t.getSymbol() + ") = " +
                        t.getNextState().getName());
            }
        }
    }
}
