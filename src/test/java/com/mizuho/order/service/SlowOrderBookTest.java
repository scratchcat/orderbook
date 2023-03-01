package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlowOrderBookTest {
    SlowOrderBook slowOrderBook;

    @BeforeEach
    public void setUp() {
        slowOrderBook = new SlowOrderBook();
    }

    @Test
    public void shouldConstructEmptyOrderBook() {
        assertNotNull(slowOrderBook);
        assertTrue(slowOrderBook.getAllOrderNanotimes().isEmpty());
    }

    @Test
    public void shouldAddOrders() {
        long nanoTime1 = slowOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = slowOrderBook.add(new Order(2l, 2d, 'B', 3l));

        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), slowOrderBook.getAllOrderNanotimes().get(0));
        assertEquals(new OrderNanotime(new Order(2l, 2d, 'B', 3l), nanoTime2), slowOrderBook.getAllOrderNanotimes().get(1));
    }

    @Test
    public void shouldRemoveOrders() {
        long nanoTime1 = slowOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = slowOrderBook.add(new Order(2l, 2d, 'B', 3l));

        slowOrderBook.remove(1l);
        assertEquals(1, slowOrderBook.getAllOrderNanotimes().size());
        assertEquals(new OrderNanotime(new Order(2l, 2d, 'B', 3l), nanoTime2), slowOrderBook.getAllOrderNanotimes().get(0));

        slowOrderBook.remove(3l);
        assertEquals(1, slowOrderBook.getAllOrderNanotimes().size());

        slowOrderBook.remove(2l);
        assertEquals(0, slowOrderBook.getAllOrderNanotimes().size());
    }

    @Test
    public void shouldAmendOrders() {
        long nanoTime1 = slowOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = slowOrderBook.add(new Order(2l, 2d, 'B', 3l));

        slowOrderBook.amend(2l, 4l);
        assertEquals(new Order(2l, 2d, 'B', 4l), slowOrderBook.getAllOrderNanotimes().get(1).getOrder());
        assertEquals(nanoTime2, slowOrderBook.getAllOrderNanotimes().get(1).getNanoTime());
    }

    @Test
    public void shouldGetMaxLevel() {
        long nanoTime1 = slowOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = slowOrderBook.add(new Order(2l, 3d, 'B', 3l));

        assertEquals(2, slowOrderBook.getMaxLevel('B'));
        assertEquals(0, slowOrderBook.getMaxLevel('O'));
    }

    @Test
    public void shouldGetPrice() {
        long nanoTime1 = slowOrderBook.add(new Order(1l, 5d, 'B', 3l));
        long nanoTime2 = slowOrderBook.add(new Order(2l, 4d, 'B', 3l));
        long nanoTime3 = slowOrderBook.add(new Order(3l, 3d, 'O', 3l));
        long nanoTime4 = slowOrderBook.add(new Order(4l, 3d, 'B', 3l));

        assertEquals(4d, slowOrderBook.getPrice('B', 2));
        assertEquals(3d, slowOrderBook.getPrice('O', 1));
    }

    @Test
    public void shouldGetTotalSize() {
        long nanoTime1 = slowOrderBook.add(new Order(1l, 5d, 'B', 3l));
        long nanoTime2 = slowOrderBook.add(new Order(2l, 4d, 'B', 3l));
        long nanoTime3 = slowOrderBook.add(new Order(3l, 3d, 'O', 3l));
        long nanoTime4 = slowOrderBook.add(new Order(4l, 4d, 'B', 6l));

        assertEquals(9l, slowOrderBook.getTotalSize('B', 2));
        assertEquals(3d, slowOrderBook.getTotalSize('O', 1));
    }

    @Test
    public void shouldGetAllOrders() {
        long nanoTime1 = slowOrderBook.add(new Order(1l, 5d, 'B', 3l));
        long nanoTime2 = slowOrderBook.add(new Order(2l, 4d, 'B', 3l));
        long nanoTime3 = slowOrderBook.add(new Order(3l, 3d, 'O', 3l));
        long nanoTime4 = slowOrderBook.add(new Order(4l, 4d, 'B', 6l));
        long nanoTime5 = slowOrderBook.add(new Order(4l, 6d, 'B', 6l));

        assertEquals(List.of(
                new Order(4l, 6d, 'B', 6l),
                new Order(1l, 5d, 'B', 3l),
                new Order(2l, 4d, 'B', 3l),
                new Order(4l, 4d, 'B', 6l)
        ), slowOrderBook.getAllOrders('B'));

        assertEquals(List.of(new Order(3l, 3d, 'O', 3l)),
                slowOrderBook.getAllOrders('O'));
    }

}