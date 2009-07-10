package edu.ualberta.med.biobank.forms;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BioBankPlugin;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.forms.input.FormInput;
import edu.ualberta.med.biobank.forms.listener.EnterKeyToNextFieldListener;
import edu.ualberta.med.biobank.model.PaletteCell;
import edu.ualberta.med.biobank.model.PatientVisit;
import edu.ualberta.med.biobank.model.Sample;
import edu.ualberta.med.biobank.model.SampleCellStatus;
import edu.ualberta.med.biobank.model.SampleType;
import edu.ualberta.med.biobank.treeview.Node;
import edu.ualberta.med.biobank.treeview.PatientVisitAdapter;
import edu.ualberta.med.biobank.validators.ScannerBarcodeValidator;
import edu.ualberta.med.biobank.widgets.AddSamplesScanPaletteWidget;
import edu.ualberta.med.biobank.widgets.SampleTypeSelectionWidget;
import edu.ualberta.med.biobank.widgets.listener.ScanPaletteModificationEvent;
import edu.ualberta.med.biobank.widgets.listener.ScanPaletteModificationListener;
import edu.ualberta.med.scanlib.ScanCell;
import edu.ualberta.med.scanlib.ScanLib;
import edu.ualberta.med.scanlib.ScanLibFactory;
import gov.nih.nci.system.query.SDKQuery;
import gov.nih.nci.system.query.example.InsertExampleQuery;

public class AddPaletteSamplesEntryForm extends BiobankEntryForm {

    public static final String ID = "edu.ualberta.med.biobank.forms.AddPaletteSamplesEntryForm";

    private Button scan;

    private PatientVisitAdapter pvAdapter;

    private PatientVisit patientVisit;

    private Composite typesSelectionPerRowComposite;

    private AddSamplesScanPaletteWidget spw;

    private List<SampleTypeSelectionWidget> sampleTypeWidgets;

    private IObservableValue scannedValue = new WritableValue(Boolean.FALSE,
        Boolean.class);
    private IObservableValue plateToScan = new WritableValue("", String.class);

    private IObservableValue typesFilled = new WritableValue(Boolean.TRUE,
        Boolean.class);

    private Text plateToScanText;

    private Composite typesSelectionCustomComposite;

    private SampleTypeSelectionWidget customSelection;

    private Composite radioComponents;

    private Button confirmAndNextButton;

    private Button confirmAndClose;

    private static boolean activityToPrint = false;
    private static boolean testDisposeOn = true;

    @Override
    public void init(IEditorSite editorSite, IEditorInput input)
        throws PartInitException {
        super.init(editorSite, input);

        Node node = ((FormInput) input).getNode();
        Assert.isNotNull(node, "Null editor input");

        Assert
            .isTrue((node instanceof PatientVisitAdapter),
                "Invalid editor input: object of type "
                    + node.getClass().getName());

        pvAdapter = (PatientVisitAdapter) node;
        patientVisit = pvAdapter.getPatientVisit();
        appService = pvAdapter.getAppService();

        setPartName("Add samples for " + patientVisit.getPatient().getNumber());
    }

    @Override
    public void dispose() {
        if (testDisposeOn && activityToPrint) {
            print();
        }
    }

    @Override
    protected void handleStatusChanged(IStatus status) {
        if (status.getSeverity() == IStatus.OK) {
            form.setMessage(getOkMessage(), IMessageProvider.NONE);
            confirmAndNextButton.setEnabled(true);
            confirmAndClose.setEnabled(true);
        } else {
            form.setMessage(status.getMessage(), IMessageProvider.ERROR);
            confirmAndNextButton.setEnabled(false);
            confirmAndClose.setEnabled(false);
            if (!BioBankPlugin.getDefault().isValidPlateBarcode(
                plateToScanText.getText())) {
                scan.setEnabled(false);
            } else {
                scan.setEnabled(true);
            }
        }
    }

    @Override
    protected String getOkMessage() {
        return "Adding samples.";
    }

