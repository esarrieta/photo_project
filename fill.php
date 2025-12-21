<?php
$conn = new mysqli('db', 'root', 'somerootpassword', 'photo_db');

// 1. Get all image files from the folder
$images = glob("images/*.{jpg,jpeg,png,gif,webp}", GLOB_BRACE);

foreach ($images as $path) {
    // Get just the filename (e.g., "vacation.jpg") from the full path
    $filename = basename($path);

    // 2. Prepare the SQL to prevent duplicates
    $stmt = $conn->prepare("INSERT IGNORE INTO photos (filename) VALUES (?)");
    $stmt->bind_param("s", $filename);
    
    if ($stmt->execute()) {
        echo "Added: $filename <br>";
    }
}
?>