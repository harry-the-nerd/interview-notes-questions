# LRU Cache

## Problem Overview

Design a data structure that follows the constraints of a **Least Recently Used (LRU) cache**.

Implement the `LRUCache` class:

- `LRUCache(int capacity)`: Initialize the cache with positive size `capacity`.
- `int get(int key)`: Return the value of `key` if it exists, otherwise return `-1`.
- `void put(int key, int value)`: Update the value if `key` already exists. Otherwise, add the key-value pair. If the cache exceeds capacity, evict the least recently used key.

Both `get` and `put` must run in `O(1)` average time.

This is the standard LeetCode 146 style LRU Cache problem.

## Example

```python
cache = LRUCache(2)
cache.put(1, 1)  # cache is {1=1}
cache.put(2, 2)  # cache is {1=1, 2=2}
cache.get(1)     # returns 1
cache.put(3, 3)  # evicts key 2
cache.get(2)     # returns -1
cache.put(4, 4)  # evicts key 1
cache.get(1)     # returns -1
cache.get(3)     # returns 3
cache.get(4)     # returns 4
```

Expected output:

```text
[null, null, null, 1, null, -1, null, -1, 3, 4]
```

## Requirements

- The cache capacity is positive.
- `get` marks the key as recently used when it exists.
- `put` marks the inserted or updated key as recently used.
- Eviction removes the least recently used key.
- At most `2 * 10^5` calls may be made to `get` and `put`.

## Solution Approach

The key observation is that the cache needs two operations in constant time:

1. Find a cache entry by key.
2. Move that entry to the most-recently-used position.

A hash map gives `O(1)` lookup by key. A doubly linked list gives `O(1)` removal and insertion when you already have the node reference. Store the least recently used item at the tail and the most recently used item at the head.

On `get(key)`:

1. If the key is missing, return `-1`.
2. Move its node to the head.
3. Return its value.

On `put(key, value)`:

1. If the key already exists, update the node and move it to the head.
2. If the key is new and the cache is full, remove the tail node and delete its key from the map.
3. Insert the new node at the head and add it to the map.

## Python Solution Using OrderedDict

Python's `OrderedDict` already maintains insertion order and supports moving keys to the end in `O(1)`.

```python
from collections import OrderedDict


class LRUCache:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.cache = OrderedDict()

    def get(self, key: int) -> int:
        if key not in self.cache:
            return -1

        self.cache.move_to_end(key)
        return self.cache[key]

    def put(self, key: int, value: int) -> None:
        if key in self.cache:
            self.cache.move_to_end(key)
        elif len(self.cache) >= self.capacity:
            self.cache.popitem(last=False)

        self.cache[key] = value
```

## Python Solution With a Doubly Linked List

In interviews, you may be asked to implement the linked-list mechanics directly instead of relying on `OrderedDict`.

```python
class Node:
    def __init__(self, key=0, value=0):
        self.key = key
        self.value = value
        self.prev = None
        self.next = None


class LRUCache:
    def __init__(self, capacity: int):
        self.capacity = capacity
        self.nodes = {}

        # Sentinels avoid edge cases when inserting/removing first or last node.
        self.head = Node()
        self.tail = Node()
        self.head.next = self.tail
        self.tail.prev = self.head

    def _remove(self, node: Node) -> None:
        node.prev.next = node.next
        node.next.prev = node.prev

    def _add_to_front(self, node: Node) -> None:
        first = self.head.next
        self.head.next = node
        node.prev = self.head
        node.next = first
        first.prev = node

    def _mark_recent(self, node: Node) -> None:
        self._remove(node)
        self._add_to_front(node)

    def get(self, key: int) -> int:
        if key not in self.nodes:
            return -1

        node = self.nodes[key]
        self._mark_recent(node)
        return node.value

    def put(self, key: int, value: int) -> None:
        if key in self.nodes:
            node = self.nodes[key]
            node.value = value
            self._mark_recent(node)
            return

        if len(self.nodes) == self.capacity:
            lru = self.tail.prev
            self._remove(lru)
            del self.nodes[lru.key]

        node = Node(key, value)
        self.nodes[key] = node
        self._add_to_front(node)
```

## Complexity

| Operation | Time | Space |
| --- | --- | --- |
| `get` | O(1) average | O(1) extra |
| `put` | O(1) average | O(1) extra |
| Cache storage | - | O(capacity) |

## Follow-Up Discussion Topics

### Why does a plain hash map not solve this?

A hash map can find values quickly, but it does not maintain recency order or identify the least recently used key in constant time.

### Why do we need a doubly linked list?

When an entry is accessed, it may be in the middle of the list. A doubly linked list lets us remove that node and reinsert it at the front in constant time.

### What happens if capacity is 1?

The same logic works. Every new key evicts the previous key. Updating the existing key should not evict it.

### How would you make this thread-safe?

Guard `get` and `put` with a lock, because both operations mutate recency order. For higher throughput, shard the cache by key hash so independent keys can use different locks.

## Test Cases

```python
cache = LRUCache(2)
cache.put(1, 1)
cache.put(2, 2)
assert cache.get(1) == 1
cache.put(3, 3)
assert cache.get(2) == -1
cache.put(4, 4)
assert cache.get(1) == -1
assert cache.get(3) == 3
assert cache.get(4) == 4

cache = LRUCache(2)
cache.put(1, 1)
cache.put(2, 2)
cache.put(1, 10)
assert cache.get(1) == 10
assert cache.get(2) == 2

cache = LRUCache(1)
cache.put(1, 1)
assert cache.get(1) == 1
cache.put(2, 2)
assert cache.get(1) == -1
assert cache.get(2) == 2
```
