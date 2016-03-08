package logicalreasoner.prover;

import logicalreasoner.inference.Branch;
import logicalreasoner.inference.Decomposition;
import logicalreasoner.inference.Inference;
import logicalreasoner.sentence.Sentence;
import logicalreasoner.truthfunction.TruthAssignment;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * The SemanticProver class represents a logical reasoner
 * which determines the validity of arguments by
 * generating a TruthAssignment Tree (like a Truth Tree,
 * except that mappings from Sentences to their values are
 * stored in each node, not only the Sentences themselves).
 */
public class SemanticProver implements Runnable {

    //Stores the initial/root TruthAssignment
    TruthAssignment masterFunction;

    //The leaves of the TruthAssignment tree
    List<TruthAssignment> openBranches;

    //All statements which can be branched upon (in descending order of size)
    PriorityQueue<Branch> branchQueue;


    /**
     * Initialize the reasoner with the premises and the negation of all interests
     * @param premises the prior knowledge of the prover
     * @param interests the interests of the prover (to be negated)
     */
    public SemanticProver(Set<Sentence> premises, Set<Sentence> interests) {
        masterFunction = new TruthAssignment();
        premises.forEach(masterFunction::setTrue);
        interests.forEach(masterFunction::setFalse);

        openBranches = new ArrayList<TruthAssignment>() {{ add(masterFunction); }};

        branchQueue = new PriorityQueue<>((b1, b2) -> {
            if (b1.size() != b2.size())
                return b1.size() > b2.size() ? 1 : 0;
            return b1.getOrigin().size() > b2.getOrigin().size() ? 1 : 0;
        });
    }

    /**
     * Reason over the TruthAssignment h by decomposing statements
     * @param h the TruthAssignment to reason over
     * @return true if changes to h have been made as a result of this call, false otherwise
     */
    public boolean reason(TruthAssignment h) {
        TruthAssignment discoveries = new TruthAssignment();
        h.getSentencesUpwards().stream().filter(s -> !h.isDecomposed(s)).forEach(s -> {
            Inference i = s.reason(h);

            if (i instanceof Decomposition) {
                i.infer(discoveries);
            }
            else if (i instanceof Branch) {
                branchQueue.offer((Branch)i);
            }
        });

        if (discoveries.isEmpty())
            return false;
        h.merge(discoveries);
        return true;
    }

    /**
     * Run the prover over the given premises & conclusion
     */
    public void run() {
        while (!reasoningCompleted()) {
            boolean updated = true;

            // Always decompose all statements before branching
            while (!openBranches.isEmpty() && updated) {
                closeBranches();
                updated = openBranches.stream().anyMatch(this::reason);
            }

            closeBranches();

            //Branch once on the largest branching statement then loop back around
            if (!openBranches.isEmpty() && !branchQueue.isEmpty())
                addBranches();
        }

        //If the tree has been completely decomposed
        //without inconsistencies, the argument is invalid
        if (isConsistent()) {
            System.out.println("\nThe argument is NOT valid.\n");
        } else {
            System.out.println("\nThe argument IS valid.\n");
        }

        masterFunction.print();
    }

    /**
     * Check if all TruthAssignments are consistent and fully decomposed.
     * @return true if all open branches are fully decomposed
     */
    public boolean reasoningCompleted() {
        return openBranches.isEmpty() || (branchQueue.isEmpty() && openBranches.stream().allMatch(TruthAssignment::decomposedAll));
    }

    public boolean isConsistent() {
        return !openBranches.isEmpty() && openBranches.stream().allMatch(TruthAssignment::isConsistent);
    }

    /**
     * Print all inferences in the TruthAssignment
     */
    public void printInferences() {
        System.out.println("\nTree: ");
        masterFunction.print();
        System.out.println("Decomposed: ");
        masterFunction.printDecomposed();
    }

    /**
     * Print the Set of open Branches
     */
    public void printBranches() {
        System.out.println("Branches: ");
        openBranches.forEach(System.out::println);
        System.out.println();
    }

    /**
     * Branch the Sentence on the top of the branchQueue
     * and update the openBranches Set to contain those children.
     */
    public void addBranches() {
        Branch b = branchQueue.poll();
        b.getParent().setDecomposed(b.getOrigin());
        if (openBranches.isEmpty())  //Make sure no unnecessary branching occurs
            return;

        b.getParent().getLeaves().forEach(leaf -> {
            b.infer(leaf).stream().filter(l -> !openBranches.contains(l)).forEach(l -> openBranches.add(l));
            openBranches.remove(leaf);
        });

        closeBranches();    //Clean up any inconsistent branches
    }

    private void closeBranches() {
        openBranches.removeIf(b -> !b.isConsistent());
    }
}
