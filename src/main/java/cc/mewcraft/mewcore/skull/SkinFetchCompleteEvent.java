package cc.mewcraft.mewcore.skull;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class SkinFetchCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();

    @Override public @NotNull HandlerList getHandlers() {
        return handlers;
    }

    public static @NotNull HandlerList getHandlerList() {
        return handlers;
    }
}
