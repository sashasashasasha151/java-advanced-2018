package ru.ifmo.rain.pakulev.arrayset;

import java.util.*;

public class ArraySet<E> extends AbstractSet<E> implements SortedSet<E> {

    private List<E> arr;
    private Comparator<? super E> comparator;

    public ArraySet() {
        arr = new ArrayList<E>();
    }

    public ArraySet(Collection<? extends E> col) {
        this(col, null);
    }

    public ArraySet(Collection<? extends E> col, Comparator<? super E> comp) {
        arr = new ArrayList<E>(col);
        arr.sort(comp);
        comparator = comp;

        if (!arr.isEmpty()) {
            ArrayList<E> newArr = new ArrayList<E>();
            newArr.add(arr.get(0));
            for (int i = 1; i < arr.size(); i++) {
                if (comp == null) {
                    if (arr.get(i) != newArr.get(newArr.size() - 1)) {
                        newArr.add(arr.get(i));
                    }
                } else {
                    if (comp.compare(arr.get(i), newArr.get(newArr.size() - 1)) != 0) {
                        newArr.add(arr.get(i));
                    }
                }
            }
            arr = newArr;
        }
        arr = Collections.unmodifiableList(arr);
    }

    private ArraySet(List<E> col, Comparator<? super E> comp) {
        arr = col;
        comparator = comp;
    }

     private ArraySet(Comparator<? super E> comp) {
         arr = new ArrayList<E>();
         comparator = comp;
     }

    public Iterator<E> iterator() {
        return arr.iterator();
    }

    public int size() {
        return arr.size();
    }

    public boolean isEmpty() {
        return arr.isEmpty();
    }

    public Comparator<? super E> comparator() {
        return comparator;
    }

    public SortedSet<E> subSet(E fromElement, E toElement) {
        int start = Collections.binarySearch(arr, fromElement, comparator);
        int end = Collections.binarySearch(arr, toElement, comparator);
        return cut(start,end);
    }

    public SortedSet<E> headSet(E toElement) {
        int end = Collections.binarySearch(arr, toElement, comparator);
        return (!arr.isEmpty()) ? cut(0,end) : new ArraySet<E>(comparator);
    }

    public SortedSet<E> tailSet(E fromElement) {
        int start = Collections.binarySearch(arr, fromElement, comparator);
        return (!arr.isEmpty()) ? cut(start,arr.size()) : new ArraySet<E>(comparator);
    }

    private SortedSet<E> cut(int start, int end) {
        if (start < 0) {
            start = -start - 1;
        }
        if (end < 0) {
            end = -end - 1;
        }
        return (start < end) ? new ArraySet<E>(arr.subList(start, end), comparator) : new ArraySet<E>(comparator);
    }

    public boolean contains(Object o) {
        return Collections.binarySearch(arr, (E) o, comparator) >= 0;
    }

    public E first() {
        if (!arr.isEmpty()) {
            return arr.get(0);
        } else {
            throw new NoSuchElementException();
        }
    }

    public E last() {
        if (!arr.isEmpty()) {
            return arr.get(arr.size()-1);
        } else {
            throw new NoSuchElementException();
        }
    }
}
