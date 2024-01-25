package jenkins.model.lazy;

static enum Boundary {
  LOWER(-1, -1),
  HIGHER(1, 0),
  FLOOR(0, -1),
  CEIL(0, 0);
  
  private final int offsetOfExactMatch;
  
  private final int offsetOfInsertionPoint;
  
  Boundary(int offsetOfExactMatch, int offsetOfInsertionPoint) {
    this.offsetOfExactMatch = offsetOfExactMatch;
    this.offsetOfInsertionPoint = offsetOfInsertionPoint;
  }
  
  public int apply(int binarySearchOutput) {
    if (binarySearchOutput >= 0)
      return binarySearchOutput + this.offsetOfExactMatch; 
    int ip = -(binarySearchOutput + 1);
    return ip + this.offsetOfInsertionPoint;
  }
}
