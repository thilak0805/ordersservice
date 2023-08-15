package com.appsdeveloperblog.estore.ordersservice.core.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Entity
@Table(name="orderlookup")
@NoArgsConstructor
@AllArgsConstructor
public class OrderLookupEntity implements Serializable {
    private static final long serialVersionUID = 5313493413859894403L;
    @Id
    public String orderId;
    @Column(unique = true)
    public String productId;
    private String addressId;
}
