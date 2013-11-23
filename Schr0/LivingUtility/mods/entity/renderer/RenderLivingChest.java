package Schr0.LivingUtility.mods.entity.renderer;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.*;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.*;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import org.lwjgl.opengl.GL11;

import Schr0.LivingUtility.mods.LivingUtility;
import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.model.ModelLivingChest;

public class RenderLivingChest extends RenderLiving
{
	protected ModelLivingChest				modelLivingChestMain;
	
	private static final ResourceLocation	TAMED_TEXTURE	= new ResourceLocation( LivingUtility.TextureDomain + "textures/mobs/nomalchest/tamed.png" );
	private static final ResourceLocation	WILD_TEXTURE	= new ResourceLocation( LivingUtility.TextureDomain + "textures/mobs/nomalchest/wild.png" );
	private static final ResourceLocation	CHEST_TEXTURE	= new ResourceLocation( "textures/entity/chest/normal.png" );
	
	public RenderLivingChest()
	{
		super( new ModelLivingChest(), 0.5F );
		this.setRenderPassModel( new ModelLivingChest() );
		this.modelLivingChestMain = (ModelLivingChest) ( this.mainModel );
	}
	
	//EntityとResourceLocationを関連付け
	@Override
	protected ResourceLocation getEntityTexture(Entity par1Entity)
	{
		return this.getTextures( (EntityLivingChest) par1Entity );
	}
	
	//EntityのResourceLocationをget
	private ResourceLocation getTextures(EntityLivingChest par1Entity)
	{
		//飼い慣らしされている場合
		if( par1Entity.isTamed() )
		{
			return TAMED_TEXTURE;
		}
		else
		{
			return WILD_TEXTURE;
		}
	}
	
	@Override
	public void doRender(Entity par1Entity, double par2, double par4, double par6, float par8, float par9)
	{
		this.renderChast( (EntityLivingChest) par1Entity, par2, par4, par6, par8, par9 );
	}
	
	@Override
	public void doRenderLiving(EntityLiving par1EntityLiving, double par2, double par4, double par6, float par8, float par9)
	{
		this.renderChast( (EntityLivingChest) par1EntityLiving, par2, par4, par6, par8, par9 );
	}
	
	private void renderChast(EntityLivingChest par1Entity, double par2, double par4, double par6, float par8, float par9)
	{
		super.doRenderLiving( par1Entity, par2, par4, par6, par8, par9 );
	}
	
	@Override
	protected int shouldRenderPass(EntityLivingBase par1EntityLivingBase, int par2, float par3)
	{
		return this.renderEntityChastDeco( (EntityLivingChest) par1EntityLivingBase, par2, par3 );
	}
	
	private int renderEntityChastDeco(EntityLivingChest par1Entity, int par2, float par3)
	{
		if( par2 == 0 )
		{
			this.bindTexture( CHEST_TEXTURE );
			return 1;
		}
		else
		{
			return -1;
		}
	}
	
	@Override
	protected void renderEquippedItems(EntityLivingBase par1EntityLivingBase, float par2)
	{
		this.func_130005_c( (EntityLiving) par1EntityLivingBase, par2 );
	}
	
