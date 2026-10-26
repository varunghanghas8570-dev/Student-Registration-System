import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

public class Main extends JFrame {
    private final Color NAVY = new Color(22, 37, 66);
    private final Color BLUE = new Color(45, 102, 218);
    private final Color BG = new Color(245, 247, 251);
    private final List<Student> students = new ArrayList<>();
    private final Path dataFile = Paths.get("students.tsv");
    private JTextField idField, nameField, emailField, courseField, searchField;
    private DefaultTableModel model;
    private JTable table;
    private JLabel totalLabel, courseLabel, statusLabel;
    private JPanel contentPanel;
    private String selectedId = null;

    static class Student {
        String id, name, email, course;
        Student(String id, String name, String email, String course) {
            this.id = id; this.name = name; this.email = email; this.course = course;
        }
    }

    public Main() {
        super("Student Registration System | Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        buildUI();
        loadStudents();
        refreshTable();
        setVisible(true);
    }

    private JPanel panel(LayoutManager layout, Color background) {
        JPanel p = new JPanel(layout); p.setBackground(background); return p;
    }
    private JLabel label(String text, int size, boolean bold, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("SansSerif", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(color); return l;
    }
    private JButton button(String text, Color background, Color foreground) {
        JButton b = new JButton(text);
        b.setBackground(background); b.setForeground(foreground);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setFocusPainted(false); b.setOpaque(true);
        b.setBorder(new EmptyBorder(11, 15, 11, 15));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
    private JTextField field() {
        JTextField f = new JTextField();
        f.setFont(new Font("SansSerif", Font.PLAIN, 14));
        f.setBorder(new CompoundBorder(new LineBorder(new Color(211, 220, 234)), new EmptyBorder(8, 10, 8, 10)));
        return f;
    }
    private JPanel card(LayoutManager layout) {
        JPanel p = panel(layout, Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(new Color(225, 230, 240)), new EmptyBorder(18, 18, 18, 18)));
        return p;
    }
    private void buildUI() {
        JPanel sidebar = panel(new BorderLayout(), NAVY);
        sidebar.setPreferredSize(new Dimension(215, 0));
        JPanel sideTop = panel(new GridLayout(0, 1, 0, 14), NAVY);
        sideTop.setBorder(new EmptyBorder(32, 20, 20, 20));
        sideTop.add(label("STUDENT HUB", 21, true, Color.WHITE));
        sideTop.add(label("ADMIN PANEL", 12, false, new Color(176, 195, 225)));
        JButton dashboard = button("Dashboard", BLUE, Color.WHITE);
        JButton registration = button("Register Student", NAVY, Color.WHITE);
        JButton records = button("Student Records", NAVY, Color.WHITE);
        sideTop.add(dashboard); sideTop.add(registration); sideTop.add(records);
        sidebar.add(sideTop, BorderLayout.NORTH);
        JLabel foot = label("Java Swing  |  Local Storage", 11, false, new Color(176, 195, 225));
        foot.setBorder(new EmptyBorder(0, 18, 22, 0));
        sidebar.add(foot, BorderLayout.SOUTH);
        add(sidebar, BorderLayout.WEST);

        contentPanel = panel(new BorderLayout(0, 18), BG);
        contentPanel.setBorder(new EmptyBorder(25, 26, 20, 26));
        JPanel top = panel(new BorderLayout(), BG);
        top.add(label("Student Management Dashboard", 25, true, NAVY), BorderLayout.NORTH);
        JLabel subtitle = label("Manage registrations, student records and courses", 13, false, new Color(105, 117, 137));
        subtitle.setBorder(new EmptyBorder(7, 0, 0, 0));
        top.add(subtitle, BorderLayout.SOUTH);
        contentPanel.add(top, BorderLayout.NORTH);

        JPanel main = panel(new BorderLayout(0, 18), BG);
        JPanel stats = panel(new GridLayout(1, 2, 15, 0), BG);
        JPanel studentsCard = card(new BorderLayout(0, 9));
        studentsCard.add(label("TOTAL STUDENTS", 12, true, new Color(105, 117, 137)), BorderLayout.NORTH);
        totalLabel = label("0", 32, true, BLUE);
        studentsCard.add(totalLabel, BorderLayout.CENTER);
        JPanel coursesCard = card(new BorderLayout(0, 9));
        coursesCard.add(label("TOTAL COURSES", 12, true, new Color(105, 117, 137)), BorderLayout.NORTH);
        courseLabel = label("0", 32, true, BLUE);
        coursesCard.add(courseLabel, BorderLayout.CENTER);
        stats.add(studentsCard); stats.add(coursesCard);
        main.add(stats, BorderLayout.NORTH);

        JPanel center = panel(new BorderLayout(16, 0), BG);
        JPanel formCard = card(new BorderLayout(0, 14));
        formCard.setPreferredSize(new Dimension(340, 0));
        formCard.add(label("Student Registration", 18, true, NAVY), BorderLayout.NORTH);
        JPanel fields = panel(new GridBagLayout(), Color.WHITE);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        idField = field(); nameField = field(); emailField = field(); courseField = field();
        JTextField[] inputs = {idField, nameField, emailField, courseField};
        String[] labels = {"Student ID", "Full Name", "Email Address", "Course"};
        for (int i = 0; i < inputs.length; i++) {
            c.gridy = i * 2; c.insets = new Insets(7, 0, 5, 0);
            fields.add(label(labels[i], 13, true, NAVY), c);
            c.gridy = i * 2 + 1; c.insets = new Insets(0, 0, 10, 0);
            fields.add(inputs[i], c);
        }
        c.gridy = 8; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
        fields.add(Box.createVerticalGlue(), c);
        formCard.add(fields, BorderLayout.CENTER);
        JPanel actions = panel(new GridLayout(2, 2, 9, 9), Color.WHITE);
        JButton addBtn = button("Add Student", BLUE, Color.WHITE);
        JButton updateBtn = button("Update", new Color(29, 138, 109), Color.WHITE);
        JButton deleteBtn = button("Delete", new Color(197, 60, 75), Color.WHITE);
        JButton clearBtn = button("Clear", new Color(232, 237, 246), NAVY);
        actions.add(addBtn); actions.add(updateBtn); actions.add(deleteBtn); actions.add(clearBtn);
        formCard.add(actions, BorderLayout.SOUTH);
        center.add(formCard, BorderLayout.WEST);

        JPanel tableCard = card(new BorderLayout(0, 14));
        JPanel tableTop = panel(new BorderLayout(8, 8), Color.WHITE);
        tableTop.add(label("Registered Students", 18, true, NAVY), BorderLayout.NORTH);
        JPanel searchBar = panel(new BorderLayout(8, 0), Color.WHITE);
        searchField = field();
        searchBar.add(searchField, BorderLayout.CENTER);
        JButton searchBtn = button("Search", BLUE, Color.WHITE);
        JButton showAllBtn = button("Show All", new Color(232, 237, 246), NAVY);
        JPanel searchActions = panel(new FlowLayout(FlowLayout.RIGHT, 5, 0), Color.WHITE);
        searchActions.add(searchBtn); searchActions.add(showAllBtn);
        searchBar.add(searchActions, BorderLayout.EAST);
        tableTop.add(searchBar, BorderLayout.SOUTH);
        tableCard.add(tableTop, BorderLayout.NORTH);
        model = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Course"}, 0) {
            @Override public boolean isCellEditable(int r, int col) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(34); table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setGridColor(new Color(235, 238, 245));
        table.setSelectionBackground(new Color(220, 232, 253));
        table.setSelectionForeground(NAVY);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(235, 240, 250));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.setAutoCreateRowSorter(true);
        tableCard.add(new JScrollPane(table), BorderLayout.CENTER);
        center.add(tableCard, BorderLayout.CENTER);
        main.add(center, BorderLayout.CENTER);
        contentPanel.add(main, BorderLayout.CENTER);
        statusLabel = label("Ready  |  Data saved locally in students.tsv", 12, false, new Color(105, 117, 137));
        contentPanel.add(statusLabel, BorderLayout.SOUTH);
        add(contentPanel, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addStudent());
        updateBtn.addActionListener(e -> updateStudent());
        deleteBtn.addActionListener(e -> deleteStudent());
        clearBtn.addActionListener(e -> clearFields());
        searchBtn.addActionListener(e -> refreshTable());
        showAllBtn.addActionListener(e -> { searchField.setText(""); refreshTable(); });
        searchField.addActionListener(e -> refreshTable());
        dashboard.addActionListener(e -> { searchField.setText(""); refreshTable(); });
        registration.addActionListener(e -> { clearFields(); idField.requestFocusInWindow(); });
        records.addActionListener(e -> { searchField.requestFocusInWindow(); });
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            int viewRow = table.getSelectedRow();
            if (viewRow < 0) return;
            int row = table.convertRowIndexToModel(viewRow);
            String id = model.getValueAt(row, 0).toString();
            Student s = find(id);
            if (s != null) {
                selectedId = id;
                idField.setText(s.id); nameField.setText(s.name);
                emailField.setText(s.email); courseField.setText(s.course);
                idField.setEditable(false);
            }
        });
    }
    private Student find(String id) {
        for (Student s : students) if (s.id.equalsIgnoreCase(id)) return s;
        return null;
    }
    private boolean validInput() {
        String id = idField.getText().trim(), name = nameField.getText().trim();
        String email = emailField.getText().trim(), course = courseField.getText().trim();
        if (id.isEmpty() || name.isEmpty() || email.isEmpty() || course.isEmpty()) {
            alert("Please fill in all fields."); return false;
        }
        if (!Pattern.matches("^[A-Za-z0-9_-]{1,30}$", id)) {
            alert("ID must be 1-30 letters, numbers, underscores or hyphens."); return false;
        }
        if (!Pattern.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$", email)) {
            alert("Please enter a valid email address."); return false;
        }
        for (String value : new String[]{name, email, course}) {
            if (value.contains("\t") || value.contains("\n") || value.contains("\r")) {
                alert("Tabs and new lines are not allowed."); return false;
            }
        }
        return true;
    }
    private void addStudent() {
        if (!validInput()) return;
        String id = idField.getText().trim();
        if (find(id) != null) { alert("This Student ID already exists."); return; }
        students.add(new Student(id, nameField.getText().trim(), emailField.getText().trim(), courseField.getText().trim()));
        if (!saveStudents()) { students.remove(students.size() - 1); return; }
        clearFields(); refreshTable(); statusLabel.setText("Student registered successfully.");
    }
    private void updateStudent() {
        if (selectedId == null) { alert("Select a student from the table first."); return; }
        if (!validInput()) return;
        Student s = find(selectedId);
        if (s == null) { alert("Student not found."); return; }
        String oldName = s.name, oldEmail = s.email, oldCourse = s.course;
        s.name = nameField.getText().trim(); s.email = emailField.getText().trim(); s.course = courseField.getText().trim();
        if (!saveStudents()) { s.name = oldName; s.email = oldEmail; s.course = oldCourse; return; }
        clearFields(); refreshTable(); statusLabel.setText("Student updated successfully.");
    }
    private void deleteStudent() {
        if (selectedId == null) { alert("Select a student first."); return; }
        Student s = find(selectedId);
        if (s == null) { alert("Student not found."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Delete student " + s.name + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        int index = students.indexOf(s); students.remove(index);
        if (!saveStudents()) { students.add(index, s); return; }
        clearFields(); refreshTable(); statusLabel.setText("Student deleted successfully.");
    }
    private void clearFields() {
        selectedId = null;
        idField.setEditable(true);
        idField.setText(""); nameField.setText(""); emailField.setText(""); courseField.setText("");
        table.clearSelection();
    }
    private void refreshTable() {
        if (model == null) return;
        String query = searchField.getText().trim().toLowerCase(Locale.ROOT);
        model.setRowCount(0);
        for (Student s : students) {
            if ((s.id + " " + s.name + " " + s.email + " " + s.course).toLowerCase(Locale.ROOT).contains(query))
                model.addRow(new Object[]{s.id, s.name, s.email, s.course});
        }
        totalLabel.setText(String.valueOf(students.size()));
        Set<String> courses = new HashSet<>();
        for (Student s : students) courses.add(s.course.toLowerCase(Locale.ROOT));
        courseLabel.setText(String.valueOf(courses.size()));
    }
    private void alert(String message) { JOptionPane.showMessageDialog(this, message, "Student Hub", JOptionPane.WARNING_MESSAGE); }
    private boolean saveStudents() {
        try (BufferedWriter writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
            for (Student s : students) {
                writer.write(String.join("\t", s.id, s.name, s.email, s.course)); writer.newLine();
            }
            return true;
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not save records: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }
    private void loadStudents() {
        // Import records from the earlier version if its file exists.
        Path source = Files.exists(dataFile) ? dataFile : Paths.get("students.txt");
        if (!Files.exists(source)) return;
        try (BufferedReader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split("\t", -1);
                if (values.length == 4 && find(values[0]) == null)
                    students.add(new Student(values[0], values[1], values[2], values[3]));
            }
            if (!source.equals(dataFile)) statusLabel.setText("Old students.txt records loaded. Next edit will save to students.tsv.");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Could not load records: " + ex.getMessage(), "Load Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::new);
    }
}
