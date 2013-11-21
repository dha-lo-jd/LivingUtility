package Schr0.LivingUtility.mods.entity.motion;

import net.minecraft.entity.Entity;

/**
 * えんちちーのモーション制御
 */
public interface Motion<E extends Entity, D extends MotionData<? super E>, M> {
	void motionTo(M model, float renderParticleTicks);

	void updateMotion();
}
