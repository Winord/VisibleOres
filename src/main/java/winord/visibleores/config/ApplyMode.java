package winord.visibleores.config;

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
