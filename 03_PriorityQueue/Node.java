
public class Node {

    /**
     * Konfigurasi papan (state) dalam bentuk String panjang 9, contoh:
     * "283164705". Karakter '0' merepresentasikan ruang kosong.
     */
    private String state;

    /**
     * Operator/aksi yang dipakai untuk mencapai state ini dari parent. Nilai:
     * "up", "down", "left", "right". Untuk node awal biasanya string kosong.
     */
    private String operator;

    /**
     * Kedalaman / jumlah langkah dari state awal sampai state ini.
     * Dikenal juga sebagai g(n) dalam notasi pencarian heuristik.
     */
    private int level;

    /**
     * Nomor urut node saat dimasukkan ke antrean (untuk keperluan
     * tracing/output).
     */
    private int nomor;

    /**
     * Nilai heuristik / fitness node ini.
     * Digunakan sebagai dasar prioritas dalam Priority Queue.
     *
     * Nilai ini bisa diisi dengan berbagai fungsi tergantung algoritma:
     *   - Greedy Best-First : fitness = h(n)  -> estimasi jarak ke tujuan
     *   - Uniform Cost (UCS): fitness = g(n)  -> biaya jalur dari awal (= level)
     *   - A*                : fitness = f(n)  -> g(n) + h(n)
     *
     * Node dengan fitness TERKECIL akan diproses lebih dahulu oleh PriorityQueue.
     */
    private int fitness;

    /**
     * Pointer ke node sebelumnya (parent) untuk rekonstruksi jalur solusi.
     */
    private Node parent;

    public Node(String state, String operator, int level, int nomor, int fitness, Node parent) {
        this.state    = state;
        this.operator = operator;
        this.level    = level;
        this.nomor    = nomor;
        this.fitness  = fitness;
        this.parent   = parent;
    }

    public String getState()    { return state; }
    public String getOperator() { return operator; }
    public int    getLevel()    { return level; }
    public int    getNomor()    { return nomor; }
    public int    getFitness()  { return fitness; }
    public Node   getParent()   { return parent; }

    public void setNomor(int nomor)   { this.nomor  = nomor; }
    public void setParent(Node parent){ this.parent  = parent; }
    public void setFitness(int fitness){ this.fitness = fitness; }

    @Override
    public String toString() {
        return "Node {"
                + "state: '"   + state + '\''
                + ", langkah: '" + (operator.isEmpty() ? "START" : operator) + '\''
                + ", level: "  + level
                + ", fitness: " + fitness
                + ", no_urut: " + nomor
                + '}';
    }
}
