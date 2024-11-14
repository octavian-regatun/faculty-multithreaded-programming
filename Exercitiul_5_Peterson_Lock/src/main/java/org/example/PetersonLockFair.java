package org.example;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class PetersonLockFair {
    private final int n;
    private final AtomicInteger[] level;
    private final AtomicInteger[] victim;
    private final AtomicBoolean[] waiting;
    private final AtomicInteger[] accessCount;
    private int sharedCounter;
    private final int sumValue = 300000;
    private final int THRESHOLD = 1;

    public PetersonLockFair(int n) {
        this.n = n;
        level = new AtomicInteger[n];
        victim = new AtomicInteger[n];
        waiting = new AtomicBoolean[n];
        accessCount = new AtomicInteger[n];
        sharedCounter = 0;

        for (int i = 0; i < n; i++) {
            level[i] = new AtomicInteger(0);
            victim[i] = new AtomicInteger(0);
            waiting[i] = new AtomicBoolean(false);
            accessCount[i] = new AtomicInteger(0);
        }
    }

    private boolean shouldYield(int i) {
        for (int k = 0; k < n; k++) {
            if (k != i && accessCount[i].get() - accessCount[k].get() >= THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    public void lock(int i) {
        waiting[i].set(true);

        while (waiting[i].get() && shouldYield(i)) {
            Thread.yield();
        }

        for (int L = 1; L < n; L++) {
            level[i].set(L);
            victim[L].set(i);
            boolean otherThr;

            do {
                otherThr = false;
                for (int k = 0; k < n; k++) {
                    if (k != i && level[k].get() >= L) {
                        otherThr = true;
                        if (shouldYield(i)) {
                            level[i].set(0);
                            Thread.yield();
                            level[i].set(L);
                        }
                        break;
                    }
                }
            } while (otherThr && victim[L].get() == i);
        }

        waiting[i].set(false);
    }

    public void unlock(int i) {
        accessCount[i].incrementAndGet();
        level[i].set(0);
    }

    public void runTest() {
        Thread[] threads = new Thread[n];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < n; i++) {
            int id = i;
            threads[i] = new Thread(() -> {
                int localAccessCount = 0;
                while (sharedCounter < sumValue) {
                    lock(id);
                    if (sharedCounter < sumValue) {
                        sharedCounter++;
                        localAccessCount++;
                    }
                    unlock(id);
                }
                System.out.println("Thread " + id + " accessed critical section " +
                        localAccessCount + " times (Total accesses: " +
                        accessCount[id].get() + ")");
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("Execution time: " + (System.currentTimeMillis() - startTime) + "ms");
        System.out.println("Counter: " + sharedCounter);

        int minAccess = Integer.MAX_VALUE;
        int maxAccess = 0;
        for (int i = 0; i < n; i++) {
            int count = accessCount[i].get();
            minAccess = Math.min(minAccess, count);
            maxAccess = Math.max(maxAccess, count);
        }
        System.out.println("Fairness metrics - Max difference between threads: " +
                (maxAccess - minAccess));
    }

    public static void main(String[] args) {
        PetersonLockFair lock = new PetersonLockFair(4);
        lock.runTest();
    }
}