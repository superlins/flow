package com.zwtech.flow.core.parser.spel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.List;

import static java.lang.invoke.MethodType.methodType;

/**
 * @author renc
 */
public enum Functions {

    REVERSE("reverse", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "reverse", methodType(String.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    LOWERCASE("lowercase", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "lowercase", methodType(String.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    UPPERCASE("uppercase", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "uppercase", methodType(String.class, String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    UUID("uuid", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "uuid", methodType(String.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    NUMBER("number", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "number", methodType(Number.class, Object.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    STRING("string", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "string", methodType(String.class, Object.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    BOXCOX("boxcox", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "boxcox", methodType(int.class, double.class, double.class,
                                double.class, double.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    LINEARMAP("linearmap", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "linearmap", methodType(double.class, double.class, double.class,
                                double.class, double.class, double.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    NONLINEARMAP("nonlinearmap", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "nonlinearmap", methodType(double.class, double.class, double.class,
                                double.class, double.class, double.class, double.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    PIECEWISEMAP("piecewisemap", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup()
                        .findStatic(FunctionHelper.class, "piecewisemap", methodType(double.class, double.class, List.class, List.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    RANDOM("random", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup().findStatic(FunctionHelper.class, "random", methodType(double.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    RANDOMINT("randomint", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup().findStatic(FunctionHelper.class, "randomint", methodType(int.class, int.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    RANDOMLONG("randomlong", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup().findStatic(FunctionHelper.class, "randomlong", methodType(long.class, long.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    RANDOMDOUBLE("randomdouble", new Object() {
        MethodHandle evaluate() {
            try {
                return MethodHandles.lookup().findStatic(FunctionHelper.class, "randomdouble", methodType(double.class, double.class));
            } catch (NoSuchMethodException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }.evaluate()),

    ;

    private final String n;
    private final MethodHandle h;

    Functions(String n, MethodHandle h) {
        this.n = n;
        this.h = h;
    }

    public String n() {
        return n;
    }

    public MethodHandle h() {
        return h;
    }
}