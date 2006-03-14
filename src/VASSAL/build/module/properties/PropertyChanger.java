package VASSAL.build.module.properties;

/**
 * Provides a new value for a global property
 * 
 * @author rkinney
 * 
 */
public class PropertyChanger {
  private String newValue;

  public PropertyChanger() {
    this(null);
  }

  public PropertyChanger(String newValue) {
    this.newValue = newValue;
  }

  public String getNewValue(String oldValue) {
    return newValue;
  }

  public void setNewValue(String newValue) {
    this.newValue = newValue;
  }

}
