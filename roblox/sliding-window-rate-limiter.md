# Sliding-Window Rate Limiter

## Problem Overview

Implement a rate limiter over a stream of request timestamps. The interview usually starts with a single global stream, then adds user and experience identifiers. A request is allowed only if accepting it would not exceed the configured request count inside the sliding window.

Reported variants call this "Rate Limiter", "Log Rate Limiter", or a LeetCode 359 / 362-style rate limiter. The Roblox version is not exactly either LeetCode problem: it asks you to return a boolean decision for each request and, in the follow-up, enforce multiple independent windows at the same time.

## Part 1: Global Sliding-Window Limiter

### Problem Statement

Given sorted request timestamps, a window length, and a maximum request count, return whether each request should be accepted.

```python
from typing import List

def rate_limiter(
    requestTimestamps: List[int],
    windowLength: int,
    maxRequests: int,
) -> List[bool]:
    """
    Return True for each accepted request and False for each denied request.
    Only accepted requests count against future capacity.
    """
    pass
```

### Example

```python
requestTimestamps = [1, 2, 3, 4, 5, 6]
windowLength = 3
maxRequests = 2

rate_limiter(requestTimestamps, windowLength, maxRequests)
# [True, True, False, True, True, False]
```

Walkthrough:

| Request | Accepted timestamps still in window | Decision |
| --- | --- | --- |
| `t = 1` | `[]` | Accept |
| `t = 2` | `[1]` | Accept |
| `t = 3` | `[1, 2]` | Deny |
| `t = 4` | `[2]` after removing `1` | Accept |
| `t = 5` | `[4]` after removing `2` | Accept |
| `t = 6` | `[4, 5]` | Deny |

### Solution

Use a queue of accepted timestamps. Before processing timestamp `t`, remove every accepted timestamp that is no longer in the open-left window `(t - windowLength, t]`. If the queue size is below `maxRequests`, accept and append `t`; otherwise deny and do not append.

```python
from collections import deque
from typing import List

def rate_limiter(
    requestTimestamps: List[int],
    windowLength: int,
    maxRequests: int,
) -> List[bool]:
    accepted = deque()
    decisions = []

    for timestamp in requestTimestamps:
        cutoff = timestamp - windowLength
        while accepted and accepted[0] <= cutoff:
            accepted.popleft()

        if len(accepted) < maxRequests:
            decisions.append(True)
            accepted.append(timestamp)
        else:
            decisions.append(False)

    return decisions
```

**Complexity:**

- Time: `O(n)`, because each accepted timestamp is appended once and removed once.
- Space: `O(k)`, where `k` is the number of accepted timestamps still inside the active window. Since denied requests are not stored, this is at most `maxRequests` for Part 1.

## Part 2: Per-User and Per-Experience Limits

### Problem Statement

Now each request has a `userId` and an `experienceId`. Enforce an independent sliding-window limit for every user and every experience. A request is accepted only if both the user limiter and the experience limiter have capacity.

```python
from typing import List

def per_entity_rate_limiter(
    requestTimestamps: List[int],
    userIds: List[int],
    experienceIds: List[str],
    windowLength: int,
    maxRequests: int,
) -> List[bool]:
    """
    Return True when the request is allowed by both:
    1. its user's sliding window
    2. its experience's sliding window
    """
    pass
```

### Example

```python
requestTimestamps = [1, 2, 3, 4, 5]
userIds = [1, 1, 2, 1, 2]
experienceIds = ["A", "A", "A", "A", "B"]
windowLength = 3
maxRequests = 1

per_entity_rate_limiter(
    requestTimestamps,
    userIds,
    experienceIds,
    windowLength,
    maxRequests,
)
# [True, False, False, True, True]
```

Walkthrough:

| Request | Reason |
| --- | --- |
| `(t=1, user=1, exp=A)` | First request for both entities, accept. |
| `(t=2, user=1, exp=A)` | User `1` already has an accepted request in the window, deny. |
| `(t=3, user=2, exp=A)` | Experience `A` already has an accepted request in the window, deny. |
| `(t=4, user=1, exp=A)` | The accepted `t=1` request is expired, accept. |
| `(t=5, user=2, exp=B)` | User `2` has no accepted request, and `B` is new, accept. |

