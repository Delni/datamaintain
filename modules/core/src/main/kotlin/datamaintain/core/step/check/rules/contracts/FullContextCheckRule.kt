package datamaintain.core.step.check.rules.contracts

import datamaintain.core.script.ExecutedScript
import datamaintain.core.script.ScriptWithContent
import datamaintain.core.step.check.rules.CheckRule

abstract class FullContextCheckRule(
        val executedScripts: Sequence<ExecutedScript>
): CheckRule {
    abstract fun check(scripts: Sequence<ScriptWithContent>)
}