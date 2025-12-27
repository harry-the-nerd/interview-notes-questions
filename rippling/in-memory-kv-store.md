# In-Memory Key-Value Store with Transactions

## Problem Overview

Design and implement an **in-memory key-value datastore** that supports basic CRUD operations and transactions. This is a classic interview question that tests your understanding of data structures, state management, and transaction semantics.

### Key Design Considerations

1. **Correctness**: Transaction isolation must be handled correctly - uncommitted changes should not affect global state.
2. **State Management**: How to efficiently track changes within a transaction while maintaining the ability to rollback.
3. **Extensibility**: The design should naturally extend from single transactions to nested transactions.

---

## Part 1: Basic Key-Value Store

### Problem Statement

Implement an in-memory key-value datastore that supports the following operations:

```python
class Database:
    def set(self, key: str, value: str) -> None:
        """Set key to value."""
        pass
    
    def get(self, key: str) -> str | None:
        """Get value for key. Returns None if key doesn't exist."""
        pass
    
    def delete(self, key: str) -> bool:
        """Delete key. Returns True if key existed, False otherwise."""
        pass
```

### Example Usage

```python
db = Database()
db.set("key1", "val1")
print(db.get("key1"))    # Output: val1
print(db.get("key2"))    # Output: None
db.delete("key1")
print(db.get("key1"))    # Output: None
```

### Part 1 Solution

This is straightforward - use a dictionary (hash map) for O(1) operations:

```python
class Database:
    def __init__(self):
        self._store: dict[str, str] = {}
    
    def set(self, key: str, value: str) -> None:
        """Set key to value."""
        self._store[key] = value
    
    def get(self, key: str) -> str | None:
        """Get value for key. Returns None if key doesn't exist."""
        return self._store.get(key)
    
    def delete(self, key: str) -> bool:
        """Delete key. Returns True if key existed, False otherwise."""
        if key in self._store:
            del self._store[key]
            return True
        return False
```

**Time & Space Complexity:**

| Operation | Time | Space                         |
| --------- | ---- | ----------------------------- |
| set       | O(1) | O(1)                          |
| get       | O(1) | O(1)                          |
| delete    | O(1) | O(1)                          |
| Overall   | -    | O(n) where n = number of keys |

---

## Part 2: Single Transaction Support

### Problem Statement

> **Interviewer**: "Now let's add support for transactions. We need `begin()`, `commit()`, and `rollback()` methods."

Add transaction support with the following semantics:

- **`begin()`**: Start a new transaction context
- **`commit()`**: Persist all changes made in the transaction to the global store
- **`rollback()`**: Discard all changes made in the transaction
- Every `begin()` will always be followed by exactly one `commit()` or `rollback()`
- Reads inside a transaction should see uncommitted changes made within that transaction

### Example 1: Commit

```python
db = Database()
db.set("key0", "val0")
print(db.get("key0"))    # Output: val0 (set outside transaction)

db.begin()
print(db.get("key0"))    # Output: val0 (visible from global store)
db.set("key1", "val1")
print(db.get("key1"))    # Output: val1 (uncommitted, but visible in transaction)
db.commit()

print(db.get("key1"))    # Output: val1 (persisted after commit)
```

### Example 2: Rollback

```python
db = Database()
db.begin()
db.set("key2", "val2")
print(db.get("key2"))    # Output: val2 (uncommitted, visible in transaction)
db.rollback()

print(db.get("key2"))    # Output: None (changes discarded)
```

### Design Discussion

Before implementing, consider:

1. **How to isolate transaction changes?**
   - Option A: Copy entire store on begin (expensive for large stores)
   - Option B: Maintain a separate "pending changes" map (more efficient)

2. **How to handle reads?**
   - First check pending changes, then fall back to global store

3. **How to handle deletes?**
   - Need a way to distinguish "key deleted in transaction" from "key doesn't exist"
   - Use a sentinel value or a separate "deleted keys" set

### Part 2 Solution

Key insight: Use a **transaction layer** that tracks only the changes, not a full copy of the store.

