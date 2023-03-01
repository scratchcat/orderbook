package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import com.mizuho.order.model.Side;

import java.util.List;

public interface OrderBook {
    long add(Order order);
    Order peek(long id);
    Order remove(long id);
    void amend(long id, long newSize);
    double getPrice(Side side, int level);
    double getTotalSize(Side side, int level);
    int getMaxLevel(Side side);
    List<Order> getAllOrders(Side side);

    default void verifyLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level needs to be 1 or more");
        }
    }

    default void verifyOrder(Order order) {
        verifySide(order.getSide());
    }

    default void verifySide(Side side) {
        if (side != Side.BID && side != Side.OFFER) {
            throw new IllegalArgumentException("side needs to be B or O");
        }
    }
}
