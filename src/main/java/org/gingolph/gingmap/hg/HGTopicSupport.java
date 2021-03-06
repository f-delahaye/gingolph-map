package org.gingolph.gingmap.hg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gingolph.gingmap.NameImpl;
import org.gingolph.gingmap.OccurrenceImpl;
import org.gingolph.gingmap.RoleImpl;
import org.gingolph.gingmap.TopicImpl;
import org.gingolph.gingmap.TopicSupport;
import org.gingolph.gingmap.equality.Equality;
import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.annotation.HGIgnore;
import org.hypergraphdb.atom.HGRel;
import org.hypergraphdb.util.HGUtils;
import org.tmapi.core.Locator;
import org.tmapi.core.ModelConstraintException;
import org.tmapi.core.Occurrence;
import org.tmapi.core.Reifiable;
import org.tmapi.core.Role;
import org.tmapi.core.Topic;

public class HGTopicSupport extends HGScopedSupport<TopicImpl> implements TopicSupport {
  // Set<Topic> types = null;
  // Set<Occurrence> occurrences = null;
  // Set<NameImpl> names = null;

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  protected HGTopicSupport() {
  }
  
  public HGTopicMapSupport getTopicMapSupport() {
    return HGTMUtil.getTopicMapOf(hyperGraph, getHandle(hyperGraph, this));
  }

  @Override
  public void setOwner(TopicImpl owner) {
      this.owner = owner;
  }
  
  @Override
  protected TopicImpl createOwner() {
    TopicImpl topic = new TopicImpl(getTopicMapSupport().getOwner());
    topic.setSupport(this);
    return topic;
  }

  @Override
  public void addSubjectIdentifier(Locator subjectIdentifier) {
    HyperGraph graph = getGraph();
    final HGHandle thisHandle = getHandle(graph, this);
    HGHandle locHandle = HGTMUtil.ensureLocatorHandle(graph, subjectIdentifier);
    graph.add(new HGRel(HGTM.SubjectIdentifier, new HGHandle[] {locHandle, thisHandle}),
        HGTM.hSubjectIdentifier);
  }

  @Override
  public Set<Locator> getSubjectIdentifiers() {
    HyperGraph graph = getGraph();
    final HGHandle handle = getHandle(graph, this);
    return handle == null ? null
        : new HashSet<>(HGTMUtil.getRelatedObjects(graph, HGTM.hSubjectIdentifier, null, handle));
  }

  @Override
  public void removeSubjectIdentifier(Locator subjectIdentifier) {
    HyperGraph graph = getGraph();
    HGHandle locatorHandle = HGTMUtil.findLocatorHandle(graph, subjectIdentifier);
    HGHandle rel = hg.findOne(graph, hg.and(hg.type(HGTM.hSubjectIdentifier),
        hg.orderedLink(locatorHandle, getHandle(graph, this))));
    if (rel != null) {
      graph.remove(rel);
      // If this locator is not used in anything else, we may remove it.
      if (graph.getIncidenceSet(locatorHandle).size() == 0) {
        graph.remove(locatorHandle, false);
      }
    }
  }

  @Override
  public void addSubjectLocator(Locator subjectLocator) throws ModelConstraintException {
    HyperGraph graph = getGraph();
    HGHandle locatorHandle = HGTMUtil.ensureLocatorHandle(graph, subjectLocator);
    graph.add(
        new HGRel(HGTM.SubjectLocator, new HGHandle[] {locatorHandle, getHandle(graph, this)}),
        HGTM.hSubjectLocator);
  }

  @HGIgnore
  @Override
  public Set<Locator> getSubjectLocators() {
    HyperGraph graph = getGraph();
    final HGHandle handle = getHandle(graph, this);
    return handle == null ? null
        : new HashSet<>(HGTMUtil.getRelatedObjects(graph, HGTM.hSubjectLocator, null, handle));
  }

  @Override
  public void removeSubjectLocator(Locator subjectLocator) {
    HyperGraph graph = getGraph();
    HGHandle locatorHandle = graph.getHandle(subjectLocator);
    HGHandle rel = hg.findOne(graph, hg.and(hg.type(HGTM.hSubjectLocator),
        hg.orderedLink(locatorHandle, getHandle(graph, this))));
    if (rel != null) {
      graph.remove(rel);
      // If this locator is not used in anything else, we may remove it.
      if (graph.getIncidenceSet(locatorHandle).size() == 0) {
        graph.remove(locatorHandle, false);
      }

    }
  }

