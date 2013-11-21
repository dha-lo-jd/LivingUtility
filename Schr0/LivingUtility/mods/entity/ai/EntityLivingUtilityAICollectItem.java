package Schr0.LivingUtility.mods.entity.ai;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import Schr0.LivingUtility.mods.entity.EntityLivingUtility;

public class EntityLivingUtilityAICollectItem extends AIBaseEntityLivingUtility {
	private EntityItem theItem;
	private final float speed;
	private final ItemStack HeldItem;

	private final PathNavigate pathfinder;
	private boolean avoidsWater;
	private int counter;
	private int catchCounter;

	private final double searchRange;
	private final double searchHeight;
	private final float canCollectRange;

	private float prev;
	private float lid;

	public EntityLivingUtilityAICollectItem(EntityLivingUtility Utility, float moveSpeed) {
		super(Utility);
		HeldItem = Utility.getHeldItem();
		speed = moveSpeed;
		pathfinder = Utility.getNavigator();
		setMutexBits(2);

		searchRange = 8.0D;
		searchHeight = 1.0D;
		canCollectRange = 1.0F;
	}

	//AIが継続する際の判定
	@Override
	public boolean continueExecuting() {
		//ターゲットが登録されていない場合
		if (theItem == null) {
			return false;
		}

		//ターゲットが無くなっていない場合
		if (!theItem.isEntityAlive()) {
			return false;
		}

		return true;
	}

	//AIが終了する際に呼ばれる処理
	@Override
	public void resetTask() {
		pathfinder.clearPathEntity();
		theUtility.getNavigator().setAvoidsWater(avoidsWater);

		theUtility.setOpen(false);
		theItem = null;
	}

	//AIの始まる判定
	@Override
	public boolean shouldExecute() {
		//ターゲットの初期化
		theItem = null;

		//Listの設定
		List<EntityItem> itemList = (theWorld.getEntitiesWithinAABB(EntityItem.class,
				theUtility.boundingBox.expand(searchRange, searchHeight, searchRange)));

		//8.0 * 1.0 * 8.0の範囲を走査
		for (EntityItem EItem : itemList) {
			ItemStack EItemStack = EItem.getEntityItem().copy();

			if (theItem == null) {
				theItem = EItem;
			} else {
				if (theUtility.getDistanceSqToEntity(EItem) < theUtility.getDistanceSqToEntity(theItem)) {
					theItem = EItem;
				}
			}
		}

		/*
		//8.0 * 1.0 * 8.0の範囲を走査
		for (EntityItem EItem : itemList)
		{
			ItemStack EItemStack = EItem.getEntityItem().copy();

			if (this.theItem == null)
			{
				if (this.theUtility.getHeldItem() == null)
				{
					this.theItem = EItem;
				}
				else
				{
					ItemStack HeldStack = this.theUtility.getHeldItem().copy();

					//持っているアイテムのみ
					if (EItemStack.isItemEqual(HeldStack))
					{
						//NBTTagが存在している場合
						if (EItemStack.hasTagCompound() && HeldStack.hasTagCompound())
						{
							if (HeldStack.stackTagCompound.equals(EItemStack.stackTagCompound))
							{
								this.theItem = EItem;
							}
						}
						else
						{
							this.theItem = EItem;
						}
					}
				}
			}
			else
			{
				if (this.theUtility.getDistanceSqToEntity(EItem) < this.theUtility.getDistanceSqToEntity(this.theItem))
				{
					if (this.theUtility.getHeldItem() == null)
					{
						this.theItem = EItem;
					}
					else
					{
						ItemStack HeldStack = this.theUtility.getHeldItem().copy();

						//持っているアイテムのみ
						if (EItemStack.isItemEqual(HeldStack))
						{
							//NBTTagが存在している場合
							if (EItemStack.hasTagCompound() && HeldStack.hasTagCompound())
							{
								if (HeldStack.stackTagCompound.equals(EItemStack.stackTagCompound))
								{
									this.theItem = EItem;
								}
							}
							else
							{
								this.theItem = EItem;
							}
						}
					}
				}
			}
		}
		*/

		//登録されていない場合
		if (theItem == null) {
			return false;
		}
		//登録されている場合
		else {
			return true;
		}
	}

	//AIが始まった際に呼ばれる処理
	@Override
	public void startExecuting() {
		counter = 0;
		catchCounter = 0;
		avoidsWater = theUtility.getNavigator().getAvoidsWater();
		theUtility.getNavigator().setAvoidsWater(false);
	}

	//AIの処理
	@Override
	public void updateTask() {
		//アイテムを拾う判定
		boolean isCollectItem = false;

		if (!pathfinder.noPath()) {
			theUtility.getLookHelper().setLookPositionWithEntity(theItem, 10.0F, theUtility.getVerticalFaceSpeed());
			catchCounter = catchCounter > 0 ? (catchCounter - 1) : 0;
		} else {
			catchCounter++;
		}

		//ターゲットに近づく
		if (counter == 0) {
			pathfinder.tryMoveToXYZ(theItem.posX, theItem.posY, theItem.posZ, speed);
		}

		//アイテム回収
		if (theUtility.getDistanceToEntity(theItem) < canCollectRange || catchCounter > 60) {
			if (theUtility.addItemStackToInventory(theItem.getEntityItem())) {
				isCollectItem = true;

				if (theItem.getEntityItem().stackSize <= 0) {
					theItem.setDead();
				}
			} else {
				theItem = null;
			}
		}

		//開閉のモーション（独自）
		//		this.theUtility.OpenMotion( isCollectItem );

		//開閉の設定//
		prev = lid;
		float f = 0.4F;//開閉速度 (0.1F)

		if (isCollectItem && lid == 0.0F) {
			//開く
			theUtility.setOpen(true);
			lid++;
		}

		if (!isCollectItem && lid > 0.0F || isCollectItem && lid < 1.0F) {
			float f1 = lid;

			if (isCollectItem) {
				lid += f;
			} else {
				lid -= f;
			}

			if (lid > 1.0F) {
				lid = 1.0F;
			}

			float f2 = 0.5F;

			if (lid < f2 && f1 >= f2) {
				//閉じる
				theUtility.setOpen(false);
			}

			if (lid < 0.0F) {
				lid = 0.0F;
			}
		}

		counter = (counter + 1) % 20;
	}

}
