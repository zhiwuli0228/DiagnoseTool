package com.geek.threaddoctor.loganalysis;

import jakarta.validation.constraints.NotBlank;

public record DirectoryScanRequest(@NotBlank String path) {
}
