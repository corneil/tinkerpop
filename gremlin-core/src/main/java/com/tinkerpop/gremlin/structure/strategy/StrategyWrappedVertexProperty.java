package com.tinkerpop.gremlin.structure.strategy;

import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Property;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.VertexProperty;
import com.tinkerpop.gremlin.structure.util.StringFactory;
import com.tinkerpop.gremlin.util.StreamFactory;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
public class StrategyWrappedVertexProperty<V> extends StrategyWrappedElement implements VertexProperty<V>, StrategyWrapped {
    private final VertexProperty<V> baseVertexProperty;
    private final Strategy.Context<StrategyWrappedVertexProperty<V>> strategyContext;
    private final StrategyWrappedVertexPropertyIterators iterators;

    public StrategyWrappedVertexProperty(final VertexProperty<V> baseVertexProperty, final StrategyWrappedGraph strategyWrappedGraph) {
        super(baseVertexProperty, strategyWrappedGraph);
        this.strategyContext = new Strategy.Context<>(strategyWrappedGraph.getBaseGraph(), this);
        this.baseVertexProperty = baseVertexProperty;
        this.iterators = new StrategyWrappedVertexPropertyIterators();
    }

    @Override
    public Graph graph() {
        return this.strategyWrappedGraph.strategy().compose(
                s -> s.getVertexPropertyGraphStrategy(strategyContext),
                this.baseVertexProperty::graph).get();
    }

    @Override
    public Object id() {
        return this.strategyWrappedGraph.strategy().compose(
                s -> s.getVertexPropertyIdStrategy(strategyContext),
                this.baseVertexProperty::id).get();
    }

    @Override
    public String label() {
        return this.strategyWrappedGraph.strategy().compose(
                s -> s.getVertexPropertyLabelStrategy(strategyContext),
                this.baseVertexProperty::label).get();
    }

    @Override
    public Set<String> keys() {
        return this.strategyWrappedGraph.strategy().compose(
                s -> s.getVertexPropertyKeysStrategy(strategyContext),
                this.baseVertexProperty::keys).get();
    }

    @Override
    public Set<String> hiddenKeys() {
        return this.strategyWrappedGraph.strategy().compose(
                s -> s.getVertexPropertyHiddenKeysStrategy(strategyContext),
                this.baseVertexProperty::hiddenKeys).get();
    }

    @Override
    public Vertex element() {
        return new StrategyWrappedVertex(this.strategyWrappedGraph.strategy().compose(
                s -> s.getVertexPropertyGetElementStrategy(strategyContext),
                this.baseVertexProperty::element).get(), strategyWrappedGraph);
    }

    @Override
    public VertexProperty.Iterators iterators() {
        return this.iterators;
    }

    @Override
    public <U> Property<U> property(final String key, final U value) {
        return new StrategyWrappedProperty<U>(this.strategyWrappedGraph.strategy().compose(
                s -> s.<U, V>getVertexPropertyPropertyStrategy(strategyContext),
                this.baseVertexProperty::property).<String,U>apply(key, value), this.strategyWrappedGraph);
    }

    @Override
    public String key() {
        return this.baseVertexProperty.key();
    }

    @Override
    public V value() throws NoSuchElementException {
        return this.baseVertexProperty.value();
    }

    @Override
    public boolean isHidden() {
        return this.baseVertexProperty.isHidden();
    }

    @Override
    public boolean isPresent() {
        return this.baseVertexProperty.isPresent();
    }

    @Override
    public void remove() {
        this.strategyWrappedGraph.strategy().compose(
                s -> s.getRemoveVertexPropertyStrategy(strategyContext),
                () -> {
                    this.baseVertexProperty.remove();
                    return null;
                }).get();
    }

    @Override
    public String toString() {
        final GraphStrategy strategy = strategyWrappedGraph.strategy().getGraphStrategy().orElse(GraphStrategy.DefaultGraphStrategy.INSTANCE);
        return StringFactory.graphStrategyPropertyString(strategy, this.baseVertexProperty);
    }

    public class StrategyWrappedVertexPropertyIterators implements VertexProperty.Iterators {
        @Override
        public <U> Iterator<Property<U>> propertyIterator(final String... propertyKeys) {
            return StreamFactory.stream(strategyWrappedGraph.strategy().compose(
                    s -> s.<U,V>getVertexPropertyIteratorsPropertiesStrategy(strategyContext),
                    (String[] pks) -> baseVertexProperty.iterators().propertyIterator(pks)).apply(propertyKeys))
                    .map(property -> (Property<U>) new StrategyWrappedProperty<>(property, strategyWrappedGraph)).iterator();
        }

        @Override
        public <U> Iterator<Property<U>> hiddenPropertyIterator(final String... propertyKeys) {
            return StreamFactory.stream(strategyWrappedGraph.strategy().compose(
                    s -> s.<U,V>getVertexPropertyIteratorsHiddensStrategy(strategyContext),
                    (String[] pks) -> baseVertexProperty.iterators().hiddenPropertyIterator(pks)).apply(propertyKeys))
                    .map(property -> (Property<U>) new StrategyWrappedProperty<>(property, strategyWrappedGraph)).iterator();
        }

        @Override
        public <U> Iterator<U> valueIterator(final String... propertyKeys) {
            return strategyWrappedGraph.strategy().compose(
                    s -> s.<U,V>getVertexPropertyIteratorsValuesStrategy(strategyContext),
                    (String[] pks) -> baseVertexProperty.iterators().valueIterator(pks)).apply(propertyKeys);
        }

        @Override
        public <U> Iterator<U> hiddenValueIterator(final String... propertyKeys) {
            return strategyWrappedGraph.strategy().compose(
                    s -> s.<U,V>getVertexPropertyIteratorsHiddenValuesStrategy(strategyContext),
                    (String[] pks) -> baseVertexProperty.iterators().hiddenValueIterator(pks)).apply(propertyKeys);
        }
    }
}
