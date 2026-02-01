import java.util.*;

public class DisasterTriageSystem {

    static int currentTime = 0;

    static int surgeons = 0;
    static int nurses = 0;
    static int isolationUnits = 0;

    static PriorityQueue<Patient> waitingHeap;
    static Queue<Patient> quarantineQueue = new LinkedList<>();
    static List<Patient> treatingPatients = new ArrayList<>();
    static Map<String, Patient> allPatients = new HashMap<>();

    public static void main(String[] args) {

        waitingHeap = new PriorityQueue<>(
                (b, a) -> Double.compare(a.getEffectiveScore(currentTime),
                        b.getEffectiveScore(currentTime))
        );

        Scanner sc = new Scanner(System.in);

        while (sc.hasNextLine()) {
            String input = sc.nextLine();
            if (input.isEmpty()) continue;

            String[] cmd = input.split(" ");

            switch (cmd[0]) {
                case "ADD_RESOURCE":
                    addResource(cmd[1], Integer.parseInt(cmd[2]));
                    break;
                case "ADD_PATIENT":
                    addPatient(cmd[1], Integer.parseInt(cmd[2]), cmd[3]);
                    break;
                case "TICK":
                    tick();
                    break;
                case "PATIENT_REPORT":
                    report(cmd[1]);
                    break;
                case "STATUS":
                    status();
                    break;
            }
        }
        sc.close();
    }

    /* ================= COMMANDS ================= */

    static void addResource(String type, int count) {
        if (type.equals("Surgeon")) surgeons += count;
        if (type.equals("Nurse")) nurses += count;
        if (type.equals("IsolationUnit")) isolationUnits += count;
    }

    static void addPatient(String name, int severity, String type) {
        Patient p = new Patient(name, severity, type, currentTime);
        allPatients.put(name, p);

        if (type.equals("Viral") && isolationUnits == 0) {
            quarantineQueue.add(p);
        } else {
            waitingHeap.add(p);
        }
    }

    static void tick() {
        currentTime++;
        System.out.println("\nTime " + currentTime + ":");

        // 1. Update treating patients
        for (Patient p : treatingPatients) {
            p.treatmentRemaining--;
            if (p.treatmentRemaining > 0) {
                System.out.println("- " + p.name + " still in treatment.");
            }
        }

        // Remove finished
        treatingPatients.removeIf(p -> {
            if (p.treatmentRemaining == 0 && p.status == Status.TREATING) {
                releaseResource(p);
                p.status = Status.DISCHARGED;
                return true;
            }
            return false;
        });

        // 2. Rebuild heap for updated scores
        rebuildHeap();

        // 3. Process quarantine
        while (isolationUnits > 0 && !quarantineQueue.isEmpty()) {
            Patient p = quarantineQueue.poll();
            isolationUnits--;
            p.status = Status.TREATING;
            p.treatmentRemaining = 3;
            treatingPatients.add(p);
            System.out.println("- " + p.name + " (Score " + p.getEffectiveScore(currentTime) +
                    "): Assigned to Isolation Unit.");
        }

        // 4. Process waiting heap
        while (!waitingHeap.isEmpty()) {
            Patient p = waitingHeap.peek();

            if (!canAssign(p)) break;

            waitingHeap.poll();
            assign(p);
        }

        // 5. Print waiting patients
        for (Patient p : waitingHeap) {
            System.out.println("- " + p.name + " (Score " + p.getEffectiveScore(currentTime) + "): WAITING.");
        }

        for (Patient p : quarantineQueue) {
            System.out.println("- " + p.name + " (Score " + p.getEffectiveScore(currentTime) +
                    "): WAITING (Quarantine).");
        }
    }

    static void report(String name) {
        Patient p = allPatients.get(name);
        if (p != null)
            System.out.println(p.name + " is " + p.status);
    }

    static void status() {
        System.out.println("\nSurgeons: " + surgeons);
        System.out.println("Nurses: " + nurses);
        System.out.println("Isolation Units: " + isolationUnits);
        System.out.println("Waiting: " + waitingHeap.size());
        System.out.println("Quarantine: " + quarantineQueue.size());
        System.out.println("Treating: " + treatingPatients.size());
    }

    /* ================= LOGIC ================= */

    static boolean canAssign(Patient p) {
        if (p.type.equals("Trauma")) return surgeons > 0;
        if (p.type.equals("General")) return surgeons > 0 || nurses > 0;
        return false;
    }

    static void assign(Patient p) {
        if (p.type.equals("Trauma")) surgeons--;
        else if (p.type.equals("General")) {
            if (nurses > 0) nurses--;
            else surgeons--;
        }
        p.status = Status.TREATING;
        p.treatmentRemaining = 3;
        treatingPatients.add(p);
        System.out.println("- " + p.name + " (Score " + p.getEffectiveScore(currentTime) + "): Assigned.");
    }

    static void releaseResource(Patient p) {
        if (p.type.equals("Trauma") || p.type.equals("General")) surgeons++;
        else isolationUnits++;
    }

    static void rebuildHeap() {
        List<Patient> temp = new ArrayList<>(waitingHeap);
        waitingHeap.clear();
        waitingHeap.addAll(temp);
    }
}

/* ================= PATIENT ================= */

class Patient {
    String name;
    int severity;
    String type;
    int arrivalTime;
    int treatmentRemaining = 0;
    Status status = Status.WAITING;

    Patient(String n, int s, String t, int time) {
        name = n;
        severity = s;
        type = t;
        arrivalTime = time;
    }

    double getEffectiveScore(int currentTime) {
        return severity + ((currentTime - arrivalTime) * 0.5);
    }
}

/* ================= STATUS ================= */

enum Status {
    WAITING, TREATING, DISCHARGED
}
