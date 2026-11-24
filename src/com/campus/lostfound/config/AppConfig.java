package com.campus.lostfound.config;

import java.io.File;

/**
 * Application-wide configuration and file paths.
 */
public final class AppConfig {
    private AppConfig() {}

    public static final String APP_NAME = "Smart Campus Lost & Found Management System";
    public static final String APP_VERSION = "1.0.0";
    
    // Directory paths
    public static final String BASE_DIR = System.getProperty("user.dir");
    public static final String DATA_DIR = BASE_DIR + File.separator + "data";
    public static final String IMAGES_DIR = DATA_DIR + File.separator + "images";
    public static final String DB_PATH = DATA_DIR + File.separator + "campus_lost_found.db";
    public static final String JDBC_URL = "jdbc:sqlite:" + DB_PATH;

    // Default admin credentials (created on initial DB bootstrap if not present)
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "admin123";
    public static final String DEFAULT_ADMIN_EMAIL = "admin@campus.edu";
    public static final String DEFAULT_ADMIN_NAME = "Campus Security Administrator";

    // Campus standard locations
    public static final String[] CAMPUS_LOCATIONS = {
        "All Locations",
        "Central Library",
        "Computer Science Lab (Block A)",
        "Engineering Complex (Block B)",
        "Science & Research Hall",
        "Student Activity Center (SAC)",
        "Main Cafeteria / Food Court",
        "Sports Ground / Gymnasium",
        "Lecture Hall Complex (LHC)",
        "Administration Building",
        "Campus Gate / Security Post",
        "Hostel / Dormitory Area",
        "Bus Stop / Parking Area",
        "Other / Outdoor Campus"
    };

    public static final String[] EDITABLE_CAMPUS_LOCATIONS = {
        "Central Library",
        "Computer Science Lab (Block A)",
        "Engineering Complex (Block B)",
        "Science & Research Hall",
        "Student Activity Center (SAC)",
        "Main Cafeteria / Food Court",
        "Sports Ground / Gymnasium",
        "Lecture Hall Complex (LHC)",
        "Administration Building",
        "Campus Gate / Security Post",
        "Hostel / Dormitory Area",
        "Bus Stop / Parking Area",
        "Other / Outdoor Campus"
    };
}
