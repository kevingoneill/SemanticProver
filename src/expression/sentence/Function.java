package expression.sentence;

import expression.Sort;
import logicalreasoner.inference.Inference;
import logicalreasoner.truthassignment.TruthAssignment;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by kevin on 7/28/16.
 */
public class Function extends Sentence {

  static Map<String, ArrayList<Sort>> functionDeclarations = new HashMap<>();

  /**
   * Create a declaration for a function named s, taking arguments of argTypes arnd returning a returnType
   *
   * @param s          the name of the new Function declaration
   * @param returnType the return type of the Function being declared
   * @param argTypes   the argument types of the Function being declared
   * @return true if the declaration was successful, false otherwise
   */
  public static boolean addDeclaration(String s, Sort returnType, ArrayList<Sort> argTypes) {
    if (functionDeclarations.containsKey(s))
      return false;
    argTypes.add(0, returnType);
    functionDeclarations.put(s, argTypes);
    return true;
  }

  /**
   * Create a declaration for a function named s, taking arguments of argTypes arnd returning a returnType
   *
   * @param s          the name of the new Function declaration
   * @param returnType the return type of the Function being declared
   * @param argTypes   the argument types of the Function being declared
   * @return true if the declaration was successful, false otherwise
   */
  public static boolean addDeclaration(String s, Sort returnType, Sort... argTypes) {
    if (functionDeclarations.containsKey(s))
      return false;
    ArrayList<Sort> a = new ArrayList<>(Arrays.asList(argTypes));
    a.add(0, returnType);
    functionDeclarations.put(s, a);
    return true;
  }

  public Function(String n, Sort sort, Sentence... sentences) {
    super(new ArrayList<>(Arrays.asList(sentences)), n, n, sort);
  }

  public Function(String n, Sort sort, ArrayList<Sentence> sentences) {
    super(sentences, n, n, sort);
  }

  public String toString() {
    if (TOSTRING == null)
      TOSTRING = print(true);
    return TOSTRING;
  }

  public String toSExpression() {
    if (TOSEXPR == null)
      TOSEXPR = print(false);
    return TOSEXPR;
  }

  private String print(boolean prefix) {
    StringBuilder builder;
    if (prefix)
      builder = new StringBuilder().append(name).append("(");
    else
      builder = new StringBuilder().append("(").append(name).append(" ");

    String delim = prefix ? ", " : " ";
    ArrayList<Sentence> a = new ArrayList<>(args);
    if (!a.isEmpty())
      a.remove(a.size() - 1);

    a.forEach(arg -> {
      if (prefix)
        builder.append(arg).append(delim);
      else
        builder.append(arg.toSExpression()).append(delim);
    });

    if (!args.isEmpty()) {
      if (prefix)
        builder.append(args.get(args.size() - 1)).append(")");
      else
        builder.append(args.get(args.size() - 1).toSExpression()).append(")");
    } else
      builder.append(")");
    return builder.toString();
  }

  @Override
  public Boolean eval(TruthAssignment h) {
    if (h.isMapped(this))
      return h.models(this);
    return null;
  }

  @Override
  public Inference reason(TruthAssignment h, int inferenceNum, int justificationNum) {
    return null;
  }
}
