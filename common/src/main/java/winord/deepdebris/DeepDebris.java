package winord.deepdebris;

import dev.architectury.platform.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class DeepDebris {
    public static final String MOD_ID = "deepdebris";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final Path CONFIG_DIR = Platform.getConfigFolder().resolve(MOD_ID);

    private DeepDebris() { }

    public static void init() {
        LOGGER.info("Initialized DeepDebris!");
    }
}
