
package fr.jaeforzs.ris.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.text.Text;

public class AutoCreateLoadingScreen extends Screen {

    private int ticks = 0;

    public AutoCreateLoadingScreen() {
        super(Text.translatable("ris.screen.loading.shutting_down"));
    }

    @Override
    public void tick() {
        super.tick();
        ticks++;

        if (ticks == 60) {
            CreateWorldScreen.show(MinecraftClient.getInstance(), () -> {});
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
//        super.render(context, mouseX, mouseY, delta);

//        this.renderBackground(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                this.title,
                this.width / 2,
                this.height / 2 - 10,
                0xFFFFFF
        );

        String dots = "...".substring(0, (ticks / 10) % 4);
        String waitingText = Text.translatable("ris.screen.loading.waiting").getString() + dots;

        context.drawCenteredTextWithShadow(
                this.textRenderer,
                Text.literal(waitingText),
                this.width / 2,
                this.height / 2 + 10,
                0xAAAAAA
        );
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}