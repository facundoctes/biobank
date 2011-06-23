package edu.ualberta.med.biobank.common.wrappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.peer.CollectionEventPeer;
import edu.ualberta.med.biobank.common.peer.DispatchPeer;
import edu.ualberta.med.biobank.common.peer.DispatchSpecimenPeer;
import edu.ualberta.med.biobank.common.peer.ShipmentInfoPeer;
import edu.ualberta.med.biobank.common.peer.SpecimenPeer;
import edu.ualberta.med.biobank.common.security.User;
import edu.ualberta.med.biobank.common.util.DispatchSpecimenState;
import edu.ualberta.med.biobank.common.util.DispatchState;
import edu.ualberta.med.biobank.common.wrappers.actions.BiobankSessionAction;
import edu.ualberta.med.biobank.common.wrappers.actions.IfAction;
import edu.ualberta.med.biobank.common.wrappers.actions.IfAction.Is;
import edu.ualberta.med.biobank.common.wrappers.base.DispatchBaseWrapper;
import edu.ualberta.med.biobank.common.wrappers.base.DispatchSpecimenBaseWrapper;
import edu.ualberta.med.biobank.common.wrappers.checks.NotNullCheck;
import edu.ualberta.med.biobank.common.wrappers.checks.UniqueOnSavedCheck;
import edu.ualberta.med.biobank.model.Dispatch;
import edu.ualberta.med.biobank.model.DispatchSpecimen;
import edu.ualberta.med.biobank.model.Log;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

public class DispatchWrapper extends DispatchBaseWrapper {
    private static final Property<String, Dispatch> WAYBILL_PROPERTY = DispatchPeer.SHIPMENT_INFO
        .to(ShipmentInfoPeer.WAYBILL);
    private static final Collection<Property<?, ? super Dispatch>> UNIQUE_WAYBILL_PER_SENDER_PROPERTIES = new ArrayList<Property<?, ? super Dispatch>>();

    static {
        UNIQUE_WAYBILL_PER_SENDER_PROPERTIES.add(WAYBILL_PROPERTY);
        UNIQUE_WAYBILL_PER_SENDER_PROPERTIES.add(DispatchPeer.SENDER_CENTER);
    }

    private final Map<DispatchSpecimenState, List<DispatchSpecimenWrapper>> dispatchSpecimenMap = new HashMap<DispatchSpecimenState, List<DispatchSpecimenWrapper>>();

    private List<DispatchSpecimenWrapper> deletedDispatchedSpecimens = new ArrayList<DispatchSpecimenWrapper>();

    private List<DispatchSpecimenWrapper> receivedDispatchedSpecimens = new ArrayList<DispatchSpecimenWrapper>();

    private boolean hasNewSpecimens = false;

    public DispatchWrapper(WritableApplicationService appService) {
        super(appService);
    }

    public DispatchWrapper(WritableApplicationService appService,
        Dispatch dispatch) {
        super(appService, dispatch);
    }

    @Override
    public Dispatch getNewObject() throws Exception {
        Dispatch newObject = super.getNewObject();
        newObject.setState(DispatchState.CREATION.getId());
        return newObject;
    }

    public String getStateDescription() {
        DispatchState state = DispatchState
            .getState(getProperty(DispatchPeer.STATE));
        if (state == null)
            return "";
        return state.getLabel();
    }

    public DispatchState getDispatchState() {
        return DispatchState.getState(getState());
    }

    public String getFormattedPackedAt() {
        if (getShipmentInfo() != null)
            return DateFormatter.formatAsDateTime(getShipmentInfo()
                .getPackedAt());
        return null;
    }

    public boolean hasErrors() {
        return !getDispatchSpecimenCollectionWithState(
            DispatchSpecimenState.MISSING, DispatchSpecimenState.EXTRA)
            .isEmpty();
    }

    public Map<DispatchSpecimenState, List<DispatchSpecimenWrapper>> getMap() {
        return dispatchSpecimenMap;
    }

