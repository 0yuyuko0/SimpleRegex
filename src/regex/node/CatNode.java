package regex.node;

import java.util.HashSet;
import java.util.List;

public class CatNode extends Node {

    public CatNode(char value, List<Node> children) {
        super(value,children);
        assert children.size() == 2;
        Node left = children.get(0);
        Node right = children.get(1);
        this.nullable = left.nullable && right.nullable;

        this.firstPosSet = new HashSet<>(left.firstPosSet);
        if (left.nullable)
            this.firstPosSet.addAll(right.firstPosSet);

        this.lastPosSet = new HashSet<>(right.lastPosSet);
        if (right.nullable)
            this.lastPosSet.addAll(left.lastPosSet);

        for (Integer lastPos : left.lastPosSet){
            leafNodeMap.get(lastPos).followPosSet.addAll(right.firstPosSet);
        }
    }
}
