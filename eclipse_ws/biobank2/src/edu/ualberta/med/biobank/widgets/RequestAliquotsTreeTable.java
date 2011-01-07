package edu.ualberta.med.biobank.widgets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.util.RequestAliquotState;
import edu.ualberta.med.biobank.common.wrappers.RequestAliquotWrapper;
import edu.ualberta.med.biobank.common.wrappers.RequestWrapper;
import edu.ualberta.med.biobank.forms.utils.RequestTableGroup;
import edu.ualberta.med.biobank.treeview.AdapterBase;
import edu.ualberta.med.biobank.treeview.RequestAliquotAdapter;
import edu.ualberta.med.biobank.treeview.admin.RequestContainerAdapter;

public class RequestAliquotsTreeTable extends BiobankWidget {

    private TreeViewer tv;
    private RequestWrapper shipment;
    protected List<RequestTableGroup> groups;

    public RequestAliquotsTreeTable(Composite parent, RequestWrapper shipment) {
        super(parent, SWT.NONE);

        this.shipment = shipment;

        setLayout(new FillLayout());
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.heightHint = 400;
        setLayoutData(gd);

        tv = new TreeViewer(this, SWT.MULTI | SWT.BORDER);
        Tree tree = tv.getTree();
        tree.setHeaderVisible(true);
        tree.setLinesVisible(true);

        TreeColumn tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Inventory Id");
        tc.setWidth(200);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Type");
        tc.setWidth(100);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Location");
        tc.setWidth(120);

        tc = new TreeColumn(tree, SWT.LEFT);
        tc.setText("Claimed By");
        tc.setWidth(100);

        ITreeContentProvider contentProvider = new ITreeContentProvider() {
            @Override
            public void dispose() {
            }

            @Override
            public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput) {
                groups = RequestTableGroup
                    .getGroupsForShipment(RequestAliquotsTreeTable.this.shipment);
            }

            @Override
            public Object[] getElements(Object inputElement) {
                return groups.toArray();
            }

            @Override
            public Object[] getChildren(Object parentElement) {
                if (parentElement instanceof RequestTableGroup)
                    return ((RequestTableGroup) parentElement).getChildren()
                        .toArray();
                else
                    return ((AdapterBase) parentElement).getChildren()
                        .toArray();
            }

            @Override
            public Object getParent(Object element) {
                if (element instanceof AdapterBase)
                    return ((AdapterBase) element).getParent();
                return null;
            }

            @Override
            public boolean hasChildren(Object element) {
                if (element instanceof RequestTableGroup)
                    return true;
                else
                    return ((AdapterBase) element).hasChildren();
            }
        };
        tv.setContentProvider(contentProvider);

        final BiobankLabelProvider labelProvider = new BiobankLabelProvider() {
            @Override
            public String getColumnText(Object element, int columnIndex) {
                if (element instanceof RequestTableGroup) {
                    if (columnIndex == 0)
                        return ((RequestTableGroup) element).getTitle();
                    return "";
                } else if (element instanceof RequestContainerAdapter) {
                    if (columnIndex == 0)
                        return ((RequestContainerAdapter) element).getLabel();
                    return "";
                } else if (element instanceof RequestAliquotAdapter) {
                    switch (columnIndex) {
                    case 0:
                        return ((RequestAliquotAdapter) element).getLabel();
                    case 1:
                        return ((RequestAliquotAdapter) element)
                            .getSampleType();
                    case 2:
                        return ((RequestAliquotAdapter) element).getPosition();
                    case 3:
                        return ((RequestAliquotAdapter) element).getClaimedBy();
                    default:
                        return "";
                    }
                }
                return "";
            }
        };
        tv.setLabelProvider(labelProvider);
        tv.setInput("root");

        tv.addDoubleClickListener(new IDoubleClickListener() {
            @Override
            public void doubleClick(DoubleClickEvent event) {
                Object o = ((IStructuredSelection) tv.getSelection())
                    .getFirstElement();
                if (o instanceof RequestAliquotWrapper) {
                    RequestAliquotWrapper ra = (RequestAliquotWrapper) o;
                    SessionManager.openViewForm(ra.getAliquot());
                }
            }
        });

