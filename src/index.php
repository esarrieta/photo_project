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

<body>
    <input type="text" id="photoFilename" placeholder="Enter Photo filename">
    <button onclick="handleInput()">Get Photo</button>
    <p id="display"></p>

    <script>
        function handleInput() {
            const photoFile = document.getElementById("photoFilename").value;

            if(!photoFile) {
                alert("Please enter a valid Photo filename.");
                return;
            }

            const getPhotoUrl = "http://localhost:8081/photos/download/file/" + photoFile;
            fetch(getPhotoUrl)
                .then(response => {
                    if(response.ok) {
                        return response.blob();
                    } else {
                        throw new Error("Photo not found");
                    }
                })
                .then(blob => {
                    const url = URL.createObjectURL(blob);
                    const display = document.getElementById("display");
                    display.innerHTML = `<img src="${url}" alt="Photo" style="max-width: 500px; max-height: 500px;" />`;
                })
                .catch(error => {
                    console.error("Error fetching photo:", error);
                    alert("An error occurred while fetching the photo: " + error.message);
                });
        }
    </script>
</body>

<body>
    <input type="text" id="photoInput" placeholder="Enter Photo to Delete">
    <button onclick="deleteInput()">Delete Photo</button>
    <p id="display"></p>

    <script>
        function deleteInput() {
            const photoInput = document.getElementById("photoInput").value;
            if(!confirm("Are you sure you want to delete this photo?")) {
                return;
            }

            if(!isNaN(photoInput)) {
                const photoId = parseInt(photoInput);

                const deleteUrl = "http://localhost:8081/photos/id/" + photoId;
                fetch(deleteUrl, {
                    method: "DELETE"
                })
                .then(response => {
                    if(response.ok) {
                        return response.text().then(text => {
                            alert(text || "Photo deleted successfully.");
                            window.location.reload();
                        });
                    } else {
                        return response.text().then(text => {
                            alert("Failed to delete photo: " + (text || "Unknown error"));
                        });
                    }
                })
                .catch(error => {
                    console.error("Error deleting photo:", error);
                    alert("An error occurred while deleting the photo: " + error.message);
                });
            } else if (/\.(jpg|jpeg|png|gif|webp|heic|JPG|JPEG|PNG|GIF|WEBP|HEIC)$/.test(photoInput)) {
                const photoFilename = photoInput;
                const deleteUrl = "http://localhost:8081/photos/file/" + encodeURIComponent(photoFilename);
                fetch(deleteUrl, {
                    method: "DELETE"
                }) 
                .then(response => {
                    if(response.ok) {
                        return response.text().then(text => {
                            alert(text || "Photo deleted successfully.");
                            window.location.reload();
                        });
                    } else {
                        return response.text().then(text => {
                            alert("Failed to delete photo: " + (text || "Unknown error"));
                        });
                    }
                })
                .catch(error => {
                    console.error("Error deleting photo:", error);
                    alert("An error occurred while deleting the photo: " + error.message);
                });
            } else {
                alert("Please enter a valid Photo ID or filename with an image extension.");
                return;
            }
        }
    </script>
</body>

<body>
    <h2>Upload Photo</h2>
    <input type="file" id="photoUpload" accept="image/*" onchange="previewPhotoAndUpload()">
    <div id="preview" style="margin: 15px 0;"></div>
    <button id="uploadButton" onclick="uploadPhoto()">Upload Photo</button>
        <script>
            let selectedFile = null;

            function previewPhotoAndUpload(){
                const fileInput = document.getElementById("photoUpload");
                const previewDiv = document.getElementById("preview");
                selectedFile = fileInput.files[0];

                if(selectedFile){
                    const reader = new FileReader();
                    reader.onload = function(e){
                        previewDiv.innerHTML = `<img src="${e.target.result}" alt="Photo Preview" style="max-width: 300px; max-height: 300px;"/>`;
                        document.getElementById("uploadButton").style.display = "inline-block";
                    }
                    reader.readAsDataURL(selectedFile);
                } else {
                    previewDiv.innerHTML = "";
                    document.getElementById("uploadButton").style.display = "none";
                }
            }
            function uploadPhoto(){
                if(!selectedFile){
                    alert("No photo selected for upload.");
                    return;
                }

                const formData = new FormData();
                formData.append("file", selectedFile);

                fetch("http://localhost:8081/photos/upload", {
                    method: "POST",
                    body: formData
                })
                .then(response => {
                    if(response.ok){
                        return response.text().then(text => {
                            alert(text || "Photo uploaded successfully.");
                            window.location.reload();
                        });
                    } else {
                        return response.text().then(text => {
                            alert("Failed to upload photo: " + (text || "Unknown error"));
                        });
                    }
                })
                .catch(error => {
                    console.error("Error uploading photo:", error);
                    alert("An error occurred while uploading the photo: " + error.message);
                });
            }

        </script>
</body>


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

$sql = "SELECT id, filename FROM photos";
$result = $conn->query($sql);

if ($result) {
    echo "<div class='gallery'>";
    while($row = $result->fetch_assoc()) {
        // Escape output to prevent XSS
        $id = htmlspecialchars($row['id'], ENT_QUOTES, 'UTF-8');
        $filename = htmlspecialchars($row['filename'], ENT_QUOTES, 'UTF-8');
        echo "<div style= 'text-align: center; margin-bottom: 10px;'>";
        echo "<img src='images/" . $filename . "' alt='Photo' />";
        echo "<p style ='margin: 5px 0; font-size: 0.9em;'><strong>ID:</strong> " . $id . "<br><strong>Filename:</strong> " . $filename . "</p>";
        echo "</div>";
    }
    echo "</div>";
} else {
    error_log("Query failed: " . $conn->error);
    echo "<p>Unable to load photos.</p>";
}

$conn->close();
?>

