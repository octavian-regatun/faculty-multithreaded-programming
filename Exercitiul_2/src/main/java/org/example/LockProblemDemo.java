package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockProblemDemo {
    static class ProblemLock extends ReentrantLock {
        private boolean shouldFail;

        public ProblemLock(boolean shouldFail) {
            this.shouldFail = shouldFail;
        }

        @Override
        public void lock() {
            if (shouldFail) {
                throw new RuntimeException("Lock acquisition failed!");
            }
            super.lock();
        }
    }

    public static void demonstrateProblem() {
        Lock problemLock = new ProblemLock(true);

        System.out.println("Varianta problematica (lock în try):");
        try {
            try {
                System.out.println("Incercam să obtinem lock-ul...");
                problemLock.lock();
                System.out.println("Am obtinut lock-ul");
            } finally {
                System.out.println("Se executa unlock în finally");
                problemLock.unlock();
            }
        } catch (Exception e) {
            System.out.println("Am prins exceptia: " + e.getMessage());
        }

        System.out.println("\nVarianta corecta (lock inainte de try):");
        try {
            System.out.println("Incercam să obtinem lock-ul...");
            problemLock.lock();
            try {
                System.out.println("Am obtinut lock-ul");
            } finally {
                System.out.println("Se executa unlock în finally");
                problemLock.unlock();
            }
        } catch (Exception e) {
            System.out.println("Am prins exceptia: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        demonstrateProblem();
    }
}