package edu.ualberta.med.biobank.forms.linkassign;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.springframework.remoting.RemoteConnectFailureException;

import edu.ualberta.med.biobank.BiobankPlugin;
import edu.ualberta.med.biobank.Messages;
import edu.ualberta.med.biobank.SessionManager;
import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.exception.ContainerLabelSearchException;
import edu.ualberta.med.biobank.common.peer.ContainerPeer;
import edu.ualberta.med.biobank.common.scanprocess.data.AssignProcessData;
import edu.ualberta.med.biobank.common.scanprocess.data.ProcessData;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ActivityStatusWrapper;
import edu.ualberta.med.biobank.common.wrappers.CollectionEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerLabelingSchemeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.SpecimenWrapper;
import edu.ualberta.med.biobank.dialogs.select.SelectParentContainerDialog;
import edu.ualberta.med.biobank.forms.listener.EnterKeyToNextFieldListener;
import edu.ualberta.med.biobank.logs.BiobankLogger;
import edu.ualberta.med.biobank.validators.AbstractValidator;
import edu.ualberta.med.biobank.validators.NonEmptyStringValidator;
import edu.ualberta.med.biobank.validators.StringLengthValidator;
import edu.ualberta.med.biobank.widgets.BiobankText;
import edu.ualberta.med.biobank.widgets.grids.ContainerDisplayWidget;
import edu.ualberta.med.biobank.widgets.grids.ScanPalletDisplay;
import edu.ualberta.med.biobank.widgets.grids.ScanPalletWidget;
import edu.ualberta.med.biobank.widgets.grids.cell.PalletCell;
import edu.ualberta.med.biobank.widgets.grids.cell.UICellStatus;
import edu.ualberta.med.biobank.widgets.utils.ComboSelectionUpdate;
import edu.ualberta.med.scannerconfig.dmscanlib.ScanCell;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class GenericAssignEntryForm extends AbstractLinkAssignEntryForm {

    public static final String ID = "edu.ualberta.med.biobank.forms.GenericAssignEntryForm"; //$NON-NLS-1$

    private static BiobankLogger logger = BiobankLogger
        .getLogger(GenericAssignEntryForm.class.getName());

    private static boolean singleMode = false;

    private static final String INVENTORY_ID_BINDING = "inventoryId-binding";

    private static final String NEW_SINGLE_POSITION_BINDING = "newSinglePosition-binding";

    private static final String PRODUCT_BARCODE_BINDING = "productBarcode-binding";

    private static final String LABEL_BINDING = "label-binding";

    private static final String PALLET_TYPES_BINDING = "palletType-binding";

    // parents of either the specimen in single mode or the pallet/box in
    // multiple mode. First container, is the direct parent, second is the
    // parent parent, etc...
    private List<ContainerWrapper> parentContainers;

    // for single specimen assign
    private BiobankText inventoryIdText;
    protected boolean inventoryIdModified;
    private Label oldSinglePositionLabel;
    private BiobankText oldSinglePositionText;
    private Label oldSinglePositionCheckLabel;
    private AbstractValidator oldSinglePositionCheckValidator;
    private BiobankText oldSinglePositionCheckText;
    private Label newSinglePositionLabel;
    private StringLengthValidator newSinglePositionValidator;
    private BiobankText newSinglePositionText;
    protected boolean positionTextModified;
    private static IObservableValue canLaunchCheck = new WritableValue(
        Boolean.TRUE, Boolean.class);
    private Label thirdSingleParentLabel;
    private Label secondSingleParentLabel;
    private ContainerDisplayWidget thirdSingleParentWidget;
    private ContainerDisplayWidget secondSingleParentWidget;
    private Composite singleVisualisation;

    // for multiple specimens assign
    private ScanPalletWidget palletWidget;
    private ContainerWrapper currentMultipleContainer;
    private Composite multipleVisualisation;
    protected boolean palletproductBarcodeTextModified;
    private NonEmptyStringValidator productBarcodeValidator;
    protected boolean multipleModificationMode;
    private IObservableValue multipleValidationMade = new WritableValue(
        Boolean.TRUE, Boolean.class);
    private Control nextFocusWidget;
    // Label of the pallet found with given product barcode
    private String palletFoundWithProductBarcodeLabel;
    private NonEmptyStringValidator palletLabelValidator;
    private BiobankText palletPositionText;
    protected boolean useNewProductBarcode;
    private ContainerWrapper containerToRemove;
    private ComboViewer palletTypesViewer;
    private ContainerDisplayWidget freezerWidget;
    private ContainerDisplayWidget hotelWidget;
    private Label freezerLabel;
    private Label hotelLabel;
    private Label palletLabel;
    protected boolean palletPositionTextModified;
    private List<ContainerTypeWrapper> palletContainerTypes;
    private BiobankText palletproductBarcodeText;
    private boolean saveEvenIfMissing;
    private boolean isFakeScanLinkedOnly;
    private Button fakeScanLinkedOnlyButton;

    @Override
    protected void init() throws Exception {
        super.init();
        setCanLaunchScan(true);
        currentMultipleContainer = new ContainerWrapper(appService);
        initPalletValues();
        addBooleanBinding(new WritableValue(Boolean.TRUE, Boolean.class),
            multipleValidationMade, "Validation needed: hit enter"); //$NON-NLS-1$
    }

    private void initPalletValues() {
        try {
            currentMultipleContainer.initObjectWith(new ContainerWrapper(
                appService));
            currentMultipleContainer.reset();
            currentMultipleContainer.setActivityStatus(ActivityStatusWrapper
                .getActiveActivityStatus(appService));
            currentMultipleContainer.setSite(SessionManager.getUser()
                .getCurrentWorkingSite());
        } catch (Exception e) {
            logger.error("Error while reseting pallet values", e); //$NON-NLS-1$
        }
    }

    @Override
    protected String getActivityTitle() {
        return "Generic Assign";
    }

    @Override
    public BiobankLogger getErrorLogger() {
        return logger;
    }

    @Override
    protected String getFormTitle() {
        return "Assign position";
    }

    @Override
    protected boolean isSingleMode() {
        return singleMode;
    }

    @Override
    protected void setSingleMode(boolean single) {
        singleMode = single;
    }

    @Override
    protected void createCommonFields(Composite commonFieldsComposite) {
        BiobankText siteLabel = createReadOnlyLabelledField(
            commonFieldsComposite, SWT.NONE,
            Messages.getString("ScanAssign.site.label")); //$NON-NLS-1$
        siteLabel.setText(SessionManager.getUser().getCurrentWorkingCenter()
            .getNameShort());
    }

    @Override
    protected int getLeftSectionWidth() {
        return 450;
    }

    @Override
    protected void createSingleFields(Composite parent) {
        Composite fieldsComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        fieldsComposite.setLayout(layout);
        toolkit.paintBordersFor(fieldsComposite);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        fieldsComposite.setLayoutData(gd);

        // inventoryID
        inventoryIdText = (BiobankText) createBoundWidgetWithLabel(
            fieldsComposite, BiobankText.class, SWT.NONE,
            Messages.getString("Cabinet.inventoryId.label"), new String[0], //$NON-NLS-1$
            singleSpecimen,
            "inventoryId", //$NON-NLS-1$
            new NonEmptyStringValidator("Inventory Id should be selected"),
            INVENTORY_ID_BINDING);
        inventoryIdText.addKeyListener(textFieldKeyListener);
        inventoryIdText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (inventoryIdModified)
                    try {
                        retrieveSpecimenData();
                    } catch (Exception ex) {
                        BiobankPlugin.openError("Move - specimen error", ex); //$NON-NLS-1$
                        focusControlInError(inventoryIdText);
                    }
                inventoryIdModified = false;
            }
        });
        inventoryIdText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                inventoryIdModified = true;
                positionTextModified = true;
                // resultShownValue.setValue(Boolean.FALSE);
                displayPositions(false);
            }
        });
        createPositionFields(fieldsComposite);
    }

    /**
     * 
     * Single assign. Search the specimen, if find it, display related
     * information
     */
    protected void retrieveSpecimenData() throws Exception {
        String inventoryId = inventoryIdText.getText();
        if (inventoryId.isEmpty()) {
            return;
        }
        // FIXME only for old Cabinet... SHould we still search for that?
        // if (inventoryId.length() == 4) {
        // // compatibility with old cabinet specimens imported
        // // 4 letters specimens are now C+4letters
        //            inventoryId = "C" + inventoryId; //$NON-NLS-1$
        // }
        // resultShownValue.setValue(false);
        reset();
        singleSpecimen.setInventoryId(inventoryId);
        inventoryIdText.setText(inventoryId);
        oldSinglePositionCheckText.setText("?");

        appendLog(Messages.getString("Cabinet.activitylog.gettingInfoId", //$NON-NLS-1$
            singleSpecimen.getInventoryId()));
        SpecimenWrapper foundSpecimen = SpecimenWrapper.getSpecimen(appService,
            singleSpecimen.getInventoryId(), SessionManager.getUser());
        if (foundSpecimen == null) {
            canLaunchCheck.setValue(false);
            throw new Exception("No specimen found with inventoryId " //$NON-NLS-1$
                + singleSpecimen.getInventoryId());
        }
        singleSpecimen.initObjectWith(foundSpecimen);
        // List<SpecimenTypeWrapper> possibleTypes = getCabinetSpecimenTypes();
        // if (!possibleTypes.contains(specimen.getSpecimenType())) {
        // canLaunchCheck.setValue(false);
        // throw new Exception(
        //                "This specimen is of type " + specimen.getSpecimenType().getNameShort() //$NON-NLS-1$
        // + ": this is not a cabinet type");
        // }
        if (singleSpecimen.isUsedInDispatch()) {
            canLaunchCheck.setValue(false);
            throw new Exception(
                "This specimen is currently in transit in a dispatch.");
        }
        canLaunchCheck.setValue(true);
        String positionString = singleSpecimen.getPositionString(true, false);
        if (positionString == null) {
            displayOldCabinetFields(false);
            positionString = "none"; //$NON-NLS-1$
        } else {
            displayOldCabinetFields(true);
            oldSinglePositionCheckText.setText(oldSinglePositionCheckText
                .getText());
            oldSinglePositionCheckText.setFocus();
        }
        oldSinglePositionText.setText(positionString);
        appendLog(Messages
            .getString(
                "Cabinet.activitylog.specimenInfo", singleSpecimen.getInventoryId(), //$NON-NLS-1$
                positionString));
    }

    /**
     * Single assign: Some fields will be displayed only if the specimen has
     * already a position
     */
    private void createPositionFields(Composite fieldsComposite) {
        // for move mode: display old position retrieved from database
        oldSinglePositionLabel = widgetCreator.createLabel(fieldsComposite,
            Messages.getString("Cabinet.old.position.label"));
        oldSinglePositionText = (BiobankText) widgetCreator.createBoundWidget(
            fieldsComposite, BiobankText.class, SWT.NONE,
            oldSinglePositionLabel, new String[0], null, null);
        oldSinglePositionText.setEnabled(false);
        oldSinglePositionText
            .addKeyListener(EnterKeyToNextFieldListener.INSTANCE);

        // for move mode: field to enter old position. Check needed to be sure
        // nothing is wrong with the specimen
        oldSinglePositionCheckLabel = widgetCreator.createLabel(
            fieldsComposite,
            Messages.getString("Cabinet.old.position.check.label"));
        oldSinglePositionCheckValidator = new AbstractValidator(
            "Enter correct old position") {
            @Override
            public IStatus validate(Object value) {
                if (value != null && !(value instanceof String)) {
                    throw new RuntimeException(
                        "Not supposed to be called for non-strings.");
                }

                if (value != null) {
                    String s = (String) value;
                    if (s.equals(oldSinglePositionText.getText())) {
                        hideDecoration();
                        return Status.OK_STATUS;
                    }
                }
                showDecoration();
                return ValidationStatus.error(errorMessage);
            }
        };
        oldSinglePositionCheckText = (BiobankText) widgetCreator
            .createBoundWidget(fieldsComposite, BiobankText.class, SWT.NONE,
                oldSinglePositionCheckLabel, new String[0], new WritableValue(
                    "", String.class), oldSinglePositionCheckValidator);
        oldSinglePositionCheckText
            .addKeyListener(EnterKeyToNextFieldListener.INSTANCE);

        // for all modes: position to be assigned to the specimen
        newSinglePositionLabel = widgetCreator.createLabel(fieldsComposite,
            Messages.getString("Cabinet.position.label"));
        newSinglePositionValidator = new StringLengthValidator(4,
            Messages.getString("Cabinet.position.validationMsg"));
        displayOldCabinetFields(false);
        newSinglePositionText = (BiobankText) widgetCreator
            .createBoundWidget(
                fieldsComposite,
                BiobankText.class,
                SWT.NONE,
                newSinglePositionLabel,
                new String[0],
                new WritableValue("", String.class), newSinglePositionValidator, NEW_SINGLE_POSITION_BINDING); //$NON-NLS-1$
        newSinglePositionText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (positionTextModified
                    && newSinglePositionValidator
                        .validate(newSinglePositionText.getText()) == Status.OK_STATUS) {
                    BusyIndicator.showWhile(PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow().getShell().getDisplay(),
                        new Runnable() {
                            @Override
                            public void run() {
                                initContainersFromPosition();
                            }
                        });
                }
                positionTextModified = false;
            }
        });
        newSinglePositionText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                positionTextModified = true;
                // resultShownValue.setValue(Boolean.FALSE);
                displayPositions(false);
            }
        });
        newSinglePositionText
            .addKeyListener(EnterKeyToNextFieldListener.INSTANCE);
        displayOldCabinetFields(false);
    }

    /**
     * Single assign: show or hide old positions fields
     */
    private void displayOldCabinetFields(boolean displayOld) {
        widgetCreator.showWidget(oldSinglePositionLabel, displayOld);
        widgetCreator.showWidget(oldSinglePositionText, displayOld);
        widgetCreator.showWidget(oldSinglePositionCheckLabel, displayOld);
        widgetCreator.showWidget(oldSinglePositionCheckText, displayOld);
        if (displayOld) {
            newSinglePositionLabel.setText(Messages
                .getString("Cabinet.new.position.label") + ":");
        } else {
            newSinglePositionLabel.setText(Messages
                .getString("Cabinet.position.label") + ":");
            oldSinglePositionCheckText.setText(oldSinglePositionText.getText());
        }
        page.layout(true, true);
    }

    @Override
    protected void createMultipleFields(Composite parent)
        throws ApplicationException {
        Composite fieldsComposite = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        layout.horizontalSpacing = 10;
        fieldsComposite.setLayout(layout);
        toolkit.paintBordersFor(fieldsComposite);
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = SWT.FILL;
        fieldsComposite.setLayoutData(gd);

        productBarcodeValidator = new NonEmptyStringValidator(
            Messages.getString("ScanAssign.productBarcode.validationMsg"));//$NON-NLS-1$
        palletLabelValidator = new NonEmptyStringValidator(
            Messages.getString("ScanAssign.palletLabel.validationMsg"));//$NON-NLS-1$

        palletproductBarcodeText = (BiobankText) createBoundWidgetWithLabel(
            fieldsComposite,
            BiobankText.class,
            SWT.NONE,
            Messages.getString("ScanAssign.productBarcode.label"), //$NON-NLS-1$
            null, currentMultipleContainer,
            ContainerPeer.PRODUCT_BARCODE.getName(), productBarcodeValidator,
            PRODUCT_BARCODE_BINDING);
        palletproductBarcodeText.addKeyListener(textFieldKeyListener);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        palletproductBarcodeText.setLayoutData(gd);
        setFirstControl(palletproductBarcodeText);

        palletproductBarcodeText.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                if (palletproductBarcodeTextModified
                    && productBarcodeValidator.validate(
                        currentMultipleContainer.getProductBarcode()).equals(
                        Status.OK_STATUS)) {
                    validateMultipleValues();
                }
                palletproductBarcodeTextModified = false;
            }
        });
        palletproductBarcodeText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!multipleModificationMode) {
                    palletproductBarcodeTextModified = true;
                    multipleValidationMade.setValue(false);
                }
            }
        });

        palletPositionText = (BiobankText) createBoundWidgetWithLabel(
            fieldsComposite, BiobankText.class, SWT.NONE,
            Messages.getString("ScanAssign.palletLabel.label"), null, //$NON-NLS-1$
            currentMultipleContainer, ContainerPeer.LABEL.getName(),
            palletLabelValidator, LABEL_BINDING);
        palletPositionText.addKeyListener(EnterKeyToNextFieldListener.INSTANCE);
        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        palletPositionText.setLayoutData(gd);
        palletPositionText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                if (palletPositionTextModified) {
                    validateMultipleValues();
                }
                palletPositionTextModified = false;
            }
        });
        palletPositionText.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                if (!multipleModificationMode) {
                    palletPositionTextModified = true;
                    multipleValidationMade.setValue(false);
                }
            }
        });

        createPalletTypesViewer(fieldsComposite);

        createPlateToScanField(fieldsComposite);

        createScanButton(parent);
    }

    private void resetParentContainers() {
        parentContainers = null;
        // if (thirdParentWidget != null)
        // thirdParentWidget.setSelection(null);
        // if (secondParentWidget != null)
        // secondParentWidget.setSelection(null);
    }

    /**
     * single assign: search possible parents from the position text
     */
    protected void initContainersFromPosition() {
        resetParentContainers();
        try {
            parentContainers = null;
            SiteWrapper currentSite = SessionManager.getUser()
                .getCurrentWorkingSite();
            String fullLabel = newSinglePositionText.getText();
            List<ContainerWrapper> foundContainers = new ArrayList<ContainerWrapper>();
            int removeSize = 2; // FIXME we are assuming that the specimen
                                // position will be only of size 2 !
            List<String> labelsTested = new ArrayList<String>();
            while (removeSize < 5) { // we are assuming that the bin
                                     // position won't be bigger than 3 !
                int cutIndex = fullLabel.length() - removeSize;
                String binLabel = fullLabel.substring(0, cutIndex);
                labelsTested.add(binLabel);
                for (ContainerWrapper cont : ContainerWrapper
                    .getContainersInSite(appService, currentSite, binLabel)) {
                    boolean canContainSamples = cont.getContainerType()
                        .getSpecimenTypeCollection() != null
                        && cont.getContainerType().getSpecimenTypeCollection()
                            .size() > 0;
                    if (canContainSamples) {
                        RowColPos rcp = null;
                        try {
                            rcp = ContainerLabelingSchemeWrapper
                                .getRowColFromPositionString(appService,
                                    fullLabel.substring(cutIndex), cont
                                        .getContainerType()
                                        .getChildLabelingSchemeId(), cont
                                        .getContainerType().getRowCapacity(),
                                    cont.getContainerType().getColCapacity());
                        } catch (Exception ex) {
                            // the test failed
                            continue;
                        }
                        if (rcp != null) // the full position string is valid:
                            foundContainers.add(cont);
                    }
                }
                removeSize++;
            }
            if (foundContainers.size() == 1) {
                initContainersParents(foundContainers.get(0));
            } else if (foundContainers.size() == 0) {
                String errorMsg = Messages.getString(
                    "Cabinet.activitylog.checkParent.error.found", //$NON-NLS-1$
                    getNotFoundLabelMessage(fullLabel, labelsTested));
                BiobankPlugin
                    .openError("Check position and specimen", errorMsg); //$NON-NLS-1$
                appendLog(Messages.getString(
                    "Cabinet.activitylog.checkParent.error", errorMsg)); //$NON-NLS-1$
                focusControlInError(newSinglePositionText);
                return;
            } else {
                SelectParentContainerDialog dlg = new SelectParentContainerDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getShell(), foundContainers);
                dlg.open();
                if (dlg.getSelectedContainer() == null) {
                    StringBuffer sb = new StringBuffer();
                    for (ContainerWrapper cont : foundContainers) {
                        sb.append(cont.getFullInfoLabel());
                    }
                    BiobankPlugin.openError("Container problem",
                        "More than one container found mathing the position label: "
                            + sb.toString() + " --- should do something");
                    focusControlInError(newSinglePositionText);
                } else
                    initContainersParents(dlg.getSelectedContainer());
            }
        } catch (Exception ex) {
            BiobankPlugin.openError("Init container from position", ex);
            focusControlInError(newSinglePositionText);
        }
        checkPositionAndSpecimen();
    }

    /**
     * single assign: initialise parents of the single specimen
     */
    private void initContainersParents(ContainerWrapper bottomContainer) {
        // only one cabinet container has been found
        parentContainers = new ArrayList<ContainerWrapper>();
        ContainerWrapper parent = bottomContainer;
        while (parent != null) {
            parentContainers.add(parent);
            parent = parent.getParentContainer();
        }
        // TODO which generic message ?
        // appendLog(Messages.getString(
        //            "Cabinet.activitylog.containers.init", //$NON-NLS-1$
        // thirdParent.getFullInfoLabel(), secondParent.getFullInfoLabel(),
        // firstParent.getFullInfoLabel()));
    }

    private String getNotFoundLabelMessage(String fullLabel,
        List<String> labelsTested) {
        StringBuffer res = new StringBuffer();
        for (int i = 0; i < labelsTested.size(); i++) {
            if (i != 0) {
                res.append(", ");
            }
            String binLabel = labelsTested.get(i);
            res.append(binLabel).append("(")
                .append(fullLabel.replace(binLabel, "")).append(")");
        }
        return res.toString();
    }

    /**
     * Single assign. Check can really add to the position
     */
    protected void checkPositionAndSpecimen() {
        BusyIndicator.showWhile(Display.getDefault(), new Runnable() {
            @Override
            public void run() {
                try {
                    appendLog("----"); //$NON-NLS-1$
                    String positionString = newSinglePositionText.getText();
                    if (parentContainers == null
                        || parentContainers.size() == 0) {
                        // resultShownValue.setValue(Boolean.FALSE);
                        displayPositions(false);
                        return;
                    }
                    appendLog(Messages.getString(
                        "Cabinet.activitylog.checkingPosition", positionString)); //$NON-NLS-1$
                    singleSpecimen.setSpecimenPositionFromString(
                        positionString, parentContainers.get(0));
                    if (singleSpecimen.isPositionFree(parentContainers.get(0))) {
                        singleSpecimen.setParent(parentContainers.get(0));
                        displayPositions(true);
                        // resultShownValue.setValue(Boolean.TRUE);
                        cancelConfirmWidget.setFocus();
                    } else {
                        BiobankPlugin.openError("Position not free", Messages
                            .getString(
                                "Cabinet.checkStatus.error", positionString, //$NON-NLS-1$
                                parentContainers.get(0).getLabel()));
                        appendLog(Messages.getString(
                            "Cabinet.activitylog.checkPosition.error", //$NON-NLS-1$
                            positionString, parentContainers.get(0).getLabel()));
                        focusControlInError(newSinglePositionText);
                        return;
                    }
                    setDirty(true);
                } catch (RemoteConnectFailureException exp) {
                    BiobankPlugin.openRemoteConnectErrorMessage(exp);
                } catch (BiobankCheckException bce) {
                    BiobankPlugin.openError(
                        "Error while checking position", bce); //$NON-NLS-1$
                    appendLog("ERROR: " + bce.getMessage()); //$NON-NLS-1$
                    // resultShownValue.setValue(Boolean.FALSE);
                    focusControlInError(inventoryIdText);
                } catch (Exception e) {
                    BiobankPlugin.openError("Error while checking position", e); //$NON-NLS-1$
                    focusControlInError(newSinglePositionText);
                }
            }
        });
    }

    /**
     * single assign. Display containers
     */
    private void displayPositions(boolean show) {
        widgetCreator.showWidget(secondSingleParentWidget, show);
        widgetCreator.showWidget(secondSingleParentLabel, show);
        widgetCreator.showWidget(thirdSingleParentLabel, show);
        widgetCreator.showWidget(thirdSingleParentWidget, show);
        if (show) {
            if (parentContainers != null && parentContainers.size() >= 3) {
                ContainerWrapper thirdParent = parentContainers.get(2);
                ContainerWrapper secondParent = parentContainers.get(1);
                ContainerWrapper firstParent = parentContainers.get(0);
                thirdSingleParentWidget.setContainerType(thirdParent
                    .getContainerType());
                thirdSingleParentWidget.setSelection(secondParent
                    .getPositionAsRowCol());
                thirdSingleParentLabel.setText(thirdParent.getLabel());
                secondSingleParentWidget.setContainer(secondParent);
                secondSingleParentWidget.setSelection(firstParent
                    .getPositionAsRowCol());
                secondSingleParentLabel.setText(secondParent.getLabel());
            }
        }
        showVisualisation(show);
        page.layout(true, true);
        book.reflow(true);
    }

    @Override
    protected void createContainersVisualisation(Composite parent) {
        createMultipleVisualisation(parent);
        createSingleVisualisation(parent);
    }

    private void createMultipleVisualisation(Composite parent) {
        multipleVisualisation = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(3, false);
        multipleVisualisation.setLayout(layout);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        multipleVisualisation.setLayoutData(gd);

        Composite freezerComposite = toolkit
            .createComposite(multipleVisualisation);
        freezerComposite.setLayout(getNeutralGridLayout());
        GridData gdFreezer = new GridData();
        gdFreezer.horizontalSpan = 3;
        gdFreezer.horizontalAlignment = SWT.RIGHT;
        freezerComposite.setLayoutData(gdFreezer);
        freezerLabel = toolkit.createLabel(freezerComposite, "Freezer"); //$NON-NLS-1$
        freezerLabel.setLayoutData(new GridData());
        freezerWidget = new ContainerDisplayWidget(freezerComposite);
        freezerWidget.initDisplayFromType(true);
        toolkit.adapt(freezerWidget);
        freezerWidget.setDisplaySize(ScanPalletDisplay.PALLET_WIDTH, 100);

        Composite hotelComposite = toolkit
            .createComposite(multipleVisualisation);
        hotelComposite.setLayout(getNeutralGridLayout());
        hotelComposite.setLayoutData(new GridData());
        hotelLabel = toolkit.createLabel(hotelComposite, "Hotel"); //$NON-NLS-1$
        hotelWidget = new ContainerDisplayWidget(hotelComposite);
        hotelWidget.initDisplayFromType(true);
        toolkit.adapt(hotelWidget);
        hotelWidget.setDisplaySize(100,
            ScanPalletDisplay.PALLET_HEIGHT_AND_LEGEND);

        Composite palletComposite = toolkit
            .createComposite(multipleVisualisation);
        palletComposite.setLayout(getNeutralGridLayout());
        palletComposite.setLayoutData(new GridData());
        palletLabel = toolkit.createLabel(palletComposite, "Pallet"); //$NON-NLS-1$
        palletWidget = new ScanPalletWidget(palletComposite,
            UICellStatus.DEFAULT_PALLET_SCAN_ASSIGN_STATUS_LIST);
        toolkit.adapt(palletWidget);
        palletWidget.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                manageDoubleClick(e);
            }
        });
        showOnlyPallet(true);

        createScanTubeAloneButton(multipleVisualisation);
    }

    private void showOnlyPallet(boolean show) {
        freezerLabel.getParent().setVisible(!show);
        ((GridData) freezerLabel.getParent().getLayoutData()).exclude = show;
        hotelLabel.getParent().setVisible(!show);
        ((GridData) hotelLabel.getParent().getLayoutData()).exclude = show;
    }

    private void showOnlyPallet(final boolean show, boolean async) {
        if (async) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    showOnlyPallet(show);
                }
            });
        } else {
            showOnlyPallet(show);
        }
    }

    private void createSingleVisualisation(Composite parent) {
        singleVisualisation = toolkit.createComposite(parent);
        GridLayout layout = new GridLayout(2, false);
        singleVisualisation.setLayout(layout);
        GridData gd = new GridData();
        gd.grabExcessHorizontalSpace = true;
        singleVisualisation.setLayoutData(gd);

        thirdSingleParentLabel = toolkit.createLabel(singleVisualisation, ""); //$NON-NLS-1$
        secondSingleParentLabel = toolkit.createLabel(singleVisualisation, ""); //$NON-NLS-1$

        ContainerTypeWrapper thirdSingleParentType = null;
        ContainerTypeWrapper secondSingleParentType = null;
        thirdSingleParentWidget = new ContainerDisplayWidget(
            singleVisualisation);
        thirdSingleParentWidget.setContainerType(thirdSingleParentType, true);
        toolkit.adapt(thirdSingleParentWidget);
        GridData gdDrawer = new GridData();
        gdDrawer.verticalAlignment = SWT.TOP;
        thirdSingleParentWidget.setLayoutData(gdDrawer);

        secondSingleParentWidget = new ContainerDisplayWidget(
            singleVisualisation);
        secondSingleParentWidget.setContainerType(secondSingleParentType, true);
        toolkit.adapt(secondSingleParentWidget);

        displayPositions(false);
    }

    private GridLayout getNeutralGridLayout() {
        GridLayout layout = new GridLayout(1, false);
        layout.horizontalSpacing = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        return layout;
    }

    private void createPalletTypesViewer(Composite parent)
        throws ApplicationException {
        palletContainerTypes = getPalletContainerTypes();
        palletTypesViewer = widgetCreator.createComboViewer(
            parent,
            Messages.getString("ScanAssign.palletType.label"), //$NON-NLS-1$
            null, null,
            Messages.getString("ScanAssign.palletType.validationMsg"), true,
            PALLET_TYPES_BINDING, new ComboSelectionUpdate() {
                @Override
                public void doSelection(Object selectedObject) {
                    if (!multipleModificationMode) {
                        ContainerTypeWrapper oldContainerType = currentMultipleContainer
                            .getContainerType();
                        currentMultipleContainer
                            .setContainerType((ContainerTypeWrapper) selectedObject);
                        if (oldContainerType != null) {
                            validateMultipleValues();
                        }
                        palletTypesViewer.getCombo().setFocus();
                    }
                }
            }); //$NON-NLS-1$
        if (palletContainerTypes.size() == 1) {
            currentMultipleContainer.setContainerType(palletContainerTypes
                .get(0));
            palletTypesViewer.setSelection(new StructuredSelection(
                palletContainerTypes.get(0)));
        }
    }

    // FIXME also for others like Box81 now !
    private List<ContainerTypeWrapper> getPalletContainerTypes()
        throws ApplicationException {
        List<ContainerTypeWrapper> palletContainerTypes = ContainerTypeWrapper
            .getContainerTypesPallet96(appService,
                currentMultipleContainer.getSite());
        if (palletContainerTypes.size() == 0) {
            BiobankPlugin.openAsyncError(Messages
                .getString("ScanAssign.dialog.noPalletFoundError.title"), //$NON-NLS-1$
                Messages.getString("ScanAssign.dialog.noPalletFoundError.msg" //$NON-NLS-1$
                    ));
        }
        return palletContainerTypes;
    }

    @Override
    protected void disableFields() {
        super.disableFields();

    }

    @Override
    protected boolean fieldsValid() {
        if (singleMode)
            return true;
        IStructuredSelection selection = (IStructuredSelection) palletTypesViewer
            .getSelection();
        return isPlateValid()
            && productBarcodeValidator.validate(
                palletproductBarcodeText.getText()).equals(Status.OK_STATUS)
            && palletLabelValidator.validate(palletPositionText.getText())
                .equals(Status.OK_STATUS) && selection.size() > 0;
    }

    @Override
    protected void saveForm() throws Exception {
        if (singleMode)
            saveSingleSpecimen();
        else
            saveMultipleSpecimens();
        setFinished(false);
    }

    private void saveMultipleSpecimens() throws Exception {
        if (saveEvenIfMissing) {
            if (containerToRemove != null) {
                containerToRemove.delete();
            }
            currentMultipleContainer.persist();
            displayPalletPositionInfo();
            int totalNb = 0;
            StringBuffer sb = new StringBuffer("SPECIMENS ASSIGNED:\n"); //$NON-NLS-1$
            try {
                Map<RowColPos, PalletCell> cells = getCells();
                for (Entry<RowColPos, PalletCell> entry : cells.entrySet()) {
                    RowColPos rcp = entry.getKey();
                    PalletCell cell = entry.getValue();
                    if (cell != null
                        && (cell.getStatus() == UICellStatus.NEW || cell
                            .getStatus() == UICellStatus.MOVED)) {
                        SpecimenWrapper specimen = cell.getSpecimen();
                        if (specimen != null) {
                            specimen.setPosition(rcp);
                            specimen.setParent(currentMultipleContainer);
                            specimen.persist();
                            String posStr = specimen.getPositionString(true,
                                false);
                            if (posStr == null) {
                                posStr = "none"; //$NON-NLS-1$
                            }
                            computeActivityLogMessage(sb, cell, specimen,
                                posStr);
                            totalNb++;
                        }
                    }
                }
            } catch (Exception ex) {
                setScanHasBeenLauched(false, true);
                throw ex;
            }
            appendLog(sb.toString());
            appendLog(Messages.getString(
                "ScanAssign.activitylog.save.summary", totalNb, //$NON-NLS-1$
                currentMultipleContainer.getLabel(), currentMultipleContainer
                    .getSite().getNameShort()));
            setFinished(false);
        }
    }

    private void computeActivityLogMessage(StringBuffer sb, PalletCell cell,
        SpecimenWrapper specimen, String posStr) {
        CollectionEventWrapper visit = specimen.getCollectionEvent();
        sb.append(Messages.getString(
            "ScanAssign.activitylog.specimen.assigned", //$NON-NLS-1$
            posStr, currentMultipleContainer.getSite().getNameShort(), cell
                .getValue(), specimen.getSpecimenType().getName(), visit
                .getPatient().getPnumber(), visit.getVisitNumber()));
    }

    private void saveSingleSpecimen() throws Exception {
        singleSpecimen.persist();
    }

    @Override
    protected String getOkMessage() {
        return "Assign position to specimens";
    }

    @Override
    public String getNextOpenedFormID() {
        return ID;
    }

    @Override
    protected ProcessData getProcessData() {
        return new AssignProcessData(currentMultipleContainer);
    }

    protected void focusControlInError(final Control control) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                control.setFocus();
            }
        });
    }

    @Override
    public void reset() throws Exception {
        super.reset();
        resetParentContainers();
        // resultShownValue.setValue(Boolean.FALSE);
        // the 2 following lines are needed. The validator won't update if don't
        // do that (why ?)
        inventoryIdText.setText("**"); //$NON-NLS-1$ 
        inventoryIdText.setText(""); //$NON-NLS-1$
        oldSinglePositionText.setText("");
        oldSinglePositionCheckText.setText(""); //$NON-NLS-1$
        newSinglePositionText.setText(""); //$NON-NLS-1$
        displayOldCabinetFields(false);

        showOnlyPallet(true);
        form.layout(true, true);
        if (!singleMode) {
            palletproductBarcodeText.setFocus();
            setCanLaunchScan(false);
        }

        singleSpecimen.reset(); // reset internal values
        setDirty(false);
        setFocus();
    }

    @Override
    public void reset(boolean resetAll) {
        super.reset(resetAll);
        String productBarcode = ""; //$NON-NLS-1$
        String label = ""; //$NON-NLS-1$
        ContainerTypeWrapper type = null;

        if (!resetAll) { // keep fields values
            productBarcode = palletproductBarcodeText.getText();
            label = palletPositionText.getText();
            type = currentMultipleContainer.getContainerType();
        } else {
            if (palletTypesViewer != null) {
                palletTypesViewer.getCombo().deselectAll();
            }
            setScanHasBeenLauched(false);
            removeRescanMode();
            freezerWidget.setSelection(null);
            hotelWidget.setSelection(null);
            palletWidget.setCells(null);
        }
        setScanHasBeenLauched(false);
        initPalletValues();

        palletproductBarcodeText.setText(productBarcode);
        productBarcodeValidator.validate(productBarcode);
        palletPositionText.setText(label);
        palletLabelValidator.validate(label);
        currentMultipleContainer.setContainerType(type);
        if (resetAll) {
            setDirty(false);
            useNewProductBarcode = false;
        }
    }

    @Override
    protected void setBindings(boolean isSingleMode) {
        setCanLaunchScan(true);
        widgetCreator.setBinding(INVENTORY_ID_BINDING, isSingleMode);
        widgetCreator.setBinding(NEW_SINGLE_POSITION_BINDING, isSingleMode);
        widgetCreator.setBinding(PRODUCT_BARCODE_BINDING, !isSingleMode);
        widgetCreator.setBinding(LABEL_BINDING, !isSingleMode);
        widgetCreator.setBinding(PALLET_TYPES_BINDING, !isSingleMode);
        super.setBindings(isSingleMode);
    }

    @Override
    protected void showSingleComposite(boolean single) {
        widgetCreator.showWidget(multipleVisualisation, !single);
        reset(false);
        widgetCreator.showWidget(singleVisualisation, single);
        super.showSingleComposite(single);
    }

    /**
     * Multiple assign: validate fields values
     */
    protected void validateMultipleValues() {
        // if null, initialisation of all fields is not finished
        if (productBarcodeValidator != null) {
            nextFocusWidget = null;
            multipleModificationMode = true;
            try {
                if (productBarcodeValidator.validate(
                    currentMultipleContainer.getProductBarcode()).equals(
                    Status.OK_STATUS)) {
                    reset(false);
                    boolean canLaunch = true;
                    boolean exists = getExistingPalletFromProductBarcode();
                    if ((!exists || !palletFoundWithProductBarcodeLabel
                        .equals(currentMultipleContainer.getLabel()))
                        && palletLabelValidator.validate(
                            currentMultipleContainer.getLabel()).equals(
                            Status.OK_STATUS)) {
                        canLaunch = checkPallet();
                    }
                    setCanLaunchScan(canLaunch);
                }
            } catch (Exception ex) {
                BiobankPlugin
                    .openError(
                        Messages.getString("ScanAssign.validation.error.title"), ex); //$NON-NLS-1$
                appendLog(Messages.getString("ScanAssign.activitylog.error", //$NON-NLS-1$
                    ex.getMessage()));
                if (ex instanceof ContainerLabelSearchException) {
                    nextFocusWidget = palletPositionText;
                }
                setCanLaunchScan(false);
            }
            if (nextFocusWidget != null) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        nextFocusWidget.setFocus();
                    }
                });
            }
            multipleModificationMode = false;
            multipleValidationMade.setValue(true);
        }
    }

    /**
     * @return true if a pallet already exists with this product barcode
     */
    private boolean getExistingPalletFromProductBarcode() throws Exception {
        ContainerWrapper palletFoundWithProductBarcode = null;
        palletFoundWithProductBarcodeLabel = null;
        palletFoundWithProductBarcode = ContainerWrapper
            .getContainerWithProductBarcodeInSite(appService,
                currentMultipleContainer.getSite(),
                currentMultipleContainer.getProductBarcode());
        if (palletFoundWithProductBarcode == null) {
            // no pallet found with this barcode
            setTypes(palletContainerTypes, true);
            palletTypesViewer.getCombo().setEnabled(true);
            return false;
        } else {
            // a pallet has been found
            palletFoundWithProductBarcodeLabel = palletFoundWithProductBarcode
                .getLabel();
            String currentLabel = palletPositionText.getText();
            currentMultipleContainer
                .initObjectWith(palletFoundWithProductBarcode);
            currentMultipleContainer.reset();
            palletPositionText.selectAll();
            palletLabelValidator.validate(palletPositionText.getText());
            palletTypesViewer.getCombo().setEnabled(false);
            palletTypesViewer.setSelection(new StructuredSelection(
                palletFoundWithProductBarcode.getContainerType()));
            appendLog(Messages.getString(
                "ScanAssign.activitylog.pallet.productBarcode.exists",
                currentMultipleContainer.getProductBarcode(),
                palletFoundWithProductBarcode.getLabel(),
                currentMultipleContainer.getSite().getNameShort(),
                palletFoundWithProductBarcode.getContainerType().getName()));
            if (!currentLabel.isEmpty()
                && !currentLabel.equals(palletFoundWithProductBarcodeLabel)) {
                currentMultipleContainer.setLabel(currentLabel);
                return false; // we still want to check the new label
            }
            return true;
        }
    }

    private void setTypes(List<ContainerTypeWrapper> types,
        boolean keepCurrentSelection) {
        IStructuredSelection selection = null;
        if (keepCurrentSelection) {
            selection = (IStructuredSelection) palletTypesViewer.getSelection();
        }
        palletTypesViewer.setInput(types);
        if (selection != null) {
            palletTypesViewer.setSelection(selection);
        }
    }

    /**
     * From the pallet product barcode, get existing information from database
     * and set the position. Set only the position if the product barcode
     * doesn't yet exist
     */
    private boolean checkPallet() throws Exception {
        boolean canContinue = true;
        boolean needToCheckPosition = true;
        ContainerTypeWrapper type = currentMultipleContainer.getContainerType();
        if (palletFoundWithProductBarcodeLabel != null) {
            // a pallet with this product barcode already exists in the
            // database.
            appendLog(Messages.getString(
                "ScanAssign.activitylog.pallet.checkLabelForProductBarcode", //$NON-NLS-1$
                currentMultipleContainer.getLabel(),
                currentMultipleContainer.getProductBarcode(),
                currentMultipleContainer.getSite().getNameShort()));
            // need to compare with this value, in case the container has
            // been copied to the current pallet
            if (palletFoundWithProductBarcodeLabel
                .equals(currentMultipleContainer.getLabel())) {
                // The position already contains this pallet. Don't need to
                // check it. Need to use exact same retrieved wrappedObject.
                // currentPalletWrapper
                // .initObjectWith(palletFoundWithProductBarcode);
                // currentPalletWrapper.reset();
                needToCheckPosition = false;
            } else {
                canContinue = openDialogPalletMoved();
                if (canContinue) {
                    // Move the pallet.
                    type = currentMultipleContainer.getContainerType();
                    appendLog(Messages.getString(
                        "ScanAssign.activitylog.pallet.moveInfo", //$NON-NLS-1$
                        currentMultipleContainer.getProductBarcode(),
                        palletFoundWithProductBarcodeLabel,
                        currentMultipleContainer.getLabel()));
                } else {
                    return false;
                }
            }
            if (type != null) {
                appendLog(Messages.getString(
                    "ScanAssign.activitylog.pallet.typeUsed", //$NON-NLS-1$
                    type.getName()));
            }
        }
        if (needToCheckPosition) {
            canContinue = checkAndSetPosition(type);
        }
        return canContinue;
    }

    private boolean openDialogPalletMoved() {
        return MessageDialog.openConfirm(PlatformUI.getWorkbench()
            .getActiveWorkbenchWindow().getShell(),
            "Pallet product barcode", //$NON-NLS-1$
            Messages.getString(
                "ScanAssign.dialog.checkPallet.otherPosition", //$NON-NLS-1$
                palletFoundWithProductBarcodeLabel,
                currentMultipleContainer.getLabel()));
    }

    /**
     * Check if position is available and set the ContainerPosition if it is
     * free
     * 
     * @return true if was able to create the ContainerPosition
     */
    private boolean checkAndSetPosition(ContainerTypeWrapper typeFixed)
        throws Exception {
        containerToRemove = null;
        List<ContainerTypeWrapper> palletTypes = palletContainerTypes;
        if (typeFixed != null) {
            palletTypes = Arrays.asList(typeFixed);
        }
        // search for containers at this position, with type in one of the type
        // listed
        List<ContainerWrapper> containersAtPosition;
        if (currentMultipleContainer.getSite() == null)
            containersAtPosition = new ArrayList<ContainerWrapper>();
        else
            containersAtPosition = currentMultipleContainer
                .getContainersWithSameLabelWithType(palletContainerTypes);
        String palletLabel = currentMultipleContainer.getLabel();
        if (containersAtPosition.size() == 0) {
            currentMultipleContainer.setPositionAndParentFromLabel(palletLabel,
                palletTypes);
            palletTypes = palletContainerTypes;
            typeFixed = null;
        } else if (containersAtPosition.size() == 1) {
            // One container found
            ContainerWrapper containerAtPosition = containersAtPosition.get(0);
            String barcode = containerAtPosition.getProductBarcode();
            if ((barcode != null && !barcode.isEmpty())
                || containerAtPosition.hasSpecimens()) {
                // Position already physically used
                boolean ok = openDialogPositionUsed(barcode);
                if (!ok) {
                    appendLog(Messages
                        .getString(
                            "ScanAssign.activitylog.pallet.positionUsedMsg", barcode, //$NON-NLS-1$
                            currentMultipleContainer.getLabel(),
                            currentMultipleContainer.getSite().getNameShort()));
                    return false;
                }
            }
            if (useNewProductBarcode) {
                // Position exists but no product barcode set before
                appendLog(Messages
                    .getString(
                        "ScanAssign.activitylog.pallet.positionUsedWithNoProductBarcode",
                        palletLabel, containerAtPosition.getContainerType()
                            .getName(), currentMultipleContainer
                            .getProductBarcode()));
            } else {
                // Position initialised but not physically used
                appendLog(Messages.getString(
                    "ScanAssign.activitylog.pallet.positionInitialized",
                    palletLabel, containerAtPosition.getContainerType()
                        .getName()));
            }

            palletTypes = Arrays.asList(containerAtPosition.getContainerType());
            typeFixed = containerAtPosition.getContainerType();
            if (palletFoundWithProductBarcodeLabel != null) {
                containerToRemove = containerAtPosition;
                // pallet already exists. Need to remove the initialisation to
                // replace it.
                currentMultipleContainer.setParent(containerAtPosition
                    .getParentContainer());
                currentMultipleContainer.setPosition(containerAtPosition
                    .getPosition());
            } else {
                // new pallet or only new product barcode. Can use the
                // initialised one
                String productBarcode = currentMultipleContainer
                    .getProductBarcode();
                currentMultipleContainer.initObjectWith(containerAtPosition);
                currentMultipleContainer.reset();
                currentMultipleContainer.setProductBarcode(productBarcode);
            }
        } else {
            BiobankPlugin.openError("Check position",
                "Found more than one pallet with position " + palletLabel);
            nextFocusWidget = palletPositionText;
            return false;
        }
        ContainerTypeWrapper oldSelection = currentMultipleContainer
            .getContainerType();
        palletTypesViewer.setInput(palletTypes);
        if (oldSelection != null) {
            palletTypesViewer
                .setSelection(new StructuredSelection(oldSelection));
        }
        if (typeFixed != null) {
            palletTypesViewer.setSelection(new StructuredSelection(typeFixed));
        }
        if (palletTypes.size() == 1) {
            palletTypesViewer.setSelection(new StructuredSelection(palletTypes
                .get(0)));
        }
        palletTypesViewer.getCombo().setEnabled(typeFixed == null);
        return true;
    }

    private boolean openDialogPositionUsed(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            // Position already use but the barcode was not set.
            if (!useNewProductBarcode) {
                useNewProductBarcode = MessageDialog
                    .openQuestion(
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                            .getShell(),
                        Messages
                            .getString("ScanAssign.dialog.positionUsed.noBarcode.title"),
                        Messages
                            .getString("ScanAssign.dialog.positionUsed.noBarcode.question"));
            }
            return useNewProductBarcode;
        } else {
            // Position already use with a different barcode
            BiobankPlugin
                .openError(Messages
                    .getString("ScanAssign.dialog.positionUsed.error.title"), //$NON-NLS-1$
                    Messages.getString(
                        "ScanAssign.dialog.positionUsed.error.msg", barcode,
                        currentMultipleContainer.getSite().getNameShort())); //$NON-NLS-1$
            nextFocusWidget = palletPositionText;
            return false;
        }
    }

    protected void manageDoubleClick(MouseEvent e) {
        if (isScanTubeAloneMode()) {
            scanTubeAlone(e);
        } else {
            PalletCell cell = (PalletCell) ((ScanPalletWidget) e.widget)
                .getObjectAtCoordinates(e.x, e.y);
            if (cell != null) {
                switch (cell.getStatus()) {
                case ERROR:
                    // do something ?
                    break;
                case MISSING:
                    SessionManager.openViewForm(cell.getExpectedSpecimen());
                    break;
                }
            }
        }
    }

    @Override
    protected boolean canScanTubeAlone(PalletCell cell) {
        return super.canScanTubeAlone(cell)
            || cell.getStatus() == UICellStatus.MISSING;
    }

    @Override
    protected void launchScanAndProcessResult() {
        super.launchScanAndProcessResult();
        page.layout(true, true);
        book.reflow(true);
        cancelConfirmWidget.setFocus();
    }

    @Override
    protected void beforeScanThreadStart() {
        showOnlyPallet(false, false);
        currentMultipleContainer.setSite(SessionManager.getUser()
            .getCurrentWorkingSite());
        currentMultipleContainer
            .setContainerType((ContainerTypeWrapper) ((IStructuredSelection) palletTypesViewer
                .getSelection()).getFirstElement());
        isFakeScanLinkedOnly = fakeScanLinkedOnlyButton != null
            && fakeScanLinkedOnlyButton.getSelection();
    }

    @Override
    protected void afterScanAndProcess(Integer rowOnly) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                cancelConfirmWidget.setFocus();
                displayPalletPositions();
                palletWidget.setCells(getCells());
                setDirty(true);
                setRescanMode();
                page.layout(true, true);
                form.reflow(true);
            }
        });
    }

    protected void displayPalletPositions() {
        if (currentMultipleContainer.hasParentContainer()) {
            ContainerWrapper hotelContainer = currentMultipleContainer
                .getParentContainer();
            ContainerWrapper freezerContainer = hotelContainer
                .getParentContainer();

            if (freezerContainer != null) {
                freezerLabel.setText(freezerContainer.getFullInfoLabel());
                freezerWidget.setContainerType(freezerContainer
                    .getContainerType());
                freezerWidget
                    .setSelection(hotelContainer.getPositionAsRowCol());
                freezerWidget.redraw();
            }

            hotelLabel.setText(hotelContainer.getFullInfoLabel());
            hotelWidget.setContainerType(hotelContainer.getContainerType());
            hotelWidget.setSelection(currentMultipleContainer
                .getPositionAsRowCol());
            hotelWidget.redraw();

            palletLabel.setText(currentMultipleContainer.getLabel());
        }
    }

    @Override
    protected Map<RowColPos, PalletCell> getFakeScanCells() throws Exception {
        if (palletFoundWithProductBarcodeLabel != null) {
            Map<RowColPos, PalletCell> palletScanned = new HashMap<RowColPos, PalletCell>();
            for (RowColPos pos : currentMultipleContainer.getSpecimens()
                .keySet()) {
                if (pos.row != 0 && pos.col != 2) {
                    palletScanned.put(pos,
                        new PalletCell(new ScanCell(pos.row, pos.col,
                            currentMultipleContainer.getSpecimens().get(pos)
                                .getInventoryId())));
                }
            }
            return palletScanned;
        } else {
            if (isFakeScanLinkedOnly) {
                return PalletCell.getRandomSpecimensNotAssigned(appService,
                    currentMultipleContainer.getSite().getId());
            }
            return PalletCell.getRandomSpecimensAlreadyAssigned(appService,
                currentMultipleContainer.getSite().getId());
        }
    }

    @Override
    protected void doBeforeSave() throws Exception {
        saveEvenIfMissing = true;
        if (currentScanState == UICellStatus.MISSING) {
            boolean save = BiobankPlugin.openConfirm(
                Messages.getString("ScanAssign.dialog.reallySave.title"), //$NON-NLS-1$
                Messages.getString("ScanAssign.dialog.saveWithMissing.msg")); //$NON-NLS-1$
            if (!save) {
                setDirty(true);
                saveEvenIfMissing = false;
            }
        }
    }

    @Override
    protected void createFakeOptions(Composite fieldsComposite) {
        Composite comp = toolkit.createComposite(fieldsComposite);
        comp.setLayout(new GridLayout());
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        comp.setLayoutData(gd);
        fakeScanLinkedOnlyButton = toolkit.createButton(comp,
            "Select linked only specimens", SWT.RADIO); //$NON-NLS-1$
        fakeScanLinkedOnlyButton.setSelection(true);
        toolkit.createButton(comp,
            "Select linked and assigned specimens", SWT.RADIO); //$NON-NLS-1$
    }

    private void displayPalletPositionInfo() {
        String productBarcode = currentMultipleContainer.getProductBarcode();
        String containerType = currentMultipleContainer.getContainerType()
            .getName();
        String palletLabel = currentMultipleContainer.getLabel();
        String siteName = currentMultipleContainer.getSite().getNameShort();
        if (palletFoundWithProductBarcodeLabel == null)
            appendLog(Messages.getString("ScanAssign.activitylog.pallet.added", //$NON-NLS-1$
                productBarcode, containerType, palletLabel, siteName));
        else if (!palletLabel.equals(palletFoundWithProductBarcodeLabel))
            appendLog(Messages.getString(
                "ScanAssign.activitylog.pallet.moved", //$NON-NLS-1$
                productBarcode, containerType,
                palletFoundWithProductBarcodeLabel, palletLabel, siteName));
    }
}
