package logicStructure.expression.impl.operation;

import logicStructure.sheet.cell.api.CellType;

public enum OperationName {
    ABS(CellType.DOUBLE),
    CONCAT(CellType.STRING),
    DIVIDE(CellType.DOUBLE),
    MINUS(CellType.DOUBLE),
    MOD(CellType.DOUBLE),
    PLUS(CellType.DOUBLE),
    POW(CellType.DOUBLE),
    REF(CellType.CELL_COORDINATE),
    SUB(CellType.STRING),
    TIMES(CellType.DOUBLE),
    INTEGER_LEAF(CellType.INTEGER),
    DOUBLE_LEAF(CellType.DOUBLE),
    STRING_LEAF(CellType.STRING),
    BOOLEAN_LEAF(CellType.BOOLEAN),
    EMPTY(CellType.EMPTY);

    private CellType type;

    OperationName(CellType type) {
        this.type = type;
    }

    public CellType getOperationType() {
        return this.type;
    }

    }
