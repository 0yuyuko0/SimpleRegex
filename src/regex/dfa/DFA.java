package regex.dfa;


import regex.ast.AST;
import regex.node.LeafNode;
import regex.node.Node;

import java.util.*;

public class DFA {
    private static final State EMPTY_STATE = State.emptyState();

    private State startState;
    private Set<State> endState;
    private Set<State> states;
    private StateTransitionTable transitionTable;

    private DFA(State startState, Set<State> endState,Set<State>states, StateTransitionTable transitionTable) {
        this.startState = startState;
        this.endState = endState;
        this.states = states;
        this.transitionTable = transitionTable;
    }


    public boolean matches(String text) {
        State currState = startState;
        int cnt = 0;
        for (int len = text.length(); cnt < len; ++cnt) {
            char c = text.charAt(cnt);
            currState = transitionTable.trans(currState, c);
            if (currState.equals(EMPTY_STATE))
                return false;
            else if (endState.contains(currState))
                if (cnt == len - 1)
                    return true;
        }
        return false;
    }

    public List<String> match(String text){
        State currState = startState;
        List<String>res = new LinkedList<>();
        StringBuilder sb = new StringBuilder();
        for(char c : text.toCharArray()){
            currState = transitionTable.trans(currState, c);
            sb.append(c);
            if(currState.equals(EMPTY_STATE)) {
                currState = startState;
                sb = new StringBuilder();
            }
            else if(endState.contains(currState)){
                currState = startState;
                res.add(sb.toString());
                sb = new StringBuilder();
            }
        }
        return res;
    }


    public static DFA compile(String pattern) {
        return build(AST.build(pattern));
    }

    private static DFA build(AST ast) {
        Node root = ast.root;
        State startState = new State(root.firstPosSet);
        Set<State> endState = new HashSet<>();
        StateTransitionTable transitionTable = new StateTransitionTable();
        Set<Character> characterSet = ast.characterSet;
        Set<State> visitedState = new HashSet<>();
        Queue<State> stateQueue = new LinkedList<>();
        stateQueue.add(startState);
        while (!stateQueue.isEmpty()) {
            State state = stateQueue.poll();
            visitedState.add(state);
            boolean isEndState = false;
            for (int num : state.numSet)
                if (ast.getLeafNode(num).value == '\0') {
                    isEndState = true;
                    break;
                }
            if (isEndState)
                endState.add(state);
            for (char ch : characterSet) {
                Set<Integer> numSet = new HashSet<>();
                for (int num : state.numSet) {
                    LeafNode leafNode;
                    if ((leafNode = ast.getLeafNode(num)).value == ch)
                        numSet.addAll(leafNode.followPosSet);
                }
                if (!numSet.isEmpty()) {
                    State newState = new State(numSet);
                    transitionTable.addStateTransition(state, ch, newState);
                    if (!visitedState.contains(newState))
                        stateQueue.add(newState);
                }
            }
        }
        return new DFA(startState, endState,visitedState, transitionTable);
    }
}
