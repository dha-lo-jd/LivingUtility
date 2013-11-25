package Schr0.LivingUtility.mods.entity.ai;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import Schr0.LivingUtility.mods.entity.EntityLivingUtility;

public class EntityLivingUtilityAIFollowOwner extends AIBaseEntityLivingUtility {
	private EntityLivingBase theOwner;
	private final float moveSpeed;
	private final PathNavigate pathfinder;
	private int catchCounter;
	private boolean avoidsWater;

	float maxDist;
	float minDist;

	public EntityLivingUtilityAIFollowOwner(EntityLivingUtility LivingUtility, float speed, float min, float max) {
		super(LivingUtility);
		moveSpeed = speed;
		pathfinder = LivingUtility.getNavigator();
		minDist = min;
		maxDist = max;
		setMutexBits(3);
	}

	@Override
	public boolean shouldExecute() {
		EntityLivingBase LivingBase = theUtility.getOwner();

		if (LivingBase == null) {
			return false;
		} else if (theUtility.isSitting()) {
			return false;
		} else if (theUtility.getDistanceSqToEntity(LivingBase) < minDist * minDist) {
			return false;
		} else {
			theOwner = LivingBase;
			return true;
		}
	}

	@Override
	public boolean continueExecuting() {
		return !pathfinder.noPath() && theUtility.getDistanceSqToEntity(theOwner) > maxDist * maxDist && !theUtility.isSitting();
	}

	@Override
	public void startExecuting() {
		catchCounter = 0;
		avoidsWater = theUtility.getNavigator().getAvoidsWater();
		theUtility.getNavigator().setAvoidsWater(false);
	}

	@Override
	public void resetTask() {
		theOwner = null;
		pathfinder.clearPathEntity();
		theUtility.getNavigator().setAvoidsWater(avoidsWater);
	}

	@Override
	public void updateTask() {
		theUtility.getLookHelper().setLookPositionWithEntity(theOwner, 10.0F, theUtility.getVerticalFaceSpeed());

		if (!theUtility.isSitting()) {
			if (--catchCounter <= 0) {
				catchCounter = 10;

				if (!pathfinder.tryMoveToEntityLiving(theOwner, moveSpeed)) {
					if (theUtility.getDistanceSqToEntity(theOwner) >= 144.0D) {
						int x = MathHelper.floor_double(theOwner.posX) - 2;
						int z = MathHelper.floor_double(theOwner.posZ) - 2;
						int y = MathHelper.floor_double(theOwner.boundingBox.minY);

						for (int l = 0; l <= 4; ++l) {
							for (int i1 = 0; i1 <= 4; ++i1) {
								if ((l < 1 || i1 < 1 || l > 3 || i1 > 3) && theWorld.doesBlockHaveSolidTopSurface(x + l, y - 1, z + i1)
										&& !theWorld.isBlockNormalCube(x + l, y, z + i1) && !theWorld.isBlockNormalCube(x + l, y + 1, z + i1)) {
									theUtility.setLocationAndAngles(x + l + 0.5F, y, z + i1 + 0.5F, theUtility.rotationYaw, theUtility.rotationPitch);
									pathfinder.clearPathEntity();
									return;
								}
							}
						}
					}
				}
			}
		}
	}
}
