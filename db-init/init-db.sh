#!/bin/bash
# init-db.sh

mysql -u root -p"$MYSQL_ROOT_PASSWORD" photo_db -e "CREATE TABLE IF NOT EXISTS photos (id INT AUTO_INCREMENT PRIMARY KEY, filename VARCHAR(255) NOT NULL UNIQUE);"

cd /var/www/html/images
for file in *.{jpg,jpeg,png,gif,webp}; do
    if [ -f "$file" ]; then
        mysql -u root -p"$MYSQL_ROOT_PASSWORD" photo_db -e "INSERT IGNORE INTO photos (filename) VALUES ('$file');"
        echo "Successfully added: $file"
    fi
done