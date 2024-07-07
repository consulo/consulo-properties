package com.intellij.lang.properties.xml;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.psi.PropertiesFile;
import consulo.application.AllIcons;
import consulo.language.pom.PomRenameableTarget;
import consulo.language.psi.PsiElement;
import consulo.language.psi.PsiInvalidElementAccessException;
import consulo.language.psi.PsiTarget;
import consulo.language.util.IncorrectOperationException;
import consulo.ui.image.Image;
import consulo.xml.psi.xml.XmlTag;
import jakarta.annotation.Nonnull;
import org.jetbrains.annotations.NonNls;

/**
 * @author Dmitry Avdeev
 *         Date: 7/26/11
 */
public class XmlProperty implements IProperty, PomRenameableTarget, PsiTarget {

  private final XmlTag myTag;
  private final XmlPropertiesFile myPropertiesFile;

  public XmlProperty(XmlTag tag, XmlPropertiesFile xmlPropertiesFile) {
    myTag = tag;
    myPropertiesFile = xmlPropertiesFile;
  }

  @Override
  public String getName() {
    return myTag.getAttributeValue("key");
  }

  @Override
  public boolean isWritable() {
    return myTag.isWritable();
  }

  @Override
  public PsiElement setName(@Nonnull String name) {
    return myTag.setAttribute("key", name);
  }

  @Override
  public String getKey() {
    return getName();
  }

  @Override
  public String getValue() {
    return myTag.getValue().getText();
  }

  @Override
  public String getUnescapedValue() {
    return getValue();
  }

  @Override
  public String getUnescapedKey() {
    return getKey();
  }

  @Override
  public void setValue(@NonNls @Nonnull String value) throws IncorrectOperationException
  {
    myTag.getValue().setText(value);
  }

  @Override
  public PropertiesFile getPropertiesFile() throws PsiInvalidElementAccessException {
    return myPropertiesFile;
  }

  @Override
  public String getDocCommentText() {
    return null;
  }

  @Override
  public PsiElement getPsiElement() {
    return myTag;
  }

  @Override
  public void navigate(boolean requestFocus) {

  }

  @Override
  public boolean canNavigate() {
    return true;
  }

  @Override
  public boolean canNavigateToSource() {
    return true;
  }

  @Override
  public Image getIcon(int flags) {
    return AllIcons.Nodes.Property;
  }

  @Override
  public boolean isValid() {
    return myTag.isValid();
  }

  @Nonnull
  @Override
  public PsiElement getNavigationElement() {
    return getPsiElement();
  }
}
