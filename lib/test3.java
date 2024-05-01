import java.io.*;

class Student implements Serializable{
    int id;
    String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }
    public static void show(Student s){
    System.out.println("Student's name is "+s.name);
    }
}

class BTree implements Serializable{

    private int T;
    public class Node implements Serializable {
        int n;
        Student students[] = new Student[2 * T - 1];
        int key[] = new int[2 * T - 1];
        Node child[] = new Node[2 * T];
        boolean leaf = true;

        public Student Find(int k) {
            for (int i = 0; i < this.n; i++) {
                if (this.key[i] == k) {
                    return students[i];
                }
            }
            return null;
        };
    }

    public BTree(int t) {
        T = t;
        root = new Node();
        root.n = 0;
        root.leaf = true;
    }

    private Node root;

    // Search key
    private Student Search(Node x, int key) {
        int i = 0;
        if (x == null)
            return null;
        for (i = 0; i < x.n; i++) {
            if (key < x.key[i]) {
                break;
            }
            if (key == x.key[i]) {
                return x.students[i];
            }
        }
        if (x.leaf) {
            return null;
        } else {
            return Search(x.child[i], key);
        }
    }

    // Splitting the node
    private void Split(Node x, int pos, Node y) {
        Node z = new Node();
        z.leaf = y.leaf;
        z.n = T - 1;
        for (int j = 0; j < T - 1; j++) {
            z.key[j] = y.key[j + T];
            z.students[j] = y.students[j + T];
        }
        if (!y.leaf) {
            for (int j = 0; j < T; j++) {
                z.child[j] = y.child[j + T];
            }
        }
        y.n = T - 1;
        for (int j = x.n; j >= pos + 1; j--) {
            x.child[j + 1] = x.child[j];
        }
        x.child[pos + 1] = z;

        for (int j = x.n - 1; j >= pos; j--) {
            x.key[j + 1] = x.key[j];
            x.students[j + 1] = x.students[j];
        }
        x.key[pos] = y.key[T - 1];
        x.students[pos] = y.students[T-1];
        x.n = x.n + 1;
    }

    // Inserting a value
    public void Insert(final int key, Student student) {
        Node r = root;
        if (r.n == 2 * T - 1) {
            Node s = new Node();
            root = s;
            s.leaf = false;
            s.n = 0;
            s.child[0] = r;
            Split(s, 0, r);
            insertValue(s, key,student);
        } else {
            insertValue(r, key,student);
        }
    }

    // Insert the node
    final private void insertValue(Node x, int k,Student student) {

        if (x.leaf) {
            int i = 0;
            for (i = x.n - 1; i >= 0 && k < x.key[i]; i--) {
                x.key[i + 1] = x.key[i];
                x.students[i + 1] = x.students[i];
            }
            x.key[i + 1] = k;
            x.students[i + 1] = student;
            x.n = x.n + 1;
        } else {
            int i = 0;
            for (i = x.n - 1; i >= 0 && k < x.key[i]; i--) {
            }
            ;
            i++;
            Node tmp = x.child[i];
            if (tmp.n == 2 * T - 1) {
                Split(x, i, tmp);
                if (k > x.key[i]) {
                    i++;
                }
            }
            insertValue(x.child[i], k,student);
        }

    }

    public void Show() {
        Show(root);
    }

    // Display
    private void Show(Node x) {
        assert (x == null);
        for (int i = 0; i < x.n; i++) {
            System.out.print(x.key[i] + " ");
        }
        if (!x.leaf) {
            for (int i = 0; i < x.n + 1; i++) {
                Show(x.child[i]);
            }
        }
    }

    // Check if present
    public Student Search(int k) {
        return Search(root, k);
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
            System.out.println("BTree saved successfully to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving BTree to" + filename+" file: " + e.getMessage());
        }
    }
     public static BTree loadFromFile(String filename) {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            BTree tree = (BTree) in.readObject();
            System.out.println("BTree loaded successfully from " + filename);
            return tree;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading BTree from file: " + e.getMessage());
            return null;
        }
    }
}
public class test3{
    public static void main(String[] args) {
        BTree tree = new BTree(3);
        tree.Insert(10, new Student(1, "Alice"));
        tree.Insert(20, new Student(2, "Bob"));
        Student.show(tree.Search(10));
        tree.saveToFile("btree_data.ser");
        BTree loadedTree = BTree.loadFromFile("btree_data.ser");
        Student.show(loadedTree.Search(10));
    }
}

