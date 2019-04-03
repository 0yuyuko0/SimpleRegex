package regex.node;

import java.util.HashSet;
import java.util.List;

public class StarNode extends Node {
    public StarNode(char value, List<Node> children) {
        super(value, children);
        assert children.size() == 1;
        Node child = children.get(0);

        this.nullable = true;
        this.firstPosSet = new HashSet<>(child.firstPosSet);
        this.lastPosSet = new HashSet<>(child.lastPosSet);

        for (Integer lastPos : this.lastPosSet)
            leafNodeMap.get(lastPos).followPosSet.addAll(this.firstPosSet);
    }
}
