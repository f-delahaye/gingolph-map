package org.gingolph.tm.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.gingolph.tm.AbstractConstruct;
import org.gingolph.tm.AssociationImpl;
import org.gingolph.tm.LocatorImpl;
import org.gingolph.tm.TopicImpl;
import org.gingolph.tm.TopicMapImpl;
import org.gingolph.tm.TopicMapSupport;
import org.gingolph.tm.index.IdentifierIndex;
import org.gingolph.tm.index.LiteralIndexImpl;
import org.gingolph.tm.index.ScopedIndexImpl;
import org.gingolph.tm.index.TypeInstanceIndexImpl;
import org.tmapi.core.Association;
import org.tmapi.core.Locator;
import org.tmapi.core.Topic;
import org.tmapi.index.Index;
import org.tmapi.index.LiteralIndex;
import org.tmapi.index.ScopedIndex;
import org.tmapi.index.TypeInstanceIndex;


public class IMTopicMapSupport extends IMConstructSupport implements TopicMapSupport {
  List<TopicImpl> topics = new ArrayList<>();
  List<AssociationImpl> associations = new ArrayList<>();
  TopicImpl reifier;
  TopicMapImpl topicMap;
  private Locator baseLocator;

  static AtomicLong counter = new AtomicLong();

  IMTopicMapSupport() {}

  @Override
  public void setOwner(TopicMapImpl owner) {
    this.topicMap = owner;
  }
  
  @Override
  public List<TopicImpl> getTopics() {
    return topics;
  }

  @Override
  public void addTopic(TopicImpl topic) {
    topics.add(topic);
  }

  @Override
  public void removeTopic(Topic topic) {
    topics.remove(topic);
  }

  @Override
  public List<AssociationImpl> getAssociations() {
    return associations;
  }

  @Override
  public void addAssociation(AssociationImpl association) {
    associations.add(association);
  }

  @Override
  public void removeAssociation(Association association) {
    associations.remove(association);
  }

  @Override
  public TopicImpl getReifier() {
    return reifier;
  }

  @Override
  public void setReifier(TopicImpl reifier) {
    this.reifier = reifier;
  }

  @Override
  public <I extends Index> I getIndex(Class<I> type) {
    Index index;
    if (LiteralIndex.class.isAssignableFrom(type)) {
      index = topicMap.registerListener(new LiteralIndexImpl(topicMap.getEquality()));
    } else if (IdentifierIndex.class.isAssignableFrom(type)) {
      index =
          topicMap.registerListener(new IdentifierIndex(topicMap, getTopics(), getAssociations()));
    } else if (ScopedIndex.class.isAssignableFrom(type)) {
      index = topicMap.registerListener(new ScopedIndexImpl(topicMap.getEquality(), getTopics(), getAssociations()));
    } else if (TypeInstanceIndex.class.isAssignableFrom(type)) {
      index = topicMap.registerListener(new TypeInstanceIndexImpl(topicMap.getEquality(), getTopics(), getAssociations()));
    } else {
      throw new UnsupportedOperationException("Unknown index " + type);
    }
    return type.cast(index);
  }

  @Override
  public String generateId(AbstractConstruct<?> construct) {
    return String.valueOf(counter.getAndIncrement());
  }

  @Override
  public Locator createLocator(String value) {
    return new LocatorImpl(value);
  }

  @Override
  public Locator getBaseLocator() {
    return baseLocator;
  }

  @Override
  public void setBaseLocator(Locator baseLocator) {
    this.baseLocator = baseLocator;
  }
  
}
