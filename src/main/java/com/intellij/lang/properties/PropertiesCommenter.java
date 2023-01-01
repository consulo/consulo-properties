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

import consulo.annotation.component.ExtensionImpl;
import consulo.language.Commenter;
import consulo.language.Language;

import javax.annotation.Nonnull;

/**
 * @author max
 */
@ExtensionImpl
public class PropertiesCommenter implements Commenter
{
	public String getLineCommentPrefix()
	{
		return "#";
	}

	public String getBlockCommentPrefix()
	{
		return null;
	}

	public String getBlockCommentSuffix()
	{
		return "";
	}

	public String getCommentedBlockCommentPrefix()
	{
		return null;
	}

	public String getCommentedBlockCommentSuffix()
	{
		return null;
	}

	@Nonnull
	@Override
	public Language getLanguage()
	{
		return PropertiesLanguage.INSTANCE;
	}
}
