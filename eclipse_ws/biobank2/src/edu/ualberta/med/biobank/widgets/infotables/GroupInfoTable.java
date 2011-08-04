package edu.ualberta.med.biobank.widgets.infotables;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.security.Group;
import edu.ualberta.med.biobank.dialogs.user.GroupEditDialog;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.widgets.BiobankLabelProvider;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class GroupInfoTable extends InfoTableWidget<Group> {
    public static final int ROWS_PER_PAGE = 12;
    private static final String[] HEADINGS = new String[] { Messages.GroupInfoTable_name_label };
    private static final String LOADING_ROW = Messages.GroupInfoTable_loading;
    private static final String GROUP_DELETE_ERROR = Messages.GroupInfoTable_delete_error_msg;
    private static final String CONFIRM_DELETE_TITLE = Messages.GroupInfoTable_delete_confirm_title;
    private static final String CONFIRM_DELETE_MESSAGE = Messages.GroupInfoTable_delete_confirm_msg;

    public GroupInfoTable(Composite parent, List<Group> collection) {
        super(parent, collection, HEADINGS, ROWS_PER_PAGE, Group.class);

        addEditItemListener(new IInfoTableEditItemListener() {
            @Override
            public void editItem(InfoTableEvent event) {
                editGroup((Group) getSelection());
            }
        });

        addDeleteItemListener(new IInfoTableDeleteItemListener() {
            @Override
            public void deleteItem(InfoTableEvent event) {
                deleteGroup((Group) getSelection());
            }
        });
    }

    @SuppressWarnings("serial")
    @Override
    protected BiobankTableSorter getComparator() {
        return new BiobankTableSorter() {
            @Override
            public int compare(Object o1, Object o2) {
                if (o1 instanceof Group && o2 instanceof Group) {
                    Group g1 = (Group) o1;
                    Group g2 = (Group) o2;

                    int cmp = g1.getName().compareToIgnoreCase(g2.getName());
                    if (cmp != 0) {
                        return cmp;
                    }
                }
                return 0;
            }
        };
    }

    @Override
    protected String getCollectionModelObjectToString(Object o) {
        if (o == null) {
            return null;
        }

        Group group = (Group) o;
        return StringUtils.join(Arrays.asList(group.getName()), "\t"); //$NON-NLS-1$
    }

    @Override
    protected IBaseLabelProvider getLabelProvider() {
        return new BiobankLabelProvider() {
            @Override
            public Image getColumnImage(Object element, int columnIndex) {
                return null;
            }

            @Override
            public String getColumnText(Object element, int columnIndex) {
                Group group = (Group) ((BiobankCollectionModel) element).o;
                if (group == null) {
                    if (columnIndex == 0) {
                        return LOADING_ROW;
                    }
                    return ""; //$NON-NLS-1$
                }

                switch (columnIndex) {
                case 0:
                    return group.getName();
                default:
                    return ""; //$NON-NLS-1$
                }
            }
        };
    }

    protected void editGroup(Group group) {
        GroupEditDialog dlg = new GroupEditDialog(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), group, null, false);
        int res = dlg.open();
        if (res == Dialog.OK) {
            reloadCollection(getCollection(), group);
            notifyListeners();
        }
    }

    protected boolean deleteGroup(Group group) {
        try {
            String name = group.getName();
            String message = MessageFormat.format(CONFIRM_DELETE_MESSAGE,
                new Object[] { name });

            if (BgcPlugin.openConfirm(CONFIRM_DELETE_TITLE, message)) {
                SessionManager.getAppService().deleteGroup(
                    SessionManager.getUser(), group);
                // remove the user from the collection
                getCollection().remove(group);
                reloadCollection(getCollection(), null);
                notifyListeners();
                return true;
            }
        } catch (ApplicationException e) {
            BgcPlugin.openAsyncError(GROUP_DELETE_ERROR, e);
        }
        return false;
    }
}