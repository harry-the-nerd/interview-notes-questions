# Moderator List Hierarchy


## Problem Overview

Implement a Reddit-style moderator list from a newline-delimited log string.

The interview usually comes in three parts:

1. A single mod list
2. Multiple communities
3. Demotions that reorder the current mod list

The core rule is:

- A moderator can remove another moderator only if they are **above** that moderator in the current mod list.
- Higher in the list means the moderator has an **earlier effective access timestamp**.

In the first part, each log line has four comma-separated fields:

```text
target, action, actor, timestamp
```

- `target`: the moderator receiving the action
- `action`: `add` or `remove` in Parts 1 and 2, plus `demote` in Part 3
- `actor`: the moderator who performed the action
- `timestamp`: integer timestamp, increasing over time

Assume:

- The logs are already sorted by timestamp
- In Part 1, timestamps are unique
- In Parts 2 and 3, timestamps are unique within each community
- Every logged action is valid
- `SYSTEM` may appear as an actor to seed the first moderator, but `SYSTEM` is not part of the returned mod list unless explicitly added
- If a removed moderator is added again later, they get a new access timestamp

Each part asks for the same three operations:

1. Constructor: build the state from the log string
2. `can_remove_mod(...)`
3. `get_mod_list(...)`

## Part 1: Single Moderator List

### Problem Statement

Implement a class for a single moderator list:

```python
class ModList:
    def __init__(self, logs: str):
        pass

    def can_remove_mod(self, actor: str, target: str) -> bool:
        pass

    def get_mod_list(self) -> list[str]:
        pass
```

### Rules

- `add` means `target` becomes an active moderator at `timestamp`
- `remove` means `target` is no longer an active moderator
- `can_remove_mod(actor, target)` returns `True` only if:
  - both moderators are currently active
  - `actor != target`
  - `actor` has an earlier effective access timestamp than `target`
- `get_mod_list()` returns active moderators from top to bottom of the mod list

### Example

```python
logs = """
alice,add,SYSTEM,1
bob,add,alice,2
carol,add,alice,3
dave,add,bob,4
carol,remove,alice,5
""".strip()

mod_list = ModList(logs)

mod_list.can_remove_mod("alice", "bob")   # True
mod_list.can_remove_mod("bob", "alice")   # False
mod_list.can_remove_mod("bob", "dave")    # True
mod_list.get_mod_list()                   # ["alice", "bob", "dave"]
```

### Part 1 Solution

Replay the log once and store each active moderator's effective access timestamp.

```python
class ModList:
    def __init__(self, logs: str):
        self.active_since: dict[str, int] = {}

        for raw_line in logs.splitlines():
            line = raw_line.strip()
            if not line:
                continue

            target, action, actor, ts = [part.strip() for part in line.split(",")]
            timestamp = int(ts)

            if action == "add":
                self.active_since[target] = timestamp
            elif action == "remove":
                self.active_since.pop(target, None)
            else:
                raise ValueError(f"Unsupported action: {action}")

    def can_remove_mod(self, actor: str, target: str) -> bool:
        if actor == target:
            return False
        if actor not in self.active_since or target not in self.active_since:
            return False
        return self.active_since[actor] < self.active_since[target]

    def get_mod_list(self) -> list[str]:
        return [
            mod
            for mod, _ in sorted(
                self.active_since.items(),
                key=lambda item: (item[1], item[0]),
            )
        ]
```

### Complexity

- Constructor: `O(n)`
- `can_remove_mod`: `O(1)`
- `get_mod_list`: `O(m log m)`, where `m` is the number of active moderators
- Space: `O(m)`

## Part 2: Add Community Support

### Problem Statement

Now extend the problem to multiple communities. Each log line becomes:

```text
community, target, action, actor, timestamp
```

Implement:

```python
class CommunityModList:
    def __init__(self, logs: str):
        pass

    def can_remove_mod(self, community: str, actor: str, target: str) -> bool:
        pass

    def get_mod_list(self, community: str) -> list[str]:
        pass
```

### Rules

- Moderator membership is tracked independently for each community
- The same username can appear in multiple communities with different access timestamps
- `can_remove_mod` only considers the given community

### Example

