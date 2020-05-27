package com.davidm1a2.insomiameter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * GUI object that shows a player's insomnia level
 */
@OnlyIn(Dist.CLIENT)
public class InsomniaGui extends AbstractGui
{
    // The insomnia bar texture
    private static final ResourceLocation BAR_TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/bars.png");
    // How long it takes for phantoms to spawn
    private static final int TIME_WHEN_PHANTOMS_CAN_SPAWN = 72000;
    // A reference to the MC instance
    private final Minecraft minecraft = Minecraft.getInstance();
    // The number of ticks since the server gave us an update
    private int timeSinceLastServerUpdate = 0;

    /**
     * Asks the server to send us the current player's stats
     */
    public void requestStats()
    {
        final ClientPlayNetHandler connection = this.minecraft.getConnection();
        if (connection != null)
        {
            connection.sendPacket(new CClientStatusPacket(CClientStatusPacket.State.REQUEST_STATS));
            timeSinceLastServerUpdate = 0;
        }
    }

    /**
     * Increment the time since the last server update on each client side tick
     */
    public void clientTick()
    {
        timeSinceLastServerUpdate++;
    }

    /**
     * Called every frame to render the GUI
     */
    public void render()
    {
        final ClientPlayerEntity player = minecraft.player;

        // Only render the GUI if the player is in survival
        if (player != null && !player.isCreative() && !player.isSpectator())
        {
            RenderSystem.pushMatrix();

            // Enable transparency
            RenderSystem.enableBlend();
            // Compute the x and y of the texture
            final int width = this.minecraft.getMainWindow().getScaledWidth();
            final int height = this.minecraft.getMainWindow().getScaledHeight();
            final int x = width / 2 - 98;
            final int y = height - 38;
            // Compute the ticks since the last rest, use the server's sent value + the number of ticks that have passed on the client
            final int ticksSinceLastRest = player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) + timeSinceLastServerUpdate;
            // Compute the percent until we can see phantoms
            final float percent = computePercent(ticksSinceLastRest);
            // Use a white color
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
            // Bind the background texture
            minecraft.textureManager.bindTexture(BAR_TEXTURE);
            // Draw the background texture
            blit(x, y, 5, 0, 5, 37, 64, 64);
            // Draw the foreground texture
            if (percent == 1.0)
            {
                blit(x, y, 10, 0, 5, 37, 64, 64);
            }
            else
            {
                blit(x, y - Math.round(percent * 37) + 37, 0, 37 - Math.round(percent * 37), 5, Math.round(percent * 37), 64, 64);
            }
            RenderSystem.disableBlend();
            RenderSystem.popMatrix();
        }
    }

    /**
     * Simple function to turn the number of ticks since last rest into an alpha value
     *
     * @param timeSinceLastRest The number of ticks since the last time we slept
     * @return A value from 0 (invisible) to 1 (visible)
     */
    private float computePercent(int timeSinceLastRest)
    {
        return MathHelper.clamp((float) timeSinceLastRest / TIME_WHEN_PHANTOMS_CAN_SPAWN, 0.0f, 1.0f);
    }
}
