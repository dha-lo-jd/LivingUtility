package Schr0.LivingUtility.mods.entity;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
import Schr0.LivingUtility.mods.entity.model.ModelLivingChest;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData.CoverState;

import com.google.common.collect.ImmutableMap;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class EntityLivingChest extends EntityLivingUtility {
	private interface HealthUpdateListner {
		void doHandle(EntityLivingChest me);
	}

	//蓋が開けられた時に送信するstate値
	public static final byte ACTION_STATE_OPEN = (byte) 50;
	public static final byte ACTION_STATE_CLOSE = (byte) 51;
	public static final byte ACTION_STATE_PICK_ITEM = (byte) 52;

	public static final Map<Byte, HealthUpdateListner> ACTION_STATE_MAPPING = ImmutableMap
			.<Byte, HealthUpdateListner> builder()//
			.put(ACTION_STATE_OPEN, new HealthUpdateListner() {
				@Override
				public void doHandle(EntityLivingChest me) {
					me.motionData.setCoverState(CoverState.OPENNING);
				}
			})//
			.put(ACTION_STATE_CLOSE, new HealthUpdateListner() {
				@Override
				public void doHandle(EntityLivingChest me) {
					me.motionData.setCoverState(CoverState.CLOSING);
				}
			})//
			.put(ACTION_STATE_PICK_ITEM, new HealthUpdateListner() {
				@Override
				public void doHandle(EntityLivingChest me) {
					me.motionData.setCoverState(CoverState.MOMENTARY_OPENNING);
				}
			})//
			.build();

	//モーション制御用DTO
	private final LivingChestMotionData motionData = new LivingChestMotionData();

	//AIの宣言
	//追従				(3)
	//自由行動			(1)
	//アイテム回収		(2)
	//チェストの走査	(2)
	public EntityLivingUtilityAIFollowOwner AIFollowOwner = new EntityLivingUtilityAIFollowOwner(this, 1.25F, 2.0F,
			2.0F);

	public EntityAIWander AIWander = new EntityAIWander(this, 1.25F);

	public EntityLivingUtilityAICollectItem AICollectItem = new EntityLivingUtilityAICollectItem(this, 1.25F);

	public EntityLivingUtilityAIFindChest AIFindChest = new EntityLivingUtilityAIFindChest(this, 1.25F);

	//実食！
	public EntityLivingUtilityAIEatVillager AIEatVillager = new EntityLivingUtilityAIEatVillager(this);

	//農業！
	public EntityLivingUtilityAIChastFarmer AIFarmer = new EntityLivingUtilityAIChastFarmer(this);

	public EntityLivingChest(World par1World) {
		super(par1World);
		setSize(0.9F, 1.35F);
		getNavigator().setAvoidsWater(true);

		//AIの切り替えの処理(独自)
		if (par1World != null && !par1World.isRemote) {
			setAITask();
		}

		//モデルのモーションセットを取得(今は決め打ちだけど切り替えもできる…かも)
		new ModelLivingChest().setupMotionMappingToMotionData(this, motionData);
	}

	//属性の付与
	@Override
	protected void applyEntityAttributes() {
		super.applyEntityAttributes();
		getEntityAttribute(SharedMonsterAttributes.maxHealth).setAttribute(20.0D);
		getEntityAttribute(SharedMonsterAttributes.movementSpeed).setAttribute(0.25D);
	}

	//30 パーティクルストリング同期用
	@Override
	protected void entityInit() {
		super.entityInit();
		dataWatcher.addObject(30, "");
	}

	//死亡時の音声
	@Override
	protected String getDeathSound() {
		return "random.break";
	}

	//被ダメージ時の音声
	@Override
	protected String getHurtSound() {
		return "dig.wood";
	}

	//内部インベントリの大きさ（abstract独自）
	@Override
	public int getLivingInventrySize() {
		return 27;
	}

	public LivingChestMotionData getMotionData() {
		return motionData;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void handleHealthUpdate(byte par1) {

		if (ACTION_STATE_MAPPING.containsKey(par1)) {
			ACTION_STATE_MAPPING.get(par1).doHandle(this);
		}
		super.handleHealthUpdate(par1);
	}

	//インタラクトした際の処理
	@Override
	public boolean interact(EntityPlayer par1EntityPlayer) {
		super.interact(par1EntityPlayer);

		//手に持っているアイテム
		ItemStack CurrentItem = par1EntityPlayer.inventory.getCurrentItem();

		//飼い慣らし状態である場合
		if (isTamed()) {
			//飼い主である場合
			if (par1EntityPlayer.username.equalsIgnoreCase(getOwnerName())) {
				//手に何も持っていない場合
				if (CurrentItem == null) {
					//スニーキング状態である場合
					if (par1EntityPlayer.isSneaking()) {
						//騎乗の処理（独自）
						setMount(par1EntityPlayer);
					}
					//非スニーキング状態の場合
					else {
						//クライアントだけの処理
						if (!worldObj.isRemote) {
							//チェストのGUIを表示
							par1EntityPlayer.displayGUIChest(this);
						}
					}

					//Itemを振る動作
					par1EntityPlayer.swingItem();
					return true;
				}
				//手にワンドを持っている場合
				else if (CurrentItem.itemID == LivingUtility.Item_LUWand.itemID) {
					//スニーキング状態である場合
					if (par1EntityPlayer.isSneaking()) {
						//お座りの処理（独自）
						setSafeSit();
					}
					//非スニーキング状態の場合
					else {
						//AIの切り替えの処理(独自)
						setAITask();
					}

					//Itemを振る動作
					par1EntityPlayer.swingItem();

					return true;
				}
				//手にマテリアル・キー以外のアイテムを持っている場合
				else if (CurrentItem.itemID != LivingUtility.Item_LUMaterial.itemID
						&& CurrentItem.itemID != LivingUtility.Item_LUKey.itemID) {
					//プレーヤーと同じアイテムを所持している場合
					if (getHeldItem() != null && getHeldItem().isItemEqual(CurrentItem)) {
						//アイテム所持の解除
						setCurrentItemOrArmor(0, null);

						//音を出す
						playSE("random.orb", 1.0F, 1.0F);
					} else {
						//手に持たせる
						setCurrentItemOrArmor(0, CurrentItem);

						//音を出す
						playSE("random.pop", 1.0F, 1.0F);
					}

					//Itemを振る動作
					par1EntityPlayer.swingItem();
				}

				return super.interact(par1EntityPlayer);
			}
			//飼い主でない場合
			else {
				//手にキーを持っている場合
				if (CurrentItem != null && CurrentItem.itemID == LivingUtility.Item_LUKey.itemID
						&& CurrentItem.getItemDamage() == 0) {
					//NBTタグを取得
					NBTTagCompound nbt = CurrentItem.getTagCompound();
					if (nbt == null) {
						nbt = new NBTTagCompound();
						CurrentItem.setTagCompound(nbt);
					}

					String OwnerName = nbt.getString("OwnerName");

					//オーナーの名前が登録されている場合
					if (OwnerName.length() > 0 && OwnerName.equalsIgnoreCase(getOwnerName())) {
						//クライアントだけの処理
						if (!worldObj.isRemote) {
							//チェストのGUIを表示
							par1EntityPlayer.displayGUIChest(this);
						}

						return true;
					}
					//オーナーでない場合
					else {
						//メッセージの出力（独自）
						Information("You are not the master of " + getInvName());

						//音を出す
						playSE("note.bass", 1.0F, 1.0F);

						return false;
					}
				}

				return super.interact(par1EntityPlayer);
			}
		}
		//飼い慣らし状態でない場合
		else {
			//飼い慣らし
			setTamed(true);
			setOwner(par1EntityPlayer.username);

			//元のBlockのItemStackのset（独自）
			ItemStack block = new ItemStack(Block.chest.blockID, 1, 0);
			setBlockStack(block);

			//メッセージの出力（独自）
			Information(getInvName() + " : Set Owner : " + par1EntityPlayer.username);

			//音を出す
			playSE("random.pop", 1.0F, 1.0F);

			return super.interact(par1EntityPlayer);
		}
	}

	@Override
	public void onLivingUpdate() {
		super.onLivingUpdate();

		//アイテムを拾う判定
		boolean isCollectItem = false;

		//アイテムの回収//
		if (!worldObj.isRemote && !dead) {
			List<?> list = worldObj.getEntitiesWithinAABB(EntityItem.class, boundingBox.expand(0.5D, 0.0D, 0.5D));
			Iterator<?> iterator = list.iterator();

			while (iterator.hasNext()) {
				EntityItem entityitem = (EntityItem) iterator.next();

				if (!entityitem.isDead && entityitem.getEntityItem() != null) {
					ItemStack itemstack = entityitem.getEntityItem();

					if (addItemStackToInventory(itemstack)) {
						isCollectItem = true;

						if (itemstack.stackSize <= 0) {
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

			if (isCollectItem) {
				//アイテム回収フラグをｸﾗｲｱﾝﾖに送信
				worldObj.setEntityState(this, ACTION_STATE_PICK_ITEM);
			}
		}

		//particleを再生させよう!
		if (worldObj.isRemote) {
			String particleName = dataWatcher.getWatchableObjectString(30);
			if (particleName != null && !particleName.equals("")) {
				for (int j = 0; j < 5; ++j) {
					Vec3 vec3 = worldObj.getWorldVec3Pool().getVecFromPool((worldObj.rand.nextFloat() - 0.5D) * 0.1D,
							Math.random() * 0.1D + 0.1D, 0.0D);
					vec3.rotateAroundX(-rotationPitch * (float) Math.PI / 180.0F);
					vec3.rotateAroundY(-rotationYaw * (float) Math.PI / 180.0F);
					Vec3 vec31 = worldObj.getWorldVec3Pool().getVecFromPool((worldObj.rand.nextFloat() - 0.5D) * 0.3D,
							(-worldObj.rand.nextFloat()) * 0.6D - 0.3D, 0.6D);
					vec31.rotateAroundX(-rotationPitch * (float) Math.PI / 180.0F);
					vec31.rotateAroundY(-rotationYaw * (float) Math.PI / 180.0F);
					vec31 = vec31.addVector(posX, posY + getEyeHeight(), posZ);
					worldObj.spawnParticle(particleName, vec31.xCoord, vec31.yCoord, vec31.zCoord, vec3.xCoord,
							vec3.yCoord + 0.05D, vec3.zCoord);

				}
			}
		}

		if (worldObj.isRemote && !dead) {
			//モーション更新
			motionData.getCurrentCoverMotion().updateMotion();
		}
	}

	//生物のアップデート
	//Entityのアップデート
	@Override
	public void onUpdate() {
		super.onUpdate();

		//何かに乗っている場合
		if (isRiding()) {
			EntityLivingBase Owner = (EntityLivingBase) ridingEntity;

			//Ownerと同様の正面を向く
			prevRotationYaw = rotationYaw = Owner.rotationYaw;
		}

		if (worldObj.isRemote && !dead) {
			//モーションデータ後処理
			motionData.afterUpdate();
		}
	}

	//足音
	@Override
	protected void playStepSound(int par1, int par2, int par3, int par4) {
		playSound("step.wood", 0.25F, 1.0F);
	}

	//AIの切り替えの処理(abstract独自)
	@Override
	public void setAITask() {
		super.setAITask();

		//飼いならし状態の場合
		if (isTamed()) {
			//手に何も持って『いない』場合
			if (getHeldItem() == null) {
				//メッセージの出力（独自）
				Information(getInvName() + " : Follow");

				// 4 追従
				tasks.addTask(4, AIFollowOwner);
			}
			//手に何か持って『いる』場合
			else {
				//『羽根』を持っている場合
				if (getHeldItem().isItemEqual(new ItemStack(Item.feather))) {
					//メッセージの出力（独自）
					Information(getInvName() + " : Freedom");

					// 4 アイテム回収
					// 5 自由行動
					tasks.addTask(4, AICollectItem);
					tasks.addTask(5, AIWander);
				}

				//『チェスト』を持っている場合
				if (getHeldItem().isItemEqual(new ItemStack(Block.chest))) {
					//メッセージの出力（独自）
					Information(getInvName() + " : FindChest");

					// 4 チェストの走査
					// 5 アイテム回収
					// 6 自由行動
					tasks.addTask(4, AIFindChest);
					tasks.addTask(5, AICollectItem);
					tasks.addTask(6, AIWander);
				}

				//『クワ』を持っている場合
				if (getHeldItem().getItem() instanceof ItemHoe) {
					//メッセージの出力（独自）
					Information(getInvName() + " : Farmer");

					// 4 農業
					// 5 自由行動
					tasks.addTask(4, AIFarmer);
					tasks.addTask(5, AIWander);
				}

				//『スカル』を持っている場合
				if (getHeldItem().getItem() instanceof ItemSkull) {
					//メッセージの出力（独自）
					Information(getInvName() + " : Eat Villager");

					// 4 村人食い
					// 5 自由行動
					tasks.addTask(4, AIEatVillager);
					tasks.addTask(5, AIWander);
				}
			}
		}
		//野生状態の場合
		else {
			// 5 アイテム回収
			// 6 自由行動
			tasks.addTask(5, AICollectItem);
			tasks.addTask(6, AIWander);
		}
	}

}
