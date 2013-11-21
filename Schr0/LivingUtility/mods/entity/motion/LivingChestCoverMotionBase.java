package Schr0.LivingUtility.mods.entity.motion;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.model.ModelLivingChest;

public abstract class LivingChestCoverMotionBase implements LivingChestMotion<ModelLivingChest> {
	protected final EntityLivingChest chast;
	protected final LivingChestMotionData motionData;

	public LivingChestCoverMotionBase(EntityLivingChest chast, LivingChestMotionData motionData) {
		this.chast = chast;
		this.motionData = motionData;
	}

	@Override
	public void motionTo(ModelLivingChest model, float renderParticleTicks) {
		int prevTicks = motionData.getPrevTicks();
		int ticks = motionData.getTicks();
		float angle = ((prevTicks + (ticks - prevTicks) * renderParticleTicks) / LivingChestMotionData.MAX_TICKS)
				* 0.5F * (float) Math.PI;
		model.Cover.rotateAngleX = -angle;
	}
}
