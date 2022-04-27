package com.rhr.cal;

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public Calculator myCalculator = new Calculator();

    Button button_equal;
    TextView answerText;
    TextView expressionText;

    Button button_or,button_7,button_8,button_9,button_CE,button_DEL;
    Button button_and,button_4,button_5,button_6,button_plus,button_minus;
    Button button_xor,button_1,button_2,button_3,button_multiple,button_division;
    Button button_left,button_right,button_0,button_dot,button_tel;
    Button button_not;

    String expression;
    String answer;
    double ans_double = 0;
    private int haveAns = 0; // 目前是否存在答案

    Vibrator vb;

    private Animation anim = null;

    private int anim_seq = 0;

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (anim_seq == 0) {
                initAnimate(button_equal);
                anim_seq = 1;
            } else if (anim_seq == 1){
                initAnimate(button_tel);
                initAnimate(button_division);
                anim_seq = 2;
            } else if (anim_seq == 2){
                initAnimate(button_dot);
                initAnimate(button_multiple);
                initAnimate(button_minus);
                anim_seq = 3;
            } else if (anim_seq == 3){
                initAnimate(button_0);
                initAnimate(button_3);
                initAnimate(button_plus);
                initAnimate(button_DEL);
                anim_seq = 4;
            } else if (anim_seq == 4){
                initAnimate(button_not);
                initAnimate(button_2);
                initAnimate(button_6);
                initAnimate(button_CE);
                initAnimate(button_right);
                anim_seq = 5;
            } else if (anim_seq == 5){
                initAnimate(button_1);
                initAnimate(button_5);
                initAnimate(button_9);
                initAnimate(button_left);
                anim_seq = 6;
            } else if (anim_seq == 6) {
                initAnimate(button_4);
                initAnimate(button_8);
                initAnimate(button_xor);
                anim_seq = 7;
            } else if (anim_seq == 7){
                initAnimate(button_7);
                initAnimate(button_and);
                anim_seq = 8;
            } else if (anim_seq == 8){
                initAnimate(button_or);
                anim_seq = -1;
            }

            handler.postDelayed(this, 35);
        }
    };

    SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences("count", 0);
        int count = preferences.getInt("count", 0);
        if (count == 0) {
            Intent intent = new Intent();
            intent.setClass(getApplicationContext(), BootActivity.class);
            startActivity(intent);
//            finish();
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("count", ++count);
        editor.commit();

//        Intent intent = new Intent(this, BootActivity.class);
//        startActivity(intent);


        init();
        setClicks();
        handler.postDelayed(runnable, 35);
    }

    private void init() {
        vb = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        expression = "";
        answer = "";
        button_equal = findViewById(R.id.button_equal);
        answerText = findViewById(R.id.Answer);
        expressionText = findViewById(R.id.Expression);

        button_0 = findViewById(R.id.button_0);
        button_1 = findViewById(R.id.button_1);
        button_2 = findViewById(R.id.button_2);
        button_3 = findViewById(R.id.button_3);
        button_4 = findViewById(R.id.button_4);
        button_5 = findViewById(R.id.button_5);
        button_6 = findViewById(R.id.button_6);
        button_7 = findViewById(R.id.button_7);
        button_8 = findViewById(R.id.button_8);
        button_9 = findViewById(R.id.button_9);
        button_dot = findViewById(R.id.button_dot);

        button_or = findViewById(R.id.button_or);
        button_and = findViewById(R.id.button_and);
        button_xor = findViewById(R.id.button_xor);
        button_left = findViewById(R.id.button_left);
        button_right = findViewById(R.id.button_right);
        button_not = findViewById(R.id.button_not);

        button_DEL = findViewById(R.id.button_DEL);
        button_plus = findViewById(R.id.button_plus);
        button_minus = findViewById(R.id.button_minus);
        button_multiple = findViewById(R.id.button_multiple);
        button_division = findViewById(R.id.button_division);
        button_CE = findViewById(R.id.button_CE);

        button_tel = findViewById(R.id.button_tel);
    }

    private void initAnimate(Button e) {
        e.setVisibility(View.INVISIBLE);
        anim = AnimationUtils.loadAnimation(this, R.anim.anims);
        e.startAnimation(anim);
        e.setVisibility(View.VISIBLE);
    }

    String getBinString(double res) {
        long a = (long) res;
        String return_val = "";
        String sign = "";
        if (a < 0) {
            a = ~a + 1;
            sign = "-";
        }
        while (a > 0) {
            long temp = a % 2;
            a /= 2;
            return_val = String.valueOf(temp) + return_val;
        }
        return sign + return_val;
    }

    private void setAnswer(double res) {
        ans_double = res;
        haveAns = 1;
        if (myCalculator.getMode() == Calculator.DEC) {
            answerText.setText(String.valueOf(res));
        } else if (myCalculator.getMode() == Calculator.BIN) {
            answerText.setText(getBinString(res));
        }
    }

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
//                CalculatorException e = new CalculatorException(CalculatorException.CAL_EXP_INNER);
//                throw e;
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

    private void setUnClickable(Button b){
        if (!b.isClickable()) {
            return;
        }
        b.setClickable(false);
        b.setTextColor(0xff000000);
        anim = AnimationUtils.loadAnimation(this, R.anim.scale_smaller);
        b.startAnimation(anim);
    }

    private void setClickable(Button b){
        if (b.isClickable()) {
            return;
        }
        b.setClickable(true);
        b.setTextColor(0xffc8c8c8);
        anim = AnimationUtils.loadAnimation(this, R.anim.scale_bigger);
        b.startAnimation(anim);
    }

    private void setClicks() {
        button_equal = findViewById(R.id.button_equal);
        answerText = findViewById(R.id.Answer);
        expressionText = findViewById(R.id.Expression);

        button_equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                press_equal();
            }
        });
        button_CE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                press_clear();
            }
        });
        button_DEL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                press_del();
            }
        });
        button_tel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                press_tel();
            }
        });
        button_not.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                press_not();
            }
        });

        // 通用的Listener
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

        View.OnTouchListener UniversalTouchListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                Button b = (Button) view;
                if (b.isClickable()) {
                    if (motionEvent.getAction() == ACTION_DOWN) {
                        b.setBackgroundColor(0xffe6e6e6);
                    }
                    if (motionEvent.getAction() == ACTION_UP) {
                        String name = String.valueOf(b.getText());
                        if (myCalculator.isNumber(name) && !name.equals("CE") && !name.equals("DEL")) {
                            b.setBackgroundColor(0xfffafafa);
                        } else {
                            b.setBackgroundColor(0xfff0f0f0);
                        }
                        return false;
                    }
                }
                return false;
            }
        };

        button_0.setOnClickListener(UniversalClickListener);
        button_1.setOnClickListener(UniversalClickListener);
        button_2.setOnClickListener(UniversalClickListener);
        button_3.setOnClickListener(UniversalClickListener);
        button_4.setOnClickListener(UniversalClickListener);
        button_5.setOnClickListener(UniversalClickListener);
        button_6.setOnClickListener(UniversalClickListener);
        button_7.setOnClickListener(UniversalClickListener);
        button_8.setOnClickListener(UniversalClickListener);
        button_9.setOnClickListener(UniversalClickListener);
        button_dot.setOnClickListener(UniversalClickListener);

        button_or.setOnClickListener(UniversalClickListener);
        button_and.setOnClickListener(UniversalClickListener);
        button_xor.setOnClickListener(UniversalClickListener);
        button_left.setOnClickListener(UniversalClickListener);
        button_right.setOnClickListener(UniversalClickListener);

        button_plus.setOnClickListener(UniversalClickListener);
        button_minus.setOnClickListener(UniversalClickListener);
        button_multiple.setOnClickListener(UniversalClickListener);
        button_division.setOnClickListener(UniversalClickListener);

        button_0.setOnTouchListener(UniversalTouchListener);
        button_1.setOnTouchListener(UniversalTouchListener);
        button_2.setOnTouchListener(UniversalTouchListener);
        button_3.setOnTouchListener(UniversalTouchListener);
        button_4.setOnTouchListener(UniversalTouchListener);
        button_5.setOnTouchListener(UniversalTouchListener);
        button_6.setOnTouchListener(UniversalTouchListener);
        button_7.setOnTouchListener(UniversalTouchListener);
        button_8.setOnTouchListener(UniversalTouchListener);
        button_9.setOnTouchListener(UniversalTouchListener);
        button_dot.setOnTouchListener(UniversalTouchListener);

