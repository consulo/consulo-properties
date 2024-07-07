package com.intellij.lang.properties.inspection.duplicatePropertyInspection;

import consulo.configurable.ConfigurableBuilder;
import consulo.configurable.ConfigurableBuilderState;
import consulo.configurable.UnnamedConfigurable;
import consulo.language.editor.inspection.InspectionToolState;
import consulo.localize.LocalizeValue;
import consulo.properties.localize.PropertiesLocalize;
import consulo.ui.ComboBox;
import consulo.ui.Label;
import consulo.util.xml.serializer.XmlSerializerUtil;

import jakarta.annotation.Nullable;

/**
 * @author VISTALL
 * @since 12/03/2023
 */
public class DuplicatePropertyInspectionState implements InspectionToolState<DuplicatePropertyInspectionState>
{
	public DuplicatePropertyScope SCOPE = DuplicatePropertyScope.FILE;
	public boolean CHECK_DUPLICATE_VALUES = true;
	public boolean CHECK_DUPLICATE_KEYS = true;
	public boolean CHECK_DUPLICATE_KEYS_WITH_DIFFERENT_VALUES = true;

	@Nullable
	@Override
	public UnnamedConfigurable createConfigurable()
	{
		ConfigurableBuilder<ConfigurableBuilderState> builder = ConfigurableBuilder.newBuilder();
		builder.component(() -> Label.create(LocalizeValue.localizeTODO("Scope:")));
		builder.valueComponent(() ->
		{
			ComboBox<DuplicatePropertyScope> comboBox = ComboBox.create(DuplicatePropertyScope.values());
			comboBox.setValue(DuplicatePropertyScope.FILE);
			return comboBox;
		}, () -> SCOPE, v -> SCOPE = v);
		builder.checkBox(PropertiesLocalize.duplicatePropertyValueOption(), () -> CHECK_DUPLICATE_VALUES, b -> CHECK_DUPLICATE_VALUES = b);
		builder.checkBox(PropertiesLocalize.duplicatePropertyKeyOption(), () -> CHECK_DUPLICATE_KEYS, b -> CHECK_DUPLICATE_KEYS = b);
		builder.checkBox(PropertiesLocalize.duplicatePropertyDiffKeyOption(), () -> CHECK_DUPLICATE_KEYS_WITH_DIFFERENT_VALUES, b -> CHECK_DUPLICATE_KEYS_WITH_DIFFERENT_VALUES = b);
		return builder.buildUnnamed();
	}

	@Nullable
	@Override
	public DuplicatePropertyInspectionState getState()
	{
		return this;
	}

	@Override
	public void loadState(DuplicatePropertyInspectionState state)
	{
		XmlSerializerUtil.copyBean(state, this);
	}
}
