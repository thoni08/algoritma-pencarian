
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

// =============================================================================
// Eight Puzzle Solver - Best-First Search dengan Priority Queue
//
// APA ITU BEST-FIRST SEARCH?
// Algoritma pencarian yang memilih node berikutnya berdasarkan nilai FITNESS
// (heuristik) — node yang paling "menjanjikan" diproses lebih dahulu.
//
// PERBEDAAN DENGAN BFS DAN DFS:
// ┌─────────────┬────────────────────────┬────────────────────────────────────┐
// │             │ BFS / DFS              │ Best-First Search (PQ)             │
// ├─────────────┼────────────────────────┼────────────────────────────────────┤
// │ Struktur    │ Queue (FIFO) /         │ PriorityQueue                      │
// │ data        │ Stack (LIFO)           │ node fitness terkecil keluar duluan│
// ├─────────────┼────────────────────────┼────────────────────────────────────┤
// │ Urutan      │ Berdasarkan waktu      │ Berdasarkan nilai FITNESS node     │
// │ proses      │ masuk antrean          │ (bukan urutan kedatangan)          │
// ├─────────────┼────────────────────────┼────────────────────────────────────┤
// │ Arah        │ Merata (BFS) /         │ Menuju ke arah yang paling dekat   │
// │ jelajah     │ Menyelam (DFS)         │ ke tujuan menurut heuristik        │
// └─────────────┴────────────────────────┴────────────────────────────────────┘
//
// NILAI FITNESS (HEURISTIK)
// Program ini menggunakan Manhattan Distance sebagai nilai fitness:
//
//   h(n) = total jarak Manhattan semua angka dari posisi sekarang ke posisi tujuan
//
//   Jarak Manhattan antara dua titik (r1,c1) dan (r2,c2):
//   |r1 - r2| + |c1 - c2|
//
//   Contoh — state saat ini vs tujuan:
//   Sekarang:  2 | 8 | 3      Tujuan:  1 | 2 | 3
//              ---------               ---------
//              1 | 6 | 4               8 | 0 | 4
//              ---------               ---------
//              7 | 0 | 5               7 | 6 | 5
//
//   Angka 2: sekarang (0,0), tujuan (0,1) → |0-0|+|0-1| = 1
//   Angka 8: sekarang (0,1), tujuan (1,0) → |0-1|+|1-0| = 2
//   Angka 1: sekarang (1,0), tujuan (0,0) → |1-0|+|0-0| = 1
//   Angka 6: sekarang (1,1), tujuan (2,1) → |1-2|+|1-1| = 1
//   ... dst (angka 0 diabaikan karena ruang kosong)
//   Total h(n) = 1+2+1+1+... = estimasi jumlah langkah minimum ke tujuan
//
// Node dengan h(n) TERKECIL = paling dekat ke tujuan → diproses duluan.
//
// REPRESENTASI PUZZLE (sama dengan BFS dan DFS)
// Papan 3x3 disimpan sebagai String 9 karakter, dibaca baris per baris:
//
//   indeks:  0 | 1 | 2   -> baris pertama  (atas)
//            ---------
//            3 | 4 | 5   -> baris kedua    (tengah)
//            ---------
//            6 | 7 | 8   -> baris ketiga   (bawah)
//
// STRUKTUR DATA YANG DIGUNAKAN
// - PriorityQueue (openPQ)  : antrean berprioritas. Node dengan fitness TERKECIL
//                             selalu berada di depan dan diambil duluan.
//                             Di Java, PriorityQueue secara default adalah min-heap.
//
// - Set (visitedStates)     : daftar state yang sudah pernah dikunjungi.
// =============================================================================
public class EightPuzzlePQ {

    private final String initialState;
    private final String goalState;

    // PriorityQueue dengan comparator: node fitness terkecil diproses duluan (min-heap).
    // Comparator: (a, b) -> a.getFitness() - b.getFitness()
    //   jika fitness(a) < fitness(b) → hasilnya negatif → a didahulukan
    private final PriorityQueue<Node> openPQ = new PriorityQueue<>(
            Comparator.comparingInt(Node::getFitness)
    );

    private final Set<String> visitedStates = new HashSet<>();

