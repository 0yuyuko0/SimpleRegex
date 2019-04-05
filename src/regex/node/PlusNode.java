package regex.node;

import java.util.HashSet;
import java.util.List;

public class PlusNode extends Node {
    public PlusNode(List<Node> children) {
        super(children);
        assert children.size() == 1;
        Node child = children.get(0);
        this.firstPosSet = new HashSet<>(child.firstPosSet);
        this.lastPosSet = new HashSet<>(child.lastPosSet);

        for (Integer lastPos : this.lastPosSet)
            leafNodeMap.get(lastPos).followPosSet.addAll(this.firstPosSet);
    }
}
