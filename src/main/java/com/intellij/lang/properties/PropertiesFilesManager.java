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

import java.util.Collection;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.encoding.EncodingManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.indexing.FileBasedIndex;
import kava.beans.PropertyChangeListener;

/**
 * @author max
 */
@Singleton
public class PropertiesFilesManager
{
	public static PropertiesFilesManager getInstance(Project project)
	{
		return project.getComponent(PropertiesFilesManager.class);
	}

	private final Project myProject;

	@Inject
	public PropertiesFilesManager(Project project, EncodingManager encodingManager)
	{
		myProject = project;
		if(myProject.isDefault())
		{
			return;
		}

		final PropertyChangeListener listener = evt ->
		{
			String propertyName = evt.getPropertyName();
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
		};
		encodingManager.addPropertyChangeListener(listener, project);
	}

	public boolean processAllPropertiesFiles(final PropertiesFileProcessor processor)
	{
		return PropertiesReferenceManager.getInstance(myProject).processAllPropertiesFiles(processor);
	}
}
