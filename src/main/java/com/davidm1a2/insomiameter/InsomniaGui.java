package com.davidm1a2.insomiameter;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.network.play.client.CClientStatusPacket;
import net.minecraft.stats.Stats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

/**
 * GUI object that shows a player's insomnia level
 */
@OnlyIn(Dist.CLIENT)
public class InsomniaGui extends AbstractGui
{
    // The insomnia background texture
    private static final ResourceLocation TEXTURE = new ResourceLocation(Constants.MOD_ID, "textures/gui/insomnia_texture.png");
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
            // Bind the background texture
            minecraft.textureManager.bindTexture(TEXTURE);
            // Compute the x and y of the texture
            final int width = this.minecraft.getMainWindow().getScaledWidth();
            final int height = this.minecraft.getMainWindow().getScaledHeight();
            final int x = width / 2 - 8;
            final int y = height - 54;
            // Compute the ticks since the last rest, use the server's sent value + the number of ticks that have passed on the client
            final int ticksSinceLastRest = player.getStats().getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)) + timeSinceLastServerUpdate;
            // Compute the alpha of the background
            final float alpha = computeAlpha(ticksSinceLastRest);
            // Use a white color with the correct alpha
            RenderSystem.color4f(1.0f, 1.0f, 1.0f, alpha);
            // Draw the texture
            blit(x, y, 64, 64, 16, 16, 16, 16);
            // Disable transparency
            RenderSystem.disableBlend();

            // Create a string containing the alpha value rounded to no decimal places
            final String textPercent = String.format("%2.0f", alpha * 100);
            final FontRenderer fontRenderer = minecraft.fontRenderer;
            final int textWidth = fontRenderer.getStringWidth(textPercent);
            // Render the percent to being able to spawn phantoms as text
            fontRenderer.drawString(textPercent, x - textWidth / 2 + 8, y + 6, Color.WHITE.getRGB());

            RenderSystem.popMatrix();
        }
    }

    /**
     * Simple function to turn the number of ticks since last rest into an alpha value
     *
     * @param timeSinceLastRest The number of ticks since the last time we slept
     * @return A value from 0 (invisible) to 1 (visible)
     */
    private float computeAlpha(int timeSinceLastRest)
    {
        return MathHelper.clamp((float) timeSinceLastRest / TIME_WHEN_PHANTOMS_CAN_SPAWN, 0.0f, 1.0f);
    }
}
