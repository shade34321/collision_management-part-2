package com.rtosProject2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Positions {

    private final ArrayList<Position> _positions = new ArrayList<>();

    public Positions( List<Plane> planes) {
        for (Plane plane:planes) {
            _positions.add(new Position(plane.GetMarker(), plane.GetMoverClone()));
        }
    }

    public Position get(int idx) {
        return _positions.get(idx);
    }

    public int size() {
        return _positions.size();
    }
}
