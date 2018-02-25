package faith.elguadia.onetimedrop;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import faith.elguadia.onetimedrop.config.Config;
import faith.elguadia.onetimedrop.db.databaseHandler;

import java.io.File;

public class start {
    public static HikariDataSource hds;
    private static databaseHandler dh;
    public static void main(String[] args) {
        File f = new File(Config.CONFIG.getDirectory());
        if(!f.exists()) f.mkdir();
        initDatabeConnection();
        dh = new databaseHandler();
        ignite.ignite();
    }

    private static void initDatabeConnection() {
        HikariConfig hc = new HikariConfig();
        hc.setJdbcUrl(Config.CONFIG.getJdbcUrl());
        //hc.addDataSourceProperty();
        hds = new HikariDataSource(hc);
    }
}
