package br.com.bea.androidtools.test.model;

import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Column.Type;
import br.com.bea.androidtools.api.model.annotations.Id;
import br.com.bea.androidtools.api.model.annotations.Metadata;
import br.com.bea.androidtools.api.model.annotations.Name;
import br.com.bea.androidtools.api.model.annotations.Table;

@Name("COMPLEX")
@Table(name = "COMPLEX")
public class ComplexEntity extends Entity<Long> {

    private static final long serialVersionUID = 1L;

    @Metadata("id")
    @Id
    @Column(name = "ID", type = Type.INTEGER)
    private Long id;

    @Metadata("SIMPLE")
    private SimpleEntity simpleEntity;

    @Override
    public Long getId() {
        return id;
    }

    public SimpleEntity getSimpleEntity() {
        return simpleEntity;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public void setSimpleEntity(final SimpleEntity simpleEntity) {
        this.simpleEntity = simpleEntity;
    }

}
