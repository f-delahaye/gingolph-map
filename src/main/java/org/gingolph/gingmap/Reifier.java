package org.gingolph.gingmap;

import org.tmapi.core.Reifiable;
import org.tmapi.core.Topic;


public interface Reifier extends Topic {
  public void setReified(Reifiable reifiable);
}
