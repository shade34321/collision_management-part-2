package com.rtosProject2;

public class Position {
    private final String _train;
    private final Mover _mover;

    public Position(String train, Mover mover) {
        _train = train;
        _mover = mover;
    }

    public String train() {
        return _train;
    }

    public int row() {
        return _mover.getRowPos();
    }

    public int col() {
        return _mover.getColPos();
    }

    public Mover mover() { return _mover; }
}
