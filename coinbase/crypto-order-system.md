# Crypto Order System


## Problem Overview

Design and implement a **crypto trading order management system**. The system processes a stream of orders, supports lifecycle operations (place, pause, resume, cancel), and displays all live orders with their current status.

You'll build this incrementally across multiple parts, with each part extending the previous functionality.

## Part 1: Basic Order Management

### Problem Statement

Implement a `CryptoOrderSystem` class that manages a stream of trading orders. Each order has an ID, and can be placed, paused, resumed, or cancelled.

```python
class CryptoOrderSystem:
    def __init__(self):
        """Initialize the order system."""
        pass

    def place(self, order_id: str, currency: str, amount: float, price: float) -> None:
        """
        Place a new order into the system.

        Args:
            order_id: Unique identifier for the order
            currency: The cryptocurrency (e.g., "BTC", "ETH")
            amount: Quantity to trade
            price: Price per unit

        Notes:
            - The order starts in ACTIVE status
            - If order_id already exists, ignore the duplicate
        """
        pass

    def pause(self, order_id: str) -> bool:
        """
        Pause an active order.

        Returns:
            True if the order was paused, False if the order doesn't exist
            or is not in ACTIVE status.

        Notes:
            - Only ACTIVE orders can be paused
        """
        pass

    def resume(self, order_id: str) -> bool:
        """
        Resume a paused order.

        Returns:
            True if the order was resumed, False if the order doesn't exist
            or is not in PAUSED status.

        Notes:
            - Only PAUSED orders can be resumed
        """
        pass

    def cancel(self, order_id: str) -> bool:
        """
        Cancel an order and remove it from the system entirely.

        Returns:
            True if the order was cancelled, False if the order doesn't exist.

        Notes:
            - Cancelled orders should be fully removed from memory
            - Both ACTIVE and PAUSED orders can be cancelled
        """
        pass

    def display_all_live_orders(self) -> list:
        """
        Return all live orders with their current status.

        Returns:
            A list of strings in the format:
            ["order1", "order2(paused)", "order3", "order4(paused)"]

        Notes:
            - Orders should appear in the order they were placed
            - Paused orders should have "(paused)" appended
            - Cancelled orders should NOT appear
        """
        pass
```

### State Machine

```
    place()         pause()
  ---------> ACTIVE --------> PAUSED
                |                |
                | cancel()       | resume() -> ACTIVE
                |                |
                v                | cancel()
             REMOVED            v
              (from          REMOVED
             memory)          (from
                             memory)
```

### Example Usage

```python
system = CryptoOrderSystem()

system.place("order1", "BTC", 10, 50000.0)
system.place("order2", "ETH", 100, 3000.0)
system.place("order3", "BTC", 5, 51000.0)
system.place("order4", "ETH", 50, 3100.0)

system.pause("order2")
system.pause("order3")

print(system.display_all_live_orders())
# ["order1", "order2(paused)", "order3(paused)", "order4"]

system.cancel("order2")

print(system.display_all_live_orders())
# ["order1", "order3(paused)", "order4"]
```

---

### Part 1 Solution

```python
from enum import Enum
from collections import OrderedDict

class OrderStatus(Enum):
    ACTIVE = "active"
    PAUSED = "paused"

class Order:
    def __init__(self, order_id: str, currency: str, amount: float, price: float):
        self.order_id = order_id
        self.currency = currency
        self.amount = amount
        self.price = price
        self.status = OrderStatus.ACTIVE

class CryptoOrderSystem:
    def __init__(self):
        self.orders = OrderedDict()  # order_id -> Order (preserves insertion order)

    def place(self, order_id: str, currency: str, amount: float, price: float) -> None:
        if order_id in self.orders:
            return  # Ignore duplicate
        self.orders[order_id] = Order(order_id, currency, amount, price)

    def pause(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        if order.status != OrderStatus.ACTIVE:
            return False
        order.status = OrderStatus.PAUSED
        return True

    def resume(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        if order.status != OrderStatus.PAUSED:
            return False
        order.status = OrderStatus.ACTIVE
        return True

    def cancel(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        del self.orders[order_id]
        return True

    def display_all_live_orders(self) -> list:
        result = []
        for order in self.orders.values():
            if order.status == OrderStatus.PAUSED:
                result.append(f"{order.order_id}(paused)")
            else:
                result.append(order.order_id)
        return result
```

