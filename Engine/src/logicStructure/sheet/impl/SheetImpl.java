package logicStructure.sheet.impl;

import logicStructure.expression.parser.Convertor;
import logicStructure.sheet.coordinate.CoordinateFactory;
import logicStructure.sheet.version.SheetVersionInfo;
import logicStructure.specialException.CircularDependencyException;
import logicStructure.sheet.api.SheetUpdateActions;
import logicStructure.sheet.cell.api.EffectiveValue;
import logicStructure.sheet.cell.api.Cell;
import logicStructure.sheet.cell.impl.CellImpl;
import logicStructure.sheet.coordinate.Coordinate;
import logicStructure.sheet.api.SheetReadActions;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class SheetImpl implements SheetUpdateActions, SheetReadActions, Cloneable, Serializable {

    private Map<Coordinate, Cell> activeCells;
    private List<Edge> edges = new ArrayList<>();
    private final String name;
    private final int numOfRows;
    private final int numOfCols;
    private int version;
    private int rowSize;
    private int colSize;

    public SheetImpl(String name, int numOfRows, int numOfCols, int rowSize, int colSize) {
        this.name = name;
        this.activeCells = new HashMap<>();
        this.numOfRows = numOfRows;
        this.numOfCols = numOfCols;
        this.version = 1;
        this.rowSize = rowSize;
        this.colSize = colSize;
    }

    @Override
    public String getSheetName(){
        return name;
    }

    @Override
    public int getVersion() {
        return version;
    }

    @Override
    public int getNumOfRows(){
        return numOfRows;
    }

    @Override
    public int getNumOfCols(){
        return numOfCols;
    }

    @Override
    public int getRowSize() {
        return rowSize;
    }

    @Override
    public int getColumnSize() {
        return colSize;
    }

    @Override
    public Coordinate getMinCoordinateInSheet(){
        return CoordinateFactory.createCoordinate(0,1);
    }

    @Override
    public Coordinate getMaxCoordinateInSheet(){
        return CoordinateFactory.createCoordinate(numOfRows-1, numOfCols);
    }

    @Override
    public EffectiveValue getCellValueByCoordinate(Coordinate cellCoordinate) {
        return activeCells.get(cellCoordinate).getEffectiveValue();
    }

    @Override
    public Cell getCellByCoordinate(Coordinate coordinate) {
        return activeCells.get(coordinate);
    }

    @Override
    public Map<Coordinate,Cell> getCells(){
        return activeCells;
    }

    @Override
    public void addNewCell(String cellID, String originalValue, int version, int rowSize, int colSize) throws Exception {
        Coordinate newCoordinate = Convertor.convertFromCellIdToCoordinate(cellID, this);
        Cell cell = new CellImpl(newCoordinate, originalValue, version, rowSize, colSize);
        cell.calculateEffectiveValue(this);
        activeCells.put(newCoordinate, cell);
    }

    @Override
    public void addEdge(Edge edge) {
        if(!edges.contains(edge)) {
            edges.add(edge);
        }

        Set<Coordinate> visited = new HashSet<>();
        Set<Coordinate> recStack = new HashSet<>();
        List<Coordinate> cycle = new ArrayList<>();

        for (Coordinate coord : activeCells.keySet()) {
            if (dfsDetectCycle(coord, visited, recStack, cycle)) {
                edges.remove(edge);
                String fromCoordinateID = edge.getFrom().createCellCoordinateString();
                String toCoordinateID = edge.getTo().createCellCoordinateString();
                String message = String.format("Cell %s is already depended on cell %s%n", toCoordinateID, fromCoordinateID);
                throw new CircularDependencyException(message + "Circular dependency detected in cells: ", cycle);
            }
        }
    }

    private boolean dfsDetectCycle(Coordinate v, Set<Coordinate> visited, Set<Coordinate> recStack, List<Coordinate> cycle) {
        if (recStack.contains(v)) {
            cycle.add(v);
            return true;
        }

        if (visited.contains(v)) {
            return false;
        }

        visited.add(v);
        recStack.add(v);

        for (Edge edge : edges) {
            if (edge.getFrom().equals(v)) {
                Coordinate w = edge.getTo();
                if (dfsDetectCycle(w, visited, recStack, cycle)) {
                    cycle.add(v);
                    return true;
                }
            }
        }

        recStack.remove(v);
        return false;
    }

    @Override
    public void removeEdge(Edge edge){
        edges.remove(edge);
    }

    @Override
    public void increaseVersion(){
        version++;
    }

    @Override
    public SheetVersionInfo setCellOriginalValueByCoordinate(String cellID, String newValue) throws Exception {
        Map<Coordinate, Cell> originalCells = new HashMap<>();
        for (Map.Entry<Coordinate, Cell> entry : this.activeCells.entrySet()) {
            originalCells.put(entry.getKey(), ((CellImpl) entry.getValue()).clone());
        }

        Coordinate newCoordinate = Convertor.convertFromCellIdToCoordinate(cellID, this);
        try{
            activeCells.get(newCoordinate).setOriginalValue(newValue, this);
        } catch (NullPointerException e){
            addNewCell(cellID.toUpperCase(), newValue, version, rowSize, colSize);
        } catch (IllegalArgumentException | IndexOutOfBoundsException e){
            if(originalCells.containsKey(newCoordinate)){
                activeCells.replace(newCoordinate, originalCells.get(newCoordinate));
            } else {
                activeCells.remove(newCoordinate);
            }
            throw e;
        }

        Integer numOfChangedCells = countAndUpdateChangedCellsVersion(originalCells);
        return new SheetVersionInfo(this, numOfChangedCells, version +1);
    }

    private Integer countAndUpdateChangedCellsVersion(Map<Coordinate, Cell> originalCells) {
        AtomicReference<Integer> countChangedCells = new AtomicReference<>(0);

        activeCells.forEach((coordinate, updatedCell) -> {
            Cell originalCell = originalCells.get(coordinate);

            if (originalCell != null) {
                EffectiveValue originalValue = originalCell.getEffectiveValue();
                EffectiveValue updatedValue = updatedCell.getEffectiveValue();

                if (valuesAreDifferent(originalValue, updatedValue)) {
                    updatedCell.setVersion(version + 1);
                    countChangedCells.updateAndGet(v -> v + 1);
                }
            } else if (updatedCell.getOriginalValue() != null && !updatedCell.getOriginalValue().isEmpty()) {
                updatedCell.setVersion(version + 1);
                countChangedCells.updateAndGet(v -> v + 1);
            }
        });

        return countChangedCells.get();
    }

    private boolean valuesAreDifferent(EffectiveValue originalValue, EffectiveValue updatedValue) {
        if (originalValue == null && updatedValue == null) {
            return false;
        }
        if (originalValue == null || updatedValue == null) {
            return true;
        }
        return !originalValue.equals(updatedValue);
    }

    @Override
    public void updateCells() throws Exception {
        List<Cell> sortedCells = topologicalSort();
        for (Cell cell : sortedCells) {
            cell.calculateEffectiveValue(this);
        }
    }

    private List<Cell> topologicalSort() {
        Map<Coordinate, Integer> inDegree = new HashMap<>();
        Queue<Cell> queue = new LinkedList<>();
        List<Cell> sortedCells = new ArrayList<>();
        Set<Coordinate> visited = new HashSet<>();
        List<Coordinate> cycle = new ArrayList<>();

        for (Cell cell : activeCells.values()) {
            inDegree.put(cell.getCoordinate(), 0);
        }

        for (Edge edge : edges) {
            inDegree.put(edge.getTo(), inDegree.get(edge.getTo()) + 1);
        }

        for (Cell cell : activeCells.values()) {
            if (inDegree.get(cell.getCoordinate()) == 0) {
                queue.add(cell);
            }
        }

        while (!queue.isEmpty()) {
            Cell cell = queue.poll();
            sortedCells.add(cell);
            visited.add(cell.getCoordinate());

            for (Edge edge : edges) {
                if (edge.getFrom().equals(cell.getCoordinate())) {
                    Coordinate dependentCoord = edge.getTo();
                    inDegree.put(dependentCoord, inDegree.get(dependentCoord) - 1);
                    if (inDegree.get(dependentCoord) == 0) {
                        queue.add(getCellByCoordinate(dependentCoord));
                    } else if (visited.contains(dependentCoord)) {
                        cycle.add(dependentCoord);
                        cycle.add(cell.getCoordinate());
                        throw new CircularDependencyException("Circular dependency detected", cycle);
                    }
                }
            }
        }

        if (sortedCells.size() != activeCells.size()) {
            throw new CircularDependencyException("Circular dependency detected in the cells", cycle);
        }

        return sortedCells;
    }

    @Override
    public SheetImpl clone() {
        try {
            SheetImpl clonedSheet = (SheetImpl) super.clone();

            clonedSheet.activeCells = new HashMap<>();
            for (Map.Entry<Coordinate, Cell> entry : this.activeCells.entrySet()) {
                clonedSheet.activeCells.put(entry.getKey(), ((CellImpl) entry.getValue()).clone());
            }

            clonedSheet.edges = new ArrayList<>();
            for (Edge edge : this.edges) {
                clonedSheet.edges.add(new Edge(edge.getFrom(), edge.getTo()));
            }

            return clonedSheet;

        } catch (CloneNotSupportedException e) {
            throw new AssertionError("Cloning not supported", e);
        }
    }
}