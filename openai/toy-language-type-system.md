# Toy Language Type System

## Problem Overview

You are tasked with implementing a type system for a toy programming language. This language supports:

- **Primitives**: `int`, `float`, `str`
- **Generics**: Uppercase letters with optional numbers, e.g., `T`, `T1`, `T2`, `S`
- **Tuples**: Lists of types enclosed in brackets, which can contain primitives, generics, or nested tuples
  - Examples: `[int, T1, str]`, `[int, str, [int, T1]]`
- **Functions**: Defined with parameters and a return type
  - Syntax: `[param1, param2, ...] -> returnType`
  - Example: `[int, [int, T1], T2] -> [str, T2, [float, float]]`

You need to implement two classes: `Node` and `Function`.

## Class Specifications

### Node Class

The `Node` class represents a type in the language, which can be either:
- A primitive or generic type (single value)
- A tuple (list of child nodes)

**Constructor:**
```python
def __init__(self, node_type: Union[str, List['Node']])
```
- If `node_type` is a string: represents a primitive or generic type
- If `node_type` is a list: represents a tuple containing child nodes

**Key Methods:**
- `__str__()`: Returns the string representation of the node (see Part 1)

### Function Class

The `Function` class represents a function signature with parameters and a return type.

**Constructor:**
```python
def __init__(self, parameters: List[Node], output_type: Node)
```
- `parameters`: List of `Node` objects representing function parameter types
- `output_type`: A `Node` object representing the return type

**Key Methods:**
- `__str__()`: Returns the string representation of the function (see Part 1)

## Part 1: String Representation

Implement the `__str__()` method for both `Node` and `Function` classes.

### Node String Format

- **Primitive/Generic types**: Return the type name as-is
  - Example: `int` → `"int"`, `T1` → `"T1"`
- **Tuples**: Return comma-separated types enclosed in brackets
  - Example: `[int, float]` → `"[int,float]"`
  - Nested example: `[int, [str, T1]]` → `"[int,[str,T1]]"`

### Function String Format

- Format: `(param1,param2,...) -> returnType`
- Example: Function with parameters `[int, T1]` and return type `[T1, str]`
  - Output: `"(int,T1) -> [T1,str]"`

**Note:** You should write your own test cases for Part 1 to verify correctness.

## Part 2: Type Inference with Generic Substitution

Implement a function `get_return_type(parameters: List[Node], function: Function) -> Node` that:

1. Takes actual parameter types and a function definition
2. Resolves generic types (T1, T2, etc.) by matching actual parameters with function parameters
3. Returns the actual return type with all generics substituted
4. Raises errors for type mismatches or conflicts

### Input Guarantees

- All actual parameter types are **concrete** (no generics)
- No need for deep DFS traversal in parameters

### Error Conditions

Your implementation must detect and raise errors for:

1. **Argument count mismatch**: Number of parameters doesn't match function definition
2. **Type mismatch**: Concrete types don't match when they should
   - Example: Expecting `int` but got `str`
3. **Generic type conflict**: Same generic type is bound to different concrete types
   - Example: `T1` is bound to both `int` and `str`

### Examples

#### Example 1: Valid Type Inference

**Function Definition:**
```python
# [T1, T2, int, T1] -> [T1, T2]
func = Function(
    [Node('T1'), Node('T2'), Node('int'), Node('T1')],
    Node([Node('T1'), Node('T2')])
)
```

**Actual Parameters:**
```python
parameters = [Node('int'), Node('str'), Node('int'), Node('int')]
```

**Expected Return:**
```python
Node([Node('int'), Node('str')])  # "[int,str]"
```

**Explanation:**
- `T1` is bound to `int` (from 1st and 4th parameters)
- `T2` is bound to `str` (from 2nd parameter)
- Return type `[T1, T2]` becomes `[int, str]`

#### Example 2: Concrete Type Mismatch Error

**Function Definition:** Same as Example 1

**Actual Parameters:**
```python
parameters = [Node('int'), Node('str'), Node('float'), Node('int')]
```

