package logicStructure.expression.impl.operation;

import logicStructure.expression.api.Expression;
import logicStructure.expression.api.Funcion;
import logicStructure.expression.impl.BinaryExpression;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.impl.EffectiveValueImpl;
import java.util.ArrayList;
import java.util.List;

public class Minus extends BinaryExpression implements Funcion {

    public Minus(Expression left, Expression right) {
        super(OperationName.MINUS, left, right);
    }

    @Override
    public EffectiveValue evaluate(EffectiveValue evaluate1, EffectiveValue evaluate2)
    {
        if(Number.class.isAssignableFrom(evaluate1.getCellType().getType()) && Number.class.isAssignableFrom(evaluate2.getCellType().getType())) {
            double result = evaluate1.extractValueWithExpectation(Number.class).doubleValue() - evaluate2.extractValueWithExpectation(Number.class).doubleValue();
            return new EffectiveValueImpl(CellType.DOUBLE, result);
        }
        else {
            if (evaluate1.getValue().toString().isEmpty() || evaluate2.getValue().toString().isEmpty()) {
                return new EffectiveValueImpl(CellType.DOUBLE, Double.NaN);
            }

            throw new IllegalArgumentException("Unsupported operation:  Both arguments must be numeric." );
        }
    }

    @Override
    public String getOperationName(){
        return OperationName.MINUS.toString();
    }

    @Override
    public OperationName getOperationType(){
        return OperationName.MINUS;
    }

    @Override
    public Class<?> getExpressionType(){
        return CellType.DOUBLE.getType();
    }

    @Override
    public List<Class> getRequiredExpressionTypesList(){
        List<Class> result = new ArrayList<>();
        result.add(Double.class);
        result.add(Double.class);
        return result;
    }
}
