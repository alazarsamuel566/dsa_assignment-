import java.util.*;

public class MemoryManager {

    private static class FreeBlock {
        int start;
        int size;
        FreeBlock next;

        FreeBlock(int start, int size) {
            this.start = start;
            this.size = size;
            this.next = null;
        }

        int getEnd() {
            return start + size;
        }

        @Override
        public String toString() {
            return String.format("[%d-%d: FREE]", start, getEnd());
        }
    }

    private static class AllocatedBlock {
        int start;
        int size;

        AllocatedBlock(int start, int size) {
            this.start = start;
            this.size = size;
        }

        int getEnd() {
            return start + size;
        }
    }

    private FreeBlock freeListHead;
    private Map<String, AllocatedBlock> allocatedBlocks;
    private int totalSize;
    private static final int ALIGNMENT = 4;

    public MemoryManager() {
        this.allocatedBlocks = new HashMap<>();
    }

    public void init(int totalSize) {
        this.totalSize = totalSize;
        this.freeListHead = new FreeBlock(0, totalSize);
        this.allocatedBlocks.clear();
        System.out.println("Memory: " + formatMemory());
    }

    private int alignSize(int size) {
        int remainder = size % ALIGNMENT;

        if (remainder == 0) {
            return size;
        }

        int alignedSize = size + (ALIGNMENT - remainder);
        return alignedSize;
    }

    public void allocate(String id, int requestedSize) {

        if (allocatedBlocks.containsKey(id)) {
            System.out.println("Error: ID '" + id + "' already allocated.");
            return;
        }

        int size = alignSize(requestedSize);

        FreeBlock prev = null;
        FreeBlock current = freeListHead;

        while (current != null) {
            if (current.size >= size) {

                int allocStart = current.start;

                allocatedBlocks.put(id, new AllocatedBlock(allocStart, size));

                if (current.size == size) {

                    if (prev == null) {
                        freeListHead = current.next;
                    } else {
                        prev.next = current.next;
                    }
                } else {

                    current.start += size;
                    current.size -= size;
                }

                System.out.println("Allocated " + size + " bytes at " + allocStart + ".");
                System.out.println("Memory: " + formatMemory());
                return;
            }

            prev = current;
            current = current.next;
        }

        System.out.println("OUT OF MEMORY: Cannot allocate " + size + " bytes.");
    }

    public void free(String id) {
        AllocatedBlock block = allocatedBlocks.remove(id);

        if (block == null) {
            System.out.println("Error: ID '" + id + "' not found.");
            return;
        }

        System.out.println("Freed " + id + ".");
        insertAndCoalesce(block.start, block.size);

        System.out.println("Memory: " + formatMemory());
    }

    private void insertAndCoalesce(int start, int size) {
        FreeBlock newBlock = new FreeBlock(start, size);

        if (freeListHead == null) {
            freeListHead = newBlock;
            return;
        }

        if (start < freeListHead.start) {
            newBlock.next = freeListHead;
            freeListHead = newBlock;
            coalesceFromNode(null, freeListHead);
            return;
        }

        FreeBlock prev = null;
        FreeBlock current = freeListHead;

        while (current != null && current.start < start) {
            prev = current;
            current = current.next;
        }

        newBlock.next = current;
        if (prev != null) {
            prev.next = newBlock;
        }

        coalesceFromNode(prev, newBlock);
    }

    private void coalesceFromNode(FreeBlock prev, FreeBlock node) {
        if (node == null)
            return;

        boolean coalesced = false;

        while (node.next != null && node.getEnd() == node.next.start) {
            FreeBlock next = node.next;
            node.size += next.size;
            node.next = next.next;
            coalesced = true;
        }

        if (prev != null && prev.getEnd() == node.start) {
            prev.size += node.size;
            prev.next = node.next;
            coalesced = true;

            coalesceFromNode(null, prev);
        }

        if (coalesced) {
            System.out.println("Coalescing...");
        }
    }

    private String formatMemory() {
        List<MemorySegment> segments = new ArrayList<>();

        FreeBlock current = freeListHead;
        while (current != null) {
            segments.add(new MemorySegment(current.start, current.getEnd(), "FREE", null));
            current = current.next;
        }

        for (Map.Entry<String, AllocatedBlock> entry : allocatedBlocks.entrySet()) {
            AllocatedBlock block = entry.getValue();
            segments.add(new MemorySegment(block.start, block.getEnd(), "ALLOC", entry.getKey()));
        }

        segments.sort(Comparator.comparingInt(s -> s.start));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < segments.size(); i++) {
            MemorySegment seg = segments.get(i);
            if (seg.type.equals("FREE")) {
                sb.append(String.format("[%d-%d: FREE]", seg.start, seg.end));
            } else {
                sb.append(String.format("[%d-%d: %s]", seg.start, seg.end, seg.id));
            }

            if (i < segments.size() - 1) {
                sb.append(" -> ");
            }
        }

        return sb.toString();
    }

    public void inspect() {
        System.out.println("Memory: " + formatMemory());
    }

    private static class MemorySegment {
        int start;
        int end;
        String type;
        String id;

        MemorySegment(int start, int end, String type, String id) {
            this.start = start;
            this.end = end;
            this.type = type;
            this.id = id;
        }
    }

    public static void main(String[] args) {
        MemoryManager mm = new MemoryManager();
        Scanner scanner = new Scanner(System.in);

        System.out.println("First-Fit Memory Manager");
        System.out.println("Commands: INIT <size>, ALLOC <id> <size>, FREE <id>, INSPECT, EXIT");
        System.out.println();

        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine().trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] parts = line.split("\\s+");
            String command = parts[0].toUpperCase();

            try {
                switch (command) {
                    case "INIT":
                        if (parts.length < 2) {
                            System.out.println("Usage: INIT <total_size>");
                            break;
                        }
                        int totalSize = Integer.parseInt(parts[1]);
                        mm.init(totalSize);
                        break;

                    case "ALLOC":
                        if (parts.length < 3) {
                            System.out.println("Usage: ALLOC <id> <size>");
                            break;
                        }
                        String allocId = parts[1];
                        int allocSize = Integer.parseInt(parts[2]);
                        mm.allocate(allocId, allocSize);
                        break;

                    case "FREE":
                        if (parts.length < 2) {
                            System.out.println("Usage: FREE <id>");
                            break;
                        }
                        String freeId = parts[1];
                        mm.free(freeId);
                        break;

                    case "INSPECT":
                        mm.inspect();
                        break;

                    case "EXIT":
                    case "QUIT":
                        System.out.println("Goodbye!");
                        scanner.close();
                        return;

                    default:
                        System.out.println("Unknown command: " + command);
                        System.out.println("Available commands: INIT, ALLOC, FREE, INSPECT, EXIT");
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid number format.");
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }

            System.out.println();
        }
    }
}
