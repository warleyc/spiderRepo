package com.mycompany.myapp.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * A Instance.
 */
@Entity
@Table(name = "instance")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "instance")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Instance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @Column(name = "country_name")
    private String countryName;

    @OneToMany(mappedBy = "instance")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @org.springframework.data.annotation.Transient
    @JsonIgnoreProperties(value = { "instance" }, allowSetters = true)
    private Set<WMISComponent> instances = new HashSet<>();

    @ManyToOne
    @JsonIgnoreProperties(value = { "applications" }, allowSetters = true)
    private Application application;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Instance id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCountryName() {
        return this.countryName;
    }

    public Instance countryName(String countryName) {
        this.setCountryName(countryName);
        return this;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public Set<WMISComponent> getInstances() {
        return this.instances;
    }

    public void setInstances(Set<WMISComponent> wMISComponents) {
        if (this.instances != null) {
            this.instances.forEach(i -> i.setInstance(null));
        }
        if (wMISComponents != null) {
            wMISComponents.forEach(i -> i.setInstance(this));
        }
        this.instances = wMISComponents;
    }

    public Instance instances(Set<WMISComponent> wMISComponents) {
        this.setInstances(wMISComponents);
        return this;
    }

    public Instance addInstance(WMISComponent wMISComponent) {
        this.instances.add(wMISComponent);
        wMISComponent.setInstance(this);
        return this;
    }

    public Instance removeInstance(WMISComponent wMISComponent) {
        this.instances.remove(wMISComponent);
        wMISComponent.setInstance(null);
        return this;
    }

    public Application getApplication() {
        return this.application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Instance application(Application application) {
        this.setApplication(application);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Instance)) {
            return false;
        }
        return id != null && id.equals(((Instance) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Instance{" +
            "id=" + getId() +
            ", countryName='" + getCountryName() + "'" +
            "}";
    }
}
