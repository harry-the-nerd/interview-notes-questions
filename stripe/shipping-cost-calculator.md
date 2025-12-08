# Shipping Cost Calculator

## Problem Overview

You are building a shipping cost calculation system for an e-commerce platform. The system needs to calculate the total shipping cost for orders based on various pricing models that differ by country and product type.

The problem is divided into three progressive parts, each introducing additional complexity:

1. **Basic Fixed Pricing**: Calculate shipping costs using simple fixed rates per product
2. **Tiered Incremental Pricing**: Handle quantity-based pricing tiers where cost per unit decreases with volume
3. **Mixed Pricing Models**: Support both fixed-tier pricing and incremental pricing within the same calculation

### Input Format

You will receive two main data structures:

**Order Object**: Contains the country code and a list of items
```python
{
    "country": "US",
    "items": [
        {"product": "mouse", "quantity": 20},
        {"product": "laptop", "quantity": 5}
    ]
}
```

**Shipping Cost Configuration**: A nested dictionary defining pricing rules by country and product

The structure of the shipping cost configuration will evolve across the three parts of the problem.

---

## Part 1: Fixed Rate Shipping

### Problem Statement

Implement a function `calculate_shipping_cost(order, shipping_cost)` that calculates the total shipping cost when each product has a fixed unit cost.

### Example

**Input:**
```python
order_us = {
    "country": "US",
    "items": [
        {"product": "mouse", "quantity": 20},
        {"product": "laptop", "quantity": 5}
    ]
}

order_ca = {
    "country": "CA",
    "items": [
        {"product": "mouse", "quantity": 20},
        {"product": "laptop", "quantity": 5}
    ]
}

shipping_cost = {
    "US": [
        {"product": "mouse", "cost": 550},
        {"product": "laptop", "cost": 1000}
    ],
    "CA": [
        {"product": "mouse", "cost": 750},
        {"product": "laptop", "cost": 1100}
    ]
}
```

**Output:**
```python
calculate_shipping_cost(order_us, shipping_cost) == 16000
# Calculation: (20 × 550) + (5 × 1000) = 11000 + 5000 = 16000

calculate_shipping_cost(order_ca, shipping_cost) == 20500
# Calculation: (20 × 750) + (5 × 1100) = 15000 + 5500 = 20500
```

### Requirements

- Handle multiple products in a single order
- Support different pricing for different countries
- Return the total shipping cost as an integer

---

## Part 2: Tiered Incremental Pricing

### Problem Statement

Extend your solution to handle tiered pricing structures where the cost per unit changes based on quantity ranges. Each quantity tier specifies a minimum quantity, maximum quantity, and the cost per unit within that range.

This is similar to volume-based discounts - buying more units reduces the per-unit cost for items in higher quantity brackets.

### Example

**Input:**
```python
# Orders remain the same as Part 1

shipping_cost = {
    "US": [
        {
            "product": "mouse",
            "costs": [
                {"minQuantity": 0, "maxQuantity": None, "cost": 550}
            ]
        },
        {
            "product": "laptop",
            "costs": [
                {"minQuantity": 0, "maxQuantity": 2, "cost": 1000},
                {"minQuantity": 3, "maxQuantity": None, "cost": 900}
            ]
        }
    ],
    "CA": [
        {
            "product": "mouse",
            "costs": [
                {"minQuantity": 0, "maxQuantity": None, "cost": 750}
            ]
        },
        {
            "product": "laptop",
            "costs": [
                {"minQuantity": 0, "maxQuantity": 2, "cost": 1100},
                {"minQuantity": 3, "maxQuantity": None, "cost": 1000}
            ]
        }
    ]
}
```

**Output:**
```python
calculate_shipping_cost(order_us, shipping_cost) == 15700
# Calculation:
# mouse: 20 × 550 = 11000 (all units at same rate)
# laptop: (2 × 1000) + (3 × 900) = 2000 + 2700 = 4700
# Total: 11000 + 4700 = 15700

calculate_shipping_cost(order_ca, shipping_cost) == 20200
# Calculation:
# mouse: 20 × 750 = 15000
# laptop: (2 × 1100) + (3 × 1000) = 2200 + 3000 = 5200
# Total: 15000 + 5200 = 20200
```

### Requirements

- Process quantity tiers sequentially from lowest to highest
- `maxQuantity` of `None` indicates unlimited quantity at that tier
- Quantity ranges are half-open intervals: [minQuantity, maxQuantity) - inclusive on the left, exclusive on the right
- Calculate the cost for each tier separately and sum them

### Clarification Questions to Ask

