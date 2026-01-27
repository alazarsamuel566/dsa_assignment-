# First-Fit Memory Manager - Java Solution

## Overview
This is a complete Java implementation of a First-Fit Memory Manager with automatic coalescing, as specified in Problem 05. The system simulates memory management for an operating system using the first-fit allocation strategy.

## Features Implemented

### ✅ Core Requirements
- **First-Fit Allocation**: Scans free blocks from the beginning and allocates the first block that fits
- **Memory Coalescing**: Automatically merges adjacent free blocks to reduce fragmentation
- **4-Byte Alignment**: All allocations are rounded up to multiples of 4 bytes
- **Singly Linked List**: Free blocks are stored in a sorted linked list by memory address
- **HashMap**: Tracks allocated blocks by ID for quick lookup during deallocation

### ✅ Operations
- `INIT <total_size>` - Initialize memory with specified size
- `ALLOC <id> <size>` - Allocate memory for an object with given ID
- `FREE <id>` - Free memory associated with an ID
- `INSPECT` - Display current memory layout
- `EXIT` - Quit the program

## Data Structures

### FreeBlock (Singly Linked List Node)
```java
class FreeBlock {
    int start;      // Starting address
    int size;       // Block size
    FreeBlock next; // Next free block (sorted by address)
}
```

### AllocatedBlock
```java
class AllocatedBlock {
    int start;  // Starting address
    int size;   // Allocated size
}
```

### Storage
- **Free List**: Singly linked list of free blocks (sorted by address)
- **Allocated Map**: HashMap<String, AllocatedBlock> for O(1) lookup

## Key Algorithms

### 1. First-Fit Allocation
1. Align requested size to 4-byte boundary
2. Scan free list from head
3. Find first block with `size >= requested_size`
4. If exact fit: remove block from free list
5. If larger: split block and update free list
6. Record allocation in HashMap

### 2. Coalescing (Merging Adjacent Free Blocks)
When freeing memory:
1. Insert freed block into sorted free list
2. Check if adjacent to previous block (prev.end == current.start)
3. Check if adjacent to next block (current.end == next.start)
4. Merge all adjacent blocks into one larger block

### 3. 4-Byte Alignment
```java
aligned_size = ((size + 3) / 4) * 4
```
Examples:
- 13 bytes → 16 bytes
- 20 bytes → 20 bytes
- 25 bytes → 28 bytes

## Compilation and Execution (Instruction to run)

### Compile
```bash
javac MemoryManager.java
```

### Run Interactively
```bash
java MemoryManager
```

### Run with Input File
```bash
java MemoryManager < test_input.txt
```

## Example Usage

### Interactive Session
```
> INIT 100
Memory: [0-100: FREE]

> ALLOC A 20
Allocated 20 bytes at 0.
Memory: [0-20: A] -> [20-100: FREE]

> ALLOC B 30
Allocated 32 bytes at 20.
Memory: [0-20: A] -> [20-52: B] -> [52-100: FREE]

> FREE A
Freed A.
Memory: [0-20: FREE] -> [20-52: B] -> [52-100: FREE]

> FREE B
Freed B.
Coalescing...
Memory: [0-100: FREE]
```

Note: B requested 30 bytes but got 32 due to 4-byte alignment.

## Test Files Included

### test_input.txt
Basic test matching the problem's sample execution

### test_extended.txt
Extended test demonstrating:
- Alignment (13 bytes → 16 bytes)
- Fragmentation and coalescing
- First-fit strategy with multiple free blocks

## Edge Cases Handled

1. **Duplicate ID**: Prevents allocating same ID twice
2. **Invalid Free**: Error message when freeing non-existent ID
3. **Out of Memory**: Proper message when no suitable block exists
4. **Exact Fit**: Correctly removes block from free list
5. **Multiple Coalescing**: Merges 3+ adjacent blocks in one operation
6. **Empty Free List**: Handles allocation when no free blocks exist

## Time Complexity

- **INIT**: O(1)
- **ALLOC**: O(n) where n = number of free blocks (first-fit scan)
- **FREE**: O(n) for insertion + O(1) for coalescing neighbors
- **INSPECT**: O(n + m) where n = free blocks, m = allocated blocks

## Space Complexity

- O(n + m) where n = free blocks, m = allocated blocks

## Design Decisions

1. **Sorted Free List**: Maintains blocks sorted by address to enable efficient coalescing
2. **HashMap for Allocations**: Provides O(1) lookup when freeing memory
3. **Automatic Alignment**: Transparent to user, happens during allocation
4. **Immediate Coalescing**: Reduces fragmentation as soon as blocks are freed

## Author
Solution for Problem 05: The First-Fit Memory Auditor
