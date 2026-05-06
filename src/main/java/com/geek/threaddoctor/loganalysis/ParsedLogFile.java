/**
 * Copyright &copy; 2026-2026 zhiwu Technologies Co., Ltd. All rights reserved.
 */

package com.geek.threaddoctor.loganalysis;

import java.util.List;

record ParsedLogFile(LogFileSummary summary, List<LogEvent> events) {
}
