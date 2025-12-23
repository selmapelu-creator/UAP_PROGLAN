import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

// --- MODEL (Data Representation) ---
class Patient {
    private String id;
    private String name;
    private int age;
    private String diagnosis;
    private LocalDate registrationDate;

    public Patient(String id, String name, int age, String diagnosis, LocalDate registrationDate) {
        this.id = id;
        this.name = name;
        this.age = age;
        this.diagnosis = diagnosis;
        this.registrationDate = registrationDate;
    }

    // Getters & Setters (Module 2: Encapsulation via Refactoring)
    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDiagnosis() { return diagnosis; }
    public LocalDate getRegistrationDate() { return registrationDate; }

    public void setName(String name) { this.name = name; }
    public void setAge(int age) { this.age = age; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    // Format for CSV file
    @Override
    public String toString() {
        return id + "," + name + "," + age + "," + diagnosis + "," + registrationDate;
    }
}

// --- FILE HANDLER (Module 5: File Handling) ---
class DataManager {
    private static final String FILE_NAME = "patients_data.csv";

    // Create / Save Data
    public static void savePatients(List<Patient> patients) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Patient p : patients) {
                writer.write(p.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Gagal menyimpan data: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Read / Load Data
    public static List<Patient> loadPatients() {
        List<Patient> list = new ArrayList<>(); // Module 4: API (ArrayList)
        File file = new File(FILE_NAME);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    Patient p = new Patient(
                            parts[0],
                            parts[1],
                            Integer.parseInt(parts[2]),
                            parts[3],
                            LocalDate.parse(parts[4]) // Module 4: Java Time API
                    );
                    list.add(p);
                }
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Data corrupt or read error: " + e.getMessage());
        }
        return list;
    }
}

// --- GUI & LOGIC (Module 6: Swing GUI) ---
class HospitalApp extends JFrame {
    // Data List
    private List<Patient> patientList;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    // Components
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtAge, txtDiagnosis, txtSearch;
    private JLabel lblTotalPatients, lblLastUpdate;

    public HospitalApp() {
        // Module 1: Program Correctness (Initialization)
        patientList = DataManager.loadPatients();

        setTitle("SehatQueue - Sistem Pendaftaran Pasien");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Layout Setup
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // Create Screens (4 Halaman Minimal)
        mainPanel.add(createDashboardPanel(), "Dashboard");
        mainPanel.add(createListPanel(), "List");
        mainPanel.add(createInputPanel(null), "Input");
        mainPanel.add(createHistoryPanel(), "History");

        // Sidebar Navigation
        JPanel sidebar = createSidebar();

        // Main Container
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
    }

