package com.mizuho.order.service;

import com.mizuho.order.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FasterOrderBookTest {
    FasterOrderBook fasterOrderBook;

    @BeforeEach
    public void setUp() {
        fasterOrderBook = new FasterOrderBook();
    }

    @Test
    public void shouldConstructEmptyOrderBook() {
        assertNotNull(fasterOrderBook);
        assertTrue(fasterOrderBook.getIdToOrderNanotimeMap().isEmpty());
    }

    @Test
    public void shouldAddOrder() {
        long nanoTime = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));

        assertEquals(1, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime), fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).toArray()[0]);
        assertEquals(1, fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).size());
    }

    @Test
    public void shouldAddOfferOrder() {
        long nanoTime = fasterOrderBook.add(new Order(1l, 2d, 'O', 3l));

        assertEquals(1, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'O', 3l), nanoTime), fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'O', 3l), nanoTime), fasterOrderBook.getPriceToOrderNanotimeOfferMap().get(2d).toArray()[0]);
        assertEquals(1, fasterOrderBook.getPriceToOrderNanotimeOfferMap().get(2d).size());
    }


    @Test
    public void shouldAddTwoOrdersWithSamePrice() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 3l));

        assertEquals(2, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(new OrderNanotime(new Order(2l, 2d, 'B', 3l), nanoTime2), fasterOrderBook.getIdToOrderNanotimeMap().get(2l));
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).toArray()[0]);
        assertEquals(new OrderNanotime(new Order(2l, 2d, 'B', 3l), nanoTime2), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).toArray()[1]);
        assertEquals(2, fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).size());
    }

    @Test
    public void shouldAddThreeOrdersTwoWithSamePriceOneWithDifferentPrice() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 1d, 'B', 3l));
        long nanoTime3 = fasterOrderBook.add(new Order(3l, 2d, 'B', 3l));

        assertEquals(3, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(new OrderNanotime(new Order(2l, 1d, 'B', 3l), nanoTime2), fasterOrderBook.getIdToOrderNanotimeMap().get(2l));
        assertEquals(new OrderNanotime(new Order(3l, 2d, 'B', 3l), nanoTime3), fasterOrderBook.getIdToOrderNanotimeMap().get(3l));
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).toArray()[0]);
        assertEquals(new OrderNanotime(new Order(2l, 1d, 'B', 3l), nanoTime2), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(1d).toArray()[0]);
        assertEquals(new OrderNanotime(new Order(3l, 2d, 'B', 3l), nanoTime3), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).toArray()[1]);
        assertEquals(2, fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).size());
        assertEquals(1, fasterOrderBook.getPriceToOrderNanotimeBidMap().get(1d).size());
    }

    @Test
    public void shouldRemoveTheOnlyBidOrder() {
        long nanoTime = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));

        Order removedOrder = fasterOrderBook.remove(1l);

        assertEquals(0, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(0, fasterOrderBook.getPriceToOrderNanotimeBidMap().size());
        assertEquals(removedOrder, new Order(1l, 2d, 'B', 3l));
    }

    @Test
    public void shouldRemoveOneOfOtherBidOrders() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 4l));

        Order removedOrder = fasterOrderBook.remove(2l);

        assertEquals(1, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertNull(fasterOrderBook.getIdToOrderNanotimeMap().get(2l));
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(1, fasterOrderBook.getPriceToOrderNanotimeBidMap().size());
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).toArray()[0]);

        assertEquals(removedOrder, new Order(2l, 2d, 'B', 4l));
    }

    @Test
    public void shouldRemoveOfferOrder() {
        long nanoTime = fasterOrderBook.add(new Order(1l, 2d, 'O', 3l));

        Order removedOrder = fasterOrderBook.remove(1l);

        assertEquals(0, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertNull(fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(0, fasterOrderBook.getPriceToOrderNanotimeOfferMap().size());
        assertEquals(removedOrder, new Order(1l, 2d, 'O', 3l));
    }

    @Test
    public void shouldRemoveOneOfOtherOfferOrders() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'O', 3l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'O', 4l));

        Order removedOrder = fasterOrderBook.remove(1l);

        assertEquals(1, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertNull(fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(1, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(1, fasterOrderBook.getPriceToOrderNanotimeOfferMap().size());
        assertEquals(new OrderNanotime(new Order(2l, 2d, 'O', 4l), nanoTime2), fasterOrderBook.getPriceToOrderNanotimeOfferMap().get(2d).toArray()[0]);

        assertEquals(removedOrder, new Order(1l, 2d, 'O', 3l));
    }

    @Test
    public void shouldRemoveOrderLeavingRemainingAtSameLevelAndOtherLevels() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 1d, 'B', 3l));
        long nanoTime3 = fasterOrderBook.add(new Order(3l, 2d, 'B', 3l));

        Order removedOrder = fasterOrderBook.remove(1l);

        assertEquals(2, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertNull(fasterOrderBook.getIdToOrderNanotimeMap().get(1l));
        assertEquals(2, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(2, fasterOrderBook.getPriceToOrderNanotimeBidMap().size());
        assertTrue(fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).contains(new OrderNanotime(new Order(3l, 2d, 'B', 3l), nanoTime3)));
        assertTrue(fasterOrderBook.getPriceToOrderNanotimeBidMap().get(1d).contains(new OrderNanotime(new Order(2l, 1d, 'B', 3l), nanoTime2)));
        assertEquals(removedOrder, new Order(1l, 2d, 'B', 3l));
    }

    @Test
    public void shouldAmendSize() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));

        fasterOrderBook.amend(1, 5l);
        assertEquals(1, fasterOrderBook.getIdToOrderNanotimeMap().size());
        assertEquals(new OrderNanotime(new Order(1l, 2d, 'B', 3l), nanoTime1), fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).toArray()[0]);
        assertEquals(1, fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).size());
        assertEquals(1, fasterOrderBook.getPriceToOrderNanotimeBidMap().get(2d).size());
        assertEquals(new Order(1l, 2d, 'B', 5l), fasterOrderBook.getIdToOrderNanotimeMap().get(1l).getOrder());
    }

    @Test
    public void shouldGetPriceForBidLevel1OrdersSamePrice() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 3l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 3l));

        assertEquals(2d, fasterOrderBook.getPrice('B', 1));
    }

    @Test
    public void shouldGetPriceForBidsOrdersDifferentPrices() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 1l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 2l));
        long nanoTime3 = fasterOrderBook.add(new Order(3l, 3d, 'B', 3l));
        long nanoTime4 = fasterOrderBook.add(new Order(4l, 1d, 'B', 4l));
        long nanoTime5 = fasterOrderBook.add(new Order(5l, 3d, 'B', 5l));
        long nanoTime6 = fasterOrderBook.add(new Order(6l, 1d, 'B', 6l));

        assertEquals(3d, fasterOrderBook.getPrice('B', 1));
        assertEquals(2d, fasterOrderBook.getPrice('B', 2));
        assertEquals(1d, fasterOrderBook.getPrice('B', 3));
    }

    @Test
    public void shouldGetPriceForOffersOrdersDifferentPrices() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 1l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 2l));
        long nanoTime3 = fasterOrderBook.add(new Order(3l, 3d, 'O', 3l));
        long nanoTime4 = fasterOrderBook.add(new Order(4l, 1d, 'O', 4l));
        long nanoTime5 = fasterOrderBook.add(new Order(5l, 3d, 'B', 5l));
        long nanoTime6 = fasterOrderBook.add(new Order(6l, 1d, 'B', 6l));
        long nanoTime7 = fasterOrderBook.add(new Order(7l, 3d, 'O', 7l));
        long nanoTime8 = fasterOrderBook.add(new Order(8l, 1d, 'O', 8l));

        assertEquals(1d, fasterOrderBook.getPrice('O', 1));
        assertEquals(3d, fasterOrderBook.getPrice('O', 2));
        assertEquals(3d, fasterOrderBook.getPrice('B', 1));
        assertEquals(2d, fasterOrderBook.getPrice('B', 2));
        assertEquals(1d, fasterOrderBook.getPrice('B', 3));
    }

    @Test
    public void shouldGetTotalSize() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 1l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 2l));
        long nanoTime3 = fasterOrderBook.add(new Order(3l, 3d, 'O', 3l));
        long nanoTime4 = fasterOrderBook.add(new Order(4l, 1d, 'O', 4l));
        long nanoTime5 = fasterOrderBook.add(new Order(5l, 3d, 'B', 5l));
        long nanoTime6 = fasterOrderBook.add(new Order(6l, 1d, 'B', 6l));
        long nanoTime7 = fasterOrderBook.add(new Order(7l, 3d, 'O', 7l));
        long nanoTime8 = fasterOrderBook.add(new Order(8l, 1d, 'O', 8l));

        assertEquals(12l, fasterOrderBook.getTotalSize('O', 1));
        assertEquals(10l, fasterOrderBook.getTotalSize('O', 2));
        assertEquals(5l, fasterOrderBook.getTotalSize('B', 1));
        assertEquals(3l, fasterOrderBook.getTotalSize('B', 2));
        assertEquals(6l, fasterOrderBook.getTotalSize('B', 3));
    }

    @Test
    public void shouldGetAllOrders() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 1l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 2l));
        long nanoTime3 = fasterOrderBook.add(new Order(3l, 3d, 'O', 3l));
        long nanoTime4 = fasterOrderBook.add(new Order(4l, 1d, 'O', 4l));
        long nanoTime5 = fasterOrderBook.add(new Order(5l, 3d, 'B', 5l));
        long nanoTime6 = fasterOrderBook.add(new Order(6l, 1d, 'B', 6l));
        long nanoTime7 = fasterOrderBook.add(new Order(7l, 3d, 'O', 7l));
        long nanoTime8 = fasterOrderBook.add(new Order(8l, 1d, 'O', 8l));

        List<Order> bidOrders = fasterOrderBook.getAllOrders('B'); //level and time order
        List<Order> offerOrders = fasterOrderBook.getAllOrders('O'); //level and time order

        List<Order> expectedBidOrders = new ArrayList<>();
        expectedBidOrders.add(new Order(5l, 3d, 'B', 5l));
        expectedBidOrders.add(new Order(2l, 2d, 'B', 2l));
        expectedBidOrders.add(new Order(1l, 2d, 'B', 1l));
        expectedBidOrders.add(new Order(6l, 1d, 'B', 6l));
        assertEquals(expectedBidOrders, bidOrders);

        List<Order> expectedOfferOrders = new ArrayList<>();
        expectedOfferOrders.add(new Order(4l, 1d, 'O', 4l));
        expectedOfferOrders.add(new Order(8l, 1d, 'O', 8l));
        expectedOfferOrders.add(new Order(3l, 3d, 'O', 3l));
        expectedOfferOrders.add(new Order(7l, 3d, 'O', 7l));
        assertEquals(expectedOfferOrders, offerOrders);
    }

    @Test
    public void shouldGetMaxBidAndOfferLevel() {
        long nanoTime1 = fasterOrderBook.add(new Order(1l, 2d, 'B', 1l));
        long nanoTime2 = fasterOrderBook.add(new Order(2l, 2d, 'B', 2l));
        long nanoTime3 = fasterOrderBook.add(new Order(3l, 3d, 'O', 3l));
        long nanoTime4 = fasterOrderBook.add(new Order(4l, 1d, 'O', 4l));
        long nanoTime5 = fasterOrderBook.add(new Order(5l, 3d, 'B', 5l));
        long nanoTime6 = fasterOrderBook.add(new Order(6l, 1d, 'B', 6l));
        long nanoTime7 = fasterOrderBook.add(new Order(7l, 3d, 'O', 7l));
        long nanoTime8 = fasterOrderBook.add(new Order(8l, 1d, 'O', 8l));

        assertEquals(3, fasterOrderBook.getMaxLevel('B'));
        assertEquals(2, fasterOrderBook.getMaxLevel('O'));
    }
}