/*
 * Copyright 2000-2009 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.lang.properties;

import java.nio.charset.Charset;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.LanguageFileType;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.CharsetToolkit;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import consulo.fileTypes.FileTypeWithPredefinedCharset;
import consulo.ui.image.Image;

/**
 * @author max
 */
public class PropertiesFileType extends LanguageFileType implements FileTypeWithPredefinedCharset
{
	public static final LanguageFileType INSTANCE = new PropertiesFileType();
	@NonNls
	public static final String DEFAULT_EXTENSION = "properties";
	@NonNls
	public static final String DOT_DEFAULT_EXTENSION = "." + DEFAULT_EXTENSION;

	private PropertiesFileType()
	{
		super(PropertiesLanguage.INSTANCE);
	}

	@Override
	@Nonnull
	public String getId()
	{
		return "Properties";
	}

	@Override
	@Nonnull
	public String getDescription()
	{
		return PropertiesBundle.message("properties.files.file.type.description");
	}

	@Override
	@Nonnull
	public String getDefaultExtension()
	{
		return DEFAULT_EXTENSION;
	}

	@Override
	public Image getIcon()
	{
		return AllIcons.FileTypes.Properties;
	}

	@Override
	public String getCharset(@Nonnull VirtualFile file, final byte[] content)
	{
		Charset charset = EncodingManager.getInstance().getDefaultCharsetForPropertiesFiles(file);
		return charset == null ? CharsetToolkit.getDefaultSystemCharset().name() : charset.name();
	}

	@Nonnull
	@Override
	public Pair<Charset, String> getPredefinedCharset(@Nonnull VirtualFile virtualFile)
	{
		return Pair.create(virtualFile.getCharset(), PropertiesBundle.message("properties.files.file.type.description"));
	}
}