```python
class Database:
    # Sentinel value to mark deleted keys
    _DELETED = object()
    
    def __init__(self):
        self._store: dict[str, str] = {}
        self._transaction: dict[str, str | object] | None = None
    
    def set(self, key: str, value: str) -> None:
        """Set key to value."""
        if self._transaction is not None:
            self._transaction[key] = value
        else:
            self._store[key] = value
    
    def get(self, key: str) -> str | None:
        """Get value for key. Returns None if key doesn't exist."""
        if self._transaction is not None:
            if key in self._transaction:
                value = self._transaction[key]
                # Check if key was deleted in transaction
                if value is self._DELETED:
                    return None
                return value
        # Fall back to global store
        return self._store.get(key)
    
    def delete(self, key: str) -> bool:
        """Delete key. Returns True if key existed, False otherwise."""
        # Check if key exists (considering transaction layer)
        exists = self.get(key) is not None
        
        if self._transaction is not None:
            # Mark as deleted in transaction
            self._transaction[key] = self._DELETED
        else:
            if key in self._store:
                del self._store[key]
        
        return exists
    
    def begin(self) -> None:
        """Start a new transaction."""
        self._transaction = {}
    
    def commit(self) -> None:
        """Commit the current transaction."""
        if self._transaction is None:
            raise RuntimeError("No active transaction")
        
        # Apply all changes to global store
        for key, value in self._transaction.items():
            if value is self._DELETED:
                self._store.pop(key, None)
            else:
                self._store[key] = value
        
        self._transaction = None
    
    def rollback(self) -> None:
        """Rollback the current transaction."""
        if self._transaction is None:
            raise RuntimeError("No active transaction")
        
        # Simply discard the transaction layer
        self._transaction = None
```

**Key Design Decisions:**

1. **Sentinel Value for Deletes**: Using `_DELETED = object()` creates a unique marker that cannot collide with any valid value.

2. **Lazy Copying**: We don't copy the entire store on `begin()`. Only modified keys are tracked in `_transaction`.

3. **Read Path**: Check transaction layer first, then global store. This provides read-your-writes consistency within a transaction.

**Time & Space Complexity:**

| Operation      | Time | Space |
| -------------- | ---- | ----- |
| begin          | O(1) | O(1)  |
| commit         | O(t) | O(1)  |
| rollback       | O(1) | O(1)  |
| set/get/delete | O(1) | O(1)  |

Where t = number of keys modified in the transaction.

---

## Part 3: Nested Transactions

### Problem Statement

> **Interviewer**: "Great! Now let's support nested transactions. Multiple transactions can be active at once, and operations affect the innermost (most recent) transaction."

Extend your solution to support nested transactions with these semantics:

- A child transaction **inherits** visible state from its parent
- Changes in a child transaction are visible to that child
- When a child **commits**, its changes merge into the parent (not global store)
- When a child **rollbacks**, its changes are discarded, parent is unaffected
- Only when the **outermost transaction commits** do changes persist to global store
- If a parent is rolled back, all child changes are also discarded

### Example: Nested Transactions

```python
db = Database()
db.begin()                  # Transaction 1 (parent)
db.set("key1", "val1")
db.set("key2", "val2")

db.begin()                  # Transaction 2 (child)
print(db.get("key1"))       # Output: val1 (inherited from parent)
db.set("key1", "val1_child")
db.commit()                 # Commit child -> merges into parent

print(db.get("key1"))       # Output: val1_child (from committed child)
print(db.get("key2"))       # Output: val2 (from parent)
db.commit()                 # Commit parent -> persists to global store

print(db.get("key1"))       # Output: val1_child (persisted globally)
print(db.get("key2"))       # Output: val2 (persisted globally)
```

### Example: Child Rollback

```python
db = Database()
db.begin()                  # Transaction 1 (parent)
db.set("key1", "val1")

db.begin()                  # Transaction 2 (child)
db.set("key1", "override")
print(db.get("key1"))       # Output: override
db.rollback()               # Rollback child

print(db.get("key1"))       # Output: val1 (child changes discarded)
db.commit()                 # Commit parent

print(db.get("key1"))       # Output: val1 (only parent changes persisted)
```

