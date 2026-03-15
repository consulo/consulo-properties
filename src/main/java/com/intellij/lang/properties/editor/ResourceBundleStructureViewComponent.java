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

import com.intellij.lang.properties.ResourceBundle;
import consulo.language.editor.PlatformDataKeys;
import consulo.project.Project;
import consulo.util.dataholder.Key;


/**
 * @author cdr
 */
class ResourceBundleStructureViewComponent extends PropertiesGroupingStructureViewComponent
{
	private final ResourceBundle myResourceBundle;

	public ResourceBundleStructureViewComponent(Project project,
			ResourceBundle resourceBundle,
			ResourceBundleEditor editor)
	{
		super(project, editor, new ResourceBundleStructureViewModel(project, resourceBundle));
		myResourceBundle = resourceBundle;
		showToolbar();
	}

	@Override
	public Object getData(Key dataId)
	{
		if(PlatformDataKeys.VIRTUAL_FILE == dataId)
		{
			return new ResourceBundleAsVirtualFile(myResourceBundle);
		}
		return super.getData(dataId);
	}

	@Override
	protected boolean showScrollToFromSourceActions()
	{
		return false;
	}
}

