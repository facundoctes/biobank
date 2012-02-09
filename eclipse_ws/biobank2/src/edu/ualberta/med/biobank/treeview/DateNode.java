package edu.ualberta.med.biobank.treeview;

import java.util.Date;
import java.util.List;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Tree;

import edu.ualberta.med.biobank.common.formatters.DateFormatter;
import edu.ualberta.med.biobank.common.wrappers.ClinicWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;

public class DateNode extends AdapterBase {

    private String text;
    private Date date;

    public DateNode(AdapterBase parent, String text, Date date) {
        super(parent, (int) date.getTime() + text.hashCode(), text + ": " //$NON-NLS-1$
            + DateFormatter.formatAsDate(date), true);
        this.setDate(date);
    }

    @Override
    public List<AbstractAdapterBase> search(Class<?> searchedClass,
        Integer objectId) {
        return findChildFromClass(searchedClass, objectId, ClinicWrapper.class);
    }

    @Override
    protected AdapterBase createChildNode() {
        return null;
    }

    @Override
    protected AdapterBase createChildNode(Object child) {
        return null;
    }

    @Override
    public String getEntryFormId() {
        return null;
    }

    @Override
    protected String getLabelInternal() {
        return null;
    }

    @Override
    public String getTooltipTextInternal() {
        return null;
    }

    @Override
    public String getViewFormId() {
        return null;
    }

    @Override
    protected List<? extends ModelWrapper<?>> getWrapperChildren()
        throws Exception {
        return null;
    }

    @Override
    protected int getWrapperChildCount() throws Exception {
        return 0;
    }

    @Override
    public void popupMenu(TreeViewer tv, Tree tree, Menu menu) {
        //
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    @Override
    public int compareTo(AbstractAdapterBase o) {
        if (o instanceof DateNode)
            return date.compareTo(((DateNode) o).date);
        return 0;
    }
}
