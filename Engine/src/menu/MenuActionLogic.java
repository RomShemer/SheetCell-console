package menu;

import logicStructure.sheet.DTO.CellDTO;
import logicStructure.sheet.DTO.SheetDTO;
import logicStructure.sheet.impl.SheetImpl;
import logicStructure.sheet.version.VersionHistory;
import xmlLoader.XmlSheetLoader;
import java.util.Map;
import logicStructure.sheet.version.VersionHistoryManager;

public interface MenuActionLogic {

    static MenuActionLogic loadSheet(String xmlFilePath, VersionHistoryManager versionHistoryManager){
        VersionHistory versionHistory = VersionHistoryManager.getVersionHistory(xmlFilePath);
        SheetImpl loadedSheet = XmlSheetLoader.fromXmlFileToObject(xmlFilePath);

        if (versionHistory == null) {
            versionHistory = new VersionHistory(loadedSheet); // יצירת היסטוריה חדשה
            VersionHistoryManager.saveVersionHistory(xmlFilePath, versionHistory);
        }

        return new SheetMenuImpl(loadedSheet, versionHistoryManager, versionHistory);
    }
    SheetDTO display();
    String getSheetName();
    CellDTO showCell(String cellId);
    void setCellValue(String cellId, String value) throws Exception;
    Map<Integer, Integer> showSheetVersionsInfo();
    Boolean isValidVersionNum(String versionNum);
    SheetDTO getSheetAccordingToVersion(Integer versionNum);
}
