package org.example;
import java.util.concurrent.atomic.AtomicInteger;

public class PetersonLock{
    private final int n;
    private final AtomicInteger[] level;
    private final AtomicInteger[] victim;
    private int sharedCounter;
    private final int LIMIT = 300000;

    public PetersonLock(int n) {
        this.n = n;
        level = new AtomicInteger[n];
        victim = new AtomicInteger[n];
        sharedCounter = 0;
        for (int i = 0; i < n; i++) {
            level[i] = new AtomicInteger(0);
            victim[i] = new AtomicInteger(0);
        }
    }

    public void lock(int i) {
        for (int L = 1; L < n; L++) {
            level[i].set(L);
            victim[L].set(i);
            boolean otherThr;
            do {
                otherThr = false;
                for (int k = 0; k < n; k++) {
                    if (k != i && level[k].get() >= L) {
                        otherThr= true;
                        break;
                    }
                }
            } while (otherThr && victim[L].get() == i);
        }
    }

    public void unlock(int i) {
        level[i].set(0);
    }




    public void runTest() {
        Thread[] threads = new Thread[n];
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < n; i++) {
            int id = i;
            threads[i] = new Thread(() -> {
                int localAccessCount = 0;
                while (sharedCounter < LIMIT) {
                    lock(id);
                    if (sharedCounter < LIMIT) {
                        sharedCounter++;
                        localAccessCount++;
                    }
                    unlock(id);
                }
                System.out.println("Thread " + id + " counter " + localAccessCount + " times.");
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
    }

    public static void main(String[] args) {
        PetersonLock lock = new PetersonLock(4);
        lock.runTest();
    }
}

