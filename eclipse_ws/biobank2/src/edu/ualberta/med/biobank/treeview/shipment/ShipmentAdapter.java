package edu.ualberta.med.biobank.treeview.shipment;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.wrappers.CenterWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.OriginInfoWrapper;
import edu.ualberta.med.biobank.common.wrappers.ProcessingEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ShipmentInfoWrapper;
import edu.ualberta.med.biobank.forms.ShipmentEntryForm;
import edu.ualberta.med.biobank.forms.ShipmentViewForm;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public class ShipmentAdapter extends AdapterBase {

    public ShipmentAdapter(AdapterBase parent, OriginInfoWrapper originInfo) {
        super(parent, originInfo);

        if (originInfo.getShipmentInfo() == null) {
            throw new NullPointerException(
                "No shipment information is associated with the given origin information.");
        }

        setHasChildren(false);
    }

    public OriginInfoWrapper getWrapper() {
        return (OriginInfoWrapper) modelObject;
    }

    @Override
    protected String getLabelInternal() {
        OriginInfoWrapper originInfo = getWrapper();
        ShipmentInfoWrapper shipmentInfo = originInfo.getShipmentInfo();

        String label = shipmentInfo.getFormattedDateReceived();
        if (shipmentInfo.getWaybill() != null) {
            label += " (" + shipmentInfo.getWaybill() + ")";
        }

        return label;
    }

    @Override
    public String getTooltipText() {
        OriginInfoWrapper originInfo = getWrapper();
        if (originInfo != null) {
            CenterWrapper<?> center = originInfo.getCenter();
            if (center != null)
                return center.getName() + " - " + getTooltipText("Shipment");
        }
        return getTooltipText("Shipment");
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        addEditMenu(menu, "Shipment");
        addViewMenu(menu, "Shipment");
        addDeleteMenu(menu, "Shipment");
    }

    @Override
    protected String getConfirmDeleteMessage() {
        return "Are you sure you want to delete this shipment?";
    }

    @Override
    public List<AdapterBase> search(Object searchedObject) {
        return findChildFromClass(searchedObject, ProcessingEventWrapper.class);
    }

    @Override
    protected AdapterBase createChildNode() {
        return null;
    }

    @Override
    protected AdapterBase createChildNode(ModelWrapper<?> child) {
        return null;
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return null;
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return 0;
    }

    @Override
    public String getEntryFormId() {
        return ShipmentEntryForm.ID;
    }

    @Override
    public String getViewFormId() {
        return ShipmentViewForm.ID;
    }

    @Override
    public boolean isDeletable() {
        return internalIsDeletable();
    }

    @Override
    public void deleteWithConfirm() {
        String msg = getConfirmDeleteMessage();
        if (msg == null) {
            throw new RuntimeException("adapter has no confirm delete msg: "
                + getClass().getName());
        }
        boolean doDelete = true;
        if (msg != null)
            doDelete = BiobankPlugin.openConfirm("Confirm Delete", msg);
        if (doDelete) {
            BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
                @Override
                public void run() {
                    try {
                        getParent().removeChild(ShipmentAdapter.this);
                        if (modelObject != null) {
                            modelObject.delete();
                            notifyListeners();
                            getParent().getParent().rebuild();
                        }
                    } catch (BiobankCheckException bce) {
                        BiobankPlugin.openAsyncError("Delete failed", bce);
                    } catch (Exception e) {
                        BiobankPlugin.openAsyncError("Delete failed", e);
                    }
                }
            });
        }
    }

}