package edu.ualberta.med.biobank.dialogs;

import java.util.Collection;
import java.util.HashMap;

import org.eclipse.core.databinding.beans.PojoObservables;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleStorageWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.validators.DoubleNumberValidator;
import edu.ualberta.med.biobank.validators.IntegerNumberValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;

public class SampleStorageDialog extends BiobankDialog {

    private static final String TITLE = "Sample Storage";

    private SampleStorageWrapper origSampleStorage;

    private SampleStorageWrapper sampleStorage;

    private HashMap<String, SampleTypeWrapper> sampleTypeMap;

    private ComboViewer sampleTypeComboViewer;

    private ComboViewer activityStatusComboViewer;

    private String currentTitle;

    public SampleStorageDialog(Shell parent,
        SampleStorageWrapper sampleStorage,
        Collection<SampleTypeWrapper> sampleTypes) {
        super(parent);
        Assert.isNotNull(sampleStorage);
        Assert.isNotNull(sampleTypes);
        this.origSampleStorage = sampleStorage;
        this.sampleStorage = new SampleStorageWrapper(null);
        this.sampleStorage.setSampleType(sampleStorage.getSampleType());
        this.sampleStorage.setVolume(sampleStorage.getVolume());
        this.sampleStorage.setQuantity(sampleStorage.getQuantity());
        this.sampleStorage.setActivityStatus(sampleStorage.getActivityStatus());
        sampleTypeMap = new HashMap<String, SampleTypeWrapper>();
        for (SampleTypeWrapper st : sampleTypes) {
            sampleTypeMap.put(st.getName(), st);
        }
        if (origSampleStorage.getSampleType() == null) {
            currentTitle = "Add " + TITLE;
        } else {
            currentTitle = "Edit " + TITLE;
        }
    }

    @Override
    protected String getDialogShellTitle() {
        return currentTitle;
    }

    @Override
    protected String getTitleAreaMessage() {
        return "";
    }

    @Override
    protected String getTitleAreaTitle() {
        return currentTitle;
    }

    @Override
    protected Image getTitleAreaImage() {
        return BioBankPlugin.getDefault().getImageRegistry()
            .get(BioBankPlugin.IMG_COMPUTER_KEY);
    }

    @Override
    protected void createDialogAreaInternal(Composite parent) throws Exception {
        Composite contents = new Composite(parent, SWT.NONE);
        contents.setLayout(new GridLayout(2, false));
        contents.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        String selection = null;
        SampleTypeWrapper st = sampleStorage.getSampleType();
        if (st != null) {
            selection = st.getName();
        }

        sampleTypeComboViewer = getWidgetCreator()
            .createComboViewerWithNoSelectionValidator(contents, "Sample Type",
                sampleTypeMap.keySet(), selection,
                "A sample type should be selected");
        sampleTypeComboViewer
            .addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection stSelection = (IStructuredSelection) sampleTypeComboViewer
                        .getSelection();
                    sampleStorage.setSampleType(sampleTypeMap.get(stSelection
                        .getFirstElement()));
                }
            });

        activityStatusComboViewer = getWidgetCreator()
            .createComboViewerWithNoSelectionValidator(
                contents,
                "Activity Status",
                ActivityStatusWrapper.getAllActivityStatuses(SessionManager
                    .getAppService()), sampleStorage.getActivityStatus(),
                "A sample type should be selected");
        activityStatusComboViewer
            .addSelectionChangedListener(new ISelectionChangedListener() {
                @Override
                public void selectionChanged(SelectionChangedEvent event) {
                    IStructuredSelection asSelection = (IStructuredSelection) activityStatusComboViewer
                        .getSelection();
                    try {
                        sampleStorage
                            .setActivityStatus((ActivityStatusWrapper) asSelection
                                .getFirstElement());
                    } catch (Exception e) {
                        BioBankPlugin.openAsyncError(
                            "Error setting activity status", e);
                    }
                }
            });

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "Volume (ml)", new String[0],
            PojoObservables.observeValue(sampleStorage, "volume"),
            new DoubleNumberValidator("Volume should be a real number", false));

        createBoundWidgetWithLabel(contents, BiobankText.class, SWT.BORDER,
            "Quantity", new String[0], PojoObservables.observeValue(
                sampleStorage, "quantity"), new IntegerNumberValidator(
                "Quantity should be a whole number", false));
    }

    @Override
    protected void okPressed() {
        origSampleStorage.setSampleType(sampleStorage.getSampleType());
        origSampleStorage.setVolume(sampleStorage.getVolume());
        origSampleStorage.setQuantity(sampleStorage.getQuantity());
        origSampleStorage.setActivityStatus(sampleStorage.getActivityStatus());
        super.okPressed();
    }

}