```python
logs = """
python,alice,add,SYSTEM,1
python,bob,add,alice,2
python,carol,add,alice,3
datascience,maya,add,SYSTEM,4
datascience,bob,add,maya,5
python,carol,remove,alice,6
""".strip()

mod_list = CommunityModList(logs)

mod_list.get_mod_list("python")        # ["alice", "bob"]
mod_list.get_mod_list("datascience")   # ["maya", "bob"]
mod_list.can_remove_mod("python", "alice", "bob")        # True
mod_list.can_remove_mod("datascience", "bob", "maya")    # False
```

### Part 2 Solution

Use a nested dictionary keyed by community, then by moderator.

```python
from collections import defaultdict


class CommunityModList:
    def __init__(self, logs: str):
        self.active_since: dict[str, dict[str, int]] = defaultdict(dict)

        for raw_line in logs.splitlines():
            line = raw_line.strip()
            if not line:
                continue

            community, target, action, actor, ts = [
                part.strip() for part in line.split(",")
            ]
            timestamp = int(ts)

            if action == "add":
                self.active_since[community][target] = timestamp
            elif action == "remove":
                self.active_since[community].pop(target, None)
            else:
                raise ValueError(f"Unsupported action: {action}")

    def can_remove_mod(self, community: str, actor: str, target: str) -> bool:
        mods = self.active_since.get(community, {})

        if actor == target:
            return False
        if actor not in mods or target not in mods:
            return False
        return mods[actor] < mods[target]

    def get_mod_list(self, community: str) -> list[str]:
        mods = self.active_since.get(community, {})
        return [
            mod
            for mod, _ in sorted(
                mods.items(),
                key=lambda item: (item[1], item[0]),
            )
        ]
```

### Complexity

- Constructor: `O(n)`
- `can_remove_mod`: `O(1)`
- `get_mod_list(community)`: `O(m log m)`
- Space: `O(total active moderators across all communities)`

## Part 3: Support Moderator Demotion

### Problem Statement

Extend the multi-community version to support a third action:

```text
community, target, demote, actor, timestamp
```

Demotion means the target moderator stays active, but their **effective access timestamp** becomes the demotion timestamp. In other words, they move down in that community's mod list.

Implement the same three functions:

```python
class ReorderableCommunityModList:
    def __init__(self, logs: str):
        pass

    def can_remove_mod(self, community: str, actor: str, target: str) -> bool:
        pass

    def get_mod_list(self, community: str) -> list[str]:
        pass
```

### Example

```python
logs = """
python,alice,add,SYSTEM,1
python,bob,add,alice,2
python,carol,add,alice,3
python,bob,demote,alice,4
""".strip()

mod_list = ReorderableCommunityModList(logs)

mod_list.get_mod_list("python")                     # ["alice", "carol", "bob"]
mod_list.can_remove_mod("python", "carol", "bob")  # True
mod_list.can_remove_mod("python", "bob", "carol")  # False
```

### Part 3 Solution

The only real change is handling `demote` by overwriting the target moderator's effective timestamp.

```python
from collections import defaultdict


class ReorderableCommunityModList:
    def __init__(self, logs: str):
        self.active_since: dict[str, dict[str, int]] = defaultdict(dict)

        for raw_line in logs.splitlines():
            line = raw_line.strip()
            if not line:
                continue

            community, target, action, actor, ts = [
                part.strip() for part in line.split(",")
            ]
            timestamp = int(ts)
            mods = self.active_since[community]

            if action == "add":
                mods[target] = timestamp
            elif action == "remove":
                mods.pop(target, None)
            elif action == "demote":
                if target in mods:
                    mods[target] = timestamp
            else:
                raise ValueError(f"Unsupported action: {action}")

    def can_remove_mod(self, community: str, actor: str, target: str) -> bool:
        mods = self.active_since.get(community, {})

        if actor == target:
            return False
        if actor not in mods or target not in mods:
            return False
        return mods[actor] < mods[target]

    def get_mod_list(self, community: str) -> list[str]:
        mods = self.active_since.get(community, {})
        return [
            mod
            for mod, _ in sorted(
                mods.items(),
                key=lambda item: (item[1], item[0]),
            )
        ]
```

### Follow-Up Discussion

- How would you support `get_mod_list()` in `O(m)` without sorting every time?
- How would you validate invalid logs instead of assuming all actions are valid?
- What if timestamps are not unique?
- How would you support querying historical mod-list state at a past timestamp?
