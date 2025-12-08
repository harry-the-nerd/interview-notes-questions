# Find Optimal Commute

## Problem Overview

You are commuting across a simplified map of San Francisco, represented as a 2D grid. Each cell on the grid is one of the following:

- `'S'`: Your home location (starting point)
- `'D'`: Your office location (destination)
- A digit from `'1'` to `k`: A street segment reserved for exactly one transportation mode
- `'X'`: An impassable roadblock

You're also given three arrays with length `k`:

- `modes`: The name of each available transportation mode (e.g., `["bike", "bus", "walk", "scooter"]`)
- `times`: The time (in minutes) required to traverse a single block using each mode
- `costs`: The cost (in dollars) to traverse a single block using each mode

## Movement Rules

- Movement is allowed **up, down, left, and right** only (no diagonal movement)
- You may **only travel along contiguous cells of the same transportation mode** (i.e., same digit)
- You **cannot switch modes** mid-journey in the base problem
- You **cannot move between cells of different modes**, nor can you cross roadblocks (`'X'`)

For each mode `i` (0-indexed), the time and cost to traverse a single block are given by `times[i]` and `costs[i]`, respectively.

The **total travel time** and **total cost** are calculated as the **sum** of the time and cost for **each mode cell traversed** along the path from `'S'` to `'D'`. Note that `'S'` and `'D'` are special cells (not mode cells) and do not contribute to the cost.

## Objective

Return the **name of the transportation mode** that yields the **minimum total time** from `'S'` to `'D'`.

- If **multiple modes** result in the same minimum time, return the one with the **lowest total cost**
- Return an **empty string** `""` if no valid route exists

## Example

### Input:
```python
grid = [
    ['S', '1', '1', '1', 'D'],
    ['2', '2', '2', '2', 'X']
]

modes = ["bike", "bus"]
times = [5, 3]
costs = [2, 1]
```

### Explanation:

Grid layout:
```
Row 0: S   1   1   1   D
Row 1: 2   2   2   2   X
```

- **Mode 1 (bike)**: Path exists from S to D
  - S (0,0) → (0,1) → (0,2) → (0,3) → D (0,4)
  - Distance: 3 mode cells traversed
  - Time: 3 × 5 = **15 minutes**
  - Cost: 3 × 2 = 6 dollars

- **Mode 2 (bus)**: Cannot reach D
  - S (0,0) → (1,0) → (1,1) → (1,2) → (1,3) → blocked by X at (1,4)
  - No path to D

### Output:
```python
"bike"  # Only valid route with 15 minutes
```

## Constraints

- `1 <= grid.length, grid[0].length <= 100` (rows × columns)
- `1 <= k <= 4` (number of transportation modes)
- `modes.length == times.length == costs.length == k`
- `1 <= times[i], costs[i] <= 100`
- Grid contains exactly one `'S'` and one `'D'`

## Approach 1: Multi-Pass BFS (Naive Solution)

### Algorithm

For each transportation mode:
1. Run a BFS starting from `'S'`
2. Only traverse cells that match the current mode's digit
3. Track the shortest path length to reach `'D'`
4. Calculate total time and cost for this mode

After all BFS runs, compare results and return the optimal mode.

### Time Complexity
- **O(k × r × c)** where:
  - `k` = number of modes
  - `r` = number of rows
  - `c` = number of columns
- We run BFS `k` times, and each BFS visits O(r × c) cells

### Space Complexity
- **O(r × c)** for the visited set and queue

### Implementation:

```python
from collections import deque
from typing import List

def findOptimalCommute(grid: List[List[str]], modes: List[str],
                       times: List[int], costs: List[int]) -> str:
    rows, cols = len(grid), len(grid[0])

    # Find start and destination
    start, dest = None, None
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 'S':
                start = (r, c)
            elif grid[r][c] == 'D':
                dest = (r, c)

    if not start or not dest:
        return ""

    best_time = float('inf')
    best_cost = float('inf')
    best_mode = ""

    # Try each transportation mode
    for mode_idx in range(len(modes)):
        mode_digit = str(mode_idx + 1)

        # BFS for this mode
        queue = deque([(start[0], start[1], 0)])  # (row, col, distance)
        visited = {start}
        found = False

        while queue and not found:
            r, c, dist = queue.popleft()

            # Check if we reached destination
            if (r, c) == dest:
                # Calculate time and cost
                total_time = dist * times[mode_idx]
                total_cost = dist * costs[mode_idx]

                # Update best mode
                if (total_time < best_time or
                    (total_time == best_time and total_cost < best_cost)):
                    best_time = total_time
                    best_cost = total_cost
                    best_mode = modes[mode_idx]
                found = True
                break

            # Explore neighbors
            for dr, dc in [(0, 1), (1, 0), (0, -1), (-1, 0)]:
                nr, nc = r + dr, c + dc

                if (0 <= nr < rows and 0 <= nc < cols and
                    (nr, nc) not in visited):
                    cell = grid[nr][nc]

                    # Can move to this cell if it matches our mode or is destination
                    if cell == mode_digit or cell == 'D':
                        visited.add((nr, nc))
                        # Don't increment distance for destination (D is not a mode cell)
                        new_dist = dist if cell == 'D' else dist + 1
                        queue.append((nr, nc, new_dist))

    return best_mode
```

