package org.statnlp.example.RelationLatent;

import edu.stanford.nlp.maxent.Feature;
import org.statnlp.hypergraph.FeatureArray;
import org.statnlp.hypergraph.FeatureManager;
import org.statnlp.hypergraph.GlobalNetworkParam;
import org.statnlp.hypergraph.Network;
import org.statnlp.util.instance_parser.InstanceParser;

import java.util.List;

public class LatentFeatureManager extends FeatureManager {
    public LatentFeatureManager(GlobalNetworkParam param_g) {
        super(param_g);
    }


    @Override
    protected FeatureArray extract_helper(Network network, int parent_k, int[] children_k, int children_k_index) {
        return null;
    }
}
