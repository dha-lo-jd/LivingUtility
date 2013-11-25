package Schr0.LivingUtility.mods.entity.ai;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAvoidEntity;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.EntityLivingUtility;
import Schr0.LivingUtility.mods.entity.model.ModelLivingChest;
import Schr0.LivingUtility.mods.entity.model.ModelLivingChest.MotionFactory;
import Schr0.LivingUtility.mods.entity.motion.LivingChestCoverMotionClose;
import Schr0.LivingUtility.mods.entity.motion.LivingChestCoverMotionEat;
import Schr0.LivingUtility.mods.entity.motion.LivingChestCoverMotionOpen;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotion;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionMap;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class EntityLivingUtilityAIEatVillager extends AIBaseEntityLivingUtility {
	// 捕食開始距離
	private static final float EAT_RANGE = 1F;
	// おっかけ時間制限
	private static final int TIME_LIMIT = 600;
	// ドロップ開始率
	private static final float DROP_RATE = 0.1F;
	// ドロップ連鎖率
	private static final float CHAIN_DROP_RATE = 0.25F;
	// ドロップアイテムマップ
	private static final Multimap<Integer, ItemStack> DROP_MAP = ArrayListMultimap.create();
	static {
		// 同レアリティからはランダムで1種のみ
		DROP_MAP.put(0, new ItemStack(Item.rottenFlesh));
		DROP_MAP.put(0, new ItemStack(Item.silk));
		DROP_MAP.put(1, new ItemStack(Item.leather));
		DROP_MAP.put(1, new ItemStack(Item.rottenFlesh, 2));
		DROP_MAP.put(2, new ItemStack(Item.leather, 2));
		DROP_MAP.put(2, new ItemStack(Item.beefRaw));
		DROP_MAP.put(2, new ItemStack(Item.porkRaw));
		DROP_MAP.put(2, new ItemStack(Item.fishRaw));
		DROP_MAP.put(2, new ItemStack(Item.chickenRaw));
		DROP_MAP.put(3, new ItemStack(Item.goldNugget));
		DROP_MAP.put(3, new ItemStack(Item.redstone, 2));
		DROP_MAP.put(3, new ItemStack(Item.dyePowder, 2, 4));// らぴす
		DROP_MAP.put(4, new ItemStack(Item.ingotIron));
		DROP_MAP.put(4, new ItemStack(Item.goldNugget, 2));
		DROP_MAP.put(4, new ItemStack(Item.redstone, 4));
		DROP_MAP.put(4, new ItemStack(Item.dyePowder, 4, 4));// らぴす
		DROP_MAP.put(4, new ItemStack(Item.glowstone, 2));
		DROP_MAP.put(5, new ItemStack(Item.ingotGold));
		DROP_MAP.put(5, new ItemStack(Item.ingotIron, 2));
		DROP_MAP.put(5, new ItemStack(Item.glowstone, 4));
		DROP_MAP.put(6, new ItemStack(Item.emerald));
		DROP_MAP.put(6, new ItemStack(Item.ingotGold, 2));
		DROP_MAP.put(7, new ItemStack(Item.diamond));
		DROP_MAP.put(7, new ItemStack(Item.emerald, 2));
	}
	// 獲物
	private EntityLiving entity;
	// つかまえた
	private boolean capture;
	// おっかけ時間
	private int time;

	static {
		ModelLivingChest.registMotionFactory(new MotionFactory() {
			@Override
			public LivingChestMotion<ModelLivingChest> create(EntityLivingChest chast, LivingChestMotionData targetMotionData,
					LivingChestMotionMap<ModelLivingChest> map) {
				LivingChestCoverMotionOpen motionOpen = (LivingChestCoverMotionOpen) map.get(map.new Key(ModelLivingChest.class,
						LivingChestCoverMotionOpen.class));
				LivingChestCoverMotionClose motionClose = (LivingChestCoverMotionClose) map.get(map.new Key(ModelLivingChest.class,
						LivingChestCoverMotionClose.class));
				return new LivingChestCoverMotionEat(chast, targetMotionData, motionOpen, motionClose);
			}
		});
	}

	public EntityLivingUtilityAIEatVillager(EntityLivingUtility LivingUtility) {
		super(LivingUtility);
		capture = false;
	}

	@Override
	public boolean continueExecuting() {
		return entity != null && time++ < TIME_LIMIT || capture;
	}

	private void dropChance(int reality) {
		ItemStack is = null;
		if (DROP_MAP.containsKey(reality)) {
			is = getRandomDrop(reality).copy();
		}
		// たまーにポロリもアリかなって
		if (is != null) {
			theUtility.entityDropItem(is, 0.5F);
			// 確率で村人のサイフ(?)からさらなるレアをゲット！
			if (theWorld.rand.nextFloat() < CHAIN_DROP_RATE) {
				dropChance(reality + 1);
			}
		}
	}

	private void eatAction() {
		// ぱこぱこ

		LivingChestMotionData motionData = ((EntityLivingChest) theUtility).getMotionData();
		LivingChestCoverMotionEat motionEat = motionData.getMotion(ModelLivingChest.class, LivingChestCoverMotionEat.class);
		motionData.setCurrentCoverMotion(motionEat);
		// 食べ散らかしはiconcrack_ItemID_Damegeで指定
		theUtility.getDataWatcher().updateObject(30, "iconcrack_363_0");
		theUtility.playSound("random.eat", 0.5F + 0.5F * theWorld.rand.nextInt(2), (theWorld.rand.nextFloat() - theWorld.rand.nextFloat()) * 0.2F + 1.0F);
	}

	private List<Entity> getInRangeEntitys(int rangeX, int rangeY, int rangeZ) {
		return theWorld.getEntitiesWithinAABBExcludingEntity(theUtility, theUtility.boundingBox.expand(rangeX, rangeY, rangeZ));
	}

	private ItemStack getRandomDrop(int reality) {
		return (ItemStack) DROP_MAP.get(reality).toArray()[theWorld.rand.nextInt(DROP_MAP.get(reality).size())];
	}

	@Override
	public void resetTask() {
		// お食事強制終了(ﾁｯ残ねn
		entity = null;
		capture = false;
		theWorld.playSoundAtEntity(theUtility, "random.burp", 1.5F, theWorld.rand.nextFloat() * 0.1F + 0.9F);
		((EntityLivingChest) theUtility).setOpen(false);
		LivingChestMotionData motionData = ((EntityLivingChest) theUtility).getMotionData();
		LivingChestCoverMotionClose motionClose = motionData.getMotion(ModelLivingChest.class, LivingChestCoverMotionClose.class);
		motionData.setCurrentCoverMotion(motionClose);
		// パーティクルを止める
		theUtility.getDataWatcher().updateObject(30, "");
		theUtility.getNavigator().clearPathEntity();
	}

	@Override
	public boolean shouldExecute() {
		entity = null;
		float minDistance = 100;
		// 最短距離の獲物を探そう！
		for (Entity e : getInRangeEntitys(5, 2, 5)) {
			if (e instanceof EntityVillager) {
				if (theUtility.getDistanceToEntity(e) < minDistance) {
					minDistance = theUtility.getDistanceToEntity(e);
					entity = (EntityLiving) e;
				}
			}
		}
		return entity != null;
	}

	@Override
	public void startExecuting() {
		time = 0;
	}

	@Override
	public void updateTask() {
		if (entity != null) {
			theUtility.getNavigator().tryMoveToXYZ(entity.posX, entity.posY, entity.posZ, 2);
			if (!capture) {
				if (theUtility.getDistanceToEntity(entity) < EntityLivingUtilityAIEatVillager.EAT_RANGE) {
					capture = true;
					// 頑張って逃げよう！
					entity.tasks.addTask(0, new EntityAIAvoidEntity((EntityCreature) entity, theUtility.getClass(), 8.0F, 1.2D, 1.2D));
					time = TIME_LIMIT;
				}
			} else {
				entity.attackEntityFrom(DamageSource.magic, 1);
				// もぐもぐ
				eatAction();
				// 剥ぎ取りタイム
				if (theWorld.rand.nextFloat() < DROP_RATE) {
					dropChance(0);
				}
				// お食事終了！
				if (entity.isDead) {
					entity = null;
					capture = false;
					theWorld.playSoundAtEntity(theUtility, "random.burp", 1.5F, theWorld.rand.nextFloat() * 0.1F + 0.9F);
					((EntityLivingChest) theUtility).setOpen(false);
					// パーティクルを止める
					theUtility.getDataWatcher().updateObject(30, "");
				}
			}
		}
	}
}