### Example: Parent Rollback

```python
db = Database()
db.begin()                  # Transaction 1 (parent)
db.set("key1", "val1")

db.begin()                  # Transaction 2 (child)
db.set("key2", "val2")
db.commit()                 # Commit child -> merges into parent

print(db.get("key1"))       # Output: val1
print(db.get("key2"))       # Output: val2
db.rollback()               # Rollback parent -> discards everything

print(db.get("key1"))       # Output: None (all changes discarded)
print(db.get("key2"))       # Output: None (child commit was only to parent)
```

### Design Discussion

The key insight is to use a **stack of transaction layers**:

```
┌─────────────────────┐
│ Transaction 3 (top) │  <- Current/innermost
├─────────────────────┤
│ Transaction 2       │
├─────────────────────┤
│ Transaction 1       │
├─────────────────────┤
│ Global Store        │  <- Base layer
└─────────────────────┘
```

- **Write**: Always write to the top of the stack
- **Read**: Search from top to bottom, return first match
- **Commit**: Pop top and merge into the layer below
- **Rollback**: Pop top and discard

### Part 3 Solution

```python
class Database:
    _DELETED = object()
    
    def __init__(self):
        self._store: dict[str, str] = {}
        self._transaction_stack: list[dict[str, str | object]] = []
    
    def _current_transaction(self) -> dict[str, str | object] | None:
        """Get the current (innermost) transaction, or None if no transaction."""
        return self._transaction_stack[-1] if self._transaction_stack else None
    
    def set(self, key: str, value: str) -> None:
        """Set key to value in the current transaction or global store."""
        current = self._current_transaction()
        if current is not None:
            current[key] = value
        else:
            self._store[key] = value
    
    def get(self, key: str) -> str | None:
        """
        Get value for key by searching from innermost transaction to global store.
        """
        # Search transaction stack from top (newest) to bottom (oldest)
        for transaction in reversed(self._transaction_stack):
            if key in transaction:
                value = transaction[key]
                if value is self._DELETED:
                    return None
                return value
        
        # Fall back to global store
        return self._store.get(key)
    
    def delete(self, key: str) -> bool:
        """Delete key in the current transaction."""
        exists = self.get(key) is not None
        
        current = self._current_transaction()
        if current is not None:
            current[key] = self._DELETED
        else:
            if key in self._store:
                del self._store[key]
        
        return exists
    
    def begin(self) -> None:
        """Start a new nested transaction."""
        self._transaction_stack.append({})
    
    def commit(self) -> None:
        """
        Commit the current transaction.
        - If nested: merge into parent transaction
        - If outermost: persist to global store
        """
        if not self._transaction_stack:
            raise RuntimeError("No active transaction")
        
        current = self._transaction_stack.pop()
        
        if self._transaction_stack:
            # Merge into parent transaction
            parent = self._transaction_stack[-1]
            for key, value in current.items():
                parent[key] = value
        else:
            # Persist to global store
            for key, value in current.items():
                if value is self._DELETED:
                    self._store.pop(key, None)
                else:
                    self._store[key] = value
    
    def rollback(self) -> None:
        """Rollback the current transaction (discard its changes)."""
        if not self._transaction_stack:
            raise RuntimeError("No active transaction")
        
        # Simply pop and discard
        self._transaction_stack.pop()
```

### Walkthrough: How It Works

Let's trace through a nested transaction example:

```python
db = Database()
# State: _store = {}, _transaction_stack = []

db.set("global", "value")
# State: _store = {"global": "value"}, _transaction_stack = []

db.begin()
# State: _store = {"global": "value"}, _transaction_stack = [{}]

db.set("key1", "v1")
# State: _store = {"global": "value"}
#        _transaction_stack = [{"key1": "v1"}]

db.begin()
# State: _store = {"global": "value"}
#        _transaction_stack = [{"key1": "v1"}, {}]

db.set("key1", "v1_child")
db.set("key2", "v2")
# State: _store = {"global": "value"}
#        _transaction_stack = [{"key1": "v1"}, {"key1": "v1_child", "key2": "v2"}]

db.get("key1")  # Returns "v1_child" (found in stack[-1])
db.get("global")  # Returns "value" (not in any transaction, found in _store)

db.commit()  # Commit child
# Merge stack[-1] into stack[-2]:
# State: _store = {"global": "value"}
#        _transaction_stack = [{"key1": "v1_child", "key2": "v2"}]

db.commit()  # Commit parent
# Persist stack[-1] to _store:
# State: _store = {"global": "value", "key1": "v1_child", "key2": "v2"}
#        _transaction_stack = []
```

