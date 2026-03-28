package fr.jaeforzs.ris;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.tab.GridScreenTab;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class RandomTab extends GridScreenTab {
    private static final Text TITLE = Text.translatable("ris.createWorld.tab.custom.title");

    private final ComplicationWidget complicationWidget;
    private final ItemWidget itemWidget;

    public RandomTab() {
        super(TITLE);
        GridWidget.Adder adder = this.grid.setColumnSpacing(20).setRowSpacing(8).createAdder(2);

        this.complicationWidget = new ComplicationWidget(0, 0, 160, 130, 1.0f);
        adder.add(this.complicationWidget);

        this.itemWidget = new ItemWidget(0, 0, 160, 130, 3.5f);
        adder.add(this.itemWidget);

        GridWidget leftButtonGrid = new GridWidget();
        GridWidget.Adder leftBtnAdder = leftButtonGrid.setColumnSpacing(4).createAdder(2);
        leftBtnAdder.add(ButtonWidget.builder(Text.translatable("ris.button.roll_complication").formatted(Formatting.BOLD), button -> {
            if (!this.complicationWidget.isAnimating()) {
                this.complicationWidget.startAnimation(40);
            }
        }).width(136).build());
        leftBtnAdder.add(ButtonWidget.builder(Text.literal("..."), button -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new ComplicationSelectionScreen(client.currentScreen, this.complicationWidget));
        }).width(20).build());
        adder.add(leftButtonGrid);

        GridWidget rightButtonGrid = new GridWidget();
        GridWidget.Adder rightBtnAdder = rightButtonGrid.setColumnSpacing(4).createAdder(2);
        rightBtnAdder.add(ButtonWidget.builder(Text.translatable("ris.button.roll").formatted(Formatting.BOLD), button -> {
            if (!this.itemWidget.isAnimating()) {
                this.itemWidget.startAnimation(40);
            }
        }).width(136).build());
        rightBtnAdder.add(ButtonWidget.builder(Text.literal("..."), button -> {
            MinecraftClient client = MinecraftClient.getInstance();
            client.setScreen(new ItemSelectionScreen(client.currentScreen, this.itemWidget));
        }).width(20).build());
        adder.add(rightButtonGrid);
    }

    public Item getCurrentItem() {
        return this.itemWidget.getCurrentItem();
    }

    public String getCurrentComplication() {
        return this.complicationWidget.getCurrentComplication();
    }

    public static int interpolateColor(int color1, int color2, float fraction) {
        int a1 = (color1 >> 24) & 0xFF;
        int r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF;
        int b1 = color1 & 0xFF;

        int a2 = (color2 >> 24) & 0xFF;
        int r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF;
        int b2 = color2 & 0xFF;

        int a = (int) (a1 + (a2 - a1) * fraction);
        int r = (int) (r1 + (r2 - r1) * fraction);
        int g = (int) (g1 + (g2 - g1) * fraction);
        int b = (int) (b1 + (b2 - b1) * fraction);

        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    @Environment(EnvType.CLIENT)
    public static class ComplicationWidget extends ClickableWidget {
        private static final List<String> COMPLICATIONS = List.of(
                "ris.comp.none", "ris.comp.giant", "ris.comp.midget", "ris.comp.twins",
                "ris.comp.triplets", "ris.comp.turtle", "ris.comp.sonic", "ris.comp.vastness",
                "ris.comp.maximalism", "ris.comp.vegetarian", "ris.comp.carnivore", "ris.comp.mole",
                "ris.comp.noir", "ris.comp.cockeyed", "ris.comp.antisocial", "ris.comp.protagonist",
                "ris.comp.npc", "ris.comp.snail", "ris.comp.acceleration", "ris.comp.holes_in_pockets",
                "ris.comp.hydrophobe", "ris.comp.thalassophobe", "ris.comp.deafness", "ris.comp.insomnia",
                "ris.comp.one_armed", "ris.comp.fragile_bones", "ris.comp.ignoramus"
        );

        private String currentComplication = COMPLICATIONS.get(0);
        private boolean animating;
        private long startTime;
        private static final long ANIMATION_DURATION = 3500;
        private final List<String> rollSequence = new ArrayList<>();
        private int targetScrollIndex = 3;
        private int lastSoundIndex = -1;
        private static final int COMP_SPACING = 30;

        private boolean finalSoundPlayed;
        private float flashAlpha;
        private float tooltipX = Float.NaN;
        private float tooltipY = Float.NaN;
        private final java.util.Random random = new java.util.Random();

        public ComplicationWidget(int x, int y, int width, int height, float scale) {
            super(x, y, width, height, Text.empty());
            this.active = false;
            generateStaticSequence();
        }

        public String getCurrentComplication() {
            return this.currentComplication;
        }

        public static String[] splitComplication(String key) {
            String full = Text.translatable(key).getString();
            int s = full.indexOf('(');
            int e = full.lastIndexOf(')');
            if (s != -1 && e != -1 && e > s) {
                return new String[]{
                        full.substring(0, s).trim(),
                        full.substring(s + 1, e).trim()
                };
            }
            return new String[]{full, ""};
        }

        private String getRandomComplication(String exclude1, String exclude2) {
            String comp;
            do {
                comp = COMPLICATIONS.get(random.nextInt(COMPLICATIONS.size() - 1) + 1);
            } while (comp.equals(exclude1) || comp.equals(exclude2));
            return comp;
        }

        private void generateStaticSequence() {
            this.rollSequence.clear();
            String lastComp = null;
            for (int i = 0; i < 7; i++) {
                if (i == targetScrollIndex) {
                    this.rollSequence.add(this.currentComplication);
                    lastComp = this.currentComplication;
                } else {
                    String exclude2 = (i == targetScrollIndex - 1 || i == targetScrollIndex + 1) ? this.currentComplication : null;
                    String comp = getRandomComplication(lastComp, exclude2);
                    this.rollSequence.add(comp);
                    lastComp = comp;
                    lastComp = comp;
                }
            }
        }

        public void setTargetComplication(String complicationKey) {
            this.currentComplication = complicationKey;
            this.animating = false;
            this.targetScrollIndex = 3;
            generateStaticSequence();
            this.flashAlpha = 1.0f;

            
            PendingCreationData.setComplicationPreview(complicationKey);
        }

        public void startAnimation(int spins) {
            String previousResult = this.currentComplication;
            this.rollSequence.clear();
            this.targetScrollIndex = spins - 1;

            String lastComp = null;
            for (int i = 0; i < spins + 5; i++) {
                String comp = (i == this.targetScrollIndex) ? getRandomComplication(lastComp, previousResult) : getRandomComplication(lastComp, null);
                this.rollSequence.add(comp);
                lastComp = comp;
            }
            this.currentComplication = this.rollSequence.get(this.targetScrollIndex);

            this.animating = true;
            this.startTime = System.currentTimeMillis();
            this.lastSoundIndex = -1;
            this.flashAlpha = 0f;
            this.finalSoundPlayed = false;
        }

        public boolean isAnimating() {
            return animating;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            int x = this.getX();
            int y = this.getY();

            context.fillGradient(x, y, x + width, y + height, 0xFF222222, 0xFF111111);
            int borderColor = animating ? 0xFFFF5555 : 0xFF555555;
            context.fill(x, y, x + width, y + 1, borderColor);
            context.fill(x, y + height - 1, x + width, y + height, borderColor);
            context.fill(x, y, x + 1, y + height, borderColor);
            context.fill(x + width - 1, y, x + width, y + height, borderColor);

            Text headerText = Text.translatable("ris.complication.title").formatted(Formatting.GRAY);
            context.drawTextWithShadow(client.textRenderer, headerText,
                    x + width / 2 - client.textRenderer.getWidth(headerText) / 2, y + 6, 0xFFFFFFFF);

            int slotX = x + 8;
            int slotY = y + 20;
            int slotW = width - 16;
            int slotH = 76;
            int centerY = slotY + slotH / 2;

            context.fill(slotX, slotY, slotX + slotW, slotY + slotH, 0xFF050505);
            context.fill(slotX - 1, slotY - 1, slotX + slotW + 1, slotY, 0xFF333333);
            context.fill(slotX - 1, slotY + slotH, slotX + slotW + 1, slotY + slotH + 1, 0xFF333333);
            context.fill(slotX - 1, slotY, slotX, slotY + slotH, 0xFF333333);
            context.fill(slotX + slotW, slotY, slotX + slotW + 1, slotY + slotH, 0xFF333333);

            float currentScroll = targetScrollIndex * COMP_SPACING;

            if (animating) {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = Math.min(1.0f, (float) elapsed / ANIMATION_DURATION);
                float ease = 1.0f - (float) Math.pow(1.0f - t, 4);
                currentScroll = ease * (targetScrollIndex * COMP_SPACING);

                int soundIndex = (int) (currentScroll / COMP_SPACING);
                if (soundIndex > lastSoundIndex && soundIndex <= targetScrollIndex) {
                    lastSoundIndex = soundIndex;
                    float pitch = 1.5f - (t * 0.5f);
                    client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, pitch));
                }

                if (t >= 1.0f) {
                    animating = false;
                    flashAlpha = 1.0f;
                    currentScroll = targetScrollIndex * COMP_SPACING;

                    PendingCreationData.setComplicationPreview(this.currentComplication);

                    if (!finalSoundPlayed) {
                        finalSoundPlayed = true;
                        client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f));
                    }
                }
            }

            context.enableScissor(slotX, slotY, slotX + slotW, slotY + slotH);
            for (int i = 0; i < rollSequence.size(); i++) {
                float compY = centerY - currentScroll + (i * COMP_SPACING);
                if (compY > slotY - COMP_SPACING && compY < slotY + slotH + COMP_SPACING) {
                    String comp = rollSequence.get(i);
                    String compName = splitComplication(comp)[0];
                    int textWidth = client.textRenderer.getWidth(compName);

                    float maxTextWidth = slotW - 8;
                    float scale = 1.3f;
                    if (textWidth * scale > maxTextWidth) scale = maxTextWidth / textWidth;

                    Matrix3x2fStack matrices = context.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(slotX + slotW / 2f, compY);
                    matrices.scale(scale, scale);
                    context.drawTextWithShadow(client.textRenderer, Text.literal(compName),
                            -textWidth / 2, -client.textRenderer.fontHeight / 2, 0xFFDDDDDD);
                    matrices.popMatrix();
                }
            }
            context.disableScissor();

            context.fillGradient(slotX, slotY, slotX + slotW, slotY + 12, 0xAA000000, 0x00000000);
            context.fillGradient(slotX, slotY + slotH - 12, slotX + slotW, slotY + slotH, 0x00000000, 0xAA000000);

            if (flashAlpha > 0) {
                int alphaInt = (int) (flashAlpha * 255);
                if (alphaInt > 255) alphaInt = 255;
                if (alphaInt < 0) alphaInt = 0;
                context.fill(slotX, slotY, slotX + slotW, slotY + slotH, (alphaInt << 24) | 0xFFFFFF);
                flashAlpha -= 0.03f;
                if (flashAlpha < 0) flashAlpha = 0;
            }

            int activeIndex = Math.max(0, Math.min(rollSequence.size() - 1, Math.round(currentScroll / COMP_SPACING)));
            String activeComp = rollSequence.get(activeIndex);
            String[] parts = splitComplication(activeComp);
            int baseTextColor = animating ? 0xFFAAAAAA : 0xFFFF5555;
            int textColor = flashAlpha > 0 ? interpolateColor(baseTextColor, 0xFFFFFFFF, flashAlpha) : baseTextColor;

            int bottomTextWidth = client.textRenderer.getWidth(parts[0]);
            float bottomScale = 1.0f;
            if (bottomTextWidth > width - 16) bottomScale = (width - 16f) / bottomTextWidth;

            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(x + width / 2f, y + height - 16);
            matrices.scale(bottomScale, bottomScale);
            context.drawTextWithShadow(client.textRenderer, Text.literal(parts[0]), -bottomTextWidth / 2, 0, textColor);
            matrices.popMatrix();

            if (!parts[1].isEmpty() && isHovered() && !animating) {
                float targetX = mouseX + 15;
                float targetY = mouseY + 15;
                if (Float.isNaN(tooltipX)) {
                    tooltipX = targetX;
                    tooltipY = targetY;
                }
                tooltipX += (targetX - tooltipX) * 0.15f;
                tooltipY += (targetY - tooltipY) * 0.15f;
                context.drawTooltip(client.textRenderer, Text.literal(parts[1]), (int) tooltipX, (int) tooltipY);
            } else {
                tooltipX = Float.NaN;
                tooltipY = Float.NaN;
            }
        }

        public static List<String> getComplications() { return COMPLICATIONS; }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    @Environment(EnvType.CLIENT)
    public static class ItemWidget extends ClickableWidget {
        private Item currentItem = Items.DIAMOND;
        private boolean animating;
        private long startTime;
        private static final long ANIMATION_DURATION = 3500;
        private final List<Item> rollSequence = new ArrayList<>();
        private int targetScrollIndex = 3;
        private int lastSoundIndex = -1;
        private static final int ITEM_SPACING = 50;

        private float flashAlpha;

        public ItemWidget(int x, int y, int width, int height, float scale) {
            super(x, y, width, height, Text.empty());
            this.active = false;
            generateStaticSequence();
        }

        public Item getCurrentItem() {
            return this.currentItem;
        }

        private Item getRandomItem(Item exclude1, Item exclude2) {
            Item item;
            do {
                item = ItemRegistry.getRandomItem();
            } while (item == exclude1 || item == exclude2);
            return item;
        }

        private void generateStaticSequence() {
            this.rollSequence.clear();
            Item lastItem = null;
            for (int i = 0; i < 7; i++) {
                if (i == targetScrollIndex) {
                    this.rollSequence.add(this.currentItem);
                    lastItem = this.currentItem;
                } else {
                    Item exclude2 = (i == targetScrollIndex - 1 || i == targetScrollIndex + 1) ? this.currentItem : null;
                    Item item = getRandomItem(lastItem, exclude2);
                    this.rollSequence.add(item);
                    lastItem = item;
                }
            }
        }

        public void setTargetItem(Item item) {
            this.currentItem = item;
            this.animating = false;
            this.targetScrollIndex = 3;
            generateStaticSequence();
            this.flashAlpha = 1.0f;
        }

        public void startAnimation(int spins) {
            Item previousResult = this.currentItem;
            this.rollSequence.clear();
            this.targetScrollIndex = spins - 1;

            Item lastItem = null;
            for (int i = 0; i < spins + 5; i++) {
                Item item = (i == this.targetScrollIndex) ? getRandomItem(lastItem, previousResult) : getRandomItem(lastItem, null);
                this.rollSequence.add(item);
                lastItem = item;
            }
            this.currentItem = this.rollSequence.get(this.targetScrollIndex);

            this.animating = true;
            this.startTime = System.currentTimeMillis();
            this.lastSoundIndex = -1;
            this.flashAlpha = 0f;
        }

        public boolean isAnimating() {
            return animating;
        }

        @Override
        protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            int x = this.getX();
            int y = this.getY();

            context.fillGradient(x, y, x + width, y + height, 0xFF222222, 0xFF111111);
            int borderColor = animating ? 0xFFFFAA00 : 0xFF555555;
            context.fill(x, y, x + width, y + 1, borderColor);
            context.fill(x, y + height - 1, x + width, y + height, borderColor);
            context.fill(x, y, x + 1, y + height, borderColor);
            context.fill(x + width - 1, y, x + width, y + height, borderColor);

            Text headerText = Text.translatable("ris.item.title").formatted(Formatting.GRAY);
            context.drawTextWithShadow(client.textRenderer, headerText,
                    x + width / 2 - client.textRenderer.getWidth(headerText) / 2, y + 6, 0xFFFFFFFF);

            int slotX = x + 8;
            int slotY = y + 20;
            int slotW = width - 16;
            int slotH = 76;
            int centerY = slotY + slotH / 2;

            context.fill(slotX, slotY, slotX + slotW, slotY + slotH, 0xFF050505);
            context.fill(slotX - 1, slotY - 1, slotX + slotW + 1, slotY, 0xFF333333);
            context.fill(slotX - 1, slotY + slotH, slotX + slotW + 1, slotY + slotH + 1, 0xFF333333);
            context.fill(slotX - 1, slotY, slotX, slotY + slotH, 0xFF333333);
            context.fill(slotX + slotW, slotY, slotX + slotW + 1, slotY + slotH, 0xFF333333);

            float currentScroll = targetScrollIndex * ITEM_SPACING;

            if (animating) {
                long elapsed = System.currentTimeMillis() - startTime;
                float t = Math.min(1.0f, (float) elapsed / ANIMATION_DURATION);
                float ease = 1.0f - (float) Math.pow(1.0f - t, 4);
                currentScroll = ease * (targetScrollIndex * ITEM_SPACING);

                int soundIndex = (int) (currentScroll / ITEM_SPACING);
                if (soundIndex > lastSoundIndex && soundIndex <= targetScrollIndex) {
                    lastSoundIndex = soundIndex;
                    float pitch = 1.5f - (t * 0.5f);
                    client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(SoundEvents.UI_BUTTON_CLICK, pitch));
                }

                if (t >= 1.0f) {
                    animating = false;
                    flashAlpha = 1.0f;
                    currentScroll = targetScrollIndex * ITEM_SPACING;
                    client.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f));
                }
            }

            context.enableScissor(slotX, slotY, slotX + slotW, slotY + slotH);
            for (int i = 0; i < rollSequence.size(); i++) {
                float itemY = centerY - currentScroll + (i * ITEM_SPACING);
                if (itemY > slotY - ITEM_SPACING && itemY < slotY + slotH + ITEM_SPACING) {
                    Item item = rollSequence.get(i);
                    Matrix3x2fStack matrices = context.getMatrices();
                    matrices.pushMatrix();
                    matrices.translate(slotX + slotW / 2f, itemY);
                    matrices.scale(2.5f, 2.5f);
                    context.drawItem(item.getDefaultStack(), -8, -8);
                    matrices.popMatrix();
                }
            }
            context.disableScissor();

            context.fillGradient(slotX, slotY, slotX + slotW, slotY + 12, 0xAA000000, 0x00000000);
            context.fillGradient(slotX, slotY + slotH - 12, slotX + slotW, slotY + slotH, 0x00000000, 0xAA000000);

            if (flashAlpha > 0) {
                int alphaInt = (int) (flashAlpha * 255);
                if (alphaInt > 255) alphaInt = 255;
                if (alphaInt < 0) alphaInt = 0;
                context.fill(slotX, slotY, slotX + slotW, slotY + slotH, (alphaInt << 24) | 0xFFFFFF);
                flashAlpha -= 0.03f;
                if (flashAlpha < 0) flashAlpha = 0;
            }

            int activeIndex = Math.max(0, Math.min(rollSequence.size() - 1, Math.round(currentScroll / ITEM_SPACING)));
            Item activeItem = rollSequence.get(activeIndex);
            Text itemName = activeItem.getName();

            int baseTextColor = animating ? 0xFFAAAAAA : 0xFFFFAA00;
            int textColor = flashAlpha > 0 ? interpolateColor(baseTextColor, 0xFFFFFFFF, flashAlpha) : baseTextColor;

            int bottomTextWidth = client.textRenderer.getWidth(itemName);
            float bottomScale = 1.0f;
            if (bottomTextWidth > width - 16) bottomScale = (width - 16f) / bottomTextWidth;

            Matrix3x2fStack matrices = context.getMatrices();
            matrices.pushMatrix();
            matrices.translate(x + width / 2f, y + height - 16);
            matrices.scale(bottomScale, bottomScale);
            context.drawTextWithShadow(client.textRenderer, itemName, -bottomTextWidth / 2, 0, textColor);
            matrices.popMatrix();
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
    }

    @Environment(EnvType.CLIENT)
    public static class ComplicationSelectionScreen extends Screen {
        private final Screen parent;
        private final ComplicationWidget complicationWidget;
        private TextFieldWidget searchBox;
        private List<String> filteredComplications;
        private int page;
        private static final int ITEMS_PER_PAGE = 7;
        private float tooltipX = Float.NaN;
        private float tooltipY = Float.NaN;

        public ComplicationSelectionScreen(Screen parent, ComplicationWidget complicationWidget) {
            super(Text.translatable("ris.complication.select"));
            this.parent = parent;
            this.complicationWidget = complicationWidget;
        }

        @Override
        protected void init() {
            filteredComplications = ComplicationWidget.getComplications();
            searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 10, 200, 20, Text.translatable("ris.search"));
            searchBox.setPlaceholder(Text.translatable("ris.search"));
            searchBox.setChangedListener(text -> {
                String query = text.toLowerCase();
                filteredComplications = ComplicationWidget.getComplications().stream()
                        .filter(m -> Text.translatable(m).getString().toLowerCase().contains(query))
                        .toList();
                page = 0;
                refreshWidgets();
            });
            refreshWidgets();
        }

        private void refreshWidgets() {
            this.clearChildren();
            this.addDrawableChild(searchBox);
            int totalPages = Math.max(1, (int) Math.ceil((double) filteredComplications.size() / ITEMS_PER_PAGE));

            this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> {
                if (page > 0) { page--; refreshWidgets(); }
            }).dimensions(this.width / 2 - 120, 10, 16, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> {
                if (page < totalPages - 1) { page++; refreshWidgets(); }
            }).dimensions(this.width / 2 + 104, 10, 16, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("ris.back"), b -> {
                this.client.setScreen(parent);
            }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());

            int startY = 40;
            int idx = page * ITEMS_PER_PAGE;
            int buttonWidth = 260;
            int buttonHeight = 20;

            for(int row = 0; row < ITEMS_PER_PAGE; row++) {
                if (idx < filteredComplications.size()) {
                    String compKey = filteredComplications.get(idx);
                    String[] parts = ComplicationWidget.splitComplication(compKey);
                    int bx = this.width / 2 - buttonWidth / 2;
                    int by = startY + row * 24;

                    this.addDrawableChild(ButtonWidget.builder(Text.literal(parts[0]), b -> {
                        complicationWidget.setTargetComplication(compKey);
                        this.client.setScreen(parent);
                    }).dimensions(bx, by, buttonWidth, buttonHeight).build());
                    idx++;
                }
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);

            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 30, 0xFFFFFFFF);

            int totalPages = Math.max(1, (int) Math.ceil((double) filteredComplications.size() / ITEMS_PER_PAGE));
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal((page + 1) + " / " + totalPages), this.width / 2, this.height - 45, 0xFFFFFFFF);

            String hoverTooltip = null;
            int idxRender = page * ITEMS_PER_PAGE;
            int startY = 40;
            int buttonWidth = 260;
            int buttonHeight = 20;

            for(int row = 0; row < ITEMS_PER_PAGE; row++) {
                if (idxRender < filteredComplications.size()) {
                    int bx = this.width / 2 - buttonWidth / 2;
                    int by = startY + row * 24;
                    if (mouseX >= bx && mouseX <= bx + buttonWidth && mouseY >= by && mouseY <= by + buttonHeight) {
                        String compKey = filteredComplications.get(idxRender);
                        String[] parts = ComplicationWidget.splitComplication(compKey);
                        if (!parts[1].isEmpty()) {
                            hoverTooltip = parts[1];
                        }
                    }
                    idxRender++;
                }
            }

            if (hoverTooltip != null) {
                float targetX = mouseX + 15;
                float targetY = mouseY + 15;
                if (Float.isNaN(tooltipX)) {
                    tooltipX = targetX;
                    tooltipY = targetY;
                }
                tooltipX += (targetX - tooltipX) * 0.15f;
                tooltipY += (targetY - tooltipY) * 0.15f;
                context.drawTooltip(this.textRenderer, Text.literal(hoverTooltip), (int) tooltipX, (int) tooltipY);
            } else {
                tooltipX = Float.NaN;
                tooltipY = Float.NaN;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public static class ItemSelectionScreen extends Screen {
        private final Screen parent;
        private final ItemWidget itemWidget;
        private TextFieldWidget searchBox;
        private List<Item> filteredItems;
        private int page;
        private static final int ITEMS_PER_PAGE = 45;

        public ItemSelectionScreen(Screen parent, ItemWidget itemWidget) {
            super(Text.translatable("ris.item.select"));
            this.parent = parent;
            this.itemWidget = itemWidget;
        }

        @Override
        protected void init() {
            filteredItems = ItemRegistry.getAllItems();
            searchBox = new TextFieldWidget(this.textRenderer, this.width / 2 - 100, 10, 200, 20, Text.translatable("ris.search"));
            searchBox.setPlaceholder(Text.translatable("ris.search"));
            searchBox.setChangedListener(text -> {
                String query = text.toLowerCase();
                filteredItems = ItemRegistry.getAllItems().stream()
                        .filter(i -> i.getName().getString().toLowerCase().contains(query))
                        .toList();
                page = 0;
                refreshWidgets();
            });
            refreshWidgets();
        }

        private void refreshWidgets() {
            this.clearChildren();
            this.addDrawableChild(searchBox);

            int totalPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE));

            this.addDrawableChild(ButtonWidget.builder(Text.literal("<"), b -> {
                if (page > 0) { page--; refreshWidgets(); }
            }).dimensions(this.width / 2 - 120, 10, 16, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.literal(">"), b -> {
                if (page < totalPages - 1) { page++; refreshWidgets(); }
            }).dimensions(this.width / 2 + 104, 10, 16, 20).build());

            this.addDrawableChild(ButtonWidget.builder(Text.translatable("ris.back"), b -> {
                this.client.setScreen(parent);
            }).dimensions(this.width / 2 - 50, this.height - 30, 100, 20).build());

            int startX = this.width / 2 - (9 * 24) / 2;
            int startY = 45;
            int idx = page * ITEMS_PER_PAGE;

            for(int row = 0; row < 5; row++) {
                for(int col = 0; col < 9; col++) {
                    if (idx < filteredItems.size()) {
                        Item item = filteredItems.get(idx);
                        int bx = startX + col * 24;
                        int by = startY + row * 24;
                        this.addDrawableChild(ButtonWidget.builder(Text.empty(), b -> {
                            itemWidget.setTargetItem(item);
                            this.client.setScreen(parent);
                        }).dimensions(bx, by, 22, 22).build());
                        idx++;
                    }
                }
            }
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {

            super.render(context, mouseX, mouseY, delta);

            context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 33, 0xFFFFFFFF);

            int startX = this.width / 2 - (9 * 24) / 2;
            int startY = 45;
            int idx = page * ITEMS_PER_PAGE;

            for(int row = 0; row < 5; row++) {
                for(int col = 0; col < 9; col++) {
                    if (idx < filteredItems.size()) {
                        Item item = filteredItems.get(idx);
                        int bx = startX + col * 24;
                        int by = startY + row * 24;
                        context.drawItem(item.getDefaultStack(), bx + 3, by + 3);

                        if (mouseX >= bx && mouseX < bx + 22 && mouseY >= by && mouseY < by + 22) {
                            context.drawTooltip(this.textRenderer, item.getName(), mouseX, mouseY);
                        }
                        idx++;
                    }
                }
            }

            int totalPages = Math.max(1, (int) Math.ceil((double) filteredItems.size() / ITEMS_PER_PAGE));
            context.drawCenteredTextWithShadow(this.textRenderer, Text.literal((page + 1) + " / " + totalPages), this.width / 2, 175, 0xFFFFFFFF);
        }
    }
}