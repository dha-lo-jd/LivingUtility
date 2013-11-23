package Schr0.LivingUtility.mods.entity.ai;

import java.util.List;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import Schr0.LivingUtility.mods.entity.EntityLivingUtility;

public class EntityLivingUtilityAICollectItem extends AIBaseEntityLivingUtility
{
	private EntityItem			theItem;
	private final float			speed;
	private final ItemStack		heldItem;
	
	private final PathNavigate	pathfinder;
	private boolean				avoidsWater;
	private int					counter;
	private int					catchCounter;
	
	private final double		searchRange;
	private final double		searchHeight;
	private final float			canCollectRange;
	
	private float				prev;
	private float				lid;
	
	public EntityLivingUtilityAICollectItem(EntityLivingUtility Utility, float moveSpeed)
	{
		super( Utility );
		this.heldItem = Utility.getHeldItem();
		this.speed = moveSpeed;
		this.pathfinder = Utility.getNavigator();
		this.setMutexBits( 2 );
		
		this.searchRange = 8.0D;
		this.searchHeight = 1.0D;
		this.canCollectRange = 1.0F;
	}
	
	//AIの始まる判定
	@Override
	public boolean shouldExecute()
	{
		//ターゲットの初期化
		this.theItem = null;
		
		//Listの設定
		List<EntityItem> itemList = (List<EntityItem>) ( this.theWorld.getEntitiesWithinAABB( EntityItem.class, this.theUtility.boundingBox.expand( searchRange, searchHeight, searchRange ) ) );
		
		//8.0 * 1.0 * 8.0の範囲を走査
		for( EntityItem EItem : itemList )
		{
			ItemStack EItemStack = EItem.getEntityItem().copy();
			
			if( this.theItem == null )
			{
				this.theItem = EItem;
			}
			else
			{
				if( this.theUtility.getDistanceSqToEntity( EItem ) < this.theUtility.getDistanceSqToEntity( this.theItem ) )
				{
					this.theItem = EItem;
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
		if( this.theItem == null )
		{
			return false;
		}
		//登録されている場合
		else
		{
			return true;
		}
	}
	
	//AIが始まった際に呼ばれる処理
	@Override
	public void startExecuting()
	{
		this.counter = 0;
		this.catchCounter = 0;
		this.avoidsWater = this.theUtility.getNavigator().getAvoidsWater();
		this.theUtility.getNavigator().setAvoidsWater( false );
	}
	
	//AIが継続する際の判定
	@Override
	public boolean continueExecuting()
	{
		//ターゲットが登録されていない場合
		if( this.theItem == null )
		{
			return false;
		}
		
		//ターゲットが無くなっていない場合
		if( !this.theItem.isEntityAlive() )
		{
			return false;
		}
		
		return true;
	}
	
	//AIが終了する際に呼ばれる処理
	@Override
	public void resetTask()
	{
		this.pathfinder.clearPathEntity();
		this.theUtility.getNavigator().setAvoidsWater( this.avoidsWater );
		
		this.theUtility.setOpen( false );
		this.theItem = null;
	}
	
	//AIの処理
	@Override
	public void updateTask()
	{
		//アイテムを拾う判定
		boolean isCollectItem = false;
		
		if( !this.pathfinder.noPath() )
		{
			this.theUtility.getLookHelper().setLookPositionWithEntity( this.theItem, 10.0F, (float) this.theUtility.getVerticalFaceSpeed() );
			this.catchCounter = this.catchCounter > 0 ? ( this.catchCounter - 1 ) : 0;
		}
		else
		{
			this.catchCounter++;
		}
		
		//ターゲットに近づく
		if( counter == 0 )
		{
			this.pathfinder.tryMoveToXYZ( theItem.posX, theItem.posY, theItem.posZ, this.speed );
		}
		
		//アイテム回収
		if( this.theUtility.getDistanceToEntity( theItem ) < this.canCollectRange || this.catchCounter > 60 )
		{
			if( this.theUtility.addItemStackToInventory( theItem.getEntityItem() ) )
			{
				isCollectItem = true;
				
				if( this.theItem.getEntityItem().stackSize <= 0 )
				{
					this.theItem.setDead();
				}
			}
			else
			{
				theItem = null;
			}
		}
		
		//開閉の設定//
		this.prev = this.lid;
		float f = 0.4F;//開閉速度 (0.1F)
		
		if( isCollectItem && this.lid == 0.0F )
		{
			//開く
			this.theUtility.setOpen( true );
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
				this.theUtility.setOpen( false );
			}
			
			if( this.lid < 0.0F )
			{
				this.lid = 0.0F;
			}
		}
		
		this.counter = ( this.counter + 1 ) % 20;
	}
	
}
