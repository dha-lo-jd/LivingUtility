package Schr0.LivingUtility.mods.entity.motion;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData.CoverState;

public class LivingChestCoverMotionOpen extends LivingChestCoverMotionBase {
	protected final LivingChestCoverMotionClose motionClose;

	public LivingChestCoverMotionOpen(EntityLivingChest chast, LivingChestMotionData motionData,
			LivingChestCoverMotionClose motionClose) {
		super(chast, motionData);
		this.motionClose = motionClose;
	}

	@Override
	public void updateMotion() {
		//蓋を閉じた通知がキていたら閉じるモーションへ
		if (motionData.getCoverState() == CoverState.CLOSING || !chast.isOpen()) {
			//音を出さない
			//			chast.playSE("random.chestopen", 0.5F, chast.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			motionData.setCurrentCoverMotion(motionClose);
		} else {
			//アニメーション進行
			super.updateMotion();
			motionData.addTicks();
		}
	}

}
