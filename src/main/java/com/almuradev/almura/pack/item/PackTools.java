package com.almuradev.almura.pack.item;

import java.util.Set;

import net.malisis.core.renderer.icon.ClippedIcon;
import net.minecraft.item.ItemTool;

import com.almuradev.almura.pack.IClipContainer;
import com.almuradev.almura.pack.IModelContainer;
import com.almuradev.almura.pack.INodeContainer;
import com.almuradev.almura.pack.IPackObject;
import com.almuradev.almura.pack.Pack;
import com.almuradev.almura.pack.model.PackModelContainer;
import com.almuradev.almura.pack.node.INode;
import com.google.common.base.Optional;

public class PackTools extends ItemTool implements IPackObject, IClipContainer, IModelContainer, INodeContainer {

	protected PackTools(float p_i45333_1_, ToolMaterial p_i45333_2_, Set p_i45333_3_) {
		super(p_i45333_1_, p_i45333_2_, p_i45333_3_);
		// TODO Auto-generated constructor stub
	}

	@Override
	public <T extends INode<?>> T addNode(T node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addNodes(INode<?>... nodes) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public <T extends INode<?>> T getNode(Class<T> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends INode<?>> boolean hasNode(Class<T> clazz) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Optional<PackModelContainer> getModelContainer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setModelContainer(PackModelContainer modelContainer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getModelName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ClippedIcon[] getClipIcons() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Pack getPack() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getIdentifier() {
		// TODO Auto-generated method stub
		return null;
	}
}
