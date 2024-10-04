package logicStructure.sheet.api;

import logicStructure.sheet.impl.Edge;
import logicStructure.sheet.version.SheetVersionInfo;

public interface SheetUpdateActions {
    SheetVersionInfo setCellOriginalValueByCoordinate(String cellID, String newValue) throws Exception;
    void addNewCell(String cellID, String originalValue, int version, int rowSize, int colSize) throws Exception;
    void addEdge(Edge edge);
    void removeEdge(Edge edge);
    void updateCells() throws Exception;
    void increaseVersion();
}