//        button_not.setOnTouchListener(UniversalTouchListener);
        button_or.setOnTouchListener(UniversalTouchListener);
        button_and.setOnTouchListener(UniversalTouchListener);
        button_xor.setOnTouchListener(UniversalTouchListener);
        button_left.setOnTouchListener(UniversalTouchListener);
        button_right.setOnTouchListener(UniversalTouchListener);


        button_plus.setOnTouchListener(UniversalTouchListener);
        button_minus.setOnTouchListener(UniversalTouchListener);
        button_multiple.setOnTouchListener(UniversalTouchListener);
        button_division.setOnTouchListener(UniversalTouchListener);

        // 更换模式

        button_plus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                myCalculator.setMode(Calculator.BIN);
                expression = "";
                answer = "";
                myCalculator.clear();
                setClickable(button_0);
                setClickable(button_1);

                setClickable(button_not);
                setClickable(button_or);
                setClickable(button_and);
                setClickable(button_xor);
                setClickable(button_left);
                setClickable(button_right);

                setUnClickable(button_2);
                setUnClickable(button_3);
                setUnClickable(button_4);
                setUnClickable(button_5);
                setUnClickable(button_6);
                setUnClickable(button_7);
                setUnClickable(button_8);
                setUnClickable(button_9);

                vb.vibrate(40);
                if (haveAns == 1) {
                    setAnswer(ans_double);
                }
                makeText("BIN");
                return true;
            }
        });

        button_minus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                myCalculator.setMode(Calculator.DEC);
                myCalculator.setMode(Calculator.DEC);
                expression = "";
                answer = "";
                myCalculator.clear();

                setClickable(button_0);
                setClickable(button_1);
                setClickable(button_2);
                setClickable(button_3);
                setClickable(button_4);
                setClickable(button_5);
                setClickable(button_6);
                setClickable(button_7);
                setClickable(button_8);
                setClickable(button_9);


                vb.vibrate(40);
                if (haveAns == 1) {
                    setAnswer(ans_double);
                }
                makeText("DEC");
                return true;
            }
        });

        button_multiple.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                expression += "(";
                expressionText.setText(expression);
                vb.vibrate(10);
                return true;
            }
        });

        button_division.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                expression += ")";
                expressionText.setText(expression);
                vb.vibrate(10);
                return true;
            }
        });
    }

    private void makeText(String e) {
        Toast.makeText(this, e, Toast.LENGTH_SHORT).show();
    }

}