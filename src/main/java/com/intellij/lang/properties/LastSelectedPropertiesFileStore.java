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
package com.intellij.lang.properties;

import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.ide.ServiceManager;
import consulo.language.file.inject.VirtualFileWindow;
import consulo.language.psi.PsiFile;
import consulo.language.statistician.StatisticsInfo;
import consulo.language.statistician.StatisticsManager;
import consulo.language.util.ModuleUtilCore;
import consulo.module.content.ProjectFileIndex;
import consulo.module.content.ProjectRootManager;
import consulo.util.io.FileUtil;
import consulo.virtualFileSystem.VirtualFile;
import consulo.virtualFileSystem.VirtualFileManager;
import consulo.virtualFileSystem.util.VirtualFileUtil;
import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.inject.Singleton;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author cdr
 */
@Singleton
@State(name = "LastSelectedPropertiesFileStore", storages = {@Storage(file = consulo.component.persist.StoragePathMacros.APP_CONFIG + "/other.xml")})
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
public class LastSelectedPropertiesFileStore implements PersistentStateComponent<Element>
{
	private final Map<String, String> lastSelectedUrls = new HashMap<String, String>();
	private String lastSelectedFileUrl;
	@NonNls
	private static final String PROPERTIES_FILE_STATISTICS_KEY = "PROPERTIES_FILE";

	public static LastSelectedPropertiesFileStore getInstance()
	{
		return ServiceManager.getService(LastSelectedPropertiesFileStore.class);
	}

	@Nullable
	public String suggestLastSelectedPropertiesFileUrl(PsiFile context)
	{
		VirtualFile virtualFile = context.getVirtualFile();

		while(virtualFile != null)
		{
			String contextUrl = virtualFile.getUrl();
			String url = lastSelectedUrls.get(contextUrl);
			if(url != null)
			{
				return url;
			}
			virtualFile = virtualFile.getParent();
		}
		if(lastSelectedFileUrl != null)
		{
			VirtualFile lastFile = VirtualFileManager.getInstance().findFileByUrl(lastSelectedFileUrl);
			final ProjectFileIndex fileIndex = ProjectRootManager.getInstance(context.getProject()).getFileIndex();
			if(lastFile != null && ModuleUtilCore.findModuleForPsiElement(context) == fileIndex.getModuleForFile(lastFile))
			{
				return lastSelectedFileUrl;
			}
		}
		return null;
	}

	public static int getUseCount(@Nonnull String path)
	{
		return StatisticsManager.getInstance().getUseCount(new StatisticsInfo(PROPERTIES_FILE_STATISTICS_KEY, path));
	}

	public void saveLastSelectedPropertiesFile(PsiFile context, PropertiesFile file)
	{
		VirtualFile virtualFile = context.getVirtualFile();
		if(virtualFile instanceof VirtualFileWindow)
		{
			virtualFile = ((VirtualFileWindow) virtualFile).getDelegate();
		}
		assert virtualFile != null;
		String contextUrl = virtualFile.getUrl();
		final VirtualFile vFile = file.getVirtualFile();
		if(vFile != null)
		{
			String url = vFile.getUrl();
			lastSelectedUrls.put(contextUrl, url);
			VirtualFile containingDir = virtualFile.getParent();
			lastSelectedUrls.put(containingDir.getUrl(), url);
			lastSelectedFileUrl = url;
			StatisticsManager.getInstance().incUseCount(new StatisticsInfo(PROPERTIES_FILE_STATISTICS_KEY, FileUtil.toSystemDependentName(VirtualFileUtil.urlToPath(url))));
		}
	}

	private void readExternal(@NonNls Element element)
	{
		lastSelectedUrls.clear();
		List list = element.getChildren("entry");
		for(Object o : list)
		{
			@NonNls Element child = (Element) o;
			String context = child.getAttributeValue("context");
			String url = child.getAttributeValue("url");
			VirtualFile propFile = VirtualFileManager.getInstance().findFileByUrl(url);
			VirtualFile contextFile = VirtualFileManager.getInstance().findFileByUrl(context);
			if(propFile != null && contextFile != null)
			{
				lastSelectedUrls.put(context, url);
			}
		}
		lastSelectedFileUrl = element.getAttributeValue("lastSelectedFileUrl");
	}

	private void writeExternal(@NonNls Element element)
	{
		for(Map.Entry<String, String> entry : lastSelectedUrls.entrySet())
		{
			String context = entry.getKey();
			String url = entry.getValue();
			@NonNls Element child = new Element("entry");
			child.setAttribute("context", context);
			child.setAttribute("url", url);
			element.addContent(child);
		}
		if(lastSelectedFileUrl != null)
		{
			element.setAttribute("lastSelectedFileUrl", lastSelectedFileUrl);
		}
	}

	public Element getState()
	{
		final Element e = new Element("state");
		writeExternal(e);
		return e;
	}

	public void loadState(Element state)
	{
		readExternal(state);
	}
}
