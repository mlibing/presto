/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner.plan;

import com.facebook.presto.spi.plan.PlanNodeId;
import com.facebook.presto.spi.relation.VariableReferenceExpression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.concurrent.Immutable;

import java.util.List;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@Immutable
public class MarkDistinctNode
        extends InternalPlanNode
{
    private final PlanNode source;
    private final VariableReferenceExpression markerVariable;

    private final Optional<VariableReferenceExpression> hashVariable;
    private final List<VariableReferenceExpression> distinctVariables;

    @JsonCreator
    public MarkDistinctNode(@JsonProperty("id") PlanNodeId id,
            @JsonProperty("source") PlanNode source,
            @JsonProperty("markerVariable") VariableReferenceExpression markerVariable,
            @JsonProperty("distinctVariables") List<VariableReferenceExpression> distinctVariables,
            @JsonProperty("hashVariable") Optional<VariableReferenceExpression> hashVariable)
    {
        super(id);
        this.source = source;
        this.markerVariable = markerVariable;
        this.hashVariable = requireNonNull(hashVariable, "hashVariable is null");
        requireNonNull(distinctVariables, "distinctVariables is null");
        checkArgument(!distinctVariables.isEmpty(), "distinctVariables cannot be empty");
        this.distinctVariables = ImmutableList.copyOf(distinctVariables);
    }

    @Override
    public List<VariableReferenceExpression> getOutputVariables()
    {
        return ImmutableList.<VariableReferenceExpression>builder()
                .addAll(source.getOutputVariables())
                .add(markerVariable)
                .build();
    }

    @Override
    public List<PlanNode> getSources()
    {
        return ImmutableList.of(source);
    }

    @JsonProperty
    public PlanNode getSource()
    {
        return source;
    }

    @JsonProperty
    public VariableReferenceExpression getMarkerVariable()
    {
        return markerVariable;
    }

    @JsonProperty
    public List<VariableReferenceExpression> getDistinctVariables()
    {
        return distinctVariables;
    }

    @JsonProperty
    public Optional<VariableReferenceExpression> getHashVariable()
    {
        return hashVariable;
    }

    @Override
    public <R, C> R accept(InternalPlanVisitor<R, C> visitor, C context)
    {
        return visitor.visitMarkDistinct(this, context);
    }

    @Override
    public PlanNode replaceChildren(List<PlanNode> newChildren)
    {
        return new MarkDistinctNode(getId(), Iterables.getOnlyElement(newChildren), markerVariable, distinctVariables, hashVariable);
    }
}
