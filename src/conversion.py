import os
import time
import mysql.connector
from mysql.connector import Error
from PIL import Image
from pillow_heif import register_heif_opener


register_heif_opener()

def connect_to_database():
    while True:
        try: 
            connection = mysql.connector.connect(
                host='db',
                database ='photo_db',
                user='root',
                password='somerootpassword'
            )
            if connection.is_connected():
                return connection
        except Error as e:
            print(f"Error connecting to the database: {e}")
            time.sleep(5)

def run_conversion(folder_path):
    conn = connect_to_database()
    if conn is None:
        return
    

    try:
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT id, filename FROM photos WHERE filename LIKE '%.heic' OR filename LIKE '%.HEIC'")
        rows = cursor.fetchall()

        for row in rows:
            filename = row["filename"]
            photo_id = row["id"]

            heic_path = os.path.join(folder_path, filename)
            new_filename = os.path.splitext(filename)[0] + ".jpg"
            jpg_path = os.path.join(folder_path, new_filename)

            if os.path.exists(heic_path):

                try:
                    image = Image.open(heic_path)
                    image.save(jpg_path, "JPEG", quality=90)

                    sql = "UPDATE photos SET filename = %s WHERE id = %s"
                    cursor.execute(sql, (new_filename, photo_id))
                    conn.commit()

                    os.remove(heic_path)
                
                except Exception as e:
                    print(f"Error converting {filename}: {e}")
    except Error as e:
        print(f"Error executing query: {e}")

    finally:
        if conn.is_connected():
            cursor.close()
            conn.close()
        
if __name__ == "__main__":
    while True:
        dir = "/app/images"
        run_conversion(dir)
        time.sleep(600)

