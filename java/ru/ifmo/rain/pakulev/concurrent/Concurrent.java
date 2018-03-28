//package ru.ifmo.rain.pakulev.concurrent;
//
//import info.kgeorgiy.java.advanced.concurrent.ScalarIP;
//
//import java.util.ArrayList;
//import java.util.Comparator;
//import java.util.List;
//import java.util.function.Predicate;
//
//
//public class Concurrent implements ScalarIP {
//
//    private class Make<T> implements Runnable {
//        List<? extends T> list;
//        Comparator<? super T> comparator;
//
//        Make(List<? extends T> list, Comparator<? super T> comparator) {
//            this.list = list;
//            this.comparator = comparator;
//        }
//
//
//        @Override
//        public void run() {
//            System.out.println(list.stream().min(comparator).get());
//        }
//    }
//
//    public <T> T maximum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
//        if (list.size() <= i) {
//            return list.stream().max(comparator).get();
//        }
//        //System.out.println("start");
//        ArrayList<T> answer = new ArrayList<T>();
//        ArrayList<Thread> threads = new ArrayList<Thread>();
//        Object o = new Object();
//
//        final int blockSize = list.size() / i + 1;
//        for (int j = 0; j < i; ++j) {
//            int n = j;
//            int sz = list.size();
//            Thread t = new Thread(() -> {
//                int l, r;
//                synchronized (o) {
//                    l = n * blockSize;
//                    r = l + blockSize;
//                }
//                T max = list.get(l);
//                for (int k = l + 1; k < r; ++k) {
//                    if (k == sz) {
//                        break;
//                    }
//                    max = comparator.compare(max, list.get(k)) > 0 ? max : list.get(k);
//                }
//                synchronized (o) {
//                    answer.add(max);
//                }
//            });
//            threads.add(t);
//            t.start();
//        }
//
//        for (Thread thread : threads) {
//            thread.join();
//        }
//        //System.out.println("finish");
//
//        return answer.stream().max(comparator).get();
//    }
//
//    public <T> T minimum(int i, List<? extends T> list, Comparator<? super T> comparator) throws InterruptedException {
//        return list.get(0);
//    }
//
//    public <T> boolean all(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
//        return false;
//    }
//
//    public <T> boolean any(int i, List<? extends T> list, Predicate<? super T> predicate) throws InterruptedException {
//        return false;
//    }
//}
