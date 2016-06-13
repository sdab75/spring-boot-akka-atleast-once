package com.ms;
import com.datastax.driver.core.*;
import com.datastax.driver.core.utils.Bytes;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.hdfs.util.ByteArray;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * Created by davenkat on 6/7/2016.
 */
public class AkkaLogAnalyzer {
    public static void main(String[] args) throws UnsupportedEncodingException {

        Cluster cluster;
        Session session;

        // Connect to the cluster and keyspace "demo"
        cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        session = cluster.connect("akka");
        ResultSet results = null;
/*
        // Insert one record into the users table
        session.execute("INSERT INTO users (lastname, age, city, email, firstname) VALUES ('Jones', 35, 'Austin', 'bob@example.com', 'Bob')");

        // Use select to get the user we just entered
        ResultSet results = session.execute("SELECT * FROM users WHERE lastname='Jones'");
        for (Row row : results) {
            System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
        }

        // Update the same user with a new age
        session.execute("update users set age = 36 where lastname = 'Jones'");

        // Select and show the change
        results = session.execute("select * from users where lastname='Jones'");
        for (Row row : results) {
            System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
        }

        // Delete the user from the users table
        session.execute("DELETE FROM users WHERE lastname = 'Jones'");
*/

        File f= new File("c:/out.xls");

        try {
            FileUtils.forceDelete(f);

            // Show that the user is gone
            ProtocolVersion protocolVersion=cluster.getConfiguration().getProtocolOptions().getProtocolVersionEnum();

            results = session.execute("SELECT * FROM messages");
            for (Row row : results) {
                String s=row.getString("persistence_id")+","+row.getLong("partition_nr")+","+row.getLong("sequence_nr")+","+(String)DataType.text().deserialize(row.getBytes("message"), protocolVersion);
                FileUtils.write(f,s,true);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            // Clean up the connection by closing it
            cluster.close();

        }
    }
}
