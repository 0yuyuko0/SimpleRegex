package regex.node;

import java.util.*;

public abstract class Node {
    private static Map<Character, Integer> operatorPriorityMap = new HashMap<>();
    private static Map<Character, Integer> numOfChildrenMap = new HashMap<>();

    protected static int seq = 0;
    public static Map<Integer, LeafNode> leafNodeMap;

    static {
        operatorPriorityMap.put('|', 1);
        operatorPriorityMap.put('&', 2);
        operatorPriorityMap.put('*', 3);
        operatorPriorityMap.put('?', 3);
        operatorPriorityMap.put('+', 3);
        operatorPriorityMap.put('(', -1);

        numOfChildrenMap.put('|', 2);
        numOfChildrenMap.put('&', 2);
        numOfChildrenMap.put('*', 1);
        numOfChildrenMap.put('?', 1);
        numOfChildrenMap.put('+', 1);
    }

    public List<Node> children;

    public Set<Integer> firstPosSet;

    public Set<Integer> lastPosSet;

    public boolean nullable;

    public Node(List<Node> children) {
        this.children = children;
    }


    public static int comparePriority(char operator1,char operator2) {
        return operatorPriorityMap.get(operator1).compareTo(operatorPriorityMap.get(operator2));
    }

    public static Node newNode(char operator, LinkedList<Node> valueStack) {
        Node newNode = null;
        LinkedList<Node> children = new LinkedList<>();
        for (int i = 0, num = numOfChildrenMap.get(operator); i < num; ++i) {
            children.addFirst(valueStack.pop());
        }
        switch (operator) {
            case '*':
                newNode = new StarNode(children);
                break;
            case '&':
                newNode = new CatNode(children);
                break;
            case '|':
                newNode = new OrNode(children);
                break;
        }
        return newNode;
    }

    public static Node newLeafNode(char value) {
        ++seq;
        LeafNode node = new LeafNode(value);
        leafNodeMap.put(seq, node);
        return node;
    }
}
