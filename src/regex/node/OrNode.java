package regex.node;

import java.util.HashSet;
import java.util.List;

public class OrNode extends Node {

    public OrNode(List<Node> children) {
        super(children);
        assert children.size() == 2;
        Node left = children.get(0);
        Node right = children.get(1);
        this.nullable = left.nullable || right.nullable;
        this.firstPosSet = new HashSet<>(left.firstPosSet);
        this.firstPosSet.addAll(right.firstPosSet);
        this.lastPosSet = new HashSet<>(right.lastPosSet);
        this.lastPosSet.addAll(left.lastPosSet);

    }
}
