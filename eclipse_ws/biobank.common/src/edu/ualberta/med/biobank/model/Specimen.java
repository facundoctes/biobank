package edu.ualberta.med.biobank.model;

import java.util.Date;
import java.util.HashSet;
import java.util.Collection;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import org.hibernate.annotations.Cascade;

@Entity
@Table(name = "SPECIMEN")
public class Specimen extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private String inventoryId;
    private Double quantity;
    private Date createdAt;
    private Specimen topSpecimen;
    private CollectionEvent collectionEvent;
    private Center currentCenter;
    private Collection<DispatchSpecimen> dispatchSpecimenCollection =
        new HashSet<DispatchSpecimen>(0);
    private CollectionEvent originalCollectionEvent;
    private SpecimenType specimenType;
    private SpecimenPosition specimenPosition;
    private Collection<Specimen> childSpecimenCollection =
        new HashSet<Specimen>(0);
    private Collection<Comment> commentCollection = new HashSet<Comment>(0);
    private Collection<RequestSpecimen> requestSpecimenCollection =
        new HashSet<RequestSpecimen>(0);
    private OriginInfo originInfo;
    private ActivityStatus activityStatus;
    private ProcessingEvent processingEvent;
    private Specimen parentSpecimen;

    @Column(name = "INVENTORY_ID", unique = true, nullable = false, length = 100)
    public String getInventoryId() {
        return this.inventoryId;
    }

    public void setInventoryId(String inventoryId) {
        this.inventoryId = inventoryId;
    }

    @Column(name = "QUANTITY")
    public Double getQuantity() {
        return this.quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    @Column(name = "CREATED_AT", nullable = false)
    public Date getCreatedAt() {
        return this.createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TOP_SPECIMEN_ID")
    public Specimen getTopSpecimen() {
        return this.topSpecimen;
    }

    public void setTopSpecimen(Specimen topSpecimen) {
        this.topSpecimen = topSpecimen;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COLLECTION_EVENT_ID", nullable = false)
    public CollectionEvent getCollectionEvent() {
        return this.collectionEvent;
    }

    public void setCollectionEvent(CollectionEvent collectionEvent) {
        this.collectionEvent = collectionEvent;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CURRENT_CENTER_ID")
    public Center getCurrentCenter() {
        return this.currentCenter;
    }

    public void setCurrentCenter(Center currentCenter) {
        this.currentCenter = currentCenter;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "specimen")
    public Collection<DispatchSpecimen> getDispatchSpecimenCollection() {
        return this.dispatchSpecimenCollection;
    }

    public void setDispatchSpecimenCollection(
        Collection<DispatchSpecimen> dispatchSpecimenCollection) {
        this.dispatchSpecimenCollection = dispatchSpecimenCollection;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORIGINAL_COLLECTION_EVENT_ID")
    public CollectionEvent getOriginalCollectionEvent() {
        return this.originalCollectionEvent;
    }

    public void setOriginalCollectionEvent(
        CollectionEvent originalCollectionEvent) {
        this.originalCollectionEvent = originalCollectionEvent;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPECIMEN_TYPE_ID", nullable = false)
    public SpecimenType getSpecimenType() {
        return this.specimenType;
    }

    public void setSpecimenType(SpecimenType specimenType) {
        this.specimenType = specimenType;
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY, mappedBy = "specimen")
    public SpecimenPosition getSpecimenPosition() {
        return this.specimenPosition;
    }

    public void setSpecimenPosition(SpecimenPosition specimenPosition) {
        this.specimenPosition = specimenPosition;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "parentSpecimen")
    @Cascade({ org.hibernate.annotations.CascadeType.SAVE_UPDATE })
    public Collection<Specimen> getChildSpecimenCollection() {
        return this.childSpecimenCollection;
    }

    public void setChildSpecimenCollection(
        Collection<Specimen> childSpecimenCollection) {
        this.childSpecimenCollection = childSpecimenCollection;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "SPECIMEN_COMMENT",
        joinColumns = { @JoinColumn(name = "SPECIMEN_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "COMMENT_ID", unique = true, nullable = false, updatable = false) })
    public Collection<Comment> getCommentCollection() {
        return this.commentCollection;
    }

    public void setCommentCollection(Collection<Comment> commentCollection) {
        this.commentCollection = commentCollection;
    }

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPECIMEN_ID", updatable = false)
    public Collection<RequestSpecimen> getRequestSpecimenCollection() {
        return this.requestSpecimenCollection;
    }

    public void setRequestSpecimenCollection(
        Collection<RequestSpecimen> requestSpecimenCollection) {
        this.requestSpecimenCollection = requestSpecimenCollection;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORIGIN_INFO_ID", nullable = false)
    public OriginInfo getOriginInfo() {
        return this.originInfo;
    }

    public void setOriginInfo(OriginInfo originInfo) {
        this.originInfo = originInfo;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTIVITY_STATUS_ID", nullable = false)
    public ActivityStatus getActivityStatus() {
        return this.activityStatus;
    }

    public void setActivityStatus(ActivityStatus activityStatus) {
        this.activityStatus = activityStatus;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESSING_EVENT_ID")
    public ProcessingEvent getProcessingEvent() {
        return this.processingEvent;
    }

    public void setProcessingEvent(ProcessingEvent processingEvent) {
        this.processingEvent = processingEvent;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PARENT_SPECIMEN_ID")
    public Specimen getParentSpecimen() {
        return this.parentSpecimen;
    }

    public void setParentSpecimen(Specimen parentSpecimen) {
        this.parentSpecimen = parentSpecimen;
    }
}
