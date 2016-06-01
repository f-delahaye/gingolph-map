package org.gingolph.tm;

import java.util.Set;
import org.tmapi.core.Association;
import org.tmapi.core.Locator;
import org.tmapi.core.Topic;
import org.tmapi.index.Index;


public interface TopicMapSupport extends ConstructSupport {
  Set<Topic> getTopics();

  void addTopic(Topic topic);

  void removeTopic(Topic topic);

  Set<Association> getAssociations();

  void addAssociation(Association association);

  void removeAssociation(Association association);

  <I extends Index> I getIndex(Class<I> type);

  Topic getReifier();

  void setReifier(Topic reifier);

  public String generateId(IdentifiedConstruct construct);

  Locator createLocator(String value);

  /**
   * Returns the topicmap's base locator
   * 
   * @return
   */
  public Locator getBaseLocator();

  public void setBaseLocator(Locator locator);
}
