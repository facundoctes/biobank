package edu.ualberta.med.biobank.common.action.scanprocess;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.ActionUtil;
import edu.ualberta.med.biobank.common.action.activityStatus.ActivityStatusEnum;
import edu.ualberta.med.biobank.common.action.exception.ActionException;
import edu.ualberta.med.biobank.common.action.scanprocess.data.ShipmentProcessData;
import edu.ualberta.med.biobank.common.action.scanprocess.result.CellProcessResult;
import edu.ualberta.med.biobank.common.action.scanprocess.result.ScanProcessResult;
import edu.ualberta.med.biobank.common.action.specimen.SpecimenIsUsedInDispatchAction;
import edu.ualberta.med.biobank.common.util.ItemState;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.model.Center;
import edu.ualberta.med.biobank.model.Specimen;
import edu.ualberta.med.biobank.model.User;

public class DispatchCreateProcess extends ServerProcess {
    private static final long serialVersionUID = 1L;

    private ShipmentProcessData data;

    public DispatchCreateProcess(ShipmentProcessData data,
        Integer currentWorkingCenterId,
        Map<RowColPos, Cell> cells,
        boolean isRescanMode, Locale locale) {
        super(currentWorkingCenterId, cells, isRescanMode, locale);
        this.data = data;
    }

    public DispatchCreateProcess(ShipmentProcessData data,
        Integer currentWorkingCenterId,
        Cell cell,
        Locale locale) {
        super(currentWorkingCenterId, cell, locale);
        this.data = data;
    }

    /**
     * Process of a map of cells
     */
    @Override
    protected ScanProcessResult getScanProcessResult(Session session,
        Map<RowColPos, Cell> cells, boolean isRescanMode)
        throws ActionException {
        ScanProcessResult res = new ScanProcessResult();
        res.setResult(cells, createProcess(session, cells));
        return res;
    }

    /**
     * Process of only one cell
     */
    @Override
    protected CellProcessResult getCellProcessResult(Session session, Cell cell)
        throws ActionException {
        CellProcessResult res = new CellProcessResult();
        ShipmentProcessData dispatchData = data;
        Center sender = null;
        if (dispatchData.getSenderId() != null) {
            sender = ActionUtil.sessionGet(session, Center.class,
                dispatchData.getSenderId());
        }
        processCellDipatchCreateStatus(session, cell, sender,
            dispatchData.isErrorIfAlreadyAdded());
        res.setResult(cell);
        return res;
    }

    /**
     * Processing for the create mode
     * 
     * @param cells
     * @return
     * @throws Exception
     */
    private CellStatus createProcess(Session session, Map<RowColPos, Cell> cells) {
        CellStatus currentScanState = CellStatus.EMPTY;
        ShipmentProcessData dispatchData = data;
        Center sender = null;
        if (dispatchData.getSenderId() != null) {
            sender = ActionUtil.sessionGet(session, Center.class,
                dispatchData.getSenderId());
        }
        if (dispatchData.getPallet(session) == null) {
            for (Cell cell : cells.values()) {
                processCellDipatchCreateStatus(session, cell, sender, false);
                currentScanState = currentScanState.mergeWith(cell.getStatus());
            }

        } else {
            for (int row = 0; row < dispatchData.getPalletRowCapacity(session); row++) {
                for (int col = 0; col < dispatchData
                    .getPalletColCapacity(session); col++) {
                    RowColPos rcp = new RowColPos(row, col);
                    Cell cell = cells.get(rcp);
                    Specimen expectedSpecimen = dispatchData
                        .getSpecimen(session, row, col);
                    if (expectedSpecimen != null) {
                        if (cell == null) {
                            cell = new Cell(row, col, null, null);
                            cells.put(rcp, cell);
                        }
                        cell.setExpectedSpecimenId(expectedSpecimen.getId());
                    }
                    if (cell != null) {
                        processCellDipatchCreateStatus(session, cell, sender,
                            false);
                        currentScanState = currentScanState.mergeWith(cell
                            .getStatus());
                    }
                }
            }
        }
        return currentScanState;
    }

