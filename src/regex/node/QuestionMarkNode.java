package regex.node;

import java.util.HashSet;
import java.util.List;

public class QuestionMarkNode extends Node {
    public QuestionMarkNode(List<Node> children) {
        super(children);
        assert children.size() == 1;
        Node child = children.get(0);
        this.nullable = true;
        this.firstPosSet = new HashSet<>(child.firstPosSet);
        this.lastPosSet = new HashSet<>(child.lastPosSet);
    }
}
