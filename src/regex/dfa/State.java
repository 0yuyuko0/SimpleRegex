package regex.dfa;

import java.util.HashSet;
import java.util.Set;

public class State {
    public Set<Integer> numSet;

    public State(Set<Integer> numSet) {
        this.numSet = numSet;
    }

    public static State emptyState(){
        return new State(new HashSet<>());
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof State))return false;
        return numSet.equals(((State) obj).numSet);
    }

    @Override
    public int hashCode() {
        return numSet.hashCode();
    }
}
