package winord.deepdebris.config;

import com.google.gson.annotations.SerializedName;

public enum TextureSource {
    @SerializedName(value = "DEFAULT", alternate = "MOD")
    DEFAULT,
    @SerializedName(value = "CUSTOM", alternate = "RESOURCE_PACK")
    CUSTOM
}
