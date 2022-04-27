package com.rhr.cal;

public class CalculatorException extends RuntimeException {
    /*
     * 1. 括号要匹配
     * 2. 不能重复出现两个运算符
     * 3. 不能出现()
     * 4. 首部不能是运算符
     * */

    public static final int CAL_EXP_BRACKET = 1; // 括号错误
    public static final int CAL_EXP_GRAMMAR = 2; // 语法错误
    public static final int CAL_EXP_DIV_BY_ZERO = 3; // 除零错误
    public static final int CAL_EXP_INNER = 4; // 内部错误

    public static int version = 0; // 0中文报错， 1英文报错
    private int index = 0;

    public static final String[] exp_CHN = {"保留","括号不匹配","语法错误","除数不能为0","内部错误，检查Calculator!"};
    public static final String[] exp_ENG= {"reserved"," mismatched parentheses","grammar mistake","divide by 0","inner mistake!"};

    CalculatorException(int a) {
        index = a;
    }

    public String exp_info() {
        if (version == 0) {
            return exp_CHN[index];
        }else {
            return exp_ENG[index];
        }
    }
}
