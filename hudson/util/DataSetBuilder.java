package hudson.util;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public final class DataSetBuilder<Row extends Comparable, Column extends Comparable> extends Object {
  private List<Number> values = new ArrayList();
  
  private List<Row> rows = new ArrayList();
  
  private List<Column> columns = new ArrayList();
  
  public void add(Number value, Row rowKey, Column columnKey) {
    this.values.add(value);
    this.rows.add(rowKey);
    this.columns.add(columnKey);
  }
  
  public CategoryDataset build() {
    DefaultCategoryDataset ds = new DefaultCategoryDataset();
    TreeSet<Row> rowSet = new TreeSet<Row>(this.rows);
    TreeSet<Column> colSet = new TreeSet<Column>(this.columns);
    Comparable[] _rows = (Comparable[])rowSet.toArray(new Comparable[0]);
    Comparable[] _cols = (Comparable[])colSet.toArray(new Comparable[0]);
    for (Comparable r : _rows)
      ds.setValue(null, r, _cols[0]); 
    for (Comparable c : _cols)
      ds.setValue(null, _rows[0], c); 
    for (int i = 0; i < this.values.size(); i++)
      ds.addValue((Number)this.values.get(i), (Comparable)this.rows.get(i), (Comparable)this.columns.get(i)); 
    return ds;
  }
}
