package Schr0.LivingUtility.mods.entity.motion;

import java.util.Map;

import net.minecraft.entity.Entity;

import com.google.common.collect.Maps;

public abstract class BasicMotionDataImpl<E extends Entity, D extends BasicMotionDataImpl<E, D>> implements
		MotionData<E> {

	private final Map<Class<?>, MotionMap<E, D, ?, Motion<E, D, ?>>> motionMap = Maps.newHashMap();

	//現在のモーション
	private LivingChestMotion<?> currentCoverMotion;

	public boolean containsMotionMap(Object model) {
		return motionMap.containsKey(model);
	}

	public LivingChestMotion<?> getCurrentCoverMotion() {
		return currentCoverMotion;
	}

	@SuppressWarnings("unchecked")
	public <M, MO extends Motion<E, D, M>> MO getMotion(Class<? extends M> model,
			Class<? extends Motion<E, D, ?>> motion) {
		MotionMap<E, D, M, Motion<E, D, M>> map = getMotionMap(model);
		return (MO) map.get(map.new Key(model, motion));
	}

	@SuppressWarnings("unchecked")
	public <M, MO extends Motion<E, D, M>> MO getMotion(M model, Motion<E, D, ?> motion) {
		return getMotion((Class<? extends M>) model.getClass(), (Class<? extends Motion<E, D, ?>>) motion.getClass());
	}

	@SuppressWarnings("unchecked")
	public <M, MO extends Motion<E, D, M>> MotionMap<E, D, M, MO> getMotionMap(Class<? extends M> model) {
		return (MotionMap<E, D, M, MO>) motionMap.get(model);
	}

	@SuppressWarnings("unchecked")
	public <M, MO extends Motion<E, D, M>> MotionMap<E, D, M, MO> getMotionMap(M model) {
		return getMotionMap((Class<? extends M>) model.getClass());
	}

	@SuppressWarnings("unchecked")
	public <M, MO extends Motion<E, D, M>> void putMotionMap(M model, MotionMap<E, D, M, MO> value) {
		motionMap.put(model.getClass(), (MotionMap<E, D, ?, Motion<E, D, ?>>) value);
	}

	public void setCurrentCoverMotion(LivingChestMotion<?> currentCoverMotion) {
		this.currentCoverMotion = currentCoverMotion;
	}
}
