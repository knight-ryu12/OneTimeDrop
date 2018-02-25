package faith.elguadia.onetimedrop;


import faith.elguadia.onetimedrop.compress.selectCompress;
import faith.elguadia.onetimedrop.config.Config;
import faith.elguadia.onetimedrop.db.databaseHandler;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static spark.Spark.*;

public class ignite {
    private static final Logger logger = LoggerFactory.getLogger(ignite.class);
    private static final databaseHandler dh = new databaseHandler();
    private static MessageDigest md;

    private static LZ4Factory lz4 = LZ4Factory.fastestInstance();

    static {
        try {
            logger.info("LZ4 Compressor Initialized. Return:"+lz4.toString());
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static void ignite() {
        port(8080);

        path("/api",() -> {
            get("/status",(request,response) -> {
                response.status(200);
                response.header("Server","OneTimeDrop V0.0.1");
                return "OK! I am ready.";
            });
            post("/upload",(request, response) -> {
                request.attribute("org.eclipse.jetty.multipartConfig",new MultipartConfigElement("/tmp"));
                Part filePart = request.raw().getPart("file");

                if(filePart == null) {
                    response.status(400);
                    response.type("application/json");
                    response.header("Pragma","no-cache");
                    return "{\"ok\":\"false\",\"errorcode\":1001}\n"; // 100-1: Is Upload Schema right? Usage "curl -X POST <URL> -F filename=@<filepath>
                }

                String filename = filePart.getSubmittedFileName();
                File f = new File(Config.CONFIG.getDirectory()+filename);
                Path ft = f.toPath();

                logger.info(String.format("Filesize:%d",filePart.getSize()));

                byte[] data = new byte[(int) filePart.getSize()];
                logger.info(String.format("FileSizeArray:%d",data.length));
                IOUtils.readFully(filePart.getInputStream(),data); //Copy.
                byte[] compressed = selectCompress.compressLZ4(data);
                //filePart.getInputStream().close(); // No need.
                /*logger.info(String.format("FileSizeArray:%d",data.length));
                LZ4Compressor lz4Compressor = lz4.fastCompressor();
                int maxCompressedLength = lz4Compressor.maxCompressedLength(data.length);
                logger.info(String.format("Try to compress> Length:%d maxCompressedLength:%d",data.length,maxCompressedLength));
                byte[] compressed = new byte[maxCompressedLength];
                int compressedLength = lz4Compressor.compress(data,0,data.length,compressed,0,maxCompressedLength);
                logger.info(String.format("Compressed> newByteArrayLen:%d newLen:%d",compressed.length,compressedLength));
                */
                // I think I am not a real person, rather than work-alone mind AI.
                //try(ByteArrayInputStream bais = new ByteArrayInputStream(data); FileOutputStream fos = new FileOutputStream(f))


                try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed);FileOutputStream fos = new FileOutputStream(f)) {
                    IOUtils.copy(bais,fos,32768);
                }

                logger.info("Copied.");
                String finger = dh.createEntry(filename, Hex.encodeHexString(md.digest(data)), (int) filePart.getSize());
                String p = FilenameUtils.getExtension(filename);
                md.reset();
                Files.move(ft,ft.resolveSibling(finger));
                response.type("application/json");
                response.header("Pragma","no-cache");
                return "{\"ok\":\"true\",\"errorcode\":0000,g:\""+finger+"\"}\n";
            });
            get("/dl/:id",(request,response) -> {
                request.attribute("org.eclipse.jetty.multipartConfig",new MultipartConfigElement("/tmp"));
                String s = request.params("id");
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                Map<String,String> m = dh.getFileFromDatabase(s);
                if(m == null) {
                    response.status(400);
                    response.type("application/json");
                    response.header("Pragma","no-cache");
                    return "{\"ok\":\"false\",\"errorcode\":1003}\n"; //Critical Error (100-3: dh returned wrong info. maybe DB error?)
                }
                String fn = m.getOrDefault("filename","");
                if(fn.equals("")) {
                    response.status(404);
                    response.type("application/json");
                    response.header("Pragma","no-cache");
                    return "{\"ok\":\"false\",\"errorcode\":3000}\n"; //Input Error (300-0: filename is not returned (Maybe no entry?))
                }
                File f = new File(Config.CONFIG.getDirectory()+s);
                Path p = f.toPath();
                if(!f.exists()) {
                    response.status(404);
                    response.type("application/json");
                    response.header("Pragma","no-cache");
                    return "{\"ok\":\"false\",\"errorcode\":3001}\n"; // (300-1: File is not exist (Deleted?))
                }
                /*try(FileInputStream fis = new FileInputStream(f)){
                    IOUtils.copy(fis,response.raw().getWriter(), Charset.defaultCharset());
                }*/ // seems it doesn't support and yeah
                // Decompress
                int decompLength = Integer.parseInt(m.getOrDefault("filelength",""));
                byte[] restored = selectCompress.decompressLZ4(f,decompLength);
                /*

                LZ4FastDecompressor decompressor = lz4.fastDecompressor();
                byte[] restored = new byte[decompLength];
                int compressedLength = decompressor.decompress(data,0,restored,0,decompLength);
                logger.info(String.format("CompressedLength:%d",compressedLength));
                //Decompressed.
                */

                //md.digest(data);
                String targethash = m.getOrDefault("filehash","");
                String hash = Hex.encodeHexString(md.digest(restored));
                if(!hash.equals(targethash)) {
                    response.status(400);
                    response.type("application/json");
                    response.header("Pragma","no-cache");
                    return "{\"ok\":\"false\",\"errorcode\":1005}\n"; //Critical Error (100-5: Hash Mismatch)
                }
                response.type("application/octet-stream");
                response.header("Content-Length",m.getOrDefault("filelength",""));
                response.header("Content-Disposition","attachment; filename=\""+fn+"\"");
                try {
                    assert restored != null;
                    response.raw().getOutputStream().write(restored);
                } catch (Exception e) {
                    return null;

                } finally {
                    response.raw().getOutputStream().flush();
                    response.raw().getOutputStream().close();
                }
                return response.raw();
            });
        });
    }
}
