package knightminer.knightperipherals.turtles.peripherals;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import dan200.computercraft.api.turtle.ITurtleAccess;
import knightminer.knightperipherals.turtles.peripherals.tasks.TaskTntDrop;
import knightminer.knightperipherals.turtles.peripherals.tasks.TaskTntExplode;
import knightminer.knightperipherals.util.LuaTimer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class PeripheralTnt implements IPeripheral {

	private ITurtleAccess turtle;

	public PeripheralTnt(ITurtleAccess turtle) {
		this.turtle = turtle;
	}

	@Override
	public String getType() {
		return "tnt";
	}

	@Override
	public String[] getMethodNames() {
		return new String[] { "explode", "bomb", "bombUp", "bombDown" };
	}

	@Override
	public Object[] callMethod(IComputerAccess computer, ILuaContext context, int method, Object[] arguments)
	        throws LuaException, InterruptedException {

		switch (method) {
		// explode
			case 0:
				LuaTimer.delay(context, computer, 40);
				return context.executeMainThreadTask(new TaskTntExplode(turtle));

			// placeBomb and dropBomb
			case 1: case 2: case 3:
				World world = turtle.getWorld();
				// return 0 for down, 1 for up, or turtle facing otherwise
				EnumFacing direction = method == 3 ? EnumFacing.DOWN : method == 2 ? EnumFacing.UP : turtle.getDirection();
				BlockPos pos = turtle.getPosition().offset(direction);
	
				// only work on air or liquids
				if (!world.isAirBlock(pos) && !world.getBlockState(pos).getBlock().getMaterial().isLiquid()) {
					return new Object[] { false, "Block at location" };
				}
	
				// find TNT to use in the selected slot
				IInventory inv = turtle.getInventory();
				int selected = turtle.getSelectedSlot();
				ItemStack stack = inv.getStackInSlot(selected);
	
				// if we don't have TNT, stop
				if (stack == null || stack.getItem() != Item.getItemFromBlock(Blocks.tnt)) {
					return new Object[] { false, "No TNT found in slot" };
				}
	
				// main TNT task
				return context.executeMainThreadTask(new TaskTntDrop(turtle, world, pos, inv, stack, selected));
		}

		return new Object[0];
	}

	@Override
	public void attach(IComputerAccess computer) {}

	@Override
	public void detach(IComputerAccess computer) {}

	@Override
	public boolean equals(IPeripheral other) {
		return (this == other);
	}

}
