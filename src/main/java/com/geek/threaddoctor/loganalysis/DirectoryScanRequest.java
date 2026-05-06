package com.geek.threaddoctor.loganalysis;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DirectoryScanRequest(@NotBlank @Size(max = 1024) String path) {
}
