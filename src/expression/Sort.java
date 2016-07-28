package expression;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * A Sort is a hierarchical type of a logical formula. Sorts can be subsorts and supersorts
 * of other sorts, and each sort has a unique name and supersort. The most general sort,
 * OBJECT, has no supersort.
 */
public class Sort {
  public static final Sort OBJECT = new Sort("Object", null);
  public static final Sort BOOLEAN = new Sort("Boolean", OBJECT);
  public static Map<String, Sort> instances = new HashMap<String, Sort>() {{
    this.put(OBJECT.getName(), OBJECT);
    this.put(BOOLEAN.getName(), BOOLEAN);
  }};

  private String name;
  private Sort superSort;
  private Set<Sort> subSorts;

  /**
   * Create a new Sort with the given name
   *
   * @param name the UID of the new Sort
   */
  private Sort(String name, Sort superSort) {
    this.name = name;
    this.superSort = superSort;
    subSorts = new HashSet<>();
  }

  /**
   * Get an instance of a new or existing Sort
   *
   * @param name the UID of the Sort to obtain
   * @return the Sort with the given name
   */
  public static Sort getSort(String name, Sort superSort) {
    Sort s = instances.get(name);
    if (s != null) {
      if (!s.getSuperSort().equals(superSort))
        throw new ExistingSortException("Cannot create a Sort with an existing name");
      return instances.get(name);
    }
    s = new Sort(name, superSort);
    instances.put(name, s);
    superSort.addSubSort(s);
    return s;
  }

  public String toString() {
    return name;
  }

  /**
   * Get an instance of a new or existing Sort
   *
   * @param name the UID of the Sort to obtain
   * @return the Sort with the given name
   */
  public static Sort getSort(String name) {
    Sort s = instances.get(name);
    if (s != null)
      return s;
    s = new Sort(name, Sort.OBJECT);
    instances.put(name, s);
    Sort.OBJECT.addSubSort(s);
    return s;
  }

  public static boolean isSort(String name) {
    return instances.containsKey(name);
  }

  /**
   * Get the unique name of this Sort
   *
   * @return the name of this Sort
   */
  public String getName() {
    return name;
  }

  /**
   * Get the parent of this Sort
   *
   * @return the supersort of this Sort
   */
  public Sort getSuperSort() {
    return superSort;
  }

  /**
   * Add a subSort to this sort
   *
   * @param s the subSort to be added
   */
  private void addSubSort(Sort s) {
    subSorts.add(s);
  }

  /**
   * Test if this is a subsort of another sort
   *
   * @param s the possible supersort of this
   * @return true if s is a supersort of this
   */
  public boolean isSubSort(Sort s) {
    return this.equals(s) || (superSort != null && (superSort.equals(s) || superSort.isSubSort(s)));
  }

  /**
   * Test if this is a subsort of another sort
   *
   * @param s the possible supersort of this
   * @return true if s is a supersort of this
   */
  public boolean isSubSortExclusive(Sort s) {
    if (superSort == null)
      return false;

    return superSort.equals(s) || superSort.isSubSort(s);
  }

  /**
   * Test if this is a supersort of another sort
   *
   * @param s the possible subsort of this
   * @return true if s is a subsort of this
   */
  public boolean isSuperSort(Sort s) {
    return subSorts.contains(s) || subSorts.stream().anyMatch(sub -> sub.isSubSort(s));
  }

  public int hashCode() {
    return name.hashCode();
  }

  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o != null && o instanceof Sort) {
      Sort s = (Sort) o;
      return name.equals(s.getName()) && ((superSort == null && s.getSuperSort() == null) || superSort.equals(s.getSuperSort()));
    }
    return false;
  }

  public static class ExistingSortException extends RuntimeException {
    private ExistingSortException(String message) {
      super(message);
    }
  }
}
