package com.rtosProject2;

/**
 * Simple tuple class since Java doesn't have one.
 * This should not be used anywhere else!
 */
public class tuple implements Comparable<tuple>{
    private final int _weight;
    private final String _plane;

    public tuple(int w, String p) {
        _weight = w;
        _plane = p;
    }

    public String getPlane() {
        return _plane;
    }

    public int getWeight() {
        return _weight;
    }

    public String toString() {
        return "Plane " + _plane + " with a weight of " + _weight;
    }

    public int size() {
        return 2;
    }


    /**
     * We only need to compare the weights so that's all we care about here.
     * @param o
     * @return
     */
    @Override
    public int compareTo(tuple o) {
        int t2 = o.getWeight();
        if (_weight < t2) {
            return -1;
        } else if (_weight > t2)  {
            return 1;
        } else if (_weight == t2) {
            return 0;
        } else {
            System.out.println("WTF did we do to get here?!?!?!");
            System.out.printf("Object weight: %d \n Passed in object weight: %d\n", _weight, t2);
            return -10;
        }
    }
}
