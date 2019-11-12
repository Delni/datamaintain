package datamaintain.report

import datamaintain.Script
import java.time.Instant

class ExecutionLineReport(
        override val date: Instant,
        override val message: String,
        override val executionStatus: ExecutionStatus,
        val script: Script
) : ScriptLineReport