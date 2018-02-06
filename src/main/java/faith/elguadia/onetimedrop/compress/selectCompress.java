package faith.elguadia.onetimedrop.compress;

import faith.elguadia.onetimedrop.ignite;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.anarres.lzo.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

    enum FILE_COMPRESSION {
        LZO,LZ4;
    }

    public static byte[] compressLZ4(byte[] sourceByte) {
        //byte[] sourceByte;
        byte[] dest;
        //Path sourceP = source.toPath();
        //sourceByte = Files.readAllBytes(sourceP);
        //logger.info(String.format("Step-1> Loaded all Bytes. Filename:%s",sourceP.getFileName()));
        LZ4Compressor lc = lz4.fastCompressor();
        final int l = sourceByte.length;
        logger.info(String.format("Step-2> InputLen:%d",l));
        int mcl = lc.maxCompressedLength(l);
        dest = new byte[mcl];
        logger.info(String.format("Compress Step-3> destByteLen:%d maxCompressedLen:%d",dest.length,mcl));
        int cl = lc.compress(sourceByte,0,l,dest,0,mcl);
        logger.info(String.format("Compressed> newByteArrayLen:%d newLen:%d",dest.length,cl));
        return dest;
    }

    public static byte[] compressLZ4(File source) throws IOException {
        Path sourceP = source.toPath();
        return compressLZ4(Files.readAllBytes(sourceP));
    }

    public static byte[] decompressLZ4(File source,int decomplen) throws IOException {
        byte[] sourceByte;
        byte[] dest;
        Path sourceP = source.toPath();
        sourceByte = Files.readAllBytes(sourceP);
        logger.info("Decompress Step-1> Loaded all bytes.");
        // Since we know original decompress byte, we'll use it to determine length of Byte Array
        LZ4FastDecompressor decomp = lz4.fastDecompressor();
        dest = new byte[decomplen]; // Trusting decompressed Length
        logger.info(String.format("Decompress Step-2> decompressedLen:%d destArrayLen:%d",decomplen,dest.length));
        int compLen = decomp.decompress(sourceByte,0,dest,0,decomplen);
        logger.info(String.format("Decompressed> compressedLen:%d",compLen));
        return dest;
    }

    public static byte[] compressLZO(byte[] in) {
        byte[] compressed = null;
        LzoAlgorithm algo = LzoAlgorithm.LZO1X;
        LzoCompressor compressor = LzoLibrary.getInstance().newCompressor(algo, null);
        try (ByteArrayInputStream bais = new ByteArrayInputStream(in);ByteArrayOutputStream baos = new ByteArrayOutputStream();LzoOutputStream lzos = new LzoOutputStream(baos,compressor,32768)) {
                IOUtils.copy(bais,lzos,32768);
                compressed = baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compressed;
    }


}