    private int nomor = 1;

    public EightPuzzlePQ(String initialState, String goalState) {
        this.initialState = initialState;
        this.goalState    = goalState;
    }

    // =========================================================================
    // HEURISTIK - Manhattan Distance
    //
    // Menghitung total jarak Manhattan semua angka dari posisi saat ini
    // ke posisi tujuannya. Angka '0' (ruang kosong) diabaikan.
    //
    // Cara menghitung baris dan kolom dari indeks string (0-8):
    //   baris  = indeks / 3   (pembagian bulat)
    //   kolom  = indeks % 3   (sisa bagi)
    //
    //   Contoh indeks 7: baris = 7/3 = 2, kolom = 7%3 = 1 → posisi (baris 2, kolom 1)
    // =========================================================================
    private int manhattanDistance(String state) {
        int total = 0;
        for (int i = 0; i < 9; i++) {
            char angka = state.charAt(i);
            if (angka == '0') continue; // ruang kosong diabaikan

            // Cari posisi angka ini di goalState
            int tujuan = goalState.indexOf(angka);

            // Hitung jarak Manhattan antara posisi sekarang (i) dan posisi tujuan
            int barisSekarang = i       / 3;
            int kolomSekarang = i       % 3;
            int barisTujuan   = tujuan  / 3;
            int kolomTujuan   = tujuan  % 3;

            total += Math.abs(barisSekarang - barisTujuan)
                   + Math.abs(kolomSekarang - kolomTujuan);
        }
        return total;
    }

    // =========================================================================
    // OPERATOR PERGERAKAN
    // Sama persis dengan BFS dan DFS, perbedaannya:
    // 1. Node baru dibuat dengan nilai fitness dari manhattanDistance()
    // 2. Node dimasukkan ke PriorityQueue (bukan Queue atau Stack)
    // =========================================================================

