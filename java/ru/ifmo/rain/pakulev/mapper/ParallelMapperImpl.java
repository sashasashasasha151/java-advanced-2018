package ru.ifmo.rain.pakulev.mapper;

import info.kgeorgiy.java.advanced.mapper.ParallelMapper;

import java.util.*;
import java.util.function.Function;

public class ParallelMapperImpl implements ParallelMapper {

    private List<Thread> workers;
    private final Queue<Runnable> tasks = new LinkedList<>();
    private boolean isFinished;
    private final Object mutex = new Object();

    public ParallelMapperImpl(int threads) {
        isFinished = false;
        workers = new ArrayList<Thread>(Collections.nCopies(threads, null));
        for (int i = 0; i < threads; ++i) {
            Thread t = new Thread(() -> {
                while (true) {
                    Runnable func = null;
                    synchronized (mutex) {
                        while (!isFinished && tasks.isEmpty()) {
                            try {
                                mutex.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }

                        if (tasks.size() != 0 && !isFinished) {
                            func = tasks.remove();
                        }
                    }

                    if (func != null) {
                        func.run();
                    }

                    synchronized (mutex) {
                        if (isFinished) {
                            return;
                        }
                        mutex.notifyAll();
                    }
                }
            });
            workers.set(i, t);
            workers.get(i).start();
        }
    }

    private class SyncList<T> {
        private List<T> list;
        private int size;

        SyncList(int n) {
            list = new ArrayList<>(Collections.nCopies(n, null));
            size = 0;
        }
        void set(int i, T value) {
            synchronized (this) {
                list.set(i, value);
                size++;
                notify();
            }
        }

        List<T> get() {
            synchronized (this) {
                try {
                    while (size < list.size()) {
                        wait();
                    }
                } catch (InterruptedException ignored) {
                }
                return list;
            }
        }
    }

    public synchronized <T, R> List<R> map(Function<? super T, ? extends R> function, List<? extends T> list) throws InterruptedException {
        SyncList<R> ans = new SyncList<>(list.size());
        for (int i = 0; i < list.size(); ++i) {
            final int j = i;
            synchronized (mutex) {
                tasks.add(() -> {
                    R a = function.apply(list.get(j));
                    ans.set(j, a);
                });
                mutex.notify();
            }
        }

        return ans.get();
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
                System.err.println(e.toString());
            }
        }
    }
}
