package edu.ualberta.med.biobank.widgets.grids;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ualberta.med.biobank.gui.common.Swt2DUtil;
import edu.ualberta.med.biobank.model.ContainerType;
import edu.ualberta.med.biobank.model.util.RowColPos;
import edu.ualberta.med.biobank.widgets.grids.well.AbstractUIWell;
import edu.ualberta.med.biobank.widgets.grids.well.UICellStatus;

/**
 * Draw a grid according to specific parameters : total number of rows, total number of columns,
 * width and height of the cells
 */
public abstract class AbstractGridDisplay extends AbstractContainerDisplay {

    private static Logger log = LoggerFactory.getLogger(AbstractGridDisplay.class.getName());

    private static final int IMAGE_BORDER_SIZE = 2;

    private int cellWidth = 60;

    private int cellHeight = 60;

    protected int gridWidth;

    protected int gridHeight;

    private int rows;

    private int columns;

    /**
     * Height used when legend in under the grid
     */
    public static final int LEGEND_HEIGHT = 20;

    /**
     * width calculated when legend in under the grid
     */
    protected int legendWidth;

    /**
     * Width used when legend is on the side of the grid
     */
    public static final int LEGEND_WIDTH = 70;

    public boolean legendOnSide = false;

    public AbstractGridDisplay(String name) {
        super(name);
    }

    @Override
    protected Image createGridImage(ContainerDisplayWidget displayWidget) {
        Display display = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay();
        Rectangle clientArea = getClientArea();
        Image image = new Image(display, clientArea.width, clientArea.height);

        // center grid on image
        Rectangle2D.Double cellRect = new Rectangle2D.Double(
            0, 0, cellWidth, cellHeight);

        boolean multiSelectionEnabled = displayWidget.getMultiSelectionManager().isEnabled();
        RowColPos widgetSelection = displayWidget.getSelection();
        Map<RowColPos, ? extends AbstractUIWell> cells = displayWidget.getCells();
        if (cells == null) {
            cells = new HashMap<RowColPos, AbstractUIWell>(0);
        }

        GC newGC = new GC(image);
        for (int row = 0; row < rows; ++row) {
            for (int col = 0; col < columns; ++col) {
                AffineTransform t = AffineTransform.getTranslateInstance(
                    col * cellWidth, row * cellHeight);
                Rectangle2D.Double rectangle = Swt2DUtil.transformRect(t, cellRect);
                Rectangle r = new Rectangle(
                    (int) rectangle.x,
                    (int) rectangle.y,
                    (int) rectangle.width,
                    (int) rectangle.height);

                Color defaultColor = getDefaultBackgroundColor(display, cells, r, row, col);

                drawRectangle(
                    display,
                    newGC,
                    r,
                    row,
                    col,
                    defaultColor,
                    multiSelectionEnabled,
                    cells,
                    widgetSelection);
                String topText = getTopTextForBox(cells, row, col);
                if (topText != null) {
                    drawText(display, newGC, topText, r, SWT.TOP);
                }
                String middleText = getMiddleTextForBox(cells, row, col);
                if (middleText != null) {
                    drawText(display, newGC, middleText, r, SWT.CENTER);
                }
                String bottomText = getBottomTextForBox(cells, row, col);
                if (bottomText != null) {
                    drawText(display, newGC, bottomText, r, SWT.BOTTOM);
                }
            }
        }

        if (legendStatus != null) {
            legendWidth = gridWidth / legendStatus.size();
            for (int i = 0; i < legendStatus.size(); i++) {
                UICellStatus status = legendStatus.get(i);
                drawLegend(display, newGC, status.getColor(), i, status.getLegend());
            }
        }
        newGC.dispose();
        return image;
    }

    @Override
    protected Rectangle getClientArea() {
        int width;
        int height;

        if ((maxWidth >= 0) && (maxHeight >= 0)) {
            width = maxWidth;
            height = maxHeight;

            cellWidth = maxWidth / columns;
            cellHeight = maxHeight / rows;
        } else {
            width = cellWidth * columns;
            height = cellHeight * rows;
        }

        gridWidth = width;
        gridHeight = height;

        width += 2 * IMAGE_BORDER_SIZE;
        height += 2 * IMAGE_BORDER_SIZE;

        if (legendStatus != null) {
            if (legendOnSide) {
                width += LEGEND_WIDTH + 4;
            } else {
                height += LEGEND_HEIGHT + 4;
            }
        }

        // log.debug("getClientArea: width: {}, height: {}", width, height);
        return new Rectangle(0, 0, width, height);

    }

    @SuppressWarnings("nls")
    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Rectangle clientArea = getClientArea();

        if (this.name.equals("PalletDisplay")) {
            log.debug("computeSize: width: {}, height: {}", clientArea.x, clientArea.y);
        }