    private List<DispatchSpecimenWrapper> getDispatchSpecimenCollectionWithState(
        DispatchSpecimenState... states) {
        return getDispatchSpecimenCollectionWithState(dispatchSpecimenMap,
            getFastDispatchSpecimenCollection(), states);
    }

    private List<DispatchSpecimenWrapper> getDispatchSpecimenCollectionWithState(
        Map<DispatchSpecimenState, List<DispatchSpecimenWrapper>> map,
        List<DispatchSpecimenWrapper> list, DispatchSpecimenState... states) {

        if (map.isEmpty()) {
            for (DispatchSpecimenState state : DispatchSpecimenState.values()) {
                map.put(state, new ArrayList<DispatchSpecimenWrapper>());
            }
            for (DispatchSpecimenWrapper wrapper : list) {
                map.get(wrapper.getSpecimenState()).add(wrapper);
            }
        }

        if (states.length == 1) {
            return map.get(states[0]);
        } else {
            List<DispatchSpecimenWrapper> tmp = new ArrayList<DispatchSpecimenWrapper>();
            for (DispatchSpecimenState state : states) {
                tmp.addAll(map.get(state));
            }
            return tmp;
        }
    }

    public List<SpecimenWrapper> getSpecimenCollection(boolean sort) {
        List<SpecimenWrapper> list = new ArrayList<SpecimenWrapper>();
        for (DispatchSpecimenWrapper da : getDispatchSpecimenCollection(false)) {
            list.add(da.getSpecimen());
        }
        if (sort) {
            Collections.sort(list);
        }
        return list;
    }

    public void addSpecimens(List<SpecimenWrapper> newSpecimens,
        DispatchSpecimenState state) throws BiobankCheckException {
        if (newSpecimens == null)
            return;

        // already added dsa
        List<DispatchSpecimenWrapper> currentDaList = getDispatchSpecimenCollection(false);
        List<DispatchSpecimenWrapper> newDispatchSpecimens = new ArrayList<DispatchSpecimenWrapper>();
        List<SpecimenWrapper> currentSpecimenList = new ArrayList<SpecimenWrapper>();

        for (DispatchSpecimenWrapper dsa : currentDaList) {
            currentSpecimenList.add(dsa.getSpecimen());
        }

        // new specimens added
        for (SpecimenWrapper specimen : newSpecimens) {
            if (specimen.getCurrentCenter().equals(getSenderCenter())) {
                if (!currentSpecimenList.contains(specimen)) {
                    DispatchSpecimenWrapper dsa = new DispatchSpecimenWrapper(
                        appService);
                    dsa.setSpecimen(specimen);
                    dsa.setDispatch(this);
                    dsa.setDispatchSpecimenState(state);
                    newDispatchSpecimens.add(dsa);
                    hasNewSpecimens = true;
                }
            } else
                throw new BiobankCheckException(
                    "Specimen does not belong to this sender.");
        }
        addToDispatchSpecimenCollection(newDispatchSpecimens);
        // make sure previously deleted ones, that have been re-added, are
        // no longer deleted
        deletedDispatchedSpecimens.removeAll(newDispatchSpecimens);
        resetMap();
    }

    @Override
    public void removeFromDispatchSpecimenCollection(
        List<? extends DispatchSpecimenBaseWrapper> dasToRemove) {
        super.removeFromDispatchSpecimenCollection(dasToRemove);
        resetMap();
    }

    public void removeSpecimens(List<SpecimenWrapper> spcs) {
        if (spcs == null) {
            throw new NullPointerException();
        }

        if (spcs.isEmpty())
            return;

        List<DispatchSpecimenWrapper> removeDispatchSpecimens = new ArrayList<DispatchSpecimenWrapper>();

        for (DispatchSpecimenWrapper dsa : getDispatchSpecimenCollection(false)) {
            if (spcs.contains(dsa.getSpecimen())) {
                removeDispatchSpecimens.add(dsa);
                deletedDispatchedSpecimens.add(dsa);
            }
        }
        removeFromDispatchSpecimenCollection(removeDispatchSpecimens);
    }

