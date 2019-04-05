package regex.ast;

import regex.node.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static regex.node.Node.*;

public class AST {
    public Node root;

    public Set<Character> characterSet;

    public Map<Integer, LeafNode> leafNodeMap;

    private static Set<Character> reservedCharacters = new HashSet<>();

    static {
        reservedCharacters.add('|');
        reservedCharacters.add('&');
        reservedCharacters.add('*');
        reservedCharacters.add('?');
        reservedCharacters.add('+');
        reservedCharacters.add('(');
    }

    public AST(Node root, Set<Character> characterSet, Map<Integer, LeafNode> leafNodeMap) {
        this.root = root;
        this.characterSet = characterSet;
        this.leafNodeMap = leafNodeMap;
    }

    public static AST buildAST(String pattern) {//双栈法构建抽象语法树
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
                case '|':
                    isPrevOperator = true;
                case '+':
                case '*':
                case '?':
                    operator = c;
                    while (!operatorStack.isEmpty() && comparePriority(operatorStack.peek(), operator) >= 0)
                        valueStack.push(newNode(operatorStack.pop(), valueStack));
                    operatorStack.push(operator);
                    break;
                case '(':
                    if (!isPrevOperator && !isFirst) {//前面是字符
                        while (!operatorStack.isEmpty() && comparePriority(operatorStack.peek(), '&') >= 0)
                            valueStack.push(newNode(operatorStack.pop(), valueStack));
                        operatorStack.push('&');
                    }
                    operatorStack.push('(');
                    isFirst = true;
                    break;
                case ')':
                    while (operatorStack.peek() != '(')
                        valueStack.push(newNode(operatorStack.pop(), valueStack));
                    operatorStack.pop();
                    break;
                case '[':
                    StringBuilder sb = new StringBuilder("(");
                    int j;
                    for (j = i + 1; (c = pattern.charAt(j)) != ']'; ++j) {
                        if (c == '-') {
                            if(pattern.charAt(j+1) == ']') {
                                sb.append('-');
                                continue;
                            }
                            sb.deleteCharAt(sb.length() - 1);
                            sb.deleteCharAt(sb.length() - 1);
                            if(reservedCharacters.contains(pattern.charAt(j-1)))
                                sb.deleteCharAt(sb.length()-1);
                            int fromCh = pattern.charAt(j - 1);
                            int toCh = pattern.charAt(j + 1);
                            char finalC = c;
                            sb.append(
                                    IntStream.range(fromCh, toCh + 1)
                                            .mapToObj(
                                                    ch ->  reservedCharacters.contains(finalC) ?
                                                            new String(new char[]{'\\',finalC}) :
                                                            String.valueOf((char)ch))
                                            .collect(Collectors.joining("|"))
                            );
                            ++j;
                        } else {
                            if (reservedCharacters.contains(c))//需要转义
                                sb.append('\\');
                            sb.append(c).append('|');
                        }
                    }
                    sb.append(')');
                    pattern = new StringBuilder(pattern).replace(i, j + 1, sb.toString()).toString();
                    --i;
                    break;
                case '\\':
                    c = pattern.charAt(++i);
                default:
                    characterSet.add(c);
                    if (!isPrevOperator && !isFirst) {//前面也是字符
                        operator = '&';
                        while (!operatorStack.isEmpty() &&
                                comparePriority(operatorStack.peek(), operator) >= 0) //先处理优先级高的
                            valueStack.push(newNode(operatorStack.pop(), valueStack));
                        operatorStack.push(operator);
                    } else
                        isPrevOperator = false;
                    isFirst = false;
                    valueStack.push(newLeafNode(c));
            }
        }
        while (!operatorStack.isEmpty())
            valueStack.push(newNode(operatorStack.pop(), valueStack));
        valueStack.push(newLeafNode('\0'));
        valueStack.push(newNode('&', valueStack));
        return new AST(valueStack.get(0), characterSet, Node.leafNodeMap);
    }

    public LeafNode getLeafNode(int num) {
        return leafNodeMap.get(num);
    }
}
