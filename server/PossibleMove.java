package server;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jjk on 1/7/16.
 *
 * The possibleMove class is used to store a place where a move can be made
 * It contains the possible Shapes and Colors that that place can contain
 * It can hand the rows and columns over to the stone that is placed here.
 *
 */
public class PossibleMove extends Space {
    private List<Stone.Shape> possibleShape;
    private List<Stone.Color> possibleColor;

    public PossibleMove(Stone.Shape[] possibleShape, Stone.Color[] possibleColor) {
        this.possibleShape = new ArrayList<Stone.Shape>();
        this.possibleColor = new ArrayList<Stone.Color>();
        for (Stone.Shape s : possibleShape) {
            this.possibleShape.add(s);
        }
        for (Stone.Color c : possibleColor) {
            this.possibleColor.add(c);
        }
    }

    /**
     * edits the stone that is placed at this location so that it has the correct row and column.
     * also updates changes it's own possible shapes and moves.
     * @param stone
     * @return
     */
    public Stone fill (Stone stone) {
        List<Space> column = new ArrayList();
        List<Space> row = new ArrayList();
        for (Space s : getColumn()) {
            if (s instanceof Stone) {
                column.add(s);
            }
        }
        for (Space s : getRow()) {
            if (s instanceof Stone) {
                row.add(s);
            }
        }
        stone.setColumn(column);
        stone.setRow(row);
        stone.setPosition(getPosition());
        stone.place();
        possibleShape.remove(stone.getShape());
        possibleColor.remove(stone.getColor());
        return stone;
    }

    public void setPossibleShape(List<Stone.Shape> possibleShape) {
        this.possibleShape = possibleShape;
    }
    public List<Stone.Shape> getPossibleShape() {
        return possibleShape;
    }

    /**
     * removes all shapes that appear in the possibleShape list and not in the argument.
     * this is useful for updating the possiblemove after a stone has been placed next to it.
     * @param possibleShape
     */
    public void retainShapes(List<Stone.Shape> possibleShape) {
        possibleShape.retainAll(possibleShape);
    }
    public void setPossibleColor(List<Stone.Color> possibleColor) {
        this.possibleColor = possibleColor;
    }
    public List<Stone.Color> getPossibleColor() {
        return possibleColor;
    }
    public void retainColors(List<Stone.Color> possibleColor) {
        this.possibleColor.retainAll(possibleColor);
    }

    public boolean acceptable(Stone stone) {
        return false;
    }
}