package com.squareup.moshi;

public class ParserStack {
  /*
   * Helper class for JsonReader
   */

  /*
   * The nesting stack. Using a manual array rather than an ArrayList saves 20%.
   */
  private int[] stack = new int[32];
  private int stackSize = 0;
  {
    stack[stackSize++] = JsonScope.EMPTY_DOCUMENT;
  }

  /*
   * The path members. It corresponds directly to stack: At indices where the
   * stack contains an object (EMPTY_OBJECT, DANGLING_NAME or NONEMPTY_OBJECT),
   * pathNames contains the name at this scope. Where it contains an array
   * (EMPTY_ARRAY, NONEMPTY_ARRAY) pathIndices contains the current index in
   * that array. Otherwise the value is undefined, and we take advantage of that
   * by incrementing pathIndices when doing so isn't useful.
   */
  private String[] pathNames = new String[32];
  private int[] pathIndices = new int[32];

  public int size() {
    return stackSize;
  }

  public void push(int newTop) {
    if (stackSize == stack.length) {
      int[] newStack = new int[stackSize * 2];
      int[] newPathIndices = new int[stackSize * 2];
      String[] newPathNames = new String[stackSize * 2];
      System.arraycopy(stack, 0, newStack, 0, stackSize);
      System.arraycopy(pathIndices, 0, newPathIndices, 0, stackSize);
      System.arraycopy(pathNames, 0, newPathNames, 0, stackSize);
      stack = newStack;
      pathIndices = newPathIndices;
      pathNames = newPathNames;
    }
    stack[stackSize++] = newTop;
  }

  public void pop() {
    stackSize--;
  }

  public int getTop() {
    return stack[stackSize - 1];
  }

  public void setTop(int element) {
    stack[stackSize - 1] = element;
  }

  public void nextName(String name) {
    pathNames[stackSize - 1] = name;
  }

  public void nextElement() {
    pathIndices[stackSize - 1]++;
  }

  public void beginArray() {
    push(JsonScope.EMPTY_ARRAY);
    pathIndices[stackSize - 1] = 0;
  }

  public void endArray() {
    stackSize--;
  }

  public void beginObject() {
    push(JsonScope.EMPTY_OBJECT);
  }

  public void endObject() {
    stackSize--;
    pathNames[stackSize] = null; // Free the last path name so that it can be garbage collected!
  }

  public void close() {
    stack[0] = JsonScope.CLOSED;
    stackSize = 1;
  }

  public void skipValue() {
    pathIndices[stackSize - 1]++;
    pathNames[stackSize - 1] = "null";
  }

  public String getPath() {
    StringBuilder result = new StringBuilder().append('$');
    for (int i = 0, size = stackSize; i < size; i++) {
      switch (stack[i]) {
        case JsonScope.EMPTY_ARRAY:
        case JsonScope.NONEMPTY_ARRAY:
          result.append('[').append(pathIndices[i]).append(']');
          break;

        case JsonScope.EMPTY_OBJECT:
        case JsonScope.DANGLING_NAME:
        case JsonScope.NONEMPTY_OBJECT:
          result.append('.');
          if (pathNames[i] != null) {
            result.append(pathNames[i]);
          }
          break;

        case JsonScope.NONEMPTY_DOCUMENT:
        case JsonScope.EMPTY_DOCUMENT:
        case JsonScope.CLOSED:
          break;
      }
    }
    return result.toString();
  }
}
