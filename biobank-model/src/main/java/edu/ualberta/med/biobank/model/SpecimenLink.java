package edu.ualberta.med.biobank.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.hibernate.envers.Audited;

/**
 * Represents a directional parent-child relationship between two
 * {@link Specimen}s so that a {@link Specimen} can have multiple parents and
 * multiple children. However, note that a parent-child relationship can only
 * exist between two {@link Specimen}s from the same {@link Patient}.
 * 
 * @author Jonathan Ferland
 * @see {@link SpecimenPath}
 */
@Audited
@Entity
@Table(name = "SPECIMEN_LINK")
public class SpecimenLink implements Serializable {
    private static final long serialVersionUID = 1L;

    private LinkId id;
    private Specimen parent;
    private Specimen child;
    private Patient patient;

    // TODO: consider ProcessingEvents

    @EmbeddedId
    public LinkId getId() {
        return id;
    }

    public void setId(LinkId id) {
        this.id = id;
    }

    @MapsId("parentId")
    @NotNull(message = "{SpecimenLink.parent.NotNull}")
    @ManyToOne
    @JoinColumn(name = "PARENT_ID", nullable = false)
    public Specimen getParent() {
        return parent;
    }

    public void setParent(Specimen parent) {
        this.parent = parent;
    }

    @MapsId("childId")
    @NotNull(message = "{SpecimenLink.child.NotNull}")
    @ManyToOne
    @JoinColumn(name = "CHILD_ID", nullable = false)
    public Specimen getChild() {
        return child;
    }

    public void setChild(Specimen child) {
        this.child = child;
    }

    /**
     * Necessary read-only property to ensure that a {@link SpecimenLink} can
     * only exist between a {@link #getParent()} and {@link #getChild()} with
     * the same {@link Specimen#getPatient()}.
     * 
     * @return
     */
    @NotNull(message = "{SpecimenLink.patient.NotNull}")
    @ManyToOne
    @JoinColumn(name = "PATIENT_ID", nullable = false)
    Patient getPatient() {
        return null; // TODO: parent.getPatient();
    }

    void setPatient(Patient patient) {
    }

    @Embeddable
    public static class LinkId implements Serializable {
        private static final long serialVersionUID = 1L;

        private Integer parentId;
        private Integer childId;

        public Integer getParentId() {
            return parentId;
        }

        public void setParentId(Integer parentId) {
            this.parentId = parentId;
        }

        public Integer getChildId() {
            return childId;
        }

        public void setChildId(Integer childId) {
            this.childId = childId;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                + ((childId == null) ? 0 : childId.hashCode());
            result = prime * result
                + ((parentId == null) ? 0 : parentId.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            LinkId other = (LinkId) obj;
            if (childId == null) {
                if (other.childId != null) return false;
            } else if (!childId.equals(other.childId)) return false;
            if (parentId == null) {
                if (other.parentId != null) return false;
            } else if (!parentId.equals(other.parentId)) return false;
            return true;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((child == null) ? 0 : child.hashCode());
        result = prime * result + ((parent == null) ? 0 : parent.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        SpecimenLink other = (SpecimenLink) obj;
        if (child == null) {
            if (other.child != null) return false;
        } else if (!child.equals(other.child)) return false;
        if (parent == null) {
            if (other.parent != null) return false;
        } else if (!parent.equals(other.parent)) return false;
        return true;
    }
}