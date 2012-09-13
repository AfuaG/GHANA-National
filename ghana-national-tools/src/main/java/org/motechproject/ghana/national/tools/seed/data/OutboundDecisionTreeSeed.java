package org.motechproject.ghana.national.tools.seed.data;

import org.motechproject.decisiontree.core.model.ITransition;
import org.motechproject.decisiontree.core.model.Node;
import org.motechproject.decisiontree.core.model.Tree;
import org.motechproject.decisiontree.core.repository.AllTrees;
import org.motechproject.ghana.national.domain.ivr.transition.PreferredLanguageTransition;
import org.motechproject.ghana.national.tools.seed.Seed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class OutboundDecisionTreeSeed extends Seed {
    @Autowired
    AllTrees allTrees;

    @Override
    protected void load() {
        Tree outboundDecisionTree = new Tree().setRootNode(new Node().setTransitions(new HashMap<String, ITransition>() {{
            put("?", new PreferredLanguageTransition());
        }}));

        outboundDecisionTree.setName("OutboundDecisionTree");
        allTrees.addOrReplace(outboundDecisionTree);
    }
}
