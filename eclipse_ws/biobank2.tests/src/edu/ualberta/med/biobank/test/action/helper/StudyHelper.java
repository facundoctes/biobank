package edu.ualberta.med.biobank.test.action.helper;

import java.util.List;

import edu.ualberta.med.biobank.common.action.activityStatus.ActivityStatusEnum;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction;
import edu.ualberta.med.biobank.common.action.study.StudySaveAction.StudyEventAttrSaveInfo;
import edu.ualberta.med.biobank.server.applicationservice.BiobankApplicationService;
import gov.nih.nci.system.applicationservice.ApplicationException;

public class StudyHelper extends Helper {

    public static Integer createStudy(BiobankApplicationService appService,
        String name, ActivityStatusEnum activityStatus)
        throws ApplicationException {
        StudySaveAction saveStudy = new StudySaveAction();
        saveStudy.setName(name);
        saveStudy.setNameShort(name);
        saveStudy.setActivityStatusId(activityStatus.getId());

        return appService.doAction(saveStudy);
    }

    public static Integer createStudy(BiobankApplicationService appService,
        String name, ActivityStatusEnum activityStatus,
        List<StudyEventAttrSaveInfo> attrInfos) throws ApplicationException {
        StudySaveAction saveStudy = new StudySaveAction();
        saveStudy.setName(name);
        saveStudy.setNameShort(name);
        saveStudy.setActivityStatusId(activityStatus.getId());
        saveStudy.setStudyEventAttrSaveInfo(attrInfos);

        return appService.doAction(saveStudy);
    }

}