## Approach 2: Single-Pass BFS (Optimized Solution)

The key insight is that we can run **one BFS** from the start and explore **all modes simultaneously** by tracking which mode each cell belongs to.

### Algorithm

1. Start BFS from `'S'`, adding all adjacent cells (regardless of mode) to the queue
2. For each cell in the queue, track `(row, col, mode_used, distance)`
3. Only add a neighbor to the queue if:
   - It matches the current mode being used, OR
   - It's the destination `'D'`
4. Each cell can only be visited **once per mode** (use a set to track `(row, col, mode)`)
5. When reaching `'D'`, calculate time and cost, and update the best result

### Key Optimization

By tracking `(row, col, mode)` in the visited set, we ensure each `(cell, mode)` pair is processed at most once.

**Crucially**: Each grid cell has a fixed mode digit (e.g., '1', '2', etc.). A cell with mode '1' can only be visited when traveling using mode 1. Therefore, although we track `(row, col, mode)` in the visited set, **each cell is visited exactly once** (by its designated mode), not k times.

This gives us **O(r × c)** total cell visits instead of O(k × r × c).

### Time Complexity
- **O(r × c)** - Each cell is visited exactly once by its designated mode
- More precisely: **O(r × c + E)** where E is the number of edges (at most 4 × r × c)
- This is a significant improvement over the naive approach's O(k × r × c)

### Space Complexity
- **O(r × c)** for the visited set and queue

### Implementation:

```python
from collections import deque
from typing import List

def findOptimalCommuteOptimized(grid: List[List[str]], modes: List[str],
                                times: List[int], costs: List[int]) -> str:
    rows, cols = len(grid), len(grid[0])

    # Find start and destination
    start, dest = None, None
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 'S':
                start = (r, c)
            elif grid[r][c] == 'D':
                dest = (r, c)

    if not start or not dest:
        return ""

    best_time = float('inf')
    best_cost = float('inf')
    best_mode = ""

    # Single BFS: (row, col, mode_used, distance)
    # mode_used is the digit of the transportation mode
    queue = deque()
    visited = set()

    # Initialize: explore all neighbors of start
    for dr, dc in [(0, 1), (1, 0), (0, -1), (-1, 0)]:
        nr, nc = start[0] + dr, start[1] + dc

        if 0 <= nr < rows and 0 <= nc < cols:
            cell = grid[nr][nc]

            if cell.isdigit():
                mode_digit = cell
                visited.add((nr, nc, mode_digit))
                queue.append((nr, nc, mode_digit, 1))

    # BFS
    while queue:
        r, c, mode_digit, dist = queue.popleft()

        # Check if we reached destination
        if grid[r][c] == 'D':
            mode_idx = int(mode_digit) - 1
            total_time = dist * times[mode_idx]
            total_cost = dist * costs[mode_idx]

            # Update best mode
            if (total_time < best_time or
                (total_time == best_time and total_cost < best_cost)):
                best_time = total_time
                best_cost = total_cost
                best_mode = modes[mode_idx]
            continue

        # Explore neighbors with the SAME mode
        for dr, dc in [(0, 1), (1, 0), (0, -1), (-1, 0)]:
            nr, nc = r + dr, c + dc

            if (0 <= nr < rows and 0 <= nc < cols and
                (nr, nc, mode_digit) not in visited):
                cell = grid[nr][nc]

                # Can move if same mode or destination
                if cell == mode_digit or cell == 'D':
                    visited.add((nr, nc, mode_digit))
                    # Don't increment distance for destination (D is not a mode cell)
                    new_dist = dist if cell == 'D' else dist + 1
                    queue.append((nr, nc, mode_digit, new_dist))

    return best_mode
```

## Follow-Up 1: Allow Mode Switching with Cost

**Question:** What if you can switch transportation modes mid-journey, but each switch incurs an additional cost of `x` dollars (and potentially additional time `t` minutes)?

### Modified Problem

- You can now switch modes at any cell
- Each switch adds `switch_cost` dollars and `switch_time` minutes to the total
- Find the optimal path considering both travel and switch costs

### Solution Approach

Use **Dijkstra's algorithm** with a priority queue:

1. State: `(total_time, total_cost, row, col, current_mode)`
2. Priority: Sort by `(total_time, total_cost)` (time first, then cost)
3. Maintain a distance matrix: `best[row][col][mode]` to track the best time/cost to reach each cell with each mode
4. When exploring neighbors:
   - **Same mode**: Add normal travel time/cost
   - **Different mode**: Add travel time/cost + switch penalties

### Time Complexity
- **O((r × c × k) log(r × c × k))** using a priority queue
- Each cell can be visited up to `k` times (once per mode)

### Implementation:

