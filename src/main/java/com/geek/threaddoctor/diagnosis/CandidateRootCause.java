package com.geek.threaddoctor.diagnosis;

import java.util.List;

public record CandidateRootCause(String causeId, String title, String description, double score, List<String> supportingEvidenceIds, List<String> riskNotes) {
}
