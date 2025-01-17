package logicStructure.sheet.cell.impl;

import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.operation.OperationName;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EffectiveValueImpl implements EffectiveValue, Cloneable, Serializable {
    private CellType cellType;
    private Object value;
    private OperationName operation;
    private List<Expression> args;

    public EffectiveValueImpl(CellType cellType, Object value) {
        this.cellType = cellType;
        this.value = value;
    }

    public EffectiveValueImpl(CellType cellType, Object value, OperationName operation, List<Expression> args) {
        this.cellType = cellType;
        this.value = value;
        this.operation = operation;
        this.args = args;
    }

    @Override
    public CellType getCellType() {
        return cellType;
    }

    @Override
    public Object getValue() {
        return value;
    }

    public OperationName getOperation() {
        return operation;
    }

    @Override
    public <T> T extractValueWithExpectation(Class<T> type) {
        if (type.isAssignableFrom(cellType.getType())) {
            return type.cast(value);
        }
        return null;
    }

    @Override
    public EffectiveValueImpl clone() {
        try {
            EffectiveValueImpl clonedEffectiveValue = (EffectiveValueImpl) super.clone();

            if (this.args != null) {
                clonedEffectiveValue.args = new ArrayList<>(this.args.size());
                for (Expression expr : this.args) {
                    clonedEffectiveValue.args.add(expr.clone());
                }
            }

            if (this.value instanceof Cloneable) {
                clonedEffectiveValue.value = cloneValue(this.value);
            }

            return clonedEffectiveValue;

        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T cloneValue(T value) {
        try {
            if (value instanceof Cloneable) {
                return (T) value.getClass().getMethod("clone").invoke(value);
            }
            return value;
        } catch (Exception e) {
            throw new AssertionError("Error during cloning value", e);
        }
    }

    @Override
    public boolean  equals(Object obj){
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        EffectiveValueImpl other = (EffectiveValueImpl) obj;

        return (Objects.equals(this.value, other.value));
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }
}
