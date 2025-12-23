import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

// --- MODEL ---
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

    public String getId() { return id; }
    public String getName() { return name; }
    public int getAge() { return age; }
    public String getDiagnosis() { return diagnosis; }
    public LocalDate getRegistrationDate() { return registrationDate; }

    @Override
    public String toString() {
        return id + "," + name + "," + age + "," + diagnosis + "," + registrationDate;
    }
}

// --- FILE HANDLER ---
class DataManager {
    // [CODE REVIEW FIX]: Menggunakan konstanta agar mudah dikelola
    private static final String FILE_NAME = "patients_data.csv";

    public static void savePatients(List<Patient> patients) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Patient p : patients) {
                writer.write(p.toString());
                writer.newLine();
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error Save: " + e.getMessage());
        }
    }

    public static List<Patient> loadPatients() {
        List<Patient> list = new ArrayList<>();
        File file = new File(FILE_NAME);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 5) {
                    list.add(new Patient(parts[0], parts[1], Integer.parseInt(parts[2]), parts[3], LocalDate.parse(parts[4])));
                }
            }
        } catch (Exception e) {
            System.err.println("Error Load: " + e.getMessage());
        }
        return list;
    }
}

// --- GUI MAIN FRAME ---
public class HospitalApp extends JFrame {
    private List<Patient> patientList;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private JPanel sidebar; // Sidebar dipisah agar bisa disembunyikan saat login

    // Components
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtName, txtAge, txtDiagnosis, txtSearch;
    private JLabel lblTotalPatients, lblLastUpdate;
    
    // Login Components
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public HospitalApp() {
        patientList = DataManager.loadPatients();
        
        setTitle("SehatQueue - Sistem Pendaftaran Pasien");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        // [FEATURE ADDITION]: Menambahkan Halaman Login
        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createDashboardPanel(), "Dashboard");
        mainPanel.add(createListPanel(), "List");
        mainPanel.add(createInputPanel(), "Input");
        mainPanel.add(createHistoryPanel(), "History");

        // Sidebar Navigation (Hidden initially)
        sidebar = createSidebar();
        sidebar.setVisible(false);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(sidebar, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        // Start at Login
        cardLayout.show(mainPanel, "Login");
    }

