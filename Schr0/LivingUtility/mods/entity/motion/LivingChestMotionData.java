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

	//現在のモーション
	private LivingChestMotion<?> currentCoverMotion;

	public int addTicks() {
		prevTicks = ticks;
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
		System.out.println(currentCoverMotion);
		this.currentCoverMotion = currentCoverMotion;
	}

	public void setPrevTicks(int prevTicks) {
		this.prevTicks = prevTicks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public int subTicks() {
		prevTicks = ticks;
		ticks--;
		ticks = ticks < 0 ? 0 : ticks;
		return ticks;
	}

}