    // -------------------------------------------------------------------------
    // GESER KE ATAS
    // SYARAT: '0' tidak boleh di baris paling atas (emptyPos > 2)
    // Angka di ATAS '0' = indeks emptyPos - 3
    // -------------------------------------------------------------------------
    private void up(Node node) {
        String state    = node.getState();
        int emptyPos    = state.indexOf("0");

        if (emptyPos > 2) {
            String newState = state.substring(0, emptyPos - 3) + "0"
                    + state.substring(emptyPos - 2, emptyPos)
                    + state.charAt(emptyPos - 3)
                    + state.substring(emptyPos + 1);

            int newLevel   = node.getLevel() + 1;
            int newFitness = manhattanDistance(newState);
            addNodeToPQ(new Node(newState, "up", newLevel, 0, newFitness, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE BAWAH
    // SYARAT: '0' tidak boleh di baris paling bawah (emptyPos < 6)
    // Angka di BAWAH '0' = indeks emptyPos + 3
    // -------------------------------------------------------------------------
    private void down(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        if (emptyPos < 6) {
            String newState = state.substring(0, emptyPos)
                    + state.substring(emptyPos + 3, emptyPos + 4)
                    + state.substring(emptyPos + 1, emptyPos + 3)
                    + "0"
                    + state.substring(emptyPos + 4);

            int newLevel   = node.getLevel() + 1;
            int newFitness = manhattanDistance(newState);
            addNodeToPQ(new Node(newState, "down", newLevel, 0, newFitness, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KIRI
    // SYARAT: '0' tidak boleh di kolom paling kiri (bukan indeks 0, 3, 6)
    // Angka di KIRI '0' = indeks emptyPos - 1
    // -------------------------------------------------------------------------
    private void left(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        if (emptyPos != 0 && emptyPos != 3 && emptyPos != 6) {
            String newState = state.substring(0, emptyPos - 1)
                    + "0"
                    + state.charAt(emptyPos - 1)
                    + state.substring(emptyPos + 1);

            int newLevel   = node.getLevel() + 1;
            int newFitness = manhattanDistance(newState);
            addNodeToPQ(new Node(newState, "left", newLevel, 0, newFitness, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KANAN
    // SYARAT: '0' tidak boleh di kolom paling kanan (bukan indeks 2, 5, 8)
    // Angka di KANAN '0' = indeks emptyPos + 1
    // -------------------------------------------------------------------------
    private void right(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        if (emptyPos != 2 && emptyPos != 5 && emptyPos != 8) {
            String newState = state.substring(0, emptyPos)
                    + state.charAt(emptyPos + 1)
                    + "0"
                    + state.substring(emptyPos + 2);

            int newLevel   = node.getLevel() + 1;
            int newFitness = manhattanDistance(newState);
            addNodeToPQ(new Node(newState, "right", newLevel, 0, newFitness, node));
        }
    }

    // -------------------------------------------------------------------------
    // SOLVE - Method utama Best-First Search
    //
    // Alurnya sama dengan BFS/DFS, perbedaannya ada di struktur data:
    // PriorityQueue otomatis mengurutkan node berdasarkan fitness terkecil.
    // Setiap kali poll() dipanggil, node paling "menjanjikan" yang keluar.
    // -------------------------------------------------------------------------
    public void solve() {
        int fitnesAwal = manhattanDistance(initialState);
        addNodeToPQ(new Node(initialState, "", 0, 0, fitnesAwal, null));

        while (!openPQ.isEmpty()) {

            // Ambil node dengan FITNESS TERKECIL dari priority queue
            Node currentNode = openPQ.poll();

            String parentInfo = currentNode.getParent() != null
                    ? "parent: {state: '" + currentNode.getParent().getState() + "'"
                            + ", level: "   + currentNode.getParent().getLevel()
                            + ", no_urut: " + currentNode.getParent().getNomor() + "}"
                    : "parent: null (node awal)";
            System.err.println("Sedang mengecek: " + currentNode + " | " + parentInfo);

            if (currentNode.getState().equals(goalState)) {
                System.out.println("\n=== SOLUSI BERHASIL DITEMUKAN! ===");
                System.out.println("Total langkah: " + currentNode.getLevel()
                        + " langkah | Node ke-" + currentNode.getNomor() + " yang diperiksa");
                System.out.println("\nUrutan langkah dari awal:");
                printPath(currentNode);
                return;
            }

            left(currentNode);
            up(currentNode);
            right(currentNode);
            down(currentNode);
        }

        System.out.println("Solusi tidak ditemukan.");
    }

    // -------------------------------------------------------------------------
    // ADD NODE TO PQ - Masukkan node ke priority queue jika belum pernah dikunjungi
    //
    // Sama dengan BFS/DFS: state ditandai visited saat push untuk menghindari
    // duplikat di dalam priority queue.
    // -------------------------------------------------------------------------
    private void addNodeToPQ(Node node) {
        if (!visitedStates.contains(node.getState())) {
            visitedStates.add(node.getState());
            node.setNomor(nomor++);
            openPQ.add(node); // PQ otomatis urutkan berdasarkan fitness
        }
    }

    // -------------------------------------------------------------------------
    // PRINT PATH - Cetak jalur solusi dari awal ke tujuan via rekursi
    // -------------------------------------------------------------------------
    private void printPath(Node node) {
        if (node == null) return;
        printPath(node.getParent());

        String langkah = node.getOperator().isEmpty() ? "KONDISI AWAL" : "Geser ke " + node.getOperator();
        System.out.println("Langkah " + node.getLevel() + ": " + langkah
                + " -> " + formatPuzzle(node.getState())
                + "  [fitness=" + node.getFitness() + "]");
    }

    private String formatPuzzle(String state) {
        return state.substring(0, 3) + "-" + state.substring(3, 6) + "-" + state.substring(6, 9);
    }

    public static void main(String[] args) {
        String asal   = "283164705";
        String tujuan = "123804765";

        System.out.println("Mencari solusi Eight Puzzle dengan Best-First Search (Priority Queue)...");
        System.out.println("Kondisi awal  : " + asal.substring(0, 3)   + "-" + asal.substring(3, 6)   + "-" + asal.substring(6));
        System.out.println("Kondisi tujuan: " + tujuan.substring(0, 3) + "-" + tujuan.substring(3, 6) + "-" + tujuan.substring(6));
        System.out.println();

        EightPuzzlePQ puzzle = new EightPuzzlePQ(asal, tujuan);
        puzzle.solve();
    }
}
