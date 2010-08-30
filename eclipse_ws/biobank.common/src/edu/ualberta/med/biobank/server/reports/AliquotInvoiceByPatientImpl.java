package edu.ualberta.med.biobank.server.reports;

import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.AliquotPosition;

public class AliquotInvoiceByPatientImpl extends AbstractReport {

    private static String QUERY = "Select Alias.inventoryId, Alias.patientVisit.patient.pnumber, Alias.patientVisit.shipment.clinic.name,  Alias.linkDate, Alias.sampleType.name from "
        + Aliquot.class.getName()
        + " as Alias left join Alias.aliquotPosition p where (p is null or p not in (from "
        + AliquotPosition.class.getName()
        + " a where a.container.label like '"
        + SENT_SAMPLES_FREEZER_NAME
        + "')) and Alias.linkDate between ? and ? and Alias.patientVisit.shipment.site "
        + SITE_OPERATOR
        + SITE_ID
        + " ORDER BY Alias.patientVisit.patient.pnumber";

    public AliquotInvoiceByPatientImpl(BiobankReport report) {
        super(QUERY, report);
    }

}
