package Schr0.LivingUtility.mods.entity.motion;

import Schr0.LivingUtility.mods.entity.EntityLivingChest;

public class LivingChestMotionData extends BasicMotionDataImpl<EntityLivingChest, LivingChestMotionData> {
	public enum CoverState {
		NONE, OPENNING, CLOSING, MOMENTARY_OPENNING,
	}

	public static final int MAX_TICKS = 8;

	private CoverState coverState;

	private int prevTicks = 0;
	private int ticks = 0;

	// 現在のモーション
	private LivingChestMotion<?> currentCoverMotion;

	public int addTicks() {
		ticks++;
		ticks = ticks > MAX_TICKS ? MAX_TICKS : ticks;
		return ticks;
	}

	public void afterUpdate() {
		setCoverState(CoverState.NONE);
	}

	public CoverState getCoverState() {
		return coverState;
	}

	@Override
	public LivingChestMotion<?> getCurrentCoverMotion() {
		return currentCoverMotion;
	}

	public int getPrevTicks() {
		return prevTicks;
	}

	public int getTicks() {
		return ticks;
	}

	public void setCoverState(CoverState coverState) {
		this.coverState = coverState;
	}

	@Override
	public void setCurrentCoverMotion(LivingChestMotion<?> currentCoverMotion) {
		this.currentCoverMotion = currentCoverMotion;
	}

	public void setPrevTicks(int prevTicks) {
		this.prevTicks = prevTicks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public void startUpdateTicks() {
		prevTicks = ticks;
	}

	public int subTicks() {
		ticks--;
		ticks = ticks < 0 ? 0 : ticks;
		return ticks;
	}

}
