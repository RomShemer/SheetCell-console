package logicStructure.sheet.DTO;

import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.coordinate.Coordinate;
import java.util.Map;
import java.util.stream.Collectors;
import logicStructure.sheet.api.SheetReadActions;

public class SheetMapper {

    public static CoordinateDTO toCoordinateDTO(Coordinate coordinate) {
        return new CoordinateDTO(coordinate.getRow(), coordinate.getColumn());
    }

    public static CellDTO toCellDTO(Cell cell) {
        return new CellDTO(
                toCoordinateDTO(cell.getCoordinate()),
                cell.getOriginalValue(),
                cell.getEffectiveValue() == null ? " ":
                        cell.getEffectiveValue().getValue() == null ? " " : cell.getEffectiveValue().getValue().toString(),
                cell.getVersion(),
                cell.getDependsOn().stream().map(SheetMapper::toCoordinateDTO).collect(Collectors.toList()),
                cell.getInfluencingOn().stream().map(SheetMapper::toCoordinateDTO).collect(Collectors.toList())
        );
    }

    public static SheetDTO toSheetDTO(SheetReadActions sheet) {
        Map<CoordinateDTO, CellDTO> cellDTOs = sheet.getCells().entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> toCoordinateDTO(entry.getKey()),
                        entry -> toCellDTO(entry.getValue())));

        return new SheetDTO(sheet.getSheetName(), sheet.getNumOfRows(), sheet.getNumOfCols(),
                sheet.getRowSize(), sheet.getColumnSize(),cellDTOs, sheet.getVersion());
    }
}