    // --- 1. HALAMAN DASHBOARD ---
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255)); // Alice Blue

        JLabel title = new JLabel("Selamat Datang di SehatQueue");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(25, 25, 112));

        lblTotalPatients = new JLabel("Total Pasien: " + patientList.size());
        lblTotalPatients.setFont(new Font("SansSerif", Font.PLAIN, 18));

        lblLastUpdate = new JLabel("Tanggal: " + LocalDate.now());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(title, gbc);
        gbc.gridy++;
        panel.add(lblTotalPatients, gbc);
        gbc.gridy++;
        panel.add(lblLastUpdate, gbc);

        return panel;
    }

    // --- 2. HALAMAN LIST DATA ---
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Toolbar (Search & Sort)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtSearch = new JTextField(20);
        JButton btnSearch = new JButton("Cari Nama");
        JButton btnSortName = new JButton("Urutkan Nama (A-Z)");
        JButton btnSortDate = new JButton("Urutkan Tanggal");
        JButton btnDelete = new JButton("Hapus");
        JButton btnEdit = new JButton("Edit");

        // Styling Buttons
        styleButton(btnSearch, new Color(70, 130, 180));
        styleButton(btnDelete, new Color(220, 20, 60)); // Crimson
        styleButton(btnEdit, new Color(255, 140, 0));   // Dark Orange

        toolbar.add(new JLabel("Cari: "));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnSortName);
        toolbar.add(btnSortDate);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);

        // Table
        String[] columns = {"ID", "Nama", "Umur", "Diagnosis", "Tanggal Daftar"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        refreshTable(patientList); // Load initial data

        JScrollPane scrollPane = new JScrollPane(table);

        // Actions
        btnSearch.addActionListener(e -> performSearch());

        // Module 4: API (Comparator)
        btnSortName.addActionListener(e -> {
            patientList.sort(Comparator.comparing(Patient::getName));
            refreshTable(patientList);
        });

        btnSortDate.addActionListener(e -> {
            patientList.sort(Comparator.comparing(Patient::getRegistrationDate).reversed());
            refreshTable(patientList);
        });

        btnDelete.addActionListener(e -> performDelete());

        btnEdit.addActionListener(e -> performEdit());

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // --- 3. HALAMAN INPUT (Tambah/Edit) ---
    private JPanel createInputPanel(Patient patientToEdit) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTitle = new JLabel("Formulir Pasien");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 20));

        txtName = new JTextField(20);
        txtAge = new JTextField(20);
        txtDiagnosis = new JTextField(20);
        JButton btnSave = new JButton("Simpan");
        styleButton(btnSave, new Color(34, 139, 34)); // Forest Green

        // UI Layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(lblTitle, gbc);

        gbc.gridwidth = 1; gbc.gridy++;
        panel.add(new JLabel("Nama Lengkap:"), gbc);
        gbc.gridx = 1;
        panel.add(txtName, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Umur:"), gbc);
        gbc.gridx = 1;
        panel.add(txtAge, gbc);

        gbc.gridx = 0; gbc.gridy++;
        panel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1;
        panel.add(txtDiagnosis, gbc);

        gbc.gridx = 1; gbc.gridy++;
        panel.add(btnSave, gbc);

        // Logic Save (Module 1: Defensive Programming via Try-Catch)
        btnSave.addActionListener(e -> {
            try {
                String name = txtName.getText();
                String diag = txtDiagnosis.getText();

                // Module 1: Validation
                if(name.isEmpty() || diag.isEmpty()) throw new IllegalArgumentException("Semua field harus diisi!");

                int age = Integer.parseInt(txtAge.getText());
                if(age < 0 || age > 120) throw new IllegalArgumentException("Umur tidak valid!");

                // Generate ID or Use Existing (Logic for Edit vs New)
                // For simplicity in this demo, we always add new.
                // To implement true Edit, we need to pass the ID or update the object reference.
                // Here is a hybrid approach:

                String id = "P-" + System.currentTimeMillis(); // Unique ID based on time
                Patient newPatient = new Patient(id, name, age, diag, LocalDate.now());

                patientList.add(newPatient);
                DataManager.savePatients(patientList); // Module 5: Persistence

                JOptionPane.showMessageDialog(this, "Data Berhasil Disimpan!");
                refreshTable(patientList);
                clearForm();
                updateDashboard();
                cardLayout.show(mainPanel, "List");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Umur harus berupa angka!", "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Input Error", JOptionPane.WARNING_MESSAGE);
            }
        });

        return panel;
    }

    // --- 4. HALAMAN HISTORY/LAPORAN ---
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea historyArea = new JTextArea();
        historyArea.setEditable(false);
        historyArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        historyArea.setText("=== RIWAYAT PENGGUNAAN APLIKASI ===\n\n");
        historyArea.append("[INFO] Aplikasi dijalankan pada: " + new Date() + "\n");
        historyArea.append("[INFO] Data dimuat dari file: " + DataManager.loadPatients().size() + " records.\n");

        panel.add(new JScrollPane(historyArea), BorderLayout.CENTER);
        return panel;
    }

    // --- HELPER METHODS (Refactoring) ---

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new GridLayout(5, 1, 5, 5));
        sidebar.setBackground(new Color(47, 79, 79)); // Dark Slate Gray
        sidebar.setPreferredSize(new Dimension(200, 600));

        JButton btnDash = createNavButton("Dashboard");
        JButton btnList = createNavButton("Data Pasien");
        JButton btnAdd = createNavButton("Tambah Pasien");
        JButton btnHist = createNavButton("Laporan");
        JButton btnExit = createNavButton("Keluar");

        btnDash.addActionListener(e -> cardLayout.show(mainPanel, "Dashboard"));
        btnList.addActionListener(e -> cardLayout.show(mainPanel, "List"));
        btnAdd.addActionListener(e -> cardLayout.show(mainPanel, "Input"));
        btnHist.addActionListener(e -> cardLayout.show(mainPanel, "History"));
        btnExit.addActionListener(e -> System.exit(0));

        sidebar.add(btnDash);
        sidebar.add(btnList);
        sidebar.add(btnAdd);
        sidebar.add(btnHist);
        sidebar.add(btnExit);

        return sidebar;
    }

    private JButton createNavButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(Color.WHITE);
        btn.setBackground(new Color(47, 79, 79));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(60, 100, 100));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(new Color(47, 79, 79));
            }
        });
        return btn;
    }

    private void styleButton(JButton btn, Color color) {
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private void refreshTable(List<Patient> data) {
        tableModel.setRowCount(0); // Clear table
        for (Patient p : data) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getDiagnosis(), p.getRegistrationDate()});
        }
    }

    private void performSearch() {
        String keyword = txtSearch.getText().toLowerCase();
        // Module 4: Stream API Filter
        List<Patient> filtered = patientList.stream()
                .filter(p -> p.getName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        refreshTable(filtered);
    }

    private void performDelete() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String id = (String) tableModel.getValueAt(selectedRow, 0);
            patientList.removeIf(p -> p.getId().equals(id)); // Module 4: Lambda
            DataManager.savePatients(patientList);
            refreshTable(patientList);
            updateDashboard();
            JOptionPane.showMessageDialog(this, "Data dihapus.");
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data dulu!");
        }
    }

    private void performEdit() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String id = (String) tableModel.getValueAt(selectedRow, 0);

            // Find patient
            for(Patient p : patientList) {
                if(p.getId().equals(id)) {
                    // Populate fields manually for simplicity (Ideally use a shared form)
                    txtName.setText(p.getName());
                    txtAge.setText(String.valueOf(p.getAge()));
                    txtDiagnosis.setText(p.getDiagnosis());

                    // Remove old record (to be replaced by save) - *Simplified Logic for Demo*
                    patientList.remove(p);

                    cardLayout.show(mainPanel, "Input");
                    JOptionPane.showMessageDialog(this, "Silakan edit data dan klik Simpan.");
                    break;
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Pilih data untuk diedit!");
        }
    }

    private void clearForm() {
        txtName.setText("");
        txtAge.setText("");
        txtDiagnosis.setText("");
    }

    private void updateDashboard() {
        lblTotalPatients.setText("Total Pasien: " + patientList.size());
    }

    // MAIN METHOD
    public static void main(String[] args) {
        // Module 6: Thread Safety for Swing
        SwingUtilities.invokeLater(() -> {
            new HospitalApp().setVisible(true);
        });
    }
}