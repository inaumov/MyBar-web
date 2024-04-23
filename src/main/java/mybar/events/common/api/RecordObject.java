package mybar.events.common.api;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Jacksonized
@AllArgsConstructor(staticName = "of")
@Builder
@Getter
@NoArgsConstructor
@ToString
public class RecordObject<T> {
    public long timestamp;
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    public T value;
}
