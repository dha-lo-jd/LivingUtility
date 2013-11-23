package Schr0.LivingUtility.mods.entity.ai;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.world.World;
import Schr0.LivingUtility.mods.entity.EntityLivingUtility;

public class EntityLivingUtilityAIFindChest extends AIBaseEntityLivingUtility
{
	private TileEntityChest		targetChest;
	private final float			speed;
	
	private final PathNavigate	pathfinder;
	private boolean				avoidsWater;
	private int					counter;
	private int					catchCounter;
	private final float			canInsertRange;
	
	private int					ChestBlockX;
	private int					ChestBlockY;
	private int					ChestBlockZ;
	
	private float				prev;
	private float				lid;
	
	public EntityLivingUtilityAIFindChest(EntityLivingUtility Utility, float moveSpeed)
	{
		super( Utility );
		this.speed = moveSpeed;
		this.pathfinder = Utility.getNavigator();
		this.setMutexBits( 2 );
		
		this.canInsertRange = 1.0F;
	}
	
	//AIの始まる判定
	@Override
	public boolean shouldExecute()
	{
		//ターゲットの初期化
		this.targetChest = null;
		
		//ターゲットが登録されていない ＆ 近くにチェストがある場合
		if( this.targetChest == null && this.getNearChest() )
		{
			TileEntity Tile = this.theWorld.getBlockTileEntity( this.ChestBlockX, this.ChestBlockY, this.ChestBlockZ );
			
			if( Tile != null && Tile instanceof TileEntityChest )
			{
				TileEntityChest TileChest = (TileEntityChest) Tile;
				
				//対象インベントリにアイテムが搬入できるかの判定（ホッパー）
				if( this.canInsertItems( this.theUtility, TileChest ) )
				{
					this.targetChest = TileChest;
				}
			}
		}
		
		//登録されていない場合
		if( this.targetChest == null )
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
		//インベントリが空きがある ＆ 近くにチェストがない ターゲットが登録されていない場合
		if( !this.getNearChest() || this.targetChest == null )
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
		this.targetChest = null;
	}
	
