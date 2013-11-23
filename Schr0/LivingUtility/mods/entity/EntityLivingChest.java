package Schr0.LivingUtility.mods.entity;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIWander;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemSkull;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import Schr0.LivingUtility.mods.LivingUtility;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAIChastFarmer;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAICollectItem;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAIEatVillager;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAIFindChest;
import Schr0.LivingUtility.mods.entity.ai.EntityLivingUtilityAIFollowOwner;

public class EntityLivingChest extends EntityLivingUtility
{
	//蓋の開閉の変数(独自)
	private float							prev;
	private float							lid;

	//蓋の開閉角度の変数(独自)
	private float							prevLidAngle;
	private float							lidAngle;

	//AIの宣言
	//追従				(3)
	//自由行動			(1)
	//アイテム回収		(2)
	//チェストの走査	(2)
	public EntityLivingUtilityAIFollowOwner	aiFollowOwner	= new EntityLivingUtilityAIFollowOwner( this, 1.25F, 2.0F, 2.0F );
	public EntityAIWander					aiWander		= new EntityAIWander( this, 1.25F );
	public EntityLivingUtilityAICollectItem	aiCollectItem	= new EntityLivingUtilityAICollectItem( this, 1.25F );
	public EntityLivingUtilityAIFindChest	aiFindChest		= new EntityLivingUtilityAIFindChest( this, 1.25F );
	//実食！
	public EntityLivingUtilityAIEatVillager	aiEatVillager	= new EntityLivingUtilityAIEatVillager( this );
	//農業！
	public EntityLivingUtilityAIChastFarmer	aiFarmer		= new EntityLivingUtilityAIChastFarmer( this );

	public EntityLivingChest(World par1World)
	{
		super( par1World );
		this.setSize( 0.9F, 1.35F );
		this.getNavigator().setAvoidsWater( true );

		//AIの切り替えの処理(独自)
		if( par1World != null && !par1World.isRemote )
		{
			this.setAITask();
		}
	}

	//内部インベントリの大きさ（abstract独自）
	@Override
	public int getLivingInventrySize()
	{
		return 27;
	}

	//AIの切り替えの処理(abstract独自)
	@Override
	public void setAITask()
	{
		super.setAITask();

		//飼いならし状態の場合
		if( this.isTamed() )
		{
			//手に何も持って『いない』場合
			if( this.getHeldItem() == null )
			{
				//メッセージの出力（独自）
				this.Information( this.getInvName() + " : Follow" );

				// 4 追従
				this.tasks.addTask( 4, aiFollowOwner );
			}
			//手に何か持って『いる』場合
			else
			{
				//『羽根』を持っている場合
				if( this.getHeldItem().isItemEqual( new ItemStack( Item.feather ) ) )
				{
					//メッセージの出力（独自）
					this.Information( this.getInvName() + " : Freedom" );

					// 4 アイテム回収
					// 5 自由行動
					this.tasks.addTask( 4, aiCollectItem );
					this.tasks.addTask( 5, aiWander );
				}

				//『チェスト』を持っている場合
				if( this.getHeldItem().isItemEqual( new ItemStack( Block.chest ) ) )
				{
					//メッセージの出力（独自）
					this.Information( this.getInvName() + " : FindChest" );

					// 4 チェストの走査
					// 5 アイテム回収
					// 6 自由行動
					this.tasks.addTask( 4, aiFindChest );
					this.tasks.addTask( 5, aiCollectItem );
					this.tasks.addTask( 6, aiWander );
				}

				//『クワ』を持っている場合
				if( this.getHeldItem().getItem() instanceof ItemHoe )
				{
					//メッセージの出力（独自）
					this.Information( this.getInvName() + " : Farmer" );

					// 4 農業
					// 5 自由行動
					this.tasks.addTask( 4, aiFarmer );
					this.tasks.addTask( 5, aiWander );
				}

				//『スカル』を持っている場合
				if( this.getHeldItem().getItem() instanceof ItemSkull )
				{
					//メッセージの出力（独自）
					this.Information( this.getInvName() + " : Eat Villager" );

					// 4 村人食い
					// 5 自由行動
					this.tasks.addTask( 4, aiEatVillager );
					this.tasks.addTask( 5, aiWander );
				}
			}
		}
		//野生状態の場合
		else
		{
			// 5 アイテム回収
			// 6 自由行動
			this.tasks.addTask( 5, aiCollectItem );
			this.tasks.addTask( 6, aiWander );
		}
	}

