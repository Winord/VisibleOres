package winord.visibleores.render;

/** Added to block entity render states by a mixin: whether the block is waxed copper that should look waxed. */
public interface WaxedRenderState {
    boolean visibleores$waxed();

    void visibleores$setWaxed(boolean waxed);
}
