package Schr0.LivingUtility.mods.entity.motion;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData.CoverState;

public class LivingChestCoverMotionClose extends LivingChestCoverMotionBase {
	private final LivingChestCoverMotionNone motionNone;
	private final LivingChestCoverMotionOpen motionOpen;
	private final LivingChestCoverMotionMomentaryOpen motionMomentaryOpen;

	public LivingChestCoverMotionClose(EntityLivingChest chast, LivingChestMotionData motionData,
			LivingChestCoverMotionNone motionNone) {
		super(chast, motionData);
		this.motionNone = motionNone;
		motionOpen = new LivingChestCoverMotionOpen(chast, motionData, this);
		motionMomentaryOpen = new LivingChestCoverMotionMomentaryOpen(chast, motionData, this, motionOpen);
	}

	public LivingChestCoverMotionMomentaryOpen getMotionMomentaryOpen() {
		return motionMomentaryOpen;
	}

	public LivingChestCoverMotionNone getMotionNone() {
		return motionNone;
	}

	public LivingChestCoverMotionOpen getMotionOpen() {
		return motionOpen;
	}

	@Override
	public void updateMotion() {
		//蓋を開けた通知がキていたら進行度を引き継ぎつつ開けるモーションへ
		//もしくはロード時に蓋が開いていることになっていた時
		if (motionData.getCoverState() == CoverState.OPENNING || chast.isOpen()) {
			//音を出さない
			//			chast.playSE("random.chestopen", 0.5F, chast.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			motionMomentaryOpen.motionData.setCurrentCoverMotion(motionOpen);
		} else if (motionData.getCoverState() == CoverState.MOMENTARY_OPENNING) {
			motionMomentaryOpen.motionData.setCurrentCoverMotion(motionMomentaryOpen);
		} else {
			//アニメーション進行
			int ticks = motionData.subTicks();

			//ticks0以下を以ってアニメーション終了ってことにする
			if (ticks <= 0) {
				motionData.setTicks(0);
				//音を出す
				chast.playSE("random.chestclosed", 0.5F, chast.worldObj.rand.nextFloat() * 0.1F + 0.9F);
				motionData.setCurrentCoverMotion(motionNone);
			}
		}
	}

}
