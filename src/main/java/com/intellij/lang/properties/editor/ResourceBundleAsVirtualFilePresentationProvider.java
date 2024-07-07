package com.intellij.lang.properties.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.application.presentation.TypePresentationProvider;
import consulo.platform.base.icon.PlatformIconGroup;
import consulo.ui.image.Image;
import jakarta.annotation.Nonnull;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 11-Aug-22
 */
@ExtensionImpl
public class ResourceBundleAsVirtualFilePresentationProvider extends TypePresentationProvider<ResourceBundleAsVirtualFile>
{
	@Nonnull
	@Override
	public Class<ResourceBundleAsVirtualFile> getItemClass()
	{
		return ResourceBundleAsVirtualFile.class;
	}

	@Nullable
	@Override
	public Image getIcon(ResourceBundleAsVirtualFile resourceBundleAsVirtualFile)
	{
		return PlatformIconGroup.nodesResourcebundle();
	}
}
