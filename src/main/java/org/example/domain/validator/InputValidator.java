package org.example.domain.validator;

import io.micrometer.common.util.StringUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.example.infrastructure.adapters.input.rest.messages.ErrorMessages;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InputValidator {
    public static void validateInput(String input) {
        if (StringUtils.isEmpty(input) || StringUtils.isBlank(input) || StringUtils.isEmpty(input.trim())) {
            throw new IllegalArgumentException(ErrorMessages.EMPTY_INPUT_ERROR);
        }
    }
}
