package com.appsdeveloperblog.estore.ordersservice.core.data;

import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.criteria.Order;

public interface OrderLookupRepository extends JpaRepository<OrderLookupEntity, String> {

    OrderLookupEntity findByProductIdOrAddressId(String productId, String addressId);
}
