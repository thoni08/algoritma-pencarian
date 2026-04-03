import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

// =============================================================================
// Eight Puzzle Solver - Algoritma BFS (Breadth-First Search)
//
// APA ITU BFS?
// BFS adalah algoritma pencarian yang menjelajahi semua kemungkinan gerakan
// satu langkah dulu, baru kemudian dua langkah, tiga langkah, dan seterusnya.
//
// Analogi: bayangkan kamu menyebarkan brosur dari titik pusat kota.
// Kamu habiskan dulu semua jalan yang berjarak 1 blok, lalu 2 blok, dst.
// BFS bekerja persis seperti itu — menjamin jalur yang ditemukan adalah
// TERPENDEK.
//
// REPRESENTASI PUZZLE
// Papan 3x3 disimpan sebagai String 9 karakter, dibaca baris per baris:
//
//   Papan nyata:    String di program:
//   1 | 2 | 3
//   ---------   ->  "123456780"
//   4 | 5 | 6
//   ---------
//   7 | 8 | 0   <- angka '0' = ruang kosong
//
// Setiap karakter punya nomor urut posisi (indeks) dari 0 sampai 8:
//
//   indeks:  0 | 1 | 2   -> baris pertama  (atas)
//            ---------
//            3 | 4 | 5   -> baris kedua    (tengah)
//            ---------
//            6 | 7 | 8   -> baris ketiga   (bawah)
//
// STRUKTUR DATA YANG DIGUNAKAN
// - Queue (openQueue)    : antrean node yang menunggu untuk diperiksa.
//                          Node yang masuk pertama akan diambil pertama
//                          (seperti antrean kasir supermarket).
//                          Sifat inilah yang membuat BFS menjelajah
//                          satu langkah dulu sebelum dua langkah.
//
// - Set (visitedStates)  : daftar susunan papan yang sudah pernah dikunjungi,
//                          agar tidak memproses papan yang sama dua kali.
// =============================================================================
public class EightPuzzleBFS {

    private final String initialState; // Kondisi awal puzzle
    private final String goalState;    // Kondisi tujuan yang ingin dicapai

    // Queue = antrean. Node yang dimasukkan pertama akan diproses pertama.
    private final Queue<Node> openQueue = new LinkedList<>();

    // Set = kumpulan data unik. Digunakan untuk menyimpan susunan papan
    // yang sudah pernah dikunjungi agar tidak diperiksa ulang.
    private final Set<String> visitedStates = new HashSet<>();

    // Penghitung untuk memberi nomor urut pada setiap node yang dibuat
    private int nomor = 1;

    public EightPuzzleBFS(String initialState, String goalState) {
        this.initialState = initialState;
        this.goalState = goalState;
    }

    // =========================================================================
    // OPERATOR PERGERAKAN
    // Ada 4 gerakan yang mungkin: atas, bawah, kiri, kanan.
    // Setiap method mencoba menggeser ruang kosong ('0') ke satu arah.
    // Jika gerakan tidak melanggar batas papan, state baru dibuat dan
    // dimasukkan ke antrean untuk diperiksa nanti.
    // =========================================================================