    /**
     * Process one cell for create mode param checkAlreadyAdded if set to true,
     * will set the Cell as error if is already added, otherwise the status will
     * only be 'already added' (this status is used while scanning: the color
     * will be different)
     */
    private CellStatus processCellDipatchCreateStatus(Session session,
        Cell scanCell,
        Center sender, boolean checkAlreadyAdded) {
        Specimen expectedSpecimen = null;
        if (scanCell.getExpectedSpecimenId() != null) {
            expectedSpecimen = ActionUtil.sessionGet(session, Specimen.class,
                scanCell.getExpectedSpecimenId());
        }
        String value = scanCell.getValue();
        if (value == null) { // no specimen scanned
            scanCell.setStatus(CellStatus.MISSING);
            scanCell.setInformation(MessageFormat.format(Messages.getString(
                "ScanAssign.scanStatus.specimen.missing", locale), //$NON-NLS-1$
                expectedSpecimen.getInventoryId()));
            scanCell.setTitle("?"); //$NON-NLS-1$
        } else {
            Specimen foundSpecimen = searchSpecimen(session, value);
            if (foundSpecimen == null) {
                // not in database
                scanCell.setStatus(CellStatus.ERROR);
                scanCell.setInformation(Messages.getString(
                    "DispatchProcess.scanStatus.specimen.notfound", locale)); //$NON-NLS-1$
            } else {
                if (expectedSpecimen != null
                    && !foundSpecimen.equals(expectedSpecimen)) {
                    // Position taken
                    scanCell.setStatus(CellStatus.ERROR);
                    scanCell
                        .setInformation(Messages
                            .getString(
                                "ScanAssign.scanStatus.specimen.positionTakenError", locale)); //$NON-NLS-1$
                    scanCell.setTitle("!"); //$NON-NLS-1$
                } else {
                    scanCell.setSpecimenId(foundSpecimen.getId());
                    if (expectedSpecimen != null
                        || data.getPallet(session) == null) {
                        checkCanAddSpecimen(session, scanCell, foundSpecimen,
                            sender, checkAlreadyAdded);
                    } else {
                        // should not be there
                        scanCell.setStatus(CellStatus.ERROR);
                        scanCell.setTitle(foundSpecimen.getCollectionEvent()
                            .getPatient().getPnumber());
                        scanCell
                            .setInformation(Messages
                                .getString(
                                    "DispatchProcess.create.specimen.anotherPallet", locale)); //$NON-NLS-1$
                    }
                }
            }
        }
        return scanCell.getStatus();
    }

    /**
     * Check at creation
     * 
     * @param cell
     * @param specimen
     * @param sender
     * @param checkAlreadyAdded
     * @throws Exception
     */
    private void checkCanAddSpecimen(Session session, Cell cell,
        Specimen specimen,
        Center sender, boolean checkAlreadyAdded) {
        if (specimen.getId() == null) {
            cell.setStatus(CellStatus.ERROR);
            cell.setInformation(""); //$NON-NLS-1$
        } else if (!specimen.getActivityStatus().getId()
            .equals(ActivityStatusEnum.ACTIVE.getId())) {
            cell.setStatus(CellStatus.ERROR);
            cell.setInformation(MessageFormat.format(Messages.getString(
                "DispatchProcess.create.specimen.status", locale), //$NON-NLS-1$
                specimen.getInventoryId()));
        } else if (!specimen.getCurrentCenter().equals(sender)) {
            cell.setStatus(CellStatus.ERROR);
            cell.setInformation(MessageFormat.format(Messages.getString(
                "DispatchProcess.create.specimen.currentCenter", locale), //$NON-NLS-1$
                specimen.getInventoryId(), specimen.getCurrentCenter()
                    .getNameShort(), sender.getNameShort()));
        } else {
            Map<Integer, ItemState> currentSpecimenIds = data
                .getCurrentDispatchSpecimenIds();
            boolean alreadyInShipment = currentSpecimenIds != null
                && currentSpecimenIds.get(specimen.getId()) != null;
            if (checkAlreadyAdded && alreadyInShipment) {
                cell.setStatus(CellStatus.ERROR);
                cell.setInformation(MessageFormat.format(Messages.getString(
                    "DispatchProcess.create.specimen.alreadyAdded", locale), //$NON-NLS-1$
                    specimen.getInventoryId()));
            } else if (new SpecimenIsUsedInDispatchAction(specimen.getId())
                .run(null, session)) {
                cell.setStatus(CellStatus.ERROR);
                cell.setInformation(MessageFormat.format(
                    Messages
                        .getString(
                            "DispatchProcess.create.specimen.inNotClosedDispatch", locale), //$NON-NLS-1$
                    specimen.getInventoryId()));
            } else {
                if (alreadyInShipment)
                    cell.setStatus(CellStatus.IN_SHIPMENT_ADDED);
                else
                    cell.setStatus(CellStatus.FILLED);
                cell.setTitle(specimen.getCollectionEvent().getPatient()
                    .getPnumber());
                cell.setSpecimenId(specimen.getId());
            }
        }
    }

    @Override
    public boolean isAllowed(User user, Session session) throws ActionException {
        // FIXME add dispatch create permission
        return true;
    }

}