	//蓋の角度（独自）
	//@SideOnly(Side.CLIENT)
	public float getCoverAngle(float par1)
	{
		return ( this.prevLidAngle + ( this.lidAngle - this.prevLidAngle ) * par1 ) * 0.5F * (float) Math.PI;
	}

	//属性の付与
	@Override
	protected void applyEntityAttributes()
	{
		super.applyEntityAttributes();
		this.getEntityAttribute( SharedMonsterAttributes.maxHealth ).setAttribute( 20.0D );
		this.getEntityAttribute( SharedMonsterAttributes.movementSpeed ).setAttribute( 0.25D );
	}

	//足音
	@Override
	protected void playStepSound(int par1, int par2, int par3, int par4)
	{
		this.playSound( "step.wood", 0.25F, 1.0F );
	}

	//被ダメージ時の音声
	@Override
	protected String getHurtSound()
	{
		return "dig.wood";
	}

	//死亡時の音声
	@Override
	protected String getDeathSound()
	{
		return "random.break";
	}

	//インタラクトした際の処理
	@Override
	public boolean interact(EntityPlayer par1EntityPlayer)
	{
		super.interact( par1EntityPlayer );

		//手に持っているアイテム
		ItemStack CurrentItem = par1EntityPlayer.inventory.getCurrentItem();

		//飼い慣らし状態である場合
		if( this.isTamed() )
		{
			//飼い主である場合
			if( par1EntityPlayer.username.equalsIgnoreCase( this.getOwnerName() ) )
			{
				//手に何も持っていない場合
				if( CurrentItem == null )
				{
					//スニーキング状態である場合
					if( par1EntityPlayer.isSneaking() )
					{
						//騎乗の処理（独自）
						this.setMount( par1EntityPlayer );
					}
					//非スニーキング状態の場合
					else
					{
						//クライアントだけの処理
						if( !this.worldObj.isRemote )
						{
							//チェストのGUIを表示
							par1EntityPlayer.displayGUIChest( this );
						}
					}

					//Itemを振る動作
					par1EntityPlayer.swingItem();
					return true;
				}
				//手にワンドを持っている場合
				else if( CurrentItem.itemID == LivingUtility.Item_LUWand.itemID )
				{
					//スニーキング状態である場合
					if( par1EntityPlayer.isSneaking() )
					{
						//お座りの処理（独自）
						this.setSafeSit();
					}
					//非スニーキング状態の場合
					else
					{
						//AIの切り替えの処理(独自)
						this.setAITask();
					}

					//Itemを振る動作
					par1EntityPlayer.swingItem();

					return true;
				}
				//手にマテリアル・キー以外のアイテムを持っている場合
				else if( CurrentItem.itemID != LivingUtility.Item_LUMaterial.itemID && CurrentItem.itemID != LivingUtility.Item_LUKey.itemID )
				{
					//プレーヤーと同じアイテムを所持している場合
					if( this.getHeldItem() != null && this.getHeldItem().isItemEqual( CurrentItem ) )
					{
						//アイテム所持の解除
						this.setCurrentItemOrArmor( 0, null );

						//音を出す
						this.playSE( "random.orb", 1.0F, 1.0F );
					}
					else
					{
						//手に持たせる
						this.setCurrentItemOrArmor( 0, CurrentItem );

						//音を出す
						this.playSE( "random.pop", 1.0F, 1.0F );
					}

					//Itemを振る動作
					par1EntityPlayer.swingItem();
				}

				return super.interact( par1EntityPlayer );
			}
			//飼い主でない場合
			else
			{
				//手にキーを持っている場合
				if( CurrentItem != null && CurrentItem.itemID == LivingUtility.Item_LUKey.itemID && CurrentItem.getItemDamage() == 0 )
				{
					//NBTタグを取得
					NBTTagCompound nbt = CurrentItem.getTagCompound();
					if( nbt == null )
					{
						nbt = new NBTTagCompound();
						CurrentItem.setTagCompound( nbt );
					}

					String OwnerName = nbt.getString( "OwnerName" );

					//オーナーの名前が登録されている場合
					if( OwnerName.length() > 0 && OwnerName.equalsIgnoreCase( this.getOwnerName() ) )
					{
						//クライアントだけの処理
						if( !this.worldObj.isRemote )
						{
							//チェストのGUIを表示
							par1EntityPlayer.displayGUIChest( this );
						}

						return true;
					}
					//オーナーでない場合
					else
					{
						//メッセージの出力（独自）
						this.Information( "You are not the master of " + this.getInvName() );

						//音を出す
						this.playSE( "note.bass", 1.0F, 1.0F );

						return false;
					}
				}

				return super.interact( par1EntityPlayer );
			}
		}
		//飼い慣らし状態でない場合
		else
		{
			//飼い慣らし
			this.setTamed( true );
			this.setOwner( par1EntityPlayer.username );

			//元のBlockのItemStackのset（独自）
			ItemStack block = new ItemStack( Block.chest.blockID, 1, 0 );
			this.setBlockStack( block );

			//メッセージの出力（独自）
			this.Information( this.getInvName() + " : Set Owner : " + par1EntityPlayer.username );

			//音を出す
			this.playSE( "random.pop", 1.0F, 1.0F );

			return super.interact( par1EntityPlayer );
		}
	}