        return new Point(clientArea.width, clientArea.height);
    }

    @SuppressWarnings("nls")
    protected void drawRectangle(
        Display display,
        GC gc,
        Rectangle rectangle,
        int indexRow,
        int indexCol,
        Color defaultBackgroundColor,
        boolean multiSelectionEnabled,
        Map<RowColPos, ? extends AbstractUIWell> cells,
        RowColPos selection) {

        if (cells == null) {
            throw new IllegalArgumentException("cells is null");
        }

        Color backgroundColor = defaultBackgroundColor;
        if (selection != null) {
            Integer selectionRow = selection.getRow();
            Integer selectionCol = selection.getCol();

            if ((selectionRow == null) || (selectionCol == null)) {
                throw new IllegalArgumentException("selection row or column is null");
            }

            if ((selectionRow == indexRow) && (selectionCol == indexCol)) {
                backgroundColor = display.getSystemColor(SWT.COLOR_YELLOW);
            }
        }
        gc.setBackground(backgroundColor);
        gc.fillRectangle(rectangle);
        gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.drawRectangle(rectangle);
        if (!cells.isEmpty() && multiSelectionEnabled) {
            AbstractUIWell cell = cells.get(new RowColPos(indexRow, indexCol));
            if (cell != null && cell.isSelected()) {
                Rectangle rect = new Rectangle(
                    rectangle.x + 5,
                    rectangle.y + 5,
                    rectangle.width - 10,
                    rectangle.height - 10);
                Color color = display.getSystemColor(SWT.COLOR_BLUE);
                gc.setForeground(color);
                gc.drawRectangle(rect);
            }
        }
        gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));

    }

    @SuppressWarnings("unused")
    protected Color getDefaultBackgroundColor(
        Display display,
        Map<RowColPos, ? extends AbstractUIWell> cells,
        Rectangle rectangle,
        int indexRow,
        int indexCol) {
        return UICellStatus.EMPTY.getColor();
    }

    @SuppressWarnings("unused")
    protected String getTopTextForBox(
        Map<RowColPos, ? extends AbstractUIWell> cells, int indexRow,
        int indexCol) {
        return null;
    }

    protected String getMiddleTextForBox(
        Map<RowColPos, ? extends AbstractUIWell> cells, int indexRow,
        int indexCol) {
        return getDefaultTextForBox(cells, indexRow, indexCol);
    }

    @SuppressWarnings("unused")
    protected String getBottomTextForBox(
        Map<RowColPos, ? extends AbstractUIWell> cells, int indexRow,
        int indexCol) {
        return null;
    }

    @SuppressWarnings("nls")
    @Override
    public void setContainerType(ContainerType type) {
        super.setContainerType(type);
        Integer rowCap = containerType.getRowCapacity();
        Integer colCap = containerType.getColCapacity();
        Assert.isNotNull(rowCap, "row capacity is null");
        Assert.isNotNull(colCap, "column capacity is null");
        setStorageSize(rowCap, colCap);
        if (colCap <= 1) {
            // single dimension size
            setCellWidth(120);
            setCellHeight(20);
            setLegendOnSide(true);
        }
    }

    private void drawText(
        Display display,
        GC gc,
        String text,
        Rectangle rectangle,
        int verticalPosition) {
        Font oldFont = gc.getFont();
        Font tmpFont = null;
        Point textSize = gc.textExtent(text);
        if (textSize.x > rectangle.width) {
            // Try to find a smallest font to see the whole text
            FontData fd = oldFont.getFontData()[0];
            int height = fd.getHeight();
            Point currentTextSize = textSize;
            while (currentTextSize.x > rectangle.width && height > 3) {
                if (tmpFont != null) {
                    tmpFont.dispose();
                }
                height--;
                FontData fd2 =
                    new FontData(fd.getName(), height, fd.getStyle());
                tmpFont = new Font(display, fd2);
                gc.setFont(tmpFont);
                currentTextSize = gc.textExtent(text);
            }
            if (height > 3) {
                textSize = currentTextSize;
            } else {
                gc.setFont(oldFont);
            }
        }
        int xTextPosition = (rectangle.width - textSize.x) / 2 + rectangle.x;
        int yTextPosition = 0;
        switch (verticalPosition) {
        case SWT.CENTER:
            yTextPosition = (rectangle.height - textSize.y) / 2 + rectangle.y;
            break;
        case SWT.TOP:
            yTextPosition = rectangle.y + 3;
            break;
        case SWT.BOTTOM:
            yTextPosition = rectangle.y + rectangle.height - textSize.y - 3;
        }
        gc.drawText(text, xTextPosition, yTextPosition, true);
        gc.setFont(oldFont);
        if (tmpFont != null) {
            tmpFont.dispose();
        }
    }

    protected void drawLegend(Display display, GC gc, Color color, int index, String text) {
        gc.setBackground(color);
        int width = legendWidth;
        int startx = legendWidth * index;
        int starty = gridHeight + 4;
        if (legendOnSide) {
            width = LEGEND_WIDTH;
            startx = gridWidth + 4;
            starty = LEGEND_HEIGHT * index;
        }
        Rectangle rectangle = new Rectangle(startx, starty, width,
            LEGEND_HEIGHT);
        gc.fillRectangle(rectangle);
        gc.drawRectangle(rectangle);
        drawText(display, gc, text, rectangle, SWT.CENTER);
    }

    /**
     * Modify only the number of rows and columns of the grid. If no max width and max height has
     * been given to the grid, the default cell width and cell height will be used
     */
    @Override
    public void setStorageSize(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }

    public void setLegendOnSide(boolean onSide) {
        this.legendOnSide = onSide;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return columns;
    }

    @Override
    public RowColPos getPositionAtCoordinates(int x, int y) {
        int col = x / getCellWidth();
        int row = y / getCellHeight();
        if (col >= 0 && col < getCols() && row >= 0 && row < getRows()) {
            return new RowColPos(row, col);
        }
        return null;
    }

}
