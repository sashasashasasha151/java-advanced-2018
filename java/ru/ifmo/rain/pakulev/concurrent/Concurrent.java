package ru.ifmo.rain.pakulev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ScalarIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;


public class Concurrent implements ScalarIP {

    private <T, R> R mainFunction(int i, List<? extends T> list,
                                  final Function<Stream<? extends T>, ? extends R> resultF,
                                  final Function<Stream<? extends R>, ? extends R> resultR) throws InterruptedException {
        if (list.size() <= i) {
            return resultF.apply(list.stream());
        }

        ArrayList<R> answer = new ArrayList<R>(Collections.nCopies(i, null));
        ArrayList<Thread> threads = new ArrayList<Thread>(Collections.nCopies(i, null));
        int remainder = list.size() % i;
        for (int j = 0; j < i; ++j) {
            int blockSize = list.size() / i + (remainder-- > 0 ? 1 : 0);
            final int k = j;
            final int l = j * blockSize;
            final int r = l + blockSize;
            Thread t = new Thread(() -> {
                answer.set(k, resultF.apply(list.subList(l, r < list.size() ? r : list.size()).stream()));
            });
            threads.set(k, t);
            t.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        return resultR.apply(answer.stream());
    }

    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return mainFunction(i, list, stream -> stream.max(comparator).get(), stream -> stream.max(comparator).get());
    }

    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
        return mainFunction(i, list, stream -> stream.min(comparator).get(), stream -> stream.min(comparator).get());
    }

    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return mainFunction(i, list, stream -> stream.allMatch(predicate), stream -> stream.allMatch(p -> p));
    }

    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return mainFunction(i, list, stream -> stream.anyMatch(predicate), stream -> stream.anyMatch(p -> p));
    }
}