	//Entityのアップデート
	@Override
	public void onUpdate()
	{
		super.onUpdate();

		//何かに乗っている場合
		if( this.isRiding() )
		{
			EntityLivingBase Owner = (EntityLivingBase) this.ridingEntity;

			//Ownerと同様の正面を向く
			this.prevRotationYaw = this.rotationYaw = Owner.rotationYaw;
		}

		//蓋の角度・音声の設定//
		this.prevLidAngle = this.lidAngle;
		float f = 0.2F;//開閉速度 (0.1F)

		if( this.isOpen() && this.lidAngle == 0.0F )
		{
			//音を出す
			this.playSE( "random.chestopen", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F );
			//this.playSE("random.eat", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
		}

		if( !this.isOpen() && this.lidAngle > 0.0F || this.isOpen() && this.lidAngle < 1.0F )
		{
			float f1 = this.lidAngle;

			if( this.isOpen() )
			{
				this.lidAngle += f;
			}
			else
			{
				this.lidAngle -= f;
			}

			if( this.lidAngle > 1.0F )
			{
				this.lidAngle = 1.0F;
			}

			float f2 = 0.5F;

			if( this.lidAngle < f2 && f1 >= f2 )
			{
				//音を出す
				this.playSE( "random.chestclosed", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F );
				//this.playSE("random.burp", 0.5F, this.worldObj.rand.nextFloat() * 0.1F + 0.9F);
			}

			if( this.lidAngle < 0.0F )
			{
				this.lidAngle = 0.0F;
			}
		}
	}