    public void removeDispatchSpecimens(List<DispatchSpecimenWrapper> dsaList) {
        if (dsaList == null) {
            throw new NullPointerException();
        }

        if (dsaList.isEmpty())
            return;

        List<DispatchSpecimenWrapper> currentDaList = getDispatchSpecimenCollection(false);
        List<DispatchSpecimenWrapper> removeDispatchSpecimens = new ArrayList<DispatchSpecimenWrapper>();

        for (DispatchSpecimenWrapper dsa : currentDaList) {
            if (dsaList.contains(dsa)) {
                removeDispatchSpecimens.add(dsa);
                deletedDispatchedSpecimens.add(dsa);
            }
        }
        removeFromDispatchSpecimenCollection(removeDispatchSpecimens);
    }

    public void receiveSpecimens(List<SpecimenWrapper> specimensToReceive) {
        List<DispatchSpecimenWrapper> nonProcessedSpecimens = getDispatchSpecimenCollectionWithState(DispatchSpecimenState.NONE);
        for (DispatchSpecimenWrapper ds : nonProcessedSpecimens) {
            if (specimensToReceive.contains(ds.getSpecimen())) {
                ds.setDispatchSpecimenState(DispatchSpecimenState.RECEIVED);
                ds.getSpecimen().setCurrentCenter(getReceiverCenter());
                receivedDispatchedSpecimens.add(ds);
            }
        }
        resetMap();
    }

    public boolean isInCreationState() {
        return getDispatchState() == null
            || DispatchState.CREATION.equals(getDispatchState());
    }

    public boolean isInTransitState() {
        return DispatchState.IN_TRANSIT.equals(getDispatchState());
    }

    public boolean isInReceivedState() {
        return DispatchState.RECEIVED.equals(getDispatchState());
    }

    public boolean hasBeenReceived() {
        return EnumSet.of(DispatchState.RECEIVED, DispatchState.CLOSED)
            .contains(getDispatchState());
    }

    public boolean isInClosedState() {
        return DispatchState.CLOSED.equals(getDispatchState());
    }

    public boolean isInLostState() {
        return DispatchState.LOST.equals(getDispatchState());
    }

    public void setState(DispatchState ds) {
        setState(ds.getId());
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(getSenderCenter() == null ? "" : getSenderCenter()
            .getNameShort() + "/");
        sb.append(getReceiverCenter() == null ? "" : getReceiverCenter()
            .getNameShort() + "/");
        sb.append(getShipmentInfo().getFormattedDateReceived());
        return sb.toString();
    }

    public boolean canBeSentBy(User user) {
        return canUpdate(user)
            && getSenderCenter().equals(user.getCurrentWorkingCenter())
            && isInCreationState() && hasDispatchSpecimens();
    }

    public boolean hasDispatchSpecimens() {
        return getSpecimenCollection(false) != null
            && !getSpecimenCollection(false).isEmpty();
    }

    public boolean canBeReceivedBy(User user) {
        return canUpdate(user)
            && getReceiverCenter().equals(user.getCurrentWorkingCenter())
            && isInTransitState();
    }

    public DispatchSpecimenWrapper getDispatchSpecimen(String inventoryId) {
        for (DispatchSpecimenWrapper dsa : getDispatchSpecimenCollection(false)) {
            if (dsa.getSpecimen().getInventoryId().equals(inventoryId))
                return dsa;
        }
        return null;
    }

