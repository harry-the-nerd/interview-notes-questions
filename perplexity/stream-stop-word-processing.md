# Stream Processing with Stop Words

## Problem Overview

Given an infinite character stream (or a very large text stream that cannot fit into memory) and a list of stop words (sensitive words), return the substring that appears before the first occurrence of any stop word.

**Constraints:**
1. **Memory Efficient**: The input is extremely large and cannot be loaded into memory all at once. It must be read in chunks.
2. **Python Generator**: Must use the `yield` keyword to implement streaming processing.
3. **Cross-Chunk Handling**: A stop word may be split across two consecutive chunks, and the system must correctly identify it.

**Example:**
```python
stop_words = ["<stop>", "<end>"]
stream_chunks = ["This is a te", "st<st", "op> message"]

# Expected output: "This is a test"
# Reason: "<stop>" is split across chunks 2 and 3
```

---

## Part 1: Core Algorithm

### Key Challenge: Cross-Chunk Stop Words

The most critical difficulty in this problem is handling stop words that are **split across chunk boundaries**. For example:
- Chunk 1: `"...te"`
- Chunk 2: `"st<st"`
- Chunk 3: `"op>..."`

The word `<stop>` is actually split across chunks 2 and 3. If we only check within each chunk, we will miss it.

### Solution: Buffer-Based Sliding Window

**Core Idea:**
1. Maintain a **buffer** to store unconsumed characters from the previous chunk.
2. When processing each new chunk, prepend the buffer to it.
3. After processing, save the last few characters (length = `max_stop_word_length - 1`) to the buffer for the next iteration.
4. This ensures that stop words spanning chunk boundaries can be detected.

**Why `max_stop_word_length - 1`?**
- If the longest stop word is 6 characters, we only need to keep the last 5 characters from the current chunk.
- When combined with the first character of the next chunk, we can form a complete 6-character window to check for stop words.

---

## Part 2: Implementation

### Generator-Based Stream Processor

```python
from typing import Iterator, List, Optional

def process_stream_with_stopwords(
    stream: Iterator[str],
    stop_words: List[str]
) -> Iterator[str]:
    """
    Process a character stream and yield characters until a stop word is found.

    Args:
        stream: An iterator yielding string chunks
        stop_words: A list of stop words to detect

    Yields:
        Characters before the first stop word
    """
    if not stop_words:
        # If no stop words, yield the entire stream
        for chunk in stream:
            yield chunk
        return

    # Calculate the maximum stop word length
    max_stop_len = max(len(word) for word in stop_words)

    # Buffer to store characters from the previous chunk
    buffer = ""

    for chunk in stream:
        # Combine buffer with current chunk
        text = buffer + chunk

        # Check for stop words in the combined text
        earliest_pos = len(text)  # Initialize to end of text
        found_stop_word = None

        for stop_word in stop_words:
            pos = text.find(stop_word)
            if pos != -1 and pos < earliest_pos:
                earliest_pos = pos
                found_stop_word = stop_word

        if found_stop_word:
            # Stop word found - yield characters before it and stop
            if earliest_pos > 0:
                yield text[:earliest_pos]
            return

        # No stop word found in this chunk
        # Yield all except the last (max_stop_len - 1) characters
        safe_to_yield_len = max(0, len(text) - (max_stop_len - 1))

        if safe_to_yield_len > 0:
            yield text[:safe_to_yield_len]
            # Update buffer with the remaining characters
            buffer = text[safe_to_yield_len:]
        else:
            # If text is too short, keep everything in buffer
            buffer = text

    # After all chunks are processed, yield any remaining buffer
    if buffer:
        yield buffer


def extract_text_before_stopword(
    stream: Iterator[str],
    stop_words: List[str]
) -> str:
    """
    Convenience function that returns the complete string before the first stop word.

    Args:
        stream: An iterator yielding string chunks
        stop_words: A list of stop words to detect

    Returns:
        The concatenated string before the first stop word
    """
    return ''.join(process_stream_with_stopwords(stream, stop_words))
```

### Usage Example

```python
# Example 1: Stop word split across chunks
def create_stream_1():
    chunks = ["This is a te", "st<st", "op> message"]
    for chunk in chunks:
        yield chunk

stop_words = ["<stop>", "<end>"]
result = extract_text_before_stopword(create_stream_1(), stop_words)
print(result)  # Output: "This is a test"


# Example 2: Stop word entirely within one chunk
def create_stream_2():
    chunks = ["Hello world", " <stop> more", " text"]
    for chunk in chunks:
        yield chunk

result = extract_text_before_stopword(create_stream_2(), stop_words)
print(result)  # Output: "Hello world "


# Example 3: No stop word found
def create_stream_3():
    chunks = ["This is ", "a normal ", "text"]
    for chunk in chunks:
        yield chunk

result = extract_text_before_stopword(create_stream_3(), stop_words)
print(result)  # Output: "This is a normal text"
```

---

## Part 3: Edge Cases & Optimizations

### Critical Edge Cases

1. **Empty Stream**
   ```python
   stream = iter([])
   result = extract_text_before_stopword(stream, ["<stop>"])
   # Expected: ""
   ```

2. **Stop Word at Beginning**
   ```python
   stream = iter(["<stop>", "text"])
   result = extract_text_before_stopword(stream, ["<stop>"])
   # Expected: ""
   ```

