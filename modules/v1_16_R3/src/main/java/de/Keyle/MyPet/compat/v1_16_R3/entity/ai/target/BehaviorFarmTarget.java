/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2020 Keyle
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

package de.Keyle.MyPet.compat.v1_16_R3.entity.ai.target;

import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.entity.ai.AIGoal;
import de.Keyle.MyPet.api.entity.ai.target.TargetPriority;
import de.Keyle.MyPet.api.skill.skills.Behavior;
import de.Keyle.MyPet.api.skill.skills.Behavior.BehaviorMode;
import de.Keyle.MyPet.api.util.Compat;
import de.Keyle.MyPet.compat.v1_16_R3.entity.EntityMyPet;
import net.minecraft.server.v1_16_R3.EntityLiving;
import net.minecraft.server.v1_16_R3.EntityMonster;
import net.minecraft.server.v1_16_R3.EntityPlayer;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.LivingEntity;

@Compat("v1_16_R3")
public class BehaviorFarmTarget implements AIGoal {

    private final MyPet myPet;
    private final EntityMyPet petEntity;
    private final EntityPlayer petOwnerEntity;
    private EntityLiving target;
    private final float range;

    public BehaviorFarmTarget(EntityMyPet petEntity, float range) {
        this.petEntity = petEntity;
        this.petOwnerEntity = ((CraftPlayer) petEntity.getOwner().getPlayer()).getHandle();
        this.myPet = petEntity.getMyPet();
        this.range = range;
    }

    @Override
    public boolean shouldStart() {
        Behavior behaviorSkill = myPet.getSkills().get(Behavior.class);
        if (!behaviorSkill.isActive() || behaviorSkill.getBehavior() != BehaviorMode.Farm) {
            return false;
        }
        if (myPet.getDamage() <= 0 && myPet.getRangedDamage() <= 0) {
            return false;
        }
        if (!petEntity.canMove()) {
            return false;
        }
        if (petEntity.hasTarget()) {
            return false;
        }

        for (EntityMonster entityMonster : this.petEntity.world.a(EntityMonster.class, this.petOwnerEntity.getBoundingBox().grow(range, range, range))) {
            if (!entityMonster.isAlive() || petEntity.h(entityMonster) > 91) {
                continue;
            }
            if (!MyPetApi.getHookHelper().canHurt(myPet.getOwner().getPlayer(), entityMonster.getBukkitEntity())) {
                continue;
            }
            this.target = entityMonster;
            return true;
        }
        return false;
    }

    @Override
    public boolean shouldFinish() {
        if (!petEntity.canMove()) {
            return true;
        }
        if (!this.petEntity.hasTarget()) {
            return true;
        }
        EntityLiving target = ((CraftLivingEntity) this.petEntity.getMyPetTarget()).getHandle();

        if (!target.isAlive()) {
            return true;
        }
        Behavior behaviorSkill = myPet.getSkills().get(Behavior.class);
        if (behaviorSkill.getBehavior() != BehaviorMode.Farm) {
            return true;
        } else if (myPet.getDamage() <= 0 && myPet.getRangedDamage() <= 0) {
            return true;
        } else if (target.world != petEntity.world) {
            return true;
        } else if (petEntity.h(target) > 400) {
            return true;
        } else return petEntity.h(((CraftPlayer) petEntity.getOwner().getPlayer()).getHandle()) > 600;
    }

    @Override
    public void start() {
        petEntity.setMyPetTarget((LivingEntity) this.target.getBukkitEntity(), TargetPriority.Farm);
    }

    @Override
    public void finish() {
        petEntity.forgetTarget();
        target = null;
    }
}