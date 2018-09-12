/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2018 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.skill.skills;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.skill.UpgradeComputer;
import de.Keyle.MyPet.api.skill.skills.Lightning;
import de.Keyle.MyPet.api.util.locale.Translation;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Random;

public class LightningImpl implements Lightning {

    private static Random random = new Random();

    private MyPet myPet;
    private boolean isStriking = false;
    protected UpgradeComputer<Integer> chance = new UpgradeComputer<>(0);
    protected UpgradeComputer<Number> damage = new UpgradeComputer<>(0);

    public LightningImpl(MyPet myPet) {
        this.myPet = myPet;
    }

    public MyPet getMyPet() {
        return myPet;
    }

    public boolean isActive() {
        return chance.getValue() > 0 && damage.getValue().doubleValue() > 0;
    }

    @Override
    public void reset() {
        damage.removeAllUpgrades();
        chance.removeAllUpgrades();
    }

    public String toPrettyString() {
        return "" + ChatColor.GOLD + chance.getValue() + ChatColor.RESET + "% -> "
                + ChatColor.GOLD + damage.getValue().doubleValue() + ChatColor.RESET + " "
                + Translation.getString("Name.Damage", myPet.getOwner());
    }

    public boolean trigger() {
        return !isStriking && random.nextDouble() <= chance.getValue() / 100.;
    }

    public void apply(LivingEntity target) {
        Player owner = myPet.getOwner().getPlayer();
        isStriking = true;
        Location loc = target.getLocation();
        MyPetApi.getPlatformHelper().strikeLightning(loc, 32);
        for (Entity entity : myPet.getEntity().get().getNearbyEntities(1.5, 1.5, 1.5)) {
            if (entity instanceof LivingEntity && entity != owner) {
                ((LivingEntity) entity).damage(damage.getValue().doubleValue(), myPet.getEntity().get());
            }
        }
        isStriking = false;
    }

    public UpgradeComputer<Integer> getChance() {
        return chance;
    }

    public UpgradeComputer<Number> getDamage() {
        return damage;
    }

    @Override
    public String toString() {
        return "LightningImpl{" +
                "chance=" + chance +
                ", damage=" + damage.getValue().doubleValue() +
                '}';
    }
}