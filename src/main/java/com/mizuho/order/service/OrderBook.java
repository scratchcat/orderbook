package com.mizuho.order.service;

import com.mizuho.order.model.Order;

import java.util.List;

public interface OrderBook {
    long add(Order order);
    Order peek(long id);
    Order remove(long id);
    void amend(long id, long newSize);
    double getPrice(char side, int level);
    double getTotalSize(char side, int level);
    int getMaxLevel(char side);
    List<Order> getAllOrders(char side);

    default void verifyLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException("level needs to be 1 or more");
        }
    }

    default void verifyOrder(Order order) {
        verifySide(order.getSide());
    }

    default void verifySide(char side) {
        if (side != 'B' && side != 'O') {
            throw new IllegalArgumentException("side needs to be B or O");
        }
    }
}