- Are the cost tiers always sorted by `minQuantity`?
- Are the quantity ranges half-open [min, max) or fully inclusive [min, max]? (Important for tier boundary calculations)
- Can tiers overlap or have gaps?
- How should we handle edge cases like zero quantity or missing products?

---

## Part 3: Mixed Pricing Models (Fixed + Incremental)

### Problem Statement

Extend the solution to support two different pricing model types within the same tier structure:

- **`incremental`**: Charge per unit as in Part 2 (quantity × cost)
- **`fixed`**: Charge a flat fee regardless of quantity within that tier

A single product can have multiple tiers alternating between fixed and incremental types.

### Example

**Input:**
```python
# Orders remain the same

shipping_cost = {
    "US": [
        {
            "product": "mouse",
            "costs": [
                {
                    "type": "incremental",
                    "minQuantity": 0,
                    "maxQuantity": None,
                    "cost": 550
                }
            ]
        },
        {
            "product": "laptop",
            "costs": [
                {
                    "type": "fixed",
                    "minQuantity": 0,
                    "maxQuantity": 2,
                    "cost": 1000
                },
                {
                    "type": "incremental",
                    "minQuantity": 3,
                    "maxQuantity": None,
                    "cost": 900
                }
            ]
        }
    ],
    "CA": [
        {
            "product": "mouse",
            "costs": [
                {
                    "type": "incremental",
                    "minQuantity": 0,
                    "maxQuantity": None,
                    "cost": 750
                }
            ]
        },
        {
            "product": "laptop",
            "costs": [
                {
                    "type": "fixed",
                    "minQuantity": 0,
                    "maxQuantity": 2,
                    "cost": 1100
                },
                {
                    "type": "incremental",
                    "minQuantity": 3,
                    "maxQuantity": None,
                    "cost": 1000
                }
            ]
        }
    ]
}
```

**Output:**
```python
calculate_shipping_cost(order_us, shipping_cost) == 14700
# Calculation:
# mouse: 20 × 550 = 11000 (incremental)
# laptop: 1000 (fixed for first 2) + (3 × 900) = 1000 + 2700 = 3700
# Total: 11000 + 3700 = 14700

calculate_shipping_cost(order_ca, shipping_cost) == 19100
# Calculation:
# mouse: 20 × 750 = 15000 (incremental)
# laptop: 1100 (fixed for first 2) + (3 × 1000) = 1100 + 3000 = 4100
# Total: 15000 + 4100 = 19100
```

### Requirements

- Support both `"fixed"` and `"incremental"` pricing types
- Fixed pricing: Add the cost value directly when quantity falls in that tier
- Incremental pricing: Multiply quantity by cost as in Part 2
- Handle alternating type patterns (e.g., fixed → incremental → fixed → incremental)

---

## Solution Approach

### Part 1: Fixed Rate Solution

**Strategy:**
1. Extract the country from the order
2. Build a lookup dictionary mapping product names to costs for that country
3. Iterate through order items and accumulate: `quantity × cost`

**Time Complexity:** O(n + m) where n is the number of products in the cost table and m is the number of items in the order

**Space Complexity:** O(n) for the lookup dictionary

**Example Implementation:**
```python
def calculate_shipping_cost(order, shipping_cost):
    country = order["country"]
    cost_map = {}

    # Build product -> cost mapping for the country
    for product_info in shipping_cost[country]:
        cost_map[product_info["product"]] = product_info["cost"]

    total = 0
    for item in order["items"]:
        product = item["product"]
        quantity = item["quantity"]
        total += quantity * cost_map[product]

    return total
```

**Edge Cases to Consider:**
- Empty order items list
- Product not found in shipping cost table
- Invalid country code
- Zero or negative quantities
- Missing or null values in input

---

### Part 2: Tiered Incremental Solution

**Strategy:**
1. Build a lookup mapping product names to their tier lists
2. For each order item, track remaining quantity to process
3. Iterate through cost tiers in sequence:
   - Determine how many units fall into the current tier
   - Calculate cost for those units
   - Reduce remaining quantity
4. Continue until all units are processed

**Key Implementation Details:**
- Tiers use half-open intervals [minQuantity, maxQuantity), so tier capacity is simply `maxQuantity - minQuantity`
- Units in this tier: `min(remaining_quantity, tier_capacity)` where tier_capacity = maxQuantity - minQuantity
- Update remaining quantity after each tier
- For unlimited tiers (maxQuantity = None), consume all remaining units

**Time Complexity:** O(n × t + m × t) where t is the average number of tiers per product

