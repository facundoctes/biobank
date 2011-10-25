package edu.ualberta.med.biobank.test.action.helper;

import java.util.HashMap;
import java.util.Map;

import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventSaveAction;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventSaveAction.SaveCEventAttrInfo;
import edu.ualberta.med.biobank.common.action.collectionEvent.CollectionEventSaveAction.SaveCEventSpecimenInfo;
import edu.ualberta.med.biobank.common.wrappers.EventAttrTypeEnum;
import edu.ualberta.med.biobank.test.Utils;

public class CollectionEventHelper extends Helper {

    public static SaveCEventSpecimenInfo createSaveCEventSpecimenInfoRandom(
        Integer specimenTypeId, Integer userId) {
        SaveCEventSpecimenInfo info = new SaveCEventSpecimenInfo();
        if (userId != null)
            info.comments = Utils.getRandomCommentInfos(userId);
        info.inventoryId = Utils.getRandomString(8, 12);
        info.quantity = r.nextDouble();
        info.specimenTypeId = specimenTypeId;
        info.statusId = 1;
        info.timeDrawn = Utils.getRandomDate();
        return info;
    }

    public static Map<String, SaveCEventSpecimenInfo> createSaveCEventSpecimenInfoRandomList(
        int nber, Integer typeId) {
        return createSaveCEventSpecimenInfoRandomList(nber, typeId, null);
    }

    /**
     * @param nber number of specimen info to create
     * @param typeId type of the specimens
     * @param userId user id is used for the comments fields, if none is
     *            provided, then no comments are added
     */
    public static Map<String, SaveCEventSpecimenInfo> createSaveCEventSpecimenInfoRandomList(
        int nber, Integer typeId, Integer userId) {
        Map<String, SaveCEventSpecimenInfo> specs = new HashMap<String, CollectionEventSaveAction.SaveCEventSpecimenInfo>();
        for (int i = 0; i < nber; i++) {
            SaveCEventSpecimenInfo info = createSaveCEventSpecimenInfoRandom(
                typeId, userId);
            specs.put(info.inventoryId, info);
        }
        return specs;
    }

    public static SaveCEventAttrInfo createSaveCEventAttrInfo(
        Integer studyEventAttrId, EventAttrTypeEnum type, String value) {
        SaveCEventAttrInfo info = new SaveCEventAttrInfo();
        info.studyEventAttrId = studyEventAttrId;
        info.type = type;
        info.value = value;
        return info;
    }
}
