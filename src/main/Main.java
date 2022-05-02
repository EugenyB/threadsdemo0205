package main;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main {
    private double totalSum;
    private int finished;

    Lock lock;
    Condition condition;

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        double a = 0;
        double b = Math.PI;
        int n = 1_000_000_000;
        int nThreads = 100;
        double delta = (b-a)/nThreads;
        long start = System.currentTimeMillis();
        List<Future<Double>> futures = new ArrayList<>();
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        for (int i = 0; i < nThreads; i++) {
            CallableCalculator calculator = new CallableCalculator(a + i * delta, a + (i + 1) * delta, n / nThreads, Math::sin);
            futures.add(executorService.submit(calculator));
        }
        executorService.shutdown();
        try {
            totalSum = 0;
            for (Future<Double> future : futures) {
                totalSum += future.get();
            }
            long finish = System.currentTimeMillis();
            System.out.println("result = " + totalSum);
            System.out.println(finish-start);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void run3() {
        double a = 0;
        double b = Math.PI;
        int n = 1_000_000_000;
        int nThreads = 20;
        double delta = (b-a)/nThreads;
        finished = 0;
        totalSum = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < nThreads; i++) {
            ThreadingCalculator calculator = new ThreadingCalculator(a+i*delta, a+(i+1)*delta, n/nThreads, Math::sin, this);
            new Thread(calculator).start();
        }
        lock = new ReentrantLock();
        condition = lock.newCondition();
        try {
            lock.lock();
            while (finished < nThreads) {
                condition.await();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
        long finish = System.currentTimeMillis();
        System.out.println("result = " + totalSum);
        System.out.println(finish-start);
    }

    private void run2() {
        double a = 0;
        double b = Math.PI;
        int n = 1_000_000_000;
        int nThreads = 20;
        double delta = (b-a)/nThreads;
        finished = 0;
        totalSum = 0;
        long start = System.currentTimeMillis();
        for (int i = 0; i < nThreads; i++) {
            ThreadingCalculator calculator = new ThreadingCalculator(a+i*delta, a+(i+1)*delta, n/nThreads, Math::sin, this);
            new Thread(calculator).start();
        }
        try {
            synchronized (this) {
                while (finished < nThreads) {
                    wait();
                }
            }
        } catch (InterruptedException ex) {
            System.err.println("interrupted");
        }
        long finish = System.currentTimeMillis();
        System.out.println("result = " + totalSum);
        System.out.println(finish-start);
    }

    private void run1() {
        double a = 0;
        double b = Math.PI;
        int n = 1_000_000_000;
        IntegralCalculator calculator = new IntegralCalculator(a, b, n, Math::sin);
        double v = calculator.calc();
        System.out.println("v = " + v);
    }

    public void sendResult(double v) {
        try {
            lock.lock();
            totalSum += v;
            finished++;
            condition.signal();
        } finally {
            lock.unlock();
        }
    }
//    public synchronized void sendResult(double v) {
//        totalSum += v;
//        finished++;
//        notify();
//    }
}
