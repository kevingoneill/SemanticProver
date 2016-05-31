package expression.sentence;

import expression.Sort;
import logicalreasoner.inference.Branch;
import logicalreasoner.inference.Decomposition;
import logicalreasoner.inference.Inference;
import logicalreasoner.truthassignment.TruthAssignment;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * The Or class represents the generalized logical disjunction
 * <p>
 * For example, (or A B), (or X Y Z)
 */
public class Or extends Sentence {

  public Or(ArrayList<Sentence> a) {
    super(a, "or", "∨", Sort.BOOLEAN);
  }

  public Boolean eval(TruthAssignment h) {
    //return pre-mapped value for this
    //if (h.isMapped(this))
    //    return h.models(this);

    //if any atoms are unmapped, return null
    if (args.contains(null))
      return null;

    //Create a new mapping in h for this with newly computed value
    //boolean val =
    return args.stream().anyMatch(arg -> arg.eval(h));
    //h.set(this, val);
    //return val;
  }

  @Override
  public Inference reason(TruthAssignment h, int inferenceNum, int justificationNum) {
    if (h.isMapped(this)) {
      if (h.models(this)) {
        Branch b = new Branch(h, this, inferenceNum, justificationNum);
        args.forEach(arg -> {
          TruthAssignment t = new TruthAssignment();
          t.setTrue(arg, inferenceNum);
          b.addBranch(t);
        });

        return b;
      } else {
        Decomposition d = new Decomposition(h, this, inferenceNum, justificationNum);
        args.forEach(d::setFalse);
        return d;
      }
    }

    return null;
  }

  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public Set<Constant> getConstants() {
    Set<Constant> s = new HashSet<>();
    args.forEach(a -> s.addAll(a.getConstants()));
    return s;
  }

  @Override
  public Sentence instantiate(Sentence c, Variable v) {
    ArrayList<Sentence> a = new ArrayList<>();
    args.forEach(arg -> a.add(arg.instantiate(c, v)));
    return new Or(a);
  }
}
