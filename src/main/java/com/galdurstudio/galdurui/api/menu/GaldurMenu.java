package com.galdurstudio.galdurui.api.menu;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Base d'un menu à slots (côté logique, client <strong>et</strong> serveur) prêt à être affiché
 * par un {@link com.galdurstudio.galdurui.api.screen.GaldurContainerScreen}.
 *
 * <p>Elle évite le code répété de chaque menu : placer une grille de slots, placer l'inventaire
 * du joueur, et gérer le Maj+clic (déplacement rapide) entre les deux.</p>
 *
 * <pre>{@code
 * public class CoffreMenu extends GaldurMenu {
 *     private final Container coffre;
 *
 *     public CoffreMenu(int id, Inventory joueur, Container coffre) {
 *         super(MES_MENUS.COFFRE.get(), id);
 *         this.coffre = coffre;
 *         addSlotGrid(coffre, 0, 9, 3, 8, 18);   // 27 cases
 *         addPlayerInventory(joueur, 8, 84);     // toujours en dernier
 *     }
 *
 *     public boolean stillValid(Player player) {
 *         return coffre.stillValid(player);
 *     }
 * }
 * }</pre>
 *
 * <p><strong>Règle</strong> : les slots du conteneur s'ajoutent d'abord ({@link #addSlotGrid} ou
 * {@code addSlot}), l'inventaire du joueur ensuite ({@link #addPlayerInventory}). Tout slot
 * ajouté avant l'inventaire du joueur compte comme un slot du conteneur pour le Maj+clic.</p>
 *
 * <p>Les coordonnées sont celles de Minecraft : en pixels d'interface depuis le coin haut gauche
 * de l'écran du menu, et désignent le coin haut gauche de l'objet (16 × 16), le cadre du slot
 * débordant d'un pixel autour.</p>
 */
public abstract class GaldurMenu extends AbstractContainerMenu {
    /** Pas entre deux slots voisins, en pixels : 16 pour l'objet et 2 pour les cadres. */
    public static final int SLOT_SIZE = 18;
    /** Écart vertical entre les 3 rangées de l'inventaire du joueur et sa barre d'action. */
    public static final int HOTBAR_GAP = 4;

    /** Nombre de slots du conteneur : tous ceux ajoutés avant l'inventaire du joueur. */
    private int containerSlots = -1;
    /** Premier et dernier (exclu) index des slots de l'inventaire du joueur. */
    private int playerStart = -1;
    private int playerEnd = -1;

    /**
     * @param type        le type de menu, enregistré par le mod
     * @param containerId l'identifiant de fenêtre donné par Minecraft
     */
    protected GaldurMenu(@Nullable MenuType<?> type, int containerId) {
        super(type, containerId);
    }

    /**
     * Ajoute une grille de slots, rangée par rangée, de gauche à droite.
     *
     * @param container  le conteneur dont les cases sont affichées
     * @param firstIndex index, dans le conteneur, de la première case de la grille
     * @param columns    nombre de colonnes
     * @param rows       nombre de rangées
     * @param x          abscisse du premier slot (coin haut gauche de l'objet)
     * @param y          ordonnée du premier slot
     * @throws IllegalStateException si l'inventaire du joueur a déjà été ajouté
     */
    protected void addSlotGrid(Container container, int firstIndex, int columns, int rows, int x, int y) {
        if (playerStart >= 0) {
            throw new IllegalStateException("Ajouter les slots du conteneur avant addPlayerInventory.");
        }
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                addSlot(new Slot(container, firstIndex + row * columns + column,
                        x + column * SLOT_SIZE, y + row * SLOT_SIZE));
            }
        }
    }

    /**
     * Ajoute l'inventaire du joueur : 3 rangées de 9, puis la barre d'action
     * {@value #HOTBAR_GAP} pixels plus bas. À appeler une seule fois, après les slots du conteneur.
     *
     * @param inventory l'inventaire du joueur
     * @param x         abscisse du premier slot (coin haut gauche de l'objet)
     * @param y         ordonnée de la première rangée ; la barre d'action est à
     *                  {@code y + 3 * 18 + 4}
     * @throws IllegalStateException si l'inventaire du joueur a déjà été ajouté
     */
    protected void addPlayerInventory(Inventory inventory, int x, int y) {
        if (playerStart >= 0) {
            throw new IllegalStateException("L'inventaire du joueur est déjà dans ce menu.");
        }
        containerSlots = slots.size();
        playerStart = slots.size();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, 9 + row * 9 + column, x + column * SLOT_SIZE, y + row * SLOT_SIZE));
            }
        }
        int hotbarY = y + 3 * SLOT_SIZE + HOTBAR_GAP;
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, x + column * SLOT_SIZE, hotbarY));
        }
        playerEnd = slots.size();
    }

    /** {@return le nombre de slots du conteneur, ou {@code -1} tant que l'inventaire du joueur n'est pas ajouté} */
    public int getContainerSlotCount() {
        return containerSlots;
    }

    /**
     * Maj+clic : envoie l'objet du conteneur vers l'inventaire du joueur (en commençant par la
     * barre d'action), ou de l'inventaire du joueur vers le conteneur. Même comportement qu'un
     * coffre vanilla. Sans inventaire du joueur dans le menu, ne déplace rien.
     */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (playerStart < 0 || index < 0 || index >= slots.size()) {
            return ItemStack.EMPTY;
        }
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < containerSlots
                ? moveItemStackTo(stack, playerStart, playerEnd, true)
                : moveItemStackTo(stack, 0, containerSlots, false);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }
}
