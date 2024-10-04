package menu;

import logicStructure.expression.parser.Convertor;
import logicStructure.sheet.DTO.CellDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.DTO.SheetMapper;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.cell.impl.CellImpl;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.sheet.version.SheetVersionInfo;
import logicStructure.sheet.version.VersionHistory;
import logicStructure.sheet.version.VersionHistoryManager;

import java.io.Serializable;
import java.util.Map;

public class SheetMenuImpl implements MenuActionLogic, Serializable {
    private SheetImpl sheet;
    private VersionHistoryManager versionHistoryManager;
    private VersionHistory versionHistory;

    public SheetMenuImpl(SheetImpl sheet, VersionHistoryManager versionHistoryManager, VersionHistory versionHistory) {
        this.sheet = sheet;
        this.versionHistoryManager = versionHistoryManager;
        this.versionHistory = versionHistory;
    }

    @Override
    public SheetDTO display(){
        return SheetMapper.toSheetDTO(sheet);
    }

    @Override
    public String getSheetName(){
        return sheet.getSheetName();
    }

    @Override
    public CellDTO showCell(String cellId){
        Coordinate coordinate = Convertor.convertFromCellIdToCoordinate(cellId, sheet);
        Cell cell = sheet.getCellByCoordinate(coordinate);
        if (cell == null) {
            cell = new CellImpl(coordinate, null, 1, sheet.getRowSize(), sheet.getColumnSize());
        }
        return SheetMapper.toCellDTO(cell);
    }

    @Override
    public void setCellValue(String cellId, String value) throws Exception {
        SheetVersionInfo sheetVersionInfo = sheet.setCellOriginalValueByCoordinate(cellId.toUpperCase(), value);
        if (sheetVersionInfo.getChangedCellsCount() > 0){
            versionHistory.saveVersionSheet(sheetVersionInfo.getVersion(),sheetVersionInfo);
            sheet.increaseVersion();
            sheetVersionInfo.getSheet().increaseVersion();
        }
    }

    @Override
    public Map<Integer, Integer> showSheetVersionsInfo(){
        return versionHistory.getSheetVersionsInfo();
    }

    @Override
    public Boolean isValidVersionNum(String versionNum){
        try {
            int versionNumInt = Integer.parseInt(versionNum);
            int maxVersionNum = versionHistory.getAllVersions().size();

            if (versionNumInt > 0 && versionNumInt <= maxVersionNum) {
                return versionHistory.isValidVersion(versionNumInt);
            } else {
                throw new IndexOutOfBoundsException(String.format("Invalid: %d is not a valid version number. " +
                        "Version number should be an integer between 1 and %d.", versionNumInt, maxVersionNum));
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid input: version number should be an integer.");
        }
    }

    @Override
    public SheetDTO getSheetAccordingToVersion(Integer versionNum) {
        SheetImpl sheetForVersion = versionHistory.getVersionSheet(versionNum);
        return SheetMapper.toSheetDTO(sheetForVersion);
    }
}
