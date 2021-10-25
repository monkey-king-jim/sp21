package deque;

import java.util.Iterator;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class LinkedListDeque<T> {
    private class Node<T> {
        T item;
        Node prev;
        Node next;

        public Node() {
            item = null;
            prev = this;
            next = this;
        }

        public Node(T item, Node p, Node n) {
            this.item = item;
            this.prev = p;
            this.next = n;
        }


    }

    int size = 0;
    Node sentinel = new Node();

    public LinkedListDeque() {
    }

    public LinkedListDeque(T item) {
        size++;
        sentinel.next = new Node(item, sentinel, sentinel);
        sentinel.prev = sentinel.next;
    }

    public void addFirst(T item) {
        size++;
        sentinel.next = new Node(item, sentinel, sentinel.next);
        sentinel.next.next.prev = sentinel.next;
    }

    public void addLast(T item) {
        size++;
        sentinel.prev = new Node(item, sentinel.prev, sentinel);
        sentinel.prev.prev.next = sentinel.prev;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void printDeque() {
        Node temp = sentinel.next;
        while (temp != sentinel) {
            System.out.print(temp.item);
            System.out.print(" ");
            temp = temp.next;
        }
        System.out.println();
    }

    public T removeFirst() {
        if (size == 0) return null;
        size--;
        T remove = (T) sentinel.next.item;
        sentinel.next = sentinel.next.next;
        sentinel.next.prev = sentinel;
        return remove;
    }

    public T removeLast() {
        if (size == 0) return null;
        size--;
        T remove = (T) sentinel.prev.item;
        sentinel.prev = sentinel.prev.prev;
        sentinel.prev.next = sentinel;
        return remove;
    }

    public T get(int index) {
        if (index > size - 1) return null;
        Node curr = sentinel.next;
        while (index > 0) {
            curr = curr.next;
        }
        return (T) curr.item;
    }


    public Iterator<T> iterator() {
        Iterator<T> it = new Iterator<T>() {
            private Node currNode = sentinel.next;

            public boolean hasNext() {
                if (currNode != sentinel) return true;
                return false;
            }

            public T next() {
                if (hasNext()) {
                    T temp = (T) currNode.item;
                    currNode = currNode.next;
                    return temp;
                }
                return null;
            }
        };
        return it;
    }

    public static void main(String[] args) {
        LinkedListDeque<String> lld1 = new LinkedListDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();
    }
}
