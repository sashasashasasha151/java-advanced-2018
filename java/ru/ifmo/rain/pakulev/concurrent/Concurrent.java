package ru.ifmo.rain.pakulev.concurrent;

import info.kgeorgiy.java.advanced.concurrent.ListIP;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Concurrent implements ListIP {

    private <T, R> R mainFunction(int i, List<? extends T> list,
                                  Function<Stream<? extends T>, ? extends R> resultF,
                                  Function<Stream<? extends R>, ? extends R> resultR) throws InterruptedException {
        if (list.size() <= i) {
            return resultF.apply(list.stream());
        }

        ArrayList<R> answer = new ArrayList<>(Collections.nCopies(i, null));
        ArrayList<Thread> threads = new ArrayList<>(Collections.nCopies(i, null));
        int remainder = list.size() % i;
        int rr = 0;
        for (int j = 0; j < i; ++j) {
            int blockSize = list.size() / i + (remainder-- > 0 ? 1 : 0);
            final int k = j;
            final int l = rr;
            final int r = l + blockSize;
            rr = r;
            Thread t = new Thread(() -> {
                answer.set(k, resultF.apply(list.subList(l, r < list.size() ? r : list.size()).stream()));
            });
            threads.set(k, t);
            threads.get(k).start();
        }

        InterruptedException exceptions = new InterruptedException();
        for (int k = 0; k < threads.size(); ++k) {
            try {
                threads.get(k).join();
            } catch (InterruptedException e) {
                exceptions.addSuppressed(e);
                k--;
            }
        }

        if (exceptions.getSuppressed().length != 0) {
            throw exceptions;
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

    public String join(int i, List<?> list) throws InterruptedException {
        return mainFunction(i, list, stream -> stream.map(Objects::toString).reduce(String::concat).orElse(""),
                stream -> stream.map(Objects::toString).reduce(String::concat).orElse(""));
    }

    public <T> List<T> filter(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
        return mainFunction(i, list, stream -> stream.filter(predicate).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }

    public <T, U> List<U> map(int i, List<? extends T> list, Function<? super T, ? extends U> function) throws InterruptedException {
        return mainFunction(i, list, stream -> stream.map(function).collect(Collectors.toList()),
                stream -> stream.flatMap(Collection::stream).collect(Collectors.toList()));
    }
}