package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
final class OrderNanotime {
    private final Order order;
    private final long nanoTime;

    public OrderNanotime(Order order) {
        this.order = order;
        this.nanoTime = System.nanoTime();
    }

    public OrderNanotime(Order order, long nanoTime) {
        this.order = order;
        this.nanoTime = nanoTime;
    }
}