**Complexity Analysis:**

| Method                    | Time | Space          |
| ------------------------- | ---- | -------------- |
| `place`                   | O(1) | O(1) per order |
| `pause`                   | O(1) | O(1)           |
| `resume`                  | O(1) | O(1)           |
| `cancel`                  | O(1) | O(1)           |
| `display_all_live_orders` | O(N) | O(N)           |

## Part 2: User-Level Operations

### Problem Statement

> **Interviewer**: "Now we need to track which user placed each order. Add support for cancelling all orders for a given user."

Extend the system to associate each order with a user and support bulk cancellation by user ID.

```python
def place(self, order_id: str, user_id: str, currency: str, amount: float, price: float) -> None:
    """Place a new order, now associated with a user."""
    pass

def cancel_all_by_user(self, user_id: str) -> int:
    """
    Cancel all orders placed by the given user.

    Returns:
        The number of orders cancelled.

    Notes:
        - All of the user's orders should be removed from memory
        - The user's tracking data should also be cleaned up
    """
    pass
```

### Design Discussion Points

1. **Additional data structure**: We need a `user_id -> set(order_ids)` mapping for efficient per-user lookups
2. **Memory cleanup**: When cancelling by user, we must clean up both the order map AND the user map
3. **Consistency**: The `cancel()` method must also update the user map

---

### Part 2 Solution

```python
from enum import Enum
from collections import OrderedDict, defaultdict

class OrderStatus(Enum):
    ACTIVE = "active"
    PAUSED = "paused"

class Order:
    def __init__(self, order_id: str, user_id: str, currency: str, amount: float, price: float):
        self.order_id = order_id
        self.user_id = user_id
        self.currency = currency
        self.amount = amount
        self.price = price
        self.status = OrderStatus.ACTIVE

class CryptoOrderSystem:
    def __init__(self):
        self.orders = OrderedDict()       # order_id -> Order
        self.user_orders = defaultdict(set)  # user_id -> set of order_ids

    def place(self, order_id: str, user_id: str, currency: str, amount: float, price: float) -> None:
        if order_id in self.orders:
            return
        order = Order(order_id, user_id, currency, amount, price)
        self.orders[order_id] = order
        self.user_orders[user_id].add(order_id)

    def pause(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        if order.status != OrderStatus.ACTIVE:
            return False
        order.status = OrderStatus.PAUSED
        return True

    def resume(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        if order.status != OrderStatus.PAUSED:
            return False
        order.status = OrderStatus.ACTIVE
        return True

    def cancel(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        # Clean up user mapping
        self.user_orders[order.user_id].discard(order_id)
        if not self.user_orders[order.user_id]:
            del self.user_orders[order.user_id]
        del self.orders[order_id]
        return True

    def cancel_all_by_user(self, user_id: str) -> int:
        if user_id not in self.user_orders:
            return 0
        order_ids = list(self.user_orders[user_id])  # Copy since we'll modify
        for order_id in order_ids:
            del self.orders[order_id]
        del self.user_orders[user_id]
        return len(order_ids)

    def display_all_live_orders(self) -> list:
        result = []
        for order in self.orders.values():
            if order.status == OrderStatus.PAUSED:
                result.append(f"{order.order_id}(paused)")
            else:
                result.append(order.order_id)
        return result
```

**Key Change:** The `cancel()` method must now also clean up the `user_orders` mapping to prevent memory leaks. This is a common follow-up the interviewer probes for — forgetting to clean up the secondary index.

**Complexity Analysis:**

| Method               | Time | Space          |
| -------------------- | ---- | -------------- |
| `place`              | O(1) | O(1) per order |
| `cancel`             | O(1) | O(1)           |
| `cancel_all_by_user` | O(K) | O(1)           |

