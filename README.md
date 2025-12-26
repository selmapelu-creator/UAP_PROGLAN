SehatQueue - Sistem Pendaftaran Pasien

SehatQueue adalah aplikasi desktop berbasis Java Swing yang dibuat untuk membantu proses administrasi pendaftaran pasien di rumah sakit atau klinik sederhana. Aplikasi ini dikembangkan sebagai Proyek Akhir Praktikum Pemrograman Lanjut.

Daftar Isi

Fitur Utama
Teknologi yang Digunakan
Struktur Proyek
Persyaratann Sistem
Cara Menjalankan Aplikasi
Akun Demo
Penjelasan Implementasi Modul
Fitur Utama Aplikasi ini memiliki beberapa fitur kunci untuk mengelola data pasien:

Login Admin Halaman masuk yang membatasi akses aplikasi hanya untuk pengguna yang memiliki kredensial admin.

Dashboard Ringkasan Halaman utama yang menampilkan informasi cepat mengenai total pasien yang terdaftar dan tanggal hari ini.

Manajemen Data Pasien (CRUD) Tambah Pasien: Formulir untuk memasukkan data pasien baru (Nama, Umur, Diagnosis). Lihat Data: Menampilkan daftar pasien dalam bentuk tabel. Hapus Data: Menghapus data pasien dari sistem. Edit Data: (Simulasi) Memperbarui data pasien yang sudah ada.

Pencarian dan Pengurutan Memudahkan admin mencari pasien berdasarkan nama atau mengurutkan data berdasarkan abjad dan tanggal pendaftaran.

Penyimpanan Data (File Handling) Data pasien disimpan secara otomatis ke dalam file CSV (patients_data.csv), sehingga data tidak hilang meskipun aplikasi ditutup.

Riwayat Aktivitas Halaman log yang mencatat waktu login admin dan status pemuatan data.

Teknologi yang Digunakan

Bahasa Pemrograman: Java (JDK 8 atau lebih baru) Antarmuka (GUI): Java Swing (JFrame, JPanel, CardLayout, JTable) Penyimpanan Data: File CSV (Comma Separated Values) Editor: IntelliJ IDEA / Netbeans

Struktur Proyek

Kode program ini disusun dalam satu file utama namun tetap menerapkan konsep pemrograman modular:

Model (Class Patient) Merepresentasikan data pasien dengan atribut seperti ID, Nama, Umur, Diagnosis, dan Tanggal. Utilities (Class DataManager) Menangani proses membaca dan menulis data ke file CSV. View/Controller (Class HospitalApp) Mengatur tampilan antarmuka, navigasi antar halaman, dan logika interaksi pengguna.

Persyaratan Sistem

Sebelum menjalankan aplikasi ini, pastikan komputer Anda telah terinstal:

Java Development Kit (JDK) versi 8 ke atas.
IDE Java (seperti IntelliJ IDEA, Eclipse, atau Netbeans) atau Terminal/Command
