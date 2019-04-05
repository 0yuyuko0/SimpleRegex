package regex.dfa;


import regex.ast.AST;
import regex.node.LeafNode;
import regex.node.Node;

import java.util.*;
import java.util.stream.Collectors;

public class DFA {
    static final State EMPTY_STATE = State.emptyState();

    private State startState;
    private Set<State> endStates;
    private Set<State> states;
    private Set<Character> characterSet;
    private StateTransitionTable transitionTable;


    public DFA(State startState, Set<State> endStates, Set<State> states, Set<Character> characterSet, StateTransitionTable transitionTable) {
        this.startState = startState;
        this.endStates = endStates;
        this.states = states;
        this.characterSet = characterSet;
        this.transitionTable = transitionTable;
    }

    public static DFA compile(String pattern) {
        return build(AST.buildAST(pattern));
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
        State startState = new State();
        Set<State> endState = new HashSet<>();
        StateTransitionTable transitionTable = new StateTransitionTable();
        Set<Character> characterSet = ast.characterSet;
        Map<State, Set<Integer>> visitedStateMap = new HashMap<>();
        Map<Set<Integer>, State> numSetToStateMap = new HashMap<>();
        numSetToStateMap.put(root.firstPosSet, startState);
        visitedStateMap.put(startState, root.firstPosSet);
        Queue<State> stateQueue = new LinkedList<>();
        stateQueue.add(startState);

        while (!stateQueue.isEmpty()) {
            State currState = stateQueue.poll();
            boolean isEndState = false;
            Set<Integer> numSetOfCurrState = visitedStateMap.get(currState);
            for (int num : numSetOfCurrState)
                if (ast.getLeafNode(num).value == '\0') {
                    isEndState = true;
                    break;
                }
            if (isEndState)
                endState.add(currState);
            for (char ch : characterSet) {
                Set<Integer> newNumSet = new HashSet<>();
                for (int num : numSetOfCurrState) {
                    LeafNode leafNode;
                    if ((leafNode = ast.getLeafNode(num)).value == ch)
                        newNumSet.addAll(leafNode.followPosSet);
                }
                if (!newNumSet.isEmpty()) {
                    State newState;
                    if (numSetToStateMap.containsKey(newNumSet))
                        newState = numSetToStateMap.get(newNumSet);
                    else {
                        newState = new State();
                        visitedStateMap.put(newState, newNumSet);
                        numSetToStateMap.put(newNumSet, newState);
                        stateQueue.add(newState);
                    }
                    transitionTable.addStateTransition(currState, ch, newState);
                }
            }
        }

        DFA dfa = new DFA(startState, endState, visitedStateMap.keySet(), characterSet, transitionTable);
        //dfa.minimize(characterSet);
        return dfa;
    }

    //
    public int minimize() {
        int numberOfStateBeforeMinimize = states.size();
        List<Set<State>> newGroupList = new ArrayList<>();
        List<Set<State>> oldGroupList;
        Set<State> allStates = new HashSet<>(states);
        Set<State> endStates = new HashSet<>(this.endStates);
        allStates.removeAll(endStates);
        if (!allStates.isEmpty())//如果state都是endState，则为空集了
            newGroupList.add(allStates);
        newGroupList.add(endStates);
        do {
            oldGroupList = new ArrayList<>(newGroupList);
            for (Set<State> oldGroup : oldGroupList) {
                if (oldGroup.size() <= 1) continue;//不可切分
                List<Set<State>> listOfGroupToMinimize = new ArrayList<>();
                listOfGroupToMinimize.add(oldGroup);
                for (char c : this.characterSet) {
                    boolean canOldGroupMinimize = false;
                    Map<Integer, Set<State>> tmpGroupMap = new HashMap<>();
                    int tmpGroupNumber = 0;//处理空State，空State组不能相等
                    for (State oldState : oldGroup) {
                        State newState = trans(oldState, c);
                        int newGroupNumber = isEmptyState(newState) ? newGroupList.size() + (++tmpGroupNumber) :
                                groupNumberOfState(newState, newGroupList);
                        tmpGroupMap.putIfAbsent(newGroupNumber, new HashSet<>());
                        tmpGroupMap.get(newGroupNumber).add(oldState);
                    }
                    Set<Set<State>> groupToRemove = new HashSet<>();
                    Set<Set<State>> groupToAdd = new HashSet<>();
                    for (Set<State> tmpGroup : tmpGroupMap.values()) {
                        for (Set<State> groupToMinimize : listOfGroupToMinimize) {
                            if (groupToMinimize.containsAll(tmpGroup)
                                    && tmpGroup.size() < groupToMinimize.size()) {//组被切分成更小的组了
                                groupToRemove.add(groupToMinimize);
                                groupToAdd.add(tmpGroup);
                                canOldGroupMinimize = true;
                            }
                        }
                    }
                    if (canOldGroupMinimize) {//可以最小化
                        listOfGroupToMinimize.removeAll(groupToRemove);
                        listOfGroupToMinimize.addAll(groupToAdd);
                    }
                }
                if (listOfGroupToMinimize.size() != 1) {//最小化过
                    newGroupList.remove(oldGroup);
                    newGroupList.addAll(listOfGroupToMinimize);
                }
            }
        } while (!oldGroupList.equals(newGroupList));

        Set<State> newStartStateCandidates = new HashSet<>(findGroup(this.startState, newGroupList));
        Set<Set<State>> newEndStatesCandidates =
                this.endStates.stream()
                        .map(es -> findGroup(es, newGroupList))
                        .collect(Collectors.toSet());
        Map<Set<State>, State> statesToReplaceMap = new HashMap<>();

        State newStartState = newStartStateCandidates.iterator().next();
        newStartStateCandidates.remove(newStartState);
        statesToReplaceMap.put(newStartStateCandidates, newStartState);

        Set<State> newEndStates = new HashSet<>();
        newEndStatesCandidates
                .forEach(
                        candidates -> {
                            State newEndState = candidates.iterator().next();
                            newEndStates.add(newEndState);
                            candidates.remove(newEndState);
                            statesToReplaceMap.put(candidates, newEndState);
                        }
                );

        this.startState = newStartState;
        this.endStates = newEndStates;
        statesToReplaceMap.keySet()
                .forEach(statesToReplace -> this.states.removeAll(statesToReplace));
        this.transitionTable.update(statesToReplaceMap);
        return numberOfStateBeforeMinimize - this.states.size();
    }

    private static Set<State> findGroup(State state, List<Set<State>> groupList) {
        ListIterator<Set<State>> iterator = groupList.listIterator();
        while (iterator.hasNext()) {
            Set<State> group = iterator.next();
            if (group.contains(state))
                return group;
        }
        throw new RuntimeException("没有组的State！！！");
    }

    private static int groupNumberOfState(State state, List<Set<State>> groupList) {
        ListIterator<Set<State>> iterator = groupList.listIterator();
        while (iterator.hasNext())
            if (iterator.next().contains(state))
                return iterator.previousIndex();
        throw new RuntimeException("没有组的State！！！");
    }


    public boolean matches(String text) {
        List<String> res = match(text);
        return res.size() == 1 && res.get(0).equals(text);
    }

    public List<String> match(String text) {
        State currState = startState;
        List<String> res = new LinkedList<>();
        int mismatchCnt = 0;
        int i = 0;
        int len = text.length();
        if(text.equals(""))
            res.add("");
        while (i < len) {
            boolean visitedEndState = false;
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
