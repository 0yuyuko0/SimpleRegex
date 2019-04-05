package regex.node;

import java.util.HashSet;
import java.util.Set;

public class LeafNode extends Node {
    public final Set<Integer> followPosSet;

    public char value;

    public LeafNode(char value) {
        super(null);
        this.value = value;
        this.nullable = false;
        this.firstPosSet = new HashSet<>();
        this.lastPosSet = new HashSet<>();
        this.followPosSet = new HashSet<>();
        this.firstPosSet.add(seq);
        this.lastPosSet.add(seq);
    }
}
