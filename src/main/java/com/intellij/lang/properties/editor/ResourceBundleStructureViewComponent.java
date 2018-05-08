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

import javax.annotation.Nonnull;

import com.intellij.lang.properties.ResourceBundle;
import consulo.properties.editor.actions.AddPropertyKeyAction;
import consulo.properties.editor.actions.RemovePropertyKeyAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;

/**
 * @author cdr
 */
class ResourceBundleStructureViewComponent extends PropertiesGroupingStructureViewComponent
{
	@Nonnull
	private final ResourceBundle myResourceBundle;

	public ResourceBundleStructureViewComponent(@Nonnull Project project,
			@Nonnull ResourceBundle resourceBundle,
			@Nonnull ResourceBundleEditor editor)
	{
		super(project, editor, new ResourceBundleStructureViewModel(project, resourceBundle));
		myResourceBundle = resourceBundle;
		showToolbar();
	}

	@Override
	public Object getData(@Nonnull Key<?> dataId)
	{
		if(PlatformDataKeys.VIRTUAL_FILE == dataId)
		{
			return new ResourceBundleAsVirtualFile(myResourceBundle);
		}
		return super.getData(dataId);
	}

	@Override
	protected boolean addCustomActions(@Nonnull DefaultActionGroup actionGroup)
	{
		actionGroup.add(new AddPropertyKeyAction(myResourceBundle, getTreeBuilder()));
		actionGroup.add(new RemovePropertyKeyAction(myResourceBundle, getTreeBuilder()));
		return true;
	}

	@Override
	protected boolean showScrollToFromSourceActions()
	{
		return false;
	}
}
