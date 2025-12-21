<?php
$folderName = basename(__DIR__);

$imageDir = 'images/';
$images = [];

if (is_dir($imageDir)) {
    $files = scandir($imageDir);
    foreach ($files as $file) {
        if (preg_match('/\.(jpg|jpeg|png|gif)$/i', $file)) {
            $images[] = $imageDir . $file;
        }
    }
}
?>

<?php
$host = 'db';
$user = 'root';
$pass = 'somerootpassword';
$db   = 'photo_db';

$conn = new mysqli($host, $user, $pass, $db);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

$sql = "SELECT filename FROM photos";
$result = $conn->query($sql);

while($row = $result->fetch_assoc()) {
    echo "<img src='images/" . $row['filename'] . "' width='200' /><br>";
}
?>

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

    <div class="gallery">
        <?php if (empty($images)): ?>
            <p>No images found in the /images folder.</p>
        <?php else: ?>
            <?php foreach ($images as $image): ?>
                <img src="<?php echo $image; ?>" alt="Photo">
            <?php endforeach; ?>
        <?php endif; ?>
    </div>

</body>
</html>