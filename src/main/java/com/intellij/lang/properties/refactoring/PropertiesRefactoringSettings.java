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
package com.intellij.lang.properties.refactoring;

import consulo.annotation.component.ComponentScope;
import consulo.annotation.component.ServiceAPI;
import consulo.annotation.component.ServiceImpl;
import consulo.application.Application;
import consulo.component.persist.PersistentStateComponent;
import consulo.component.persist.State;
import consulo.component.persist.Storage;
import consulo.util.xml.serializer.XmlSerializerUtil;
import jakarta.inject.Singleton;

@State(
		name = "PropertiesRefactoringSettings",
		storages = {
				@Storage(
						file = consulo.component.persist.StoragePathMacros.APP_CONFIG + "/other.xml"
				)
		}
)
@ServiceAPI(ComponentScope.APPLICATION)
@ServiceImpl
@Singleton
public class PropertiesRefactoringSettings implements PersistentStateComponent<PropertiesRefactoringSettings>
{
	public boolean RENAME_SEARCH_IN_COMMENTS = false;

	public static PropertiesRefactoringSettings getInstance()
	{
		return Application.get().getInstance(PropertiesRefactoringSettings.class);
	}

	public PropertiesRefactoringSettings getState()
	{
		return this;
	}

	public void loadState(PropertiesRefactoringSettings state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}