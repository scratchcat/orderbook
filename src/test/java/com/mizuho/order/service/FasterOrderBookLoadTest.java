package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static com.mizuho.order.service.FasterOrderBook.EXPECTED_MAX_ORDER_BOOK_SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FasterOrderBookLoadTest {
    FasterOrderBook fasterOrderBook;
    Random random = new Random();

    @BeforeEach
    public void setUp() {
        fasterOrderBook = new FasterOrderBook();
    }

    @Test
    public void shouldAddAndRemoveManyOrders() {
        long start = System.currentTimeMillis();
        add();
        remove();
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to add and remove " + EXPECTED_MAX_ORDER_BOOK_SIZE + " orders.");
    }

    @Test
    public void shouldAddAndAmendManyOrders() {
        long start = System.currentTimeMillis();
        add();
        amend();
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to add and amend " + EXPECTED_MAX_ORDER_BOOK_SIZE + " orders.");
    }

    @Test
    public void shouldAddAndGetPriceManyOrders() {
        add();
        long start = System.currentTimeMillis();
        double bidPriceLevel10 = fasterOrderBook.getPrice('B', 10);
        double offerPriceLevel10 = fasterOrderBook.getPrice('O', 10);
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to getPrice for bid and offer level10");
    }

    @Test
    public void shouldAddAndGetTotalSizeManyOrders() {
        add();
        long start = System.currentTimeMillis();
        for (int i = 1; i <= fasterOrderBook.getMaxLevel('B'); i++) {
            double bidTotalSize = fasterOrderBook.getTotalSize('B', i);
        }
        for (int i = 1; i <= fasterOrderBook.getMaxLevel('O'); i++) {
            double offerTotalSize = fasterOrderBook.getTotalSize('O', i);
        }
        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to getTotalSize for bid and offer level10");
    }

    @Test
    public void shouldAddAndGetAllOrdersManyOrders() {
        add();
        long start = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": count of all bid orders: " + fasterOrderBook.getAllOrders('B').size());
        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": count of all offer orders: " + fasterOrderBook.getAllOrders('O').size());

        long stop = System.currentTimeMillis();

        System.out.println(EXPECTED_MAX_ORDER_BOOK_SIZE + ": it took: " + (stop - start) + " ms to getAllOrders for bid and offer");
    }

    private void amend() {
        for (long i = 1; i <= EXPECTED_MAX_ORDER_BOOK_SIZE; i++) {
            long newSize = fasterOrderBook.peek(i).getSize() + 1;
            fasterOrderBook.amend(i, newSize);
            assertEquals(newSize, fasterOrderBook.peek(i).getSize());
        }
    }

    private void add() {
        for (long i = 1; i <= EXPECTED_MAX_ORDER_BOOK_SIZE; i++) {
            double v = random.nextInt(10000);
            long nanoTime = fasterOrderBook.add(new Order(i, v, (i % 2) == 0 ? 'B' : 'O', (long) random.nextInt(10)));
        }
    }

    private void remove() {
        for (long i = 1; i <= EXPECTED_MAX_ORDER_BOOK_SIZE; i++) {
            Order order = fasterOrderBook.remove(i);
            assertEquals(i, order.getId());
        }
    }
}
