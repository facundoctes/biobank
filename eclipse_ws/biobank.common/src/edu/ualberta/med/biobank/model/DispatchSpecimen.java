package edu.ualberta.med.biobank.model;

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
import javax.persistence.Table;

@Entity
@Table(name = "DISPATCH_SPECIMEN")
public class DispatchSpecimen extends AbstractBiobankModel {
    private static final long serialVersionUID = 1L;

    private Integer state;
    private Dispatch dispatch;
    private Specimen specimen;
    private Collection<Comment> commentCollection = new HashSet<Comment>(0);

    @Column(name = "STATE")
    public Integer getState() {
        return this.state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DISPATCH_ID", nullable = false)
    public Dispatch getDispatch() {
        return this.dispatch;
    }

    public void setDispatch(Dispatch dispatch) {
        this.dispatch = dispatch;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SPECIMEN_ID", nullable = false)
    public Specimen getSpecimen() {
        return this.specimen;
    }

    public void setSpecimen(Specimen specimen) {
        this.specimen = specimen;
    }

    @ManyToMany(cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JoinTable(name = "DISPATCH_SPECIMEN_COMMENT",
        joinColumns = { @JoinColumn(name = "DISPATCH_SPECIMEN_ID", nullable = false, updatable = false) },
        inverseJoinColumns = { @JoinColumn(name = "COMMENT_ID", unique = true, nullable = false, updatable = false) })
    public Collection<Comment> getCommentCollection() {
        return this.commentCollection;
    }

    public void setCommentCollection(Collection<Comment> commentCollection) {
        this.commentCollection = commentCollection;
    }
}
