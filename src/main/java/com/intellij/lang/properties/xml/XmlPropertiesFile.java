/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
package com.intellij.lang.properties.xml;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.lang.properties.psi.Property;
import consulo.annotation.access.RequiredReadAction;
import consulo.application.util.CachedValue;
import consulo.application.util.CachedValueProvider;
import consulo.application.util.CachedValuesManager;
import consulo.language.psi.PsiDirectory;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiFile;
import consulo.language.util.IncorrectOperationException;
import consulo.project.Project;
import consulo.util.collection.MultiMap;
import consulo.util.dataholder.Key;
import consulo.util.dataholder.UserDataHolderBase;
import consulo.virtualFileSystem.VirtualFile;
import consulo.xml.psi.xml.XmlFile;
import consulo.xml.psi.xml.XmlTag;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.*;

/**
 * @author Dmitry Avdeev
 * Date: 7/26/11
 */
public class XmlPropertiesFile extends UserDataHolderBase implements PropertiesFile
{

	private static final Key<CachedValue<PropertiesFile>> KEY = Key.create("xml properties file");
	private final XmlFile myFile;

	private MultiMap<String, IProperty> myProperties;

	@Nullable
	@RequiredReadAction
	public static PropertiesFile getPropertiesFile(final PsiFile file)
	{
		return file instanceof XmlFile ? getPropertiesFile((XmlFile) file) : null;
	}

	@RequiredReadAction
	public static PropertiesFile getPropertiesFile(final XmlFile file)
	{
		CachedValuesManager manager = CachedValuesManager.getManager(file.getProject());
		return manager.getCachedValue(file, KEY, () -> {
			PropertiesFile value = !XmlPropertiesIndex.isAccepted(file.getText()) ? null : new XmlPropertiesFile(file);
			return CachedValueProvider.Result.create(value, file);
		}, false);
	}

	private XmlPropertiesFile(XmlFile file)
	{
		myFile = file;
	}

	private MultiMap<String, IProperty> getPropertiesImpl()
	{
		if(myProperties != null)
		{
			return myProperties;
		}

		XmlTag rootTag = myFile.getRootTag();
		if(rootTag == null)
		{
			return myProperties = MultiMap.empty();
		}

		XmlTag[] entries = rootTag.findSubTags("entry");
		MultiMap<String, IProperty> map = new MultiMap<>();

		for(XmlTag entry : entries)
		{
			XmlProperty property = new XmlProperty(entry, this);
			map.putValue(property.getKey(), property);
		}
		return myProperties = map;
	}

	@Nonnull
	@Override
	public PsiFile getContainingFile()
	{
		return myFile;
	}

	@Nonnull
	@Override
	public List<IProperty> getProperties()
	{
		return new ArrayList<IProperty>(getPropertiesImpl().values());
	}

	@Override
	public IProperty findPropertyByKey(@Nonnull String key)
	{
		Collection<IProperty> properties = getPropertiesImpl().get(key);
		return properties.isEmpty() ? null : properties.iterator().next();
	}

	@Nonnull
	@Override
	public List<IProperty> findPropertiesByKey(@Nonnull String key)
	{
		return new ArrayList<IProperty>(getPropertiesImpl().get(key));
	}

	@Nonnull
	@Override
	public ResourceBundle getResourceBundle()
	{
		return PropertiesUtil.getResourceBundle(getContainingFile());
	}

	@Nonnull
	@Override
	public Locale getLocale()
	{
		return PropertiesUtil.getLocale(getVirtualFile());
	}

	@Nonnull
	@Override
	public PsiElement addProperty(@Nonnull IProperty property) throws IncorrectOperationException
	{
		return null;
	}

	@Nonnull
	@Override
	public PsiElement addPropertyAfter(@Nonnull Property property, @Nullable Property anchor) throws IncorrectOperationException
	{
		return null;
	}

	@Nullable
	@Override
	public void removeProperties(@Nonnull String key)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public IProperty addProperty(String key, String value)
	{
		XmlTag rootTag = myFile.getRootTag();
		XmlTag entry = rootTag.createChildTag("entry", "", value, false);
		entry.setAttribute("key", key);
		return new XmlProperty(entry, this);
	}

	@Nonnull
	@Override
	public Map<String, String> getNamesMap()
	{
		Map<String, String> result = new HashMap<String, String>();
		for(IProperty property : getProperties())
		{
			result.put(property.getUnescapedKey(), property.getValue());
		}
		return result;
	}

	@Override
	public String getName()
	{
		return getContainingFile().getName();
	}

	@Override
	public VirtualFile getVirtualFile()
	{
		return getContainingFile().getVirtualFile();
	}

	@Override
	public PsiDirectory getParent()
	{
		return getContainingFile().getParent();
	}

	@Override
	public Project getProject()
	{
		return getContainingFile().getProject();
	}

	@Override
	public String getText()
	{
		return getContainingFile().getText();
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		XmlPropertiesFile that = (XmlPropertiesFile) o;

		if(!myFile.equals(that.myFile))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return myFile.hashCode();
	}
}
