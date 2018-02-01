package faith.elguadia.onetimedrop.compress;

import faith.elguadia.onetimedrop.ignite;
import net.jpountz.lz4.LZ4Factory;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class selectCompress {
    private static final Logger logger = LoggerFactory.getLogger(selectCompress.class);
    private static final LZ4Factory lz4;
    static {
        lz4 = LZ4Factory.fastestInstance();
    }
    public static void compressLZ4(File source, File dest) throws IOException {
        byte[] sourceByte;
        byte[] destByte;
        Path sourceP = source.toPath();
        sourceByte = Files.readAllBytes(sourceP);
    }
}
