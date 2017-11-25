package com.rtosProject2;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.Label;

/**
 * CLASS Plane
 * - Handles movement and display of each train.
 * - Includes logic for individual train motion, plus methods for displaying them in the viewer.
 *
 * USAGE
 * display.AddPlane("Plane for X", Plane.Movement.X, "X", 0, 0, false);
 * display.AddPlane("Process C output for X, Y, Z", Plane.Movement.Set, "",-1,-1,true);
 */
public class Plane {

    public static final String emptyFormat = "   ";
    public static final String labelFormat = " %s ";
    public static final char emptyCell = '+';
    public static final int rows = 8;
    public static final int cols = 7;
    public static boolean haltX = false;
    public static boolean haltY = false;
    public static boolean haltZ = false;

    /**
     * Enum used to tell the plane which movement operation to perform. For example,
     * Movement.X goes diagonal when called by Move()
     */
    public enum Movement {
        NotSet,
        All,
        X,
        Y,
        Z,
        Set
    }

    /**
     * Constructor to initialize a new plane
     * @param marker X, Y, or Z
     * @param mover Handles moving
     * @param labels The array of numbers on the outside and dots inside the plane
     */
    public Plane(String marker, Mover mover,  Label[][] labels) {
        _mover = mover;
        _marker = marker;
        _labels = labels;
    }

    private final String _marker;
    private final Label[][] _labels;
    private int _atRow = -1;
    private int _atCol = -1;
    private final Mover _mover;

    /**
     * Shows the marker at its XY position in a non-collision state
     * @param x
     * @param y
     */
    public void Initialize() {
        ShowMarker(_mover.getRowPos(), _mover.getColPos(), false);
    }

    /**
     * Sets a row/col position to empty (.). The row col position is resolved
     * to a Label component, which then is passed to the SetEmptyFormat overload.
     * @param row
     * @param col
     */
    public void SetEmptyFormat(int row, int col) {
        if (row == -1 || col == -1) return;
        Label label = GetLabelInPlane(row, col);
        SetEmptyFormat(label);
    }

    /**
     * Accepts a Label component and resets it to empty coloring and character
     * @param label
     */
    public static void SetEmptyFormat(Label label) {
        label.setText(String.format(labelFormat, Plane.emptyCell));
        label.setForegroundColor(TextColor.ANSI.CYAN);
        label.setBackgroundColor(TextColor.ANSI.WHITE);
    }

    /**
     * Sets the coloring of a Label component to the occupied state,
     * which can either be standard or red-collision
     * @param label
     * @param isCollision
     */
    private void SetOccupiedFormat(Label label, boolean isCollision) {
        if (isCollision) {
            label.setForegroundColor(TextColor.ANSI.WHITE);
            label.setBackgroundColor(TextColor.ANSI.RED);
        }
        else
        {
            label.setForegroundColor(TextColor.ANSI.BLUE);
            label.setBackgroundColor(TextColor.ANSI.CYAN);
        }
    }


    /**
     * Handles moving the train one position in the plane using logic
     * described in the homework assignment. X = diagonal, Y is veritcal, Z is horizontal
     * @return
     */
    public void Move() {

        switch (_mover.getMovement()) {
            case X:
                if(haltX ){
                    System.out.println("halt x");
                    ShowMarker(_atRow, _atCol, false);

                }
                else {
                    _mover.move();
                    ShowMarker(_mover.getRowPos(), _mover.getColPos(), false);
                }
                break;
            case Y:
                if (haltY){
                    ShowMarker(_atRow, _atCol, false);
                }
                else {
                    _mover.move();
                    ShowMarker(_mover.getRowPos(), _mover.getColPos(), false);
                }
                break;
            case Z:
                if(haltZ){
                    System.out.println("halt z");
                    ShowMarker(_atRow, _atCol, false);
                }
                else {
                    _mover.move();
                    ShowMarker(_mover.getRowPos(), _mover.getColPos(), false);
                }
                break;
            case Set:
                break;
        }
    }

    /**
     * Returns the Label component related to a row/col position.
     * @param row
     * @param col
     * @return
     */
    private Label GetLabelInPlane(int row, int col) {
        System.out.println(row + " --- " + col);
        return _labels[row + 1][col + 1];
    }

    /**
     * Tells the plane to show the marker (X, Y, Z) at a specific row/col and whether it is
     * in a collision state
     * @param row
     * @param col
     * @param isCollision
     */
    public void ShowMarker(int row, int col, boolean isCollision) {

        ShowMarker(row, col, isCollision, "");
    }

    public Mover GetMoverClone() {
        return _mover.clone();
    }

    /**
     * Resets the plane to empty format.
     */
    public void ResetDisplay() {
        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                SetEmptyFormat(row, col);
            }
        }
    }

    /**
     * Gets the marker at a specific row/col position. Will either be X,Y,Z or a dot (empty)
     * @param row
     * @param col
     * @return
     */
    public String GetMarker(int row, int col) {
        Label label = GetLabelInPlane(row, col);
        String contents = label.getText();

        //if contents holds an empty marker, return nothing
        if (contents.equals(String.format(labelFormat, Plane.emptyCell))) {
            return "";
        } else {
            return contents.trim();
        }
    }

    /**
     * Returns the standard marker for this plane. Use GetMarker(row,col) to get the current marking at a specific pos
     * @return
     */
    public String GetMarker() {
        return _marker;
    }

    /**
     * Arbitrarily shows a marker in normal or collision state at a row column.
     * Used by ProcessC to show all trains one plane and possibly more than one
     * train in a single row/col position (thus, marker is a string).
     * @param row
     * @param col
     * @param isCollision
     * @param marker
     */
    public void ShowMarker(int row, int col, boolean isCollision, String marker) {
        Label label = GetLabelInPlane(row, col);
        marker = String.format(labelFormat, marker.equals("") ? _marker : marker);
        marker = marker.length() > labelFormat.length() ? marker.trim() : marker;
        if (_mover != null && _mover.getMovement() != Movement.Set) {
            SetEmptyFormat(_atRow, _atCol);
            _atRow = row;
            _atCol = col;
        }
        label.setText(marker);
        SetOccupiedFormat(label, isCollision);
    }

}
