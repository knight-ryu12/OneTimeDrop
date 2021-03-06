package faith.elguadia.onetimedrop.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import faith.elguadia.onetimedrop.start;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

import org.slf4j.Logger;

public class databaseHandler {
    // Maybe rewrite this.
    // Ifrit "Well. I think Chroma is not going to rewrite this."
    // Chroma "What, no, I'll in near future"



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
                            //"filecompmethod VARCHAR(16) NOT NULL," +
                            "created TIMESTAMP" +
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
                    "INSERT INTO onetimedrop (filename,filehash,filelength,filefinger,created) VALUES (?,?,?,?,?)"
            );
            ps.setString(1,filename);
            ps.setString(2,hash);
            ps.setInt(3,filelength);
            ps.setString(4,finger);
            ps.setTimestamp(5,new Timestamp(System.currentTimeMillis()));
            //ps.setDate(5,new java.sql.Date(new Date().getTime()),);
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
                d.put("created",rs.getTimestamp("created").toString());
                //d.put("filecompmethod", rs.getString("filecompmethod"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        logger.info(String.format("Select fileID:%s",fileId));
        return d;
    }

    public List<String> getFileListFromTime(long time) {
        List<String> filelist = new ArrayList<>();
        try(Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "SELECT filefinger FROM onetimedrop WHERE created<=?"
            );
            ps.setTimestamp(1,new Timestamp(time));
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                filelist.add(rs.getString("filefinger"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return filelist;
    }

    public void deleteEntry(String finger) {
        try (Connection con = ds.getConnection()) {
            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM ONLY onetimedrop WHERE filefinger=?"
            );
            ps.setString(1,finger);
            ps.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
