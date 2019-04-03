package regex.dfa;

import java.util.HashMap;
import java.util.Map;

public class StateTransitionTable {
    private Map<State, Map<Character, State>> transitionMap = new HashMap<>();


    public State trans(State oldState, Character c) {
        Map<Character, State> tmp = transitionMap.get(oldState);
        if (tmp == null)
            return State.emptyState();
        return tmp.compute(c, (k, v) -> v == null ? State.emptyState() : v);
    }

    public void addStateTransition(State oldState, Character c, State newState) {
        transitionMap.computeIfAbsent(oldState, k -> new HashMap<>());
        Map<Character, State> stateMap = transitionMap.get(oldState);
        stateMap.put(c, newState);
    }
}
