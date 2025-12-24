<!DOCTYPE html>
<html>
<head>
    <title>Photo Gallery</title>
    <style>
        body { font-family: sans-serif; text-align: center; background: #f4f4f4; }
        .gallery { display: flex; flex-wrap: wrap; justify-content: center; gap: 20px; padding: 20px; }
        img { width: 300px; height: 200px; object-fit: cover; border-radius: 8px; box-shadow: 0 4px 8px rgba(0,0,0,0.1); }
    </style>
</head>
<body>

    <h1>Photos</h1>
    <p>
        <a href="images/" target="_blank" style="font-size: 1.2em; color: #007bff;">
            📂 View images
        </a>
    </p>

</body>
</html>


<?php
// Get database configuration from environment variables
$host = getenv('DB_HOST') ?: 'db';
$user = getenv('MYSQL_USER') ?: 'photo_app';
$pass = getenv('MYSQL_PASSWORD') ?: 'photo_app_secure_password_456';
$db   = getenv('MYSQL_DATABASE') ?: 'photo_db';

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    // Don't expose connection details in production
    error_log("Database connection failed: " . $conn->connect_error);
    echo "<p style='color: red;'>Unable to connect to database. Please try again later.</p>";
    exit;
}

$sql = "SELECT filename FROM photos";
$result = $conn->query($sql);

if ($result) {
    echo "<div class='gallery'>";
    while($row = $result->fetch_assoc()) {
        // Escape output to prevent XSS
        $filename = htmlspecialchars($row['filename'], ENT_QUOTES, 'UTF-8');
        echo "<img src='images/" . $filename . "' alt='Photo' />";
    }
    echo "</div>";
} else {
    error_log("Query failed: " . $conn->error);
    echo "<p>Unable to load photos.</p>";
}

$conn->close();
?>

