package net.multiplemonomials.eer.item.tool;


import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.multiplemonomials.eer.configuration.CommonConfiguration;
import net.multiplemonomials.eer.creativetab.CreativeTab;
import net.multiplemonomials.eer.interfaces.IChargeable;
import net.multiplemonomials.eer.interfaces.IKeyBound;
import net.multiplemonomials.eer.item.ItemEE;
import net.multiplemonomials.eer.reference.Key;
import net.multiplemonomials.eer.reference.Reference;
import net.multiplemonomials.eer.util.BlockHelper;
import net.multiplemonomials.eer.util.EMCHelper;
import net.multiplemonomials.eer.util.PowerItemUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;


public class ItemPickaxeMatter extends ItemPickaxe implements IChargeable, IKeyBound
{
	
	Matter _matterType;
	
//Either dark or red matter material is passed in the initialization
	public ItemPickaxeMatter(Matter matterType)
	{
		super(matterType._toolMaterial);
		
		setUnlocalizedName("pickaxe" + matterType.name());
		setCreativeTab(CreativeTab.EER_TAB);
		
		setNoRepair();
		
		maxStackSize = 1;
		
		_matterType = matterType;
	}
	
	//not repairable... because it never breaks
	@Override
    public boolean getIsRepairable(ItemStack par1ItemStack, ItemStack par2ItemStack)
    {
        return false;
    }
	
	//do not decrease durability on block destroyed
	@Override
    public boolean onBlockDestroyed(ItemStack p_150894_1_, World p_150894_2_, Block p_150894_3_, int p_150894_4_, int p_150894_5_, int p_150894_6_, EntityLivingBase p_150894_7_)
    {

        return true;
    }
	
	//do not damage tool
    public boolean hitEntity(ItemStack par1ItemStack, EntityLivingBase par2EntityLivingBase, EntityLivingBase par3EntityLivingBase)
    {
        return true;
    }
	
	//dig speed increases with durability
	@Override
	public float getDigSpeed(ItemStack itemstack, Block block, int metadata)
    	{
        	if(block.getMaterial() != Material.iron && block.getMaterial() != Material.anvil && block.getMaterial() != Material.rock)
        	{
      			return super.getDigSpeed(itemstack, block, metadata);
        	}
        
        //for every charge level, efficiency increases by 3
        //added the 3 to give it a slight buff versus the other tools
        return efficiencyOnProperMaterial + PowerItemUtils.computeEfficiencyBonus(itemstack.getItemDamage(), _matterType) + 3;
    }
	
    @Override
    public void doKeyBindingAction(EntityPlayer entityPlayer, ItemStack itemStack, Key key)
    {
    	if(key == Key.CHARGE)
    	{
    		PowerItemUtils.bumpChargeOnItem(itemStack, _matterType);
    	}
    }
    
    @Override
    public String getUnlocalizedName()
    {
        return String.format("item.%s%s", Reference.RESOURCE_PREFIX, ItemEE.getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    @Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
        return String.format("item.%s%s", Reference.RESOURCE_PREFIX, ItemEE.getUnwrappedUnlocalizedName(super.getUnlocalizedName()));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister iconRegister)
    {
        itemIcon = iconRegister.registerIcon(ItemEE.getUnwrappedUnlocalizedName(this.getUnlocalizedName()));
    }

    public boolean isDamageable()
    {
        return false;
    }
    
    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int side, float x2, float y2, float z2)
    {	
        if (side == 0)
        {
            --y;
        }

        else if (side == 1)
        {
            ++y;
        }

        else if (side == 2)
        {
            --z;
        }

        else if (side == 3)
        {
            ++z;
        }

        else if (side == 4)
        {
            --x;
        }

        else if (side == 5)
        {
            ++x;
        }

        if (!player.canPlayerEdit(x, y, z, side, itemStack))
        {
            return false;
        }
        else
        {
            if (world.isAirBlock(x, y, z))
            {
                world.playSoundEffect((double)x + 0.5D, (double)y + 0.5D, (double)z + 0.5D, "fire.ignite", 1.0F, itemRand.nextFloat() * 0.4F + 0.8F);
                


            	
            	//TODO: don't take EMC for blocks that are skipped because they are not air
                
            	//take some EMC
            	double emcLeft = getAvailableEMC(itemStack);
            	int blocksToSet = getDamage(itemStack) == CommonConfiguration.MAX_ITEM_CHARGES ? 1 : MathHelper.floor_double(Math.pow((CommonConfiguration.MAX_ITEM_CHARGES - getDamage(itemStack)) + 1, 2));
            	double neededEMC = CommonConfiguration.DM_FLINT_REQUIRED_EMC_PER_BLOCK * blocksToSet;
            	
            	
            	if(emcLeft < neededEMC)
            	{
            		emcLeft += EMCHelper.consumeEMCFromPlayerInventory(player, neededEMC - emcLeft);
            	}
            	if(emcLeft >= neededEMC)
            	{
            		 emcLeft -= neededEMC;
            		 itemStack.stackTagCompound.setDouble("emcLeft", emcLeft);
            		 
            		 if(getDamage(itemStack) == CommonConfiguration.MAX_ITEM_CHARGES)
                     {
                     	world.setBlock(x, y, z, Blocks.fire, 0, 2);
                     }
                     else
                     {
                     	BlockHelper.setAirBlocksAround(x, y, z, Blocks.fire, 0, CommonConfiguration.MAX_ITEM_CHARGES - getDamage(itemStack), world);
                     }
            	}
            	else
            	{
            		itemStack.stackTagCompound.setDouble("emcLeft", emcLeft);
            		return false;
            	}
                
            }
            return true;
        }
}
