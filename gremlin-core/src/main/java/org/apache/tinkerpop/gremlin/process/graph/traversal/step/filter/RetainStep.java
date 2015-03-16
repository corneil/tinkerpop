/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.gremlin.process.graph.traversal.step.filter;

import org.apache.tinkerpop.gremlin.process.Traversal;
import org.apache.tinkerpop.gremlin.process.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.process.traverser.TraverserRequirement;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public final class RetainStep<S> extends FilterStep<S> {

    private final String sideEffectKeyOrPathLabel;
    private final Collection<S> retainCollection;
    private final S retainObject;
    private final short choice;

    public RetainStep(final Traversal.Admin traversal, final String sideEffectKeyOrPathLabel) {
        super(traversal);
        this.sideEffectKeyOrPathLabel = sideEffectKeyOrPathLabel;
        this.retainCollection = null;
        this.retainObject = null;
        this.choice = 0;
    }

    public RetainStep(final Traversal.Admin traversal, final Collection<S> retainCollection) {
        super(traversal);
        this.sideEffectKeyOrPathLabel = null;
        this.retainCollection = retainCollection;
        this.retainObject = null;
        this.choice = 1;
    }

    public RetainStep(final Traversal.Admin traversal, final S retainObject) {
        super(traversal);
        this.sideEffectKeyOrPathLabel = null;
        this.retainCollection = null;
        this.retainObject = retainObject;
        this.choice = 2;
    }

    @Override
    protected boolean filter(final Traverser.Admin<S> traverser) {
        switch (this.choice) {
            case 0: {
                final Object retain = traverser.asAdmin().getSideEffects().exists(this.sideEffectKeyOrPathLabel) ?
                        traverser.sideEffects(this.sideEffectKeyOrPathLabel) :
                        traverser.path(this.sideEffectKeyOrPathLabel);
                return retain instanceof Collection ?
                        ((Collection) retain).contains(traverser.get()) :
                        retain.equals(traverser.get());
            }
            case 1:
                return this.retainCollection.contains(traverser.get());
            default:
                return this.retainObject.equals(traverser.get());
        }
    }

    @Override
    public String toString() {
        return TraversalHelper.makeStepString(this, this.sideEffectKeyOrPathLabel);
    }

    @Override
    public Set<TraverserRequirement> getRequirements() {
        return null == this.sideEffectKeyOrPathLabel ?
                Collections.singleton(TraverserRequirement.OBJECT) :
                Stream.of(TraverserRequirement.OBJECT,
                        TraverserRequirement.SIDE_EFFECTS,
                        TraverserRequirement.PATH_ACCESS).collect(Collectors.toSet());
    }
}