	//AIの処理
	@Override
	public void updateTask()
	{
		if( !this.pathfinder.noPath() )
		{
			this.theUtility.getLookHelper().setLookPosition( (double) ( this.ChestBlockX + 0.5D ), (double) this.ChestBlockY, (double) ( this.ChestBlockZ + 0.5D ), 10.0F, (float) this.theUtility.getVerticalFaceSpeed() );
			this.catchCounter = this.catchCounter > 0 ? ( this.catchCounter - 1 ) : 0;
		}
		else
		{
			this.catchCounter++;
		}
		
		//チェストに近づく
		if( counter == 0 )
		{
			if( this.canInsertRange < this.theUtility.getDistanceSq( (double) ( this.ChestBlockX + 0.5D ), (double) this.ChestBlockY, (double) ( this.ChestBlockZ + 0.5D ) ) )
			{
				this.theUtility.getNavigator().tryMoveToXYZ( (double) ( this.ChestBlockX + 0.5D ), (double) this.ChestBlockY, (double) ( this.ChestBlockZ + 0.5D ), this.speed );
			}
		}
		
		//ターゲットに収納する判定
		boolean isFindChest = false;
		
		//アイテム収納
		if( this.theUtility.getDistance( (double) ( this.ChestBlockX + 0.5D ), (double) this.ChestBlockY, (double) ( this.ChestBlockZ + 0.5D ) ) < this.canInsertRange || 60 < this.catchCounter )
		{
			//Inventryを開く
			this.theUtility.openChest();
			this.targetChest.openChest();
			
			//自身のインベントリを走査
			for( int i = 0; i < this.theUtility.getSizeInventory(); i++ )
			{
				ItemStack inItem = this.theUtility.getStackInSlot( i );
				
				if( inItem != null )
				{
					ItemStack insertItem = TileEntityHopper.insertStack( this.targetChest, inItem, 0 );
					
					if( insertItem == null || insertItem.stackSize == 0 )
					{
						this.theUtility.containerItems[i] = null;
						isFindChest = true;
					}
				}
			}
			
			//Inventryを閉じる
			this.theUtility.closeChest();
			this.targetChest.closeChest();
			
			//ターゲットを初期化
			this.targetChest = null;
		}
		
		//開閉の設定//
		this.prev = this.lid;
		float f = 0.4F;//開閉速度 (0.1F)
		
		if( isFindChest && this.lid == 0.0F )
		{
			//開く
			this.theUtility.setOpen( true );
			this.lid++;
		}
		
		if( !isFindChest && this.lid > 0.0F || isFindChest && this.lid < 1.0F )
		{
			float f1 = this.lid;
			
			if( isFindChest )
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
	
	//近くのチェストの走査（オセロット）
	private boolean getNearChest()
	{
		boolean isWatch = false;
		int Y = (int) this.theUtility.posY;
		
		for( int X = (int) this.theUtility.posX - 8; (double) X < this.theUtility.posX + 8.0D; ++X )
		{
			for( int Z = (int) this.theUtility.posZ - 8; (double) Z < this.theUtility.posZ + 8.0D; ++Z )
			{
				if( this.isTileChest( this.theWorld, X, Y, Z ) && this.theWorld.isAirBlock( X, Y + 1, Z ) )
				{
					if( this.canInsertRange < this.theUtility.getDistanceSq( (double) X, (double) Y, (double) Z ) )
					{
						this.ChestBlockX = X;
						this.ChestBlockY = Y;
						this.ChestBlockZ = Z;
						isWatch = true;
					}
				}
			}
		}
		
		return isWatch;
	}
	
	//チェストの判定（オセロット）
	private boolean isTileChest(World par1World, int par2, int par3, int par4)
	{
		TileEntity Tile = par1World.getBlockTileEntity( par2, par3, par4 );
		
		if( Tile != null && Tile instanceof TileEntityChest )
		{
			TileEntityChest TileChest = (TileEntityChest) Tile;
			
			if( TileChest.numUsingPlayers < 1 )
			{
				return true;
			}
		}
		
		return false;
	}
	
	//対象インベントリにアイテムが搬入できるかの判定（ホッパー）
	private boolean canInsertItems(IInventory par0IInventory, IInventory par1IInventory)
	{
		boolean isEmpty = false;
		
		//Inventryを開ける
		par0IInventory.openChest();
		par1IInventory.openChest();
		
		//自身のインベントリを走査
		for( int i = 0; i < par0IInventory.getSizeInventory(); ++i )
		{
			ItemStack inItem = par0IInventory.getStackInSlot( i );
			
			if( inItem != null )
			{
				//対象のインベントリを走査
				for( int j = 0; j < par1IInventory.getSizeInventory(); ++j )
				{
					ItemStack inChestItem = par1IInventory.getStackInSlot( j );
					
					//面からの搬入可能の判定（ホッパー）
					if( this.canInsertItemToInventory( par1IInventory, inItem, j, 0 ) )
					{
						if( inChestItem == null )
						{
							int max = Math.min( inItem.getMaxStackSize(), par1IInventory.getInventoryStackLimit() );
							if( max >= inItem.stackSize )
							{
								isEmpty = true;
							}
						}
						//ItemStackの判定（ホッパー）
						else if( areItemStacksEqualItem( inChestItem, inItem ) )
						{
							int max = Math.min( inItem.getMaxStackSize(), par1IInventory.getInventoryStackLimit() );
							if( max > inChestItem.stackSize )
							{
								ItemStack inItemcopy = inItem.copy();
								ItemStack inChestItemcopy = inChestItem.copy();
								int l = Math.min( inItemcopy.stackSize, max - inChestItemcopy.stackSize );
								inItemcopy.stackSize -= l;
								inChestItemcopy.stackSize += l;
								isEmpty = l > 0;
							}
						}
					}
				}
			}
		}
		
		//Inventry閉じる
		par0IInventory.closeChest();
		par1IInventory.closeChest();
		
		return isEmpty;
	}
	
	//面からの搬入可能の判定（ホッパー）
	private boolean canInsertItemToInventory(IInventory par0IInventory, ItemStack par1ItemStack, int slot, int side)
	{
		return !par0IInventory.isItemValidForSlot( slot, par1ItemStack ) ? false : !( par0IInventory instanceof ISidedInventory ) || ( (ISidedInventory) par0IInventory ).canInsertItem( slot, par1ItemStack, side );
	}
	
	//ItemStackの判定（ホッパー）
	private boolean areItemStacksEqualItem(ItemStack par0ItemStack, ItemStack par1ItemStack)
	{
		return par0ItemStack.itemID != par1ItemStack.itemID ? false : ( par0ItemStack.getItemDamage() != par1ItemStack.getItemDamage() ? false : ( par0ItemStack.stackSize > par0ItemStack.getMaxStackSize() ? false : ItemStack.areItemStackTagsEqual( par0ItemStack, par1ItemStack ) ) );
	}
	
}
