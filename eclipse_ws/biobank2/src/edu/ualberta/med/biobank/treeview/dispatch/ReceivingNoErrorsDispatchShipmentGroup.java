package edu.ualberta.med.biobank.treeview.dispatch;

import java.util.ArrayList;
import java.util.Collection;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.treeview.AdapterBase;

public class ReceivingNoErrorsDispatchShipmentGroup extends
    AbstractDispatchShipmentGroup {

    public ReceivingNoErrorsDispatchShipmentGroup(AdapterBase parent, int id) {
        super(parent, id, "Receiving");
    }

    @Override
    protected Collection<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        SiteWrapper site = SessionManager.getCurrentSite();
        if (!SessionManager.getInstance().isAllSitesSelected()) {
            return site.getReceivingNoErrorsDispatchShipmentCollection();
        }
        return new ArrayList<ModelWrapper<?>>();
    }

}
