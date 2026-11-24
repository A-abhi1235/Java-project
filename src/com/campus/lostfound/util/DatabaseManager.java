package com.campus.lostfound.util;

import com.campus.lostfound.config.AppConfig;
import com.campus.lostfound.exception.DatabaseException;
import com.campus.lostfound.model.Role;

import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Manages SQLite database connection lifecycle, DDL migrations, and initial seeding.
 */
public class DatabaseManager {
    private static DatabaseManager instance;

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL: SQLite JDBC Driver not found in classpath!");
        }
    }

    private DatabaseManager() {
        ensureDataDirectoryExists();
        initializeDatabase();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void ensureDataDirectoryExists() {
        File dataDir = new File(AppConfig.DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        File imagesDir = new File(AppConfig.IMAGES_DIR);
        if (!imagesDir.exists()) {
            imagesDir.mkdirs();
        }
    }

    public Connection getConnection() throws DatabaseException {
        try {
            Connection conn = DriverManager.getConnection(AppConfig.JDBC_URL);
            // Enable SQLite Foreign Key constraints
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON;");
            }
            return conn;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to establish SQLite database connection: " + e.getMessage(), e);
        }
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Users Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS users (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  username TEXT UNIQUE NOT NULL," +
                "  password_hash TEXT NOT NULL," +
                "  salt TEXT NOT NULL," +
                "  full_name TEXT NOT NULL," +
                "  email TEXT NOT NULL," +
                "  phone TEXT," +
                "  role TEXT NOT NULL DEFAULT 'STUDENT'," +
                "  student_id TEXT," +
                "  created_at TEXT NOT NULL" +
                ");"
            );

            // 2. Categories Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS categories (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  name TEXT UNIQUE NOT NULL," +
                "  description TEXT," +
                "  icon TEXT" +
                ");"
            );

            // 3. Items Table (Polymorphic: LostItem & FoundItem)
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS items (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  item_type TEXT NOT NULL," + // 'LOST' or 'FOUND'
                "  title TEXT NOT NULL," +
                "  description TEXT NOT NULL," +
                "  category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE RESTRICT," +
                "  location TEXT NOT NULL," +
                "  date_reported TEXT NOT NULL," + // ISO date (YYYY-MM-DD)
                "  image_url TEXT," +
                "  status TEXT NOT NULL DEFAULT 'OPEN'," + // OPEN, CLAIM_PENDING, CLAIMED, RETURNED, CLOSED
                "  reported_by_user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                "  contact_phone TEXT," +
                "  contact_email TEXT," +
                "  reward_amount REAL DEFAULT 0.0," + // Specific to LostItem
                "  last_seen_details TEXT," +         // Specific to LostItem
                "  storage_location TEXT," +          // Specific to FoundItem
                "  verification_question TEXT," +     // Specific to FoundItem
                "  created_at TEXT NOT NULL," +
                "  updated_at TEXT NOT NULL" +
                ");"
            );

            // Indexes for fast searching and filtering
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_items_type_status ON items(item_type, status);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_items_category ON items(category_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_items_location ON items(location);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_items_reported_by ON items(reported_by_user_id);");

            // 4. Claims Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS claims (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  item_id INTEGER NOT NULL REFERENCES items(id) ON DELETE CASCADE," +
                "  claimant_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                "  proof_details TEXT NOT NULL," +
                "  status TEXT NOT NULL DEFAULT 'PENDING'," + // PENDING, APPROVED, REJECTED, CANCELLED
                "  reviewer_notes TEXT," +
                "  reviewed_by_user_id INTEGER REFERENCES users(id) ON DELETE SET NULL," +
                "  created_at TEXT NOT NULL," +
                "  reviewed_at TEXT" +
                ");"
            );
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_claims_item ON claims(item_id);");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_claims_claimant ON claims(claimant_id);");

            // 5. Notifications Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS notifications (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE," +
                "  title TEXT NOT NULL," +
                "  message TEXT NOT NULL," +
                "  is_read INTEGER NOT NULL DEFAULT 0," +
                "  related_item_id INTEGER REFERENCES items(id) ON DELETE SET NULL," +
                "  created_at TEXT NOT NULL" +
                ");"
            );
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_notif_user ON notifications(user_id, is_read);");

            // 6. Audit Logs Table
            stmt.execute(
                "CREATE TABLE IF NOT EXISTS audit_logs (" +
                "  id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "  user_id INTEGER REFERENCES users(id) ON DELETE SET NULL," +
                "  action TEXT NOT NULL," +
                "  details TEXT," +
                "  created_at TEXT NOT NULL" +
                ");"
            );

            // Seed initial data
            seedCategories(conn);
            seedDefaultAdmin(conn);
            seedSampleCampusData(conn);

        } catch (SQLException | DatabaseException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void seedCategories(Connection conn) throws SQLException {
        String countSql = "SELECT COUNT(*) FROM categories";
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(countSql)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // already seeded
            }
        }

        String insert = "INSERT INTO categories (name, description, icon) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insert)) {
            Object[][] defaultCats = {
                {"Electronics & Gadgets", "Laptops, phones, headphones, chargers, tablets, power banks", "laptop"},
                {"Wallets & Purses", "Wallets, coin pouches, cardholders", "wallet"},
                {"ID Cards & Documents", "Campus ID cards, driver licenses, passports, notebooks, certificates", "id-card"},
                {"Keys & Access Badges", "Dorm keys, bike keys, vehicle key fobs, NFC cards", "key"},
                {"Bags & Backpacks", "Backpacks, tote bags, gym bags, laptop sleeves", "briefcase"},
                {"Books & Stationery", "Textbooks, notebooks, pencil cases, scientific calculators", "book"},
                {"Clothing & Apparel", "Jackets, hoodies, scarves, hats, sports jerseys", "shirt"},
                {"Bottles & Flasks", "Hydro flasks, thermos mugs, water bottles", "coffee"},
                {"Jewelry & Watches", "Wristwatches, smartwatches, rings, chains, sunglasses", "watch"},
                {"Sports & Musical", "Badminton racquets, balls, guitars, musical instruments", "dribbble"},
                {"Miscellaneous", "Umbrellas, eyeglasses, other campus items", "box"}
            };

            for (Object[] cat : defaultCats) {
                ps.setString(1, (String) cat[0]);
                ps.setString(2, (String) cat[1]);
                ps.setString(3, (String) cat[2]);
                ps.executeUpdate();
            }
        }
    }

    private void seedDefaultAdmin(Connection conn) throws SQLException {
        String checkAdmin = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement ps = conn.prepareStatement(checkAdmin)) {
            ps.setString(1, AppConfig.DEFAULT_ADMIN_USERNAME);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    return; // admin exists
                }
            }
        }

        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(AppConfig.DEFAULT_ADMIN_PASSWORD, salt);

        String insertAdmin = "INSERT INTO users (username, password_hash, salt, full_name, email, phone, role, student_id, created_at) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertAdmin)) {
            ps.setString(1, AppConfig.DEFAULT_ADMIN_USERNAME);
            ps.setString(2, hash);
            ps.setString(3, salt);
            ps.setString(4, AppConfig.DEFAULT_ADMIN_NAME);
            ps.setString(5, AppConfig.DEFAULT_ADMIN_EMAIL);
            ps.setString(6, "+1-555-0199");
            ps.setString(7, Role.ADMIN.name());
            ps.setString(8, "STAFF-001");
            ps.setString(9, DateUtil.formatIsoDateTime(LocalDateTime.now()));
            ps.executeUpdate();
        }
    }

    private void seedSampleCampusData(Connection conn) throws SQLException {
        // Check if sample student exists
        String checkStudent = "SELECT COUNT(*) FROM users WHERE username = 'john_student'";
        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(checkStudent)) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // sample data already exists
            }
        }

        // Insert sample students
        String saltJohn = PasswordUtil.generateSalt();
        String hashJohn = PasswordUtil.hashPassword("student123", saltJohn);
        int johnId = 0;

        String insertUser = "INSERT INTO users (username, password_hash, salt, full_name, email, phone, role, student_id, created_at) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "john_student");
            ps.setString(2, hashJohn);
            ps.setString(3, saltJohn);
            ps.setString(4, "Johnathan Doe");
            ps.setString(5, "john.doe@campus.edu");
            ps.setString(6, "+1-555-0101");
            ps.setString(7, Role.STUDENT.name());
            ps.setString(8, "STU-2024-8841");
            ps.setString(9, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(10)));
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) johnId = gk.getInt(1);
            }
        }

        String saltEmma = PasswordUtil.generateSalt();
        String hashEmma = PasswordUtil.hashPassword("student123", saltEmma);
        int emmaId = 0;
        try (PreparedStatement ps = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, "emma_watson");
            ps.setString(2, hashEmma);
            ps.setString(3, saltEmma);
            ps.setString(4, "Emma Watson");
            ps.setString(5, "emma.w@campus.edu");
            ps.setString(6, "+1-555-0102");
            ps.setString(7, Role.STUDENT.name());
            ps.setString(8, "STU-2024-9120");
            ps.setString(9, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(8)));
            ps.executeUpdate();
            try (ResultSet gk = ps.getGeneratedKeys()) {
                if (gk.next()) emmaId = gk.getInt(1);
            }
        }

        // Insert sample items
        String insertItem = "INSERT INTO items (" +
            "item_type, title, description, category_id, location, date_reported, image_url, " +
            "status, reported_by_user_id, contact_phone, contact_email, reward_amount, " +
            "last_seen_details, storage_location, verification_question, created_at, updated_at" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(insertItem)) {
            // Item 1: Lost - Apple MacBook Air M2
            ps.setString(1, "LOST");
            ps.setString(2, "Space Grey MacBook Air M2 13-inch");
            ps.setString(3, "Lost during study group on 2nd floor silent reading zone. Has sticker of NASA and GitHub on top lid.");
            ps.setInt(4, 1); // Electronics
            ps.setString(5, "Central Library");
            ps.setString(6, DateUtil.formatIsoDate(LocalDate.now().minusDays(2)));
            ps.setString(7, null);
            ps.setString(8, "OPEN");
            ps.setInt(9, johnId);
            ps.setString(10, "+1-555-0101");
            ps.setString(11, "john.doe@campus.edu");
            ps.setDouble(12, 50.0);
            ps.setString(13, "Left on desk #14 near East window");
            ps.setString(14, null);
            ps.setString(15, null);
            ps.setString(16, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(2)));
            ps.setString(17, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(2)));
            ps.executeUpdate();

            // Item 2: Found - Blue Hydro Flask
            ps.setString(1, "FOUND");
            ps.setString(2, "Deep Blue Hydro Flask (32 oz)");
            ps.setString(3, "Found on the bench beside the indoor basketball court after evening practice. Has some small scratches on bottom.");
            ps.setInt(4, 8); // Bottles & Flasks
            ps.setString(5, "Sports Ground / Gymnasium");
            ps.setString(6, DateUtil.formatIsoDate(LocalDate.now().minusDays(1)));
            ps.setString(7, null);
            ps.setString(8, "OPEN");
            ps.setInt(9, emmaId);
            ps.setString(10, "+1-555-0102");
            ps.setString(11, "emma.w@campus.edu");
            ps.setDouble(12, 0.0);
            ps.setString(13, null);
            ps.setString(14, "Security Guard Desk at Gym entrance");
            ps.setString(15, "What color is the silicone boot / straw cap?");
            ps.setString(16, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(1)));
            ps.setString(17, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(1)));
            ps.executeUpdate();

            // Item 3: Lost - Black Leather Bi-fold Wallet
            ps.setString(1, "LOST");
            ps.setString(2, "Tommy Hilfiger Black Leather Wallet");
            ps.setString(3, "Contains campus ID card STU-2024-9120, metro card, and driver's license. Slipped out of coat pocket.");
            ps.setInt(4, 2); // Wallets
            ps.setString(5, "Main Cafeteria / Food Court");
            ps.setString(6, DateUtil.formatIsoDate(LocalDate.now().minusDays(3)));
            ps.setString(7, null);
            ps.setString(8, "OPEN");
            ps.setInt(9, emmaId);
            ps.setString(10, "+1-555-0102");
            ps.setString(11, "emma.w@campus.edu");
            ps.setDouble(12, 20.0);
            ps.setString(13, "Near Subway counter or booth #6");
            ps.setString(14, null);
            ps.setString(15, null);
            ps.setString(16, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(3)));
            ps.setString(17, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(3)));
            ps.executeUpdate();

            // Item 4: Found - Keychain with 3 keys and Dorm tag
            ps.setString(1, "FOUND");
            ps.setString(2, "Silver Keychain with 3 brass keys & Dorm Tag");
            ps.setString(3, "Found on floor under terminal 18 in Lab Block A. Tag has number 'D-304'.");
            ps.setInt(4, 4); // Keys
            ps.setString(5, "Computer Science Lab (Block A)");
            ps.setString(6, DateUtil.formatIsoDate(LocalDate.now().minusDays(1)));
            ps.setString(7, null);
            ps.setString(8, "OPEN");
            ps.setInt(9, johnId);
            ps.setString(10, "+1-555-0101");
            ps.setString(11, "john.doe@campus.edu");
            ps.setDouble(12, 0.0);
            ps.setString(13, null);
            ps.setString(14, "CS Lab Admin Office room 102");
            ps.setString(15, "What cartoon character keychain ornament is attached?");
            ps.setString(16, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(1)));
            ps.setString(17, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(1)));
            ps.executeUpdate();

            // Item 5: Found - TI-84 Plus CE Graphing Calculator
            ps.setString(1, "FOUND");
            ps.setString(2, "TI-84 Plus CE Color Graphing Calculator");
            ps.setString(3, "Left behind on desk after Calculus 2 lecture. Blue sliding case.");
            ps.setInt(4, 6); // Books & Stationery
            ps.setString(5, "Lecture Hall Complex (LHC)");
            ps.setString(6, DateUtil.formatIsoDate(LocalDate.now().minusDays(4)));
            ps.setString(7, null);
            ps.setString(8, "RETURNED");
            ps.setInt(9, 1); // Admin
            ps.setString(10, "+1-555-0199");
            ps.setString(11, "admin@campus.edu");
            ps.setDouble(12, 0.0);
            ps.setString(13, null);
            ps.setString(14, "Returned to verified owner");
            ps.setString(15, "Name written inside the battery compartment");
            ps.setString(16, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(4)));
            ps.setString(17, DateUtil.formatIsoDateTime(LocalDateTime.now().minusDays(1)));
            ps.executeUpdate();
        }
    }
}
