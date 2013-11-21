package Schr0.LivingUtility.mods.entity.model;

import java.util.Set;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.MathHelper;
import Schr0.LivingUtility.mods.entity.EntityLivingChest;
import Schr0.LivingUtility.mods.entity.motion.LivingChestCoverMotionNone;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotion;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionData;
import Schr0.LivingUtility.mods.entity.motion.LivingChestMotionMap;

import com.google.common.collect.Sets;

public class ModelLivingChest extends ModelBase {
	public interface MotionFactory {
		LivingChestMotion<ModelLivingChest> create(EntityLivingChest chast, LivingChestMotionData targetMotionData,
				LivingChestMotionMap<ModelLivingChest> map);
	}

	public static boolean registMotionFactory(MotionFactory motionFactory) {
		return motionFactorys.add(motionFactory);
	}

	public ModelRenderer Body;
	public ModelRenderer Rleg;
	public ModelRenderer Lleg;
	public ModelRenderer Rarm;
	public ModelRenderer Larm;

	public ModelRenderer Core;

	public ModelRenderer Cover;

	private static final Set<MotionFactory> motionFactorys = Sets.newLinkedHashSet();

	public ModelLivingChest() {
		textureWidth = 64;
		textureHeight = 64;
		setTextureOffset("Cover.cover1", 0, 0);
		setTextureOffset("Cover.cover2", 0, 0);

		//--------身体--------//
		Body = new ModelRenderer(this, 0, 19);
		Body.addBox(-7F, -4F, -7F, 14, 10, 14);
		Body.setRotationPoint(0F, 9F, 0F);
		setRotation(Body, 0F, 0F, 0F);
		Body.setTextureSize(64, 64);
		Body.mirror = true;
		//--------身体--------//

		//--------コア--------//
		Core = new ModelRenderer(this, 36, 44);
		Core.addBox(-2F, -2F, -1F, 4, 4, 1);
		Core.setTextureSize(64, 64);
		Core.mirror = true;

		//Body.setRotationPoint(0F, 9F, 0F);
		//Core.setRotationPoint(0F, 11F, -7F);
		Core.setRotationPoint(0F, 2F, -7F);

		//setRotation(Body, 0F, 0F, 0F);
		//setRotation(Core, 0F, 0F, -0.7853982F);
		setRotation(Core, 0F, 0F, -0.7853982F);

		Body.addChild(Core);
		//--------コア--------//

		//--------蓋--------//
		Cover = new ModelRenderer(this, "Cover");
		Cover.addBox("cover1", -7F, -5F, -14F, 14, 5, 14);
		Cover.addBox("cover2", -1F, -2F, -15F, 2, 4, 1);
		Cover.mirror = true;

		//Body.setRotationPoint(0F, 9F, 0F);
		//Cover.setRotationPoint(0F, 5F, 7F);
		Cover.setRotationPoint(0F, -4F, 7F);

		//setRotation(Body, 0F, 0F, 0F);
		//setRotation(Cover, 0F, 0F, 0F);
		setRotation(Cover, 0F, 0F, 0F);

		Body.addChild(Cover);
		//--------蓋--------//

		//--------右腕--------//
		Rarm = new ModelRenderer(this, 18, 44);
		Rarm.addBox(-1F, 0F, -1F, 2, 9, 2);
		Rarm.setTextureSize(64, 64);
		Rarm.mirror = true;
		Rarm.setRotationPoint(-7F, 0F, 0F);
		setRotation(Rarm, 0F, 0F, 0F);
		//--------右腕--------//

		//--------左腕--------//
		Larm = new ModelRenderer(this, 27, 44);
		Larm.addBox(-1F, 0F, -1F, 2, 9, 2);
		Larm.setRotationPoint(7F, 9F, 0F);
		setRotation(Larm, 0F, 0F, -0.3665191F);
		Larm.setTextureSize(64, 64);
		Larm.mirror = true;
		//--------左腕--------//

		//--------右足--------//
		Rleg = new ModelRenderer(this, 0, 44);
		Rleg.addBox(-1F, 0F, -1F, 2, 9, 2);
		Rleg.setRotationPoint(-3F, 15F, 0F);
		setRotation(Rleg, 0F, 0F, 0F);
		Rleg.setTextureSize(64, 64);
		Rleg.mirror = true;
		//--------右足--------//

		//--------左足--------//
		Lleg = new ModelRenderer(this, 9, 44);
		Lleg.addBox(-1F, 0F, -1F, 2, 9, 2);
		Lleg.setRotationPoint(3F, 15F, 0F);
		setRotation(Lleg, 0F, 0F, 0F);
		Lleg.setTextureSize(64, 64);
		Lleg.mirror = true;
		//--------左足--------//

	}

