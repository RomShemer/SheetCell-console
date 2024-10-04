package logicStructure.expression.parser;

import logicStructure.expression.api.Expression;
import logicStructure.expression.impl.operation.OperationName;
import logicStructure.sheet.cell.api.CellType;
import logicStructure.sheet.cell.api.NegativeFunctionType;
import logicStructure.sheet.coordinate.Coordinate;

import java.util.List;
import java.util.stream.IntStream;

public abstract class Validator {

    public static Boolean isNumeric(Expression expression) {
        Class<?> expressionType = expression.getExpressionType();
        return expressionType == Double.class || Double.class.isAssignableFrom(expressionType) ||
                Number.class.isAssignableFrom(expressionType);
    }

    public static Boolean isString(Expression expression) {
        return expression.getExpressionType() == CellType.STRING.getType();
    }

    public static Boolean isBoolean(Expression expression) {
        return expression.getExpressionType() == CellType.BOOLEAN.getType();
    }

    public static Boolean isValidNumOfArgs(Integer numArgs, Integer requiredNumArgs) {
        return numArgs == requiredNumArgs;
    }

    public static String compareReceivedTypesToRequired(List<Expression> received, List<Class> required) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < received.size(); i++) {
            Class<?> receivedType = received.get(i).getExpressionType();
            Class<?> requiredType = required.get(i);

            if (received.get(i).getOperationType() == OperationName.REF) {
                result.append(requiredType.getSimpleName());
            } else if (requiredType.isAssignableFrom(receivedType)) {
                result.append(requiredType.getSimpleName());
            } else if (requiredType == Double.class && Number.class.isAssignableFrom(receivedType)) {
                result.append(requiredType.getSimpleName());
            } else {
                result.append(receivedType.getSimpleName());
            }
            if (i < received.size() - 1) {
                result.append(", ");
            }
        }
        return result.toString();
    }


    public static Boolean isAllReceivedTypeMatch(List<Expression> received, List<Class> required) {
        return IntStream.range(0, received.size())
                .allMatch(i -> {
                    Expression expr = received.get(i);
                    if (expr.getOperationType() == OperationName.REF) {
                        return true;
                    }
                    Object exprValue = expr.evaluate().getValue();
                    if (required.get(i) == Coordinate.class && exprValue == null) {
                        return true;
                    }
                    if (exprValue.equals(Double.NaN) || exprValue.equals(NegativeFunctionType.UNDEFINED)) {
                        return true;
                    }
                    if (required.get(i) == Double.class && Number.class.isAssignableFrom(exprValue.getClass())) {
                        return true;
                    }

                    return required.get(i).isAssignableFrom(exprValue.getClass());
                });
    }
}
