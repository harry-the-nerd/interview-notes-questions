# Weighted LRU Cache

## Problem Overview

Design and implement a **Weighted LRU (Least Recently Used) Cache** that extends the traditional LRU cache by assigning a size (or weight) to each item. Unlike a standard LRU cache where each item counts as 1 toward the capacity, in a weighted LRU cache, the capacity is calculated as the **sum of all item sizes**.

This variant is commonly used in systems where cached items have varying memory footprints, such as:
- Image caching (different resolutions have different file sizes)
- API response caching (responses vary in payload size)
- Database query result caching (result sets have different row counts)

### Requirements

Your implementation should support two core operations:

1. **`get(key)`**: Retrieve the value associated with the key. Returns -1 if the key doesn't exist.
2. **`put(key, value, size)`**: Insert or update a key-value pair with an associated size. If adding the item causes the total size to exceed capacity, evict the least recently used items until there's enough space.

### Example

```python
# Capacity is 10 (total weight, not item count)
cache = WeightedLRUCache(capacity=10)

cache.put("a", 1, 3)      # Cache: {"a": (1, size=3)} -> total size = 3
cache.put("b", 2, 4)      # Cache: {"a": (1, 3), "b": (2, 4)} -> total size = 7
cache.put("c", 3, 5)      # Exceeds capacity (7 + 5 = 12 > 10)
                          # Evict "a" (LRU, size=3) -> total size = 4
                          # Now add "c" -> total size = 9
                          # Cache: {"b": (2, 4), "c": (3, 5)}

cache.get("a")            # Returns -1 (evicted)
cache.get("b")            # Returns 2 (marks "b" as recently used)

cache.put("d", 4, 3)      # Would exceed (9 + 3 = 12 > 10)
                          # Evict "c" (LRU, size=5) -> total size = 4
                          # Add "d" -> total size = 7
                          # Cache: {"b": (2, 4), "d": (4, 3)}
```

---

## Part 1: Basic Implementation

### Problem Statement

Implement a `WeightedLRUCache` class with the following interface:

```python
class WeightedLRUCache:
    def __init__(self, capacity: int):
        """
        Initialize the cache with a maximum total capacity (by weight).

        Args:
            capacity: Maximum total size of all cached items
        """
        pass

    def get(self, key: str) -> int:
        """
        Get the value associated with the key.

        Returns:
            The value if key exists, -1 otherwise.
        """
        pass

    def put(self, key: str, value: int, size: int) -> None:
        """
        Insert or update a key-value pair with the given size.

        If the key already exists, update its value and size.
        If adding the item exceeds capacity, evict LRU items until there's room.
        """
        pass
```

### Test Cases

```python
# Test 1: Basic operations
cache = WeightedLRUCache(10)
cache.put("a", 1, 5)
assert cache.get("a") == 1

# Test 2: Eviction when full
cache = WeightedLRUCache(10)
cache.put("a", 1, 6)
cache.put("b", 2, 6)  # Evicts "a"
assert cache.get("a") == -1
assert cache.get("b") == 2

# Test 3: LRU ordering with get()
cache = WeightedLRUCache(10)
cache.put("a", 1, 4)
cache.put("b", 2, 4)
cache.get("a")        # "a" is now most recent
cache.put("c", 3, 4)  # Evicts "b" (LRU), not "a"
assert cache.get("b") == -1
assert cache.get("a") == 1
```

---

## Part 2: Handling Edge Cases

### Problem Statement

Extend your implementation to handle various edge cases that occur in production systems.

### Edge Cases to Handle

1. **Item larger than capacity**: When a single item's size exceeds the total capacity
2. **Update existing key with different size**: When `put()` is called with an existing key but different size
3. **Multiple evictions**: When adding one item requires evicting multiple existing items
4. **Zero-size items**: Items with size 0 (should they be allowed?)

### Example Edge Cases

```python
cache = WeightedLRUCache(10)

# Case 1: Item larger than capacity
cache.put("huge", 1, 15)  # Size 15 > capacity 10
# Options: raise error, don't insert, or clear cache and insert anyway

# Case 2: Update with different size
cache.put("a", 1, 3)
cache.put("a", 100, 8)   # Same key, different size
# Total size changes from 3 to 8

# Case 3: Multiple evictions
cache = WeightedLRUCache(10)
cache.put("a", 1, 3)
cache.put("b", 2, 3)
cache.put("c", 3, 3)     # Total = 9
cache.put("d", 4, 8)     # Needs to evict multiple items
                          # Must evict "a", "b", and "c" to fit "d"

# Case 4: Exact fit after eviction
cache = WeightedLRUCache(10)
cache.put("a", 1, 5)
cache.put("b", 2, 5)     # Total = 10 (exactly at capacity)
cache.put("c", 3, 5)     # Evict "a", add "c", total remains 10
```

