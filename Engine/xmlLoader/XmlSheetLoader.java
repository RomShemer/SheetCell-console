package xmlLoader;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import logicStructure.sheet.impl.SheetImpl;
import xmlLoader.jaxb.STLCell;
import xmlLoader.jaxb.STLCells;
import xmlLoader.jaxb.STLSheet;
import java.io.File;

public class XmlSheetLoader {

    public static SheetImpl fromXmlFileToObject(String filePath) {

        try {
            XmlSheetValidator.validateXmlPath(filePath);
            File file = new File(filePath);
            XmlSheetValidator.isXmlFileExists(file);
            JAXBContext jaxbContext = JAXBContext.newInstance(STLSheet.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            STLSheet sheet = (STLSheet) jaxbUnmarshaller.unmarshal(file);

            XmlSheetValidator.validateSheetSize(sheet);
            XmlSheetValidator.validateCellsWithinBounds(sheet);
            return createSheetObject(sheet);

        } catch (JAXBException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private static SheetImpl createSheetObject(STLSheet stlSheet){
        int rowSize = stlSheet.getSTLLayout().getSTLSize().getRowsHeightUnits();
        int colSize = stlSheet.getSTLLayout().getSTLSize().getColumnWidthUnits();
        SheetImpl sheet = new SheetImpl(stlSheet.getName(), stlSheet.getSTLLayout().getRows(), stlSheet.getSTLLayout().getColumns(), rowSize, colSize);
        STLCells cells = stlSheet.getSTLCells();

        for (STLCell cell : cells.getSTLCell()) {
            try {
                sheet.addNewCell(XmlSheetValidator.createCellId(cell), cell.getSTLOriginalValue(), sheet.getVersion(), rowSize, colSize);
            }
            catch (Exception e) {
                throw new IllegalArgumentException(String.format("%s (cell:%s)", e.getMessage(), XmlSheetValidator.createCellId(cell)));
            }
        }

        return sheet;
    }
}
