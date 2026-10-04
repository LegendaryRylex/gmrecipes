package dev.rylex.gmrecipes.guide;

import dev.rylex.gmrecipes.GmRecipes;
import guideme.document.LytRect;
import guideme.document.block.LytBlock;
import guideme.document.block.LytSlot;
import guideme.document.block.LytSlotGrid;
import guideme.layout.LayoutContext;
import guideme.render.GuiAssets;
import guideme.render.RenderContext;
import guideme.style.ResolvedTextStyle;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * The superclass builds its own item-only slots, so this class holds and lays out every cell itself
 * and turns off the inherited empty-slot pass.
 */
public final class LytIngredientGrid extends LytSlotGrid {

    private static final ResolvedTextStyle STYLE = RecipeBoxStyle.TEXT;
    private static final String CHANCE_KEY = GmRecipes.MOD_ID + ".recipe.chance";
    private static final int AMOUNT_GAP = 2;

    private final int columns;
    private final int rows;
    private final LytBlock[] cells;
    private final String[] chances;
    private final boolean[] amounts;
    private boolean renderEmptySlots = true;
    private int cellWidth = LytSlot.OUTER_SIZE;
    private int cellHeight = LytSlot.OUTER_SIZE;
    private int columnPitch = LytSlot.OUTER_SIZE;
    private int rowPitch = LytSlot.OUTER_SIZE;

    public LytIngredientGrid(int columns, int rows) {
        super(columns, rows);
        this.columns = columns;
        this.rows = rows;
        this.cells = new LytBlock[columns * rows];
        this.chances = new String[columns * rows];
        this.amounts = new boolean[columns * rows];
        super.setRenderEmptySlots(false);
    }

    public static LytIngredientGrid row(List<SlotContent> contents) {
        List<SlotContent> filled = contents.stream().filter(c -> !c.isEmpty()).toList();
        LytIngredientGrid grid = new LytIngredientGrid(filled.size(), 1);
        for (int i = 0; i < filled.size(); i++) {
            grid.set(i, 0, filled.get(i));
        }
        return grid;
    }

    public static LytIngredientGrid of(LytBlock slot) {
        LytIngredientGrid grid = new LytIngredientGrid(1, 1);
        grid.cells[0] = slot;
        grid.append(slot);
        return grid;
    }

    public static LytIngredientGrid column(List<SlotContent> contents) {
        List<SlotContent> filled = contents.stream().filter(c -> !c.isEmpty()).toList();
        LytIngredientGrid grid = new LytIngredientGrid(1, filled.size());
        for (int i = 0; i < filled.size(); i++) {
            grid.set(0, i, filled.get(i));
        }
        return grid;
    }

    public static LytIngredientGrid wrapped(List<SlotContent> contents, int maxColumns) {
        List<SlotContent> filled = contents.stream().filter(c -> !c.isEmpty()).toList();
        int columns = Math.max(1, Math.min(filled.size(), maxColumns));
        int rows = Math.max(1, (filled.size() + columns - 1) / columns);
        LytIngredientGrid grid = new LytIngredientGrid(columns, rows);
        for (int i = 0; i < filled.size(); i++) {
            grid.set(i % columns, i / columns, filled.get(i));
        }
        grid.setRenderEmptySlots(false);
        return grid;
    }

    public void set(int x, int y, SlotContent content) {
        if (x < 0 || x >= columns) {
            throw new IndexOutOfBoundsException("x: " + x);
        }
        if (y < 0 || y >= rows) {
            throw new IndexOutOfBoundsException("y: " + y);
        }
        int index = y * columns + x;
        LytBlock previous = cells[index];
        if (previous != null) {
            removeChild(previous);
        }
        chances[index] = null;
        amounts[index] = content.showsAmount();
        if (content.isEmpty()) {
            cells[index] = null;
            return;
        }
        if (content.chance() < 1) {
            chances[index] = Component.translatable(CHANCE_KEY, percent(content.chance()))
                    .getString();
        }
        LytBlock slot = content.createSlot();
        cells[index] = slot;
        append(slot);
    }

    @Override
    public void setItem(int x, int y, ItemStack item) {
        set(x, y, SlotContent.of(item));
    }

    @Override
    public void setIngredient(int x, int y, Ingredient ingredient) {
        set(x, y, SlotContent.of(ingredient));
    }

    @Override
    public boolean isRenderEmptySlots() {
        return renderEmptySlots;
    }

    @Override
    public void setRenderEmptySlots(boolean renderEmptySlots) {
        this.renderEmptySlots = renderEmptySlots;
    }

    @Override
    protected LytRect computeBoxLayout(LayoutContext context, int x, int y, int availableWidth) {
        cellWidth = LytSlot.OUTER_SIZE;
        cellHeight = LytSlot.OUTER_SIZE;
        for (String chance : chances) {
            if (chance != null) {
                cellWidth = Math.max(cellWidth, textWidth(context, chance) + 2);
                cellHeight = LytSlot.OUTER_SIZE + context.getLineHeight(STYLE);
            }
        }
        int gap = showsAnyAmount() ? AMOUNT_GAP : 0;
        columnPitch = cellWidth + gap;
        rowPitch = cellHeight + gap;
        int inset = (cellWidth - LytSlot.OUTER_SIZE) / 2;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                LytBlock cell = cells[row * columns + col];
                if (cell != null) {
                    cell.layout(context, x + col * columnPitch + inset, y + row * rowPitch, availableWidth);
                }
            }
        }
        return new LytRect(x, y, columnPitch * columns - gap, rowPitch * rows - gap);
    }

    private boolean showsAnyAmount() {
        for (boolean amount : amounts) {
            if (amount) {
                return true;
            }
        }
        return false;
    }

    private static int textWidth(LayoutContext context, String text) {
        double advances = text.codePoints()
                .mapToDouble(cp -> context.getAdvance(cp, STYLE))
                .sum();
        return (int) Math.ceil(advances * STYLE.fontScale());
    }

    private static String percent(float chance) {
        return BigDecimal.valueOf(chance * 100.0)
                .setScale(2, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    @Override
    public void render(RenderContext context) {
        if (renderEmptySlots) {
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    if (cells[row * columns + col] == null) {
                        context.drawIcon(
                                getBounds().x() + columnPitch * col + (cellWidth - LytSlot.OUTER_SIZE) / 2,
                                getBounds().y() + rowPitch * row,
                                GuiAssets.SLOT_BACKGROUND);
                    }
                }
            }
        }
        super.render(context);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
        super.renderBatch(context, buffers);
        for (int i = 0; i < chances.length; i++) {
            if (chances[i] != null) {
                float width = context.getWidth(chances[i], STYLE);
                float x = getBounds().x() + (i % columns) * columnPitch + (cellWidth - width) / 2f;
                float y = getBounds().y() + (i / columns) * rowPitch + LytSlot.OUTER_SIZE;
                context.renderTextInBatch(chances[i], STYLE, x, y, buffers);
            }
        }
    }
}
