package org.gingolph.tm.index;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import org.gingolph.tm.AssociationImpl;
import org.gingolph.tm.NameImpl;
import org.gingolph.tm.OccurrenceImpl;
import org.gingolph.tm.TopicImpl;
import org.gingolph.tm.VariantImpl;
import org.tmapi.core.Association;
import org.tmapi.core.Construct;
import org.tmapi.core.Name;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Scoped;
import org.tmapi.core.Topic;
import org.tmapi.core.Variant;
import org.tmapi.index.ScopedIndex;


public class ScopedIndexImpl extends ClassifiedItemsIndexImpl<Scoped> implements ScopedIndex {

  protected ClassifiedAndUnclassifiedItems<Association> scopedAndUnscopedAssociations = new ClassifiedAndUnclassifiedItems<>();
  ClassifiedAndUnclassifiedItems<Name> namesByThemes = new ClassifiedAndUnclassifiedItems<>();
  ClassifiedAndUnclassifiedItems<Occurrence> occurrencesByThemes = new ClassifiedAndUnclassifiedItems<>();
  ClassifiedAndUnclassifiedItems<Variant> variantsByThemes = new ClassifiedAndUnclassifiedItems<>();

  public ScopedIndexImpl(Collection<TopicImpl> topics, Collection<AssociationImpl> associations) {
    associations.forEach(association -> registerItem(association, this.scopedAndUnscopedAssociations));
    topics.forEach(topic -> {
      topic.getNullSafeOccurrenceImpls().forEach(occurrence -> registerItem(occurrence, this.occurrencesByThemes));
      topic.getNullSafeNameImpls().forEach(name -> {
        registerItem(name, namesByThemes);
        name.getNullSafeVariantImpls().forEach(variant -> registerItem(variant, this.variantsByThemes));
      });
    });
  }
  
  @Override
  public Collection<Association> getAssociations(Topic theme) {
    return getItemsByClassifier(scopedAndUnscopedAssociations, theme);
  }

  @Override
  public Collection<Association> getAssociations(Topic[] themes, boolean matchAll) { 
    return getItemsByClassifiers(scopedAndUnscopedAssociations, themes, matchAll);
  }

  private <T extends Scoped> Collection<T> getItemsByClassifiers(
      org.gingolph.tm.index.ClassifiedItemsIndexImpl.ClassifiedAndUnclassifiedItems<T> scopedAndUnscopedItems,
      Topic[] themes, boolean matchAll) {
    return super.getItemsByClassifiers(scopedAndUnscopedItems, themes, matchAll, this::getNullSafeClassifierList);
  }

  @Override
  public Collection<Topic> getAssociationThemes() {
    return getClassifiers(scopedAndUnscopedAssociations);
  }

  @Override
  public Collection<Occurrence> getOccurrences(Topic theme) {
    return getItemsByClassifier(occurrencesByThemes, theme);
  }

  @Override
  public Collection<Occurrence> getOccurrences(Topic[] themes, boolean matchAll) {
    return getItemsByClassifiers(occurrencesByThemes, themes, matchAll);
  }

  @Override
  public Collection<Topic> getOccurrenceThemes() {
    return getClassifiers(occurrencesByThemes);
  }

  @Override
  public Collection<Name> getNames(Topic theme) {
    return getItemsByClassifier(namesByThemes, theme);
  }

  @Override
  public Collection<Name> getNames(Topic[] themes, boolean matchAll) {
    return getItemsByClassifiers(namesByThemes, themes, matchAll);
  }

  @Override
  public Collection<Topic> getNameThemes() {
    return getClassifiers(namesByThemes);
  }

  @Override
  public Collection<Variant> getVariants(Topic theme) {
    if (theme == null) {
      throw new IllegalArgumentException("null theme not supported");
    }
    return getItemsByClassifier(variantsByThemes, theme);
  }

  @Override
  public Collection<Variant> getVariants(Topic[] themes, boolean matchAll) {
    return getItemsByClassifiers(variantsByThemes, themes, matchAll);
  }

  @Override
  public Collection<Topic> getVariantThemes() {
    Set<Topic> variantThemes = Collections.newSetFromMap(new IdentityHashMap<Topic, Boolean>());
    variantThemes.addAll(getClassifiers(variantsByThemes));
    variantThemes.addAll(getClassifiers(namesByThemes));
    return Collections.unmodifiableCollection(variantThemes);
  }
  
  @SuppressWarnings({"unchecked", "rawtypes"})
  protected ClassifiedAndUnclassifiedItems<Scoped> getItems(Class classifiedClass) {
    if (classifiedClass == AssociationImpl.class) {
      return (ClassifiedAndUnclassifiedItems)scopedAndUnscopedAssociations;
    }
    if (classifiedClass == OccurrenceImpl.class) {
      return (ClassifiedAndUnclassifiedItems)occurrencesByThemes;
    }
    if (classifiedClass == NameImpl.class) {
      return (ClassifiedAndUnclassifiedItems)namesByThemes;
    }
    if (classifiedClass == VariantImpl.class) {
      return (ClassifiedAndUnclassifiedItems)variantsByThemes;
    }
    throw new IllegalArgumentException("unsupported scoped class "+classifiedClass);    
  }
  

  @Override
  public void onThemeChanged(Scoped scoped, Topic themeToAdd, Topic themeToRemove) {
    final ClassifiedAndUnclassifiedItems<Scoped> scopedAndUnscoped = getItems(scoped.getClass());
    if (themeToRemove != null) {
      scopedAndUnscoped.unclassify(scoped, themeToRemove);
      if (scoped.getScope().isEmpty()) {
        scopedAndUnscoped.unclassifiedItems.add(scoped);
      }
    }
    if (themeToAdd != null) {
      scopedAndUnscoped.classify(scoped,  themeToAdd);
      if (scoped.getScope().size() == 1) {
        // was previously unscoped, remove it from there
        scopedAndUnscoped.unclassifiedItems.remove(scoped);
      }
    }
  }

  @Override
  protected Scoped cast(Construct construct) {
    return construct instanceof Scoped? (Scoped) construct : null;
  }

  @Override
  protected Collection<Topic> getNullSafeClassifierList(Scoped classified) {
    return classified.getScope();
  }

}
