package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import com.mizuho.order.model.Side;

import java.util.*;
import java.util.stream.Collectors;

public class SlowOrderBook implements OrderBook {
    public static final int EXPECTED_MAX_ORDER_BOOK_SIZE = 20_000;
    //price, then for same price, pick the oldest
    private List<OrderNanotime> orderNanotimes;

    public SlowOrderBook() {
        orderNanotimes = new ArrayList<>();
    }

    @Override
    public long add(Order order) {
        verifyOrder(order);
        OrderNanotime orderNanotime = new OrderNanotime(order);
        orderNanotimes.add(orderNanotime);
        return orderNanotime.getNanoTime();
    }

    @Override
    public Order peek(long id) {
        for (OrderNanotime orderNanotime : orderNanotimes) {
            if (orderNanotime.getOrder().getId() == id) {
                return orderNanotime.getOrder();
            }
        }
        return null;
    }

    @Override
    public Order remove(long id) {
        int indexToRemove = -1;
        for (int i = 0; i < orderNanotimes.size(); i++) {
            OrderNanotime orderNanotime = orderNanotimes.get(i);
            if (orderNanotime.getOrder().getId() == id) {
                indexToRemove = i;
            }
        }
        if (indexToRemove != -1) {
            return orderNanotimes.remove(indexToRemove).getOrder();
        }
        return null;
    }

    @Override
    public void amend(long id, long newSize) {
        int indexToAmend = -1;
        for (int i = 0; i < orderNanotimes.size(); i++) {
            OrderNanotime orderNanotime = orderNanotimes.get(i);
            if (orderNanotime.getOrder().getId() == id) {
                indexToAmend = i;
            }
        }
        if (indexToAmend != -1) {
            OrderNanotime orderNanotime = orderNanotimes.get(indexToAmend);
            orderNanotime = new OrderNanotime(
                    new Order(orderNanotime.getOrder().getId(), orderNanotime.getOrder().getPrice(), orderNanotime.getOrder().getSide(), newSize),
                    orderNanotime.getNanoTime());
            orderNanotimes.set(indexToAmend, orderNanotime);
        }
    }

    @Override
    public int getMaxLevel(Side side) {
        Set<Double> prices = new HashSet<>();
        for (OrderNanotime orderNanotime : orderNanotimes) {
            if (orderNanotime.getOrder().getSide() == side) {
                prices.add(orderNanotime.getOrder().getPrice());
            }
        }
        return prices.size();
    }

    @Override
    public double getPrice(Side side, int level) {
        //this approach of iterating through, calculating the levels per side would be very slow for large books
        List<Double> prices = new ArrayList<>();
        for (OrderNanotime orderNanotime : orderNanotimes) {
            if (orderNanotime.getOrder().getSide() == side) {
                prices.add(orderNanotime.getOrder().getPrice());
            }
        }
        Collections.sort(prices);
        return prices.get(level - 1);
    }

    @Override
    public double getTotalSize(Side side, int level) {
        double price = getPrice(side, level);
        long totalSize = 0;
        for (OrderNanotime orderNanotime : orderNanotimes) {
            if (orderNanotime.getOrder().getPrice() == price) {
                totalSize += orderNanotime.getOrder().getSize();
            }
        }
        return totalSize;
    }

    @Override
    public List<Order> getAllOrders(Side side) {
        List<OrderNanotime> list = orderNanotimes.stream().filter(orderNanotime -> orderNanotime.getOrder().getSide() == side).collect(Collectors.toList());
        Collections.sort(
                list,
                (a, b) -> {
                    int priceDiff = (int) (b.getOrder().getPrice() - a.getOrder().getPrice());
                    if (priceDiff != 0) {
                        return priceDiff;
                    }
                    return (int) (a.getNanoTime() - b.getNanoTime());
                }
        );
        return list.stream().map(OrderNanotime::getOrder).collect(Collectors.toList());
    }

    public List<OrderNanotime> getAllOrderNanotimes() {
        return this.orderNanotimes;
    }
}
