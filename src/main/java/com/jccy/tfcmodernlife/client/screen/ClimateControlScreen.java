package com.jccy.tfcmodernlife.client.screen;

import com.jccy.tfcmodernlife.common.blockentity.ClimateControlBlockEntity;
import com.jccy.tfcmodernlife.common.climate.GreenhouseTemperatureHelper;
import com.jccy.tfcmodernlife.common.container.ClimateControlContainer;
import java.util.ArrayList;
import java.util.List;
import net.dries007.tfc.client.RenderHelpers;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public abstract class ClimateControlScreen<T extends ClimateControlBlockEntity, C extends ClimateControlContainer<T>> extends AbstractContainerScreen<C>
{
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 186;

    private static final int DISPLAY_X = 38;
    private static final int DISPLAY_Y = 14;
    private static final int DISPLAY_WIDTH = 76;
    private static final int DISPLAY_HEIGHT = 50;

    private static final int SEGMENT_TOP = 0;
    private static final int SEGMENT_UPPER_LEFT = 1;
    private static final int SEGMENT_UPPER_RIGHT = 2;
    private static final int SEGMENT_MIDDLE = 3;
    private static final int SEGMENT_LOWER_LEFT = 4;
    private static final int SEGMENT_LOWER_RIGHT = 5;
    private static final int SEGMENT_BOTTOM = 6;
    private static final int[][] DIGIT_SEGMENTS = {
        {SEGMENT_TOP, SEGMENT_UPPER_LEFT, SEGMENT_UPPER_RIGHT, SEGMENT_LOWER_LEFT, SEGMENT_LOWER_RIGHT, SEGMENT_BOTTOM},
        {SEGMENT_UPPER_RIGHT, SEGMENT_LOWER_RIGHT},
        {SEGMENT_TOP, SEGMENT_UPPER_RIGHT, SEGMENT_MIDDLE, SEGMENT_LOWER_LEFT, SEGMENT_BOTTOM},
        {SEGMENT_TOP, SEGMENT_UPPER_RIGHT, SEGMENT_MIDDLE, SEGMENT_LOWER_RIGHT, SEGMENT_BOTTOM},
        {SEGMENT_UPPER_LEFT, SEGMENT_UPPER_RIGHT, SEGMENT_MIDDLE, SEGMENT_LOWER_RIGHT},
        {SEGMENT_TOP, SEGMENT_UPPER_LEFT, SEGMENT_MIDDLE, SEGMENT_LOWER_RIGHT, SEGMENT_BOTTOM},
        {SEGMENT_TOP, SEGMENT_UPPER_LEFT, SEGMENT_MIDDLE, SEGMENT_LOWER_LEFT, SEGMENT_LOWER_RIGHT, SEGMENT_BOTTOM},
        {SEGMENT_TOP, SEGMENT_UPPER_RIGHT, SEGMENT_LOWER_RIGHT},
        {SEGMENT_TOP, SEGMENT_UPPER_LEFT, SEGMENT_UPPER_RIGHT, SEGMENT_MIDDLE, SEGMENT_LOWER_LEFT, SEGMENT_LOWER_RIGHT, SEGMENT_BOTTOM},
        {SEGMENT_TOP, SEGMENT_UPPER_LEFT, SEGMENT_UPPER_RIGHT, SEGMENT_MIDDLE, SEGMENT_LOWER_RIGHT, SEGMENT_BOTTOM}
    };
    private static final int[][] LARGE_SEGMENT_RECTS = {
        {13, 196, 7, 2, 2, 0},
        {11, 197, 2, 9, 0, 1},
        {20, 197, 2, 9, 9, 1},
        {13, 205, 7, 3, 2, 9},
        {11, 207, 2, 9, 0, 11},
        {20, 207, 2, 9, 9, 11},
        {13, 215, 7, 2, 2, 19}
    };
    private static final int LARGE_MINUS_X = 43;
    private static final int LARGE_MINUS_Y = 32;
    private static final int LARGE_MINUS_SRC_X = 4;
    private static final int LARGE_MINUS_SRC_Y = 206;
    private static final int LARGE_MINUS_W = 5;
    private static final int LARGE_MINUS_H = 2;
    private static final int LARGE_FIRST_DIGIT_X = 50;
    private static final int LARGE_SECOND_DIGIT_X = 64;
    private static final int LARGE_DIGIT_Y = 22;
    private static final int LARGE_UNIT_X = 77;
    private static final int LARGE_UNIT_Y = 29;
    private static final int LARGE_UNIT_SRC_X = 38;
    private static final int LARGE_UNIT_SRC_Y = 203;
    private static final int LARGE_UNIT_W = 11;
    private static final int LARGE_UNIT_H = 14;

    private static final int[][] SMALL_SEGMENT_RECTS = {
        {12, 224, 3, 1, 1, 0},
        {11, 225, 1, 3, 0, 1},
        {15, 225, 1, 3, 4, 1},
        {12, 228, 3, 1, 1, 4},
        {11, 229, 1, 3, 0, 5},
        {15, 229, 1, 3, 4, 5},
        {12, 232, 3, 1, 1, 8}
    };
    private static final int SMALL_MINUS_X = 46;
    private static final int SMALL_MINUS_Y = 54;
    private static final int SMALL_MINUS_SRC_X = 7;
    private static final int SMALL_MINUS_SRC_Y = 228;
    private static final int SMALL_MINUS_W = 3;
    private static final int SMALL_MINUS_H = 1;
    private static final int SMALL_FIRST_DIGIT_X = 50;
    private static final int SMALL_SECOND_DIGIT_X = 56;
    private static final int SMALL_DIGIT_Y = 50;
    private static final int SMALL_UNIT_X = 63;
    private static final int SMALL_UNIT_Y = 55;
    private static final int SMALL_UNIT_SRC_X = 24;
    private static final int SMALL_UNIT_SRC_Y = 229;
    private static final int SMALL_UNIT_W = 3;
    private static final int SMALL_UNIT_H = 4;

    private static final int COLD_ICON_X = 98;
    private static final int COLD_ICON_Y = 17;
    private static final int COLD_ICON_SRC_X = 59;
    private static final int COLD_ICON_SRC_Y = 191;
    private static final int COLD_ICON_W = 11;
    private static final int COLD_ICON_H = 13;
    private static final int HEAT_ICON_X = 98;
    private static final int HEAT_ICON_Y = 33;
    private static final int HEAT_ICON_SRC_X = 59;
    private static final int HEAT_ICON_SRC_Y = 207;
    private static final int HEAT_ICON_W = 12;
    private static final int HEAT_ICON_H = 11;
    private static final int THERMOMETER_X = 103;
    private static final int THERMOMETER_Y = 49;
    private static final int THERMOMETER_SRC_X = 64;
    private static final int THERMOMETER_SRC_Y = 223;
    private static final int THERMOMETER_W = 8;
    private static final int THERMOMETER_H = 12;

    private static final int BUTTON_Y = 72;
    private static final int BUTTON_H = 10;
    private static final int MINUS_FIVE_X = 43;
    private static final int MINUS_ONE_X = 54;
    private static final int PLUS_ONE_X = 67;
    private static final int PLUS_FIVE_X = 75;
    private static final int BUTTON_SRC_Y = 192;
    private static final int BUTTON_PRESSED_SRC_Y = 204;
    private static final int[] BUTTON_X = {MINUS_FIVE_X, MINUS_ONE_X, PLUS_ONE_X, PLUS_FIVE_X};
    private static final int[] BUTTON_W = {9, 6, 6, 9};
    private static final int[] BUTTON_SRC_X = {80, 91, 104, 112};

    private static final int ENERGY_X = 123;
    private static final int ENERGY_Y = 16;
    private static final int ENERGY_WIDTH = 17;
    private static final int ENERGY_HEIGHT = 62;
    private static final int ENERGY_FRAME_SRC_X = 144;
    private static final int ENERGY_FRAME_SRC_Y = 192;
    private static final int ENERGY_FILL_X = 128;
    private static final int ENERGY_FILL_Y = 21;
    private static final int ENERGY_FILL_WIDTH = 7;
    private static final int ENERGY_FILL_HEIGHT = 52;
    private static final int ENERGY_FILL_SRC_X = 131;
    private static final int ENERGY_FILL_SRC_Y = 197;

    private int pressedButton = -1;

    protected ClimateControlScreen(C container, Inventory playerInventory, Component title)
    {
        super(container, playerInventory, title);
        imageWidth = GUI_WIDTH;
        imageHeight = GUI_HEIGHT;
        inventoryLabelY = imageHeight - 94;
    }

    protected abstract ResourceLocation getBackground();

    protected boolean hasHeatIndicator()
    {
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY)
    {
        graphics.blit(getBackground(), leftPos, topPos, 0, 0, imageWidth, imageHeight);
        if (hasPanelPower())
        {
            drawDisplay(graphics);
            drawModeIcons(graphics);
        }
        drawButtons(graphics);
        drawEnergy(graphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY)
    {
        // The climate control art uses symbols and hover text instead of labels.
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick)
    {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        renderCustomTooltips(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            final int buttonId = getButtonAt(mouseX, mouseY);
            if (buttonId >= 0)
            {
                pressedButton = buttonId;
                sendButton(buttonId);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (button == 0)
        {
            pressedButton = -1;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void drawDisplay(GuiGraphics graphics)
    {
        drawLargeTemperature(graphics, GreenhouseTemperatureHelper.roundForPanel(menu.getSyncData().get(ClimateControlBlockEntity.DATA_AFTER_TEMPERATURE)));
        drawSmallTemperature(graphics, GreenhouseTemperatureHelper.roundForPanel(menu.getSyncData().get(ClimateControlBlockEntity.DATA_BEFORE_TEMPERATURE)));
        graphics.blit(getBackground(), leftPos + THERMOMETER_X, topPos + THERMOMETER_Y, THERMOMETER_SRC_X, THERMOMETER_SRC_Y, THERMOMETER_W, THERMOMETER_H);
    }

    private void drawModeIcons(GuiGraphics graphics)
    {
        final boolean running = menu.getSyncData().get(2) != 0;
        if (!running)
        {
            return;
        }
        final int afterTemperature = menu.getSyncData().get(ClimateControlBlockEntity.DATA_AFTER_TEMPERATURE);
        final int beforeTemperature = menu.getSyncData().get(ClimateControlBlockEntity.DATA_BEFORE_TEMPERATURE);
        if (afterTemperature < beforeTemperature)
        {
            graphics.blit(getBackground(), leftPos + COLD_ICON_X, topPos + COLD_ICON_Y, COLD_ICON_SRC_X, COLD_ICON_SRC_Y, COLD_ICON_W, COLD_ICON_H);
        }
        else if (afterTemperature > beforeTemperature && hasHeatIndicator())
        {
            graphics.blit(getBackground(), leftPos + HEAT_ICON_X, topPos + HEAT_ICON_Y, HEAT_ICON_SRC_X, HEAT_ICON_SRC_Y, HEAT_ICON_W, HEAT_ICON_H);
        }
    }

    private void drawLargeTemperature(GuiGraphics graphics, int temperature)
    {
        final int clamped = clampTemperature(temperature);
        if (clamped < 0)
        {
            graphics.blit(getBackground(), leftPos + LARGE_MINUS_X, topPos + LARGE_MINUS_Y, LARGE_MINUS_SRC_X, LARGE_MINUS_SRC_Y, LARGE_MINUS_W, LARGE_MINUS_H);
        }

        final int absolute = Math.abs(clamped);
        drawDigit(graphics, absolute / 10, LARGE_FIRST_DIGIT_X, LARGE_DIGIT_Y, LARGE_SEGMENT_RECTS);
        drawDigit(graphics, absolute % 10, LARGE_SECOND_DIGIT_X, LARGE_DIGIT_Y, LARGE_SEGMENT_RECTS);
        graphics.blit(getBackground(), leftPos + LARGE_UNIT_X, topPos + LARGE_UNIT_Y, LARGE_UNIT_SRC_X, LARGE_UNIT_SRC_Y, LARGE_UNIT_W, LARGE_UNIT_H);
    }

    private void drawSmallTemperature(GuiGraphics graphics, int temperature)
    {
        final int clamped = clampTemperature(temperature);
        if (clamped < 0)
        {
            graphics.blit(getBackground(), leftPos + SMALL_MINUS_X, topPos + SMALL_MINUS_Y, SMALL_MINUS_SRC_X, SMALL_MINUS_SRC_Y, SMALL_MINUS_W, SMALL_MINUS_H);
        }

        final int absolute = Math.abs(clamped);
        drawDigit(graphics, absolute / 10, SMALL_FIRST_DIGIT_X, SMALL_DIGIT_Y, SMALL_SEGMENT_RECTS);
        drawDigit(graphics, absolute % 10, SMALL_SECOND_DIGIT_X, SMALL_DIGIT_Y, SMALL_SEGMENT_RECTS);
        graphics.blit(getBackground(), leftPos + SMALL_UNIT_X, topPos + SMALL_UNIT_Y, SMALL_UNIT_SRC_X, SMALL_UNIT_SRC_Y, SMALL_UNIT_W, SMALL_UNIT_H);
    }

    private void drawDigit(GuiGraphics graphics, int digit, int x, int y, int[][] segmentRects)
    {
        final int safeDigit = Math.max(0, Math.min(9, digit));
        for (int segment : DIGIT_SEGMENTS[safeDigit])
        {
            final int[] rect = segmentRects[segment];
            graphics.blit(getBackground(), leftPos + x + rect[4], topPos + y + rect[5], rect[0], rect[1], rect[2], rect[3]);
        }
    }

    private int clampTemperature(int temperature)
    {
        return Math.max(-99, Math.min(99, temperature));
    }

    private void drawButtons(GuiGraphics graphics)
    {
        for (int id = 0; id < BUTTON_X.length; id++)
        {
            drawButton(graphics, id);
        }
    }

    private void drawButton(GuiGraphics graphics, int id)
    {
        graphics.blit(getBackground(), leftPos + BUTTON_X[id], topPos + BUTTON_Y, BUTTON_SRC_X[id], pressedButton == id ? BUTTON_PRESSED_SRC_Y : BUTTON_SRC_Y, BUTTON_W[id], BUTTON_H);
    }

    private void drawEnergy(GuiGraphics graphics)
    {
        graphics.blit(getBackground(), leftPos + ENERGY_X, topPos + ENERGY_Y, ENERGY_FRAME_SRC_X, ENERGY_FRAME_SRC_Y, ENERGY_WIDTH, ENERGY_HEIGHT);

        final int energy = menu.getSyncData().get(1);
        final int fillHeight = Math.min(ENERGY_FILL_HEIGHT, Math.round(ENERGY_FILL_HEIGHT * energy / (float) ClimateControlBlockEntity.ENERGY_CAPACITY));
        if (fillHeight > 0)
        {
            final int sourceY = ENERGY_FILL_SRC_Y + (ENERGY_FILL_HEIGHT - fillHeight);
            final int destY = ENERGY_FILL_Y + (ENERGY_FILL_HEIGHT - fillHeight);
            graphics.blit(getBackground(), leftPos + ENERGY_FILL_X, topPos + destY, ENERGY_FILL_SRC_X, sourceY, ENERGY_FILL_WIDTH, fillHeight);
        }
    }

    private void renderCustomTooltips(GuiGraphics graphics, int mouseX, int mouseY)
    {
        if (isInside(mouseX, mouseY, ENERGY_X, ENERGY_Y, ENERGY_WIDTH, ENERGY_HEIGHT))
        {
            final int energy = menu.getSyncData().get(1);
            graphics.renderTooltip(font, List.of(
                Component.translatable("tfc_modern_life.tooltip.energy", energy, ClimateControlBlockEntity.ENERGY_CAPACITY),
                Component.translatable("tfc_modern_life.tooltip.energy_start_threshold", ClimateControlBlockEntity.START_ENERGY_THRESHOLD)
            ), java.util.Optional.empty(), mouseX, mouseY);
            return;
        }

        final int target = menu.getSyncData().get(0);
        final int minTarget = menu.getSyncData().get(7);
        final int maxTarget = menu.getSyncData().get(8);
        if (isInsideButton(mouseX, mouseY, 0))
        {
            renderDecreaseTooltip(graphics, mouseX, mouseY, target, minTarget, 5);
            return;
        }
        if (isInsideButton(mouseX, mouseY, 1))
        {
            renderDecreaseTooltip(graphics, mouseX, mouseY, target, minTarget, 1);
            return;
        }
        if (isInsideButton(mouseX, mouseY, 2))
        {
            renderIncreaseTooltip(graphics, mouseX, mouseY, target, maxTarget, 1);
            return;
        }
        if (isInsideButton(mouseX, mouseY, 3))
        {
            renderIncreaseTooltip(graphics, mouseX, mouseY, target, maxTarget, 5);
            return;
        }
        if (isInside(mouseX, mouseY, DISPLAY_X, DISPLAY_Y, DISPLAY_WIDTH, DISPLAY_HEIGHT))
        {
            graphics.renderTooltip(font, createInfoTooltip(), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    private void renderDecreaseTooltip(GuiGraphics graphics, int mouseX, int mouseY, int target, int minTarget, int step)
    {
        final int available = Math.max(0, target - minTarget);
        graphics.renderTooltip(font, Component.translatable("tfc_modern_life.tooltip.decrease_step", Math.min(step, available), available), mouseX, mouseY);
    }

    private void renderIncreaseTooltip(GuiGraphics graphics, int mouseX, int mouseY, int target, int maxTarget, int step)
    {
        final int available = Math.max(0, maxTarget - target);
        graphics.renderTooltip(font, Component.translatable("tfc_modern_life.tooltip.increase_step", Math.min(step, available), available), mouseX, mouseY);
    }

    private List<Component> createInfoTooltip()
    {
        final List<Component> lines = new ArrayList<>();
        final int structureType = menu.getSyncData().get(ClimateControlBlockEntity.DATA_STRUCTURE_TYPE);
        final int tier = menu.getSyncData().get(ClimateControlBlockEntity.DATA_TIER);
        if (structureType == ClimateControlBlockEntity.STRUCTURE_GREENHOUSE)
        {
            final int minTarget = menu.getSyncData().get(ClimateControlBlockEntity.DATA_MIN_TARGET);
            final int maxTarget = menu.getSyncData().get(ClimateControlBlockEntity.DATA_MAX_TARGET);
            lines.add(Component.translatable("tfc_modern_life.tooltip.power_multiplier", getPowerMultiplier()));
            lines.add(Component.translatable(
                "tfc_modern_life.tooltip.greenhouse_with_base_temperature",
                getStructureName(structureType, tier),
                GreenhouseTemperatureHelper.formatSignedTemperatureDeltaTenths(menu.getSyncData().get(ClimateControlBlockEntity.DATA_BASE_TEMPERATURE_DELTA))
            ));
            lines.add(Component.translatable(hasHeatIndicator() ? "tfc_modern_life.tooltip.temperature_adjust_range" : "tfc_modern_life.tooltip.cooling_adjust_range", Math.max(Math.abs(minTarget), Math.abs(maxTarget))));
            addCommonInfo(lines);
        }
        else if (structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR)
        {
            final int minimumTemperature = menu.getSyncData().get(ClimateControlBlockEntity.DATA_BEFORE_TEMPERATURE)
                + menu.getSyncData().get(ClimateControlBlockEntity.DATA_MIN_TARGET) * GreenhouseTemperatureHelper.TEMPERATURE_SCALE;
            lines.add(getStructureName(structureType, tier));
            lines.add(Component.translatable("tfc_modern_life.tooltip.power_multiplier", getPowerMultiplier()));
            lines.add(Component.translatable("tfc_modern_life.tooltip.minimum_temperature", GreenhouseTemperatureHelper.formatTemperatureTenths(minimumTemperature)));
            if (menu.getSyncData().get(ClimateControlBlockEntity.DATA_PRESERVATION_TENTHS) > 0)
            {
                lines.add(Component.translatable("tfc_modern_life.tooltip.preservation_multiplier", menu.getSyncData().get(ClimateControlBlockEntity.DATA_PRESERVATION_TENTHS) / 10f));
            }
            if (hasHeatIndicator())
            {
                lines.add(Component.translatable("tfc_modern_life.tooltip.temperature_adjust_range", Math.max(Math.abs(menu.getSyncData().get(ClimateControlBlockEntity.DATA_MIN_TARGET)), Math.abs(menu.getSyncData().get(ClimateControlBlockEntity.DATA_MAX_TARGET)))));
            }
            addCommonInfo(lines);
        }
        else
        {
            lines.add(Component.translatable("screen.tfc_modern_life.climate_station.inactive"));
        }
        return lines;
    }

    private void addCommonInfo(List<Component> lines)
    {
        lines.add(Component.translatable("tfc_modern_life.tooltip.controlled_temperature", GreenhouseTemperatureHelper.formatTemperatureTenths(menu.getSyncData().get(ClimateControlBlockEntity.DATA_AFTER_TEMPERATURE))));
        lines.add(Component.translatable("tfc_modern_life.tooltip.base_temperature", GreenhouseTemperatureHelper.formatTemperatureTenths(menu.getSyncData().get(ClimateControlBlockEntity.DATA_BEFORE_TEMPERATURE))));
        lines.add(Component.translatable("tfc_modern_life.tooltip.effective_space", menu.getSyncData().get(ClimateControlBlockEntity.DATA_EFFECTIVE_SPACE)));
        lines.add(Component.translatable("tfc_modern_life.tooltip.energy_per_tick", menu.getSyncData().get(ClimateControlBlockEntity.DATA_ENERGY_PER_TICK)));
    }

    private float getPowerMultiplier()
    {
        return menu.getSyncData().get(ClimateControlBlockEntity.DATA_POWER_MULTIPLIER) / 10f;
    }

    private Component getStructureName(int structureType, int tier)
    {
        final String tierId = switch (tier)
        {
            case 1 -> "wood";
            case 2 -> "copper";
            case 3 -> "iron";
            case 4 -> "stainless_steel";
            case 5 -> "mixed";
            default -> "custom";
        };
        if (structureType == ClimateControlBlockEntity.STRUCTURE_CELLAR)
        {
            final String cellarTierId = switch (tier)
            {
                case 1 -> "sealed_brick";
                case 2 -> "wrought_iron";
                case 3 -> "stainless_steel";
                case 4 -> "mixed";
                default -> "custom";
            };
            return Component.translatable("screen.tfc_modern_life.cellar." + cellarTierId);
        }
        return Component.translatable("screen.tfc_modern_life.greenhouse." + tierId);
    }

    private boolean hasPanelPower()
    {
        return menu.getSyncData().get(1) > 0;
    }

    private void sendButton(int id)
    {
        if (minecraft != null && minecraft.gameMode != null)
        {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    protected boolean isInside(double mouseX, double mouseY, int x, int y, int width, int height)
    {
        return RenderHelpers.isInside((int) mouseX, (int) mouseY, leftPos + x, topPos + y, width, height);
    }

    private int getButtonAt(double mouseX, double mouseY)
    {
        for (int id = 0; id < BUTTON_X.length; id++)
        {
            if (isInsideButton(mouseX, mouseY, id))
            {
                return id;
            }
        }
        return -1;
    }

    private boolean isInsideButton(double mouseX, double mouseY, int id)
    {
        return id >= 0 && id < BUTTON_X.length && isInside(mouseX, mouseY, BUTTON_X[id], BUTTON_Y, BUTTON_W[id], BUTTON_H);
    }
}
