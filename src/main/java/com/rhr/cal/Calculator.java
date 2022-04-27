package com.rhr.cal;

import java.util.HashMap;
import java.util.Stack;
import java.util.Vector;

public class Calculator {
    // double类型
    public static final int DEC = 10; // 十进制
    // public static final int HEX = 16;
    public static final int BIN = 2;

    private String expression; // 表达式

    private int mode = DEC; // 获取数字的模式

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private int pointer = 0; // 当前的指针，指向expression的某一字符

    private int false_flag = 0; // 关于表达式是否错误的flag

    Vector<String> expression_out = new Vector<>(); // 输出的后缀表达式，double转化为String
    Stack<String> sign_stack = new Stack<>(); // 对于运算符使用的栈

    private HashMap<String, Integer> privilege_in = new HashMap<>(); // 内部优先级
    private HashMap<String, Integer> privilege_out = new HashMap<>(); // 外部优先级
    /*
    * 优先级越高越大
    * 加减乘除 左移右移 and or xor 两种括号
    * not不是双目运算符，不考虑
    * */

    Calculator(){
        privilege_in.put("(",0);
        privilege_in.put(")",13);

        privilege_out.put(")",0);
        privilege_out.put("(",13);

        privilege_in.put("*",12);
        privilege_in.put("/",12);

        privilege_in.put("+",10);
        privilege_in.put("-",10);

        privilege_in.put("<<",8);
        privilege_in.put(">>",8);
        privilege_in.put("&",6);
        privilege_in.put("^",4);
        privilege_in.put("|",2);

        privilege_out.put("*",11);
        privilege_out.put("/",11);

        privilege_out.put("+",9);
        privilege_out.put("-",9);

        privilege_out.put("<<",7);
        privilege_out.put(">>",7);
        privilege_out.put("&",5);
        privilege_out.put("^",3);
        privilege_out.put("|",1);

        privilege_in.put("#",-1);
        privilege_out.put("#",-1);
        //特殊字符表示栈底部
    }

    public double getAnswer(String exp) {
        // 给外部调用的接口
        if (exp.equals("")){
            return 0;
        }
        pointer = 0;
        false_flag = 0;
        expression_out.clear();
        sign_stack.clear();
        // 初始化

        expression = exp;
        expression += "#"; // 结束符号
        if (expression.charAt(0) == '+' || expression.charAt(0) == '-'){
            expression = "0" + expression;
        }

        bracketMatch();
        checkCorrectness();
        return calculate();
    }

    private void bracketMatch() {
        // 检测括号是否正确
        int len = expression.length();
        int a = 0;
        for (int i = 0; i < len; ++i) {
            if (expression.charAt(i) == '(')
                a++;
            if (expression.charAt(i) == ')')
                a--;
            if (a < 0) {
                CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_BRACKET);
                throw e;
            }
        }

