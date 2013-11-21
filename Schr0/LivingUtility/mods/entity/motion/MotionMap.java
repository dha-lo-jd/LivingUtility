package Schr0.LivingUtility.mods.entity.motion;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.minecraft.entity.Entity;

import com.google.common.collect.Maps;

/**
 * モーション制御クラスをマッピングしておくマップ
 */
public class MotionMap<E extends Entity, D extends MotionData<? super E>, M, MO extends Motion<E, D, M>> {
	public class Key {
		private final Class<? extends M> model;
		private final Class<? extends Motion<E, D, ?>> motion;

		public Key(Class<? extends M> model, Class<? extends Motion<E, D, ?>> motion) {
			super();
			this.model = model;
			this.motion = motion;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			@SuppressWarnings("unchecked")
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (model == null) {
				if (other.model != null) {
					return false;
				}
			} else if (!model.equals(other.model)) {
				return false;
			}
			if (motion == null) {
				if (other.motion != null) {
					return false;
				}
			} else if (!motion.equals(other.motion)) {
				return false;
			}
			return true;
		}

		private MotionMap<E, D, M, MO> getOuterType() {
			return MotionMap.this;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((model == null) ? 0 : model.hashCode());
			result = prime * result + ((motion == null) ? 0 : motion.hashCode());
			return result;
		}
	}

	private final Map<Key, MO> map = Maps.newHashMap();

	public MotionMap(Class<? extends M> model, Set<? extends MO> motions) {
		for (MO mo : motions) {
			put(model, mo);
		}
	}

	public boolean containsKey(Key key) {
		return map.containsKey(key);
	}

	public MO get(Class<? extends M> model, Class<? extends MO> motion) {
		return get(new Key(model, motion));
	}

	public MO get(Key key) {
		return map.get(key);
	}

	public MO put(Class<? extends M> model, MO motion) {
		@SuppressWarnings("unchecked")
		Class<? extends Motion<E, D, ?>> motionClass = (Class<? extends Motion<E, D, ?>>) motion.getClass();
		return map.put(new Key(model, motionClass), motion);
	}

	public Collection<MO> values() {
		return map.values();
	}

}