```python
import heapq
from typing import List

def findOptimalCommuteWithSwitching(grid: List[List[str]], modes: List[str],
                                    times: List[int], costs: List[int],
                                    switch_time: int, switch_cost: int) -> str:
    rows, cols = len(grid), len(grid[0])

    # Find start and destination
    start, dest = None, None
    for r in range(rows):
        for c in range(cols):
            if grid[r][c] == 'S':
                start = (r, c)
            elif grid[r][c] == 'D':
                dest = (r, c)

    if not start or not dest:
        return ""

    # Priority queue: (total_time, total_cost, row, col, mode_idx)
    pq = []

    # Initialize with all modes from start
    for dr, dc in [(0, 1), (1, 0), (0, -1), (-1, 0)]:
        nr, nc = start[0] + dr, start[1] + dc
        if 0 <= nr < rows and 0 <= nc < cols:
            cell = grid[nr][nc]
            if cell.isdigit():
                mode_idx = int(cell) - 1
                heapq.heappush(pq, (times[mode_idx], costs[mode_idx], nr, nc, mode_idx))

    # Track best (time, cost) for each (row, col, mode)
    best = {}

    while pq:
        curr_time, curr_cost, r, c, mode_idx = heapq.heappop(pq)

        # Check if we've seen this state with better time/cost
        state = (r, c, mode_idx)
        if state in best:
            best_time, best_cost = best[state]
            if (curr_time > best_time or
                (curr_time == best_time and curr_cost >= best_cost)):
                continue

        best[state] = (curr_time, curr_cost)

        # Check if reached destination
        if grid[r][c] == 'D':
            return modes[mode_idx]

        # Explore neighbors
        for dr, dc in [(0, 1), (1, 0), (0, -1), (-1, 0)]:
            nr, nc = r + dr, c + dc

            if 0 <= nr < rows and 0 <= nc < cols:
                cell = grid[nr][nc]

                if cell == 'X':
                    continue

                # Special case: destination
                if cell == 'D':
                    # Move to destination with current mode (no additional cost)
                    next_state = (nr, nc, mode_idx)
                    if next_state in best:
                        best_time, best_cost = best[next_state]
                        if (curr_time > best_time or
                            (curr_time == best_time and curr_cost >= best_cost)):
                            continue
                    heapq.heappush(pq, (curr_time, curr_cost, nr, nc, mode_idx))
                    continue

                # Try all possible modes for next cell
                for next_mode_idx in range(len(modes)):
                    next_mode_digit = str(next_mode_idx + 1)

                    # Check if this mode is valid for the cell
                    if cell != next_mode_digit:
                        continue

                    # Calculate new time and cost
                    new_time = curr_time + times[next_mode_idx]
                    new_cost = curr_cost + costs[next_mode_idx]

                    # Add switch penalty if changing modes
                    if next_mode_idx != mode_idx:
                        new_time += switch_time
                        new_cost += switch_cost

                    # Check if this is better than previous visit
                    next_state = (nr, nc, next_mode_idx)
                    if next_state in best:
                        best_time, best_cost = best[next_state]
                        if (new_time > best_time or
                            (new_time == best_time and new_cost >= best_cost)):
                            continue

                    heapq.heappush(pq, (new_time, new_cost, nr, nc, next_mode_idx))

    return ""  # No path found
```

## Follow-Up 2: Limited Number of Mode Switches

**Question:** What if you can switch modes at most `max_switches` times?

### Solution Approach

Modify the Dijkstra's algorithm to include the number of switches in the state:

1. State: `(total_time, total_cost, row, col, current_mode, switches_used)`
2. Track: `best[row][col][mode][switches_used]`
3. Only allow switching if `switches_used < max_switches`

### Time Complexity
- **O((r × c × k × max_switches) log(r × c × k × max_switches))**

## Edge Cases to Consider

1. **No valid path**: Return `""` (e.g., D is surrounded by 'X' or wrong mode cells)
2. **S and D adjacent with no mode cells**: No valid path, return `""`
3. **All modes blocked**: No mode can reach destination, return `""`
4. **Tie in time, different costs**: Choose the cheaper option (correctly implemented)
5. **Multiple paths with same mode**: BFS finds shortest, which gives minimum time
6. **Grid with only S and D**: No mode cells means no valid path, return `""`
7. **Single mode cell between S and D**: Path length of 1, cost = 1 × times[mode]

## Key Insights

- **Base problem**: Candidate should recognize this as a graph traversal problem (BFS)
- **Optimization**: Key insight is to run single-pass BFS where each cell is visited exactly once by its designated mode, reducing complexity from O(k × r × c) to O(r × c)
- **Common mistakes**:
  - Incorrectly counting S or D cells in the cost calculation
  - Using Dijkstra instead of BFS for the base problem (overkill)
  - Not handling tie-breaking (minimum time, then minimum cost)
- **Follow-up concepts**:
  - Understanding of Dijkstra's algorithm
  - State space design with mode switching
  - Ability to extend solutions to more complex scenarios
- **Important**: Use BFS for base problem (uniform cost per step within same mode). Only use Dijkstra/priority queue when switching is allowed (non-uniform costs).
