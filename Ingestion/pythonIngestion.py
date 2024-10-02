# Python Script to put csv file into hdfs (db -> .csv (temp file)-> .csv (on hdfs cluster) 
# Since Python doesn't split data (parts) into multiple files like Sqoop’s, it must be handled through script. 
# Once data is split, HDFS will handle the distribution of these files across its blocks and nodes. 
# Below is equivalent Sqoop command:
# sqoop import --connect jdbc:postgresql://ec2-18-132-73-146.eu-west-2.compute.amazonaws.com:5432/testdb 
# --username consultants -password <Password> 
# --table  demp --m 1 
# --target-dir ukussept/deep/demp/
# 
import psycopg2
import subprocess
import os
import csv
import re

# PostgreSQL connection parameters
HOST = "ec2-18-132-73-146.eu-west-2.compute.amazonaws.com"
DATABASE = "testdb"
USER = "consultants"
PASSWORD = "Password"
PORT = "5432"

# HDFS details
HDFS_DIR = "ukussept/deep/demp/"
BATCH_SIZE = 3  # Number of rows to fetch per batch
LOCAL_FILE_PREFIX = "part"

def get_last_hdfs_partition(hdfs_dir):
    """ Get the last partition number from HDFS files """
    try:
        # Run the `hadoop fs -ls` command to list files in the directory
        result = subprocess.run(["hadoop", "fs", "-ls", hdfs_dir], stdout=subprocess.PIPE, stderr=subprocess.PIPE)

        # Check if the command succeeded
        if result.returncode != 0:
            print(f"Error occurred while listing files in HDFS: {result.stderr.decode('utf-8')}")
            return -1  # Return -1 if no files exist or error occurs

        # Extract the file names from the result
        file_list = result.stdout.decode('utf-8').splitlines()

        # Regex to find partitioned file names like "demp_data_part_(\d+)\.csv"
        partition_regex = re.compile(r"part_(\d+)\.csv")

        # Find the highest partition number
        last_part_number = -1
        for file_info in file_list:
            match = partition_regex.search(file_info)
            if match:
                part_number = int(match.group(1))
                last_part_number = max(last_part_number, part_number)

        return last_part_number

    except subprocess.CalledProcessError as e:
        print(f"Error occurred while listing files in HDFS: {e}")
        return -1  # Return -1 if no files exist or error occurs

def fetch_data_from_postgres():
    """ Fetch data from PostgreSQL in batches and upload each batch to HDFS """
    try:
        # Connect to PostgreSQL
        connection = psycopg2.connect(
            host=HOST,
            database=DATABASE,
            user=USER,
            password=PASSWORD,
            port=PORT
        )
        cursor = connection.cursor()

        # Query to fetch the data
        cursor.execute("SELECT * FROM demp")

        # Find the last partition number in HDFS
        last_part_number = get_last_hdfs_partition(HDFS_DIR)
        print(f"Last partition number found in HDFS: {last_part_number}")

        # Increment to start the next partition
        part_number = last_part_number + 1

        while True:
            # Fetch a batch of rows
            rows = cursor.fetchmany(BATCH_SIZE)

            # If no more rows, stop the loop
            if not rows:
                break

            # Create a local file for each batch (part file)
            local_temp_file = f"{LOCAL_FILE_PREFIX}_{part_number}.csv"

            with open(local_temp_file, 'w', newline='') as csvfile:
                csvwriter = csv.writer(csvfile)
                # Write the headers for the first file
                if part_number == last_part_number + 1:
                    csvwriter.writerow([i[0] for i in cursor.description])
                # Write the data rows for the current batch
                csvwriter.writerows(rows)

            print(f"Data for part {part_number} saved to {local_temp_file}")

            # Upload the current part to HDFS
            upload_to_hdfs(local_temp_file, HDFS_DIR)

            # Clean up the local temp file after uploading
            os.remove(local_temp_file)
            print(f"Cleaned up local temp file: {local_temp_file}")

            # Increment the part number
            part_number += 1

    except Exception as e:
        print(f"Error occurred while fetching data: {e}")
    finally:
        # Clean up PostgreSQL connection
        if connection:
            cursor.close()
            connection.close()

def upload_to_hdfs(local_path, hdfs_dir):
    """ Upload the local CSV file to HDFS """
    try:
        # Create target directory in HDFS if it does not exist
        subprocess.run(["hadoop", "fs", "-mkdir", "-p", hdfs_dir], check=True)

        # Use hadoop fs -put to upload file to HDFS
        subprocess.run(["hadoop", "fs", "-put", local_path, hdfs_dir], check=True)
        print(f"Successfully uploaded {local_path} to HDFS directory {hdfs_dir}")

    except subprocess.CalledProcessError as e:
        print(f"Error occurred while uploading to HDFS: {e}")

if __name__ == "__main__":
    # Fetch data from PostgreSQL and upload it in parts to HDFS
    fetch_data_from_postgres()
