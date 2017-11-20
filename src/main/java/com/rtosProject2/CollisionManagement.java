package com.rtosProject2;


import java.util.concurrent.Callable;

/**
 * This is an implementation of the look ahead algorithm
 * An overview of the algorithm can be found in Process A
 * <p>
 * This class implements the Callable class of Java.
 * This allows a thread to return an object (integer) when is is released
 */
public class CollisionManagement implements Callable<Integer> {
    private Object[][] state;
    private Plane.Movement movement;
    private int look_ahead;

    private final int stateTrain = 0;
    private final int stateRow = 1;
    private final int stateCol = 2;
    private final int stateMover = 3;


    // Constructor
    public CollisionManagement(Positions positions, Plane.Movement movement, int look_ahead) {

        Object[][] state = new Object[positions.size()][4];
        for (int pos = 0; pos < positions.size(); pos++) {
            Position position = positions.get(pos);
            state[pos][stateTrain] = position.train();
            state[pos][stateRow] = position.row();
            state[pos][stateCol] = position.col();
            state[pos][stateMover] = position.mover().clone();
        }

        this.state = state;
        this.movement = movement;
        this.look_ahead = look_ahead;
    }


    /**
     * Uses a FOR loop for deterministic reasons.
     * Recursion has been avoided
     * <p>
     * Moves the trains based on their fixed movements
     * for the desired duration
     * <p>
     * When the sub-algorithm is completed, the distance is returned.
     *
     * @return Integer
     */
    @Override
    public Integer call() {
        int counter = 0;

        try {
            for (int i = 0; i < look_ahead; i++) {

                // IF collision:  We are done looking ahead
                if ((state[0][stateRow] == state[1][stateRow] && state[0][stateCol] == state[1][stateCol]) ||
                        (state[0][stateRow] == state[2][stateRow] && state[0][stateCol] == state[2][stateCol]) ||
                        (state[1][stateRow] == state[2][stateRow] && state[1][stateCol] == state[2][stateCol])
                        )
                    break;

                // Move either all trains normally or all trains except current movement value
                for (int train = 0; train < 3; train++) {
                    Mover mover = (Mover) state[train][stateMover];
                    if (movement != mover.getMovement()) {
                        mover.move();
                        state[train][stateRow] = mover.getRowPos();
                        state[train][stateCol] = mover.getColPos();
                    }
                }
                if (movement == Plane.Movement.All)
                    System.out.println("X: " + state[0][stateRow] + ", " + state[0][stateCol]);
                counter++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return counter;
    }
}
