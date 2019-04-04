package regex.dfa;


import regex.ast.AST;
import regex.node.LeafNode;
import regex.node.Node;

import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

public class DFA {
    private static final State EMPTY_STATE = State.emptyState();

    private State startState;
    private Set<State> endStates;
    private Set<State> states;
    private StateTransitionTable transitionTable;

    private DFA(State startState, Set<State> endStates, Set<State> states, StateTransitionTable transitionTable) {
        this.startState = startState;
        this.endStates = endStates;
        this.states = states;
        this.transitionTable = transitionTable;
    }


    public static DFA compile(String pattern) {
        return build(AST.build(pattern));
    }

    private State trans(State oldState, char c) {
        return transitionTable.trans(oldState, c);
    }

    private static boolean isEmptyState(State state) {
        return state.equals(EMPTY_STATE);
    }

    private boolean isEndState(State state) {
        return endStates.contains(state);
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
        DFA dfa = new DFA(startState, endState, visitedState, transitionTable);
        //dfa.minimize(characterSet);
        return dfa;
    }





    public boolean matches(String text) {
        List<String>res = match(text);
        return res.size() == 1 && res.get(0).equals(text);
    }

    public List<String> match(String text) {
        State currState = startState;
        List<String> res = new LinkedList<>();
        boolean visitedEndState = false;
        int mismatchCnt = 0;
        int i = 0;
        int len = text.length();
        while (i < len) {
            StringBuilder sb = new StringBuilder();
            for (; i < len; ++i) {
                char c = text.charAt(i);
                currState = trans(currState, c);
                if (visitedEndState)
                    ++mismatchCnt;//已经转移到过endState结点了
                sb.append(c);
                if (isEmptyState(currState)) {
                    if (visitedEndState) {
                        sb.replace(sb.length() - mismatchCnt, sb.length(), "");//处理转移过程中误匹配的字符
                        res.add(sb.toString());
                    }
                    currState = startState;
                    sb = new StringBuilder();
                    visitedEndState = false;
                } else if (isEndState(currState)) {
                    visitedEndState = true;
                    mismatchCnt = 0;
                }
            }
            if (visitedEndState) {//已经匹配过但还没加入结果
                sb.replace(sb.length() - mismatchCnt, sb.length(), "");
                res.add(sb.toString());
                i -= mismatchCnt;//如果有错误匹配的字符则减掉重新匹配
            }
            currState = startState;
        }
        return res;
    }
}