    @Override
    protected void createFormContent() {
        form.setText("Adding samples for patient "
            + patientVisit.getPatient().getNumber() + " for visit "
            + patientVisit.getDateDrawn());

        GridLayout layout = new GridLayout(1, false);
        form.getBody().setLayout(layout);

        createPaletteSection();
        createFieldsSection();
        createTypesSelectionSection();
        createButtonsSection();

        WritableValue wv = new WritableValue(Boolean.FALSE, Boolean.class);
        UpdateValueStrategy uvs = new UpdateValueStrategy();
        uvs.setAfterConvertValidator(new IValidator() {
            @Override
            public IStatus validate(Object value) {
                if (value instanceof Boolean && !(Boolean) value) {
                    return ValidationStatus.error("Scanner should be launched");
                } else {
                    return Status.OK_STATUS;
                }
            }

        });
        dbc.bindValue(wv, scannedValue, uvs, uvs);
        scannedValue.setValue(false);

        wv = new WritableValue(Boolean.TRUE, Boolean.class);
        uvs = new UpdateValueStrategy();
        uvs.setAfterConvertValidator(new IValidator() {
            @Override
            public IStatus validate(Object value) {
                if (value instanceof Boolean && !(Boolean) value) {
                    return ValidationStatus.error("Give a type to each sample");
                } else {
                    return Status.OK_STATUS;
                }
            }

        });
        dbc.bindValue(wv, typesFilled, uvs, uvs);
    }

