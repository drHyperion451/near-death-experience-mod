package me.drhyperion451.neardeathexp;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

import static me.drhyperion451.neardeathexp.NearDeathExperienceMod.bellSound;

/**
 * Handles the rendering of Jesus overlay when the player is near death.
 * This class manages the fade-in/fade-out animation and proper texture scaling
 * using low-level rendering methods for correct alpha blending.
 */

public class JesusOverlay {
    // Texture resource location for the Jesus image
    private static final Identifier JESUS_TEXTURE = NearDeathExperienceMod.id("textures/gui/jesus.png");

    // Animation state variables
    private boolean isActive = false;
    private int animationTicks = 0;

    // Animation timing constants (in ticks, 20 ticks = 1 second)
    private static final int FADE_IN_DURATION = 20;  // 1 second fade in
    private static final int SHOW_DURATION = 40;     // 3 seconds at full opacity
    private static final int FADE_OUT_DURATION = 40; // 2 seconds for fade out
    private static final int TOTAL_DURATION = FADE_IN_DURATION + SHOW_DURATION + FADE_OUT_DURATION;


    // Scaled dimensions for display (adjust these to change the size)
    private final int scaledWidth = 256;  // Half the original size
    private final int scaledHeight = 144; // Half the original size

    /**
     * Constructor - registers the HUD render callback
     */
    public JesusOverlay() {
        // Register the callback to render on every HUD frame
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    /**
     * Activates the Jesus overlay effect
     * Called by HealthTracker when near-death conditions are met
     */
    public void showJesus() {
        if (!isActive) {
            isActive = true;
            animationTicks = 0;
            playBellSound();
        }
    }

    /**
     * Plays the bell sound effect when Jesus appears
     */
    private void playBellSound() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.getSoundManager().play(PositionedSoundInstance.master(bellSound, 1.0f, 1.0f));
        }
    }

    /**
     * Main render callback - called every frame when HUD is rendered
     * Handles the animation timing and rendering logic
     * Animation is paused when the game is paused
     */
    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        // Skip rendering if the overlay is not active
        if (!isActive) return;

        MinecraftClient client = MinecraftClient.getInstance();
        // Skip rendering if player is not available
        if (client.player == null) return;

        // Only update animation ticks if the game is not paused
        // This ensures the animation pauses when the game is paused (ESC menu, etc.)
        if (!client.isPaused()) {
            animationTicks++;
        }

        // Calculate current alpha value based on animation phase
        float alpha = calculateAlpha();

        // If completely transparent, stop the animation
        if (alpha <= 0) {
            isActive = false;
            return;
        }

        // Render the Jesus image with current alpha
        renderJesus(context, alpha);

        // Stop animation after total duration (only when not paused)
        if (!client.isPaused() && animationTicks >= TOTAL_DURATION) {
            isActive = false;
        }
    }

    /**
     * Calculates the alpha transparency value based on current animation phase
     * Now includes three phases: fade-in, show, and fade-out
     * @return float value between 0.0 (transparent) and 1.0 (opaque)
     */
    private float calculateAlpha() {
        float fullOpacity = 0.5f;
        if (animationTicks <= FADE_IN_DURATION) {
            // Phase 1: Fade in from transparent to opaque
            return (float) (animationTicks * fullOpacity / FADE_IN_DURATION);
        } else if (animationTicks <= FADE_IN_DURATION + SHOW_DURATION) {
            // Phase 2: Show at full opacity
            return fullOpacity;
        } else {
            // Phase 3: Fade out gradually
            int fadeOutProgress = animationTicks - FADE_IN_DURATION - SHOW_DURATION;
            return fullOpacity - ((float) fadeOutProgress / FADE_OUT_DURATION);
        }
    }

    /**
     * Renders the Jesus texture using low-level OpenGL calls for proper alpha blending
     * This method uses Tessellator to manually create a textured quad with transparency
     *
     * @param context The DrawContext for accessing transformation matrices
     * @param alpha The transparency value (0.0 = transparent, 1.0 = opaque)
     */
    private void renderJesus(DrawContext context, float alpha) {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        // Calculate centered position on screen
        int x = (screenWidth - scaledWidth) / 2;
        int y = (screenHeight - scaledHeight) / 2;

        // Configure OpenGL render state for texture rendering with transparency
        RenderSystem.setShader(GameRenderer::getPositionTexProgram); // Use position + texture shader
        RenderSystem.setShaderTexture(0, JESUS_TEXTURE); // Bind our texture to slot 0
        RenderSystem.enableBlend(); // Enable alpha blending
        RenderSystem.defaultBlendFunc(); // Use default blend function (src_alpha, one_minus_src_alpha)
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha); // Set color with alpha

        // Get the current transformation matrix from the context
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        // Create tessellator for manual vertex building
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);

        // Build a quad (rectangle) with 4 vertices
        // Each vertex has position (x, y, z) and texture coordinates (u, v)
        // Texture coordinates: (0,0) = top-left, (1,1) = bottom-right

        // Bottom-left vertex
        buffer.vertex(matrix, x, y + scaledHeight, 0).texture(0.0f, 1.0f);
        // Bottom-right vertex
        buffer.vertex(matrix, x + scaledWidth, y + scaledHeight, 0).texture(1.0f, 1.0f);
        // Top-right vertex
        buffer.vertex(matrix, x + scaledWidth, y, 0).texture(1.0f, 0.0f);
        // Top-left vertex
        buffer.vertex(matrix, x, y, 0).texture(0.0f, 0.0f);

        // Submit the buffer to be rendered
        BufferRenderer.drawWithGlobalProgram(buffer.end());

        // Restore OpenGL state to avoid affecting other rendering
        RenderSystem.disableBlend(); // Disable blending
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Reset color to full opacity
    }
}