Where K = number of orders belonging to the user.

## Part 3: Sharding by User ID

### Problem Statement

> **Interviewer**: "We can no longer store all orders in a single stream. We need to shard orders across multiple streams. All of a user's orders must go into the same stream for consistency. How would you design and implement this?"

Modify the system to distribute orders across `N` streams using user-based sharding:

```python
class CryptoOrderSystem:
    def __init__(self, num_streams: int):
        """
        Initialize the system with multiple streams.

        Args:
            num_streams: Number of streams to shard across.

        Notes:
            - Use hash(user_id) % num_streams to determine which stream
              a user's orders belong to
            - All orders from the same user must be in the same stream
        """
        pass
```

### Requirements

1. **Consistent sharding**: `hash(user_id) % num_streams` determines the target stream
2. **All operations** must route to the correct stream
3. **`display_all_live_orders`** now returns orders from all streams (merged)
4. **Same user, same stream**: Guarantees ordering consistency for per-user operations

### Design Discussion Points

1. **Why shard by user?** — Ensures all of a user's orders are co-located, making `cancel_all_by_user` efficient (only touches one stream)
2. **Hash function choice** — Python's built-in `hash()` works for demonstration; in production, use consistent hashing
3. **Stream independence** — Each stream can be processed by a different worker/thread in a distributed system
4. **Rebalancing** — What happens if you change `num_streams`? (Consistent hashing discussion)

---

### Part 3 Solution

```python
from enum import Enum
from collections import OrderedDict, defaultdict

class OrderStatus(Enum):
    ACTIVE = "active"
    PAUSED = "paused"

class Order:
    def __init__(self, order_id: str, user_id: str, currency: str, amount: float, price: float):
        self.order_id = order_id
        self.user_id = user_id
        self.currency = currency
        self.amount = amount
        self.price = price
        self.status = OrderStatus.ACTIVE

class Stream:
    """A single shard that manages a subset of orders."""
    def __init__(self):
        self.orders = OrderedDict()          # order_id -> Order
        self.user_orders = defaultdict(set)  # user_id -> set of order_ids

    def place(self, order: Order) -> None:
        if order.order_id in self.orders:
            return
        self.orders[order.order_id] = order
        self.user_orders[order.user_id].add(order.order_id)

    def pause(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        if order.status != OrderStatus.ACTIVE:
            return False
        order.status = OrderStatus.PAUSED
        return True

    def resume(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        if order.status != OrderStatus.PAUSED:
            return False
        order.status = OrderStatus.ACTIVE
        return True

    def cancel(self, order_id: str) -> bool:
        if order_id not in self.orders:
            return False
        order = self.orders[order_id]
        self.user_orders[order.user_id].discard(order_id)
        if not self.user_orders[order.user_id]:
            del self.user_orders[order.user_id]
        del self.orders[order_id]
        return True

    def cancel_all_by_user(self, user_id: str) -> list:
        if user_id not in self.user_orders:
            return []
        order_ids = list(self.user_orders[user_id])
        for order_id in order_ids:
            del self.orders[order_id]
        del self.user_orders[user_id]
        return order_ids

    def get_live_orders(self) -> list:
        result = []
        for order in self.orders.values():
            if order.status == OrderStatus.PAUSED:
                result.append(f"{order.order_id}(paused)")
            else:
                result.append(order.order_id)
        return result


class CryptoOrderSystem:
    def __init__(self, num_streams: int):
        self.num_streams = num_streams
        self.streams = [Stream() for _ in range(num_streams)]
        self.order_to_stream = {}  # order_id -> stream_index (for non-user operations)

    def _get_stream_index(self, user_id: str) -> int:
        """Determine which stream a user's orders belong to."""
        return hash(user_id) % self.num_streams

    def place(self, order_id: str, user_id: str, currency: str, amount: float, price: float) -> None:
        if order_id in self.order_to_stream:
            return  # Ignore duplicate
        stream_idx = self._get_stream_index(user_id)
        order = Order(order_id, user_id, currency, amount, price)
        self.streams[stream_idx].place(order)
        self.order_to_stream[order_id] = stream_idx

    def pause(self, order_id: str) -> bool:
        if order_id not in self.order_to_stream:
            return False
        stream_idx = self.order_to_stream[order_id]
        return self.streams[stream_idx].pause(order_id)

    def resume(self, order_id: str) -> bool:
        if order_id not in self.order_to_stream:
            return False
        stream_idx = self.order_to_stream[order_id]
        return self.streams[stream_idx].resume(order_id)

    def cancel(self, order_id: str) -> bool:
        if order_id not in self.order_to_stream:
            return False
        stream_idx = self.order_to_stream[order_id]
        result = self.streams[stream_idx].cancel(order_id)
        if result:
            del self.order_to_stream[order_id]
        return result

    def cancel_all_by_user(self, user_id: str) -> int:
        stream_idx = self._get_stream_index(user_id)
        cancelled_ids = self.streams[stream_idx].cancel_all_by_user(user_id)
        for order_id in cancelled_ids:
            del self.order_to_stream[order_id]
        return len(cancelled_ids)

    def display_all_live_orders(self) -> list:
        """Merge live orders from all streams."""
        result = []
        for stream in self.streams:
            result.extend(stream.get_live_orders())
        return result
```

