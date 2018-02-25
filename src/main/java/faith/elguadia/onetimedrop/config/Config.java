package faith.elguadia.onetimedrop.config;

import com.google.common.base.CharMatcher;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.YAMLException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

public class Config {

    private String JdbcUrl;
    private String directory;
    public static final Config CONFIG;

    static {
        Config c;
        try {
            c = new Config(
                    loadConfigFile("credentials"),
                    loadConfigFile("config")
            );
        } catch (final IOException e) {
            c = null;
            //log.error("Could not load config files!", e);
        }
        CONFIG = c;
    }



    public Config(File cred, File configFile) {
        try {
            Yaml yaml = new Yaml();
            String credFileStr = FileUtils.readFileToString(cred,"UTF-8");
            String configFileStr = FileUtils.readFileToString(configFile,"UTF-8");

            credFileStr = cleanTabs(credFileStr,"credentials.yaml");
            configFileStr = cleanTabs(configFileStr,"config.yaml");

            Map<String, Object> creds = yaml.load(credFileStr);
            Map<String, Object> config = yaml.load(configFileStr);

            creds.keySet().forEach((String key) -> creds.putIfAbsent(key, ""));
            config.keySet().forEach((String key) -> config.putIfAbsent(key, ""));

            JdbcUrl = (String) creds.getOrDefault("JdbcUrl","");
            directory = (String) config.getOrDefault("directory","");
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config file.",e);
        } catch (YAMLException | ClassCastException e) {
            throw e;
        }
    }

    private static File loadConfigFile(String name) throws IOException {
        String path = "./" + name + ".yaml";
        File file = new File(path);
        if (!file.exists() || file.isDirectory()) {
            throw new FileNotFoundException("Could not find '" + path + "' file.");
        }
        return file;
    }
    private static String cleanTabs(String content, String file) {
        CharMatcher tab = CharMatcher.is('\t');
        if (tab.matchesAnyOf(content)) {
            //log.warn("{} contains tab characters! Trying a fix-up.", file);
            return tab.replaceFrom(content, "  ");
        } else {
            return content;
        }
    }

    public String getJdbcUrl() {
        return JdbcUrl;
    }
    public String getDirectory() {
        return directory;
    }
}
