package edu.ualberta.med.biobank.model;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import edu.ualberta.med.biobank.common.action.eventattr.EventAttrTypeEnum;

public class EventAttrCustom {

    public static final String VALUE_BIND_STRING = "value"; //$NON-NLS-1$
    public static final String VALUE_MULTIPLE_SEPARATOR = ";"; //$NON-NLS-1$

    private Boolean isDefault;
    private Integer studyEventAttrId;
    private String label;
    private EventAttrTypeEnum type;
    private String[] allowedValues;
    private String value;
    private String origValue;
    private GlobalEventAttr globalEventAttr;

    protected PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(
            this);

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        propertyChangeSupport.firePropertyChange("isDefault", this.isDefault, //$NON-NLS-1$
            this.isDefault = isDefault);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        propertyChangeSupport.firePropertyChange("label", this.label, //$NON-NLS-1$
            this.label = label);
    }

    public EventAttrTypeEnum getType() {
        return type;
    }

    public void setType(EventAttrTypeEnum type) {
        propertyChangeSupport.firePropertyChange("type", this.type, //$NON-NLS-1$
            this.type = type);
    }

    public void setType(String typeName) {
        setType(EventAttrTypeEnum.getEventAttrType(typeName));
    }

    public String[] getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(String[] allowedValues) {
        propertyChangeSupport.firePropertyChange("allowedValues", //$NON-NLS-1$
            this.allowedValues, this.allowedValues = allowedValues);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        propertyChangeSupport.firePropertyChange("value", this.value, //$NON-NLS-1$
            this.value = value);
    }

    public String getOrigValue() {
        return origValue;
    }

    public void setOrigValue(String value) {
        propertyChangeSupport.firePropertyChange("value", this.value, //$NON-NLS-1$
            this.origValue = value);
    }

    public void addPropertyChangeListener(String propertyName,
        PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(propertyName, listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    public Integer getStudyEventAttrId() {
        return studyEventAttrId;
    }

    public void setStudyEventAttrId(Integer studyEventAttrId) {
        this.studyEventAttrId = studyEventAttrId;
    }

    public void setGlobalEventAttr(GlobalEventAttr globalEventAttr) {
        this.globalEventAttr = globalEventAttr;
    }

    public Integer getGlobalEventAttrId() {
        return globalEventAttr.getId();
    }

};