    public List<DispatchSpecimenWrapper> getNonProcessedDispatchSpecimenCollection() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.NONE);
    }

    public List<DispatchSpecimenWrapper> getExtraDispatchSpecimens() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.EXTRA);
    }

    public List<DispatchSpecimenWrapper> getMissingDispatchSpecimens() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.MISSING);
    }

    public List<DispatchSpecimenWrapper> getReceivedDispatchSpecimens() {
        return getDispatchSpecimenCollectionWithState(DispatchSpecimenState.RECEIVED);
    }

    private static final String FAST_DISPATCH_SPECIMEN_QRY = "select ra from "
        + DispatchSpecimen.class.getName() + " ra inner join fetch ra."
        + DispatchSpecimenPeer.SPECIMEN.getName()
        + " as spec inner join fetch spec."
        + SpecimenPeer.SPECIMEN_TYPE.getName() + " inner join fetch spec."
        + SpecimenPeer.COLLECTION_EVENT.getName()
        + " as cevent inner join fetch cevent."
        + CollectionEventPeer.PATIENT.getName() + " inner join fetch spec."
        + SpecimenPeer.ACTIVITY_STATUS.getName() + " where ra."
        + Property.concatNames(DispatchSpecimenPeer.DISPATCH, DispatchPeer.ID)
        + " = ?";

    // fast... from db. should only call this once then use the cached value
    public List<DispatchSpecimenWrapper> getFastDispatchSpecimenCollection() {
        if (!isPropertyCached(DispatchPeer.DISPATCH_SPECIMEN_COLLECTION)) {
            List<DispatchSpecimen> results;
            // test hql
            HQLCriteria query = new HQLCriteria(FAST_DISPATCH_SPECIMEN_QRY,
                Arrays.asList(new Object[] { getId() }));
            try {
                results = appService.query(query);
            } catch (ApplicationException e) {
                throw new RuntimeException(e);
            }
            wrappedObject.setDispatchSpecimenCollection(results);
        }
        return getDispatchSpecimenCollection(false);
    }

    public boolean canBeClosedBy(User user) {
        return isInReceivedState() && canUpdate(user);
    }

    @Override
    public void reload() throws Exception {
        super.reload();
        resetMap();
    }

    @Override
    protected void resetInternalFields() {
        super.resetInternalFields();
        resetMap();
        deletedDispatchedSpecimens.clear();
        receivedDispatchedSpecimens.clear();
        hasNewSpecimens = false;
    }

    public void resetMap() {
        dispatchSpecimenMap.clear();
    }

    @Override
    protected Log getLogMessage(String action, String site, String details) {
        Log log = new Log();
        log.setAction(action);

        DispatchState state = getDispatchState();

        if (site != null) {
            log.setCenter(site);
        } else if (state != null) {
            if (state.equals(DispatchState.CREATION)
                || state.equals(DispatchState.IN_TRANSIT)) {
                log.setCenter(getSenderCenter().getNameShort());
            } else {
                log.setCenter(getReceiverCenter().getNameShort());
            }
        }

        List<String> detailsList = new ArrayList<String>();
        if (details.length() > 0) {
            detailsList.add(details);
        }

        detailsList.add(new StringBuilder("state: ").append(
            getStateDescription()).toString());

        if ((state != null)
            && ((state.equals(DispatchState.CREATION)
                || state.equals(DispatchState.IN_TRANSIT) || state
                .equals(DispatchState.LOST)))) {
            String packedAt = getFormattedPackedAt();
            if ((packedAt != null) && (packedAt.length() > 0)) {
                detailsList.add(new StringBuilder("packed at: ").append(
                    packedAt).toString());
            }
        }

        ShipmentInfoWrapper shipInfo = getShipmentInfo();
        if (shipInfo != null) {
            String receivedAt = shipInfo.getFormattedDateReceived();
            if ((receivedAt != null) && (receivedAt.length() > 0)) {
                detailsList.add(new StringBuilder("received at: ").append(
                    receivedAt).toString());
            }

            String waybill = shipInfo.getWaybill();
            if (waybill != null) {
                detailsList.add(new StringBuilder(", waybill: ")
                    .append(waybill).toString());
            }
        }
        log.setDetails(StringUtils.join(detailsList, ", "));
        log.setType("Dispatch");
        return log;
    }

    private static final String DISPATCH_HQL_STRING = "from "
        + Dispatch.class.getName() + " as d inner join fetch d."
        + DispatchPeer.SHIPMENT_INFO.getName() + " as s ";

    /**
     * Search for shipments in the site with the given waybill
     */
    public static List<DispatchWrapper> getDispatchesByWaybill(
        WritableApplicationService appService, String waybill)
        throws ApplicationException {
        StringBuilder qry = new StringBuilder(DISPATCH_HQL_STRING + " where s."
            + ShipmentInfoPeer.WAYBILL.getName() + " = ?");
        HQLCriteria criteria = new HQLCriteria(qry.toString(),
            Arrays.asList(new Object[] { waybill }));

        List<Dispatch> origins = appService.query(criteria);
        List<DispatchWrapper> shipments = ModelWrapper.wrapModelCollection(
            appService, origins, DispatchWrapper.class);

        return shipments;
    }

    /**
     * Search for shipments in the site with the given date received. Don't use
     * hour and minute.
     */
    public static List<DispatchWrapper> getDispatchesByDateReceived(
        WritableApplicationService appService, Date dateReceived)
        throws ApplicationException {

        StringBuilder qry = new StringBuilder(DISPATCH_HQL_STRING
            + " where DATE(s." + ShipmentInfoPeer.RECEIVED_AT.getName()
            + ") = DATE(?)");
        HQLCriteria criteria = new HQLCriteria(qry.toString(),
            Arrays.asList(new Object[] { dateReceived }));

        List<Dispatch> origins = appService.query(criteria);
        List<DispatchWrapper> shipments = ModelWrapper.wrapModelCollection(
            appService, origins, DispatchWrapper.class);

        return shipments;
    }

    public static List<DispatchWrapper> getDispatchesByDateSent(
        WritableApplicationService appService, Date dateSent)
        throws ApplicationException {

        StringBuilder qry = new StringBuilder(DISPATCH_HQL_STRING
            + " where DATE(s." + ShipmentInfoPeer.PACKED_AT.getName()
            + ") = DATE(?)");
        HQLCriteria criteria = new HQLCriteria(qry.toString(),
            Arrays.asList(new Object[] { dateSent }));

        List<Dispatch> origins = appService.query(criteria);
        List<DispatchWrapper> shipments = ModelWrapper.wrapModelCollection(
            appService, origins, DispatchWrapper.class);

        return shipments;
    }

    @Override
    public List<? extends CenterWrapper<?>> getSecuritySpecificCenters() {
        List<CenterWrapper<?>> centers = new ArrayList<CenterWrapper<?>>();
        if (getSenderCenter() != null)
            centers.add(getSenderCenter());
        if (getReceiverCenter() != null)
            centers.add(getReceiverCenter());
        return centers;
    }

    public boolean hasNewSpecimens() {
        return hasNewSpecimens;
    }

    @Override
    protected TaskList getPersistTasks() {
        TaskList tasks = new TaskList();

        tasks.add(check().notNull(DispatchPeer.SENDER_CENTER));
        tasks.add(check().notNull(DispatchPeer.RECEIVER_CENTER));

        tasks.add(new NotNullCheck(this, DispatchPeer.SENDER_CENTER));

        tasks.add(cascade().deleteRemoved(
            DispatchPeer.DISPATCH_SPECIMEN_COLLECTION));
        tasks.add(cascade().persistAdded(
            DispatchPeer.DISPATCH_SPECIMEN_COLLECTION));

        // TODO: probably remove the following:
        // tasks.add(cascade().delete(deletedDispatchedSpecimens));
        // tasks.add(cascade().persist(receivedDispatchedSpecimens));

        tasks.add(super.getPersistTasks());

        BiobankSessionAction checkWaybill = new UniqueOnSavedCheck<Dispatch>(
            this, UNIQUE_WAYBILL_PER_SENDER_PROPERTIES);

        tasks.add(new IfAction<Dispatch>(this, WAYBILL_PROPERTY, Is.NOT_NULL,
            checkWaybill));

        tasks.add(check().ifProperty(WAYBILL_PROPERTY, Is.NOT_NULL,
            checkWaybill));

        return tasks;
    }

    // TODO: remove this override when all persist()-s are like this!
    @Override
    public void persist() throws Exception {
        WrapperTransaction.persist(this, appService);
    }

    @Override
    public void delete() throws Exception {
        WrapperTransaction.delete(this, appService);
    }
}
