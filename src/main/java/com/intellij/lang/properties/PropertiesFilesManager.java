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

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.intellij.openapi.vfs.encoding.EncodingManagerListener;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

/**
 * @author max
 */
@Singleton
public class PropertiesFilesManager
{
	public static class MyTopic implements EncodingManagerListener
	{
		private final Project myProject;

		@Inject
		public MyTopic(Project project)
		{
			myProject = project;
		}

		@Override
		public void propertyChanged(@Nullable Document document, @Nonnull String propertyName, Object oldValue, Object newValue)
		{
			if(EncodingManager.PROP_NATIVE2ASCII_SWITCH.equals(propertyName) ||
					EncodingManager.PROP_PROPERTIES_FILES_ENCODING.equals(propertyName))
			{
				DumbService.getInstance(myProject).smartInvokeLater(new Runnable()
				{
					public void run()
					{
						ApplicationManager.getApplication().runWriteAction(() ->
						{
							if(myProject.isDisposed())
							{
								return;
							}
							Collection<VirtualFile> filesToRefresh = FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME, PropertiesFileType.INSTANCE, GlobalSearchScope.allScope(myProject));
							VirtualFile[] virtualFiles = VfsUtil.toVirtualFileArray(filesToRefresh);
							FileDocumentManager.getInstance().saveAllDocuments();

							//force to re-detect encoding
							for(VirtualFile virtualFile : virtualFiles)
							{
								virtualFile.setCharset(null);
							}
							FileDocumentManager.getInstance().reloadFiles(virtualFiles);
						});
					}
				});
			}
		}
	}

	@Nonnull
	public static PropertiesFilesManager getInstance(Project project)
	{
		return project.getInstance(PropertiesFilesManager.class);
	}

	private final Project myProject;

	@Inject
	public PropertiesFilesManager(Project project)
	{
		myProject = project;
	}

	public boolean processAllPropertiesFiles(final PropertiesFileProcessor processor)
	{
		return PropertiesReferenceManager.getInstance(myProject).processAllPropertiesFiles(processor);
	}
}
