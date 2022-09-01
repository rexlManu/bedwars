/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package de.rexlmanu.bedwars.utils;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/******************************************************************************************
 *    Urheberrechtshinweis                                                                *
 *    Copyright © Emmanuel Lampe 2019                                                  *
 *    Erstellt: 02.09.2019 / 06:46                                               *
 *                                                                                        *
 *    Alle Inhalte dieses Quelltextes sind urheberrechtlich geschützt.                    *
 *    Das Urheberrecht liegt, soweit nicht ausdrücklich anders gekennzeichnet,            *
 *    bei Emmanuel Lampe. Alle Rechte vorbehalten.                                        *
 *                                                                                        * 
 *    Jede Art der Vervielfältigung, Verbreitung, Vermietung, Verleihung,                 *
 *    öffentlichen Zugänglichmachung oder andere Nutzung                                  *
 *    bedarf der ausdrücklichen, schriftlichen Zustimmung von Emmanuel Lampe.             *
 ******************************************************************************************/

public final class InventoryFill {

    private final Inventory inv;
    private List<Integer> sideSlots = new ArrayList<>();

    public InventoryFill(Inventory inv) {
        this.inv = inv;
    }

    public void fillSidesWithItem(ItemStack item) {
        int size = inv.getSize();
        int rows = size / 9;

        if (rows >= 3) {
            for (int i = 0; i <= 8; i++) {
                this.inv.setItem(i, item);

                sideSlots.add(i);
            }

            for (int s = 8; s < (this.inv.getSize() - 9); s += 9) {
                int lastSlot = s + 1;
                this.inv.setItem(s, item);
                this.inv.setItem(lastSlot, item);

                sideSlots.add(s);
                sideSlots.add(lastSlot);
            }

            for (int lr = (this.inv.getSize() - 9); lr < this.inv.getSize(); lr++) {
                this.inv.setItem(lr, item);

                sideSlots.add(lr);
            }
        }
    }

    public List<Integer> getNonSideSlots() {
        List<Integer> availableSlots = new ArrayList<>();

        for (int i = 0; i < this.inv.getSize(); i++) {
            if (! this.sideSlots.contains(i)) {
                availableSlots.add(i);
            }
        }

        return availableSlots;
    }

}