    private void createPaletteSection() {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(1, false);
        client.setLayout(layout);
        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.CENTER;
        gd.grabExcessHorizontalSpace = true;
        client.setLayoutData(gd);

        spw = new AddSamplesScanPaletteWidget(client);
        spw.setVisible(true);
        toolkit.adapt(spw);
        spw.setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, false));
    }

    private void createTypesSelectionSection() {
        // Radio buttons
        radioComponents = toolkit.createComposite(form.getBody());
        RowLayout compLayout = new RowLayout();
        radioComponents.setLayout(compLayout);
        toolkit.paintBordersFor(radioComponents);
        radioComponents.setEnabled(false);

        final Button radioRowSelection = toolkit.createButton(radioComponents,
            "Row choice", SWT.RADIO);
        final Button radioCustomSelection = toolkit.createButton(
            radioComponents, "Custom Selection choice", SWT.RADIO);

        // stackLayout
        final Composite selectionComp = toolkit.createComposite(form.getBody());
        final StackLayout selectionStackLayout = new StackLayout();
        selectionComp.setLayout(selectionStackLayout);

        List<SampleType> sampleTypes = getAllSampleTypes();
        createTypeSelectionPerRowComposite(selectionComp, sampleTypes);
        createTypeSelectionCustom(selectionComp, sampleTypes);
        radioRowSelection.setSelection(true);
        selectionStackLayout.topControl = typesSelectionPerRowComposite;

        radioRowSelection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioRowSelection.getSelection()) {
                    selectionStackLayout.topControl = typesSelectionPerRowComposite;
                    selectionComp.layout();
                    for (SampleTypeSelectionWidget sampleType : sampleTypeWidgets) {
                        sampleType.addBinding(dbc);
                        sampleType.resetValues(false);
                    }
                    customSelection.addBinding(dbc);
                    spw.disableSelection();
                    typesFilled.setValue(Boolean.TRUE);
                    spw.redraw();
                }
            }
        });
        radioCustomSelection.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (radioCustomSelection.getSelection()) {
                    selectionStackLayout.topControl = typesSelectionCustomComposite;
                    selectionComp.layout();
                    for (SampleTypeSelectionWidget sampleType : sampleTypeWidgets) {
                        sampleType.removeBinding(dbc);
                    }
                    customSelection.addBinding(dbc);
                    spw.enableSelection();
                    typesFilled.setValue(spw.isEverythingTyped());
                    spw.redraw();
                }
            }
        });
    }

    private void createTypeSelectionCustom(Composite parent,
        List<SampleType> sampleTypes) {
        typesSelectionCustomComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(3, false);
        typesSelectionCustomComposite.setLayout(layout);
        toolkit.paintBordersFor(typesSelectionCustomComposite);

        Label label = toolkit.createLabel(typesSelectionCustomComposite,
            "Choose type for selected samples:");
        GridData gd = new GridData();
        gd.horizontalSpan = 3;
        label.setLayoutData(gd);

        customSelection = new SampleTypeSelectionWidget(
            typesSelectionCustomComposite, null, sampleTypes, toolkit);
        customSelection.resetValues(true);

        Button applyType = toolkit.createButton(typesSelectionCustomComposite,
            "Apply", SWT.PUSH);
        applyType.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                SampleType type = customSelection.getSelection();
                if (type != null) {
                    for (PaletteCell cell : spw.getSelectedCells()) {
                        cell.setType(type);
                        cell.setStatus(SampleCellStatus.TYPE);
                    }
                    spw.clearSelection();
                    customSelection.resetValues(true);
                    typesFilled.setValue(spw.isEverythingTyped());
                    spw.redraw();
                }
            }
        });
        spw.addModificationListener(new ScanPaletteModificationListener() {
            @Override
            public void modification(ScanPaletteModificationEvent spme) {
                customSelection.setNumber(spme.selections);
            }
        });
    }

    private void createTypeSelectionPerRowComposite(Composite parent,
        List<SampleType> sampleTypes) {
        typesSelectionPerRowComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(3, false);
        layout.horizontalSpacing = 10;
        typesSelectionPerRowComposite.setLayout(layout);
        toolkit.paintBordersFor(typesSelectionPerRowComposite);

        sampleTypeWidgets = new ArrayList<SampleTypeSelectionWidget>();
        char letter = 'A';
        for (int i = 0; i < ScanCell.ROW_MAX; i++) {
            final SampleTypeSelectionWidget typeWidget = new SampleTypeSelectionWidget(
                typesSelectionPerRowComposite, letter, sampleTypes, toolkit);
            final int indexRow = i;
            typeWidget
                .addSelectionChangedListener(new ISelectionChangedListener() {
                    @Override
                    public void selectionChanged(SelectionChangedEvent event) {
                        setTypeForRow(typeWidget, indexRow);
                        setDirty(true);
                    }

                });
            typeWidget.addBinding(dbc);
            sampleTypeWidgets.add(typeWidget);
            letter += 1;
        }
    }

    private void createFieldsSection() {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        toolkit.paintBordersFor(client);

        Composite comp = toolkit.createComposite(client);
        layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        comp.setLayout(layout);
        GridData gd = new GridData();
        gd.widthHint = 200;
        comp.setLayoutData(gd);
        toolkit.paintBordersFor(comp);
        // TODO : could be a combo as there is not other need of the handheld
        // scanner in this form !
        plateToScanText = (Text) createBoundWidgetWithLabel(comp, Text.class,
            SWT.NONE, "Plate to Scan", new String[0], plateToScan,
            ScannerBarcodeValidator.class, "Enter a valid plate barcode");
        plateToScanText.removeKeyListener(keyListener);
        plateToScanText.addKeyListener(EnterKeyToNextFieldListener.INSTANCE);

        scan = toolkit.createButton(client, "Scan", SWT.PUSH);
        scan.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                scan();
            }
        });
    }

    private void createButtonsSection() {
        Composite client = toolkit.createComposite(form.getBody());
        GridLayout layout = new GridLayout(4, false);
        layout.horizontalSpacing = 10;
        client.setLayout(layout);
        client.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        toolkit.paintBordersFor(client);

        initCancelButton(client);

        confirmAndNextButton = toolkit.createButton(client,
            "Confirm and scan next", SWT.PUSH);
        confirmAndNextButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveAndNext();
            }
        });
        confirmAndClose = toolkit.createButton(client, "Confirm and Close",
            SWT.PUSH);
        confirmAndClose.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveAndClose();
            }
        });

    }

    private void scan() {
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            public void run() {
                try {
                    PaletteCell[][] cells;
                    if (BioBankPlugin.isRealScanEnabled()) {
                        int plateNum = BioBankPlugin.getDefault()
                            .getPlateNumber(plateToScan.getValue().toString());
                        ScanLib scanLib = ScanLibFactory.getScanLib();
                        int r = scanLib
                            .slDecodePlate(ScanLib.DPI_300, plateNum);
                        if (r < 0) {
                            BioBankPlugin.openError("Scanner",
                                "Could not decode image. Return code is: " + r);
                            return;
                        }
                        cells = PaletteCell.getScanLibResults();
                    } else {
                        cells = PaletteCell.getRandomScanLink();
                    }

                    enabledOthersComponents();

                    for (int i = 0; i < cells.length; i++) { // rows
                        int samplesNumber = 0;
                        sampleTypeWidgets.get(i).resetValues(true);
                        for (int j = 0; j < cells[i].length; j++) { // columns
                            if (cells[i][j] != null) {
                                if (cells[i][j].getValue() != null) {
                                    samplesNumber++;
                                    cells[i][j].setStatus(SampleCellStatus.NEW);
                                } else {
                                    cells[i][j]
                                        .setStatus(SampleCellStatus.EMPTY);
                                }
                            }
                        }
                        sampleTypeWidgets.get(i).setNumber(samplesNumber);
                    }

                    // Show result in grid
                    spw.setScannedElements(cells);
                } catch (RemoteConnectFailureException exp) {
                    BioBankPlugin.openRemoteConnectErrorMessage();
                } catch (Exception e) {
                    SessionManager.getLogger().error("Error while scanning", e);
                }
            }
        });
    }

    protected void enabledOthersComponents() {
        scannedValue.setValue(true);
        radioComponents.setEnabled(true);

    }

    private List<SampleType> getAllSampleTypes() {
        try {
            return appService.search(SampleType.class, new SampleType());
        } catch (final RemoteConnectFailureException exp) {
            BioBankPlugin.openRemoteConnectErrorMessage();
        } catch (Exception exp) {
        }
        return null;
    }

    @Override
    protected void saveForm() {
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            public void run() {
                try {
                    List<SDKQuery> queries = new ArrayList<SDKQuery>();
                    PaletteCell[][] cells = spw.getScannedElements();
                    for (int indexRow = 0; indexRow < cells.length; indexRow++) {
                        for (int indexColumn = 0; indexColumn < cells[indexRow].length; indexColumn++) {
                            PaletteCell cell = cells[indexRow][indexColumn];
                            if (PaletteCell.hasValue(cell)
                                && cell.getStatus().equals(
                                    SampleCellStatus.TYPE)) {
                                // add new samples
                                Sample sample = new Sample();
                                sample.setInventoryId(cell.getValue());
                                sample.setPatientVisit(patientVisit);
                                sample.setSampleType(cell.getType());
                                queries.add(new InsertExampleQuery(sample));
                            }
                        }
                    }
                    appService.executeBatchQuery(queries);
                    activityToPrint = true;
                } catch (RemoteConnectFailureException exp) {
                    BioBankPlugin.openRemoteConnectErrorMessage();
                } catch (Exception e) {
                    SessionManager.getLogger().error(
                        "Error when adding samples", e);
                }
            }
        });
    }

    private void setTypeForRow(SampleTypeSelectionWidget typeWidget,
        int indexRow) {
        if (typeWidget.needToSave()) {
            SampleType type = typeWidget.getSelection();
            PaletteCell[][] cells = spw.getScannedElements();
            for (int indexColumn = 0; indexColumn < cells[indexRow].length; indexColumn++) {
                PaletteCell cell = cells[indexRow][indexColumn];
                if (PaletteCell.hasValue(cell)) {
                    cell.setType(type);
                    cell.setStatus(SampleCellStatus.TYPE);
                    spw.redraw();
                }
            }
        }
    }

    @Override
    protected void cancelForm() {
        spw.setScannedElements(null);
        for (SampleTypeSelectionWidget stw : sampleTypeWidgets) {
            stw.resetValues(true);
        }
    }

    @Override
    public void setFocus() {
        if (plateToScan.getValue().toString().isEmpty()) {
            plateToScanText.setFocus();
        }
    }

    private void saveAndClose() {
        testDisposeOn = true;
        doSaveInternal();
        getSite().getPage().closeEditor(AddPaletteSamplesEntryForm.this, false);
        pvAdapter.performExpand();
        Node.openForm(new FormInput(pvAdapter), PatientVisitViewForm.ID);
    }

    private void print() {
        if (BioBankPlugin.isAskPrint()) {
            boolean doPrint = MessageDialog.openQuestion(PlatformUI
                .getWorkbench().getActiveWorkbenchWindow().getShell(), "Print",
                "Do you want to print information ?");
            if (doPrint) {
                // FIXME implement print functionality
            }
        }
        activityToPrint = false;
    }

    private void saveAndNext() {
        testDisposeOn = false;
        doSaveInternal();
        getSite().getPage().closeEditor(AddPaletteSamplesEntryForm.this, false);
        Node.openForm(new FormInput(pvAdapter), AddPaletteSamplesEntryForm.ID);
    }

}
