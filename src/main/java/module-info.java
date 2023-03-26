/**
 * @author VISTALL
 * @since 11-Aug-22
 */
module com.intellij.properties
{
	// TODO remove it in future
	requires java.desktop;
	requires forms.rt;

	requires consulo.ide.api;
	requires consulo.language.impl;

	requires com.intellij.xml;

	requires consulo.util.xml.fast.reader;

	// TODO remove it in future
	requires consulo.ide.impl;

	exports com.intellij.lang.properties;
	exports com.intellij.lang.properties.copyright.psi;
	exports com.intellij.lang.properties.editor;
	exports com.intellij.lang.properties.editor.enter;
	exports com.intellij.lang.properties.findUsages;
	exports com.intellij.lang.properties.ide;
	exports com.intellij.lang.properties.ide.favoritesTreeView;
	exports com.intellij.lang.properties.inspection.duplicatePropertyInspection;
	exports com.intellij.lang.properties.inspection.unused;
	exports com.intellij.lang.properties.parsing;
	exports com.intellij.lang.properties.projectView;
	exports com.intellij.lang.properties.psi;
	exports com.intellij.lang.properties.psi.idCache;
	exports com.intellij.lang.properties.psi.impl;
	exports com.intellij.lang.properties.refactoring;
	exports com.intellij.lang.properties.references;
	exports com.intellij.lang.properties.spellchecker;
	exports com.intellij.lang.properties.structureView;
	exports com.intellij.lang.properties.xml;
	exports consulo.properties;
	exports consulo.properties.icon;
	exports consulo.properties.localize;
	exports consulo.properties.xml;

	opens com.intellij.lang.properties.xml to consulo.application.impl;
}