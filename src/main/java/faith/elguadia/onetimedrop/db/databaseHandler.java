package faith.elguadia.onetimedrop.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import faith.elguadia.onetimedrop.start;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;

public class databaseHandler {
    private final HikariDataSource ds;
    private static final Logger logger = LoggerFactory.getLogger(databaseHandler.class);
    public databaseHandler() {
        if(start.hds == null) {
            throw new RuntimeException("SQL isn't connected properly - this shouldn't happen.");
        }
        ds = start.hds;
        createTable();
    }

    private void createTable() {
        try(Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS onetimedrop (" +
                            "id SERIAL PRIMARY KEY," +
                            "filename VARCHAR(255) NOT NULL," +
                            "filehash VARCHAR(255) NOT NULL," +
                            "filelength INTEGER NOT NULL," +
                            "filefinger VARCHAR(16) NOT NULL," +
                            "created TIMESTAMP," +
                            "modified TIMESTAMP" +
                            ")"
            );
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String createEntry(String filename,String hash,int filelength) {
        String finger = RandomStringUtils.random(16,0,0,true,true,null, ThreadLocalRandom.current());
        try(Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO onetimedrop (filename,filehash,filelength,filefinger) VALUES (?,?,?,?)"
            );
            ps.setString(1,filename);
            ps.setString(2,hash);
            ps.setInt(3,filelength);
            ps.setString(4,finger);

            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info(String.format("Insert fileID:%s",finger));
        return finger;
    }

    public Map<String,String> getFileFromDatabase(String fileId) {
        //String s;
        Map<String,String> d = new HashMap<>();
        try(Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM onetimedrop WHERE filefinger=?"
            );
            ps.setString(1,fileId);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                d.put("filename", rs.getString("filename"));
                d.put("filehash", rs.getString("filehash"));
                d.put("filelength", rs.getString("filelength"));
                d.put("filefinger", rs.getString("filefinger"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info(String.format("Select fileID:%s",fileId));
        return d;
    }
}
