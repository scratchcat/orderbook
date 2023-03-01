package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import com.mizuho.order.model.Side;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class FasterOrderBook implements OrderBook {
    public static final int EXPECTED_MAX_ORDER_BOOK_SIZE = 7_000_000;

    //price, then for same price, pick the oldest
    private Map<Long, OrderNanotime> idToOrderNanotimeMap;
    private Map<Double, TotalSizeInfo> priceToOrderNanotimeBidMap;
    private Map<Double, TotalSizeInfo> priceToOrderNanotimeOfferMap;

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

        if (order.getSide()== Side.BID) {
            if (priceToOrderNanotimeBidMap.containsKey(order.getPrice())) {
                priceToOrderNanotimeBidMap.get(order.getPrice()).add(orderNanotime);
            } else {
                TotalSizeInfo totalSizeInfo = new TotalSizeInfo();
                totalSizeInfo.add(orderNanotime);
                priceToOrderNanotimeBidMap.put(order.getPrice(), totalSizeInfo);
            }
        } else if (order.getSide() ==Side.OFFER) {
            if (priceToOrderNanotimeOfferMap.containsKey(order.getPrice())) {
                priceToOrderNanotimeOfferMap.get(order.getPrice()).add(orderNanotime);
            } else {
                TotalSizeInfo totalSizeInfo = new TotalSizeInfo();
                totalSizeInfo.add(orderNanotime);
                priceToOrderNanotimeOfferMap.put(order.getPrice(), totalSizeInfo);
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
        if (orderNanotime.getOrder().getSide() == Side.BID) {
            priceToOrderNanotimeBidMap.get(orderNanotime.getOrder().getPrice()).remove(orderNanotime);
            if (priceToOrderNanotimeBidMap.get(orderNanotime.getOrder().getPrice()).isOrderListEmpty()) {
                priceToOrderNanotimeBidMap.remove(orderNanotime.getOrder().getPrice());
            }
        } else { //O as previously added ok
            priceToOrderNanotimeOfferMap.get(orderNanotime.getOrder().getPrice()).remove(orderNanotime);
            if (priceToOrderNanotimeOfferMap.get(orderNanotime.getOrder().getPrice()).isOrderListEmpty()) {
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
    public double getPrice(Side side, int level) {
        verifySide(side);
        verifyLevel(level);

        if (side == Side.BID) {
            Object[] objects = priceToOrderNanotimeBidMap.keySet().toArray();
            if (objects.length < level) {
                throw new IllegalArgumentException("level should be at most: " + objects.length);
            }
            return (double) objects[level - 1];
        } else if (side == Side.OFFER) {
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
    public double getTotalSize(Side side, int level) {
        verifySide(side);
        verifyLevel(level);

        if (side == Side.BID) {
            return priceToOrderNanotimeBidMap.values().stream()
                    .collect(Collectors.toList()).get(level - 1).getTotalSize();
        } else if (side == Side.OFFER) {
            return priceToOrderNanotimeOfferMap.values().stream()
                    .collect(Collectors.toList()).get(level - 1).getTotalSize();
        } else {
            throw new IllegalArgumentException("side should be B or O");
        }
    }

    @Override
    public int getMaxLevel(Side side) {
        if (side == Side.BID) {
            return priceToOrderNanotimeBidMap.keySet().size();
        } else if (side == Side.OFFER) {
            return priceToOrderNanotimeOfferMap.keySet().size();
        } else {
            throw new IllegalArgumentException("side should be B or O");
        }
    }

    @Override
    public List<Order> getAllOrders(Side side) { //SortedByLevelAndTime
        verifySide(side);
        List<Order> returnOrders = new ArrayList<>();
        if (side == Side.BID) {
            for (TotalSizeInfo totalSizeInfo : priceToOrderNanotimeBidMap.values()) {
                List<OrderNanotime> arrayList = totalSizeInfo.getOrderNanotimeList();
                Collections.reverse(arrayList);
                for (OrderNanotime orderNanotime : arrayList) {
                    returnOrders.add(orderNanotime.getOrder());
                }
            }
        } else if (side == Side.OFFER) {
            for (TotalSizeInfo totalSizeInfo : priceToOrderNanotimeOfferMap.values()) {
                for (OrderNanotime orderNanotime : totalSizeInfo.getOrderNanotimeList()) {
                    returnOrders.add(orderNanotime.getOrder());
                }
            }
        }
        return returnOrders;
    }
}

@Getter
class TotalSizeInfo {
    private int totalSize;
    private List<OrderNanotime> orderNanotimeList;

    public TotalSizeInfo() {
        totalSize = 0;
        orderNanotimeList = new LinkedList<>(); //ArrayList remove was slow for many orders with same price, linkedlist is much quicker
    }

    public void add(OrderNanotime orderNanotime) {
        this.orderNanotimeList.add(orderNanotime);
        totalSize += orderNanotime.getOrder().getSize();
    }

    public void remove(OrderNanotime orderNanotime) {
        this.orderNanotimeList.remove(orderNanotime);
        totalSize -= orderNanotime.getOrder().getSize();
    }

    public boolean isOrderListEmpty() {
        return orderNanotimeList.isEmpty();
    }
}