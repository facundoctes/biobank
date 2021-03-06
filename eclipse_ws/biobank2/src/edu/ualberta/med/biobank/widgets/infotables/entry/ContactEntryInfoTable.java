package edu.ualberta.med.biobank.widgets.infotables.entry;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.xnap.commons.i18n.I18n;
import org.xnap.commons.i18n.I18nFactory;

import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ContactWrapper;
import edu.ualberta.med.biobank.dialogs.select.ContactAddDialog;
import edu.ualberta.med.biobank.gui.common.BgcPlugin;
import edu.ualberta.med.biobank.gui.common.widgets.IInfoTableAddItemListener;
import edu.ualberta.med.biobank.gui.common.widgets.IInfoTableDeleteItemListener;
import edu.ualberta.med.biobank.gui.common.widgets.IInfoTableEditItemListener;
import edu.ualberta.med.biobank.gui.common.widgets.InfoTableEvent;
import edu.ualberta.med.biobank.widgets.infotables.BiobankTableSorter;
import edu.ualberta.med.biobank.widgets.infotables.ContactInfoTable;

public class ContactEntryInfoTable extends ContactInfoTable {
    public static final I18n i18n = I18nFactory
        .getI18n(ContactEntryInfoTable.class);

    private List<ContactWrapper> addedOrModifiedContacts;

    private List<ContactWrapper> deletedContacts;

    private List<ContactWrapper> originalContacts;

    public ContactEntryInfoTable(Composite parent, List<ContactWrapper> contacts) {
        super(parent, contacts);
        originalContacts = new ArrayList<ContactWrapper>();
        if (contacts != null) {
            originalContacts.addAll(contacts);
        }
        if (contacts == null) {
            contacts = new ArrayList<ContactWrapper>();
        }
        addedOrModifiedContacts = new ArrayList<ContactWrapper>();
        deletedContacts = new ArrayList<ContactWrapper>();

        addAddItemListener(new IInfoTableAddItemListener<ContactWrapper>() {
            @Override
            public void addItem(InfoTableEvent<ContactWrapper> event) {
                addContact();
            }
        });

        addEditItemListener(new IInfoTableEditItemListener<ContactWrapper>() {
            @Override
            public void editItem(InfoTableEvent<ContactWrapper> event) {
                ContactWrapper contact = getSelection();
                if (contact != null)
                    addOrEditContact(false, contact);
            }
        });

        addDeleteItemListener(new IInfoTableDeleteItemListener<ContactWrapper>() {
            @SuppressWarnings("nls")
            @Override
            public void deleteItem(InfoTableEvent<ContactWrapper> event) {
                ContactWrapper contact = getSelection();
                if (contact != null) {
                    if (!contact.deleteAllowed()) {
                        BgcPlugin
                            .openError(
                                i18n.tr("Contact Delete Error"),
                                i18n.tr(
                                    "Cannot delete contact \"{0}\" since it is associated with one or more studies",
                                    contact.getName()));
                        return;
                    }

                    if (!BgcPlugin
                        .openConfirm(
                            i18n.tr("Delete Contact"),
                            i18n.tr(
                                "Are you sure you want to delete contact \"{0}\"?",
                                contact.getName()))) {
                        return;
                    }

                    deletedContacts.add(contact);
                    getList().remove(contact);
                    notifyListeners();
                }
            }
        });
    }

    @Override
    protected boolean isEditMode() {
        return true;
    }

    @SuppressWarnings("nls")
    private void addOrEditContact(boolean add, ContactWrapper contactWrapper) {
        ContactAddDialog dlg = new ContactAddDialog(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(), contactWrapper);
        int res = dlg.open();
        if (res == Dialog.OK) {
            ContactWrapper contact = dlg.getContactWrapper();
            if (add) {
                // only add to the collection when adding and not editing
                getList().add(contact);
                addedOrModifiedContacts.add(contact);
            }
            notifyListeners();
        } else if (!add && res == Dialog.CANCEL) {
            try {
                contactWrapper.reload();
            } catch (Exception e) {
                BgcPlugin.openAsyncError(
                    // error dialog
                    i18n.tr("Cancel error"), e);
            }
        }
    }

    public void addContact() {
        addOrEditContact(true,
            new ContactWrapper(SessionManager.getAppService()));
    }

    public List<ContactWrapper> getAddedOrModifedContacts() {
        return addedOrModifiedContacts;
    }

    public List<ContactWrapper> getDeletedContacts() {
        return deletedContacts;
    }

    @Override
    public void reload() {
        addedOrModifiedContacts = new ArrayList<ContactWrapper>();
        deletedContacts = new ArrayList<ContactWrapper>();
        setList(new ArrayList<ContactWrapper>(originalContacts), null);
    }

    @SuppressWarnings("serial")
    @Override
    protected BiobankTableSorter getComparator() {
        return new BiobankTableSorter() {
            @Override
            public int compare(Object e1, Object e2) {
                try {
                    TableRowData i1 = getCollectionModelObject(e1);
                    TableRowData i2 = getCollectionModelObject(e2);
                    return super.compare(i1.name, i2.name);
                } catch (Exception e) {
                    return 0;
                }
            }
        };
    }
}
