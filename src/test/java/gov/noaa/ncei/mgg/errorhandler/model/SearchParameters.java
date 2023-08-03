package gov.noaa.ncei.mgg.errorhandler.model;

import javax.validation.constraints.NotBlank;

public class SearchParameters {

  @NotBlank
  private String parameter;

  public String getParameter() {
    return parameter;
  }

  public void setParameter(String parameter) {
    this.parameter = parameter;
  }
}
