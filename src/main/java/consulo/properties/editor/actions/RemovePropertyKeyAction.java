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

import org.jetbrains.annotations.NotNull;
import com.intellij.CommonBundle;
import com.intellij.icons.AllIcons;
import com.intellij.ide.structureView.newStructureView.StructureViewComponent;
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
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.util.Function;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.tree.TreeUtil;

/**
 * @author VISTALL
 * @since 22:30/28.03.13
 */
public class RemovePropertyKeyAction extends AnAction
{
	@NotNull
	private final ResourceBundle myBundle;
	@NotNull
	private final AbstractTreeBuilder myTreeBuilder;

	public RemovePropertyKeyAction(@NotNull ResourceBundle bundle, @NotNull AbstractTreeBuilder treeBuilder)
	{
		super(CommonBundle.message("button.remove"), PropertiesBundle.message("remove.property.intention.text"), IconUtil.getRemoveIcon());
		myBundle = bundle;
		myTreeBuilder = treeBuilder;
	}

	@Override
	public void actionPerformed(AnActionEvent e)
	{
		final Project project = e.getData(CommonDataKeys.PROJECT);
		final List<StructureViewComponent.StructureViewTreeElementWrapper> treeElements = TreeUtil.collectSelectedObjectsOfType(myTreeBuilder
				.getTree(), StructureViewComponent.StructureViewTreeElementWrapper.class);

		String joinedProperties = StringUtil.join(treeElements, new Function<StructureViewComponent.StructureViewTreeElementWrapper, String>()
		{
			@Override
			public String fun(StructureViewComponent.StructureViewTreeElementWrapper temp)
			{
				return temp.getName();
			}
		}, ", ");

		final int result = Messages.showYesNoDialog(project, PropertiesBundle.message("remove.dialog.confirm.description", joinedProperties),
				PropertiesBundle.message("remove.dialog.confirm.title"), AllIcons.General.QuestionDialog);
		if(result != Messages.OK)
		{
			return;
		}

		new WriteCommandAction<Object>(project)
		{
			@Override
			protected void run(Result<Object> result) throws Throwable
			{
				final List<PropertiesFile> propertiesFiles = myBundle.getPropertiesFiles(project);
				for(PropertiesFile propertiesFile : propertiesFiles)
				{
					for(StructureViewComponent.StructureViewTreeElementWrapper treeElement : treeElements)
					{
						propertiesFile.removeProperties(treeElement.getName());
					}
				}
				myTreeBuilder.queueUpdate();
			}
		}.execute();
	}
}
