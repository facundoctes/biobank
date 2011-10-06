package edu.ualberta.med.biobank.common.action.patient;

import java.util.Date;

import org.hibernate.Session;

import edu.ualberta.med.biobank.common.action.Action;
import edu.ualberta.med.biobank.common.action.ActionException;
import edu.ualberta.med.biobank.common.action.study.GetStudyInfoAction;
import edu.ualberta.med.biobank.model.Patient;
import edu.ualberta.med.biobank.model.User;

public class PatientSaveAction implements Action<Patient> {

    private static final long serialVersionUID = 1L;

    private Integer patientId;
    private Integer studyId;
    private String pnumber;
    private Date createdAt;

    public PatientSaveAction(Integer patientId, Integer studyId,
        String pnumber, Date createdAt) {
        this.patientId = patientId;
        this.studyId = studyId;
        this.pnumber = pnumber;
        this.createdAt = createdAt;
    }

    @Override
    public boolean isAllowed(User user, Session session) {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public Patient doAction(Session session) throws ActionException {
        Patient patientToSave;
        if (patientId == null) {
            patientToSave = new Patient();
        } else {
            // retrieve original patient
            PatientInfo pinfo = new GetPatientInfoAction(patientId)
                .doAction(session);
            patientToSave = pinfo.patient;
        }
        patientToSave.setPnumber(pnumber);
        patientToSave.setCreatedAt(createdAt);
        patientToSave.setStudy(new GetStudyInfoAction(studyId)
            .doAction(session));

        session.saveOrUpdate(patientToSave);

        return patientToSave;
    }
}