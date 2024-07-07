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
package com.intellij.lang.properties.editor;

import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.annotation.component.ExtensionImpl;
import consulo.application.ApplicationManager;
import consulo.application.dumb.DumbAware;
import consulo.application.util.function.Computable;
import consulo.disposer.Disposer;
import consulo.fileEditor.FileEditor;
import consulo.fileEditor.FileEditorPolicy;
import consulo.fileEditor.FileEditorProvider;
import consulo.fileEditor.FileEditorState;
import consulo.language.psi.PsiFile;
import consulo.language.psi.PsiManager;
import consulo.project.Project;
import consulo.virtualFileSystem.VirtualFile;
import org.jdom.Element;

import jakarta.annotation.Nonnull;

@ExtensionImpl
public class ResourceBundleEditorProvider implements FileEditorProvider, DumbAware
{
	public static final ResourceBundleFileType RESOURCE_BUNDLE_FILE_TYPE = new ResourceBundleFileType();

	@Override
	public boolean accept(@Nonnull final Project project, @Nonnull final VirtualFile file)
	{
		if(file instanceof ResourceBundleAsVirtualFile)
		{
			return true;
		}
		PropertiesFile propertiesFile = ApplicationManager.getApplication().runReadAction(new Computable<PropertiesFile>()
		{
			@Override
			public PropertiesFile compute()
			{
				PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
				return psiFile == null ? null : PropertiesUtil.getPropertiesFile(psiFile);
			}
		});
		return propertiesFile != null && propertiesFile.getResourceBundle().getPropertiesFiles(project).size() > 1;
	}

	@Override
	@Nonnull
	public FileEditor createEditor(@Nonnull Project project, @Nonnull final VirtualFile file)
	{
		ResourceBundle resourceBundle;
		if(file instanceof ResourceBundleAsVirtualFile)
		{
			resourceBundle = ((ResourceBundleAsVirtualFile) file).getResourceBundle();
		}
		else
		{
			PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
			if(psiFile == null)
			{
				throw new IllegalArgumentException("psifile cannot be null");
			}
			resourceBundle = PropertiesUtil.getPropertiesFile(psiFile).getResourceBundle();
		}

		return new ResourceBundleEditor(project, resourceBundle);
	}

	@Override
	public void disposeEditor(@Nonnull FileEditor editor)
	{
		Disposer.dispose(editor);
	}

	@Override
	@Nonnull
	public FileEditorState readState(@Nonnull Element element, @Nonnull Project project, @Nonnull VirtualFile file)
	{
		return new ResourceBundleEditor.ResourceBundleEditorState(null);
	}

	@Override
	public void writeState(@Nonnull FileEditorState state, @Nonnull Project project, @Nonnull Element element)
	{
	}

	@Override
	@Nonnull
	public FileEditorPolicy getPolicy()
	{
		return FileEditorPolicy.PLACE_AFTER_DEFAULT_EDITOR;
	}

	@Override
	@Nonnull
	public String getEditorTypeId()
	{
		return "ResourceBundle";
	}
}
