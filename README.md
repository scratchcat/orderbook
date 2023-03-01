# orderbook

I assume that the "price time priority basis" means that at a certain level (price), the oldest order is return first.

I started writing an array based implementation and soon realised that for large orderbooks (ie millions or orders), performance would be an issue, and that map based data structures would need to be used to maintain data shapes for efficient search and filtering. So I started again, implementing a faster approach. I thought it useful to compare performance, so I have maintained these two approaches as part of the solution. This array based approach became SlowOrderBook. The map based approach is the FasterOrderBook, and the one to inspect please.

I then read part B and wondered if I had included some of the answer in A. I would further use internally ConcurrentHashMap and ConcurrentSkipListMap (the concurrent version of TreeMap) of FasterOrderBook to support multi-threaded systems. I would make Order.price be BigDecimal to avoid floating point issues. I would also make Order immutable to avoid updating issues. I would also maintain a sum of sizes at each level to avoid sum-ing for each call.