	//Itemを持たせる処理
	private void func_130005_c(EntityLiving par1EntityLiving, float par2)
	{
		float f1 = 1.0F;
		GL11.glColor3f( f1, f1, f1 );
		super.renderEquippedItems( par1EntityLiving, par2 );
		ItemStack itemstack = par1EntityLiving.getHeldItem();
		ItemStack itemstack1 = par1EntityLiving.func_130225_q( 3 );
		float f2;
		
		if( itemstack1 != null )
		{
			GL11.glPushMatrix();
			this.modelLivingChestMain.Rarm.postRender( 0.0625F );
			
			IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer( itemstack1, EQUIPPED );
			boolean is3D = ( customRenderer != null && customRenderer.shouldUseRenderHelper( EQUIPPED, itemstack1, BLOCK_3D ) );
			
			if( itemstack1.getItem() instanceof ItemBlock )
			{
				if( is3D || RenderBlocks.renderItemIn3d( Block.blocksList[itemstack1.itemID].getRenderType() ) )
				{
					f2 = 0.625F;
					GL11.glTranslatef( 0.0F, -0.25F, 0.0F );
					GL11.glRotatef( 90.0F, 0.0F, 1.0F, 0.0F );
					GL11.glScalef( f2, -f2, -f2 );
				}
				
				this.renderManager.itemRenderer.renderItem( par1EntityLiving, itemstack1, 0 );
			}
			else if( itemstack1.getItem().itemID == Item.skull.itemID )
			{
				f2 = 1.0625F;
				GL11.glScalef( f2, -f2, -f2 );
				String s = "";
				
				if( itemstack1.hasTagCompound() && itemstack1.getTagCompound().hasKey( "SkullOwner" ) )
				{
					s = itemstack1.getTagCompound().getString( "SkullOwner" );
				}
				
				TileEntitySkullRenderer.skullRenderer.func_82393_a( -0.5F, 0.0F, -0.5F, 1, 180.0F, itemstack1.getItemDamage(), s );
			}
			
			GL11.glPopMatrix();
		}
		
		if( itemstack != null )
		{
			GL11.glPushMatrix();
			
			if( this.mainModel.isChild )
			{
				f2 = 0.5F;
				GL11.glTranslatef( 0.0F, 0.625F, 0.0F );
				GL11.glRotatef( -20.0F, -1.0F, 0.0F, 0.0F );
				GL11.glScalef( f2, f2, f2 );
			}
			
			this.modelLivingChestMain.Rarm.postRender( 0.0625F );
			GL11.glTranslatef( -0.0625F, 0.4375F, 0.0625F );
			
			IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer( itemstack, EQUIPPED );
			boolean is3D = ( customRenderer != null && customRenderer.shouldUseRenderHelper( EQUIPPED, itemstack, BLOCK_3D ) );
			
			if( itemstack.getItem() instanceof ItemBlock && ( is3D || RenderBlocks.renderItemIn3d( Block.blocksList[itemstack.itemID].getRenderType() ) ) )
			{
				f2 = 0.5F;
				GL11.glTranslatef( 0.0F, 0.1875F, -0.3125F );
				f2 *= 0.75F;
				GL11.glRotatef( 20.0F, 1.0F, 0.0F, 0.0F );
				GL11.glRotatef( 45.0F, 0.0F, 1.0F, 0.0F );
				GL11.glScalef( -f2, -f2, f2 );
			}
			else if( itemstack.itemID == Item.bow.itemID )
			{
				f2 = 0.625F;
				GL11.glTranslatef( 0.0F, 0.125F, 0.3125F );
				GL11.glRotatef( -20.0F, 0.0F, 1.0F, 0.0F );
				GL11.glScalef( f2, -f2, f2 );
				GL11.glRotatef( -100.0F, 1.0F, 0.0F, 0.0F );
				GL11.glRotatef( 45.0F, 0.0F, 1.0F, 0.0F );
			}
			else if( Item.itemsList[itemstack.itemID].isFull3D() )
			{
				f2 = 0.625F;
				
				if( Item.itemsList[itemstack.itemID].shouldRotateAroundWhenRendering() )
				{
					GL11.glRotatef( 180.0F, 0.0F, 0.0F, 1.0F );
					GL11.glTranslatef( 0.0F, -0.125F, 0.0F );
				}
				
				this.func_82422_c();
				GL11.glScalef( f2, -f2, f2 );
				GL11.glRotatef( -100.0F, 1.0F, 0.0F, 0.0F );
				GL11.glRotatef( 45.0F, 0.0F, 1.0F, 0.0F );
			}
			else
			{
				f2 = 0.375F;
				GL11.glTranslatef( 0.25F, 0.1875F, -0.1875F );
				GL11.glScalef( f2, f2, f2 );
				GL11.glRotatef( 60.0F, 0.0F, 0.0F, 1.0F );
				GL11.glRotatef( -90.0F, 1.0F, 0.0F, 0.0F );
				GL11.glRotatef( 20.0F, 0.0F, 0.0F, 1.0F );
			}
			
			this.renderManager.itemRenderer.renderItem( par1EntityLiving, itemstack, 0 );
			
			if( itemstack.getItem().requiresMultipleRenderPasses() )
			{
				for( int x = 1; x < itemstack.getItem().getRenderPasses( itemstack.getItemDamage() ); x++ )
				{
					this.renderManager.itemRenderer.renderItem( par1EntityLiving, itemstack, x );
				}
			}
			
			GL11.glPopMatrix();
		}
	}
	
	//持ち手の角度？
	private void func_82422_c()
	{
		GL11.glTranslatef( 0.0F, 0.0875F, -0.05F );
	}
	
}
