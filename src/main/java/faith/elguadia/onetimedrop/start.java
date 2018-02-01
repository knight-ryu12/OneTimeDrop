package faith.elguadia.onetimedrop;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import faith.elguadia.onetimedrop.config.Config;
import faith.elguadia.onetimedrop.db.databaseHandler;

public class start {
    public static HikariDataSource hds;
    private static databaseHandler dh;
    public static void main(String[] args) {
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
