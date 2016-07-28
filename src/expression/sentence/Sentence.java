package expression.sentence;

import expression.Expression;
import expression.Sort;
import logicalreasoner.inference.Inference;
import logicalreasoner.truthassignment.TruthAssignment;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * The Sentence class represents any type of logical
 * Sentence, (ie. BooleanSentence, Proposition, Predicate, or generated by connectives)
 */
public abstract class Sentence extends Expression {
  static HashMap<String, Sentence> instances = new HashMap<>();
  protected ArrayList<Sentence> args;
  Sort sort;
  protected Integer SIZE = null, QUANTIFIER_COUNT = null, ATOM_COUNT = null;

  /**
   * Create a new logical Sentence
   *
   * @param a    its arguments
   * @param n    its name
   * @param s    its symbol
   * @param type its Sort
   */
  protected Sentence(ArrayList<Sentence> a, String n, String s, Sort type) {
    super(n, s);
    args = a;
    sort = type;
    HASH_CODE = toString().hashCode();
  }

  public List<Sentence> getArgs() {
    return new ArrayList<>(args);
  }

  /**
   * Obtain a Sentence instance by direct lookup using
   * the full s-expression string of the statement
   *
   * @param sExpr the symbol string representing the desired Sentence
   * @return the corresponding Sentence Object
   */
  public static Sentence makeSentence(String sExpr) {
    if (instances.containsKey(sExpr))
      return instances.get(sExpr);
    Sentence s = new SentenceReader().parse(sExpr);
    instances.put(sExpr, s);
    return s;
  }

  /**
   * Obtain a Sentence instance by lookup of symbol name and arguments
   *
   * @param name the label of this Sentence
   * @param args the list of arguments of the desired Sentence
   * @return the corresponding Sentence Object
   */
  public static Sentence makeSentence(String name, List<Sentence> args) {
    return makeSentence(AbstractSentenceReader.sentenceString(name, args));
  }

  public static Sentence makeSentence(String name, Variable var, Sentence s) {
    return makeSentence(AbstractSentenceReader.sentenceString(name, var, s));
  }

  /**
   * Obtain a Sentence instance by direct lookup using
   * the full s-expression string of the statement
   *
   * @param sExpr the symbol string representing the desired Sentence
   * @return the corresponding Sentence Object
   */
  public static Sentence makeSentenceStrict(String sExpr) {
    if (instances.containsKey(sExpr))
      return instances.get(sExpr);
    Sentence s = new StrictSentenceReader().parse(sExpr);
    instances.put(sExpr, s);
    return s;
  }

  /**
   * Obtain a Sentence instance by lookup of symbol name and arguments
   *
   * @param name the label of this Sentence
   * @param args the list of arguments of the desired Sentence
   * @return the corresponding Sentence Object
   */
  public static Sentence makeSentenceStrict(String name, List<Sentence> args) {
    return makeSentenceStrict(AbstractSentenceReader.sentenceString(name, args));
  }

  public static Sentence makeSentenceStrict(String name, Variable var, Sentence s) {
    return makeSentenceStrict(AbstractSentenceReader.sentenceString(name, var, s));
  }

  public abstract Boolean eval(TruthAssignment h);

  public abstract Inference reason(TruthAssignment h, int inferenceNum, int justificationNum);

