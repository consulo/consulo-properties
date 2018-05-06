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
package consulo.properties.editor.actions;

import java.util.List;

import javax.annotation.Nonnull;

import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.util.treeView.AbstractTreeBuilder;
import com.intellij.lang.properties.PropertiesBundle;
import com.intellij.lang.properties.ResourceBundle;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.application.Result;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.InputValidator;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.IconUtil;

/**
 * @author VISTALL
 * @since 22:19/28.03.13
 */
public class AddPropertyKeyAction extends AnAction
{
	private final ResourceBundle myResourceBundle;
	private final AbstractTreeBuilder myTreeBuilder;

	public AddPropertyKeyAction(@Nonnull ResourceBundle resourceBundle, AbstractTreeBuilder treeBuilder)
	{
		super(CommonBundle.message("button.add"), PropertiesBundle.message("button.add.property.description"), IconUtil.getAddIcon());
		myResourceBundle = resourceBundle;
		myTreeBuilder = treeBuilder;
	}

	@Override
	public void actionPerformed(final AnActionEvent e)
	{
		final Project project = e.getData(CommonDataKeys.PROJECT);
		final List<PropertiesFile> propertiesFiles = myResourceBundle.getPropertiesFiles(project);

		final String message = Messages.showInputDialog(project, PropertiesBundle.message("add.dialog.property.name"),
				PropertiesBundle.message("add.dialog.property.title"), AllIcons.General.QuestionDialog, null, new InputValidator()
		{
			@Override
			public boolean checkInput(String inputString)
			{
				if(StringUtil.isEmptyOrSpaces(inputString))
				{
					return false;
				}

				boolean hasInAll = true;
				for(PropertiesFile propertiesFile : propertiesFiles)
				{
					if(propertiesFile.findPropertyByKey(inputString) == null)
					{
						hasInAll = false;
					}
				}
				return !hasInAll && !inputString.contains(" ");
			}

			@Override
			public boolean canClose(String inputString)
			{
				return checkInput(inputString);
			}
		});

		if(message == null)
		{
			return;
		}

		new WriteCommandAction(project)
		{
			@Override
			protected void run(Result result) throws Throwable
			{
				for(PropertiesFile propertiesFile : propertiesFiles)
				{
					final String temp = message.trim();
					propertiesFile.addProperty(temp, temp);
				}

				myTreeBuilder.queueUpdate();
			}
		}.execute();
	}
}
