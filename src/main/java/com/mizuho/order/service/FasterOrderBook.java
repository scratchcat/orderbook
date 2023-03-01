package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class FasterOrderBook implements OrderBook {
    public static final int EXPECTED_MAX_ORDER_BOOK_SIZE = 3_000_000;

    //price, then for same price, pick the oldest
    private Map<Long, OrderNanotime> idToOrderNanotimeMap;
    private Map<Double, List<OrderNanotime>> priceToOrderNanotimeBidMap;
    private Map<Double, List<OrderNanotime>> priceToOrderNanotimeOfferMap;

    public FasterOrderBook() {
        idToOrderNanotimeMap = new HashMap<>(EXPECTED_MAX_ORDER_BOOK_SIZE);
        priceToOrderNanotimeBidMap = new TreeMap<>(Comparator.reverseOrder());
        priceToOrderNanotimeOfferMap = new TreeMap<>();
    }

    @Override
    public long add(Order order) {
        verifyOrder(order);
        OrderNanotime orderNanotime = new OrderNanotime(order);
        idToOrderNanotimeMap.put(order.getId(), orderNanotime);

        if (order.getSide() == 'B') {
            if (priceToOrderNanotimeBidMap.containsKey(order.getPrice())) {
                priceToOrderNanotimeBidMap.get(order.getPrice()).add(orderNanotime);
            } else {
                List<OrderNanotime> orderNanotimeList = new ArrayList<>();
                orderNanotimeList.add(orderNanotime);
                priceToOrderNanotimeBidMap.put(order.getPrice(), orderNanotimeList);
            }
        } else if (order.getSide() == 'O') {
            if (priceToOrderNanotimeOfferMap.containsKey(order.getPrice())) {
                priceToOrderNanotimeOfferMap.get(order.getPrice()).add(orderNanotime);
            } else {
                List<OrderNanotime> orderNanotimeList = new ArrayList<>();
                orderNanotimeList.add(orderNanotime);
                priceToOrderNanotimeOfferMap.put(order.getPrice(), orderNanotimeList);
            }
        }
        return orderNanotime.getNanoTime();
    }

    @Override
    public Order peek(long id) {
        return idToOrderNanotimeMap.get(id).getOrder();
    }

    @Override
    public Order remove(long id) {
        OrderNanotime orderNanotime = idToOrderNanotimeMap.remove(id);
        if (orderNanotime.getOrder().getSide() == 'B') {
            priceToOrderNanotimeBidMap.get(orderNanotime.getOrder().getPrice()).remove(orderNanotime);
            if (priceToOrderNanotimeBidMap.get(orderNanotime.getOrder().getPrice()).isEmpty()) {
                priceToOrderNanotimeBidMap.remove(orderNanotime.getOrder().getPrice());
            }
        } else { //O as previously added ok
            priceToOrderNanotimeOfferMap.get(orderNanotime.getOrder().getPrice()).remove(orderNanotime);
            if (priceToOrderNanotimeOfferMap.get(orderNanotime.getOrder().getPrice()).isEmpty()) {
                priceToOrderNanotimeOfferMap.remove(orderNanotime.getOrder().getPrice());
            }
        }
        return orderNanotime.getOrder();
    }

    @Override
    public void amend(long id, long newSize) {
        OrderNanotime orderNanotime = idToOrderNanotimeMap.get(id);

        orderNanotime = new OrderNanotime(
                new Order(orderNanotime.getOrder().getId(), orderNanotime.getOrder().getPrice(), orderNanotime.getOrder().getSide(), newSize),
                orderNanotime.getNanoTime());
        idToOrderNanotimeMap.put(id, orderNanotime);
    }

    @Override
    public double getPrice(char side, int level) {
        verifySide(side);
        verifyLevel(level);

        if (side == 'B') {
            Object[] objects = priceToOrderNanotimeBidMap.keySet().toArray();
            if (objects.length < level) {
                throw new IllegalArgumentException("level should be at most: " + objects.length);
            }
            return (double) objects[level - 1];
        } else if (side == 'O') {
            Object[] objects = priceToOrderNanotimeOfferMap.keySet().toArray();
            if (objects.length < level) {
                throw new IllegalArgumentException("level should be at most: " + objects.length);
            }
            return (double) objects[level - 1];
        } else {
            throw new IllegalArgumentException("side should be B or O");
        }
    }

    @Override
    public double getTotalSize(char side, int level) {
        verifySide(side);
        verifyLevel(level);

        if (side == 'B') {
            return priceToOrderNanotimeBidMap.values().stream()
                    .collect(Collectors.toList()).get(level - 1).stream().map(ont -> ont.getOrder().getSize()).reduce(0l, Long::sum);
        } else if (side == 'O') {
            return priceToOrderNanotimeOfferMap.values().stream()
                    .collect(Collectors.toList()).get(level - 1).stream().map(ont -> ont.getOrder().getSize()).reduce(0l, Long::sum);
        } else {
            throw new IllegalArgumentException("side should be B or O");
        }
    }

    @Override
    public int getMaxLevel(char side) {
        if (side == 'B') {
            return priceToOrderNanotimeBidMap.keySet().size();
        } else if (side == 'O') {
            return priceToOrderNanotimeOfferMap.keySet().size();
        } else {
            throw new IllegalArgumentException("side should be B or O");
        }
    }

    @Override
    public List<Order> getAllOrders(char side) { //SortedByLevelAndTime
        verifySide(side);
        List<Order> returnOrders = new ArrayList<>();
        if (side == 'B') {
            for (List<OrderNanotime> list : priceToOrderNanotimeBidMap.values()) {
                List<OrderNanotime> arrayList = new ArrayList<>(list);
                Collections.reverse(arrayList);
                for (OrderNanotime orderNanotime : arrayList) {
                    returnOrders.add(orderNanotime.getOrder());
                }
            }
        } else if (side == 'O') {
            for (List<OrderNanotime> list : priceToOrderNanotimeOfferMap.values()) {
                for (OrderNanotime orderNanotime : list) {
                    returnOrders.add(orderNanotime.getOrder());
                }
            }
        }
        return returnOrders;
    }
}