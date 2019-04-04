package regex.ast;

import regex.node.*;

import java.util.*;
import static regex.node.Node.*;

public class AST {
    public Node root;

    public Set<Character>characterSet;

    public Map<Integer,LeafNode>leafNodeMap;

    public AST(Node root, Set<Character> characterSet,Map<Integer,LeafNode>leafNodeMap) {
        this.root = root;
        this.characterSet = characterSet;
        this.leafNodeMap = leafNodeMap;
    }

    public static AST build(String pattern) {//双栈法构建抽象语法树
        Node.leafNodeMap = new HashMap<>();
        LinkedList<Node> valueStack = new LinkedList<>();
        LinkedList<Character> operatorStack = new LinkedList<>();
        Set<Character> characterSet = new LinkedHashSet<>();
        boolean isPrevOperator = false;
        boolean isFirst = true;
        char operator = '\0';
        for (int i = 0; i < pattern.length(); ++i) {
            char c = pattern.charAt(i);
            switch (c) {
                case '|':isPrevOperator = true;
                case '*':
                    operator = c;
                    while (!operatorStack.isEmpty() && comparePriority(operatorStack.peek(), operator) >= 0)
                        valueStack.push(newNode(operatorStack.pop(), valueStack));
                    operatorStack.push(operator);
                    break;
                case '(':
                    if(!isPrevOperator&&!isFirst)//前面是字符
                        operatorStack.push('&');
                    operatorStack.push('(');
                    isFirst = true;
                    break;
                case ')':
                    while(operatorStack.peek() !='(')
                        valueStack.push(newNode(operatorStack.pop(), valueStack));
                    operatorStack.pop();
                    break;
                default:
                    characterSet.add(c);
                    if (!isPrevOperator && !isFirst) {//前面也是字符
                        operator = '&';
                        while (!operatorStack.isEmpty() &&
                                comparePriority(operatorStack.peek(), operator) >= 0) //先处理优先级高的
                            valueStack.push(newNode(operatorStack.pop(), valueStack));
                        operatorStack.push(operator);
                    }else
                        isPrevOperator = false;
                    isFirst = false;
                    valueStack.push(newLeafNode(c));
            }
        }
        while (!operatorStack.isEmpty())
            valueStack.push(newNode(operatorStack.pop(), valueStack));
        valueStack.push(newLeafNode('\0'));
        valueStack.push(newNode('&', valueStack));
        return new AST(valueStack.get(0), characterSet,Node.leafNodeMap);
    }

    public LeafNode getLeafNode(int num){
        return leafNodeMap.get(num);
    }
}
