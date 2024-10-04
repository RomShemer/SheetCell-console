package logicStructure.sheet.cell.api;

public interface EffectiveValue extends Cloneable{
        CellType getCellType();
        Object getValue();
        <T> T extractValueWithExpectation(Class<T> type);
        EffectiveValue clone();
}
