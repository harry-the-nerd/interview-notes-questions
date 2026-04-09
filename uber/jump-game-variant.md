# Jump Game Variant

## Problem Overview

You are given an integer array `nums` where `nums[i]` is the score at index `i`.

You start at index `0` and want to reach index `n - 1`. Every time you land on an index, you add that index's value to your total score. The starting index already counts as landed on, so `nums[0]` is included in the score.

From index `i`, you may jump only to the right by:

- exactly `1` step
- exactly `p` steps where `p` is a prime number whose last digit is `3`

Examples of valid prime jumps include `3`, `13`, `23`, and so on.

Return the maximum total score you can collect when you land on index `n - 1`.

This is the first Uber question in the Dark Interview collection, and it is a good dynamic programming problem because the unusual jump rule forces you to combine recurrence design with preprocessing.

---

## Problem Statement

Implement:

```python
def max_jump_score(nums: list[int]) -> int:
    pass
```

### Constraints

- `1 <= len(nums) <= 10^5`
- `-10^4 <= nums[i] <= 10^4`
- You may jump right by `1` or by any prime number whose last digit is `3`

### Example 1

```python
nums = [5, -100, 4, 10]

# Output: 15
```

Explanation:

- A `3`-step jump from index `0` to index `3` is allowed.
- The score is `5 + 10 = 15`.

### Example 2

```python
nums = [4, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 20]

# Output: 24
```

Explanation:

- A `13`-step jump is allowed because `13` is prime and ends in `3`.
- Jumping directly from `0` to `13` gives `4 + 20 = 24`.

### Example 3

```python
nums = [7]

# Output: 7
```

Explanation:

- You already start on the last index.

---

## Solution Approach

This is a dynamic programming problem.

Let `dp[i]` be the maximum score possible when you land on index `i`.

Because a `1`-step jump is always allowed, every position is reachable. To compute `dp[i]`, the previous landing position could be:

- `i - 1`
- `i - p` for any valid prime jump length `p`

That gives the recurrence:

```python
dp[i] = nums[i] + max(dp[i - 1], dp[i - p1], dp[i - p2], ...)
```

The remaining question is how to efficiently enumerate the valid jump lengths. Since jumps only depend on distance, not on array values, precompute all prime numbers up to `n - 1` with a sieve, then keep only the primes ending in `3`.

### Why this works

1. Every optimal path to index `i` must come from a legal previous index.
2. The best path to that previous index is already stored in `dp`.
3. Taking the maximum over all legal previous indices guarantees the optimal score at `i`.

---

## Reference Solution

```python
def special_primes(limit: int) -> list[int]:
    if limit < 3:
        return []

    is_prime = [True] * (limit + 1)
    is_prime[0] = False
    is_prime[1] = False

    i = 2
    while i * i <= limit:
        if is_prime[i]:
            j = i * i
            while j <= limit:
                is_prime[j] = False
                j += i
        i += 1

    jumps = []
    for p in range(3, limit + 1):
        if is_prime[p] and p % 10 == 3:
            jumps.append(p)

    return jumps


def max_jump_score(nums: list[int]) -> int:
    jumps = special_primes(len(nums) - 1)
    dp = [0] * len(nums)
    dp[0] = nums[0]

    for i in range(1, len(nums)):
        best = dp[i - 1]

        for jump in jumps:
            if jump > i:
                break
            best = max(best, dp[i - jump])

        dp[i] = best + nums[i]

    return dp[-1]
```

### Walkthrough

For `nums = [5, -100, 4, 10]`:

- `dp[0] = 5`
- `dp[1] = 5 + (-100) = -95`
- `dp[2] = -95 + 4 = -91`
- `dp[3]` can come from:
  - `dp[2]` via a `1`-step jump
  - `dp[0]` via a `3`-step jump
- So `dp[3] = 10 + max(-91, 5) = 15`

---

## Complexity Analysis

Let:

- `n` be the length of `nums`
- `k` be the number of primes up to `n - 1` whose last digit is `3`

Then:

- Sieve preprocessing: `O(n log log n)`
- Dynamic programming: `O(n * k)`
- Space: `O(n)`

For the interview, this is typically accepted because the set of valid special primes is much smaller than all possible jump lengths, and the recurrence is straightforward to reason about.

---

## Common Follow-Ups

1. How would you reduce the memory from `O(n)` if you only needed the final answer?
2. What changes if the jump rule is "any prime number" rather than "prime ending in `3`"?
3. How would you reconstruct the actual path, not just the score?
4. Could negative values make greedy strategies fail?

---

## Discussion Topics

- Why a greedy "take the biggest next value" strategy is not correct
- How sieve preprocessing works and why it is appropriate here
- How to extend the recurrence if the interviewer adds more jump types
- Whether the `O(n * k)` transition can be optimized further for very large `n`
