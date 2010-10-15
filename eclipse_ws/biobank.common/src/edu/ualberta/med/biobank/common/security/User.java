package edu.ualberta.med.biobank.common.security;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import edu.ualberta.med.biobank.common.util.NotAProxy;
import edu.ualberta.med.biobank.common.wrappers.ModelWrapper;
import edu.ualberta.med.biobank.common.wrappers.SiteWrapper;
import edu.ualberta.med.biobank.model.Site;
import gov.nih.nci.system.applicationservice.WritableApplicationService;

public class User implements Serializable, NotAProxy {
    @Deprecated
    private static final long serialVersionUID = 1L;

    private Long id;

    private String login;
    private String firstName;
    private String lastName;
    private String password;
    private String email;

    private boolean needToChangePassword;

    private List<Group> groups = new ArrayList<Group>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setNeedToChangePassword(boolean needToChangePassword) {
        this.needToChangePassword = needToChangePassword;
    }

    public boolean isNeedToChangePassword() {
        return needToChangePassword;
    }

    public void setGroups(List<Group> groups) {
        this.groups = groups;
    }

    public List<Group> getGroups() {
        return groups;
    }

    public void copy(User user) {
        id = user.getId();
        login = user.getLogin();
        firstName = user.getFirstName();
        lastName = user.getLastName();
        password = user.getPassword();
        email = user.getEmail();
        needToChangePassword = user.isNeedToChangePassword();
        groups = new ArrayList<Group>(user.getGroups());
    }

    public boolean isWebsiteAdministrator() {
        for (Group group : groups) {
            if (group.isWebsiteAdministrator()) {
                return true;
            }
        }
        return false;
    }

    public boolean isSiteAdministrator(SiteWrapper site) {
        Integer id = null;
        if (site != null)
            id = site.getId();
        return isSiteAdministrator(id);
    }

    public boolean isSiteAdministrator(Integer siteId) {
        if (isWebsiteAdministrator()) {
            return true;
        }
        for (Group group : groups) {
            if (group.isSiteAdministrator(siteId)) {
                return true;
            }
        }
        return false;
    }

    public boolean canUpdateSite(SiteWrapper site) {
        Integer id = null;
        if (site != null)
            id = site.getId();
        return canUpdateSite(id);
    }

    public boolean canUpdateSite(Integer siteId) {
        for (Group group : groups) {
            if (group.hasPrivilegeOnObject(Privilege.UPDATE,
                Site.class.getName(), siteId)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPrivilegeOnObject(Privilege privilege, Integer siteId,
        Class<?> clazz) {
        return hasPrivilegeOnObject(privilege, siteId, clazz, null);
    }

    public boolean hasPrivilegeOnObject(Privilege privilege, Integer siteId,
        Class<?> clazz, Integer id) {
        String type;
        boolean canCreateDeleteUpdate = true;
        if (ModelWrapper.class.isAssignableFrom(clazz)) {
            ModelWrapper<?> wrapper = null;
            try {
                Constructor<?> constructor = clazz
                    .getConstructor(WritableApplicationService.class);
                wrapper = (ModelWrapper<?>) constructor
                    .newInstance((WritableApplicationService) null);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                return false;
            }
            type = wrapper.getWrappedClass().getName();
            if (privilege != Privilege.READ && siteId != null)
                canCreateDeleteUpdate = canUpdateSite(siteId);
            canCreateDeleteUpdate = canCreateDeleteUpdate
                && wrapper.checkSpecificAccess(this, siteId);
        } else {
            type = clazz.getName();
        }
        return canCreateDeleteUpdate
            && hasPrivilegeOnObject(privilege, type, id);
    }

    private boolean hasPrivilegeOnObject(Privilege privilege, String type,
        Integer id) {
        for (Group group : groups) {
            if (group.hasPrivilegeOnObject(privilege, type, id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>();
        properties.put("id", id);
        properties.put("login", login);
        properties.put("password", password);
        properties.put("firstName", firstName);
        properties.put("lastName", lastName);
        properties.put("email", email);
        properties.put("groups", groups);
        return properties.toString();
    }
}
