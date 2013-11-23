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
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAILookAtTradePlayer;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAISit;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAITrade;

public abstract class EntityLivingUtility extends EntityGolem implements IInventory
{
	//開閉の変数(独自)
	private float					prev;
	private float					lid;

	//プレイヤー
	private EntityPlayer			thePlayer;

	//内部インベントリのItemstack
	public ItemStack[]				containerItems;

	//元のブロックのItmeStack
	private final ItemStack[]		blockStack	= new ItemStack[ 1 ];

	//お座りのAI（オオカミ改変）
	public EntityLivingUtilityAISit	aiSit		= new EntityLivingUtilityAISit( this );

	public EntityLivingUtility(World par1World)
	{
		super( par1World );

		//インベントリサイズの設定
		if( this.getLivingInventrySize() != 0 )
		{
			this.containerItems = new ItemStack[ this.getLivingInventrySize() ];
		}
	}

	//---------------------独自の処理----------------------//

	//内部インベントリの大きさ（abstract独自）
	public abstract int getLivingInventrySize();

	//AIの切り替えの処理(独自)
	public void setAITask()
	{
		//音を出す
		this.playSE( "random.orb", 1.0F, 1.0F );

		//AIの除去
		for( int i = 0; i < this.tasks.taskEntries.size(); i++ )
		{
			this.tasks.taskEntries.remove( i );
		}

		//基本AIの設定
		// 0 水泳		(4)
		// 1 お座り		(5)
		// 2 取引		(none)
		// 2 取引注視	(none)
		// 3 注視		(2)
		this.tasks.addTask( 0, new EntityAISwimming( this ) );
		this.tasks.addTask( 1, this.aiSit );
		this.tasks.addTask( 2, new EntityLivingUtilityAITrade( this ) );
		this.tasks.addTask( 2, new EntityLivingUtilityAILookAtTradePlayer( this ) );
		this.tasks.addTask( 3, new EntityAIWatchClosest( this, EntityLiving.class, 6.0F, 0.02F ) );
	}

	//お座りの処理（独自）
	public void setSafeSit()
	{
		//音を出す
		this.playSE( "random.pop", 1.0F, 1.0F );

		//クライアントだけの処理
		if( !this.worldObj.isRemote )
		{
			//お座り状態である場合
			if( this.isSitting() )
			{
				//お座り解除
				this.aiSit.setSitting( false );
			}
			//お座り状態でない場合
			else
			{
				//メッセージの出力（独自）
				this.Information( this.getInvName() + " : Sit down" );

				//お座り
				this.aiSit.setSitting( true );
			}
		}

		//ジャンプ解除
		this.isJumping = false;

		//追従Entityの解除
		this.setPathToEntity( (PathEntity) null );
	}

	//騎乗の処理（独自）
	public void setMount(Entity entity)
	{
		//音を出す
		this.playSE( "random.pop", 1.0F, 1.0F );

		//クライアントだけの処理
		if( !this.worldObj.isRemote )
		{
			//降ろす
			if( entity.riddenByEntity == this )
			{
				//メッセージの出力（独自）
				this.Information( this.getInvName() + " : Dismount" );

				this.mountEntity( null );
			}
			//乗せる
			else if( entity.riddenByEntity == null )
			{
				//メッセージの出力（独自）
				this.Information( this.getInvName() + " : Mount" );

				this.mountEntity( entity );
			}
		}
	}

	//元のBlockのItemStackのset（独自）
	public void setBlockStack(ItemStack par2ItemStack)
	{
		this.blockStack[0] = par2ItemStack;
	}

	//元のBlockのItemStackのget（独自）
	public ItemStack getBlockStack()
	{
		return this.blockStack[0];
	}

	//SEの出力（独自）
	public void playSE(String type, float vol, float pitch)
	{
		this.worldObj.playSoundEffect( this.posX, this.posY, this.posZ, type, vol, pitch );
	}

	//メッセージの出力（独自）
	public void Information(String Message)
	{
		if( !this.worldObj.isRemote )
		{
			//オーナーが存在している ＆ EntityPlayerの場合
			if( this.getOwner() != null && this.getOwner() instanceof EntityPlayer )
			{
				//メッセージ
				( (EntityPlayer) this.getOwner() ).addChatMessage( Message );
			}
		}
	}

	//----------------------基本の処理----------------------//

	//乗っている場合の位置
	@Override
	public double getYOffset()
	{
		if( ridingEntity != null )
		{
			if( ridingEntity instanceof EntityPlayer )
			{
				return (double) ( ridingEntity.yOffset - 1.2F );
			}
			else
			{
				return (double) ( ridingEntity.yOffset + 0.15F );
			}
		}

		return super.getYOffset();
	}

