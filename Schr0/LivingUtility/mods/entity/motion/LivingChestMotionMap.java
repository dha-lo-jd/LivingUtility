package Schr0.LivingUtility.mods.entity.motion;

import java.util.Set;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;

public class LivingChestMotionMap<M> extends
		MotionMap<EntityLivingChest, LivingChestMotionData, M, LivingChestMotion<M>> {

	public LivingChestMotionMap(Class<? extends M> model, Set<? extends LivingChestMotion<M>> motions) {
		super(model, motions);
	}

}
