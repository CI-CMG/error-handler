package gov.noaa.ncei.mgg.errorhandler.controller;

import static org.mockito.Mockito.mock;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonMappingException.Reference;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.noaa.ncei.mgg.errorhandler.ApiError.ApiErrorBuilder;
import gov.noaa.ncei.mgg.errorhandler.ApiException;
import gov.noaa.ncei.mgg.errorhandler.model.SearchParameters;
import java.beans.PropertyChangeEvent;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import javax.validation.Valid;
import javax.validation.metadata.ConstraintDescriptor;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@SuppressWarnings("deprecation")
@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api/v1/test")
public class RestController {

  private final ObjectMapper objectMapper;

  public RestController(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @SuppressWarnings("unused")
  @GetMapping(path = "/handle-internal")
  public void testHandleInternal(@Valid SearchParameters searchParameters) {
  }

  @GetMapping(path = "/json-exception")
  public void testJsonException() throws JsonProcessingException {
    objectMapper.readValue("", Integer.class);
  }

  @GetMapping(path = "/constraint-violation")
  public void testConstraintViolation() {
    ConstraintViolation<Object> constraintViolation = new ConstraintViolation<Object>() {
      @Override
      public String getMessage() {
        return "Test invalid";
      }

      @Override
      public String getMessageTemplate() {
        return null;
      }

      @Override
      public Object getRootBean() {
        return null;
      }

      @Override
      public Class<Object> getRootBeanClass() {
        return null;
      }

      @Override
      public Object getLeafBean() {
        return null;
      }

      @Override
      public Object[] getExecutableParameters() {
        return new Object[0];
      }

      @Override
      public Object getExecutableReturnValue() {
        return null;
      }

      @Override
      public Path getPropertyPath() {
        return PathImpl.createPathFromString("TEST.TEST");
      }

      @Override
      public Object getInvalidValue() {
        return null;
      }

      @Override
      public ConstraintDescriptor<?> getConstraintDescriptor() {
        return null;
      }

      @Override
      public <U> U unwrap(Class<U> aClass) {
        return null;
      }
    };
    
    Set<ConstraintViolation<Object>> constraintViolations = new HashSet<>();
    constraintViolations.add(constraintViolation);
    
    throw new ConstraintViolationException(constraintViolations);
  }

  @GetMapping(path = "/api-exception")
  public void testApiException() throws JsonProcessingException {
    throw new ApiException(
        HttpStatus.NOT_FOUND,
        new ApiErrorBuilder().error("Test flash error")
            .fieldError("TEST", "Test field error")
            .additionalData(objectMapper.readTree("{\"TEST\": \"ADDITIONAL_DATA\"}"))
            .build()
    );
  }

  @GetMapping(path = "/exception")
  public void testException() throws Exception {
    throw new Exception("Test exception");
  }

  @GetMapping(path = "/media-type-exception")
  public void testMediaTypeException() throws Exception {
    throw new Exception("Test exception", new HttpMediaTypeNotAcceptableException(""));
  }

  @GetMapping(path = "/message-not-readable")
  public void testMessageNotReadable() {
    HttpInputMessage message = new MockHttpInputMessage("TEST MESSAGE".getBytes(StandardCharsets.UTF_8));
    throw new HttpMessageNotReadableException("TEST", message);
  }

  @GetMapping(path = "/message-not-readable-json")
  public void testMessageNotReadableJson() {
    HttpInputMessage message = new MockHttpInputMessage("TEST MESSAGE".getBytes(StandardCharsets.UTF_8));
    JsonMappingException exception = new JsonMappingException("TEST");
    exception.prependPath(new Reference("", "TEST"));
    exception.prependPath(new Reference("", 1));
    throw new HttpMessageNotReadableException("TEST", exception, message);
  }

  @GetMapping(path = "/missing-parameter")
  public void testMissingParameter() throws MissingServletRequestParameterException {
    throw new MissingServletRequestParameterException("TEST", "PARAMETER");
  }

  @GetMapping(path = "/type-mismatch")
  public void testTypeMismatch() {
    PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(
        "",
        "TEST",
        "",
        ""
    );
    throw new TypeMismatchException(propertyChangeEvent, String.class);
  }

  @GetMapping(path = "/type-mismatch-no-name")
  public void testTypeMismatchNoPropertyName() {
    PropertyChangeEvent propertyChangeEvent = new PropertyChangeEvent(
        "",
        "TEST",
        "",
        ""
    );
    throw new MethodArgumentTypeMismatchException(
        "",
        String.class,
        "TEST",
        mock(MethodParameter.class),
        new Exception()
    );
  }

  @GetMapping(path = "/invalid-argument")
  public void testInvalidArgument() throws MethodArgumentNotValidException {
    throw new MethodArgumentNotValidException(mock(MethodParameter.class), mock(BindingResult.class));
  }

}
