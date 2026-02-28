# Design In-Memory File System

## Problem Overview

Design and implement an **in-memory file system** that supports:

- Listing directory contents
- Creating nested directories
- Creating files and appending content
- Reading file content

This is similar to [LeetCode 588 - Design In-Memory File System](https://leetcode.com/problems/design-in-memory-file-system/description/), and is a common Snowflake interview question around data structures and API design.

### Key Design Considerations

1. **Path traversal**: Efficiently parse and navigate absolute paths like `/a/b/c`.
2. **Directory vs file behavior**: `ls(path)` behaves differently for files vs directories.
3. **Content appending**: Writes should append to an existing file rather than overwrite.
4. **Sorted output**: Directory listings should be returned in lexicographic order.

---

## Problem Statement

Implement the `FileSystem` class:

```python
from typing import List

class FileSystem:
    def __init__(self):
        """Initialize the in-memory filesystem."""
        pass

    def ls(self, path: str) -> List[str]:
        """
        If path is a file path, return [filename].
        If path is a directory path, return sorted names of files and directories directly under it.
        """
        pass

    def mkdir(self, path: str) -> None:
        """
        Create a directory path recursively.
        Intermediate directories that do not exist should be created.
        """
        pass

    def addContentToFile(self, filePath: str, content: str) -> None:
        """
        Create file if missing, then append content to it.
        """
        pass

    def readContentFromFile(self, filePath: str) -> str:
        """
        Return full file content.
        """
        pass
```

### Constraints

- `1 <= path.length, filePath.length <= 100`
- Paths are absolute and start with `/`
- `1 <= content.length <= 50`
- At most `300` API calls in total
- All operations are valid

### Example 1

```python
fs = FileSystem()
print(fs.ls("/"))                     # []
fs.mkdir("/a/b/c")
fs.addContentToFile("/a/b/c/d", "hello")
print(fs.ls("/"))                     # ["a"]
print(fs.readContentFromFile("/a/b/c/d"))  # "hello"
```

### Example 2

```python
fs = FileSystem()
fs.mkdir("/x/y")
fs.addContentToFile("/x/y/file", "abc")
fs.addContentToFile("/x/y/file", "def")
print(fs.readContentFromFile("/x/y/file"))  # "abcdef"
print(fs.ls("/x/y/file"))                   # ["file"]
```

---

## Design Approach

Use a **tree (trie-like) structure**:

- Each node represents a directory or file
- `children: dict[str, Node]` stores nested files/directories
- `is_file: bool` tells if node is a file
- `content: str` stores file contents

### Why this works well

1. Path segments naturally map to tree edges.
2. `mkdir` and file creation become path traversal with optional node creation.
3. `ls` is easy:
   - File path: return just the file name
   - Directory path: return sorted child names

---

## Solution

```python
from typing import Dict, List


class Node:
    def __init__(self):
        self.children: Dict[str, Node] = {}
        self.is_file = False
        self.content = ""


class FileSystem:
    def __init__(self):
        self.root = Node()

    def _parts(self, path: str) -> List[str]:
        if path == "/":
            return []
        return [part for part in path.split("/") if part]

    def _traverse(self, path: str, create: bool = False) -> Node:
        node = self.root
        for part in self._parts(path):
            if part not in node.children:
                if not create:
                    raise KeyError(f"Path does not exist: {path}")
                node.children[part] = Node()
            node = node.children[part]
        return node

    def ls(self, path: str) -> List[str]:
        if path == "/":
            return sorted(self.root.children.keys())

        parts = self._parts(path)
        node = self._traverse(path)

        # If path points to a file, return only the file name.
        if node.is_file:
            return [parts[-1]]

        return sorted(node.children.keys())

    def mkdir(self, path: str) -> None:
        self._traverse(path, create=True)

    def addContentToFile(self, filePath: str, content: str) -> None:
        file_node = self._traverse(filePath, create=True)
        file_node.is_file = True
        file_node.content += content

    def readContentFromFile(self, filePath: str) -> str:
        file_node = self._traverse(filePath)
        return file_node.content
```

---

## Complexity Analysis

Let:

- `L` = number of path segments (depth)
- `K` = number of entries in a listed directory

| Operation             | Time          | Space |
| --------------------- | ------------- | ----- |
| `mkdir(path)`         | O(L)          | O(L) in worst-case new nodes |
| `addContentToFile`    | O(L + C)      | O(L) for new path nodes, O(C) appended content |
| `readContentFromFile` | O(L)          | O(1) |
| `ls(path)`            | O(L + K logK) | O(K) for sorted output |

---

## Common Interview Follow-Ups

1. **How would you support delete operations?**
   - Add `rm(path)` for file delete and `rmdir(path)` for empty directory remove.
2. **How would you support very large files?**
   - Store content in chunk lists instead of one large string.
3. **How would you make this thread-safe?**
   - Use fine-grained locks per node or read-write locks for higher read concurrency.
4. **How would you persist this to disk?**
   - Add snapshot + write-ahead-log so the in-memory tree can be reconstructed.
