# Auto-Expire Cache

## Problem Overview

Design and implement a **key-value cache with automatic expiration**. The system should allow storing key-value pairs that automatically expire after a specified duration.

This is similar to [LeetCode 2622 - Cache With Time Limit](https://leetcode.com/problems/cache-with-time-limit/description/).

You'll build this incrementally across multiple parts, with each part extending the previous functionality.

### Key Design Considerations

1. **API Design**: Consider what the interface should look like, what types the inputs/outputs should be, and edge cases.
2. **Expiration Strategy**: How and when do expired entries get removed?
3. **Memory Efficiency**: How do you prevent memory leaks from expired but not-yet-removed entries?
4. **Thread Safety**: Consider how concurrent access would work in a distributed system.

### Also Known As: Log Rate Limiter

This problem is frequently asked at Netflix under the name **"Log Rate Limiter"** with a slightly different framing:

> Given a time window (e.g., 1 minute), implement a `print` function that takes an event with `name` and `timestamp`. If the same event name was seen within the time window, suppress it; otherwise, print it.

**Interview flow typically follows:**

1. Start with the basic solution using a map to store seen events and their timestamps
2. Discuss memory issues if the rate limiter runs for a long time or handles massive data
3. Discuss deletion strategies: LRU, lazy deletion, or periodic cleanup (cron job)
4. Follow-up: How would you handle out-of-order timestamps while maintaining LRU?

---

## Part 1: Basic Auto-Expire Cache

### Problem Statement

Implement an `AutoExpireCache` class with the following methods:

```python
class AutoExpireCache:
    def __init__(self):
        """Initialize the cache."""
        pass

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        """
        Store a key-value pair that expires after ttl_seconds.

        Args:
            key: The cache key
            value: The value to store (can be any type, including False/None/0)
            ttl_seconds: Time-to-live in seconds

        Notes:
            - If key already exists, override it with new value and TTL
            - TTL countdown starts from the moment set() is called
        """
        pass

    def get(self, key: str) -> Any:
        """
        Retrieve a value by key.

        Args:
            key: The cache key

        Returns:
            The value if key exists and hasn't expired, None otherwise

        Notes:
            - Return None for expired keys (not False, as False can be a valid value)
            - This is a good time to clean up the expired entry (lazy deletion)
        """
        pass
```

### API Design Discussion Points

During the interview, be prepared to discuss:

1. **Return value for cache miss**: Why return `None` instead of `False`?
   - `False` can be a valid cached value
   - Could also raise an exception, but `None` is more Pythonic for "not found"

2. **Should `set` return anything?**
   - Could return `True`/`False` for success
   - Could return the previous value if key existed
   - Keeping it simple (void) is usually fine

3. **Error handling**: Should we validate inputs?
   - Negative TTL?
   - Empty key?
   - For this interview, simple validation is sufficient

### Example Usage

```python
import time

cache = AutoExpireCache()

# Set a value with 2 second TTL
cache.set("user_session", {"user_id": 123}, 2)

# Immediately get - should return the value
print(cache.get("user_session"))  # {"user_id": 123}

# Wait for expiration
time.sleep(3)

# After expiration - should return None
print(cache.get("user_session"))  # None

# Test with False as a valid value
cache.set("feature_flag", False, 60)
result = cache.get("feature_flag")
print(result)            # False
print(result is None)    # False (it's actually the value False, not None)
```

---

### Part 1 Solution

The simplest approach uses a dictionary with expiration timestamps and **lazy deletion** (delete when accessed):

```python
import time
from typing import Any

class AutoExpireCache:
    def __init__(self):
        self.cache = {}  # key -> (value, expire_timestamp)

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        """O(1) time complexity."""
        expire_at = time.time() + ttl_seconds
        self.cache[key] = (value, expire_at)

    def get(self, key: str) -> Any:
        """O(1) time complexity with lazy deletion."""
        if key not in self.cache:
            return None

        value, expire_at = self.cache[key]

        if time.time() > expire_at:
            # Lazy deletion: remove expired entry when accessed
            del self.cache[key]
            return None

        return value
```

**Complexity Analysis:**

| Method | Time | Space          |
| ------ | ---- | -------------- |
| `set`  | O(1) | O(1) per entry |
| `get`  | O(1) | O(1)           |

### What is Lazy Deletion?

**Lazy deletion** means we don't actively clean up expired entries. Instead, we only check and remove them when they're accessed via `get()`.

**Pros:**
- Simple implementation
- No background threads or timers needed
- O(1) operations

**Cons:**
- Memory can accumulate with expired entries that are never accessed
- If keys are set but rarely/never read, they stay in memory forever

---

## Part 2: Preventing Memory Leaks

### Problem Statement

> **Interviewer**: "Your current implementation has a memory issue. If we set millions of keys that are never accessed again, they'll stay in memory forever even after expiring. How would you handle this?"

Discuss and implement strategies to actively clean up expired entries.

### Strategy 1: Periodic Cleanup (Cron Job / Background Thread)

Run a background process that periodically scans for and removes expired entries:

```python
import time
import threading
from typing import Any

class AutoExpireCache:
    def __init__(self, cleanup_interval_seconds: int = 60):
        self.cache = {}  # key -> (value, expire_timestamp)
        self.lock = threading.Lock()

        # Start background cleanup thread
        self.cleanup_interval = cleanup_interval_seconds
        self.running = True
        self.cleanup_thread = threading.Thread(target=self._cleanup_loop, daemon=True)
        self.cleanup_thread.start()

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        """O(1) time complexity."""
        expire_at = time.time() + ttl_seconds
        with self.lock:
            self.cache[key] = (value, expire_at)

    def get(self, key: str) -> Any:
        """O(1) time complexity with lazy deletion."""
        with self.lock:
            if key not in self.cache:
                return None

            value, expire_at = self.cache[key]

            if time.time() > expire_at:
                del self.cache[key]
                return None

            return value

    def _cleanup_loop(self) -> None:
        """Background thread that periodically cleans up expired entries."""
        while self.running:
            time.sleep(self.cleanup_interval)
            self._cleanup_expired()

    def _cleanup_expired(self) -> None:
        """O(N) scan to remove all expired entries."""
        current_time = time.time()
        with self.lock:
            # Create list of keys to delete (can't modify dict during iteration)
            expired_keys = [
                key for key, (_, expire_at) in self.cache.items()
                if current_time > expire_at
            ]
            for key in expired_keys:
                del self.cache[key]

    def shutdown(self) -> None:
        """Stop the background cleanup thread."""
        self.running = False
        self.cleanup_thread.join()
```

### Strategy 2: Min-Heap for Efficient Expiration

Use a min-heap to track expiration times, allowing O(log N) cleanup:

```python
import time
import heapq
import threading
from typing import Any

class AutoExpireCache:
    def __init__(self):
        self.cache = {}  # key -> (value, expire_timestamp, version)
        self.expiry_heap = []  # min-heap of (expire_timestamp, key, version)
        self.key_version = {}  # key -> current version (for invalidation)
        self.lock = threading.Lock()

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        """O(log N) due to heap push."""
        expire_at = time.time() + ttl_seconds
        with self.lock:
            # Increment version to invalidate old heap entries
            version = self.key_version.get(key, 0) + 1
            self.key_version[key] = version

            self.cache[key] = (value, expire_at, version)
            heapq.heappush(self.expiry_heap, (expire_at, key, version))

    def get(self, key: str) -> Any:
        """O(1) average case."""
        with self.lock:
            if key not in self.cache:
                return None

            value, expire_at, version = self.cache[key]

            if time.time() > expire_at:
                del self.cache[key]
                return None

            return value

    def cleanup_expired(self) -> int:
        """
        Remove all expired entries efficiently using the heap.
        Returns number of entries removed.
        O(E log N) where E is number of expired entries.
        """
        current_time = time.time()
        removed = 0

        with self.lock:
            while self.expiry_heap:
                expire_at, key, version = self.expiry_heap[0]

                if expire_at > current_time:
                    break  # No more expired entries

                heapq.heappop(self.expiry_heap)

                # Check if this is still the current version
                if key in self.cache:
                    _, _, current_version = self.cache[key]
                    if version == current_version:
                        del self.cache[key]
                        removed += 1

        return removed
```

**Why version tracking?**

When a key is updated with a new TTL, the old heap entry becomes stale. The version number helps us identify and skip these stale entries during cleanup.

### Trade-offs Discussion

| Approach      | Set      | Get  | Cleanup    | Memory Overhead |
| ------------- | -------- | ---- | ---------- | --------------- |
| Lazy only     | O(1)     | O(1) | N/A        | None            |
| Periodic scan | O(1)     | O(1) | O(N)       | Thread          |
| Min-heap      | O(log N) | O(1) | O(E log N) | Heap + versions |

---

## Part 3: Fixed-Size LRU Cache with Expiration

### Problem Statement

> **Interviewer**: "What if memory is still a concern and we need to enforce a maximum cache size? Implement a bounded cache that evicts the least recently used entries when full."

Modify the cache to have a fixed capacity and use LRU eviction:

```python
class AutoExpireCache:
    def __init__(self, capacity: int):
        """
        Initialize a bounded cache with LRU eviction.

        Args:
            capacity: Maximum number of entries the cache can hold
        """
        pass
```

### Requirements

1. **Fixed capacity**: Never exceed the specified number of entries
2. **LRU eviction**: When full, evict the least recently used entry
3. **Expiration still works**: Expired entries should still be treated as non-existent
4. **Access updates recency**: Both `get` and `set` should update the entry's recency

---

### Part 3 Solution

Use Python's `OrderedDict` which maintains insertion order and allows moving items to the end:

```python
import time
from collections import OrderedDict
from typing import Any

class AutoExpireCache:
    def __init__(self, capacity: int):
        if capacity <= 0:
            raise ValueError("Capacity must be positive")

        self.capacity = capacity
        self.cache = OrderedDict()  # key -> (value, expire_timestamp)

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        """O(1) amortized time complexity."""
        expire_at = time.time() + ttl_seconds

        # If key exists, remove it first (to update position)
        if key in self.cache:
            del self.cache[key]

        # Evict LRU if at capacity
        while len(self.cache) >= self.capacity:
            self.cache.popitem(last=False)  # Remove oldest (LRU)

        # Add new entry (at end = most recently used)
        self.cache[key] = (value, expire_at)

    def get(self, key: str) -> Any:
        """O(1) time complexity."""
        if key not in self.cache:
            return None

        value, expire_at = self.cache[key]

        if time.time() > expire_at:
            del self.cache[key]
            return None

        # Move to end (mark as most recently used)
        self.cache.move_to_end(key)

        return value

    def size(self) -> int:
        """Return current number of entries (including expired)."""
        return len(self.cache)
```

### Example Usage

```python
cache = AutoExpireCache(capacity=3)

cache.set("a", 1, 60)
cache.set("b", 2, 60)
cache.set("c", 3, 60)
print(cache.size())  # 3

# Access 'a' to make it recently used
cache.get("a")

# Add new entry, 'b' is LRU and gets evicted
cache.set("d", 4, 60)

print(cache.get("a"))  # 1 (still exists)
print(cache.get("b"))  # None (evicted)
print(cache.get("c"))  # 3 (still exists)
print(cache.get("d"))  # 4 (just added)
```

### Manual LRU Implementation (Without OrderedDict)

If the interviewer wants you to implement LRU from scratch, use a doubly-linked list with a hash map:

```python
import time
from typing import Any

class Node:
    def __init__(self, key: str, value: Any, expire_at: float):
        self.key = key
        self.value = value
        self.expire_at = expire_at
        self.prev = None
        self.next = None

class AutoExpireCache:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.cache = {}  # key -> Node

        # Dummy head and tail for easier list manipulation
        self.head = Node("", None, 0)  # Most recently used
        self.tail = Node("", None, 0)  # Least recently used
        self.head.next = self.tail
        self.tail.prev = self.head

    def _remove(self, node: Node) -> None:
        """Remove node from doubly-linked list."""
        node.prev.next = node.next
        node.next.prev = node.prev

    def _add_to_head(self, node: Node) -> None:
        """Add node right after head (most recently used position)."""
        node.prev = self.head
        node.next = self.head.next
        self.head.next.prev = node
        self.head.next = node

    def _move_to_head(self, node: Node) -> None:
        """Move existing node to most recently used position."""
        self._remove(node)
        self._add_to_head(node)

    def _evict_lru(self) -> None:
        """Remove the least recently used entry."""
        lru = self.tail.prev
        if lru != self.head:
            self._remove(lru)
            del self.cache[lru.key]

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        """O(1) time complexity."""
        expire_at = time.time() + ttl_seconds

        if key in self.cache:
            # Update existing entry
            node = self.cache[key]
            node.value = value
            node.expire_at = expire_at
            self._move_to_head(node)
        else:
            # Evict if at capacity
            if len(self.cache) >= self.capacity:
                self._evict_lru()

            # Add new entry
            node = Node(key, value, expire_at)
            self.cache[key] = node
            self._add_to_head(node)

    def get(self, key: str) -> Any:
        """O(1) time complexity."""
        if key not in self.cache:
            return None

        node = self.cache[key]

        if time.time() > node.expire_at:
            self._remove(node)
            del self.cache[key]
            return None

        self._move_to_head(node)
        return node.value
```

**Complexity Analysis:**

| Method | Time | Space          |
| ------ | ---- | -------------- |
| `set`  | O(1) | O(1) per entry |
| `get`  | O(1) | O(1)           |

---

## Complete Solutions

### Solution 1: Simple with Lazy Deletion

```python
import time
from typing import Any

class AutoExpireCache:
    def __init__(self):
        self.cache = {}

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        self.cache[key] = (value, time.time() + ttl_seconds)

    def get(self, key: str) -> Any:
        if key not in self.cache:
            return None
        value, expire_at = self.cache[key]
        if time.time() > expire_at:
            del self.cache[key]
            return None
        return value
```

### Solution 2: With Background Cleanup

```python
import time
import threading
from typing import Any

class AutoExpireCache:
    def __init__(self, cleanup_interval: int = 60):
        self.cache = {}
        self.lock = threading.Lock()
        self.running = True
        self.cleanup_thread = threading.Thread(
            target=self._cleanup_loop,
            args=(cleanup_interval,),
            daemon=True
        )
        self.cleanup_thread.start()

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        with self.lock:
            self.cache[key] = (value, time.time() + ttl_seconds)

    def get(self, key: str) -> Any:
        with self.lock:
            if key not in self.cache:
                return None
            value, expire_at = self.cache[key]
            if time.time() > expire_at:
                del self.cache[key]
                return None
            return value

    def _cleanup_loop(self, interval: int) -> None:
        while self.running:
            time.sleep(interval)
            self._cleanup_expired()

    def _cleanup_expired(self) -> None:
        current = time.time()
        with self.lock:
            expired = [k for k, (_, exp) in self.cache.items() if current > exp]
            for k in expired:
                del self.cache[k]
```

### Solution 3: LRU with Expiration (OrderedDict)

```python
import time
from collections import OrderedDict
from typing import Any

class AutoExpireCache:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.cache = OrderedDict()

    def set(self, key: str, value: Any, ttl_seconds: int) -> None:
        if key in self.cache:
            del self.cache[key]
        while len(self.cache) >= self.capacity:
            self.cache.popitem(last=False)
        self.cache[key] = (value, time.time() + ttl_seconds)

    def get(self, key: str) -> Any:
        if key not in self.cache:
            return None
        value, expire_at = self.cache[key]
        if time.time() > expire_at:
            del self.cache[key]
            return None
        self.cache.move_to_end(key)
        return value
```

---

## Follow-Up Discussion Topics

### Discussed in Interviews

1. **Why return `None` for cache miss instead of `False`?**
   - `False` can be a valid value stored in the cache
   - Alternative: raise `KeyError` or return a tuple `(found, value)`

2. **Thread safety considerations**
   - Use `threading.Lock` for Python
   - Consider read-write locks if reads >> writes
   - Discuss atomic operations and race conditions

3. **Distributed cache considerations**
   - How would this scale across multiple servers?
   - Consistency between nodes
   - Network partition handling
   - Redis/Memcached as production solutions

### Additional Extensions

1. **TTL Refresh on Access**: Should `get()` extend the TTL?
   - "Sliding expiration" vs "absolute expiration"

2. **Bulk Operations**: `mset()`, `mget()` for multiple keys

3. **Callbacks**: Notify when entries expire

4. **Statistics**: Hit rate, miss rate, eviction count

---

## Complexity Summary

| Implementation     | set      | get  | Space         | Memory Safety |
| ------------------ | -------- | ---- | ------------- | ------------- |
| Lazy deletion      | O(1)     | O(1) | O(N)          | No            |
| Background cleanup | O(1)     | O(1) | O(N) + thread | Yes           |
| Min-heap           | O(log N) | O(1) | O(N)          | Yes           |
| LRU (OrderedDict)  | O(1)     | O(1) | O(capacity)   | Yes           |
| LRU (manual)       | O(1)     | O(1) | O(capacity)   | Yes           |