  public String toString() {
    if (TOSTRING == null) {
      if (!args.isEmpty()) {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < args.size() - 1; ++i) {
          builder.append(args.get(i)).append(" ").append(symbol).append(" ");
        }
        builder.append(args.get(args.size() - 1)).append(")");
        TOSTRING = builder.toString();
      } else
        TOSTRING = symbol;
    }
    return TOSTRING;
  }

  public String toSExpression() {
    if (TOSEXPR == null) {
      StringBuilder builder = new StringBuilder();
      builder.append("(").append(name);
      args.forEach(arg -> builder.append(" ").append(arg.toSExpression()));
      builder.append(")");
      TOSEXPR = builder.toString();
    }
    //return toSExpression();
    return TOSEXPR;
  }

  public String toMATR() {
    if (args.isEmpty())
      return sort.toString() + ":" + name;
    StringBuilder builder = new StringBuilder();
    builder.append("(").append(sort).append(":").append(name);
    boolean b = this instanceof And || this instanceof Or || this instanceof Iff;
    if (b)
      builder.append(" {");
    else
      builder.append(" ");
    IntStream.rangeClosed(0, args.size() - 1).forEach(i -> {
      if (i == 0)
        builder.append(args.get(i).toMATR());
      builder.append(" " + args.get(i).toMATR());
    });
    //args.forEach(arg -> builder.append(" ").append(arg.toMATR()));

    if (b)
      builder.append("})");
    else
      builder.append(")");
    return builder.toString();
  }

  public boolean equals(Object o) {
    return this == o;
  }

  public boolean isAtomic() {
    return this instanceof Atom || this instanceof Predicate;
  }

  public boolean isLiteral() {
    return isAtomic() || this instanceof Not && args.get(0).isLiteral();
  }

  public Sentence getSubSentence(int i) {
    return args.get(i);
  }

  public int numArgs() {
    return args.size();
  }

  public int size() {
    if (SIZE == null)
      SIZE = args.stream().mapToInt(Sentence::size).sum() + numArgs();
    return SIZE;
  }

  public Sort getSort() {
    return sort;
  }

  public boolean isQuantifier() {
    return false;
  }

  public Set<Sentence> getConstants() {
    return args.stream()
            .flatMap(s -> s.getConstants().stream())
            .collect(Collectors.toSet());
  }

  public Sentence instantiate(Sentence c, Variable v) {
    return Sentence.makeSentence(name, args.stream().map(a -> a.instantiate(c, v)).collect(Collectors.toList()));
  }

  public int quantifierCount() {
    if (QUANTIFIER_COUNT == null)
      QUANTIFIER_COUNT = args.stream().mapToInt(Sentence::quantifierCount)
              .reduce(isQuantifier() ? 1 : 0, (a, b) -> a + b);
    return QUANTIFIER_COUNT;
  }

  public int atomCount() {
    if (ATOM_COUNT == null)
      ATOM_COUNT = args.stream().mapToInt(Sentence::atomCount)
              .reduce(isAtomic() ? 1 : 0, (a, b) -> a + b);
    return ATOM_COUNT;
  }

  public Stream<Sentence> getSubSentences() {
    if (args.isEmpty())
      return Stream.of(this);
    return Stream.concat(Stream.of(this), args.stream().flatMap(Sentence::getSubSentences));
  }

  public static Comparator<Sentence> quantifierComparator = (e1, e2) -> {
    if (e1 instanceof Exists) {   // Always instantiate existential quantifiers before universals
      if (e2 instanceof Exists)
        return e1.atomCount()
                - e2.atomCount();
      return -1;
    } else if (e2 instanceof Exists)
      return 1;

    return 0;
    /*
    ForAll f1 = (ForAll) e1,
            f2 = (ForAll) e2;

    if (f1.getSentence().isLiteral() && !f2.getSentence().isLiteral())
      return -1;
    else if (!f1.getSentence().isLiteral() && f2.getSentence().isLiteral())
      return 1;

    int q1 = f1.quantifierCount(),
            q2 = f2.quantifierCount();   // Always instantiate statements with less quantifiers

    if (q1 != q2)
      return q1 - q2;

    if (f1.getSentence().size() != f2.getSentence().size())
      return f1.getSentence().size() - f2.getSentence().size();

    q1 = f1.atomCount();
    q2 = f2.atomCount();   // Always instantiate statements with less atoms
    if (q1 != q2)
      return q1 - q2;

    return f1.getSentence().numArgs() - f2.getSentence().numArgs();
    */
  };
}
