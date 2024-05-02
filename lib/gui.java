import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

class Student implements Serializable {
    int id;
    String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    public String value() {
        return this.name;
    }

}

class BTree implements Serializable {

    private int T;

    public class Node implements Serializable {
        int n;
        Student students[] = new Student[2 * T - 1];
        int key[] = new int[2 * T - 1];
        Node child[] = new Node[2 * T];
        boolean leaf = true;

        public Student find(int k) {
            for (int i = 0; i < this.n; i++) {
                if (this.key[i] == k) {
                    return students[i];
                }
            }
            return null;
        }
    }

    private Node root;

    public BTree(int t) {
        T = t;
        root = new Node();
        root.n = 0;
        root.leaf = true;
    }

    private Student search(Node x, int key) {
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
            return search(x.child[i], key);
        }
    }

    public Student read(int key) {
        return search(root, key);
    }

    private void split(Node x, int pos, Node y) {
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
        x.students[pos] = y.students[T - 1];
        x.n = x.n + 1;
    }

    public void insert(int key, Student student) {
        Node r = root;
        if (r.n == 2 * T - 1) {
            Node s = new Node();
            root = s;
            s.leaf = false;
            s.n = 0;
            s.child[0] = r;
            split(s, 0, r);
            insertNonFull(s, key, student);
        } else {
            insertNonFull(r, key, student);
        }
    }

    private void insertNonFull(Node x, int k, Student student) {
        int i = x.n - 1;
        if (x.leaf) {
            while (i >= 0 && k < x.key[i]) {
                x.key[i + 1] = x.key[i];
                x.students[i + 1] = x.students[i];
                i--;
            }
            x.key[i + 1] = k;
            x.students[i + 1] = student;
            x.n = x.n + 1;
        } else {
            while (i >= 0 && k < x.key[i]) {
                i--;
            }
            i++;
            if (x.child[i].n == 2 * T - 1) {
                split(x, i, x.child[i]);
                if (k > x.key[i]) {
                    i++;
                }
            }
            insertNonFull(x.child[i], k, student);
        }
    }

    public void delete(int key) {
        delete(root, key);
    }

    private void delete(Node x, int key) {
        if (x == null) {
            return;
        }

        int idx = 0;
        while (idx < x.n && key > x.key[idx]) {
            idx++;
        }

        if (idx < x.n && key == x.key[idx]) {
            if (x.leaf) {
                removeFromLeaf(x, idx);
            } else {
                deleteInternalNode(x, idx);
            }
        } else {
            if (x.leaf) {
                System.out.println("Key " + key + " does not exist in the BTree.");
                return;
            }

            boolean flag = (idx == x.n);

            if (x.child[idx].n < T) {
                fill(x, idx);
            }

            if (flag && idx > x.n) {
                delete(x.child[idx - 1], key);
            } else {
                delete(x.child[idx], key);
            }
        }
    }

    private void removeFromLeaf(Node x, int idx) {
        for (int i = idx + 1; i < x.n; i++) {
            x.key[i - 1] = x.key[i];
            x.students[i - 1] = x.students[i];
        }
        x.n--;
    }

    private void deleteInternalNode(Node x, int idx) {
        int k = x.key[idx];
        if (x.child[idx].n >= T) {
            int pred = getPredecessor(x, idx);
            x.key[idx] = pred;
            delete(x.child[idx], pred);
        } else if (x.child[idx + 1].n >= T) {
            int succ = getSuccessor(x, idx);
            x.key[idx] = succ;
            delete(x.child[idx + 1], succ);
        } else {
            merge(x, idx);
            delete(x.child[idx], k);
        }
    }

    private int getPredecessor(Node x, int idx) {
        Node cur = x.child[idx];
        while (!cur.leaf) {
            cur = cur.child[cur.n];
        }
        return cur.key[cur.n - 1];
    }

    private int getSuccessor(Node x, int idx) {
        Node cur = x.child[idx + 1];
        while (!cur.leaf) {
            cur = cur.child[0];
        }
        return cur.key[0];
    }

    private void fill(Node x, int idx) {
        if (idx != 0 && x.child[idx - 1].n >= T) {
            borrowFromPrev(x, idx);
        } else if (idx != x.n && x.child[idx + 1].n >= T) {
            borrowFromNext(x, idx);
        } else {
            if (idx != x.n) {
                merge(x, idx);
            } else {
                merge(x, idx - 1);
            }
        }
    }

    private void borrowFromPrev(Node x, int idx) {
        Node child = x.child[idx];
        Node sibling = x.child[idx - 1];

        for (int i = child.n - 1; i >= 0; i--) {
            child.key[i + 1] = child.key[i];
            child.students[i + 1] = child.students[i];
        }

        if (!child.leaf) {
            for (int i = child.n; i >= 0; i--) {
                child.child[i + 1] = child.child[i];
            }
        }

        child.key[0] = x.key[idx - 1];
        child.students[0] = x.students[idx - 1];

        if (!child.leaf) {
            child.child[0] = sibling.child[sibling.n];
        }

        x.key[idx - 1] = sibling.key[sibling.n - 1];
        x.students[idx - 1] = sibling.students[sibling.n - 1];

        child.n += 1;
        sibling.n -= 1;
    }

    private void borrowFromNext(Node x, int idx) {
        Node child = x.child[idx];
        Node sibling = x.child[idx + 1];

        child.key[child.n] = x.key[idx];
        child.students[child.n] = x.students[idx];

        if (!child.leaf) {
            child.child[child.n + 1] = sibling.child[0];
        }

        x.key[idx] = sibling.key[0];
        x.students[idx] = sibling.students[0];

        for (int i = 1; i < sibling.n; i++) {
            sibling.key[i - 1] = sibling.key[i];
            sibling.students[i - 1] = sibling.students[i];
        }

        if (!sibling.leaf) {
            for (int i = 1; i <= sibling.n; i++) {
                sibling.child[i - 1] = sibling.child[i];
            }
        }

        child.n += 1;
        sibling.n -= 1;
    }

    private void merge(Node x, int idx) {
        Node child = x.child[idx];
        Node sibling = x.child[idx + 1];

        child.key[T - 1] = x.key[idx];
        child.students[T - 1] = x.students[idx];

        for (int i = 0; i < sibling.n; i++) {
            child.key[i + T] = sibling.key[i];
            child.students[i + T] = sibling.students[i];
        }

        if (!child.leaf) {
            for (int i = 0; i <= sibling.n; i++) {
                child.child[i + T] = sibling.child[i];
            }
        }

        for (int i = idx + 1; i < x.n; i++) {
            x.key[i - 1] = x.key[i];
            x.students[i - 1] = x.students[i];
        }

        for (int i = idx + 2; i <= x.n; i++) {
            x.child[i - 1] = x.child[i];
        }

        child.n += sibling.n + 1;
        x.n--;

        sibling = null;
    }

    public void show() {
        show(root);
    }

    // Display
    private void show(Node x) {
        assert (x == null);
        for (int i = 0; i < x.n; i++) {
            System.out.print(x.key[i] + " ");
        }
        if (!x.leaf) {
            for (int i = 0; i < x.n + 1; i++) {
                show(x.child[i]);
            }
        }
    }

    public void saveToFile(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(this);
            System.out.println("BTree saved successfully to " + filename);
        } catch (IOException e) {
            System.err.println("Error saving BTree to" + filename + " file: " + e.getMessage());
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

public class gui extends JFrame implements ActionListener {

    private BTree bTree;
    private JTextField keyField, idField, nameField;
    private JTextArea outputArea;

    public gui() {
        super("Student Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(800, 600));

        // Initialize BTree
        bTree = new BTree(3);

        // Title Panel
        JPanel titlePanel = new JPanel();
        JLabel titleLabel = new JLabel("Student Management System");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);

        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder("Enter Student Details"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(10, 10, 10, 10);

        inputPanel.add(new JLabel("Key:"), gbc);
        gbc.gridx++;
        keyField = new JTextField(10);
        inputPanel.add(keyField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        inputPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx++;
        idField = new JTextField(10);
        inputPanel.add(idField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        inputPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx++;
        nameField = new JTextField(20);
        inputPanel.add(nameField, gbc);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton insertButton = new JButton("Insert");
        insertButton.addActionListener(this);
        buttonPanel.add(insertButton);

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(this);
        buttonPanel.add(deleteButton);

        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(this);
        buttonPanel.add(searchButton);

        JButton showButton = new JButton("Show");
        showButton.addActionListener(this);
        buttonPanel.add(showButton);

        // Output Area
        outputArea = new JTextArea(20, 60);
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Output"));

        // Add Components to Frame
        add(titlePanel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String keyStr = keyField.getText();
        String idStr = idField.getText();
        String name = nameField.getText();
        int key, id;

        try {
            key = Integer.parseInt(keyStr);
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid input. Please enter integers for Key and ID.");
            return;
        }

        try {
            if (e.getActionCommand().equals("Insert")) {
                bTree.insert(key, new Student(id, name));
                writeToOutput("Inserted: Key = " + key + ", ID = " + id + ", Name = " + name);
            } else if (e.getActionCommand().equals("Delete")) {
                bTree.delete(key);
                writeToOutput("Deleted: Key = " + key);
            } else if (e.getActionCommand().equals("Search")) {
                Student student = bTree.read(key);
                writeToOutput("Searched: Key = " + key + ", Result: "
                        + (student != null ? student.id + " " + student.name : "NULL"));
            } else if (e.getActionCommand().equals("Show")) {

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintStream ps = new PrintStream(baos);
                PrintStream old = System.out;
                System.setOut(ps);
                bTree.show();
                System.out.flush();
                System.setOut(old);
                String outputText = baos.toString();
                writeToOutput(outputText);
            }
        } catch (Exception ex) {
            writeToOutput("An error occurred: " + ex.getMessage());
        }
    }

    private void writeToOutput(String text) {
        outputArea.append(text + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(gui::new);
    }
}