### Requirements

- Define and document behavior for each edge case
- Handle updates to existing keys efficiently (adjust total size correctly)
- Evict multiple items if necessary to make room for a new item

---

## Part 3: Optimized Implementation

### Problem Statement

Optimize your implementation for O(1) time complexity for both `get()` and `put()` operations.

### Follow-Up Questions

1. What data structures would you use to achieve O(1) operations?
2. How do you maintain the LRU ordering efficiently?
3. What is the space complexity of your optimized solution?

### Requirements

- `get()` should be O(1)
- `put()` should be O(1) amortized (noting that worst case involves evicting multiple items)
- Efficiently track both the key-value mappings and the LRU ordering

---

## Solution Approach

### Clarification Questions to Ask

- Should `get()` update the item's access time (making it "most recently used")?
- Can an item's size change when updating an existing key?
- What should happen if a single item's size exceeds the total capacity?
- Are sizes guaranteed to be positive integers?
- Should the cache handle concurrent access?

### Part 1: Basic Implementation Using OrderedDict

**Strategy:**
1. Use Python's `OrderedDict` which maintains insertion order and provides O(1) access
2. Call `move_to_end()` on access to update LRU ordering
3. Track total size separately
4. Evict from the front (oldest) until there's enough space

**Example Implementation:**

```python
from collections import OrderedDict

class WeightedLRUCache:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.cache = OrderedDict()  # key -> (value, size)
        self.current_size = 0

    def get(self, key: str) -> int:
        if key not in self.cache:
            return -1

        # Move to end to mark as recently used
        self.cache.move_to_end(key)
        return self.cache[key][0]  # Return value

    def put(self, key: str, value: int, size: int) -> None:
        # If key exists, remove its size from current total
        if key in self.cache:
            old_value, old_size = self.cache[key]
            self.current_size -= old_size
            del self.cache[key]

        # Evict LRU items until we have enough space
        while self.current_size + size > self.capacity and self.cache:
            oldest_key, (oldest_value, oldest_size) = self.cache.popitem(last=False)
            self.current_size -= oldest_size

        # Add new item (if it fits)
        if size <= self.capacity:
            self.cache[key] = (value, size)
            self.current_size += size
```

**Time Complexity:**
- `get()`: O(1) - dictionary lookup and move_to_end
- `put()`: O(k) amortized where k is the number of items to evict

**Space Complexity:**
- O(n) where n is the number of cached items

---

### Part 2: Edge Case Handling

**Enhanced Implementation:**

```python
from collections import OrderedDict

class WeightedLRUCache:
    def __init__(self, capacity: int):
        if capacity <= 0:
            raise ValueError("Capacity must be positive")
        self.capacity = capacity
        self.cache = OrderedDict()
        self.current_size = 0

    def get(self, key: str) -> int:
        if key not in self.cache:
            return -1

        self.cache.move_to_end(key)
        return self.cache[key][0]

    def put(self, key: str, value: int, size: int) -> None:
        if size <= 0:
            raise ValueError("Size must be positive")

        # Handle item larger than capacity
        if size > self.capacity:
            # Option 1: Raise error
            raise ValueError(f"Item size {size} exceeds capacity {self.capacity}")
            # Option 2: Clear cache and don't insert (just return)
            # self.cache.clear()
            # self.current_size = 0
            # return

        # Remove existing key's size if present
        if key in self.cache:
            _, old_size = self.cache[key]
            self.current_size -= old_size
            del self.cache[key]

        # Evict LRU items until there's room
        while self.current_size + size > self.capacity:
            oldest_key, (_, oldest_size) = self.cache.popitem(last=False)
            self.current_size -= oldest_size

        # Add the new item
        self.cache[key] = (value, size)
        self.current_size += size

    def size(self) -> int:
        """Return current total size of cached items."""
        return self.current_size

    def __len__(self) -> int:
        """Return number of items in cache."""
        return len(self.cache)
```

**Edge Case Behaviors:**

