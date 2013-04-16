package br.com.bea.androidtools.test.model;

import java.math.BigDecimal;
import java.util.Date;
import br.com.bea.androidtools.api.model.Entity;
import br.com.bea.androidtools.api.model.annotations.Column;
import br.com.bea.androidtools.api.model.annotations.Column.Type;
import br.com.bea.androidtools.api.model.annotations.DateFormat;
import br.com.bea.androidtools.api.model.annotations.Id;
import br.com.bea.androidtools.api.model.annotations.Metadata;
import br.com.bea.androidtools.api.model.annotations.Table;

@Table(name = "SIMPLE")
public class SimpleEntity extends Entity<Long> {

    private static final long serialVersionUID = 1L;
    @Metadata("alone")
    @Column(name = "ALONE", type = Type.INTEGER)
    private boolean alone;

    @Metadata("data")
    @Column(name = "DATA", type = Type.BLOB)
    private byte[] data;

    @Metadata("id")
    @Id
    @Column(name = "ID", type = Type.INTEGER)
    private Long id;

    @Metadata("name")
    @Column(name = "NAME", type = Type.TEXT)
    private String name;

    @Metadata("price")
    @Column(name = "PRICE", type = Type.REAL)
    private BigDecimal price;

    @DateFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Metadata("version")
    @Column(name = "VERSION", type = Type.NUMERIC)
    private Date version;

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final SimpleEntity other = (SimpleEntity) obj;
        if (id == null) {
            if (other.id != null) return false;
        } else if (!id.equals(other.id)) return false;
        return true;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    @Override
    public Date getVersion() {
        return version;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        return result;
    }

    public boolean isAlone() {
        return alone;
    }

    public void setAlone(final boolean alone) {
        this.alone = alone;
    }

    public void setData(final byte[] data) {
        this.data = data;
    }

    @Override
    public void setId(final Long id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public void setVersion(final Date version) {
        this.version = version;
    }

}
