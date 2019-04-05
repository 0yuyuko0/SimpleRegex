package regex.dfa;

import java.util.HashSet;
import java.util.Set;

public class State {
    private static int cnt = 0;

    private int id;

    public State(){
        this.id = ++cnt;
    }

    private State(int id){
        this.id = id;
    }

    public static State emptyState(){
        return new State(Integer.MIN_VALUE);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof State))return false;
        return this.id == ((State) obj).id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return "State "+id;
    }
}
