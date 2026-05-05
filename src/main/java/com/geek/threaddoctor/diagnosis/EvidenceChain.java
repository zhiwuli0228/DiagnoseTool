package com.geek.threaddoctor.diagnosis;

import java.util.List;

public record EvidenceChain(String conclusion, List<String> evidences) {
}
