package com.geek.threaddoctor.diagnosis;

public record FollowUpEvidenceRequest(
        String title,
        String reason,
        String expectedFormat,
        String guidance) {
}