### Time & Space Complexity

| Operation  | Time | Space |
| ---------- | ---- | ----- |
| begin      | O(1) | O(1)  |
| commit     | O(t) | O(1)  |
| rollback   | O(1) | O(1)  |
| get        | O(d) | O(1)  |
| set/delete | O(1) | O(1)  |

Where:
- t = number of keys modified in the committed transaction
- d = depth of transaction stack (number of nested transactions)

**Total Space**: O(n + m) where n = keys in global store, m = total keys across all transaction layers

---

## Alternative Approaches

### Approach 1: Full Copy on Begin

Instead of tracking only changes, copy the entire state when a transaction begins:

```python
def begin(self):
    # Make a full copy of current state
    if self._transaction_stack:
        current_state = dict(self._transaction_stack[-1])
    else:
        current_state = dict(self._store)
    self._transaction_stack.append(current_state)
```

**Pros:**
- Simpler reads (just check one layer)
- No need for `_DELETED` sentinel

**Cons:**
- O(n) time and space for each `begin()` where n = total keys
- Not scalable for large datasets

### Approach 2: Undo Log

Track inverse operations to rollback:

```python
def begin(self):
    self._undo_stack.append([])  # Log of undo operations

def set(self, key, value):
    current_undo = self._undo_stack[-1] if self._undo_stack else None
    if current_undo is not None:
        # Log the inverse operation
        old_value = self.get(key)
        if old_value is None:
            current_undo.append(("delete", key))
        else:
            current_undo.append(("set", key, old_value))
    # Perform the actual set
    self._store[key] = value

def rollback(self):
    for operation in reversed(self._undo_stack.pop()):
        if operation[0] == "delete":
            del self._store[operation[1]]
        else:  # set
            self._store[operation[1]] = operation[2]
```

**Pros:**
- Changes are immediately visible (no layered lookup)

**Cons:**
- Complex to implement correctly
- Rollback can be expensive: O(t) where t = operations in transaction

---

## Edge Cases to Consider

1. **Commit/Rollback without Begin**
   ```python
   db.commit()  # Should raise RuntimeError
   ```

2. **Deleting a Non-Existent Key**
   ```python
   db.delete("nonexistent")  # Should return False, no error
   ```

3. **Re-setting a Deleted Key**
   ```python
   db.begin()
   db.set("key", "val1")
   db.delete("key")
   db.set("key", "val2")  # Should work
   db.get("key")  # Should return "val2"
   db.commit()
   ```

4. **Deeply Nested Transactions**
   ```python
   for _ in range(1000):
       db.begin()
   # Should handle gracefully
   ```

5. **Empty Transactions**
   ```python
   db.begin()
   db.commit()  # Valid, just no changes
   ```

---

## Follow-Up Discussion Topics

### 1. Thread Safety

> **Interviewer**: "How would you make this thread-safe?"

Options:
- **Global Lock**: Simple but limits concurrency
- **Read-Write Lock**: Allow concurrent reads, exclusive writes
- **Per-Transaction Isolation**: Each thread gets its own transaction

### 2. Persistence

> **Interviewer**: "How would you persist to disk?"

- Write-ahead logging (WAL) for durability
- Snapshot + WAL for recovery
- Background flushing for performance

### 3. Memory Limits

> **Interviewer**: "What if the dataset is larger than memory?"

- LRU eviction policy
- Disk-backed storage with memory cache
- Compression for cold data

### 4. Transaction Timeout

> **Interviewer**: "What if a transaction runs forever?"

- Add timeout parameter to `begin()`
- Background thread to abort long-running transactions
- Resource cleanup on timeout
