package com.intellij.lang.properties.xml;

import java.util.List;

import com.intellij.lang.properties.IProperty;
import com.intellij.lang.properties.PropertiesReferenceManager;
import com.intellij.lang.properties.PropertiesUtil;
import com.intellij.lang.properties.psi.PropertiesFile;
import com.intellij.openapi.application.PluginPathManager;
import consulo.language.psi.PsiFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;

/**
 * @author Dmitry Avdeev
 *         Date: 7/26/11
 */
public abstract class XmlPropertiesTest extends LightPlatformCodeInsightFixtureTestCase {

  public void testXmlProperties() throws Exception {
    myFixture.configureByFile("foo.xml");
    List<PropertiesFile> files = PropertiesReferenceManager.getInstance(getProject()).findPropertiesFiles(myModule, "foo");
    assertEquals(1, files.size());
    PropertiesFile file = files.get(0);
    assertEquals(1, file.findPropertiesByKey("foo").size());

    List<IProperty> properties = PropertiesUtil.findPropertiesByKey(getProject(), "foo");
    assertEquals(1, properties.size());
  }

  public void testWrongFile() throws Exception {
    PsiFile psiFile = myFixture.configureByFile("wrong.xml");
    PropertiesFile file = PropertiesUtil.getPropertiesFile(psiFile);
    assertNull(file);
  }

  public void testHighlighting() throws Exception {
    myFixture.testHighlighting("foo.xml");
  }

  @Override
  protected String getTestDataPath() {
    return PluginPathManager.getPluginHomePath("properties") + "/testData/xml/";
  }

  @Override
  protected boolean isWriteActionRequired() {
    return false;
  }
}