        if (shipment.isInAcceptedState()) {
            final Menu menu = new Menu(this);
            tv.getTree().setMenu(menu);

            menu.addListener(SWT.Show, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    for (MenuItem menuItem : menu.getItems()) {
                        menuItem.dispose();
                    }

                    RequestAliquotWrapper ra = getSelectedAliquot();
                    if (ra != null) {
                        addClipboardCopySupport(menu, labelProvider);
                        addSetUnavailableMenu(menu);
                        addClaimMenu(menu);
                    } else {
                        AdapterBase node = getSelectedNode();
                        if (node != null) {
                            addClaimMenu(menu);
                        }
                    }
                }
            });
        }
    }

    protected AdapterBase getSelectedNode() {
        IStructuredSelection selection = (IStructuredSelection) tv
            .getSelection();
        if (selection != null && selection.size() > 0)
            return (AdapterBase) selection.getFirstElement();
        return null;
    }

    protected RequestAliquotWrapper getSelectedAliquot() {
        AdapterBase node = getSelectedNode();
        if (node != null && node instanceof RequestAliquotAdapter) {
            return (RequestAliquotWrapper) ((RequestAliquotAdapter) node)
                .getModelObject();
        }
        return null;
    }

    protected void addClaimMenu(Menu menu) {
        MenuItem item;
        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Claim");
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                claim(getSelectedNode());
                tv.refresh();
            }
        });
    }

    protected void claim(AdapterBase node) {
        try {
            if (node instanceof RequestAliquotAdapter) {
                RequestAliquotWrapper a = (RequestAliquotWrapper) ((RequestAliquotAdapter) node)
                    .getModelObject();
                a.setClaimedBy(SessionManager.getUser().getFirstName());
                a.persist();
            } else {
                List<AdapterBase> children = node.getChildren();
                for (AdapterBase child : children)
                    claim(child);
            }
        } catch (Exception e) {
            BioBankPlugin.openAsyncError("Failed to claim", e);
        }
    }

    private void addSetUnavailableMenu(final Menu menu) {
        MenuItem item;
        item = new MenuItem(menu, SWT.PUSH);
        item.setText("Flag as unavailable");
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                getSelectedAliquot().setState(
                    RequestAliquotState.UNAVAILABLE_STATE.getId());
                shipment.resetStateLists();
                tv.refresh();
            }
        });
    }

    public void refresh() {
        tv.refresh();
    }

    private void addClipboardCopySupport(Menu menu,
        final BiobankLabelProvider labelProvider) {
        Assert.isNotNull(menu);
        MenuItem item = new MenuItem(menu, SWT.PUSH);
        item.setText("Copy");
        item.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent event) {
                int numCols = tv.getTree().getColumnCount();
                List<Object> selectedRows = new ArrayList<Object>();
                IStructuredSelection sel = (IStructuredSelection) tv
                    .getSelection();
                for (Iterator<Object> iterator = sel.iterator(); iterator
                    .hasNext();) {
                    Object item = iterator.next();
                    String row = "";
                    for (int i = 0; i < numCols; i++) {
                        String text = labelProvider.getColumnText(item, i);
                        if (text != null)
                            row += text;
                        if (i < numCols - 1)
                            row += ", ";
                    }
                    selectedRows.add(row);
                }
                if (selectedRows.size() > 0) {
                    StringBuilder sb = new StringBuilder();
                    for (Object row : selectedRows) {
                        if (sb.length() != 0) {
                            sb.append(System.getProperty("line.separator"));
                        }
                        sb.append(row.toString());
                    }
                    TextTransfer textTransfer = TextTransfer.getInstance();
                    Clipboard cb = new Clipboard(Display.getDefault());
                    cb.setContents(new Object[] { sb.toString() },
                        new Transfer[] { textTransfer });
                }
            }
        });
    }

    public boolean isAllProcessed() {
        return groups.get(RequestAliquotState.NONPROCESSED_STATE.getId())
            .getChildren().size() == 0;
    }

}
