package com.intellij.lang.properties.xml;

import java.util.Map;

import com.intellij.openapi.application.PluginPathManager;
import consulo.virtualFileSystem.VirtualFile;
import com.intellij.testFramework.fixtures.LightPlatformCodeInsightFixtureTestCase;
import com.intellij.util.indexing.FileContentImpl;

/**
 * @author Dmitry Avdeev
 *         Date: 7/25/11
 */
public abstract class XmlPropertiesIndexTest extends LightPlatformCodeInsightFixtureTestCase {
  public void testIndex() {
    final VirtualFile file = myFixture.configureByFile("foo.xml").getVirtualFile();
    Map<XmlPropertiesIndex.Key, String> map = new XmlPropertiesIndex().map(FileContentImpl.createByFile(file));

    assertEquals(3, map.size());
    assertEquals("bar", map.get(new XmlPropertiesIndex.Key("foo")));
    assertEquals("baz", map.get(new XmlPropertiesIndex.Key("fu")));
    assertTrue(map.containsKey(XmlPropertiesIndex.MARKER_KEY));
  }

  public void testSystemId() throws Exception {
    final VirtualFile file = myFixture.configureByFile("wrong.xml").getVirtualFile();
    Map<XmlPropertiesIndex.Key, String> map = new XmlPropertiesIndex().map(FileContentImpl.createByFile(file));

    assertEquals(0, map.size());
  }

  @Override
  protected String getTestDataPath() {
    return PluginPathManager.getPluginHomePath("properties") + "/testData/xml/";
  }

}
