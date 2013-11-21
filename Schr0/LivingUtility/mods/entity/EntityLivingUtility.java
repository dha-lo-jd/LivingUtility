package Schr0.LivingUtility.mods.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import Schr0.LivingUtility.mods.LivingUtility;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAILookAtTradePlayer;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAISit;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAITrade;

public abstract class EntityLivingUtility extends EntityGolem implements IInventory {

	//プレイヤー
	private EntityPlayer thePlayer;

	//内部インベントリのItemstack
	public ItemStack[] ContainerItems;

	//元のブロックのItmeStack
	private final ItemStack[] BlockStack = new ItemStack[1];

	//お座りのAI（オオカミ改変）
	public EntityLivingUtilityAISit aiSit = new EntityLivingUtilityAISit(this);

	public EntityLivingUtility(World par1World) {
		super(par1World);

		//インベントリサイズの設定
		if (getLivingInventrySize() != 0) {
			ContainerItems = new ItemStack[getLivingInventrySize()];
		}
	}

	//---------------------独自の処理----------------------//

	//インベントリにアイテムを追加（インベントリプレイヤー改変）
	public boolean addItemStackToInventory(ItemStack par1ItemStack) {
		openChest();
		int slot;

		if (par1ItemStack == null) {
			closeChest();
			return false;
		} else if (par1ItemStack.stackSize == 0) {
			closeChest();
			return false;
		} else {
			if (par1ItemStack.isItemDamaged()) {
				slot = getFirstEmptyStack();

				if (slot >= 0) {
					ContainerItems[slot] = ItemStack.copyItemStack(par1ItemStack);
					par1ItemStack.stackSize = 0;

					closeChest();
					return true;
				} else {
					closeChest();
					return false;
				}
			} else {
				do {
					slot = par1ItemStack.stackSize;
					par1ItemStack.stackSize = storePartialItemStack(par1ItemStack);
				} while (par1ItemStack.stackSize > 0 && par1ItemStack.stackSize < slot);

				closeChest();
				return par1ItemStack.stackSize < slot;
			}
		}
	}

