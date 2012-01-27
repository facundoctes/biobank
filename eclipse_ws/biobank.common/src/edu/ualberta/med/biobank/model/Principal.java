package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * Principal generated by hbm2java
 */
@Entity
@Table(name = "PRINCIPAL")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR",
    discriminatorType = DiscriminatorType.STRING)
public class Principal extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private Collection<Membership> membershipCollection =
        new HashSet<Membership>(0);

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "principal")
    public Collection<Membership> getMembershipCollection() {
        return this.membershipCollection;
    }

    public void setMembershipCollection(
        Collection<Membership> membershipCollection) {
        this.membershipCollection = membershipCollection;
    }
}
