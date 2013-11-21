package Schr0.LivingUtility.mods.entity.motion;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData.CoverState;

/**
 * 一度だけ開けて閉じる
 */
public class LivingChestCoverMotionEat extends LivingChestCoverMotionBase {
	private enum State {
		OPENNING, CLOSING,
	}

	private final LivingChestCoverMotionOpen motionOpen;
	private final LivingChestCoverMotionClose motionClose;

	private State state = State.OPENNING;

	private int stage = 0;
	private static final int BITE_AMOUNT = 3;

	public LivingChestCoverMotionEat(EntityLivingChest chast, LivingChestMotionData motionData,
			LivingChestCoverMotionOpen motionOpen, LivingChestCoverMotionClose motionClose) {
		super(chast, motionData);
		this.motionOpen = motionOpen;
		this.motionClose = motionClose;
	}

	private void reset() {
		stage = 0;
		state = State.OPENNING;
	}

	@Override
	public void updateMotion() {
		super.updateMotion();

		int ticks;
		if (state == State.OPENNING) {
			ticks = motionData.addTicks();
			ticks = motionData.addTicks();
			if (ticks >= LivingChestMotionData.MAX_TICKS * 0.8F) {
				state = State.CLOSING;
			}
		} else {
			ticks = motionData.subTicks();
			ticks = motionData.subTicks();
			if (ticks <= 0) {
				state = State.OPENNING;
				stage++;
			}
		}

		if (stage >= BITE_AMOUNT) {
			reset();
			//蓋を開けた通知がキていたら蓋開けモーションへ
			//もしくはロード時に蓋が開いていることになっていた時
			if (motionData.getCoverState() == CoverState.OPENNING || chast.isOpen()) {
				motionData.setCurrentCoverMotion(motionOpen);
			} else {
				motionData.setCurrentCoverMotion(motionClose);
			}
		}
	}

}
