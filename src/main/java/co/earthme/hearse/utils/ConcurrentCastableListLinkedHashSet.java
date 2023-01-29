package co.earthme.hearse.utils;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import java.util.*;

//只用于解决与车万女仆mod的冲突问题。请勿使用到其他地方
@Deprecated
public class ConcurrentCastableListLinkedHashSet<E> extends LinkedHashSet<E> implements Set<E>{
    private final List<E> bakingList = ObjectLists.synchronize(new ObjectArrayList<>());

    @Override
    public int size() {
        return this.bakingList.size();
    }

    @Override
    public boolean isEmpty() {
        return this.bakingList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return this.bakingList.contains(o);
    }

    @Override
    public Iterator<E> iterator() {
        return this.bakingList.iterator();
    }

    @Override
    public Object[] toArray() {
        return this.bakingList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.bakingList.toArray(a);
    }

    @Override
    public boolean add(E e) {
        return this.bakingList.add(e);
    }

    @Override
    public boolean remove(Object o) {
        return this.bakingList.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return this.bakingList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        return this.bakingList.addAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.bakingList.retainAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return this.bakingList.removeAll(c);
    }

    @Override
    public void clear() {
        this.bakingList.clear();
    }
}
