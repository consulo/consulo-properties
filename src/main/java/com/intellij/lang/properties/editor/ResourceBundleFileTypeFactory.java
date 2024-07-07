package com.intellij.lang.properties.editor;

import consulo.annotation.component.ExtensionImpl;
import consulo.virtualFileSystem.fileType.FileTypeConsumer;
import consulo.virtualFileSystem.fileType.FileTypeFactory;
import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 09-Aug-22
 */
@ExtensionImpl
public class ResourceBundleFileTypeFactory extends FileTypeFactory
{
	@Override
	public void createFileTypes(@Nonnull FileTypeConsumer consumer)
	{
		consumer.consume(ResourceBundleEditorProvider.RESOURCE_BUNDLE_FILE_TYPE, "");
	}
}
