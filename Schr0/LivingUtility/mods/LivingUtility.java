package Schr0.LivingUtility.mods;

import java.util.logging.Level;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraftforge.common.Configuration;
import Schr0.LivingUtility.mods.creativetab.CreativeTabLUMaterial;
import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.item.ItemLUKey;
import Schr0.LivingUtility.mods.item.ItemLUMaterial;
import Schr0.LivingUtility.mods.item.ItemLUWand;
import Schr0.LivingUtility.mods.proxy.CommonProxy;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.EntityRegistry;

@Mod(modid = "LivingUtility", name = "LivingUtility", version = "1.0")
@NetworkMod(
		clientSideRequired = true,
		serverSideRequired = false)
public class LivingUtility
{
	@SidedProxy(clientSide = "Schr0.LivingUtility.mods.proxy.ClientProxy", serverSide = "Schr0.LivingUtility.mods.proxy.CommonProxy")
	public static CommonProxy			proxy;
	
	@Mod.Instance("LivingUtility")
	public static LivingUtility			instance;
	
	//items
	public static Item					Item_LUMaterial;
	public static Item					Item_LUWand;
	public static Item					Item_LUKey;
	
	//items_id
	public static int					ItemID_LUMaterial;
	public static int					ItemID_LUWand;
	public static int					ItemID_LUKey;
	
	//CreativeTab
	public static final CreativeTabs	LUTabs			= new CreativeTabLUMaterial( "Living Utility Materials" );
	
	//テクスチャのdomain
	public static final String			TextureDomain	= "schr0_livingutility:";
	
	//前処理
	@Mod.EventHandler
	public void PreInit(FMLPreInitializationEvent event)
	{
		Configuration cfg = new Configuration( event.getSuggestedConfigurationFile() );
		try
		{
			cfg.load();
			
			//item
			this.ItemID_LUMaterial = cfg.getItem( "A1 : Utility Materials", 16900 ).getInt();
			this.ItemID_LUWand = cfg.getItem( "A2 : Utility Wand", 16901 ).getInt();
			this.ItemID_LUKey = cfg.getItem( "A3 : Utility Key", 16902 ).getInt();
		}
		catch( Exception e )
		{
			FMLLog.log( Level.SEVERE, e, "ERROR !!" );
		}
		finally
		{
			cfg.save();
			
			//Itemの初期処理（独自）
			this.addItems();
		}
	}
	
	//Itemの初期処理（独自）
	private void addItems()
	{
		int itemid = 256;
		Item_LUMaterial = new ItemLUMaterial( ItemID_LUMaterial - itemid ).setUnlocalizedName( "Item_LUMaterial" ).setCreativeTab( LUTabs );
		Item_LUWand = new ItemLUWand( ItemID_LUWand - itemid ).setUnlocalizedName( "Item_LUWand" ).setCreativeTab( LUTabs );
		Item_LUKey = new ItemLUKey( ItemID_LUKey - itemid ).setUnlocalizedName( "Item_LUKey" ).setCreativeTab( LUTabs );
	}
	
	//中処理
	@Mod.EventHandler
	public void Load(FMLInitializationEvent event)
	{
		//Entityの処理
		this.addEntity();
		
		//クライアントでの処理
		this.proxy.registerClient();
	}
	
	//Entityの処理
	private void addEntity()
	{
		//mod内での同期ID
		int LivingChestID = 0;
		
		//-----registerGlobalEntityIDの引数の解説-----//
		// 1 Entityのclass
		// 2 内部名
		// 3 EntityID [ EntityRegistry.findGlobalUniqueEntityId() ]
		// 4 mobEggの『全体』の色(RGB表記)
		// 5 mobEggの『斑』の色(RGB表記)
		EntityRegistry.registerGlobalEntityID( EntityLivingChest.class, "Chast", EntityRegistry.findGlobalUniqueEntityId(), 0x8f691d, 0x695229 );
		
		//-----registerModEntityの引数の解説-----//
		// 1 Entityのclass
		// 2 内部名
		// 3 mod内での同期ID(※mod内で被らないければOK)
		// 4 @Modのclass(instance)
		// 5 更新可能な距離
		// 6 更新頻度
		// 7 速度情報を持つか否か
		EntityRegistry.registerModEntity( EntityLivingChest.class, "Chast", LivingChestID, this, 250, 1, true );
	}
	
	//後処理
	@Mod.EventHandler
	public void PostInit(FMLPostInitializationEvent event)
	{
		//none
	}
}