	//生物のアップデート
	@Override
	public void onLivingUpdate()
	{
		super.onLivingUpdate();

		//アイテムを拾う判定
		boolean isCollectItem = false;

		//アイテムの回収//
		if( !this.worldObj.isRemote && !this.dead )
		{
			List list = this.worldObj.getEntitiesWithinAABB( EntityItem.class, this.boundingBox.expand( 0.5D, 0.0D, 0.5D ) );
			Iterator iterator = list.iterator();

			while( iterator.hasNext() )
			{
				EntityItem entityitem = (EntityItem) iterator.next();

				if( !entityitem.isDead && entityitem.getEntityItem() != null )
				{
					ItemStack itemstack = entityitem.getEntityItem();

					if( this.addItemStackToInventory( itemstack ) )
					{
						isCollectItem = true;

						if( itemstack.stackSize <= 0 )
						{
							entityitem.setDead();
						}
					}
					/*
										//何か持っている場合
										if( this.getHeldItem() != null )
										{
											ItemStack HeldStack = this.getHeldItem().copy();

											//持っているアイテムのみ
											if( HeldStack.isItemEqual( itemstack ) )
											{
												//NBTTagが存在している場合
												if( itemstack.hasTagCompound() && HeldStack.hasTagCompound() )
												{
													if( itemstack.stackTagCompound.equals( HeldStack.stackTagCompound ) )
													{
														//インベントリにアイテムを追加（プレイヤー改変）
														if( this.addItemStackToInventory( itemstack ) )
														{
															isCollectItem = true;

															if( itemstack.stackSize <= 0 )
															{
																entityitem.setDead();
															}
														}
													}
												}
												else
												{
													//インベントリにアイテムを追加（プレイヤー改変）
													if( this.addItemStackToInventory( itemstack ) )
													{
														isCollectItem = true;

														if( itemstack.stackSize <= 0 )
														{
															entityitem.setDead();
														}
													}
												}
											}
										}
										//何も持っていない場合
										else
										{
											if( this.addItemStackToInventory( itemstack ) )
											{
												isCollectItem = true;

												if( itemstack.stackSize <= 0 )
												{
													entityitem.setDead();
												}
											}
										}
										*/
				}
			}
		}

		//開閉の設定//
		this.prev = this.lid;
		float f = 0.4F;//開閉速度 (0.1F)

		if( isCollectItem && this.lid == 0.0F )
		{
			//開く
			this.setOpen( true );
			this.lid++;
		}

		if( !isCollectItem && this.lid > 0.0F || isCollectItem && this.lid < 1.0F )
		{
			float f1 = this.lid;

			if( isCollectItem )
			{
				this.lid += f;
			}
			else
			{
				this.lid -= f;
			}

			if( this.lid > 1.0F )
			{
				this.lid = 1.0F;
			}

			float f2 = 0.5F;

			if( this.lid < f2 && f1 >= f2 )
			{
				//閉じる
				this.setOpen( false );
			}

			if( this.lid < 0.0F )
			{
				this.lid = 0.0F;
			}
		}

		//particleを再生させよう!
		if( worldObj.isRemote )
		{
			String particleName = dataWatcher.getWatchableObjectString( 30 );
			if( particleName != null && !particleName.equals( "" ) )
			{
				for( int j = 0; j < 5; ++j )
				{
					Vec3 vec3 = this.worldObj.getWorldVec3Pool().getVecFromPool( ( (double) this.worldObj.rand.nextFloat() - 0.5D ) * 0.1D, Math.random() * 0.1D + 0.1D, 0.0D );
					vec3.rotateAroundX( -this.rotationPitch * (float) Math.PI / 180.0F );
					vec3.rotateAroundY( -this.rotationYaw * (float) Math.PI / 180.0F );
					Vec3 vec31 = this.worldObj.getWorldVec3Pool().getVecFromPool( ( (double) this.worldObj.rand.nextFloat() - 0.5D ) * 0.3D, (double) ( -this.worldObj.rand.nextFloat() ) * 0.6D - 0.3D, 0.6D );
					vec31.rotateAroundX( -this.rotationPitch * (float) Math.PI / 180.0F );
					vec31.rotateAroundY( -this.rotationYaw * (float) Math.PI / 180.0F );
					vec31 = vec31.addVector( this.posX, this.posY + (double) this.getEyeHeight(), this.posZ );
					this.worldObj.spawnParticle( particleName, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord, vec3.yCoord + 0.05D, vec3.zCoord );

				}
			}
		}
	}

	//30 パーティクルストリング同期用
	@Override
	protected void entityInit()
	{
		super.entityInit();
		this.dataWatcher.addObject( 30, "" );
	}

	//蓋の角度をセット
	public void setLidAngle(float lidAngle)
	{
		this.lidAngle = lidAngle;
	}

	//蓋の角度を取得
	public float getLidAngle()
	{
		return this.lidAngle;
	}
}