    // --- 0. HALAMAN LOGIN ---
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        JLabel title = new JLabel("LOGIN ADMIN");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        
        txtUsername = new JTextField(15);
        txtPassword = new JPasswordField(15);
        JButton btnLogin = new JButton("Masuk");
        styleButton(btnLogin, new Color(70, 130, 180));

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);
        
        gbc.gridwidth = 1; gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        panel.add(txtUsername, gbc);
        
        gbc.gridy++; gbc.gridx = 0;
        panel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        panel.add(txtPassword, gbc);
        
        gbc.gridy++; gbc.gridx = 1;
        panel.add(btnLogin, gbc);

        btnLogin.addActionListener(e -> {
            String user = txtUsername.getText();
            String pass = new String(txtPassword.getPassword());
            
            // Simple validation
            if (user.equals("admin") && pass.equals("admin")) {
                sidebar.setVisible(true); // Show menu
                updateDashboard();
                cardLayout.show(mainPanel, "Dashboard");
            } else {
                JOptionPane.showMessageDialog(this, "Username/Password Salah!", "Login Gagal", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // --- 1. DASHBOARD ---
    private JPanel createDashboardPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Selamat Datang, Admin!");
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(47, 79, 79));

        lblTotalPatients = new JLabel("Total Pasien: " + patientList.size());
        lblTotalPatients.setFont(new Font("SansSerif", Font.PLAIN, 18));
        
        lblLastUpdate = new JLabel("Hari ini: " + LocalDate.now());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 10, 10, 10);
        panel.add(title, gbc);
        gbc.gridy++; panel.add(lblTotalPatients, gbc);
        gbc.gridy++; panel.add(lblLastUpdate, gbc);

        return panel;
    }

    // --- 2. LIST DATA ---
    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        txtSearch = new JTextField(15);
        JButton btnSearch = new JButton("Cari");
        JButton btnSort = new JButton("Urutkan (A-Z)");
        JButton btnDelete = new JButton("Hapus");
        
        styleButton(btnSearch, new Color(100, 149, 237));
        styleButton(btnSort, new Color(100, 149, 237));
        styleButton(btnDelete, new Color(220, 20, 60));

        toolbar.add(new JLabel("Cari Nama:"));
        toolbar.add(txtSearch);
        toolbar.add(btnSearch);
        toolbar.add(btnSort);
        toolbar.add(btnDelete);

        String[] cols = {"ID", "Nama", "Umur", "Diagnosis", "Tanggal"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel);
        refreshTable(patientList);

        btnSearch.addActionListener(e -> performSearch());
        btnSort.addActionListener(e -> {
            patientList.sort(Comparator.comparing(Patient::getName));
            refreshTable(patientList);
        });
        btnDelete.addActionListener(e -> performDelete());

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        return panel;
    }

    // --- 3. INPUT DATA ---
    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtName = new JTextField(20);
        txtAge = new JTextField(20);
        txtDiagnosis = new JTextField(20);
        JButton btnSave = new JButton("Simpan Data");
        styleButton(btnSave, new Color(34, 139, 34));

        gbc.gridx = 0; gbc.gridy = 0; panel.add(new JLabel("Nama:"), gbc);
        gbc.gridx = 1; panel.add(txtName, gbc);
        
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Umur:"), gbc);
        gbc.gridx = 1; panel.add(txtAge, gbc);
        
        gbc.gridx = 0; gbc.gridy++; panel.add(new JLabel("Diagnosis:"), gbc);
        gbc.gridx = 1; panel.add(txtDiagnosis, gbc);
        
        gbc.gridx = 1; gbc.gridy++; panel.add(btnSave, gbc);

        btnSave.addActionListener(e -> saveData());
        return panel;
    }

    // --- 4. HISTORY ---
    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea();
        area.setText("=== Log Aktivitas ===\nSistem Siap digunakan.\nAdmin Login pada: " + new Date());
        panel.add(new JScrollPane(area));
        return panel;
    }

    // --- LOGIC METHODS ---
    private void saveData() {
        try {
            String name = txtName.getText();
            String diag = txtDiagnosis.getText();
            if(name.isEmpty() || diag.isEmpty()) throw new IllegalArgumentException("Data tidak boleh kosong!");
            
            int age = Integer.parseInt(txtAge.getText());
            if(age < 0) throw new IllegalArgumentException("Umur tidak valid!");

            Patient p = new Patient("P-" + System.currentTimeMillis(), name, age, diag, LocalDate.now());
            patientList.add(p);
            DataManager.savePatients(patientList);
            
            JOptionPane.showMessageDialog(this, "Sukses!");
            refreshTable(patientList);
            txtName.setText(""); txtAge.setText(""); txtDiagnosis.setText("");
            cardLayout.show(mainPanel, "List");
            updateDashboard();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Umur harus angka!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void performSearch() {
        String key = txtSearch.getText().toLowerCase();
        List<Patient> filtered = patientList.stream()
                .filter(p -> p.getName().toLowerCase().contains(key))
                .collect(Collectors.toList());
        refreshTable(filtered);
    }

    private void performDelete() {
        int row = table.getSelectedRow();
        if (row >= 0) {
            String id = (String) tableModel.getValueAt(row, 0);
            patientList.removeIf(p -> p.getId().equals(id));
            DataManager.savePatients(patientList);
            refreshTable(patientList);
            updateDashboard();
            JOptionPane.showMessageDialog(this, "Dihapus.");
        } else {
            JOptionPane.showMessageDialog(this, "Pilih baris dulu.");
        }
    }

    private void updateDashboard() {
        lblTotalPatients.setText("Total Pasien: " + patientList.size());
    }

    private void refreshTable(List<Patient> data) {
        tableModel.setRowCount(0);
        for (Patient p : data) {
            tableModel.addRow(new Object[]{p.getId(), p.getName(), p.getAge(), p.getDiagnosis(), p.getRegistrationDate()});
        }
    }

    private JPanel createSidebar() {
        JPanel p = new JPanel(new GridLayout(6, 1, 5, 5));
        p.setBackground(new Color(47, 79, 79));
        p.setPreferredSize(new Dimension(180, 0));
        
        JButton[] btns = {
            new JButton("Dashboard"), new JButton("Data Pasien"), 
            new JButton("Input Baru"), new JButton("History"), new JButton("Logout")
        };
        
        String[] cmds = {"Dashboard", "List", "Input", "History", "Logout"};

        for (int i = 0; i < btns.length; i++) {
            JButton b = btns[i];
            String cmd = cmds[i];
            b.setBackground(new Color(47, 79, 79));
            b.setForeground(Color.WHITE);
            b.setFocusPainted(false);
            b.setBorderPainted(false);
            b.setFont(new Font("SansSerif", Font.BOLD, 14));
            
            b.addActionListener(e -> {
                if(cmd.equals("Logout")) {
                    sidebar.setVisible(false);
                    cardLayout.show(mainPanel, "Login");
                    txtUsername.setText(""); txtPassword.setText("");
                } else {
                    cardLayout.show(mainPanel, cmd);
                }
            });
            p.add(b);
        }
        return p;
    }

    private void styleButton(JButton b, Color c) {
        b.setBackground(c);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new HospitalApp().setVisible(true));
    }
}