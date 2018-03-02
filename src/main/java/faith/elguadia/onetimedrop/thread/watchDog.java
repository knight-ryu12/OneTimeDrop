package faith.elguadia.onetimedrop.thread;

import faith.elguadia.onetimedrop.config.Config;
import faith.elguadia.onetimedrop.db.databaseHandler;
import faith.elguadia.onetimedrop.start;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class watchDog implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(watchDog.class);
    private final databaseHandler dh = new databaseHandler();
    @Override
    public void run() {
        long time = System.currentTimeMillis();
        time -= Config.CONFIG.getTime()*1000;
        List<String> l = dh.getFileListFromTime(time);
        if(!l.isEmpty()) {
            logger.info(String.valueOf(l));
            for (String s : l) {
                logger.info("Deleting file:" + s);
                dh.deleteEntry(s);
                boolean t = new File(Config.CONFIG.getDirectory() + s).delete();
                logger.info("Deleted, ret:" + t);
            }
        }
    }
}