### Solution

Use two maps:

- `user_id -> deque[accepted_timestamps]`
- `experience_id -> deque[accepted_timestamps]`

For each request, clean both queues first. Then check both counts. Only append the timestamp to both queues if the request is accepted.

```python
from collections import defaultdict, deque
from typing import Deque, Dict, List

def _cleanup(window: Deque[int], timestamp: int, windowLength: int) -> None:
    cutoff = timestamp - windowLength
    while window and window[0] <= cutoff:
        window.popleft()

def per_entity_rate_limiter(
    requestTimestamps: List[int],
    userIds: List[int],
    experienceIds: List[str],
    windowLength: int,
    maxRequests: int,
) -> List[bool]:
    user_windows: Dict[int, Deque[int]] = defaultdict(deque)
    experience_windows: Dict[str, Deque[int]] = defaultdict(deque)
    decisions = []

    for timestamp, user_id, experience_id in zip(
        requestTimestamps,
        userIds,
        experienceIds,
    ):
        user_window = user_windows[user_id]
        experience_window = experience_windows[experience_id]

        _cleanup(user_window, timestamp, windowLength)
        _cleanup(experience_window, timestamp, windowLength)

        if len(user_window) < maxRequests and len(experience_window) < maxRequests:
            decisions.append(True)
            user_window.append(timestamp)
            experience_window.append(timestamp)
        else:
            decisions.append(False)

    return decisions
```

**Complexity:**

- Time: `O(n)` amortized. Each accepted request can be removed once from its user queue and once from its experience queue.
- Space: `O(a)`, where `a` is the number of accepted requests still active across entity windows. Each accepted request is stored in two queues.

## Follow-up Variant: More Request Fields

Some follow-ups add fields such as IP address, device ID, or several arbitrary request attributes and ask how you would rate-limit separately by each field.

The pattern is the same: create one map of queues per dimension. A request is accepted only if every dimension-specific queue has capacity.

```python
from collections import defaultdict, deque
from typing import Deque, Dict, Hashable, List, Mapping

def multi_field_rate_limiter(
    requestTimestamps: List[int],
    fieldsByName: Mapping[str, List[Hashable]],
    windowLength: int,
    maxRequests: int,
) -> List[bool]:
    windows: Dict[str, Dict[Hashable, Deque[int]]] = {
        field_name: defaultdict(deque)
        for field_name in fieldsByName
    }
    decisions = []

    for i, timestamp in enumerate(requestTimestamps):
        current_windows = []

        for field_name, values in fieldsByName.items():
            window = windows[field_name][values[i]]
            _cleanup(window, timestamp, windowLength)
            current_windows.append(window)

        if all(len(window) < maxRequests for window in current_windows):
            decisions.append(True)
            for window in current_windows:
                window.append(timestamp)
        else:
            decisions.append(False)

    return decisions
```

Adding IP limits becomes a data change:

```python
multi_field_rate_limiter(
    requestTimestamps,
    {
        "user": userIds,
        "experience": experienceIds,
        "ip": ipAddresses,
    },
    windowLength,
    maxRequests,
)
```

If the interviewer wants different limits per field, store a config per dimension:

```python
limits = {
    "user": (60, 100),       # windowLength, maxRequests
    "experience": (60, 500),
    "ip": (60, 50),
}
```

Then clean and compare each queue with its own `(windowLength, maxRequests)` pair.

## Test Cases

```python
assert rate_limiter([1, 2, 3, 4, 5, 6], 3, 2) == [
    True, True, False, True, True, False
]

# Requests exactly on the left boundary expire.
assert rate_limiter([1, 4], 3, 1) == [True, True]

# Denied requests are not stored.
assert rate_limiter([1, 2, 3, 4], 3, 2) == [True, True, False, True]

assert per_entity_rate_limiter(
    [1, 2, 3, 4, 5],
    [1, 1, 2, 1, 2],
    ["A", "A", "A", "A", "B"],
    3,
    1,
) == [True, False, False, True, True]

# Experience cap can deny a request even when the user is new.
assert per_entity_rate_limiter(
    [10, 11],
    [1, 2],
    ["game-1", "game-1"],
    10,
    1,
) == [True, False]
```
