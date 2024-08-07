/*
 * Copyright 2013-2017 consulo.io
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

package consulo.properties.xml;

import com.intellij.lang.properties.xml.XmlPropertiesFile;
import consulo.annotation.access.RequiredReadAction;
import consulo.annotation.component.ExtensionImpl;
import consulo.language.icon.IconDescriptor;
import consulo.language.icon.IconDescriptorUpdater;
import consulo.language.psi.PsiElement;
import consulo.properties.icon.PropertiesIconGroup;
import consulo.xml.psi.xml.XmlFile;

import jakarta.annotation.Nonnull;

/**
 * @author VISTALL
 * @since 14:59/20.07.13
 */
@ExtensionImpl(order = "after xml")
public class XmlPropertiesIconDescriptorUpdater implements IconDescriptorUpdater
{
	@RequiredReadAction
	@Override
	public void updateIcon(@Nonnull IconDescriptor iconDescriptor, @Nonnull PsiElement element, int flags)
	{
		if(element instanceof XmlFile && XmlPropertiesFile.getPropertiesFile((XmlFile) element) != null)
		{
			iconDescriptor.setMainIcon(PropertiesIconGroup.xmlproperties());
		}
	}
}
