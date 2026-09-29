package main;

import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

public class IntegralCalculator {
    private final double a;
    private final double b;
    private final int n;
    private final double h;
    private final DoubleUnaryOperator f;

    public IntegralCalculator(double a, double b, int n, DoubleUnaryOperator f) {
        this.a = a;
        this.b = b;
        this.n = n;
        this.h = (b-a)/n;
        this.f = f;
    }

    public double calc() {
        return IntStream.range(0, n).mapToDouble(i -> a + i * h).map(f).sum() * h;
    }
}
