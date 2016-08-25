package cn.techwolf.boss.utils;

import cn.techwolf.common.log.LoggerManager;

import java.util.EmptyStackException;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zhaoyalong on 2015/5/30.
 */
public class ScriptEngineUtils {

//    private static class ScriptEngineHolder {
//        public static final ScriptEngine INSTANCE = new ScriptEngineManager().getEngineByName("js");
//    }
//
//    public static <T> T eval(String str) {
//        try {
//            return (T) ScriptEngineHolder.INSTANCE.eval(str);
//        } catch (ScriptException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    /**
     * 计算规则最终结果 公式为 1，0的 与 ,或 操作
     *
     * @param evalStr
     * @return
     */
    public static boolean ruleResult(String evalStr) {
        boolean result = false;
        try {
            evalStr = evalStr.replaceAll("\\s", ""); //去掉所有空格
            Pattern p = Pattern.compile("\\(((?!\\(|\\)).)*?\\)");  //正则寻找最短括号表达式
            Matcher m = p.matcher(evalStr);
            while (m.find()) {  //如果找到 处理子串
                String subStr = m.group();
                Stack<String> s = compute(subStr);  //计算子串表达式 结果
                evalStr = evalStr.replace(subStr, s.pop()); //用结果替换字串
                m = p.matcher(evalStr); //再寻找 直到寻找完毕
            }
            Stack finalStack = compute(evalStr); //计算没有括号的表达式值
            if (!finalStack.isEmpty() && finalStack.size() == 1) {
                result = finalStack.pop().equals("1");
            } else {
                LoggerManager.error("ScriptEngineUtils ruleResult has wrong");
            }
        } catch (Exception e) {
            LoggerManager.error("ScriptEngineUtils ruleResult has wrong", e);
        }
        return result;
    }

    private static Stack<String> compute(String subStr) {
        char[] cs = subStr.toCharArray();
        Stack<String> s = new Stack<String>();
        try {
            for (char c : cs) {
                if (c == '(' || c == ')') {   //遇到括号不处理 跳过
                    continue;
                } else if (s.isEmpty() || (c == '|' || c == '&')) { //栈是空的 或者遇到了符号 入栈
                    if (!s.isEmpty() && (s.peek().equals("|") || s.peek().equals("&"))) {
                        LoggerManager.error("ScriptEngineUtils compute evalStr has wrong:" + subStr);
                        s.push("0");
                        return s;
                    }
                    s.push(String.valueOf(c));
                } else {  //栈不空 且是结果字符 则进行计算 要保证表达式的顺序正确
                    if (!s.peek().equals("|") && !s.peek().equals("&")) {
                        LoggerManager.error("ScriptEngineUtils compute evalStr has wrong:" + subStr);
                        s.push("0");
                        return s;
                    }
                    int one = Integer.parseInt(String.valueOf(c));
                    String op = s.pop();
                    int two = Integer.parseInt(s.pop());
                    int result;
                    if (op.equals("|")) {
                        result = one | two;
                    } else {
                        result = one & two;
                    }
                    s.push(String.valueOf(result)); //结果入栈
                }
            }
        } catch (NumberFormatException e) {
            LoggerManager.error("ScriptEngineUtils compute NumberFormatException", e);
        } catch (EmptyStackException e) {
            LoggerManager.error("ScriptEngineUtils compute EmptyStackException", e);
        }
        return s;
    }

    public static void main(String[] args) {
        System.out.println(ruleResult("( 1 | 0 )&(1|(1&0))"));
    }
}