**Expected:** Raise exception

**Explanation:**
- 3rd parameter expects concrete type `int` but receives `float`
- This is a type mismatch error

#### Example 3: Generic Type Conflict Error

**Function Definition:** Same as Example 1

**Actual Parameters:**
```python
parameters = [Node('int'), Node('str'), Node('int'), Node('str')]
```

**Expected:** Raise exception

**Explanation:**
- 1st parameter binds `T1` to `int`
- 4th parameter tries to bind `T1` to `str`
- This is a generic type conflict - same generic bound to different types

#### Example 4: Nested Tuples

**Function Definition:**
```python
# [[ T1, float ], T1] -> [T1, [T1, float]]
func = Function(
    [
        Node([Node('T1'), Node('float')]),
        Node('T1')
    ],
    Node([Node('T1'), Node([Node('T1'), Node('float')])])
)
```

**Actual Parameters:**
```python
parameters = [
    Node([Node('str'), Node('float')]),
    Node('str')
]
```

**Expected Return:**
```python
Node([Node('str'), Node([Node('str'), Node('float')])])
# "[str,[str,float]]"
```

**Explanation:**
- `T1` is bound to `str` from both parameters
- Return type substitutes `T1` with `str`

#### Example 5: Complex Function with Multiple Generics

**Function Definition:**
```python
# [[T1, float], T2, S] -> [S, T1]
func = Function(
    [
        Node([Node('T1'), Node('float')]),
        Node('T2'),
        Node('S')
    ],
    Node([Node('S'), Node('T1')])
)
```

**Actual Parameters:**
```python
parameters = [
    Node([Node('str'), Node('float')]),
    Node([Node('int'), Node('str')]),
    Node('int')
]
```

**Expected Return:**
```python
Node([Node('int'), Node('str')])
# "[int,str]"
```

**Explanation:**
- `T1` is bound to `str`
- `T2` is bound to `[int, str]` (tuple)
- `S` is bound to `int`
- Return type `[S, T1]` becomes `[int, str]`

## Implementation Hints

### Suggested Helper Methods

You may find it useful to implement:

1. **`is_generic_type(node: Node) -> bool`**: Check if a node contains any generic types
2. **`clone(node: Node) -> Node`**: Deep copy a node
3. **`bind_generics(func_param: Node, actual_param: Node, binding_map: dict)`**:
   - Recursively match function parameters with actual parameters
   - Populate the binding map with generic type mappings
   - Raise errors on conflicts
4. **`substitute_generics(node: Node, binding_map: dict) -> Node`**:
   - Replace all generic types in a node with their concrete bindings
   - Recursively handle nested tuples

### Algorithm Outline

```
1. Validate parameter count matches
2. Initialize empty binding_map = {}
3. For each (func_param, actual_param) pair:
   a. If func_param is a generic type:
      - Check if already bound in binding_map
      - If bound, verify it matches actual_param
      - If not bound, add to binding_map
   b. If func_param is concrete:
      - Verify it exactly matches actual_param
   c. If func_param is a tuple:
      - Recursively process each child
4. Substitute all generics in return type using binding_map
5. Return the substituted return type
```

## Test Cases for Part 2

The interviewer will provide comprehensive test cases for Part 2. Your implementation should pass all provided tests, which will cover:
- Basic generic substitution
- Nested tuple handling
- Type mismatch detection
- Generic conflict detection
- Edge cases with multiple generics

## Notes

- The parameter types in `get_return_type` are **always concrete** (no generic types like T1, T2)
- Focus on clear error messages when raising exceptions
- Consider edge cases like empty tuples, single-element tuples, and deeply nested structures

## Sample Solution

Below is a reference implementation demonstrating one approach to solving this problem:

