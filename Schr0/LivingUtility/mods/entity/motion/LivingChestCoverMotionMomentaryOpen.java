package Schr0.LivingUtility.mods.entity.motion;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData.CoverState;

/**
 * 一度だけ開けて閉じる
 */
public class LivingChestCoverMotionMomentaryOpen extends LivingChestCoverMotionOpen {
	private final LivingChestCoverMotionOpen motionOpen;

	public LivingChestCoverMotionMomentaryOpen(EntityLivingChest chast, LivingChestMotionData motionData,
			LivingChestCoverMotionClose motionClose, LivingChestCoverMotionOpen motionOpen) {
		super(chast, motionData, motionClose);
		this.motionOpen = motionOpen;
	}

	@Override
	public void updateMotion() {
		//蓋を開けた通知がキていたら蓋開けモーションへ
		//もしくはロード時に蓋が開いていることになっていた時
		if (motionData.getCoverState() == CoverState.OPENNING || chast.isOpen()) {
			motionData.setCurrentCoverMotion(motionOpen);
		} else {
			int ticks = motionData.addTicks();

			System.out.println(ticks);

			//ticks20以上を以ってアニメーション終了ってことにする
			if (ticks >= LivingChestMotionData.MAX_TICKS * 0.3F) {
				motionData.setCurrentCoverMotion(motionClose);
			}
		}
	}

}
