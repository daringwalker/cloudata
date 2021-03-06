package com.cloudata.structured.sql.simple;

import java.util.List;
import java.util.Map;

import com.facebook.presto.metadata.QualifiedTableName;
import com.facebook.presto.sql.planner.Symbol;

public class SimpleTableScan extends SimpleNode {

    public List<String> columnNames;
    public List<SimpleExpression> expressions;

    /**
     * The columns array contains expressions that are bound to the current table row.
     * 
     * When we advance the current table row, we refresh each of the columns, and this is how we update the expression
     * values.
     */
    SimpleColumnExpression[] columns;

    Map<Symbol, SimpleExpression> symbolToExpression;

    // final TableMetadata tableMetadata;
    public QualifiedTableName tableName;

    public SimpleTableScan(QualifiedTableName tableName) {
        this.tableName = tableName;
    }

    public SimpleExpression getExpression(Symbol symbol) {
        return symbolToExpression.get(symbol);
    }

    public String getTableName() {
        return tableName.getTableName();
    }

    @Override
    public <C, R> R accept(SimpleNodeVisitor<C, R> visitor, C context) {
        return visitor.visitTableScan(this, context);
    }

}