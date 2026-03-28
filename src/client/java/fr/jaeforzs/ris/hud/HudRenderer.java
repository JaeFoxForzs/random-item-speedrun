

package fr.jaeforzs.ris.hud;

import fr.jaeforzs.ris.complication.ClientComplicationManager;
import fr.jaeforzs.ris.timer.ClientTimerManager;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Отвечает за отрисовку HUD элементов спидрана.
 */
public class HudRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger("RIS/HudRenderer");

    
    private static Text cachedItemName;
    private static Text cachedCompName;
    private static Item lastItem;
    private static String lastCompId;
    private static boolean lastCompleted;

    /**
     * Инициализирует рендерер и регистрирует callback.
     */
    public static void initialize() {
        HudRenderCallback.EVENT.register(HudRenderer::render);
        LOGGER.info("HudRenderer initialized");
    }

    private static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        Item targetItem = ClientTimerManager.getTargetItem();
        String compId = ClientComplicationManager.getCurrentComplicationId();
        boolean completed = ClientTimerManager.isCompleted();

        
        updateCache(targetItem, compId, completed);

        
        String timeStr = ClientTimerManager.getFormattedTime();
        Formatting timeColor = completed ? Formatting.GREEN : Formatting.WHITE;
        Text timeText = Text.literal(timeStr).formatted(timeColor);

        
        renderPanel(context, client, targetItem, timeText, completed);
    }

    private static void updateCache(Item item, String compId, boolean completed) {
        
        if (item != lastItem || completed != lastCompleted) {
            Formatting color = completed ? Formatting.GREEN : Formatting.GOLD;
            cachedItemName = item.getName().copy().formatted(color);
            lastItem = item;
            lastCompleted = completed;
        }

        
        if (!compId.equals(lastCompId)) {
            cachedCompName = Text.translatable(compId).formatted(Formatting.GRAY);
            lastCompId = compId;
        }
    }

    private static void renderPanel(DrawContext context, MinecraftClient client,
                                    Item targetItem, Text timeText, boolean completed) {

        int w1 = client.textRenderer.getWidth(cachedItemName);
        int w2 = client.textRenderer.getWidth(cachedCompName);
        int maxTextWidth = Math.max(w1, w2);
        int timeWidth = client.textRenderer.getWidth(timeText);

        Text checkmark = Text.literal(" ✔").formatted(Formatting.GREEN, Formatting.BOLD);
        int checkWidth = completed ? client.textRenderer.getWidth(checkmark) : 0;

        int padding = 5;
        int iconSize = 16;
        int panelX = 5;
        int panelY = 5;
        int panelHeight = 24;

        int totalWidth = padding + iconSize + padding + maxTextWidth + (padding * 2) + timeWidth + checkWidth + padding;

        
        context.fill(panelX, panelY, panelX + totalWidth, panelY + panelHeight, 0x88000000);

        
        int borderColor = completed ? 0xFF55FF55 : 0xFFFFAA00;
        context.fill(panelX, panelY - 1, panelX + totalWidth, panelY, borderColor);
        context.fill(panelX, panelY + panelHeight, panelX + totalWidth, panelY + panelHeight + 1, borderColor);
        context.fill(panelX - 1, panelY, panelX, panelY + panelHeight, borderColor);
        context.fill(panelX + totalWidth, panelY, panelX + totalWidth + 1, panelY + panelHeight, borderColor);

        
        context.drawItem(targetItem.getDefaultStack(), panelX + padding, panelY + 4);

        
        int textX = panelX + padding + iconSize + padding;
        context.drawTextWithShadow(client.textRenderer, cachedItemName, textX, panelY + 3, 0xFFFFFFFF);
        context.drawTextWithShadow(client.textRenderer, cachedCompName, textX, panelY + 13, 0xFFFFFFFF);

        
        int timerX = textX + maxTextWidth + (padding * 2);
        context.drawTextWithShadow(client.textRenderer, timeText, timerX, panelY + 8, 0xFFFFFFFF);

        
        if (completed) {
            context.drawTextWithShadow(client.textRenderer, checkmark, timerX + timeWidth, panelY + 8, 0xFFFFFFFF);
        }
    }

    /**
     * Сбрасывает кэш (при смене мира).
     */
    public static void reset() {
        cachedItemName = null;
        cachedCompName = null;
        lastItem = null;
        lastCompId = null;
    }
}