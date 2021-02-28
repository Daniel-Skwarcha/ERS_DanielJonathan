package com.revature.models;

import org.apache.logging.log4j.core.config.Order;

import javax.persistence.*;
import java.io.File;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * The base unit of the ERS system. ready to include images
 */
@Entity
@Table(catalog="revature_storage", name = "ers_reimbursements", schema="ers")
public class Reimbursement {
    @Id
    @GeneratedValue
    private Integer id;

    @Column(name= "amount")
    private Double amount;

    @Column(name = "submitted")
    private Timestamp submitted;

    @Column(name="resolved")
    private Timestamp resolved;

    @Column(name = "description")
    private String description;

    // troy misspelled receipt in the database
    @Column(name = "reciept")
    private File receipt;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="author_id")
    @OrderBy
    private User author;

    @ManyToOne(targetEntity = User.class)
    @JoinColumn(name="resolver_id")
    @OrderBy
    private User resolver;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "reimbursement_status_id")
    private ReimbursementStatus reimbursementStatus;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "reimbursement_type_id")
    private ReimbursementType reimbursementType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Timestamp getSubmitted() {
        return submitted;
    }

    public void setSubmitted(Timestamp submitted) {
        this.submitted = submitted;
    }

    public Timestamp getResolved() {
        return resolved;
    }

    public void setResolved(Timestamp resolved) {
        this.resolved = resolved;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public File getReceipt() {
        return receipt;
    }

    public void setReceipt(File receipt) {
        this.receipt = receipt;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(User author) {
        this.author = author;
    }

    public User getResolver() {
        return resolver;
    }

    public void setResolver(User resolver) {
        this.resolver = resolver;
    }

    public ReimbursementStatus getReimbursementStatus() {
        return reimbursementStatus;
    }

    public void setReimbursementStatus(ReimbursementStatus reimbursementStatus) {
        this.reimbursementStatus = reimbursementStatus;
    }

    public ReimbursementType getReimbursementType() {
        return reimbursementType;
    }

    public void setReimbursementType(ReimbursementType reimbursementType) {
        this.reimbursementType = reimbursementType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reimbursement that = (Reimbursement) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(amount, that.amount) &&
                Objects.equals(submitted, that.submitted) &&
                Objects.equals(resolved, that.resolved) &&
                Objects.equals(description, that.description) &&
                Objects.equals(receipt, that.receipt) &&
                Objects.equals(author, that.author) &&
                Objects.equals(resolver, that.resolver) &&
                reimbursementStatus == that.reimbursementStatus &&
                reimbursementType == that.reimbursementType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, amount, submitted, resolved, description, receipt, author, resolver, reimbursementStatus, reimbursementType);
    }

    @Override
    public String toString() {
        return "Reimbursement{" +
                "id=" + id +
                ", amount=" + amount +
                ", submitted=" + submitted +
                ", resolved=" + resolved +
                ", description='" + description + '\'' +
                ", receipt=" + receipt +
                ", author=" + author +
                ", resolver=" + resolver +
                ", reimbursementStatus=" + reimbursementStatus +
                ", reimbursementType=" + reimbursementType +
                '}';
    }
}