| Edge Case           | Behavior                    | Rationale                        |
| ------------------- | --------------------------- | -------------------------------- |
| Item > capacity     | Raise ValueError            | Prevents silent failures         |
| Update existing key | Adjust size difference      | Maintains correct total          |
| Multiple evictions  | Evict until space available | Guarantees insertion if possible |
| Zero/negative size  | Raise ValueError            | Sizes must be positive           |

---

### Part 3: Doubly Linked List + HashMap (O(1) Operations)

For maximum efficiency, use a doubly linked list with a hash map:

```python
class Node:
    def __init__(self, key: str, value: int, size: int):
        self.key = key
        self.value = value
        self.size = size
        self.prev = None
        self.next = None


class WeightedLRUCache:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.current_size = 0
        self.cache = {}  # key -> Node

        # Dummy head and tail for easier manipulation
        self.head = Node("", 0, 0)  # LRU end
        self.tail = Node("", 0, 0)  # MRU end
        self.head.next = self.tail
        self.tail.prev = self.head

    def _remove(self, node: Node) -> None:
        """Remove node from linked list."""
        node.prev.next = node.next
        node.next.prev = node.prev

    def _add_to_end(self, node: Node) -> None:
        """Add node to the end (most recently used)."""
        node.prev = self.tail.prev
        node.next = self.tail
        self.tail.prev.next = node
        self.tail.prev = node

    def _evict_lru(self) -> Node:
        """Remove and return the LRU node."""
        lru = self.head.next
        self._remove(lru)
        return lru

    def get(self, key: str) -> int:
        if key not in self.cache:
            return -1

        node = self.cache[key]
        # Move to end (most recently used)
        self._remove(node)
        self._add_to_end(node)
        return node.value

    def put(self, key: str, value: int, size: int) -> None:
        if size > self.capacity:
            raise ValueError(f"Item size {size} exceeds capacity {self.capacity}")

        # If key exists, remove it first
        if key in self.cache:
            old_node = self.cache[key]
            self.current_size -= old_node.size
            self._remove(old_node)
            del self.cache[key]

        # Evict LRU items until there's room
        while self.current_size + size > self.capacity and self.head.next != self.tail:
            lru = self._evict_lru()
            self.current_size -= lru.size
            del self.cache[lru.key]

        # Add new node
        new_node = Node(key, value, size)
        self.cache[key] = new_node
        self._add_to_end(new_node)
        self.current_size += size
```

**Complexity Analysis:**

| Operation                  | Time Complexity | Notes                                |
| -------------------------- | --------------- | ------------------------------------ |
| `get()`                    | O(1)            | HashMap lookup + linked list reorder |
| `put()` (no eviction)      | O(1)            | HashMap insert + linked list append  |
| `put()` (with k evictions) | O(k)            | Must evict k items                   |

**Why Doubly Linked List + HashMap?**

- **HashMap**: O(1) lookup by key
- **Doubly Linked List**: O(1) removal and insertion at both ends
- **Combined**: O(1) for moving any element to MRU position

---

## Follow-Up Discussion Topics

### Alternative Approaches

**Approach 1: Segment-based caching**
- Divide capacity into fixed-size segments
- Round item sizes to nearest segment multiple
- Trade-off: simpler eviction, potential space waste

**Approach 2: Weighted LFU (Least Frequently Used)**
- Instead of recency, track access frequency weighted by size
- Evict items with lowest (frequency / size) ratio
- Better for stable access patterns

### Production Considerations

1. **Thread Safety**
   - Use locks around cache operations
   - Consider read-write locks for read-heavy workloads
   - Or use lock-free data structures for maximum concurrency

2. **Size Estimation**
   - How to accurately measure item size in memory?
   - Should size include metadata overhead?
   - Use `sys.getsizeof()` or custom size calculators

3. **Monitoring**
   - Track hit rate, eviction rate
   - Monitor capacity utilization
   - Alert on unusually large items

4. **TTL (Time-To-Live)**
   - Extend to support item expiration
   - Combine LRU with TTL-based eviction

### Comparison with Standard LRU

| Aspect            | Standard LRU        | Weighted LRU          |
| ----------------- | ------------------- | --------------------- |
| Capacity unit     | Item count          | Total size/weight     |
| Evictions per put | At most 1           | Potentially multiple  |
| Use case          | Uniform item sizes  | Variable item sizes   |
| Implementation    | Simpler             | Slightly more complex |
| Memory efficiency | Lower (fixed slots) | Higher (flexible)     |