3. **Multiple Overlapping Stop Words**
   ```python
   stream = iter(["test<st", "op><end>more"])
   stop_words = ["<stop>", "<end>"]
   # Should stop at "<stop>", not "<end>"
   # Expected: "test"
   ```

4. **Very Small Chunks**
   ```python
   stream = iter(["<", "s", "t", "o", "p", ">"])
   # Each character is a separate chunk
   # Must still detect "<stop>"
   ```

5. **Stop Word Longer Than Chunk Size**
   ```python
   stream = iter(["a", "b", "<", "s", "t", "o", "p", ">", "c"])
   stop_words = ["<stop>"]
   # Expected: "ab"
   ```

### Performance Optimizations

1. **Use Trie Data Structure for Multiple Stop Words**

   If the number of stop words is very large (e.g., thousands), using a Trie (prefix tree) can significantly speed up the search:

   ```python
   class TrieNode:
       def __init__(self):
           self.children = {}
           self.is_end_of_word = False

   class Trie:
       def __init__(self, words: List[str]):
           self.root = TrieNode()
           for word in words:
               self.insert(word)

       def insert(self, word: str):
           node = self.root
           for char in word:
               if char not in node.children:
                   node.children[char] = TrieNode()
               node = node.children[char]
           node.is_end_of_word = True

       def find_earliest_match(self, text: str) -> Optional[int]:
           """Find the position of the earliest stop word in text."""
           for start_pos in range(len(text)):
               node = self.root
               for i in range(start_pos, len(text)):
                   char = text[i]
                   if char not in node.children:
                       break
                   node = node.children[char]
                   if node.is_end_of_word:
                       return start_pos
           return None

   # Usage in the main function:
   # trie = Trie(stop_words)
   # earliest_pos = trie.find_earliest_match(text)
   ```

   **Time Complexity:**
   - Linear search: $O(n \times m \times k)$ where $n$ = text length, $m$ = number of stop words, $k$ = average stop word length
   - Trie-based: $O(n^2)$ in worst case, but much faster in practice

2. **Aho-Corasick Algorithm**

   For production systems with many stop words, the Aho-Corasick algorithm provides $O(n + m + z)$ complexity where $z$ is the number of matches. This is optimal for multi-pattern matching.

### Memory Complexity Analysis

- **Buffer Size**: $O(L)$ where $L$ is the length of the longest stop word
- **Stop Words Storage**: $O(m \times k)$ where $m$ = number of stop words, $k$ = average length
- **Total Space**: $O(L + m \times k)$, independent of the stream size

This is critical for handling streams that are gigabytes or terabytes in size.

---

## Key Discussion Points in Interview

### 1. Why Use Generators?

**Memory Efficiency:**
- Generators process data lazily (on-demand) using `yield`.
- Only one chunk is in memory at a time, plus a small buffer.
- This allows processing of unlimited stream sizes.

**Comparison:**
```python
# BAD: Loads entire stream into memory
def bad_approach(stream):
    full_text = ''.join(stream)  # Out of memory for large streams!
    # ... process full_text

# GOOD: Processes incrementally
def good_approach(stream):
    for chunk in stream:
        yield process(chunk)  # Only one chunk in memory
```

### 2. Buffer Size Calculation

**Question:** "Why is the buffer size `max_stop_len - 1`?"

**Answer:**
- Suppose the longest stop word is `"<stop>"` (6 characters).
- To detect it across chunk boundaries, we need to ensure we can see at least 6 consecutive characters.
- By keeping the last 5 characters from chunk $n$ and prepending them to chunk $n+1$, we guarantee any 6-character pattern spanning the boundary will be visible.

**Visual Example:**
```
Chunk 1: "...ABC" → buffer = "ABC" (assuming max_stop_len = 4)
Chunk 2: "DEF..." → text = "ABCDEF..."
Now we can detect "ABCD", "BCDE", "CDEF" spanning the boundary
```

### 3. Python-Specific Tricks

**Interviewer might ask:** "How would you implement this in production?"

**Key Points:**
- Use `collections.deque` for the buffer if frequent append/pop operations are needed (though for string it's not necessary here).
- Consider using `re.search()` for regex-based stop word matching.
- Use `io.StringIO` for testing with mock streams.
- Handle character encoding properly (UTF-8, especially for non-English text).

### 4. Alternative Approaches

**Regex-Based Solution:**
```python
import re

def process_with_regex(stream, stop_words):
    # Escape special regex characters
    escaped = [re.escape(word) for word in stop_words]
    pattern = '|'.join(escaped)  # "word1|word2|word3"
    regex = re.compile(pattern)

    buffer = ""
    max_stop_len = max(len(word) for word in stop_words)

    for chunk in stream:
        text = buffer + chunk
        match = regex.search(text)

        if match:
            yield text[:match.start()]
            return

        safe_len = max(0, len(text) - (max_stop_len - 1))
        if safe_len > 0:
            yield text[:safe_len]
            buffer = text[safe_len:]
        else:
            buffer = text

    if buffer:
        yield buffer
```

**Trade-offs:**
- **Pros**: More concise, handles regex patterns natively
- **Cons**: Regex compilation overhead, slightly harder to debug

---

## Related Problems

- **LeetCode 76**: Minimum Window Substring (similar sliding window concept)
- **LeetCode 438**: Find All Anagrams in a String (character window)
- **Text Streaming Processing**: Log file parsing, real-time text analysis
- **Tokenization**: Breaking text into words/sentences with delimiters