### Example Usage

```python
system = CryptoOrderSystem(num_streams=3)

# User "alice" and "bob" may land on different streams
system.place("order1", "alice", "BTC", 10, 50000.0)
system.place("order2", "alice", "ETH", 100, 3000.0)
system.place("order3", "bob", "BTC", 5, 51000.0)
system.place("order4", "bob", "ETH", 50, 3100.0)

system.pause("order2")

print(system.display_all_live_orders())
# All live orders across all streams (order may vary by stream assignment)

# Cancel all of alice's orders — only touches one stream
cancelled = system.cancel_all_by_user("alice")
print(f"Cancelled {cancelled} orders")  # 2

print(system.display_all_live_orders())
# Only bob's orders remain
```

**Why this design works:**

1. **Same user, same stream**: `hash(user_id) % num_streams` guarantees co-location
2. **Efficient per-user ops**: `cancel_all_by_user` only touches one stream
3. **Parallel processing**: Each stream can be handled by a separate worker/thread
4. **Order-level routing**: The `order_to_stream` map lets us route `pause`/`resume`/`cancel` by order ID without knowing the user

## Follow-Up Discussion Topics

### Discussed in Interviews

1. **Sharding strategy**: Why `hash(user_id) % N` and not `hash(order_id) % N`?
   - User-based sharding keeps all user orders together
   - Makes `cancel_all_by_user` a single-stream operation
   - Trade-off: potential hot spots if one user has many orders

2. **Consistent hashing**: What happens when you add or remove streams?
   - Simple modulo requires reshuffling all data
   - Consistent hashing minimizes data movement
   - Virtual nodes for better load distribution

3. **Out-of-order events**: What if events arrive with out-of-order timestamps?
   - Buffer events and process in timestamp order
   - Time-windowed processing (e.g., process all events in 5-second windows)
   - Queue-based solution with a priority queue sorted by timestamp

4. **Thread safety**: If each stream is processed by a separate worker, what synchronization is needed?
   - Per-stream locks (not a global lock)
   - The `order_to_stream` lookup needs concurrent access protection

## Complexity Summary

| Implementation      | place | pause/resume | cancel | cancel_all_by_user | display_all |
| ------------------- | ----- | ------------ | ------ | ------------------ | ----------- |
| Single stream       | O(1)  | O(1)         | O(1)   | O(K)               | O(N)        |
| Sharded (S streams) | O(1)  | O(1)         | O(1)   | O(K)               | O(N)        |

Where N = total orders, K = orders per user, S = number of streams.

Space complexity: O(N) for orders + O(U) for user index, where U = number of users.
