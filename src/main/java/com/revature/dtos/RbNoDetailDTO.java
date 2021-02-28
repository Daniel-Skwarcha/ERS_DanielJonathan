package com.revature.dtos;

import java.util.Objects;

/**
 * A DTO to facilitate easy transferring reimbursements
 */
public class RbNoDetailDTO {
    private Integer id;
    private Integer authorId;
    private String status;
    private String type;


    public RbNoDetailDTO() {
        super();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RbDTO)) return false;
        RbNoDetailDTO rbDTO = (RbNoDetailDTO) o;
        return Objects.equals(getId(), rbDTO.getId()) &&
                Objects.equals(getAuthorId(), rbDTO.getAuthorId()) &&
                Objects.equals(getStatus(), rbDTO.getStatus()) &&
                Objects.equals(getType(), rbDTO.getType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAuthorId(), getStatus(), getType());
    }

    @Override
    public String toString() {
        return "RbDTO{" +
                "id=" + id +
                ", authorName='" + authorId + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}

