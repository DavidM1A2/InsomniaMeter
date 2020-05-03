package com.davidm1a2.insomiameter;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerWakeUpEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

/**
 * Main mod file that sets up the InsomniaGui object and handles events
 */
@Mod(Constants.MOD_ID)
public class InsomniaMeter
{
    /**
     * GUI rendering class instance. Only create this on the client since the server can't see this class
     */
    @OnlyIn(Dist.CLIENT)
    private final InsomniaGui insomniaGui = new InsomniaGui();

    /**
     * Constructor registers this object to receive forge events
     */
    public InsomniaMeter()
    {
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Gets called every frame to render the chat. We render our HUD element here too
     *
     * @param event ignored
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void drawOverlay(final RenderGameOverlayEvent.Chat event)
    {
        insomniaGui.render();
    }

    /**
     * Called when we connect to the server, we have the client request the server's current stats
     *
     * @param event ignored
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void clientConnectedToServerEvent(final ClientPlayerNetworkEvent.LoggedInEvent event)
    {
        insomniaGui.requestStats();
    }

    /**
     * Called when we sleep and wake up. Have the server update us on the player's new stats
     *
     * @param event ignored
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onPlayerWakeUpEvent(final PlayerWakeUpEvent event)
    {
        insomniaGui.requestStats();
    }

    /**
     * When the a tick happens we pass that on to our gui
     *
     * @param event The tick details
     */
    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onClientTickEvent(final TickEvent.ClientTickEvent event)
    {
        // Ensure we are at the end of a tick, on the client side, and the tick is for the client
        if (event.phase == TickEvent.Phase.END && event.side == LogicalSide.CLIENT && event.type == TickEvent.Type.CLIENT)
        {
            insomniaGui.clientTick();
        }
    }
}
