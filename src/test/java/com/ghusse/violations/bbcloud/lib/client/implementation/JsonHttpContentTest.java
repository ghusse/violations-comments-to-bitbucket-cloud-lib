package com.ghusse.violations.bbcloud.lib.client.implementation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.JsonHttpContent;
import com.ghusse.ci.violations.bbcloud.lib.client.implementation.JsonHttpContentFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.OutputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class JsonHttpContentTest {
  @Mock
  private ObjectMapper mapper;

  @Mock
  private OutputStream stream;

  @InjectMocks
  private JsonHttpContentFactory factory;

  @Test
  public void itShouldWriteTheMappedValueToTheStream() throws IOException {
    Object value = new Object();
    JsonHttpContent target = factory.create(value);

    target.writeTo(this.stream);

    verify(this.mapper, times(1)).writeValue(this.stream, value);
  }

  public void itShouldDoNothingIfTheValueIsNull() throws IOException {
    JsonHttpContent target = factory.create(null);

    target.writeTo(this.stream);

    verify(this.mapper, never()).writeValue(any(OutputStream.class), any(Object.class));
  }
}