    // -------------------------------------------------------------------------
    // GESER KE ATAS
    // Angka yang ada DI ATAS ruang kosong turun mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 0 | 3
    //             ---------   ->            ---------
    //             4 | 0 | 5                 4 | 2 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 7 | 8
    //
    // SYARAT: '0' tidak boleh ada di baris paling atas (tidak ada baris di atasnya).
    //
    //   indeks:  [ 0  1  2 ]  <- baris atas   -> TIDAK BISA naik
    //            [ 3  4  5 ]  <- baris tengah  -> bisa naik
    //            [ 6  7  8 ]  <- baris bawah   -> bisa naik
    //
    //   Baris atas = indeks 0, 1, 2.
    //   Jadi syaratnya: emptyPos > 2
    //   (indeks 3 ke atas artinya '0' ada di baris tengah atau bawah)
    //
    // CARA MENUKAR DI DALAM STRING
    // Di dalam program, puzzle bukan papan 2D melainkan satu baris String.
    // Menukar dua karakter di String tidak bisa langsung (String tidak bisa diubah
    // huruf per huruf di Java), jadi kita harus menyusun ulang potongan-potongannya.
    //
    // Contoh konkret: state = "142053678", emptyPos = 4 (posisi '0')
    //
    //   Papan:   1 | 4 | 2       String: "1 4 2 0 5 3 6 7 8"
    //            ---------               indeks: 0 1 2 3 4 5 6 7 8
    //            5 | 0 | 3
    //            ---------
    //            6 | 7 | 8
    //
    //   Angka yang harus turun = angka DI ATAS '0' = indeks emptyPos - 3 = 1 = '4'
    //   (kenapa -3? karena setiap baris memiliki 3 kolom, naik satu baris = mundur 3 indeks)
    //
    //   Cara menyusun String baru:
    //   state = "1  4  2  5  0  3  6  7  8"
    //            [0][1][2][3][4][5][6][7][8]
    //
    //   ① "1"        = state.substring(0, emptyPos-3)  -> ambil semua sebelum angka atas ('4')
    //   ② "0"        = "0"                             -> taruh '0' di posisi angka atas tadi
    //   ③ "25"       = state.substring(emptyPos-2, emptyPos) -> ambil karakter di antaranya
    //   ④ "4"        = state.charAt(emptyPos-3)        -> angka atas ('4') pindah ke posisi '0' lama
    //   ⑤ "3678"     = state.substring(emptyPos+1)     -> ambil sisa setelah '0' lama
    //
    //   Hasilnya: "1" + "0" + "25" + "4" + "3678" = "102543678"
    //
    //   Papan baru:  1 | 0 | 2
    //                ---------
    //                5 | 4 | 3
    //                ---------
    //                6 | 7 | 8
    // -------------------------------------------------------------------------
    private void up(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0"); // Cari posisi indeks dari ruang kosong

        // Boleh geser ke atas hanya jika '0' bukan di baris paling atas (indeks > 2)
        if (emptyPos > 2) {
            String newState = state.substring(0, emptyPos - 3) + "0"
                    + state.substring(emptyPos - 2, emptyPos)
                    + state.charAt(emptyPos - 3)
                    + state.substring(emptyPos + 1);

            int newLevel = node.getLevel() + 1;
            addNodeToQueue(new Node(newState, "up", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE BAWAH
    // Angka yang ada DI BAWAH ruang kosong naik mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 2 | 3
    //             ---------   ->            ---------
    //             4 | 0 | 5                 4 | 7 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 0 | 8
    //
    // SYARAT: '0' tidak boleh ada di baris paling bawah (tidak ada baris di bawahnya).
    //
    //   indeks:  [ 0  1  2 ]  <- baris atas   -> bisa turun
    //            [ 3  4  5 ]  <- baris tengah  -> bisa turun
    //            [ 6  7  8 ]  <- baris bawah   -> TIDAK BISA turun
    //
    //   Baris bawah = indeks 6, 7, 8.
    //   Jadi syaratnya: emptyPos < 6
    //
    // CARA MENUKAR DI DALAM STRING
    // Angka yang harus naik = angka DI BAWAH '0' = indeks emptyPos + 3
    // (kenapa +3? karena turun satu baris = maju 3 indeks)
    //
    //   ① state.substring(0, emptyPos)         -> semua karakter sebelum '0'
    //   ② state.charAt(emptyPos+3)             -> angka bawah naik ke posisi '0'
    //   ③ state.substring(emptyPos+1, emptyPos+3) -> karakter di antara keduanya
    //   ④ "0"                                  -> '0' turun ke posisi angka bawah tadi
    //   ⑤ state.substring(emptyPos+4)          -> sisa karakter setelahnya
    // -------------------------------------------------------------------------
    private void down(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        // Boleh geser ke bawah hanya jika '0' bukan di baris paling bawah (indeks < 6)
        if (emptyPos < 6) {
            String newState = state.substring(0, emptyPos)
                    + state.substring(emptyPos + 3, emptyPos + 4)
                    + state.substring(emptyPos + 1, emptyPos + 3)
                    + "0"
                    + state.substring(emptyPos + 4);

            int newLevel = node.getLevel() + 1;
            addNodeToQueue(new Node(newState, "down", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KIRI
    // Angka yang ada DI KIRI ruang kosong berpindah ke kanan mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 2 | 3
    //             ---------   ->            ---------
    //             4 | 5 | 0                 4 | 0 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 7 | 8
    //
    // SYARAT: '0' tidak boleh ada di kolom paling kiri.
    //
    //   indeks:  [(0) 1   2 ]  <- indeks 0 = kolom kiri -> DILARANG
    //            [(3) 4   5 ]  <- indeks 3 = kolom kiri -> DILARANG
    //            [(6) 7   8 ]  <- indeks 6 = kolom kiri -> DILARANG
    //
    //   Kenapa perlu dicek? Karena String bersambung terus tanpa jeda baris.
    //   Kalau '0' ada di indeks 3 (awal baris ke-2) dan kita geser kiri,
    //   program akan mengambil indeks 3-1=2 yang merupakan akhir baris ke-1.
    //   Di papan nyata mereka berbeda baris, tapi di String indeksnya berdekatan!
    //
    // CARA MENUKAR DI DALAM STRING
    // Angka di kiri = indeks emptyPos - 1
    //
    //   ① state.substring(0, emptyPos-1) -> semua sebelum angka kiri
    //   ② "0"                            -> '0' berpindah ke posisi angka kiri
    //   ③ state.charAt(emptyPos-1)       -> angka kiri berpindah ke posisi '0' lama
    //   ④ state.substring(emptyPos+1)    -> sisa karakter setelah '0' lama
    // -------------------------------------------------------------------------
    private void left(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        // Boleh geser ke kiri hanya jika '0' bukan di kolom paling kiri
        if (emptyPos != 0 && emptyPos != 3 && emptyPos != 6) {
            String newState = state.substring(0, emptyPos - 1)
                    + "0"
                    + state.charAt(emptyPos - 1)
                    + state.substring(emptyPos + 1);

            int newLevel = node.getLevel() + 1;
            addNodeToQueue(new Node(newState, "left", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // GESER KE KANAN
    // Angka yang ada DI KANAN ruang kosong berpindah ke kiri mengisi ruang kosong.
    //
    // Contoh:
    //   Sebelum:  1 | 2 | 3       Sesudah:  1 | 2 | 3
    //             ---------   ->            ---------
    //             0 | 4 | 5                 4 | 0 | 5
    //             ---------                 ---------
    //             6 | 7 | 8                 6 | 7 | 8
    //
    // SYARAT: '0' tidak boleh ada di kolom paling kanan (tidak ada angka di kanannya).
    //
    //   indeks:  [ 0   1  (2)]  <- indeks 2 = kolom kanan -> DILARANG
    //            [ 3   4  (5)]  <- indeks 5 = kolom kanan -> DILARANG
    //            [ 6   7  (8)]  <- indeks 8 = kolom kanan -> DILARANG
    //
    // CARA MENUKAR DI DALAM STRING
    // Angka di kanan = indeks emptyPos + 1
    //
    //   ① state.substring(0, emptyPos)  -> semua sebelum '0'
    //   ② state.charAt(emptyPos+1)      -> angka kanan berpindah ke posisi '0'
    //   ③ "0"                           -> '0' berpindah ke posisi angka kanan tadi
    //   ④ state.substring(emptyPos+2)   -> sisa karakter setelah angka kanan
    // -------------------------------------------------------------------------
    private void right(Node node) {
        String state = node.getState();
        int emptyPos = state.indexOf("0");

        // Boleh geser ke kanan hanya jika '0' bukan di kolom paling kanan
        if (emptyPos != 2 && emptyPos != 5 && emptyPos != 8) {
            String newState = state.substring(0, emptyPos)
                    + state.charAt(emptyPos + 1)
                    + "0"
                    + state.substring(emptyPos + 2);

            int newLevel = node.getLevel() + 1;
            addNodeToQueue(new Node(newState, "right", newLevel, 0, node));
        }
    }

    // -------------------------------------------------------------------------
    // SOLVE - Method utama yang menjalankan algoritma BFS
    //
    // LANGKAH-LANGKAH ALGORITMA BFS:
    //
    //   1. Masukkan kondisi awal puzzle ke dalam antrean.
    //   2. Ambil kondisi paling depan dari antrean, lalu cek:
    //      a. Apakah ini kondisi tujuan? -> Selesai! Cetak urutan langkahnya.
    //      b. Bukan tujuan? -> Coba semua 4 arah gerakan.
    //         Setiap gerakan valid menghasilkan kondisi baru -> masuk antrean.
    //   3. Ulangi langkah 2 sampai tujuan ditemukan atau antrean habis.
    //
    // KENAPA BFS SELALU MENEMUKAN JALUR TERPENDEK?
    // Karena BFS memproses semua kondisi 1 langkah terlebih dahulu, baru
    // kondisi 2 langkah, dst. Saat kondisi tujuan ditemukan pertama kali,
    // itu PASTI berasal dari jalur dengan jumlah langkah paling sedikit.
    // -------------------------------------------------------------------------
    public void solve() {
        // Langkah 1: Masukkan kondisi awal. Level=0, belum ada gerakan, tidak ada parent.
        addNodeToQueue(new Node(initialState, "", 0, 0, null));

        // Langkah 2: Terus proses selama antrean masih ada isinya
        while (!openQueue.isEmpty()) {

            // Ambil node paling depan antrean (yang paling lama menunggu)
            Node currentNode = openQueue.poll();

            // Cetak ke stderr (bukan stdout) agar log progres tidak tercampur
            // dengan output solusi yang dicetak ke stdout di bawah
            System.err.println("Sedang mengecek: " + currentNode);

            // Cek apakah kondisi ini adalah kondisi tujuan
            if (currentNode.getState().equals(goalState)) {
                System.out.println("\n=== SOLUSI BERHASIL DITEMUKAN! ===");
                System.out.println("Total langkah: " + currentNode.getLevel()
                        + " langkah | Node ke-" + currentNode.getNomor() + " yang diperiksa");
                System.out.println("\nUrutan langkah dari awal:");
                printPath(currentNode);
                break;
            } else {
                // Bukan tujuan -> coba semua 4 arah gerakan dari kondisi ini.
                // Gerakan yang valid otomatis masuk ke antrean di dalam method masing-masing.
                // Urutan di bawah mempengaruhi urutan pemrosesan, tapi BUKAN hasil akhirnya.
                left(currentNode);
                up(currentNode);
                right(currentNode);
                down(currentNode);
            }
        }
    }

    // -------------------------------------------------------------------------
    // ADD NODE TO QUEUE - Memasukkan node ke antrean jika belum pernah dikunjungi
    //
    // Kenapa perlu dicek dulu?
    // Tanpa pengecekan ini, program bisa terjebak mengulang kondisi yang sama
    // selamanya. Misalnya: geser kanan -> geser kiri -> geser kanan -> ...
    //
    // Kenapa menggunakan Set, bukan List biasa?
    // Set menyimpan data tanpa duplikat dan pengecekan "sudah ada atau belum"
    // berlangsung sangat cepat — tidak peduli ada 10 atau 100.000 kondisi
    // tersimpan, kecepatannya tetap sama.
    // Kalau pakai List, semakin banyak data tersimpan, pengecekan semakin lama
    // karena harus menelusuri satu per satu dari awal.
    // -------------------------------------------------------------------------
    private void addNodeToQueue(Node node) {
        if (!visitedStates.contains(node.getState())) {
            visitedStates.add(node.getState()); // Tandai kondisi ini sudah pernah dikunjungi
            node.setNomor(nomor++);             // Beri nomor urut untuk memudahkan tracking
            openQueue.add(node);                // Masukkan ke antrean untuk diproses nanti
        }
    }

    // -------------------------------------------------------------------------
    // PRINT PATH - Mencetak urutan langkah dari kondisi awal hingga tujuan
    //
    // Cara kerjanya:
    // Setiap node menyimpan node "sebelumnya" (parent). Misalnya:
    //   node tujuan -> node langkah ke-5 -> ... -> node langkah ke-1 -> node awal -> null
    //
    // Kita ingin mencetak dari awal ke akhir, tapi yang kita pegang adalah node
    // TUJUAN. Solusinya: gunakan rekursi untuk mundur ke node awal dulu,
    // baru cetak saat kembali ke depan.
    //
    // Analogi: seperti tumpukan piring. Kita ambil satu per satu dari atas
    // (mundur ke awal), taruh di tempat, lalu cetak dari piring paling bawah
    // (kondisi awal) ke piring paling atas (kondisi tujuan).
    //
    // Cara rekursinya:
    //   printPath(tujuan)
    //     -> printPath(langkah ke-5)
    //          -> printPath(langkah ke-4)
    //               -> ... -> printPath(awal)
    //                              -> printPath(null) <- berhenti di sini
    //                         cetak [awal]
    //               cetak [langkah ke-4]
    //          cetak [langkah ke-5]
    //     cetak [tujuan]
    // -------------------------------------------------------------------------
    private void printPath(Node node) {
        if (node == null) {
            return; // Sudah melewati node paling awal, berhenti dan mulai cetak saat kembali
        }
        printPath(node.getParent()); // Mundur ke node sebelumnya dulu

        // Setelah rekursi kembali ke sini, baru cetak node ini
        String langkah = node.getOperator().isEmpty() ? "KONDISI AWAL" : "Geser ke " + node.getOperator();
        System.out.println("Langkah " + node.getLevel() + ": " + langkah
                + " -> " + formatPuzzle(node.getState()));
    }

    // Mengubah string puzzle menjadi tampilan yang lebih mudah dibaca.
    // Contoh: "123456780" -> "123-456-780"  (tanda '-' memisahkan tiap baris)
    private String formatPuzzle(String state) {
        return state.substring(0, 3) + "-" + state.substring(3, 6) + "-" + state.substring(6, 9);
    }

    public static void main(String[] args) {
        // Ganti nilai di bawah untuk mencoba kondisi awal dan tujuan yang berbeda.
        // Gunakan angka 0-8 masing-masing tepat satu kali. '0' = ruang kosong.
        String asal   = "283164705"; // Kondisi awal puzzle (acak)
        String tujuan = "123804765"; // Kondisi tujuan yang ingin dicapai

        System.out.println("Mencari solusi Eight Puzzle dengan BFS...");
        System.out.println("Kondisi awal  : " + asal.substring(0, 3) + "-" + asal.substring(3, 6) + "-" + asal.substring(6));
        System.out.println("Kondisi tujuan: " + tujuan.substring(0, 3) + "-" + tujuan.substring(3, 6) + "-" + tujuan.substring(6));
        System.out.println();

        EightPuzzleBFS puzzle = new EightPuzzleBFS(asal, tujuan);
        puzzle.solve();
    }
}
