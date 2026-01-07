package com.zwtech.flow.core.parser.spel;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Random;
import java.util.UUID;

import static java.lang.Math.*;
import static java.util.Objects.requireNonNull;

/**
 * @author renc
 */
public final class FunctionHelper {

    private static final Random RANDOM = new Random(47);

    public static double random() {
        return RANDOM.nextDouble();
    }

    public static int randomint() {
        return RANDOM.nextInt();
    }

    public static int randomint(int bound) {
        return RANDOM.nextInt(bound);
    }

    // https://github.com/spring-projects/spring-framework/issues/34109
    public static int randomint(int... args) {
        if (args == null || args.length == 0) {
            return RANDOM.nextInt();
        } else if (args.length == 1) {
            return RANDOM.nextInt(args[0]);
        } else {
            return RANDOM.nextInt(args[0], args[1]);
        }
    }

    public static long randomlong() {
        return RANDOM.nextLong();
    }

    public static long randomlong(long bound) {
        return RANDOM.nextLong(bound);
    }

    public static long randomlong(long... args) {
        if (args == null || args.length == 0) {
            return RANDOM.nextLong();
        } else if (args.length == 1) {
            return RANDOM.nextLong(args[0]);
        } else {
            return RANDOM.nextLong(args[0], args[1]);
        }
    }

    public static double randomdouble() {
        return RANDOM.nextDouble();
    }

    public static double randomdouble(double bound) {
        return RANDOM.nextDouble(bound);
    }

    public static double randomdouble(double... args) {
        if (args == null || args.length == 0) {
            return RANDOM.nextDouble();
        } else if (args.length == 1) {
            return RANDOM.nextDouble(args[0]);
        } else {
            return RANDOM.nextDouble(args[0], args[1]);
        }
    }

    public static String reverse(String o) {
        return new StringBuilder(requireNonNull(o)).reverse().toString();
    }

    public static String lowercase(String s) {
        return s.toLowerCase();
    }

    public static String uppercase(String s) {
        return s.toUpperCase();
    }

    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    public static String string(Object o) {
        return switch (o) {
            case null -> null;
            case String s -> s;
            case Number n -> n.toString();
            case Boolean b -> Boolean.toString(b);
            case Character c -> Character.toString(c);
            case java.util.Date date -> new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
            case java.time.LocalDateTime dateTime -> dateTime.toString();
            case java.time.LocalDate date -> date.toString();
            case java.time.LocalTime time -> time.toString();
            default -> o.toString();
        };
    }

    public static Number number(Object o) {
        return switch (o) {
            case null -> null;
            case Number n -> n;
            case String s -> parseStringToNumber(s);
            case Boolean b -> b ? 1 : 0;
            case java.util.Date date -> date.getTime();
            default -> throw new IllegalArgumentException("Unsupported type for conversion to number: " + o.getClass().getName());
        };
    }

    private static Number parseStringToNumber(String str) {
        try {
            if (str.contains(".")) {
                return Double.parseDouble(str);
            } else {
                return Long.parseLong(str);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid string format for number: " + str, e);
        }
    }

    /**
     * 线性映射：将 [minIn, maxIn] 的值映射到 [minOut, maxOut]
     *
     * @param v        输入值
     * @param minIn    输入区间的最小值
     * @param maxIn    输入区间的最大值
     * @param minOut   输出区间的最小值
     * @param maxOut   输出区间的最大值
     * @return 映射后的值
     */
    public static double linearmap(double v, double minIn, double maxIn, double minOut, double maxOut) {
        if (minIn == maxIn) {
            throw new IllegalArgumentException("Input range cannot be zero.");
        }

        double ratio = (v - minIn) / (maxIn - minIn);

        return minOut + ratio * (maxOut - minOut);
    }

    /**
     * 非线性映射：基于对数、指数或幂的映射
     *
     * @param v           输入值
     * @param minIn       输入区间的最小值
     * @param maxIn       输入区间的最大值
     * @param minOut      输出区间的最小值
     * @param maxOut      输出区间的最大值
     * @param power       指定非线性映射的幂次方（例如 0.5 表示平方根，2 表示平方）
     * @return 映射后的值
     */
    public static double nonlinearmap(double v, double minIn, double maxIn, double minOut, double maxOut, double power) {
        if (minIn == maxIn) {
            throw new IllegalArgumentException("Input range cannot be zero.");
        }

        double normalized = (v - minIn) / (maxIn - minIn);
        double transformed = Math.pow(normalized, power);

        return minOut + transformed * (maxOut - minOut);
    }

    /**
     * 分段映射函数：将输入值根据多个区间分别映射到对应的输出区间。
     *
     * @param v             输入值
     * @param inputBounds   输入区间数组，格式为 {min1, max1, min2, max2, ...}
     * @param outputBounds  输出区间数组，格式为 {min1, max1, min2, max2, ...}
     * @return 映射后的值
     */
    public static double piecewisemap(double v, List<Number> inputBounds, List<Number> outputBounds) {
        if (inputBounds.size() != outputBounds.size() || inputBounds.size() % 2 != 0) {
            throw new IllegalArgumentException("Bounds arrays must have the same even length.");
        }

        for (int i = 0; i < inputBounds.size(); i += 2) {
            double minInput = inputBounds.get(i).doubleValue();
            double maxInput = inputBounds.get(i + 1).doubleValue();
            double minOutput = outputBounds.get(i).doubleValue();
            double maxOutput = outputBounds.get(i + 1).doubleValue();

            if (v >= minInput && v <= maxInput) {
                return minOutput + (v - minInput) / (maxInput - minInput) * (maxOutput - minOutput);
            }
        }

        throw new IllegalArgumentException("Value is out of range.");
    }

    public static int boxcox(double prob, double min, double max, double lambda) {
        if (prob <= 0 || prob > 1) {
            return 0;
        }

        double score;
        score = (lambda == 0) ? log(prob) : (pow(prob, lambda) - 1) / lambda;
        score = max(min, min(max, -score));

        int s = (int) round((score - min) * 550 / (max - min) + 300);

        return max(300, min(850, s));
    }
}