	//描画
	@Override
	public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
		super.render(par1Entity, par2, par3, par4, par5, par6, par7);

		//モーション
		setRotationAngles(par2, par3, par4, par5, par6, par7, par1Entity);

		EntityLivingChest Chast = (EntityLivingChest) par1Entity;
		Body.render(par7);
		Rarm.render(par7);
		Larm.render(par7);
		Rleg.render(par7);
		Lleg.render(par7);
	}

	//アニメーション
	@Override
	public void setLivingAnimations(EntityLivingBase par1EntityLivingBase, float par2, float par3, float par4) {
		EntityLivingChest LivingChest = (EntityLivingChest) par1EntityLivingBase;

		LivingChestMotionData motionData = LivingChest.getMotionData();
		LivingChestMotion<ModelLivingChest> mo = motionData.getMotion(this, motionData.getCurrentCoverMotion());

		mo.motionTo(this, par4);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	//モーション
	@Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
			Entity par7Entity) {
		EntityLivingChest LivingChest = (EntityLivingChest) par7Entity;

		//お座りしている or 乗っている場合
		if (LivingChest.isSitting() || LivingChest.isRiding()) {
			//RotationPointを変更
			Body.setRotationPoint(0F, 17F, 0F);
			Larm.setRotationPoint(7F, 17F, 0F);
			Rarm.setRotationPoint(-7F, 17F, 0F);
			Rleg.setRotationPoint(-3F, 23F, 0F);
			Lleg.setRotationPoint(3F, 23F, 0F);

			Rleg.rotateAngleX = -1.570796F;
			Lleg.rotateAngleX = -1.570796F;
			Rarm.rotateAngleX = -0.9424778F;
			Larm.rotateAngleX = -0.9424778F;
		}
		//通常時
		else {
			//RotationPointを変更
			Body.setRotationPoint(0F, 9F, 0F);
			Rarm.setRotationPoint(-7F, 0F, 0F);
			Larm.setRotationPoint(7F, 9F, 0F);
			Rarm.setRotationPoint(-7F, 9F, 0F);
			Rleg.setRotationPoint(-3F, 15F, 0F);
			Lleg.setRotationPoint(3F, 15F, 0F);

			//歩行動作
			Rarm.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float) Math.PI) * 1.4F * par2;
			Larm.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
			Rleg.rotateAngleX = MathHelper.cos(par1 * 0.6662F) * 1.4F * par2;
			Lleg.rotateAngleX = MathHelper.cos(par1 * 0.6662F + (float) Math.PI) * 1.4F * par2;
		}

		//全体の向きの統一
		Body.rotateAngleY = par4 / (180F / (float) Math.PI);
		Body.rotateAngleY = Rarm.rotateAngleY = Larm.rotateAngleY = Rleg.rotateAngleY = Lleg.rotateAngleY;
		//乗っていない場合
		if (!LivingChest.isRiding()) {
			Body.rotateAngleX = par5 / (180F / (float) Math.PI);
		}

		//コアの回転
		Core.rotateAngleZ = par3 * 0.2F;

		//腕の揺らめき
		Rarm.rotateAngleZ = (MathHelper.sin(par3 * 0.05F) * 0.05F) + 0.3665191F;
		Larm.rotateAngleZ = -(MathHelper.sin(par3 * 0.05F) * 0.05F) - 0.3665191F;
	}

	public void setupMotionMappingToMotionData(EntityLivingChest chast, LivingChestMotionData targetMotionData) {
		LivingChestCoverMotionNone none = new LivingChestCoverMotionNone(chast, targetMotionData);
		Set<? extends LivingChestMotion<ModelLivingChest>> motions = Sets.newHashSet(none, none.getMotionClose(),
				none.getMotionOpen(), none.getMotionMomentaryOpen());

		LivingChestMotionMap<ModelLivingChest> map = new LivingChestMotionMap<ModelLivingChest>(this.getClass(),
				motions);

		for (MotionFactory factory : motionFactorys) {
			LivingChestMotion<ModelLivingChest> motion = factory.create(chast, targetMotionData, map);
			map.put(this.getClass(), motion);
		}

		targetMotionData.putMotionMap(this, map);
		targetMotionData.setCurrentCoverMotion(none);
	}
}
