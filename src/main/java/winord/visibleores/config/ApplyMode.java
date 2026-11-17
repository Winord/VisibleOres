package winord.visibleores.config;

/** Where the replacement texture is shown: on placed blocks, on items, or both. */
public enum ApplyMode {
    BLOCK,
    ITEM,
    BOTH;

    public boolean blocks() {
        return this != ITEM;
    }

    public boolean items() {
        return this != BLOCK;
    }
}
