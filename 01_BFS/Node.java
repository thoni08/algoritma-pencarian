
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
     * Kedalaman BFS (depth) = jumlah langkah dari state awal sampai state ini.
     */
    private int level;

    /**
     * Nomor urut node saat dimasukkan ke antrean (untuk keperluan
     * tracing/output).
     */
    private int nomor;

    /**
     * Pointer ke node sebelumnya (parent) untuk rekonstruksi jalur solusi.
     */
    private Node parent;

    public Node(String state, String operator, int level, int nomor, Node parent) {
        this.state = state;
        this.operator = operator;
        this.level = level;
        this.nomor = nomor;
        this.parent = parent;
    }

    public String getState() {
        return state;
    }

    public String getOperator() {
        return operator;
    }

    public int getLevel() {
        return level;
    }

    public int getNomor() {
        return nomor;
    }

    public void setNomor(int nomor) {
        this.nomor = nomor;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    @Override
    public String toString() {
        // Parent tidak dicetak agar output ringkas dan tidak nested.
        return "Node {"
                + "state: '" + state + '\''
                + ", langkah: '" + (operator.isEmpty() ? "START" : operator) + '\''
                + ", level: " + level
                + ", no_urut: " + nomor
                + '}';
    }
}