```python
from typing import Union, List


class Node:
    def __init__(self, node_type: Union[str, List['Node']]):
        self.type_list = ['str', 'float', 'int']

        if isinstance(node_type, str):
            self.base = node_type
            self.children = []
        else:
            self.base = None
            self.children = node_type

    def get_content(self) -> Union[str, List['Node']]:
        if self.base:
            return self.base
        return self.children

    def is_base_generic_type(self):
        return self.base and self.base not in self.type_list

    def is_generic_type(self):
        if self.is_base_generic_type():
            return True
        return any([child.is_generic_type() for child in self.children])

    def clone(self):
        if self.base:
            return Node(self.base)
        return Node([child.clone() for child in self.children])

    def __str__(self) -> str:
        if self.base:
            return self.base

        node_types = []
        for child in self.children:
            node_types.append(str(child))

        return f"[{','.join(node_types)}]"

    def __eq__(self, other) -> bool:
        if not isinstance(other, Node):
            return False
        return str(other) == str(self)


class Function:
    def __init__(self, param: List[Node], output: Node):
        self.parameters = param
        self.output_type = output

    def __str__(self) -> str:
        param_str = ','.join([str(param) for param in self.parameters])
        output_str = str(self.output_type)
        return f'({param_str}) -> {output_str}'


def binding(func_param: Node, param: Node, binding_map: dict):
    # If func_param is a base generic type (e.g., T1, T2)
    if func_param.is_generic_type() and func_param.base:
        if func_param.base in binding_map and binding_map[func_param.base] != param:
            raise Exception(f'invocation argument type mismatched on {func_param} and {param}')
        if func_param.base not in binding_map:
            binding_map[func_param.base] = param
    # If both match exactly (concrete types)
    elif func_param == param:
        return
    # If both are tuples, recursively bind children
    elif not func_param.base and not param.base:
        # Check tuple lengths match
        if len(func_param.children) != len(param.children):
            raise Exception(f'tuple length mismatch: {func_param} vs {param}')
        for sub_func_node, sub_param_node in zip(func_param.children, param.children):
            binding(sub_func_node, sub_param_node, binding_map)
    else:
        raise Exception(f'mismatch parameter on {func_param} and {param}')


def replace_invocation_arguments(node: Node, binding_map: dict) -> Node:
    if not node.is_generic_type():
        return node.clone()

    if not node.children:
        cloned = binding_map[node.base].clone()
        return Node(cloned.get_content())

    return Node([replace_invocation_arguments(child, binding_map) for child in node.children])


def get_return_type(parameters: List[Node], function: Function) -> Node:
    if len(parameters) != len(function.parameters):
        raise Exception("Illegal Arguments")

    binding_map = {}

    for func_node, param_node in zip(function.parameters, parameters):
        binding(func_node, param_node, binding_map)

    if not function.output_type.is_generic_type():
        return function.output_type

    return replace_invocation_arguments(function.output_type, binding_map)


# Example usage
if __name__ == "__main__":
    # Test string representation
    node1 = Node('T')
    node2 = Node('float')
    node3 = Node('T')
    node4 = Node([node1, node2])
    node5 = Node([node4, node3])

    print(node5)  # Output: [[T,float],T]

    func = Function([node5, Node('S')], Node([Node('S'), Node('T')]))

    print(func)  # Output: ([[T,float],T],S) -> [S,T]

    # Test type inference
    node11 = Node('str')
    node22 = Node('float')
    node33 = Node('str')
    node44 = Node([node11, node22])
    node55 = Node([node44, node33])

    node = get_return_type([node55, Node([Node('float'), Node('int')])], func)
    print(node)  # Output: [[float,int],str]
```

### Key Implementation Details:

1. **`is_base_generic_type()`**: Checks if a base type is generic by verifying it's not in the primitive type list
2. **`is_generic_type()`**: Recursively checks if a node or any of its children contain generics
3. **`binding()`**: Recursively matches function parameters with actual parameters, populating the binding map and validating types
4. **`replace_invocation_arguments()`**: Substitutes all generic types in the return type with their concrete bindings
5. **Tuple length validation**: The solution includes a check to ensure tuple lengths match during binding (this is an important edge case)
