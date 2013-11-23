package Schr0.LivingUtility.mods.item;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import Schr0.LivingUtility.mods.LivingUtility;
import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.EntityLivingUtility;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemLUWand extends Item
{
	//Iconの配列
	private final String[]	textures	= new String[]
										{
										LivingUtility.TextureDomain + "UtilityWand",
										};
	
	@SideOnly(Side.CLIENT)
	private Icon[]			Icons;
	
	public ItemLUWand(int id)
	{
		super( id );
		this.maxStackSize = 1;
		this.setHasSubtypes( true );
		this.setMaxDamage( 0 );
	}
	
	//内部名の設定
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack)
	{
		int Meta = ( textures.length - 1 );
		int i = MathHelper.clamp_int( par1ItemStack.getItemDamage(), 0, Meta );
		return super.getUnlocalizedName() + "." + "LU_Materials_" + i;
	}
	
	//metaIconsの登録
	@Override
	public Icon getIconFromDamage(int par1)
	{
		int Meta = ( textures.length - 1 );
		int i = MathHelper.clamp_int( par1, 0, Meta );
		return this.Icons[i];
	}
	
	//使用するテクスチャファイル
	@SideOnly(Side.CLIENT)
	@Override
	public void registerIcons(IconRegister par1IconRegister)
	{
		this.Icons = new Icon[ textures.length ];
		
		for( int i = 0; i < textures.length; ++i )
		{
			this.Icons[i] = par1IconRegister.registerIcon( textures[i] );
		}
	}
	
	//クリエイティブに表示するアイテムの設定
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List)
	{
		for( int i = 0; i < textures.length; i++ )
		{
			par3List.add( new ItemStack( par1, 1, i ) );
		}
	}
	
	//引き絞りの長さ
	@Override
	public int getMaxItemUseDuration(ItemStack par1ItemStack)
	{
		return 72000;
	}
	
	//引き絞りのアクション
	@Override
	public EnumAction getItemUseAction(ItemStack par1ItemStack)
	{
		return EnumAction.bow;
	}
	
	//生き物にクリックした場合の判定
	@Override
	public boolean itemInteractionForEntity(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, EntityLivingBase par3EntityLivingBase)
	{
		//EntityLivingUtilityがプレーヤーに乗っている場合
		if( par2EntityPlayer.riddenByEntity != null && par2EntityPlayer.riddenByEntity instanceof EntityLivingUtility )
		{
			EntityLivingUtility LivingUtility = (EntityLivingUtility) par2EntityPlayer.riddenByEntity;
			
			if( par3EntityLivingBase instanceof EntityLivingUtility )
			{
				return false;
			}
			else
			{
				//騎乗の処理（独自）
				LivingUtility.setMount( par3EntityLivingBase );
				return true;
			}
		}
		else
		{
			return super.itemInteractionForEntity( par1ItemStack, par2EntityPlayer, par3EntityLivingBase );
		}
	}
	
	//右クリックの処理
	@Override
	public ItemStack onItemRightClick(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		int mode = par1ItemStack.getItemDamage();
		double posX = par3EntityPlayer.posX;
		double posY = par3EntityPlayer.posY;
		double posZ = par3EntityPlayer.posZ;
		
		//引き絞る
		par3EntityPlayer.setItemInUse( par1ItemStack, this.getMaxItemUseDuration( par1ItemStack ) );
		
		return par1ItemStack;
	}
	
	//使い切った後のItemStack
	@Override
	public ItemStack onEaten(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer)
	{
		return par1ItemStack;
	}
	
	//右クリックを離した際の処理
	@Override
	public void onPlayerStoppedUsing(ItemStack par1ItemStack, World par2World, EntityPlayer par3EntityPlayer, int par4)
	{
		double posX = par3EntityPlayer.posX;
		double posY = par3EntityPlayer.posY;
		double posZ = par3EntityPlayer.posZ;
		
		//32tick以上で離した場合
		if( 32 < this.getMaxItemUseDuration( par1ItemStack ) - par4 )
		{
			//EntityPlayerから 4.0 * 0.5 * 4.0 の範囲
			List list = par2World.getEntitiesWithinAABB( EntityLivingUtility.class, par3EntityPlayer.boundingBox.expand( 4.0D, 0.5D, 4.0D ) );
			Iterator iterator = list.iterator();
			boolean isSwing = false;
			
			while( iterator.hasNext() )
			{
				EntityLivingUtility LivingUtility = (EntityLivingUtility) iterator.next();
				
				//飼いならし状態 ＆ 死んでいない場合
				if( LivingUtility.isTamed() && par3EntityPlayer.username.equalsIgnoreCase( LivingUtility.getOwnerName() ) && !LivingUtility.isDead )
				{
					//AIの切り替えの処理(独自)
					LivingUtility.setAITask();
					isSwing = true;
				}
			}
			
			//isSwingがtrueの場合
			if( isSwing )
			{
				//Itemを振る動作
				par3EntityPlayer.swingItem();
			}
			
		}
	}
	
	//ブロックにインタラクトした際の処理
	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, World par3World, int par4, int par5, int par6, int par7, float par8, float par9, float par10)
	{
		//EntityLivingUtilityがプレーヤーに乗っている場合
		if( par2EntityPlayer.riddenByEntity != null && par2EntityPlayer.riddenByEntity instanceof EntityLivingUtility )
		{
			//面により座標を再設定
			if( par7 == 0 )
				--par5;
			if( par7 == 1 )
				++par5;
			if( par7 == 2 )
				--par6;
			if( par7 == 3 )
				++par6;
			if( par7 == 4 )
				--par4;
			if( par7 == 5 )
				++par4;
			
			if( !par2EntityPlayer.canPlayerEdit( par4, par5, par6, par7, par1ItemStack ) )
			{
				return false;
			}
			//対象が空気ブロックである場合
			else if( par3World.isAirBlock( par4, par5, par6 ) )
			{
				//EntityLivingChestがプレーヤーに乗っている場合
				if( par2EntityPlayer.riddenByEntity instanceof EntityLivingChest )
				{
					EntityLivingChest LivingChest = (EntityLivingChest) par2EntityPlayer.riddenByEntity;
					
					LivingChest.openChest();
					
					//クライアントだけの処理
					if( !par3World.isRemote )
					{
						if( LivingChest.getBlockStack() != null )
						{
							ItemStack block = LivingChest.getBlockStack().copy();
							
							//元のBlockの設置
							par3World.setBlock( par4, par5, par6, block.itemID, block.getItemDamage(), 3 );
						}
						else
						{
							//チェストの設置
							par3World.setBlock( par4, par5, par6, Block.chest.blockID, 0, 3 );
						}
						
						//設置したTileEntityの読み込み
						TileEntity Tile = par3World.getBlockTileEntity( par4, par5, par6 );
						
						//TileEntityChestの場合
						if( Tile != null && Tile instanceof TileEntityChest )
						{
							TileEntityChest TileChest = (TileEntityChest) Tile;
							
							if( TileChest.numUsingPlayers > 0 )
							{
								return false;
							}
							
							//中身がある場合には保持
							int newSize = TileChest.getSizeInventory();
							ItemStack[] chestContents = ObfuscationReflectionHelper.getPrivateValue( TileEntityChest.class, TileChest, 0 );
							System.arraycopy( LivingChest.containerItems, 0, chestContents, 0, Math.min( newSize, LivingChest.containerItems.length ) );
							
							for( int i = 0; i < Math.min( newSize, LivingChest.containerItems.length ); i++ )
							{
								LivingChest.containerItems[i] = null;
							}
							
							TileChest.updateContainingBlockInfo();
							TileChest.checkForAdjacentChests();
							
							//TileEntityの設置
							par3World.setBlockTileEntity( par4, par5, par6, TileChest );
						}
						
						//コアをドロップ
						LivingChest.entityDropItem( new ItemStack( LivingUtility.Item_LUMaterial.itemID, 1, 0 ), 0.5F );
						
						//消滅
						LivingChest.setDead();
					}
					
					//BlockChestを継承している場合
					if( Block.blocksList[par3World.getBlockId( par4, par5, par6 )] instanceof BlockChest )
					{
						Block blockChest = (BlockChest) Block.blocksList[par3World.getBlockId( par4, par5, par6 )];
						
						//向きの修正
						blockChest.onBlockPlacedBy( par3World, par4, par5, par6, par2EntityPlayer, par1ItemStack );
					}
					
					//SEの出力（独自
					this.playSE( par3World, par4, par5, par6, "random.pop", 1.0F, 1.0F );
					
					return true;
				}
			}
		}
		
		return false;
	}
	
	//SEの出力（独自）
	private void playSE(World world, double PosX, double PosY, double PosZ, String type, float vol, float pitch)
	{
		world.playSoundEffect( PosX, PosY, PosZ, type, vol, pitch );
	}
	
	//メッセージの表示（独自）
	private void Message(EntityPlayer player, String messe)
	{
		//クライアントだけの処理
		if( !player.worldObj.isRemote )
		{
			player.addChatMessage( messe );
		}
	}
	
}
