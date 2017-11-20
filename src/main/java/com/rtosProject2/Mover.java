package com.rtosProject2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Handles all movement, including any direction and velocity. Can be set to random configuration or specific.
 */
public class Mover {
    private Plane.Movement _movement;
    private int _velocity = 0;
    private int _moveCol = 0;
    private int _moveRow = 0;
    private int _currentColPos = 0;
    private int _currentRowPos = 0;
    private Direction _direction = Direction.NotSet;
    private static Random _random = new Random();

    public enum Direction {
        NotSet,
        N,
        NE,
        E,
        SE,
        S,
        SW,
        W,
        NW
    }

    private Mover(Plane.Movement movement, int velocity, int moveRow, int moveCol, int startRow, int startCol) {
        this._movement = movement;
        this._velocity = velocity;
        this._moveCol = moveCol;
        this._moveRow = moveRow;
        this._currentColPos = startCol;
        this._currentRowPos = startRow;

        assert moveCol != 0 && moveRow != 0;
        assert moveCol <= 1 && moveRow <= 1;
        assert moveCol >= -1 && moveRow >= 1;
        assert velocity > 0 || velocity < 3;
        assert startCol < 8 && startCol >=0;
        assert startRow < 8 && startRow >=0;
        assert movement == Plane.Movement.X || movement == Plane.Movement.Y || movement == Plane.Movement.Z;

        if (moveRow == 0) {
            _direction = moveCol == 1
                    ? Direction.S
                    : Direction.N;
        } else if (moveRow == 1) {
            _direction = moveCol == 1
                        ? Direction.SE
                        : moveCol == 0
                            ? Direction.E
                            : Direction.NE;
        } else {
            _direction = moveCol == 1
                    ? Direction.NW
                    : moveCol == 0
                        ? Direction.W
                        : Direction.SW;
        }
    }

    private static Mover factoryForX(boolean useRandomPosAndDir) {
        int velocity = 1;
        int moveCol = 1;
        int moveRow = 1;
        int startCol = 0;
        int startRow = 0;
        if (useRandomPosAndDir) {
            //make it will move - 0 and 0 will not move
            do  {
                moveCol = getRandom(-1, 1);
                moveRow = getRandom(-1, 1);
            } while (moveCol == 0 && moveRow == 0);
            startRow = getRandom(0, Plane.rows - 1);
            startCol = getRandom(0, Plane.cols - 1);
        }
        return new Mover(Plane.Movement.X, velocity, moveRow, moveCol, startRow, startCol);
    }

    private static Mover factoryForY(boolean useRandomPosAndDir) {
        int velocity = 1;
        int moveCol = 0;
        int moveRow = 1;
        int startCol = 2;
        int startRow = 0;
        if (useRandomPosAndDir) {
            //make it will move - 0 and 0 will not move
            do  {
                moveCol = getRandom(-1, 1);
                moveRow = getRandom(-1, 1);
            } while (moveCol == 0 && moveRow == 0);
            startRow = getRandom(0, Plane.rows - 1);
            startCol = getRandom(0, Plane.cols - 1);
        }
        return new Mover(Plane.Movement.Y, velocity, moveRow, moveCol, startRow, startCol);
    }

    private static int getRandom(int min, int max) {
        return _random.nextInt((max - min) + 1) + min;
    }

    private static Mover factoryForZ(boolean useRandomPosAndDir, int velocityZ) {
        int velocity = velocityZ;
        int moveCol = 1;
        int moveRow = 0;
        int startRow = 3;
        int startCol = 6;
        if (useRandomPosAndDir) {
            //make it will move - 0 and 0 will not move
            do  {
                moveCol = getRandom(-1, 1);
                moveRow = getRandom(-1, 1);
            } while (moveCol == 0 && moveRow == 0);
            startRow = getRandom(0, Plane.rows - 1);
            startCol = getRandom(0, Plane.cols - 1);
        }
        return new Mover(Plane.Movement.Z, velocity, moveRow, moveCol, startRow, startCol);
    }

    /**
     * Gets a list of movers in order X, Y, Z, with unique positions (even when random)
     * @param useRandomPosAndDir
     * @param velocityZ - set to 1 or 2
     * @return
     */
    public static List<Mover> getMovers(boolean useRandomPosAndDir, int velocityZ) {
        Mover x = null, y = null, z = null;
        boolean isUnique = false;
        while (!isUnique) {
            x = factoryForX(useRandomPosAndDir);
            y = factoryForY(useRandomPosAndDir);
            z = factoryForZ(useRandomPosAndDir, velocityZ);
            //make sure all are in unique start positions... if not, do it again
            isUnique = x._currentColPos != y._currentColPos && x._currentColPos != z._currentColPos &&
                       x._currentRowPos != y._currentRowPos && x._currentColPos != z._currentColPos &&
                       y._currentColPos != z._currentColPos && y._currentRowPos != z._currentRowPos;

        }
        return new ArrayList<>(Arrays.asList(x, y, z));
    }

    public void move() {
        _currentColPos = (_currentColPos + (_moveCol * _velocity)) % Plane.cols;
        if (_currentColPos < 0) _currentColPos = Plane.cols - 1;
        _currentRowPos = (_currentRowPos + (_moveRow * _velocity)) % Plane.rows;
        if (_currentRowPos < 0) _currentRowPos = Plane.rows - 1;
    }

    public int getColPos() { return _currentColPos; }
    public int getRowPos() { return _currentRowPos; }
    public Plane.Movement getMovement() { return _movement; }
    public int getVelocity() { return _velocity; }
    public Mover.Direction getDirection() {
        return _direction;
    }

    public Mover clone() {
        return new Mover(_movement, _velocity, _moveRow, _moveCol, _currentRowPos, _currentColPos);
    }

}
