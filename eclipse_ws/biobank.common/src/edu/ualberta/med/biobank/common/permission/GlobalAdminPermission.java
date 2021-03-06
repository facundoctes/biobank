package edu.ualberta.med.biobank.common.permission;

import edu.ualberta.med.biobank.common.action.ActionContext;
import edu.ualberta.med.biobank.model.Membership;
import edu.ualberta.med.biobank.model.User;

public class GlobalAdminPermission implements Permission {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isAllowed(ActionContext context) {
        User user = context.getUser();
        for (Membership membership : user.getAllMemberships()) {
            boolean isEveryPermission = membership.isEveryPermission();
            boolean isGlobal = membership.getDomain().isGlobal();
            if (isEveryPermission && isGlobal) return true;
        }
        return false;
    }
}
