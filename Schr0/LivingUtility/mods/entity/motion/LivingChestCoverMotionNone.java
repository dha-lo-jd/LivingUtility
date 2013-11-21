package Schr0.LivingUtility.mods.entity.motion;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData.CoverState;

public class LivingChestCoverMotionNone extends LivingChestCoverMotionBase {
	private final LivingChestCoverMotionOpen motionOpen;
	private final LivingChestCoverMotionMomentaryOpen motionMomentaryOpen;
	private final LivingChestCoverMotionClose motionClose;

	public LivingChestCoverMotionNone(EntityLivingChest chast, LivingChestMotionData motionData) {
		super(chast, motionData);
		motionClose = new LivingChestCoverMotionClose(chast, motionData, this);
		motionOpen = motionClose.getMotionOpen();
		motionMomentaryOpen = motionClose.getMotionMomentaryOpen();
	}

	public LivingChestCoverMotionClose getMotionClose() {
		return motionClose;
	}

	public LivingChestCoverMotionMomentaryOpen getMotionMomentaryOpen() {
		return motionMomentaryOpen;
	}

	public LivingChestCoverMotionOpen getMotionOpen() {
		return motionOpen;
	}

	@Override
	public void updateMotion() {
		//蓋を開けた通知がキていたら蓋開けモーションへ
		//もしくはロード時に蓋が開いていることになっていた時
		if (motionData.getCoverState() == CoverState.OPENNING || chast.isOpen()) {
			//音を出す
			chast.playSE("random.chestopen", 0.5F, chast.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			motionData.setCurrentCoverMotion(motionOpen);
		} else if (motionData.getCoverState() == CoverState.MOMENTARY_OPENNING) {
			//音を出す
			chast.playSE("random.chestopen", 0.5F, chast.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			motionData.setCurrentCoverMotion(motionMomentaryOpen);
		} else {
			super.updateMotion();
		}
	}
}