  @Override
  public void addType(TopicImpl type, Equality equality) {
    HyperGraph graph = getGraph();
    HGHandle tHandle = getHandle(graph, type);
    graph.add(new HGRel(HGTM.TypeOf, new HGHandle[] {tHandle, getHandle(graph, this)}),
        HGTM.hTypeOf);
  }

  @HGIgnore
  @Override
  public Set<TopicImpl> getTypes() {
    try {
      Set<TopicImpl> types = owner.getTopicMap().getEquality().newSet();
      types.addAll(HGTMUtil.getRelatedObjects(this, HGTM.hTypeOf, false));
      return types;
    } catch (RuntimeException e) {
      // TODO Auto-generated catch block
      throw e;
    }
  }

  @Override
  public boolean removeType(Topic type) {
    HyperGraph graph = getGraph();
    HGHandle typeHandle = getHandle(graph, type);
    HGHandle rel = hg.findOne(graph,
        hg.and(hg.type(HGTM.hTypeOf), hg.orderedLink(typeHandle, getHandle(graph, this))));
    if (rel != null) {
      graph.remove(rel);
      return true;
    }
    return false;
  }

  @Override
  public void addOccurrence(OccurrenceImpl occurrence) {
    HyperGraph graph = getGraph();
    HGHandle occurrenceHandle = add(graph, occurrence);
    graph.add(
        new HGRel(HGTM.OccurenceOf, new HGHandle[] {occurrenceHandle, getHandle(graph, this)}),
        HGTM.hOccurrenceOf);
  }

  @HGIgnore
  @Override
  public List<OccurrenceImpl> getOccurrences() {
    return HGTMUtil.getRelatedObjects(this, HGTM.hOccurrenceOf, false);
  }

  @Override
  public void removeOccurrence(Occurrence occurrence) {
    HyperGraph graph = getGraph();
    graph.remove(getHandle(graph, occurrence), false);
  }

  @HGIgnore
  @Override
  public Reifiable getReified() {
    HyperGraph graph = getGraph();
    HGHandle thisHandle = getHandle(graph, this);
    final HGConstructSupport<?> reifiedSupport = thisHandle == null ? null : (HGConstructSupport<?>) HGTMUtil.getOneRelated(graph,
        HGTM.hReifierOf, thisHandle, null);
    return reifiedSupport == null ? null : (Reifiable) reifiedSupport.getOwner();
  }

  @Override
  public void setReified(Reifiable reifiable) {}

  @Override
  public void addRolePlayed(RoleImpl role) {

  }

  @HGIgnore
  @Override
  public List<RoleImpl> getRolesPlayed() {
    List<RoleImpl> result = new ArrayList<>();
    HGSearchResult<HGRoleSupport> rs = null;
    try {
      HyperGraph graph = getGraph();
      HGHandle thisH = getHandle(graph, this);
      if (thisH == null) {
        return null;
      }
      rs = graph.find(
          hg.apply(hg.deref(graph), hg.and(hg.type(HGRoleSupport.class), hg.incident(thisH))));
      while (rs.hasNext()) {
        HGRoleSupport role = rs.next();
        if (thisH.equals(role.getTargetAt(0))) {
          result.add(role.getOwner());
        }
      }
      return result;
    } finally {
      HGUtils.closeNoException(rs);
    }
  }

  @Override
  public void removeRolePlayed(Role role) {
    // HyperGraph graph = getGraph();
    // final HGHandle roleHandle = getHandle(graph, role);
    // roleHandle may be null e.g. if AssociationImpl.removeRole(RoleImpl role)
    // role.getPlayer().removeRolePlayed(role) and roleSupport.removeRole will both be called and
    // try to remove the role.
    // The second one will fail
    // if (roleHandle != null) {
    // graph.remove(roleHandle, false);
    // }
  }

  @Override
  public void addName(NameImpl name) {
    HyperGraph graph = getGraph();
    HGHandle nameHandle = add(graph, name);
    graph.add(new HGRel(HGTM.NameOf, new HGHandle[] {nameHandle, getHandle(graph, this)}),
        HGTM.hNameOf);
  }

  @HGIgnore
  @Override
  public List<NameImpl> getNames() {
    return HGTMUtil.getRelatedObjects(this, HGTM.hNameOf, false);
  }

  @Override
  public void removeName(NameImpl name) {
    HyperGraph graph = getGraph();
    graph.remove(getHandle(graph, name), false);
  }
}