**Example Implementation:**
```python
def calculate_shipping_cost(order, shipping_cost):
    country = order["country"]
    cost_map = {}

    # Build product -> costs list mapping
    for product_info in shipping_cost[country]:
        cost_map[product_info["product"]] = product_info["costs"]

    total = 0
    for item in order["items"]:
        product = item["product"]
        quantity = item["quantity"]
        remaining = quantity

        for tier in cost_map[product]:
            if remaining <= 0:
                break

            min_qty = tier["minQuantity"]
            max_qty = tier["maxQuantity"]
            cost = tier["cost"]

            # Calculate tier capacity
            if max_qty is None:
                tier_units = remaining
            else:
                tier_capacity = max_qty - min_qty
                tier_units = min(remaining, tier_capacity)

            total += tier_units * cost
            remaining -= tier_units

    return total
```

**Important Considerations:**
- Verify assumptions about tier ordering with the interviewer
- Confirm whether tier boundaries are half-open [min, max) or fully inclusive [min, max]
- Handle the `maxQuantity: None` case for unlimited tiers
- Validate that all quantity gets consumed (no gaps in tiers)
- Note: The example assumes half-open intervals [minQuantity, maxQuantity) where maxQuantity is exclusive

---

### Part 3: Mixed Pricing Solution

**Strategy:**
- Extend Part 2's solution with conditional logic based on the `type` field
- When `type == "fixed"`: Add the cost value directly (not multiplied by quantity)
- When `type == "incremental"`: Use Part 2's logic (quantity × cost)
- Handle tier capacity calculation the same way for both types

**Key Difference:**
```python
# In the tier processing loop:
if tier["type"] == "fixed":
    total += cost  # Fixed fee for this tier
else:  # incremental
    total += tier_units * cost  # Per-unit pricing
```

**Time Complexity:** O(n × t + m × t) - same as Part 2

**Example Implementation:**
```python
def calculate_shipping_cost(order, shipping_cost):
    country = order["country"]
    cost_map = {}

    for product_info in shipping_cost[country]:
        cost_map[product_info["product"]] = product_info["costs"]

    total = 0
    for item in order["items"]:
        product = item["product"]
        quantity = item["quantity"]
        remaining = quantity

        for tier in cost_map[product]:
            if remaining <= 0:
                break

            min_qty = tier["minQuantity"]
            max_qty = tier["maxQuantity"]
            cost = tier["cost"]
            pricing_type = tier["type"]

            # Calculate tier capacity
            if max_qty is None:
                tier_units = remaining
            else:
                tier_capacity = max_qty - min_qty
                tier_units = min(remaining, tier_capacity)

            # Apply pricing based on type
            if pricing_type == "fixed":
                total += cost
            else:  # incremental
                total += tier_units * cost

            remaining -= tier_units

    return total
```

---

## Follow-Up Discussion Topics

### Code Quality and Production Readiness

**Question:** If this code were to be deployed to production, how would you improve code readability and maintainability?

**Considerations:**
- **Separation of Concerns:** Extract tier processing into separate functions
- **Type Safety:** Add type hints and use dataclasses or TypedDict for structured data
- **Error Handling:** Validate inputs and handle missing data gracefully
- **Testing:** Write comprehensive unit tests covering edge cases
- **Configuration Validation:** Ensure shipping cost configurations are valid before runtime
- **Logging:** Add logging for debugging pricing calculations
- **Documentation:** Include docstrings explaining the pricing model

**Example Refactoring:**
```python
from typing import Dict, List, Optional
from dataclasses import dataclass

@dataclass
class TierCost:
    type: str
    min_quantity: int
    max_quantity: Optional[int]
    cost: int

def calculate_tier_cost(quantity: int, tier: TierCost) -> tuple[int, int]:
    """
    Calculate cost for a single tier and return (cost, units_consumed).
    """
    if tier.max_quantity is None:
        tier_units = quantity
    else:
        tier_capacity = tier.max_quantity - tier.min_quantity
        tier_units = min(quantity, tier_capacity)

    if tier.type == "fixed":
        calculated_cost = tier.cost
    else:
        calculated_cost = tier_units * tier.cost

    return calculated_cost, tier_units

def calculate_product_cost(quantity: int, tiers: List[TierCost]) -> int:
    """
    Calculate total cost for a product given its quantity and tier structure.
    """
    total = 0
    remaining = quantity

    for tier in tiers:
        if remaining <= 0:
            break

        tier_cost, units_consumed = calculate_tier_cost(remaining, tier)
        total += tier_cost
        remaining -= units_consumed

    return total
```

### Unsorted Tiers

**Question:** What if the cost tiers are not sorted by `minQuantity`?

**Approach:**
- Sort the tiers before processing: `sorted(tiers, key=lambda t: t["minQuantity"])`
- Add validation to detect overlapping or invalid tier ranges
- Time complexity increases slightly due to sorting: O(t log t) per product