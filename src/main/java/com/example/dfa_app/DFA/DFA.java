package com.example.dfa_app.DFA;

import java.util.*;

class DFA {
    private Set<State> states;
    private Set<String> alphabet;
    private State initialState;
    private Set<State> acceptingStates;

    public DFA() {
        states = new HashSet<>();
        alphabet = new HashSet<>();
        acceptingStates = new HashSet<>();
    }

    public void inputDFA() {
        Scanner scanner = new Scanner(System.in);

        // Input States
        System.out.println("Enter the number of states:");
        int numStates = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter the states (separated by space):");
        String[] stateNames = scanner.nextLine().split("\\s+");
        Map<String, State> stateMap = new HashMap<>();

        for (String name : stateNames) {
            State state = new State(name);
            states.add(state);
            stateMap.put(name, state);
        }

        // Input Alphabet
        System.out.println("Enter the alphabet symbols (separated by space):");
        String[] symbols = scanner.nextLine().split("\\s+");
        alphabet.addAll(Arrays.asList(symbols));

        // Input Initial State
        System.out.println("Enter the initial state:");
        String initStateName = scanner.nextLine();
        initialState = stateMap.get(initStateName);

        // Input Accepting States
        System.out.println("Enter the number of accepting states:");
        int numAcceptingStates = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter the accepting states (separated by space):");
        String[] acceptingStateNames = scanner.nextLine().split("\\s+");
        for (String name : acceptingStateNames) {
            State state = stateMap.get(name);
            state.setAccepting(true);
            acceptingStates.add(state);
        }

        // Input Transition Function
        System.out.println("Enter the transition function:");
        for (State state : states) {
            for (String symbol : alphabet) {
                System.out.println("δ(" + state.getName() + ", " + symbol + ") = ?");
                String nextStateName = scanner.nextLine();
                State nextState = stateMap.get(nextStateName);
                state.addTransition(symbol, nextState);
            }
        }

        scanner.close();
    }

    public void removeUnreachableStates() {
        System.out.println("\n*** Step 1: Removing Unreachable States ***");
        Set<State> reachableStates = new HashSet<>();
        Queue<State> queue = new LinkedList<>();
        reachableStates.add(initialState);
        queue.add(initialState);

        while (!queue.isEmpty()) {
            State current = queue.poll();
            for (Transition t : current.getTransitions()) {
                State nextState = t.getNextState();
                if (nextState != null && reachableStates.add(nextState)) {
                    queue.add(nextState);
                }
            }
        }

        // Remove unreachable states
        states.retainAll(reachableStates);
        acceptingStates.retainAll(reachableStates);

        System.out.print("Reachable States: ");
        for (State state : reachableStates) {
            System.out.print(state.getName() + " ");
        }
        System.out.println();
    }

    public void minimizeDFA() {
        System.out.println("\n*** Step 2: Minimizing DFA using Partitioning Method ***");

        // Initial Partition
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

        // Refinement Loop
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
                        State nextState = state.getTransition(symbol);
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

        // Reconstruct DFA
        rebuildDFA(partitions);
    }

    private void rebuildDFA(List<Set<State>> partitions) {
        Map<State, State> stateMapping = new HashMap<>();
        Set<State> newStates = new HashSet<>();
        Set<State> newAcceptingStates = new HashSet<>();
        State newInitialState = null;

        // Create new states representing partitions
        int index = 0;
        for (Set<State> partition : partitions) {
            State representative = partition.iterator().next();
            State newState = new State("P" + index);
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

        // Define transitions for new states
        for (State newState : newStates) {
            State oldRepresentative = null;
            for (Map.Entry<State, State> entry : stateMapping.entrySet()) {
                if (entry.getValue().equals(newState)) {
                    oldRepresentative = entry.getKey();
                    break;
                }
            }
            for (String symbol : alphabet) {
                State oldTargetState = oldRepresentative.getTransition(symbol);
                if (oldTargetState != null) {
                    State newTargetState = stateMapping.get(oldTargetState);
                    newState.addTransition(symbol, newTargetState);
                }
            }
        }

        // Update DFA components
        states = newStates;
        acceptingStates = newAcceptingStates;
        initialState = newInitialState;
    }

    private int getPartitionIndex(List<Set<State>> partitions, State state) {
        for (int i = 0; i < partitions.size(); i++) {
            if (partitions.get(i).contains(state)) {
                return i;
            }
        }
        return -1; // State not found
    }

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
                System.out.println("δ(" + state.getName() + ", " + t.getSymbol() + ") = " + t.getNextState().getName());
            }
        }
    }
}
