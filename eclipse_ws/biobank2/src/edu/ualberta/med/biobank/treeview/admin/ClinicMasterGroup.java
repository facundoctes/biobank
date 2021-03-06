package edu.ualberta.med.biobank.treeview.admin;

import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.action.clinic.ClinicGetAllAction;
import edu.ualberta.med.biobank.common.permission.clinic.ClinicCreatePermission;
import edu.ualberta.med.biobank.common.permission.clinic.ClinicReadPermission;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.gui.common.BgcLogger;
import edu.ualberta.med.biobank.model.Clinic;
import edu.ualberta.med.biobank.treeview.AbstractAdapterBase;
import edu.ualberta.med.biobank.treeview.AbstractClinicGroup;

public class ClinicMasterGroup extends AbstractClinicGroup {
    private static final I18n i18n = I18nFactory
        .getI18n(ClinicMasterGroup.class);

    @SuppressWarnings("unused")
    private static BgcLogger LOGGER = BgcLogger
        .getLogger(ClinicMasterGroup.class.getName());

    private List<Clinic> clinics = null;

    private final boolean createAllowed;

    private final boolean readAllowed;

    @SuppressWarnings("nls")
    public ClinicMasterGroup(SessionAdapter sessionAdapter, int id) {
        super(sessionAdapter, id,
            // tree node label.
            i18n.tr("All Clinics"));

        this.createAllowed = isAllowed(new ClinicCreatePermission());
        this.readAllowed = isAllowed(new ClinicReadPermission());
        this.hasChildren = this.readAllowed;
    }

    @Override
    public void performExpand() {
        if (!readAllowed) return;
        super.performExpand();
    }

    @SuppressWarnings("nls")
    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        if (!createAllowed) return;

        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(
            // menu item label.
            i18n.tr("Add Clinic"));
        mi.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                addClinic();
            }
        });
    }

    @Override
    protected List<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        if (!readAllowed) return null;

        clinics = SessionManager.getAppService().doAction(
            new ClinicGetAllAction()).getList();

        return ModelWrapper.wrapModelCollection(SessionManager.getAppService(),
            clinics, ClinicWrapper.class);
    }

    public void addClinic() {
        ClinicWrapper clinic =
            new ClinicWrapper(SessionManager.getAppService());
        ClinicAdapter adapter = new ClinicAdapter(this, clinic);
        adapter.openEntryForm();
    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        return 0;
    }
}