        if (a != 0) {
            CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_BRACKET);
            throw e;
        }
    }

    private void checkCorrectness() {
        /*
        * 首部不能是运算符
        * 不能连续出现两个运算符
        * 不能出现()连续
        *
        * 运算符前方不能是(
        * )的前方不能是运算符
        * ()不被视为运算符
        *
        * (没有要求
        * 运算符前方不能是运算符和(
        * )前方不能是运算符和(
        * 数字的前方不能是)
        * */
        String a = getNext();
        int signBefore = 0;
        int leftBracketBefore = 0;
        int rightBracketBefore = 0;
        if (!isNumber(a) && !a.equals("+") && !a.equals("-") && !a.equals("(")) {
            // 首部出现错误
            CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
            throw e;
        }
        while (!a.equals("#")) {
            if (isNumber(a)) {
                if (rightBracketBefore == 1) {
                    CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
                    throw e;
                }
                signBefore = 0;
                rightBracketBefore = 0;
                leftBracketBefore = 0;
                a = getNext();
            } else {
                if (a.equals("(")) {
                    if (rightBracketBefore == 1) {
                        CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
                        throw e;
                    }
                    leftBracketBefore = 1;
                    signBefore = 0;
                    rightBracketBefore = 0;
                    a = getNext();
                } else if (a.equals(")")) {
                    if (leftBracketBefore == 1 || signBefore == 1) {
                        CalculatorException e=new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
                        throw e;
                    }
                    rightBracketBefore = 1;
                    signBefore = 0;
                    leftBracketBefore = 0;
                    a = getNext();
                } else { // 普通运算符
                    if (signBefore == 1 || leftBracketBefore == 1) {
                        CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
                        throw e;
                    }
                    signBefore = 1;
                    leftBracketBefore = 0;
                    rightBracketBefore = 0;
                    a = getNext();
                }
            }
        }

        pointer = 0;
    }

    private double calculate() {
        // 内部调用的计算接口
        // 先生成后缀表达式， 然后计算

        generate(); // 生成后缀表达式

        Stack<String> s = new Stack<>();
        Vector<String> exp_pre = new Vector<>();
        for (int i = 0; i < expression_out.size(); ++i) {
            if (isNumber(expression_out.get(i))) {
                double num = getNum(expression_out.get(i));
                exp_pre.add(String.valueOf(num));
            } else {
                exp_pre.add(expression_out.get(i));
            }
        }
        int len = exp_pre.size();
        for (int i = 0; i < len; ++i) {
            if (isNumber(exp_pre.get(i))) {
                s.push(exp_pre.get(i));
            } else {
                String op1, op2;
                if (s.size() < 2) {
                    CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
                    throw e;
                }
                op2 = s.pop();
                op1 = s.pop();
                if (!isNumber(op2) || !isNumber(op1)) {
                    CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
                    throw e;
                }
                if (exp_pre.get(i).equals("+")) {
                    String temp=String.valueOf(getNum_dec(op1)+getNum_dec(op2));
                    s.push(temp);
                } else if (exp_pre.get(i).equals("-")) {
                    String temp=String.valueOf(getNum_dec(op1)-getNum_dec(op2));
                    s.push(temp);
                } else if (exp_pre.get(i).equals("*")) {
                    String temp=String.valueOf(getNum_dec(op1)*getNum_dec(op2));
                    s.push(temp);
                } else if (exp_pre.get(i).equals("/")) {
                    if(Double.valueOf(op2) == 0.0) {
                        CalculatorException e=new CalculatorException(CalculatorException.CAL_EXP_DIV_BY_ZERO);
                        throw e;
                    }

                    String temp = String.valueOf(getNum_dec(op1)/getNum_dec(op2));
                    s.push(temp);
                } else if (exp_pre.get(i).equals("<<")) {
                    Double op1_t, op2_t;
                    op1_t = getNum_dec(op1);
                    op2_t = getNum_dec(op2);
                    int op1_i = op1_t.intValue();
                    int op2_i = op2_t.intValue();
                    String temp = String.valueOf(op1_i << op2_i);
                    s.push(temp);
                } else if (exp_pre.get(i).equals(">>")) {
                    Double op1_t, op2_t;
                    op1_t = getNum_dec(op1);
                    op2_t = getNum_dec(op2);
                    int op1_i = op1_t.intValue();
                    int op2_i = op2_t.intValue();
                    String temp = String.valueOf(op1_i >> op2_i);
                    s.push(temp);
                } else if (exp_pre.get(i).equals("&")) {
                    Double op1_t, op2_t;
                    op1_t = getNum_dec(op1);
                    op2_t = getNum_dec(op2);
                    int op1_i = op1_t.intValue();
                    int op2_i = op2_t.intValue();
                    String temp = String.valueOf(op1_i & op2_i);
                    s.push(temp);
                } else if (exp_pre.get(i).equals("|")) {
                    Double op1_t, op2_t;
                    op1_t = getNum_dec(op1);
                    op2_t = getNum_dec(op2);
                    int op1_i = op1_t.intValue();
                    int op2_i = op2_t.intValue();
                    String temp = String.valueOf(op1_i | op2_i);
                    s.push(temp);
                } else if (exp_pre.get(i).equals("^")) {
                    Double op1_t, op2_t;
                    op1_t = getNum_dec(op1);
                    op2_t = getNum_dec(op2);
                    int op1_i = op1_t.intValue();
                    int op2_i = op2_t.intValue();
                    String temp = String.valueOf(op1_i ^ op2_i);
                    s.push(temp);
                }
            }
        }
        return Double.valueOf(s.pop());
    }

    private double getNum(String e) {
        // 获取一个数字
        // 用于计算过程中，而非中缀转后缀的过程中
        double before = 0;
        double after = 0;
        double mode_double = mode;
        double ratio = 1.0 / mode_double;
        int len = e.length();

        int i = 0;
        for (; i < len; ++i) {
            if (e.charAt(i) == '.') {
                i++; // 跳离小数点
                break;
            }
            before *= mode_double;
            if (e.charAt(i) <= '9' && e.charAt(i) >= '0'){
                before += e.charAt(i) - '0';
            }
        }
        for (; i < len; ++i) {
            if (e.charAt(i) <= '9' && e.charAt(i) >= '0') {
                after += (e.charAt(i) - '0') * ratio;
            } else if (e.charAt(i) == '.') {
                CalculatorException b=new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
                throw b;
            }
            ratio /= mode_double;
        }
        return before + after;
    }

    private double getNum_dec(String e) {
        return Double.valueOf(e);
    }

    private String getNext() {
        // 获取下一个元素，同时调整pointer的位置
        if (pointer >= expression.length()) {
            CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_GRAMMAR);
            throw e;
        }
        if (isNumber()) {
            return getNum();
        } else {
            if (expression.charAt(pointer) == '<' || expression.charAt(pointer) == '>') {
                pointer += 2;
                return expression.substring(pointer - 2, pointer);
            }
            pointer += 1;
            return expression.substring(pointer - 1, pointer);
        }
    }

    private String getNum() {
        // 从pointer开始获取一个数字的字符串
        // 会修改pointer的值
        // 最终停下来的地方是一个运算符或者字符串结束符
        int begin = pointer;
        if (mode == DEC) {
            while((expression.charAt(pointer) <= '9' && expression.charAt(pointer) >= '0') || expression.charAt(pointer) == '.') {
                pointer++;
            }
            return expression.substring(begin,pointer);
        } else if (mode == BIN) {
            while ((expression.charAt(pointer) == '0' || expression.charAt(pointer) == '1' || expression.charAt(pointer) == '.')) {
                pointer++;
            }
            return expression.substring(begin, pointer);
        }
        false_flag = 1;
        return expression.substring(0, 1);
    }


    private boolean isNumber() {
        return (expression.charAt(pointer) <= '9' && expression.charAt(pointer) >= '0') || expression.charAt(pointer) == '.';
    }

    public boolean isNumber(String ch) {
        if((ch.charAt(0) <= '9' && ch.charAt(0) >= '0') || ch.charAt(0) == '.')
            return true;
        else if(ch.length() >= 2 && (ch.charAt(0) == '-' || ch.charAt(0) == '+'))
        {
            if((ch.charAt(1) <= '9' && ch.charAt(1) >= '0'))
                return true;
        }

        return false;
    }

    private void generate() {
        // 根据expression生成后缀表达式
        pointer = 0;
        sign_stack.clear();
        sign_stack.push("#"); // 作为栈底

        int len = expression.length();
        expression_out.clear();
        String ch = "#";
        String ch1, op;
        ch = getNext();
        while (!sign_stack.isEmpty()) {
            if (isNumber(ch)) {
                expression_out.add(ch);
                ch = getNext();
            } else {
                while (!sign_stack.isEmpty()) {
                    ch1 = sign_stack.lastElement();

                    if (privilege_in.get(ch1) < privilege_out.get(ch)) {
                        sign_stack.push(ch);
                        ch = getNext();
                        break;
                    } else if (privilege_in.get(ch1) > privilege_out.get(ch)) {
                        expression_out.add(sign_stack.pop());
                    } else {
                        op = sign_stack.pop();
                        if (op.equals("(")) {
                            ch = getNext();
                        }
                    }
                }
            }
        }
    }

    public void clear() {
        expression = "";
        pointer = 0;
        false_flag = 0;
        expression_out.clear();
        sign_stack.clear();
    }
}
