package expression.metasentence;

import metareasoner.metainference.MetaInference;
import metareasoner.proof.Proof;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * The IFF class represents the metalogical IFF statement
 */
public class IFF extends MetaSentence {
    public IFF(MetaSentence s1, MetaSentence s2) {
        super(new ArrayList<>(Arrays.asList(s1, s2)), "IFF", "IFF");
    }

    public MetaInference reasonForwards(Proof p, int inferenceNum) {
        return null;
    }

    public MetaInference reasonBackwards(Proof p, int inferenceNum) {
        return null;
    }
}
