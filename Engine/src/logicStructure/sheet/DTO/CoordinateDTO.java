package logicStructure.sheet.DTO;

import logicStructure.sheet.coordinate.CoordinateFactory;

public class CoordinateDTO {
    private final int row;
    private final int column;

    public CoordinateDTO(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public String createCellCoordinateString(){
        return CoordinateFactory.createCoordinate(row, column).createCellCoordinateString();
    }

    @Override
    public String toString() {
        return this.createCellCoordinateString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || this.getClass() != object.getClass())
            return false;
        CoordinateDTO current = (CoordinateDTO) object;
        return row == current.getRow() && column == current.getColumn();
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + row;
        result = 31 * result + column;
        return result;
    }
}
