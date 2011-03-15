package edu.ualberta.med.biobank.importer;

import java.sql.Connection;

import org.apache.log4j.Logger;

import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class Freezer99Importer extends FreezerImporter {

    protected static final Logger logger = Logger
        .getLogger(Freezer99Importer.class.getName());

    protected static String DEFAULT_QUERY = "select patient_visit.date_received, "
        + "patient_visit.date_taken, study_list.study_name_short, "
        + "sample_list.sample_name_short, freezer.*, patient.dec_chr_nr,  "
        + "patient_visit.bb2_pv_id from freezer "
        + "join patient_visit on patient_visit.visit_nr=freezer.visit_nr "
        + "join patient on patient.patient_nr=patient_visit.patient_nr "
        + "join study_list on study_list.study_nr=patient_visit.study_nr "
        + "join sample_list on freezer.sample_nr=sample_list.sample_nr "
        + "where freezer.fnum = ? and freezer.rack= ? "
        + "order by freezer.box, freezer.cell";

    public Freezer99Importer(WritableApplicationService appService,
        Connection con, Configuration configuration, final SiteWrapper site,
        ContainerWrapper container, int bbpdbFreezerNum) throws Exception {
        super(appService, con, configuration, site, container, bbpdbFreezerNum,
            DEFAULT_QUERY);
    }

}