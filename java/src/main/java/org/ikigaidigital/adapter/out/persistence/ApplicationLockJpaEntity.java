package org.ikigaidigital.adapter.out.persistence;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** A named row used only as a database-wide lock (SELECT ... FOR UPDATE). */
@Entity
@Table(name = "applicationLocks")
public class ApplicationLockJpaEntity {

    @Id
    private String name;

    protected ApplicationLockJpaEntity() {
    }

    public String getName() {
        return name;
    }
}


