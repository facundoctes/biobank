package edu.ualberta.med.biobank.model;

import java.util.HashSet;
import java.util.Collection;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

/**
 * CollectionEvent generated by hbm2java
 */
@Entity
@Table(name = "COLLECTION_EVENT")
public class CollectionEvent extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private Integer visitNumber;
    private Collection<Specimen> allSpecimenCollection = new HashSet<Specimen>(
        0);
    private Patient patient;
    private ActivityStatus activityStatus;
    private Collection<EventAttr> eventAttrCollection = new HashSet<EventAttr>(
        0);
    private Collection<Comment> commentCollection = new HashSet<Comment>(0);
    private Collection<Specimen> originalSpecimenCollection =
        new HashSet<Specimen>(0);

    @Column(name = "VISIT_NUMBER", nullable = false)
    public Integer getVisitNumber() {
        return this.visitNumber;
    }

    public void setVisitNumber(Integer visitNumber) {
        this.visitNumber = visitNumber;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "collectionEvent")
    @Cascade({ CascadeType.SAVE_UPDATE })
    public Collection<Specimen> getAllSpecimenCollection() {
        return this.allSpecimenCollection;
    }

    public void setAllSpecimenCollection(
        Collection<Specimen> allSpecimenCollection) {
        this.allSpecimenCollection = allSpecimenCollection;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PATIENT_ID", nullable = false)
    public Patient getPatient() {
        return this.patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_STATUS_ID", nullable = false)
    public ActivityStatus getActivityStatus() {
        return this.activityStatus;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    @OneToMany(cascade = javax.persistence.CascadeType.REMOVE, fetch = FetchType.LAZY, mappedBy = "collectionEvent")
    @Cascade({ CascadeType.SAVE_UPDATE })
    public Collection<EventAttr> getEventAttrCollection() {
        return this.eventAttrCollection;
    }

    public void setEventAttrCollection(Collection<EventAttr> eventAttrCollection) {
        this.eventAttrCollection = eventAttrCollection;
    }

    @ManyToMany(cascade = javax.persistence.CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(name = "COLLECTION_EVENT_COMMENT",
        joinColumns = { @JoinColumn(name = "COLLECTION_EVENT_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "COMMENT_ID", unique = true, nullable = false, updatable = false) })
    public Collection<Comment> getCommentCollection() {
        return this.commentCollection;
    }

    public void setCommentCollection(Collection<Comment> commentCollection) {
        this.commentCollection = commentCollection;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "originalCollectionEvent")
    public Collection<Specimen> getOriginalSpecimenCollection() {
        return this.originalSpecimenCollection;
    }

    public void setOriginalSpecimenCollection(
        Collection<Specimen> originalSpecimenCollection) {
        this.originalSpecimenCollection = originalSpecimenCollection;
    }
}
