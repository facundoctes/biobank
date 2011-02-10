package edu.ualberta.med.biobank.test.reports;

import edu.ualberta.med.biobank.common.wrappers.AliquotWrapper;
import edu.ualberta.med.biobank.common.wrappers.ContainerWrapper;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.PatientWrapper;
import edu.ualberta.med.biobank.common.wrappers.ProcessingEventWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleStorageWrapper;
import edu.ualberta.med.biobank.common.wrappers.SampleTypeWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.common.wrappers.StudyWrapper;
import edu.ualberta.med.biobank.model.Aliquot;
import edu.ualberta.med.biobank.model.Container;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.SampleStorage;
import gov.nih.nci.system.applicationservice.ApplicationException;
import gov.nih.nci.system.applicationservice.WritableApplicationService;
import gov.nih.nci.system.query.hibernate.HQLCriteria;

import java.util.ArrayList;
import java.util.List;

public class CachedReportDataSource implements ReportDataSource {
    private WritableApplicationService appService;
    private List<SiteWrapper> sites;
    private List<SampleTypeWrapper> sampleTypes;
    private List<SampleStorageWrapper> sampleStorages;
    private List<AliquotWrapper> aliquots;
    private List<ContainerWrapper> containers;
    private List<StudyWrapper> studies;
    private List<ProcessingEventWrapper> patientVisits;
    private List<PatientWrapper> patients;

    public CachedReportDataSource(WritableApplicationService appService) {
        this.appService = appService;
    }

    public List<SiteWrapper> getSites() throws Exception {
        if (sites == null) {
            sites = SiteWrapper.getSites(appService);
        }
        return sites;
    }

    public List<SampleTypeWrapper> getSampleTypes() throws ApplicationException {
        if (sampleTypes == null) {
            sampleTypes = SampleTypeWrapper
                .getAllSampleTypes(appService, false);
        }
        return sampleTypes;
    }

    public List<SampleStorageWrapper> getSampleStorages()
        throws ApplicationException {
        if (sampleStorages == null) {
            HQLCriteria criteria = new HQLCriteria("from "
                + SampleStorage.class.getName());
            List<SampleStorage> rawSampleStorage = appService.query(criteria);

            sampleStorages = new ArrayList<SampleStorageWrapper>();
            for (SampleStorage sampleStorage : rawSampleStorage) {
                sampleStorages.add(new SampleStorageWrapper(appService,
                    sampleStorage));
            }
        }
        return sampleStorages;
    }

    public List<AliquotWrapper> getAliquots() throws ApplicationException {
        if (aliquots == null) {
            HQLCriteria criteria = new HQLCriteria("from "
                + Aliquot.class.getName());
            List<Aliquot> rawAliquots = appService.query(criteria);

            aliquots = new ArrayList<AliquotWrapper>();
            for (Aliquot aliquot : rawAliquots) {
                aliquots.add(new AliquotWrapper(appService, aliquot));
            }
        }
        return aliquots;
    }

    public List<ContainerWrapper> getContainers() throws ApplicationException {
        if (containers == null) {
            HQLCriteria criteria = new HQLCriteria("from "
                + Container.class.getName());
            List<Container> tmp = appService.query(criteria);
            containers = ModelWrapper.wrapModelCollection(appService, tmp,
                ContainerWrapper.class);
        }
        return containers;
    }

    public List<StudyWrapper> getStudies() throws ApplicationException {
        if (studies == null) {
            studies = StudyWrapper.getAllStudies(appService);
        }
        return studies;
    }

    public List<ProcessingEventWrapper> getPatientVisits()
        throws ApplicationException {
        return null;
        // FIXME: patient visits need to be converted
        // if (patientVisits == null) {
        // HQLCriteria criteria = new HQLCriteria("from "
        // + ProcessingEvent.class.getName());
        // List<PatientVisit> rawVisits = appService.query(criteria);
        //
        // patientVisits = new ArrayList<ProcessingEventWrapper>();
        // for (PatientVisit visit : rawVisits) {
        // patientVisits.add(new ProcessingEventWrapper(appService, visit));
        // }
        // }
        // return patientVisits;
    }

    public List<PatientWrapper> getPatients() throws ApplicationException {
        if (patients == null) {
            HQLCriteria criteria = new HQLCriteria("from "
                + Patient.class.getName());
            List<Patient> rawPatients = appService.query(criteria);

            patients = new ArrayList<PatientWrapper>();
            for (Patient patient : rawPatients) {
                patients.add(new PatientWrapper(appService, patient));
            }
        }
        return patients;
    }

    public WritableApplicationService getAppService() {
        return appService;
    }
}
