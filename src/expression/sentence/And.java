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
 * The And class represents the generalized logical conjunction
 * <p>
 * For example, (and A B), (and X Y Z)
 */
public class And extends Sentence {
  public And(ArrayList<Sentence> a) {
    super(a, "and", "∧", Sort.BOOLEAN);
  }

  public Boolean eval(TruthAssignment h) {
    //return pre-mapped value for this
    if (h.isMapped(this))
      return h.models(this);

    //if any atoms are unmapped, return null
    if (args.contains(null))
      return null;

    return args.stream().allMatch(arg -> arg.eval(h));
  }

  @Override
  public Inference reason(TruthAssignment h, int inferenceNum, int justificationNum) {
    if (h.isMapped(this)) {
      if (h.models(this)) {
        Decomposition d = new Decomposition(h, this, inferenceNum, justificationNum);
        args.forEach(d::setTrue);
        return d;
      } else {
        Branch b = new Branch(h, this, inferenceNum, justificationNum);
        args.forEach(arg -> {
          TruthAssignment t = new TruthAssignment();
          t.setFalse(arg, inferenceNum);
          b.addBranch(t);
        });
        return b;
      }
    }

    return null;
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
    return new And(a);
  }
}
