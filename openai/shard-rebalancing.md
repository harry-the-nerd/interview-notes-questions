# Shard Rebalancing

## Problem Overview

You are given several shards, each covering an **inclusive integer key range**. Implement `rebalance()` so that after rebalancing:

- Each key belongs to **at most `limit` shards**
- Existing shard IDs are preserved
- The overall covered key space is preserved
- Earlier shards keep priority over later shards
- Later shards are rebalanced by moving their **start** to the right when necessary

Implement the missing `rebalance()` method:

```python
from dataclasses import dataclass


@dataclass
class Shard:
    id: str
    start: int
    end: int


class Shards:
    def __init__(self, limit: int):
        # Provided
        pass

    def add_shard(self, shard: Shard):
        # Provided
        pass

    def remove_shard(self, shard_id: str):
        # Provided
        pass

    def rebalance(self):
        # TODO
        pass
```

## Rules

Use the following rules:

1. A shard covers every integer key in the inclusive range `[start, end]`.
2. `limit >= 1` means each key may appear in at most `limit` shards after rebalancing.
3. Rebalancing may **increase a shard's `start`**, but should not invent new shard IDs or split one shard into multiple disjoint ranges.
4. The shard `end` stays unchanged in this formulation.
5. If a shard becomes empty (`start > end`), remove it.
6. Process shards in sorted order by `(start, id)`. Earlier shards get priority.
7. Preserve the original covered key space. If the input covered keys `10..30`, the rebalanced result should still cover `10..30`.

For example:

```text
limit = 1
(A, 10, 20)
(B, 10, 30)
```

should become:

```text
(A, 10, 20)
(B, 21, 30)
```

and not collapse into a brand new shard such as `(C, 10, 30)`.

## Examples

### Example 1

```text
limit = 1
(A, 0, 100)
(B, 80, 180)
```

Result:

```text
(A, 0, 100)
(B, 101, 180)
```

Keys `80..100` were duplicated, so shard `B` is shifted right until the overlap disappears.

### Example 2

```text
limit = 1
(A, 10, 20)
(B, 10, 30)
```

Result:

```text
(A, 10, 20)
(B, 21, 30)
```

### Example 3

```text
limit = 1
(A, 0, 100)
(B, 40, 110)
(C, 150, 200)
(D, 180, 250)
```

Result:

```text
(A, 0, 100)
(B, 101, 110)
(C, 150, 200)
(D, 201, 250)
```

This is **not** "four overlaps" globally. The constraint is per key: each key can appear in at most `limit` shards.

### Example 4

```text
limit = 2
(A, 0, 100)
(B, 40, 110)
(C, 80, 120)
```

Result:

```text
(A, 0, 100)
(B, 40, 110)
(C, 101, 120)
```

Keys `80..100` were covered by three shards, so `C` is shifted right until every key is in at most two shards.

## Expected Approach

Because earlier shards keep priority and later shards can only move to the right, a greedy approach still works. The important detail is:

1. Sort shards by `(start, id)`
2. Process shards in that order, keeping earlier shards fixed
3. For the current shard, look at the ranges already covered by previous kept shards
4. Find the **rightmost key** in `[start, end]` that is already covered by `limit` previous shards
5. Move the shard's `start` to one past that key
6. If the shard becomes empty, remove it

The key observation is that the current shard must avoid every key that is already "full". Moving the start to one past the rightmost saturated key is the smallest valid adjustment.

## Reference Solution

```python
from dataclasses import dataclass


@dataclass
class Shard:
    id: str
    start: int
    end: int


class Shards:
    def __init__(self, limit: int):
        if limit < 1:
            raise ValueError("limit must be >= 1")
        self.limit = limit
        self._shards: list[Shard] = []

    def add_shard(self, shard: Shard) -> None:
        self._shards.append(shard)

    def remove_shard(self, shard_id: str) -> None:
        self._shards = [shard for shard in self._shards if shard.id != shard_id]

    def _rightmost_saturated_key(
        self,
        shard: Shard,
        kept: list[Shard],
    ) -> int | None:
        events: list[tuple[int, int]] = []

        for prev in kept:
            left = max(shard.start, prev.start)
            right = min(shard.end, prev.end)
            if left > right:
                continue

            events.append((left, 1))
            events.append((right + 1, -1))

        if not events:
            return None

        events.sort()
        active = 0
        blocked_until = None
        i = 0

        while i < len(events):
            pos = events[i][0]

            while i < len(events) and events[i][0] == pos:
                active += events[i][1]
                i += 1

            next_pos = events[i][0] if i < len(events) else shard.end + 1
            if active >= self.limit and pos <= shard.end:
                blocked_until = min(shard.end, next_pos - 1)

        return blocked_until

    def rebalance(self) -> None:
        ordered = sorted(self._shards, key=lambda shard: (shard.start, shard.id))
        rebalanced: list[Shard] = []

        for shard in ordered:
            blocked_until = self._rightmost_saturated_key(shard, rebalanced)
            new_start = shard.start if blocked_until is None else blocked_until + 1

            if new_start > shard.end:
                continue

            shard.start = new_start
            rebalanced.append(shard)

        self._shards = rebalanced

    def get_shards(self) -> list[Shard]:
        return list(self._shards)
```

### Complexity

- Sorting: `O(n log n)`
- For each shard, sweeping the already-kept intervals costs up to `O(n log n)` in this reference version
- Total: `O(n^2 log n)` time
- Extra space: `O(n)`
