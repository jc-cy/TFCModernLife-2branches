package com.jccy.tfcmodernlife.common.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.jccy.tfcmodernlife.common.ModRecipeSerializers;
import java.util.ArrayList;
import java.util.List;
import net.dries007.tfc.util.Helpers;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public final class GreenhouseDisassemblyRecipe extends CustomRecipe
{
    private final Ingredient input;
    private final int inputCount;
    private final Ingredient tool;
    private final ItemStack result;
    private final List<ItemStack> extraProducts;

    public GreenhouseDisassemblyRecipe(ResourceLocation id, Ingredient input, int inputCount, Ingredient tool, ItemStack result, List<ItemStack> extraProducts)
    {
        super(id, CraftingBookCategory.MISC);
        this.input = input;
        this.inputCount = inputCount;
        this.tool = tool;
        this.result = result;
        this.extraProducts = List.copyOf(extraProducts);
    }

    @Override
    public boolean matches(CraftingContainer inv, Level level)
    {
        final Match match = findMatch(inv);
        return match.valid() && match.inputTotal() >= inputCount;
    }

    @Override
    public ItemStack assemble(CraftingContainer inv, RegistryAccess access)
    {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return width * height >= 2;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access)
    {
        return result.copy();
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        final NonNullList<Ingredient> ingredients = NonNullList.create();
        ingredients.add(input);
        ingredients.add(tool);
        return ingredients;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer inv)
    {
        final NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
        int inputToConsume = inputCount;
        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (tool.test(stack))
            {
                remaining.set(i, getToolRemainder(stack));
            }
            else if (input.test(stack))
            {
                final int consume = Math.min(inputToConsume, stack.getCount());
                if (consume == 0)
                {
                    remaining.set(i, stack.copyWithCount(1));
                }
                inputToConsume -= consume;
            }
        }

        final Player player = ForgeHooks.getCraftingPlayer();
        if (player != null)
        {
            extraProducts.forEach(stack -> ItemHandlerHelper.giveItemToPlayer(player, stack.copy()));
        }
        return remaining;
    }

    @Override
    public RecipeSerializer<?> getSerializer()
    {
        return ModRecipeSerializers.GREENHOUSE_DISASSEMBLY.get();
    }

    public int inputCount()
    {
        return inputCount;
    }

    public int countOccupiedInputSlots(CraftingContainer inv)
    {
        return findMatch(inv).inputSlots();
    }

    public int[] extraInputConsumption(CraftingContainer inv)
    {
        final int[] extraConsumption = new int[inv.getContainerSize()];
        int inputToConsume = inputCount;
        for (int i = 0; i < inv.getContainerSize() && inputToConsume > 0; i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty() && input.test(stack))
            {
                final int consume = Math.min(inputToConsume, stack.getCount());
                if (consume > 1)
                {
                    extraConsumption[i] = consume - 1;
                }
                inputToConsume -= consume;
            }
        }
        return extraConsumption;
    }

    public boolean isInput(ItemStack stack)
    {
        return input.test(stack);
    }

    public List<List<ItemStack>> getJeiInputs()
    {
        return List.of(withCount(input, inputCount), withCount(tool, 1));
    }

    public List<ItemStack> getJeiOutputs()
    {
        final List<ItemStack> outputs = new ArrayList<>(1 + extraProducts.size());
        outputs.add(result.copy());
        extraProducts.stream().map(ItemStack::copy).forEach(outputs::add);
        return outputs;
    }

    private Match findMatch(CraftingContainer inv)
    {
        int inputSlots = 0;
        int inputTotal = 0;
        int toolSlots = 0;

        for (int i = 0; i < inv.getContainerSize(); i++)
        {
            final ItemStack stack = inv.getItem(i);
            if (stack.isEmpty())
            {
                continue;
            }
            if (tool.test(stack))
            {
                if (stack.getCount() != 1)
                {
                    return Match.invalid();
                }
                toolSlots++;
            }
            else if (input.test(stack))
            {
                inputSlots++;
                inputTotal += stack.getCount();
            }
            else
            {
                return Match.invalid();
            }
        }
        return new Match(toolSlots == 1 && inputSlots > 0, inputSlots, inputTotal);
    }

    private static List<ItemStack> withCount(Ingredient ingredient, int count)
    {
        return List.of(ingredient.getItems()).stream()
            .map(stack -> stack.copyWithCount(count))
            .toList();
    }

    private static ItemStack getToolRemainder(ItemStack stack)
    {
        if (stack.isDamageableItem())
        {
            return Helpers.damageCraftingItem(stack.copy(), 1);
        }
        if (isUnbreakable(stack))
        {
            return stack.copy();
        }
        if (stack.hasCraftingRemainingItem())
        {
            return stack.getCraftingRemainingItem();
        }
        return ItemStack.EMPTY;
    }

    private static boolean isUnbreakable(ItemStack stack)
    {
        final @Nullable net.minecraft.nbt.CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean("Unbreakable");
    }

    private record Match(boolean valid, int inputSlots, int inputTotal)
    {
        static Match invalid()
        {
            return new Match(false, 0, 0);
        }
    }

    public static final class Serializer implements RecipeSerializer<GreenhouseDisassemblyRecipe>
    {
        @Override
        public GreenhouseDisassemblyRecipe fromJson(ResourceLocation id, JsonObject json)
        {
            final Ingredient input = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "input"));
            final int inputCount = GsonHelper.getAsInt(json, "input_count");
            if (inputCount <= 0)
            {
                throw new JsonParseException("input_count must be positive");
            }
            final Ingredient tool = Ingredient.fromJson(GsonHelper.getAsJsonObject(json, "tool"));
            final ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            final List<ItemStack> extraProducts = new ArrayList<>();
            if (json.has("extra_products"))
            {
                for (JsonElement element : GsonHelper.getAsJsonArray(json, "extra_products"))
                {
                    extraProducts.add(CraftingHelper.getItemStack(element.getAsJsonObject(), true));
                }
            }
            return new GreenhouseDisassemblyRecipe(id, input, inputCount, tool, result, extraProducts);
        }

        @Override
        public @Nullable GreenhouseDisassemblyRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer)
        {
            final Ingredient input = Ingredient.fromNetwork(buffer);
            final int inputCount = buffer.readVarInt();
            final Ingredient tool = Ingredient.fromNetwork(buffer);
            final ItemStack result = buffer.readItem();
            final int extraCount = buffer.readVarInt();
            final List<ItemStack> extraProducts = new ArrayList<>(extraCount);
            for (int i = 0; i < extraCount; i++)
            {
                extraProducts.add(buffer.readItem());
            }
            return new GreenhouseDisassemblyRecipe(id, input, inputCount, tool, result, extraProducts);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, GreenhouseDisassemblyRecipe recipe)
        {
            recipe.input.toNetwork(buffer);
            buffer.writeVarInt(recipe.inputCount);
            recipe.tool.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.extraProducts.size());
            recipe.extraProducts.forEach(buffer::writeItem);
        }
    }
}
