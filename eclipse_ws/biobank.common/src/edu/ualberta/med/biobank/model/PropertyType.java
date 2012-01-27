package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "PROPERTY_TYPE")
public class PropertyType extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private String name;
    private Collection<PropertyModifier> propertyModifierCollection =
        new HashSet<PropertyModifier>(0);

    @Column(name = "NAME")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "PROPERTY_TYPE_ID", updatable = false)
    public Collection<PropertyModifier> getPropertyModifierCollection() {
        return this.propertyModifierCollection;
    }

    public void setPropertyModifierCollection(
        Collection<PropertyModifier> propertyModifierCollection) {
        this.propertyModifierCollection = propertyModifierCollection;
    }
}
