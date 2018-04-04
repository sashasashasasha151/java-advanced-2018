package ru.ifmo.rain.pakulev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> workers;
    private final Queue<Runnable> tasks = new LinkedList<>();
    private boolean isFinished;
    private final Object mutex = new Object();
    private final Object cond = new Object();

    public ParallelMapperImpl(int threads) {
        isFinished = false;
        workers = new ArrayList<Thread>(Collections.nCopies(threads, null));
        for (int i = 0; i < threads; ++i) {
            Thread t = new Thread(() -> {
                for (; ; ) {
                    Runnable func = null;
                    synchronized (mutex) {
                        while (!isFinished && tasks.isEmpty()) {
                            try {
                                mutex.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        if (tasks.size() != 0 && !isFinished) {
                            func = tasks.remove();
                        }
//                        if (func != null) {
//                            func.run();
//                        }
//
//                        mutex.notifyAll();
                    }
                    if (func != null) {
                        func.run();
                    }

                    synchronized (mutex) {
                        mutex.notifyAll();
                        if (isFinished) {
                            return;
                        }
                    }
                }
            });
            workers.set(i, t);
            workers.get(i).start();
        }

    }

    public synchronized <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        ArrayList<R> ans = new ArrayList<>(Collections.nCopies(list.size(), null));
        for (int i = 0; i < list.size(); ++i) {
            final int j = i;
            synchronized (mutex) {
                tasks.add(() -> {
                    R a = function.apply(list.get(j));
                    ans.set(j,a);
                });
                mutex.notify();
            }
        }

        synchronized (mutex) {
            while (!tasks.isEmpty()) {
                mutex.wait();
            }
        }

        return ans;
    }

    public synchronized void close() {
        synchronized (mutex) {
            isFinished = true;
            mutex.notifyAll();
        }
        for (Thread thread : workers) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
