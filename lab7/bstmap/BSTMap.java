package bstmap;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable, V> implements Map61B<K, V> {

    private BSTNode root;
    private int size = 0;

    public BSTMap() {
    }

    /** Removes all of the mappings from this map. */
    @Override
    public void clear() {
        size = 0;
        root = null;
    }

    /** Returns true if and only if this dictionary contains KEY as the
     *  key of some key-value pair. */
    @Override
    public boolean containsKey(K key) {
        if (root == null) return false;
        return root.get(key) != null;
    }

    /** Returns the value corresponding to KEY or null if no such value exists. */
    @Override
    public V get(K key) {
        if (root == null) return null;
        BSTNode temp = root.get(key);
        if (temp == null) return  null;
        return temp.val;
    }

    /* Returns the number of key-value mappings in this map. */
    @Override
    public int size() { return size;}


    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        if (root == null) {
            root = new BSTNode(key, value);
            size ++;
            return;
        }
        root.insert(root, key, value);
        size ++;
    }

    @Override
    public Set<K> keySet() {
        throw new UnsupportedOperationException();
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
        throw new UnsupportedOperationException();
    }


    private class BSTNode {
        K key;
        V val;
        BSTNode leftNode;
        BSTNode rightNode;

        public BSTNode() {

        }

        public BSTNode(K key, V val) {
            this.key = key;
            this.val = val;
            this.leftNode = null;
            this.rightNode = null;
        }

        BSTNode get(K k) {
            if (k.compareTo(key) == 0) {
                return this;
            }
            if (k.compareTo(key) < 0 && leftNode != null) {
                return leftNode.get(k);
            }
            if (k.compareTo(key) > 0 && rightNode != null) {
                return rightNode.get(k);
            }
            return null;
        }

        BSTNode insert(BSTNode treeNode, K k, V v) {
            if (treeNode == null) {
                return new BSTNode(k, v);
            }
            if (k.compareTo(key) < 0) {
                treeNode.leftNode = insert(treeNode.leftNode, k, v);
            } else if (k.compareTo(key) > 0) {
                treeNode.rightNode = insert(treeNode.rightNode, k, v);
            } else {
                treeNode.val = v;
            }
            return treeNode;
        }
    }
}