	//視線の高さ
	@Override
	public float getEyeHeight()
	{
		return this.height;
	}

	//AIを適用する判定
	@Override
	public boolean isAIEnabled()
	{
		return true;
	}

	//AIの処理
	@Override
	protected void updateAITasks()
	{
		super.updateAITasks();
	}

	//dataWatcherの処理
	//16 : 飼い慣らしの判定
	//17 : オーナー
	//18 : 子供状態
	//19 : 開閉状態
	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject( 16, Byte.valueOf( (byte) 0 ) );
		this.dataWatcher.addObject( 17, "" );
		this.dataWatcher.addObject( 18, Byte.valueOf( (byte) 0 ) );
		this.dataWatcher.addObject( 19, Byte.valueOf( (byte) 0 ) );
	}

	//飼いならしの判定 16
	public boolean isTamed()
	{
		return ( this.dataWatcher.getWatchableObjectByte( 16 ) & 4 ) != 0;
	}

	//飼いならしの処理 16
	public void setTamed(boolean par1)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte( 16 );

		if( par1 )
		{
			this.dataWatcher.updateObject( 16, Byte.valueOf( (byte) ( b0 | 4 ) ) );
		}
		else
		{
			this.dataWatcher.updateObject( 16, Byte.valueOf( (byte) ( b0 & -5 ) ) );
		}

		//AIの切り替えの処理(独自)
		this.setAITask();
	}

	//お座りの判定 16
	public boolean isSitting()
	{
		return ( this.dataWatcher.getWatchableObjectByte( 16 ) & 1 ) != 0;
	}

	//お座りの処理 16
	public void setSitting(boolean par1)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte( 16 );

		if( par1 )
		{
			this.dataWatcher.updateObject( 16, Byte.valueOf( (byte) ( b0 | 1 ) ) );
		}
		else
		{
			this.dataWatcher.updateObject( 16, Byte.valueOf( (byte) ( b0 & -2 ) ) );
		}
	}

	//オーナー設定の処理 17
	public void setOwner(String par1Str)
	{
		this.dataWatcher.updateObject( 17, par1Str );
	}

	//オーナーの生物名称 17
	public String getOwnerName()
	{
		return this.dataWatcher.getWatchableObjectString( 17 );
	}

	//オーナーのEntityLivingBaseをget
	public EntityLivingBase getOwner()
	{
		return this.worldObj.getPlayerEntityByName( this.getOwnerName() );
	}

	//子供状態をSet 18
	public void setChild(boolean par1)
	{
		this.dataWatcher.updateObject( 18, Byte.valueOf( (byte) ( par1 ? 1 : 0 ) ) );

		if( par1 && this.worldObj != null && !this.worldObj.isRemote )
		{
			//サイズの変更
			this.setSize( 0.5F, 0.5F );
		}
	}

	//子供状態の判定
	@Override
	public boolean isChild()
	{
		return this.getDataWatcher().getWatchableObjectByte( 18 ) == 1;
	}

	//開閉の判定 19
	public boolean isOpen()
	{
		return ( this.dataWatcher.getWatchableObjectByte( 19 ) & 4 ) != 0;
	}

	//開閉の処理 19
	public void setOpen(boolean par1)
	{
		byte b0 = this.dataWatcher.getWatchableObjectByte( 19 );

		if( par1 )
		{
			this.dataWatcher.updateObject( 19, Byte.valueOf( (byte) ( b0 | 4 ) ) );
		}
		else
		{
			this.dataWatcher.updateObject( 19, Byte.valueOf( (byte) ( b0 & -5 ) ) );
		}
	}

	//NBTの書き込み
	@Override
	public void writeEntityToNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.writeEntityToNBT( par1NBTTagCompound );

		//内部インベントリの保存（独自）
		this.save();

		//オーナー
		if( this.getOwnerName() == null )
		{
			par1NBTTagCompound.setString( "Owner", "" );
		}
		else
		{
			par1NBTTagCompound.setString( "Owner", this.getOwnerName() );
		}

		//お座り状態
		par1NBTTagCompound.setBoolean( "Sitting", this.isSitting() );

		//子供状態
		if( this.isChild() )
		{
			par1NBTTagCompound.setBoolean( "IsChild", true );
		}

		//開閉状態
		par1NBTTagCompound.setBoolean( "Open", this.isOpen() );

		//元のBlockのItemStack
		NBTTagList par1nbttaglistA = new NBTTagList();
		NBTTagCompound par1nbttaglistB = new NBTTagCompound();
		if( this.blockStack[0] != null )
		{
			this.blockStack[0].writeToNBT( par1nbttaglistB );
		}
		par1nbttaglistA.appendTag( par1nbttaglistB );
		par1NBTTagCompound.setTag( "BlockStack", par1nbttaglistA );

	}

	//NBTの読み込み
	@Override
	public void readEntityFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		super.readEntityFromNBT( par1NBTTagCompound );

		//内部インベントリの読み込み（独自）
		this.load();

		//オーナー
		String OwnerName = par1NBTTagCompound.getString( "Owner" );

		if( OwnerName.length() > 0 )
		{
			this.setOwner( OwnerName );
			this.setTamed( true );
		}

		//お座り状態
		this.aiSit.setSitting( par1NBTTagCompound.getBoolean( "Sitting" ) );
		this.setSitting( par1NBTTagCompound.getBoolean( "Sitting" ) );

		//子供状態
		if( par1NBTTagCompound.getBoolean( "IsChild" ) )
		{
			this.setChild( true );
		}

		//開閉状態
		this.setOpen( par1NBTTagCompound.getBoolean( "Open" ) );

		//元のBlockのItemStack
		NBTTagList par1nbttaglistA;
		if( par1NBTTagCompound.hasKey( "BlockStack" ) )
		{
			par1nbttaglistA = par1NBTTagCompound.getTagList( "BlockStack" );
			this.blockStack[0] = ItemStack.loadItemStackFromNBT( (NBTTagCompound) par1nbttaglistA.tagAt( 0 ) );
		}

		//AIの切り替えの処理(独自)
		this.setAITask();
	}

	//落とすアイテム（複数）
	@Override
	protected void dropFewItems(boolean par1, int par2)
	{
		//元になったブロックをドロップ
		if( this.getBlockStack() != null )
		{
			this.entityDropItem( this.getBlockStack(), 0.5F );
		}

		//----------インベントリの中身をﾄﾞﾛｯﾌﾟ----------//
		for( int i = 0; i < this.getSizeInventory(); ++i )
		{
			ItemStack itemstack = this.getStackInSlot( i );

			if( itemstack != null )
			{
				float f = this.rand.nextFloat() * 0.8F + 0.1F;
				float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
				float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

				while( itemstack.stackSize > 0 )
				{
					int j = this.rand.nextInt( 21 ) + 10;

					if( j > itemstack.stackSize )
					{
						j = itemstack.stackSize;
					}

					itemstack.stackSize -= j;
					EntityItem entityitem = new EntityItem( this.worldObj, this.posX + (double) f, this.posY + (double) f1, this.posZ + (double) f2, new ItemStack( itemstack.itemID, j, itemstack.getItemDamage() ) );

					if( itemstack.hasTagCompound() )
					{
						entityitem.getEntityItem().setTagCompound( (NBTTagCompound) itemstack.getTagCompound().copy() );
					}

					float f3 = 0.05F;
					entityitem.motionX = (double) ( (float) this.rand.nextGaussian() * f3 );
					entityitem.motionY = (double) ( (float) this.rand.nextGaussian() * f3 + 0.2F );
					entityitem.motionZ = (double) ( (float) this.rand.nextGaussian() * f3 );
					this.worldObj.spawnEntityInWorld( entityitem );
				}
			}
		}
		//----------インベントリの中身をﾄﾞﾛｯﾌﾟ----------//
	}

	//攻撃を受けた際の処理
	@Override
	public boolean attackEntityFrom(DamageSource par1DamageSource, float par2)
	{
		//乗っている場合にはダメージ無効
		if( this.isRiding() )
		{
			return false;
		}

		return super.attackEntityFrom( par1DamageSource, par2 );
	}

	//インタラクトした際の処理
	@Override
	public boolean interact(EntityPlayer par1EntityPlayer)
	{
		//Customerのset（独自）
		this.setCustomer( par1EntityPlayer );
		return super.interact( par1EntityPlayer );
	}

	//Customerのset（独自）
	public void setCustomer(EntityPlayer par1EntityPlayer)
	{
		this.thePlayer = par1EntityPlayer;
	}

	//Customerのget（独自）
	public EntityPlayer getCustomer()
	{
		return this.thePlayer;
	}

	//取引をしている間の判定（独自）
	public boolean isTrading()
	{
		return this.thePlayer != null;
	}

	//----------------------内部インベントリの処理----------------------//

	//内部インベントリの読み込み（独自）
	public void load()
	{
		// ItemStackのNBTを取得、空の中身を作成しておく
		NBTTagCompound nbttagcompound = this.getEntityData();
		this.containerItems = new ItemStack[ this.getSizeInventory() ];

		// NBTが無ければ中身は空のままで
		if( nbttagcompound == null )
		{
			return;
		}

		NBTTagList nbttaglist = nbttagcompound.getTagList( "Items" );
		for( int i = 0; i < nbttaglist.tagCount(); i++ )
		{
			NBTTagCompound nbttagcompound1 = (NBTTagCompound) nbttaglist.tagAt( i );
			int j = nbttagcompound1.getByte( "Slot" ) & 0xff;
			if( j >= 0 && j < this.containerItems.length )
			{
				this.containerItems[j] = ItemStack.loadItemStackFromNBT( nbttagcompound1 );
			}
		}
	}

	//内部インベントリの保存（独自）
	public void save()
	{
		NBTTagList nbttaglist = new NBTTagList();
		for( int i = 0; i < this.containerItems.length; i++ )
		{
			if( this.containerItems[i] != null )
			{
				NBTTagCompound nbttagcompound1 = new NBTTagCompound();
				nbttagcompound1.setByte( "Slot", (byte) i );
				this.containerItems[i].writeToNBT( nbttagcompound1 );
				nbttaglist.appendTag( nbttagcompound1 );
			}
		}

		//ItemStackのNBTに中身を保存
		NBTTagCompound nbttagcompound = this.getEntityData();
		if( nbttagcompound == null )
		{
			nbttagcompound = new NBTTagCompound();
		}

		nbttagcompound.setTag( "Items", nbttaglist );
	}

	//内部インベントリが一杯の場合の判定（独自）
	public boolean isFullItemStack()
	{
		//内部インベントリの読み込み（独自）
		this.load();

		return ( this.getFirstEmptyStack() == -1 );
	}

	//インベントリのサイズ
	@Override
	public int getSizeInventory()
	{
		return this.containerItems.length;
	}

	//中身のItemStack
	@Override
	public ItemStack getStackInSlot(int par1)
	{
		return this.containerItems[par1];
	}

	//???
	@Override
	public ItemStack decrStackSize(int par1, int par2)
	{
		if( this.containerItems[par1] != null )
		{
			ItemStack itemstack;

			if( this.containerItems[par1].stackSize <= par2 )
			{
				itemstack = this.containerItems[par1];
				this.containerItems[par1] = null;
				return itemstack;
			}
			else
			{
				itemstack = this.containerItems[par1].splitStack( par2 );

				if( this.containerItems[par1].stackSize == 0 )
				{
					this.containerItems[par1] = null;
				}

				return itemstack;
			}
		}
		else
		{
			return null;
		}
	}

	//Slotから読み込む中身のItemStack
	@Override
	public ItemStack getStackInSlotOnClosing(int par1)
	{
		if( this.containerItems[par1] != null )
		{
			ItemStack itemstack = this.containerItems[par1];
			this.containerItems[par1] = null;
			return itemstack;
		}
		else
		{
			return null;
		}
	}

	//インベントリへの搬入
	@Override
	public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
	{
		this.containerItems[par1] = par2ItemStack;

		if( par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit() )
		{
			par2ItemStack.stackSize = this.getInventoryStackLimit();
		}
	}

	//インベントリの名称
	@Override
	public String getInvName()
	{
		String name = this.getEntityName();

		if( this.hasCustomNameTag() )
		{
			name = this.getCustomNameTag();
		}

		return name;

	}

	//アイテムの名称判定？
	@Override
	public boolean isInvNameLocalized()
	{
		return true;
	}

	//搬入されるItemStackの数
	@Override
	public int getInventoryStackLimit()
	{
		return 64;
	}

	//開く際に呼ばれる
	@Override
	public void openChest()
	{
		//開く
		this.setOpen( true );

		//内部インベントリの読み込み（独自）
		this.load();
	}

	//閉じる際に呼ばれる
	@Override
	public void closeChest()
	{
		//閉じる
		this.setOpen( false );

		//内部インベントリの保存（独自）
		this.save();
	}

	//中身が変化する際に呼ばれる
	@Override
	public void onInventoryChanged()
	{
		//内部インベントリの保存（独自）
		this.save();
	}

	//インベントリを開いているための条件
	@Override
	public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
	{
		if( this.getCustomer() != par1EntityPlayer )
		{
			return false;
		}

		return true;
	}

	//搬入可能なItemStackの判定
	@Override
	public boolean isItemValidForSlot(int par1, ItemStack par2ItemStack)
	{
		return true;
	}

	//最初の空きスロットを取得（プレイヤー改変）
	public int getFirstEmptyStack()
	{
		this.openChest();

		for( int i = 0; i < this.getSizeInventory(); ++i )
		{
			if( this.containerItems[i] == null )
			{
				this.closeChest();
				return i;
			}
		}

		this.closeChest();
		return -1;
	}

	//インベントリにアイテムを追加（インベントリプレイヤー改変）
	public boolean addItemStackToInventory(ItemStack par1ItemStack)
	{
		this.openChest();
		int slot;

		if( par1ItemStack == null )
		{
			this.closeChest();
			return false;
		}
		else if( par1ItemStack.stackSize == 0 )
		{
			this.closeChest();
			return false;
		}
		else
		{
			if( par1ItemStack.isItemDamaged() )
			{
				slot = this.getFirstEmptyStack();

				if( slot >= 0 )
				{
					this.containerItems[slot] = ItemStack.copyItemStack( par1ItemStack );
					par1ItemStack.stackSize = 0;

					this.closeChest();
					return true;
				}
				else
				{
					this.closeChest();
					return false;
				}
			}
			else
			{
				do
				{
					slot = par1ItemStack.stackSize;
					par1ItemStack.stackSize = this.storePartialItemStack( par1ItemStack );
				}
				while( par1ItemStack.stackSize > 0 && par1ItemStack.stackSize < slot );

				this.closeChest();
				return par1ItemStack.stackSize < slot;
			}
		}
	}

	//インベントリにアイテムを格納（1）（インベントリプレイヤー改変）
	private int storePartialItemStack(ItemStack par1ItemStack)
	{
		this.openChest();

		int itemID = par1ItemStack.itemID;
		int size = par1ItemStack.stackSize;
		int slot;

		if( par1ItemStack.getMaxStackSize() == 1 )
		{
			slot = this.getFirstEmptyStack();

			if( slot < 0 )
			{
				this.closeChest();
				return size;
			}
			else
			{
				if( this.containerItems[slot] == null )
				{
					this.containerItems[slot] = ItemStack.copyItemStack( par1ItemStack );
				}

				this.closeChest();
				return 0;
			}
		}
		else
		{
			slot = this.storeItemStack( par1ItemStack );

			if( slot < 0 )
			{
				slot = this.getFirstEmptyStack();
			}

			if( slot < 0 )
			{
				this.closeChest();
				return size;
			}
			else
			{
				if( this.containerItems[slot] == null )
				{
					this.containerItems[slot] = new ItemStack( itemID, 0, par1ItemStack.getItemDamage() );

					if( par1ItemStack.hasTagCompound() )
					{
						this.containerItems[slot].setTagCompound( (NBTTagCompound) par1ItemStack.getTagCompound().copy() );
					}
				}

				int i = size;

				if( size > this.containerItems[slot].getMaxStackSize() - this.containerItems[slot].stackSize )
				{
					i = this.containerItems[slot].getMaxStackSize() - this.containerItems[slot].stackSize;
				}

				if( i > this.getInventoryStackLimit() - this.containerItems[slot].stackSize )
				{
					i = this.getInventoryStackLimit() - this.containerItems[slot].stackSize;
				}

				if( i == 0 )
				{
					this.closeChest();
					return size;
				}
				else
				{
					size -= i;
					this.containerItems[slot].stackSize += i;

					this.closeChest();
					return size;
				}
			}
		}
	}

	//インベントリにアイテムを格納（2）（インベントリプレイヤー改変）
	private int storeItemStack(ItemStack par1ItemStack)
	{
		this.openChest();

		for( int i = 0; i < this.getSizeInventory(); ++i )
		{
			if( this.containerItems[i] != null
					&& this.containerItems[i].itemID == par1ItemStack.itemID
					&& this.containerItems[i].isStackable()
					&& this.containerItems[i].stackSize < this.containerItems[i].getMaxStackSize()
					&& this.containerItems[i].stackSize < this.getInventoryStackLimit()
					&& ( !this.containerItems[i].getHasSubtypes() || this.containerItems[i].getItemDamage() == par1ItemStack.getItemDamage() )
					&& ItemStack.areItemStackTagsEqual( this.containerItems[i], par1ItemStack ) )
			{
				this.closeChest();
				return i;
			}
		}

		this.closeChest();
		return -1;
	}

}
