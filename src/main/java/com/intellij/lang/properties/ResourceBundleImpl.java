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

/**
 * @author Alexey
 */
package com.intellij.lang.properties;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.Nonnull;

import org.jetbrains.annotations.NonNls;

import javax.annotation.Nullable;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;

public class ResourceBundleImpl implements ResourceBundle
{
	@Nonnull
	private final VirtualFile myBaseDirectory;
	@Nonnull
	private final String myBaseName;
	@NonNls
	private static final String RESOURCE_BUNDLE_PREFIX = "resourceBundle:";

	public ResourceBundleImpl(@Nonnull VirtualFile baseDirectory, @Nonnull String baseName)
	{
		myBaseDirectory = baseDirectory;
		myBaseName = baseName;
	}

	public static final ResourceBundle NULL = new ResourceBundle()
	{
		@Override
		@Nonnull
		public List<PropertiesFile> getPropertiesFiles(final Project project)
		{
			return Collections.emptyList();
		}

		@Override
		@Nonnull
		public PropertiesFile getDefaultPropertiesFile(final Project project)
		{
			throw new IllegalStateException();
		}

		@Override
		@Nonnull
		public String getBaseName()
		{
			return "";
		}

		@Override
		@Nonnull
		public VirtualFile getBaseDirectory()
		{
			throw new IllegalStateException();
		}
	};

	@Override
	@Nonnull
	public List<PropertiesFile> getPropertiesFiles(final Project project)
	{
		VirtualFile[] children = myBaseDirectory.getChildren();
		List<PropertiesFile> result = new SmartList<PropertiesFile>();
		final PsiManager psiManager = PsiManager.getInstance(project);
		Application application = ApplicationManager.getApplication();
		for(final VirtualFile file : children)
		{
			if(!file.isValid())
			{
				continue;
			}
			if(Comparing.strEqual(PropertiesUtil.getBaseName(file), myBaseName))
			{
				PropertiesFile propertiesFile = PropertiesUtil.getPropertiesFile(application.runReadAction(new Computable<PsiFile>()
				{
					@Override
					public PsiFile compute()
					{
						return psiManager.findFile(file);
					}
				}));
				if(propertiesFile != null)
				{
					result.add(propertiesFile);
				}
			}
		}
		return result;
	}

	@Override
	@Nonnull
	public PropertiesFile getDefaultPropertiesFile(final Project project)
	{
		List<PropertiesFile> files = getPropertiesFiles(project);
		// put default properties file first
		ContainerUtil.quickSort(files, new Comparator<PropertiesFile>()
		{
			@Override
			public int compare(final PropertiesFile o1, final PropertiesFile o2)
			{
				return Comparing.compare(o1.getName(), o2.getName());
			}
		});
		return files.get(0);
	}

	@Override
	@Nonnull
	public String getBaseName()
	{
		return myBaseName;
	}

	@Override
	public boolean equals(final Object o)
	{
		if(this == o)
		{
			return true;
		}
		if(o == null || getClass() != o.getClass())
		{
			return false;
		}

		final ResourceBundleImpl resourceBundle = (ResourceBundleImpl) o;

		if(!myBaseDirectory.equals(resourceBundle.myBaseDirectory))
		{
			return false;
		}
		if(!myBaseName.equals(resourceBundle.myBaseName))
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = myBaseDirectory.hashCode();
		result = 29 * result + myBaseName.hashCode();
		return result;
	}

	@Nullable
	public static ResourceBundle createByUrl(String url)
	{
		if(!url.startsWith(RESOURCE_BUNDLE_PREFIX))
		{
			return null;
		}

		String defaultPropertiesUrl = url.substring(RESOURCE_BUNDLE_PREFIX.length());
		final int idx = defaultPropertiesUrl.lastIndexOf('/');
		if(idx == -1)
		{
			return null;
		}
		String baseDir = defaultPropertiesUrl.substring(0, idx);
		String baseName = defaultPropertiesUrl.substring(idx + 1);
		VirtualFile baseDirectory = VirtualFileManager.getInstance().findFileByUrl(baseDir);
		if(baseDirectory != null)
		{
			return new ResourceBundleImpl(baseDirectory, baseName);
		}
		return null;
	}

	public String getUrl()
	{
		return RESOURCE_BUNDLE_PREFIX + getBaseDirectory() + "/" + getBaseName();
	}

	@Override
	@Nonnull
	public VirtualFile getBaseDirectory()
	{
		return myBaseDirectory;
	}
}