	//攻撃を受けた際の処理
	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2) {
		//乗っている場合にはダメージ無効
		if (isRiding()) {
			return false;
		}

		return super.attackEntityFrom(par1DamageSource, par2);
	}

	//閉じる際に呼ばれる
	@Override
	public void closeChest() {
		//閉じる
		setOpen(false);

		//内部インベントリの保存（独自）
		save();
	}

	//???
	@Override
	public ItemStack decrStackSize(int par1, int par2) {
		if (ContainerItems[par1] != null) {
			ItemStack itemstack;

			if (ContainerItems[par1].stackSize <= par2) {
				itemstack = ContainerItems[par1];
				ContainerItems[par1] = null;
				return itemstack;
			} else {
				itemstack = ContainerItems[par1].splitStack(par2);

				if (ContainerItems[par1].stackSize == 0) {
					ContainerItems[par1] = null;
				}

				return itemstack;
			}
		} else {
			return null;
		}
	}

	//落とすアイテム（複数）
	@Override
	protected void dropFewItems(boolean par1, int par2) {
		//元になったブロックをドロップ
		if (getBlockStack() != null) {
			entityDropItem(getBlockStack(), 0.5F);
		}

		//----------インベントリの中身をﾄﾞﾛｯﾌﾟ----------//
		for (int i = 0; i < getSizeInventory(); ++i) {
			ItemStack itemstack = getStackInSlot(i);

			if (itemstack != null) {
				float f = rand.nextFloat() * 0.8F + 0.1F;
				float f1 = rand.nextFloat() * 0.8F + 0.1F;
				float f2 = rand.nextFloat() * 0.8F + 0.1F;

				while (itemstack.stackSize > 0) {
					int j = rand.nextInt(21) + 10;

					if (j > itemstack.stackSize) {
						j = itemstack.stackSize;
					}

					itemstack.stackSize -= j;
					EntityItem entityitem = new EntityItem(worldObj, posX + f, posY + f1, posZ + f2, new ItemStack(
							itemstack.itemID, j, itemstack.getItemDamage()));

					if (itemstack.hasTagCompound()) {
						entityitem.getEntityItem().setTagCompound((NBTTagCompound) itemstack.getTagCompound().copy());
					}

					float f3 = 0.05F;
					entityitem.motionX = (float) rand.nextGaussian() * f3;
					entityitem.motionY = (float) rand.nextGaussian() * f3 + 0.2F;
					entityitem.motionZ = (float) rand.nextGaussian() * f3;
					worldObj.spawnEntityInWorld(entityitem);
				}
			}
		}
		//----------インベントリの中身をﾄﾞﾛｯﾌﾟ----------//
	}

	//dataWatcherの処理
	//16 : 飼い慣らしの判定
	//17 : オーナー
	//18 : モード
	//19 : 開閉状態
	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(16, Byte.valueOf((byte) 0));
		dataWatcher.addObject(17, "");
		dataWatcher.addObject(18, new Integer(0));
		dataWatcher.addObject(19, Byte.valueOf((byte) 0));
	}

	//元のBlockのItemStackのget（独自）
	public ItemStack getBlockStack() {
		return BlockStack[0];
	}

	//Customerのget（独自）
	public EntityPlayer getCustomer() {
		return thePlayer;
	}

	//視線の高さ
	@Override
	public float getEyeHeight() {
		return height;
	}

	//----------------------基本の処理----------------------//

	//最初の空きスロットを取得（プレイヤー改変）
	public int getFirstEmptyStack() {
		openChest();

		for (int i = 0; i < getSizeInventory(); ++i) {
			if (ContainerItems[i] == null) {
				closeChest();
				return i;
			}
		}

		closeChest();
		return -1;
	}

	//搬入されるItemStackの数
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	//インベントリの名称
	@Override
	public String getInvName() {
		String name = getEntityName();

		if (hasCustomNameTag()) {
			name = getCustomNameTag();
		}

		return name;

	}

	//内部インベントリの大きさ（abstract独自）
	public abstract int getLivingInventrySize();

	//ModeをGet 18
	public int getMode() {
		return dataWatcher.getWatchableObjectInt(18);
	}

	//オーナーのEntityLivingBaseをget
	public EntityLivingBase getOwner() {
		return worldObj.getPlayerEntityByName(getOwnerName());
	}

	//オーナーの生物名称 17
	public String getOwnerName() {
		return dataWatcher.getWatchableObjectString(17);
	}

	//インベントリのサイズ
	@Override
	public int getSizeInventory() {
		return ContainerItems.length;
	}

	//中身のItemStack
	@Override
	public ItemStack getStackInSlot(int par1) {
		return ContainerItems[par1];
	}

	//Slotから読み込む中身のItemStack
	@Override
	public ItemStack getStackInSlotOnClosing(int par1) {
		if (ContainerItems[par1] != null) {
			ItemStack itemstack = ContainerItems[par1];
			ContainerItems[par1] = null;
			return itemstack;
		} else {
			return null;
		}
	}

	//乗っている場合の位置
	@Override
	public double getYOffset() {
		if (ridingEntity != null) {
			if (ridingEntity instanceof EntityPlayer) {
				return ridingEntity.yOffset - 1.2F;
			} else {
				return ridingEntity.yOffset + 0.15F;
			}
		}

		return super.getYOffset();
	}

	//メッセージの出力（独自）
	public void Information(String Message) {
		if (!worldObj.isRemote) {
			//メッセージ
			LivingUtility.proxy.addMessage(Message);
		}
	}

	//インタラクトした際の処理
	@Override
	public boolean interact(EntityPlayer par1EntityPlayer) {
		//Customerのset（独自）
		setCustomer(par1EntityPlayer);
		return super.interact(par1EntityPlayer);
	}

	//AIを適用する判定
	@Override
	public boolean isAIEnabled() {
		return true;
	}

	//内部インベントリが一杯の場合の判定（独自）
	public boolean isFullItemStack() {
		//内部インベントリの読み込み（独自）
		load();

		return (getFirstEmptyStack() == -1);
	}

	//アイテムの名称判定？
	@Override
	public boolean isInvNameLocalized() {
		return true;
	}

	//搬入可能なItemStackの判定
	@Override
	public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack) {
		return true;
	}

	//開閉の判定 19
	public boolean isOpen() {
		return (dataWatcher.getWatchableObjectByte(19) & 4) != 0;
	}

	//お座りの判定 16
	public boolean isSitting() {
		return (dataWatcher.getWatchableObjectByte(16) & 1) != 0;
	}

	//飼いならしの判定 16
	public boolean isTamed() {
		return (dataWatcher.getWatchableObjectByte(16) & 4) != 0;
	}

	//取引をしている間の判定（独自）
	public boolean isTrading() {
		return thePlayer != null;
	}

	//インベントリを開いているための条件
	@Override
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer) {
		if (getCustomer() != par1EntityPlayer) {
			return false;
		}

		return true;
	}

	//内部インベントリの読み込み（独自）
	public void load() {
		// ItemStackのNBTを取得、空の中身を作成しておく
		NBTTagCompound nbttagcompound = getEntityData();
		ContainerItems = new ItemStack[getSizeInventory()];

		// NBTが無ければ中身は空のままで
		if (nbttagcompound == null) {
			return;
		}

		NBTTagList nbttaglist = nbttagcompound.getTagList("Items");
		for (int i = 0; i < nbttaglist.tagCount(); i++) {
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt(i);
			int j = nbttagcompound1.getByte("Slot") & 0xff;
			if (j >= 0 && j < ContainerItems.length) {
				ContainerItems[j] = ItemStack.loadItemStackFromNBT(nbttagcompound1);
			}
		}
	}

	//中身が変化する際に呼ばれる
	@Override
	public void onInventoryChanged() {
		//内部インベントリの保存（独自）
		save();
	}

	//----------------------内部インベントリの処理----------------------//

	//開く際に呼ばれる
	@Override
	public void openChest() {
		//開く
		setOpen(true);

		//内部インベントリの読み込み（独自）
		load();
	}

	//SEの出力（独自）
	public void playSE(String type, float vol, float pitch) {
		worldObj.playSoundEffect(posX, posY, posZ, type, vol, pitch);
	}

	//NBTの読み込み
	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound) {
		super.readEntityFromNBT(par1NBTTagCompound);

		//内部インベントリの読み込み（独自）
		load();

		//オーナー
		String OwnerName = par1NBTTagCompound.getString("Owner");

		if (OwnerName.length() > 0) {
			setOwner(OwnerName);
			setTamed(true);
		}

		//お座り状態
		aiSit.setSitting(par1NBTTagCompound.getBoolean("Sitting"));
		setSitting(par1NBTTagCompound.getBoolean("Sitting"));

		//Mode
		setMode(par1NBTTagCompound.getInteger("Mode"));

		//開閉状態
		setOpen(par1NBTTagCompound.getBoolean("Open"));

		//元のBlockのItemStack
		NBTTagList par1nbttaglistA;
		if (par1NBTTagCompound.hasKey("BlockStack")) {
			par1nbttaglistA = par1NBTTagCompound.getTagList("BlockStack");
			BlockStack[0] = ItemStack.loadItemStackFromNBT((NBTTagCompound) par1nbttaglistA.tagAt(0));
		}

		//AIの切り替えの処理(独自)
		setAITask();
	}

	//内部インベントリの保存（独自）
	public void save() {
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = 0; i < ContainerItems.length; i++) {
			if (ContainerItems[i] != null) {
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte("Slot", (byte) i);
				ContainerItems[i].writeToNBT(nbttagcompound1);
				nbttaglist.appendTag(nbttagcompound1);
			}
		}

		//ItemStackのNBTに中身を保存
		NBTTagCompound nbttagcompound = getEntityData();
		if (nbttagcompound == null) {
			nbttagcompound = new NBTTagCompound();
		}

		nbttagcompound.setTag("Items", nbttaglist);
	}

	//AIの切り替えの処理(独自)
	public void setAITask() {
		//音を出す
		playSE("random.orb", 1.0F, 1.0F);

		//AIの除去
		for (int i = 0; i < tasks.taskEntries.size(); i++) {
			tasks.taskEntries.remove(i);
		}

		//基本AIの設定
		// 0 水泳		(4)
		// 1 お座り		(5)
		// 2 取引		(none)
		// 2 取引注視	(none)
		// 3 注視		(2)
		tasks.addTask(0, new EntityAISwimming(this));
		tasks.addTask(1, aiSit);
		tasks.addTask(2, new EntityLivingUtilityAITrade(this));
		tasks.addTask(2, new EntityLivingUtilityAILookAtTradePlayer(this));
		tasks.addTask(3, new EntityAIWatchClosest(this, EntityLiving.class, 6.0F, 0.02F));
	}

	//元のBlockのItemStackのset（独自）
	public void setBlockStack(ItemStack par2ItemStack) {
		BlockStack[0] = par2ItemStack;
	}

	//Customerのset（独自）
	public void setCustomer(EntityPlayer par1EntityPlayer) {
		thePlayer = par1EntityPlayer;
	}

	//インベントリへの搬入
	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
		ContainerItems[par1] = par2ItemStack;

		if (par2ItemStack != null && par2ItemStack.stackSize > getInventoryStackLimit()) {
			par2ItemStack.stackSize = getInventoryStackLimit();
		}
	}

	//ModeをSet 18
	public void setMode(int par1) {
		dataWatcher.updateObject(18, Integer.valueOf(par1));
	}

	//騎乗の処理（独自）
	public void setMount(Entity entity) {
		//音を出す
		playSE("random.pop", 1.0F, 1.0F);

		//クライアントだけの処理
		if (!worldObj.isRemote) {
			//降ろす
			if (entity.riddenByEntity == this) {
				//メッセージの出力（独自）
				Information(getInvName() + " : Dismount");

				mountEntity(null);
			}
			//乗せる
			else if (entity.riddenByEntity == null) {
				//メッセージの出力（独自）
				Information(getInvName() + " : Mount");

				mountEntity(entity);
			}
		}
	}

	//開閉の処理 19
	public void setOpen(boolean par1) {
		byte b0 = dataWatcher.getWatchableObjectByte(19);

		if (par1) {
			dataWatcher.updateObject(19, Byte.valueOf((byte) (b0 | 4)));
		} else {
			dataWatcher.updateObject(19, Byte.valueOf((byte) (b0 & -5)));
		}
	}

	//オーナー設定の処理 17
	public void setOwner(String par1Str) {
		dataWatcher.updateObject(17, par1Str);
	}

	//お座りの処理（独自）
	public void setSafeSit() {
		//音を出す
		playSE("random.pop", 1.0F, 1.0F);

		//クライアントだけの処理
		if (!worldObj.isRemote) {
			//お座り状態である場合
			if (isSitting()) {
				//お座り解除
				aiSit.setSitting(false);
			}
			//お座り状態でない場合
			else {
				//メッセージの出力（独自）
				Information(getInvName() + " : Sit down");

				//お座り
				aiSit.setSitting(true);
			}
		}

		//ジャンプ解除
		isJumping = false;

		//追従Entityの解除
		setPathToEntity((PathEntity) null);
	}

	//お座りの処理 16
	public void setSitting(boolean par1) {
		byte b0 = dataWatcher.getWatchableObjectByte(16);

		if (par1) {
			dataWatcher.updateObject(16, Byte.valueOf((byte) (b0 | 1)));
		} else {
			dataWatcher.updateObject(16, Byte.valueOf((byte) (b0 & -2)));
		}
	}

	//飼いならしの処理 16
	public void setTamed(boolean par1) {
		byte b0 = dataWatcher.getWatchableObjectByte(16);

		if (par1) {
			dataWatcher.updateObject(16, Byte.valueOf((byte) (b0 | 4)));
		} else {
			dataWatcher.updateObject(16, Byte.valueOf((byte) (b0 & -5)));
		}

		//AIの切り替えの処理(独自)
		setAITask();
	}

	//インベントリにアイテムを格納（2）（インベントリプレイヤー改変）
	private int storeItemStack(ItemStack par1ItemStack) {
		openChest();

		for (int i = 0; i < getSizeInventory(); ++i) {
			if (ContainerItems[i] != null
					&& ContainerItems[i].itemID == par1ItemStack.itemID
					&& ContainerItems[i].isStackable()
					&& ContainerItems[i].stackSize < ContainerItems[i].getMaxStackSize()
					&& ContainerItems[i].stackSize < getInventoryStackLimit()
					&& (!ContainerItems[i].getHasSubtypes() || ContainerItems[i].getItemDamage() == par1ItemStack
							.getItemDamage()) && ItemStack.areItemStackTagsEqual(ContainerItems[i], par1ItemStack)) {
				closeChest();
				return i;
			}
		}

		closeChest();
		return -1;
	}

	//インベントリにアイテムを格納（1）（インベントリプレイヤー改変）
	private int storePartialItemStack(ItemStack par1ItemStack) {
		openChest();

		int itemID = par1ItemStack.itemID;
		int size = par1ItemStack.stackSize;
		int slot;

		if (par1ItemStack.getMaxStackSize() == 1) {
			slot = getFirstEmptyStack();

			if (slot < 0) {
				closeChest();
				return size;
			} else {
				if (ContainerItems[slot] == null) {
					ContainerItems[slot] = ItemStack.copyItemStack(par1ItemStack);
				}

				closeChest();
				return 0;
			}
		} else {
			slot = storeItemStack(par1ItemStack);

			if (slot < 0) {
				slot = getFirstEmptyStack();
			}

			if (slot < 0) {
				closeChest();
				return size;
			} else {
				if (ContainerItems[slot] == null) {
					ContainerItems[slot] = new ItemStack(itemID, 0, par1ItemStack.getItemDamage());

					if (par1ItemStack.hasTagCompound()) {
						ContainerItems[slot].setTagCompound((NBTTagCompound) par1ItemStack.getTagCompound().copy());
					}
				}

				int i = size;

				if (size > ContainerItems[slot].getMaxStackSize() - ContainerItems[slot].stackSize) {
					i = ContainerItems[slot].getMaxStackSize() - ContainerItems[slot].stackSize;
				}

				if (i > getInventoryStackLimit() - ContainerItems[slot].stackSize) {
					i = getInventoryStackLimit() - ContainerItems[slot].stackSize;
				}

				if (i == 0) {
					closeChest();
					return size;
				} else {
					size -= i;
					ContainerItems[slot].stackSize += i;

					closeChest();
					return size;
				}
			}
		}
	}

	//AIの処理
	@Override
	protected void updateAITasks() {
		super.updateAITasks();
	}

	//NBTの書き込み
	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound) {
		super.writeEntityToNBT(par1NBTTagCompound);

		//内部インベントリの保存（独自）
		save();

		//オーナー
		if (getOwnerName() == null) {
			par1NBTTagCompound.setString("Owner", "");
		} else {
			par1NBTTagCompound.setString("Owner", getOwnerName());
		}

		//お座り状態
		par1NBTTagCompound.setBoolean("Sitting", isSitting());

		//開閉状態
		par1NBTTagCompound.setBoolean("Open", isOpen());

		//Mode
		par1NBTTagCompound.setInteger("Mode", getMode());

		//元のBlockのItemStack
		NBTTagList par1nbttaglistA = new NBTTagList();
		NBTTagCompound par1nbttaglistB = new NBTTagCompound();
		if (BlockStack[0] != null) {
			BlockStack[0].writeToNBT(par1nbttaglistB);
		}
		par1nbttaglistA.appendTag(par1nbttaglistB);
		par1NBTTagCompound.setTag("BlockStack", par1nbttaglistA);

	}

}
