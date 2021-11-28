package hashmap;

import java.util.*;

/**
 * A hash table-backed Map implementation. Provides amortized constant time
 * access to elements via get(), remove(), and put() in the best case.
 * <p>
 * Assumes null keys will never be inserted, and does not resize down upon remove().
 *
 * @author J
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;

    private int bucketsLen;

    private double loadFactor;

    private int size = 0;

    private HashSet<K> keySet = new HashSet<>();

    /**
     * Constructors
     */
    public MyHashMap() {
        bucketsLen = 16;
        loadFactor = 0.75;
        buckets = createTable(bucketsLen);
    }

    public MyHashMap(int initialSize) {
        bucketsLen = initialSize;
        loadFactor = 0.75;
        buckets = createTable(bucketsLen);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad     maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        bucketsLen = initialSize;
        loadFactor = maxLoad;
        buckets = createTable(bucketsLen);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     * <p>
     * The only requirements of a hash table bucket are that we can:
     * 1. Insert items (`add` method)
     * 2. Remove items (`remove` method)
     * 3. Iterate through items (`iterator` method)
     * <p>
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     * <p>
     * Override this method to use different data structures as
     * the underlying bucket type
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return new LinkedList<>();
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     * <p>
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        return new Collection[tableSize];
    }

    @Override
    public void clear() {
        buckets = createTable(bucketsLen);
        size = 0;
        keySet.clear();
    }

    @Override
    public boolean containsKey(K key) {
        return keySet.contains(key);
    }

    @Override
    public V get(K key) {
        int index = getIndex(key);
        if (buckets[index] != null) {
            for (Node node : buckets[index]) {
                if (key.equals(node.key)) {
                    return node.value;
                }
            }
        }
        return null;
    }

    private int getIndex(K key) {
        int index = key.hashCode() % bucketsLen;
        if (index < 0) {
            return bucketsLen + index;
        }
        return index;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public void put(K key, V value) {
        int index = getIndex(key);
        // base case when the key exists
        if (keySet.contains(key)) {
            for (Node node : buckets[index]) {
                if (key.equals(node.key)) {
                    node.value = value;
                    return;
                }
            }
        }

        size++;

        if (buckets[index] == null) {
            buckets[index] = createBucket();
        }
        buckets[index].add(createNode(key, value));
        keySet.add(key);

        // check if N/M > Load Factor, and resize if it does
        if ((double) size / bucketsLen > loadFactor) {
            resize();
        }
    }

    private void resize() {
        Collection<Node>[] tempBuckets = buckets;
        bucketsLen *= 2;
        buckets = createTable(bucketsLen);
        for (Collection<Node> bucket : tempBuckets) {
            if (bucket != null) {
                for (Node node : bucket) {
                    if (buckets[getIndex(node.key)] == null) {
                        buckets[getIndex(node.key)] = createBucket();
                    }
                    buckets[getIndex(node.key)].add(node);
                }
            }
        }
    }

    @Override
    public Set<K> keySet() {
        return keySet;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        Stack<K> temp = new Stack<>();
        temp.addAll(keySet);
        return new Iterator<K>() {
            @Override
            public boolean hasNext() {
                return !temp.isEmpty();
            }

            @Override
            public K next() {
                if (!hasNext()) throw new NoSuchElementException();
                return temp.pop();
            }
        };
    }
}
