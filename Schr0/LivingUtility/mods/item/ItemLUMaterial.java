package Schr0.LivingUtility.mods.item;

import java.util.List;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.Icon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import Schr0.LivingUtility.mods.LivingUtility;
import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import cpw.mods.fml.common.ObfuscationReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemLUMaterial extends Item
{
	//Iconの配列
	private final String[]	textures	= new String[]
										{
										LivingUtility.TextureDomain + "UtilityCore",
										};
	
	@SideOnly(Side.CLIENT)
	private Icon[]			Icons;
	
	public ItemLUMaterial(int id)
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
	
	//インタラクトの前に呼ばれる処理
	@Override
	public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world, int X, int Y, int Z, int side, float hitX, float hitY, float hitZ)
	{
		//コアの場合
		if( stack.getItemDamage() == 0 )
		{
			//クライントだけの処理
			if( world.isRemote )
				return false;
			
			//TileEntityの読み込み
			TileEntity Tile = world.getBlockTileEntity( X, Y, Z );
			
			//ブロックのIDの読み込み
			int BlockID = world.getBlockId( X, Y, Z );
			
			//ブロックのメタデータの読み込み
			int BlockMeta = world.getBlockMetadata( X, Y, Z );
			
			//TileEntityChestの場合
			if( Tile != null && Tile instanceof TileEntityChest )
			{
				TileEntityChest TileChest = (TileEntityChest) Tile;
				
				if( TileChest.numUsingPlayers > 0 )
				{
					return false;
				}
				
				//EntityLivingを宣言
				EntityLivingChest LivingChest = new EntityLivingChest( world );
				
				//向きの修正
				LivingChest.setLocationAndAngles( X + 0.5D, Y, Z + 0.5D, MathHelper.wrapAngleTo180_float( world.rand.nextFloat() * 360.0F ), 0.0F );
				LivingChest.rotationYawHead = LivingChest.rotationYaw;
				LivingChest.renderYawOffset = LivingChest.rotationYaw;
				
				//元のBlockのItemStackのset（独自）
				ItemStack block = new ItemStack( BlockID, 1, BlockMeta );
				LivingChest.setBlockStack( block );
				
				//沸いた際に呼ばれる
				LivingChest.onSpawnWithEgg( (EntityLivingData) null );
				
				//飼い慣らし
				LivingChest.setTamed( true );
				LivingChest.setOwner( player.username );
				
				//中身の呼び出し
				LivingChest.openChest();
				
				//中身がある場合には保持
				int newSize = LivingChest.getSizeInventory();
				ItemStack[] chestContents = ObfuscationReflectionHelper.getPrivateValue( TileEntityChest.class, TileChest, 0 );
				System.arraycopy( chestContents, 0, LivingChest.containerItems, 0, Math.min( newSize, chestContents.length ) );
				
				for( int i = 0; i < Math.min( newSize, chestContents.length ); i++ )
				{
					chestContents[i] = null;
				}
				
				//中身の保存
				LivingChest.closeChest();
				
				TileChest.updateContainingBlockInfo();
				TileChest.checkForAdjacentChests();
				
				//ブロックの破壊
				world.destroyBlock( X, Y, Z, false );
				
				//子供状態[WIP]
				//LivingChest.setChild( true );
				
				//Chastのスポーン
				world.spawnEntityInWorld( LivingChest );
				
				//スタックを減らす処理
				if( !player.capabilities.isCreativeMode )
				{
					--stack.stackSize;
				}
				
				return true;
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
