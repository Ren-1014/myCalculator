## MyCalculator

简介：一个简单的计算器app，可以实现基本的十进制和二进制运算。

ref: https://github.com/grkid/Cal

#### 1、功能

使用说明：

- 长按`+`：转换二进制模式
- 长按`-`：转换十进制模式
- 长按`x`：生成`(`
- 长按`÷`：生成`)`

其他说明：

1. 二进制模式下数字2~9不可使用
2. 十进制模式下`|`、`&`、`^`、`<<`、`>>`不可使用
3. 清除键：`CE`，删除键：`DEL`
4. `~`为单目运算符，在按下`=`键后使用

> 版本1.2
>
> - 修复`<<`和`>>`运算bug；
> - 添加首次打开app的引导界面

#### 2、实现原理

算法原理是本项目最核心的部分，因为我们人用的都是中缀表达式，即形如`1+(2+3)/2`，而对于计算机则难以通过其计算，更好的方式是转化为后缀表达式，其不再包含括号，也不再考虑运算符优先级，一律从左到右计算。

后缀表达式的规则：

- 先从左到右依次入栈；

- 当是数字的时候直接入栈；
- 当是运算符号的时候，就将栈的最上面两个数拿出进行运算 后 再将结果进栈 记住（栈顶元素永远在运算符号的右边）。

中缀到后缀转换规则：

- 括号的优先级最低、其余运算符优先级与我们日常使用一致；
- 遇到数字，直接输出到后缀表达式中（不进行压栈）；
- 遇到左括号，将它压入栈中；
- 遇到右括号，将栈顶的运算符依次弹出并输出到后缀表达式中（直到遇到左括号停止，左括号只弹出不输出）；
- 遇到运算符，分两种情况处理：
  1. 当前运算符优先级 > 栈顶运算符优先级，进行压栈；
  2. 当前运算符优先级 <= 栈顶运算符优先级，将栈顶运算符弹出并输出到后缀表达式中，然后前运算符继续和新的栈顶运算符进行优先级比较，直到当前运算符优先级 > 栈顶运算符优先级停止，如果已经到了中缀表达式的结束位置，中缀表达式处理完毕。
- 最后，别忘了将栈中还没有弹出的运算符依次弹出并输出到后缀表达式中。



#### 3、实现过程

##### 3.1 Calculator

首先，作为一个计算器，最核心的功能便是得到express表达式后进行计算，我的设计是实现一个`Calculator`类，完成一个给外部调用的接口`getAnswei()`，具体实现过程如下：

首先完成`Calculator`类的初始化：

```java
	public static final int DEC = 10; // 十进制
    // public static final int HEX = 16;
    public static final int BIN = 2;
    private String expression; // 表达式
    private int mode = DEC; // 获取数字的模式
	Vector<String> expression_out = new Vector<>(); // 输出的后缀表达式，double转化为String
    Stack<String> sign_stack = new Stack<>(); // 对于运算符使用的栈

    private HashMap<String, Integer> privilege_in = new HashMap<>(); // 内部优先级
    private HashMap<String, Integer> privilege_out = new HashMap<>(); // 外部优先级
```

`getAnswer()`主要分为两个步骤：检测异常和计算。检测异常又分为`bracketMatch()`和`checkCorrectness()`，即括号检测和语法检测；计算也分为中缀转后缀、计算后缀，即`generate()`和`calculator()`。

```java
Calculator(){privilege}

public double getAnswer(String exp) { // 外部调用的接口
    // 先初始化
    backetMatch();
    checkCorrectness();
    return calculate();
}

private void bracketMatch(){}

private void checkCorrectness(){
    String a = getNext();
    int signBefore = 0;
    int leftBracketBefore = 0;
    int rightBracketBefore = 0;
}

private double calculate() {
    generate(); // 生成后缀表达式
    Stack<String> s = new Stack<>();
    Vector<String> exp_pre = new Vector<>();
    
    return Double.valueof(s.pop());
}

private void generate() { // 根据expression生成后缀表达式
    
}

private double getNum(String e);
private double getNum_dec(String e);
private String getNext();
private String getNum();
private boolean isNumber();
public boolean isNumber(String ch);
public void clear();
```

`CalculatorException`：

```java
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
```

`Calculator`完成之后，便可以开始设计GUI了！

##### 3.2 GUI

我的设计很简答，首先设计一个首次开启app的引导界面`BootActivity`，只在安装app后首次打开时出现，使用`SharedPreferences`实现：

`BootActivity`

```java
public class BootActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_boot);

        Button b_return  = findViewById(R.id.button_return);
        b_return.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}
```

`MainActivity`

```java
SharedPreferences preferences;

onCreate(Bundle savedInstanceState) {
    preferences = getSharedPreferences("count", 0);
        int count = preferences.getInt("count", 0);
        if (count == 0) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), BootActivity.class);
            startActivity(intent);
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("count", ++count);
        editor.commit();
}
```

UI刷新则使用一个最普通的方法：

```java
Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {initAnimate}
    }
onCreate(Bundle savedInstanceState) {
    	init();
        setClicks();
        handler.postDelayed(runnable, 35);
}
```

其中，`init()`初始化，注册按钮、`answerText`等；`setClicks()`主要分为两类，即普通按键和特殊按键。

```java
// UniversalClickListener
View.OnClickListener UniversalClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button temp = (Button) view;
                if (temp.isClickable()) {
                    String name = String.valueOf(temp.getText());
                    if (name.equals("÷")) {
                        name = "/";
                    }
                    if (name.equals("×")) {
                        name = "*";
                    }
                    expression += name;
                    expressionText.setText(expression);
                }
            }
        };
```

```java
// specialClickListener
button_equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                press_equal();
            }
        });
button_DEL button_CE button_tel button_not button_CE
// 其中not为单目运算符，应在press_equal后使用
```

```java
private void press_equal() {
        try {
            double res = myCalculator.getAnswer(String.valueOf(expressionText.getText()));
            ans_double = res;
            setAnswer(res);
        } catch (CalculatorException e) {
            haveAns = 0;
            answerText.setText(e.exp_info());
            ans_double = 0;
        }

        vb.vibrate(20);
    }

    private void press_clear() {
        expression = "";
        answer = "";
        ans_double = 0;
        expressionText.setText(expression);
        answerText.setText(answer);
        myCalculator.clear();
        vb.vibrate(40);
    }

    private void press_del() {
        if (expression.length() != 0) {
            if (expression.charAt(expression.length() - 1) == '<' || expression.charAt(expression.length() - 1) == '>') {
                expression = expression.substring(0, expression.length() - 2);
            } else {
                expression = expression.substring(0, expression.length() - 1);
            }
        }
        expressionText.setText(expression);
        vb.vibrate(40);
    }

    private void press_tel() {
        StringBuilder phoneNum= new StringBuilder();
        for (int i = 0; i < expression.length(); ++i) {
            char ch = expression.charAt(i);
            if (ch > '9' || ch < '0') {
                continue;
            }
            phoneNum.append(ch);
        }
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        Uri uri = Uri.parse("tel:" + phoneNum.toString());
        intent.setData(uri);
        startActivity(intent);
    }

    private void press_not() {
        if (haveAns == 1) {
            long a = new Double(ans_double).longValue();
            a = ~a;
//            answerText.setText(String.valueOf(a));
            answerText.setText(getBinString(a));
        }
    }
	private void makeText(String e) {
        Toast.makeText(this, e, Toast.LENGTH_SHORT).show();
    }
```

至此，一个简单的计算器application就完成了！

