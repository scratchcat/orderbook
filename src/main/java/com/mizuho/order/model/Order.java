package com.mizuho.order.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public final class Order {
    private final long id;
    private final double price;
    private final char side; //B or O
    private final long size;
}
