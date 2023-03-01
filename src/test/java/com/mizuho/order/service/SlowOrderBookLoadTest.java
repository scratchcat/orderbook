package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static com.mizuho.order.service.SlowOrderBook.EXPECTED_MAX_ORDER_BOOK_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class SlowOrderBookLoadTest {
    SlowOrderBook slowOrderBook;
    Random random = new Random();

    @BeforeEach
    public void setUp() {
        slowOrderBook = new SlowOrderBook();
    }

    @Test
    public void shouldAddAndRemoveManyOrders() {
        long start = System.currentTimeMillis();
        add();
        remove();
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took:" + (stop - start) + " ms to add and remove " + EXPECTED_MAX_ORDER_BOOK_SIZE + " orders.");
    }

    @Test
    public void shouldAddAndAmendManyOrders() {
        long start = System.currentTimeMillis();
        add();
        amend();
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took:" + (stop - start) + " ms to add and remove " + EXPECTED_MAX_ORDER_BOOK_SIZE + " orders.");
    }

    @Test
    public void shouldAddAndGetPriceManyOrders() {
        add();
        long start = System.currentTimeMillis();
        double bidPriceLevel10 = slowOrderBook.getPrice('B', 10);
        double offerPriceLevel10 = slowOrderBook.getPrice('O', 10);
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to getPrice for bid and offer level10");
    }

    @Test
    public void shouldAddAndGetTotalSizeManyOrders() {
        add();
        long start = System.currentTimeMillis();
        int b = slowOrderBook.getMaxLevel('B');
        for (int i = 1; i <= b; i++) {
            double bidTotalSize = slowOrderBook.getTotalSize('B', i);
        }
        int o = slowOrderBook.getMaxLevel('O');
        for (int i = 1; i <= o; i++) {
            double offerTotalSize = slowOrderBook.getTotalSize('O', i);
        }
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to getTotalSize for bid and offer level10");
    }

    @Test
    public void shouldAddAndGetAllOrdersManyOrders() {
        add();
        long start = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": count of all bid orders: " + slowOrderBook.getAllOrders('B').size());
        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": count of all offer orders: " + slowOrderBook.getAllOrders('O').size());

        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to getAllOrders for bid and offer");
    }

    private void amend() {
        for (long i = 1; i <= EXPECTED_MAX_ORDER_BOOK_SIZE; i++) {
            long newSize = slowOrderBook.peek(i).getSize() + 1;
            slowOrderBook.amend(i, newSize);
            assertEquals(newSize, slowOrderBook.peek(i).getSize());
        }
    }

    private void add() {
        for (long i = 1; i <= EXPECTED_MAX_ORDER_BOOK_SIZE; i++) {
            double v = random.nextInt(10000);
            long nanoTime = slowOrderBook.add(new Order(i, v, (i % 2) == 0 ? 'B' : 'O', (long) random.nextInt(10)));
        }
    }

    private void remove() {
        for (long i = 1; i <= EXPECTED_MAX_ORDER_BOOK_SIZE; i++) {
            Order order = slowOrderBook.remove(i);
            assertEquals(i, order.getId());
        }
    }
}
