package edu.ualberta.med.biobank.server.reports;

import edu.ualberta.med.biobank.common.reports.BiobankReport;
import edu.ualberta.med.biobank.common.util.AbstractRowPostProcess;
import edu.ualberta.med.biobank.common.util.DateRangeRowPostProcess;

public class PsByStudyImpl extends AbstractReport {

    private static final String QUERY =
        "select pv.clinicShipmentPatient.patient.study.nameShort,"
            + " year(pv.dateProcessed), "
            + GROUPBY_DATE
            + "(pv.dateProcessed), "
            + "count(distinct pv.clinicShipmentPatient.patient) from edu.ualberta.med.biobank.model.PatientVisit pv"
            + " where pv.dateProcessed between ? and ? and pv.clinicShipmentPatient.clinicShipment.site "
            + SITE_OPERATOR
            + SITE_ID
            + " group by pv.clinicShipmentPatient.patient.study.nameShort, year(pv.dateProcessed), "
            + GROUPBY_DATE + "(pv.dateProcessed)";

    private DateRangeRowPostProcess dateRangePostProcess;

    public PsByStudyImpl(BiobankReport report) {
        super(QUERY, report);
        dateRangePostProcess =
            new DateRangeRowPostProcess(report.getGroupBy().equals("Year"), 1);
    }

    @Override
    public AbstractRowPostProcess getRowPostProcess() {
        return dateRangePostProcess;
    }

}