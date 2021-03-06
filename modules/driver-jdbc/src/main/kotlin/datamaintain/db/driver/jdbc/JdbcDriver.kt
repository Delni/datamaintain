package datamaintain.db.driver.jdbc

import datamaintain.core.db.driver.DatamaintainDriver
import datamaintain.core.script.ExecutedScript
import datamaintain.core.script.ExecutionStatus
import datamaintain.core.script.ScriptAction
import datamaintain.core.script.ScriptWithContent
import datamaintain.core.step.executor.Execution
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException
import java.util.*

class JdbcDriver(jdbcUri: String) : DatamaintainDriver(jdbcUri) {
    private val connection: Connection = DriverManager.getConnection(jdbcUri)

    companion object {
        const val EXECUTED_SCRIPTS_TABLE = "executedScripts"
    }

    override fun executeScript(script: ScriptWithContent): Execution {
        return try {
            connection.createStatement().executeUpdate(script.content)
            Execution(ExecutionStatus.OK, null)
        } catch (e: SQLException) {
            Execution(ExecutionStatus.KO, e.message)
        }
    }

    override fun listExecutedScripts(): Sequence<ExecutedScript> {
        createExecutedScriptsTableIfNotExists()

        val statement = connection.createStatement()
        val executionOutput: ResultSet = statement.executeQuery("SELECT * from $EXECUTED_SCRIPTS_TABLE")
        val executedScript = mutableListOf<ExecutedScript>()
        while (executionOutput.next()) {
            executedScript.add(executionOutput.toExecutedScript())
        }
        return executedScript.asSequence()
    }

    override fun markAsExecuted(executedScript: ExecutedScript): ExecutedScript {
        val insertStmt = connection.prepareStatement("""
            INSERT INTO $EXECUTED_SCRIPTS_TABLE (id, name, checksum, identifier, executionStatus, action) 
            VALUES (?, ?, ?, ?, ?, ?)"""
        )

        try {
            connection.autoCommit = false
            with(executedScript) {
                insertStmt.setString(1, UUID.randomUUID().toString())
                insertStmt.setString(2, name)
                insertStmt.setString(3, checksum)
                insertStmt.setString(4, identifier)
                insertStmt.setString(5, executionStatus.name)
                insertStmt.setString(6, action!!.name)
            }
            insertStmt.execute()
            connection.commit()
        } catch (e: SQLException) {
            connection.rollback()
            throw IllegalStateException("Query $insertStmt fail with exit code ${e.errorCode} an output : ${e.message}")
        }
        return executedScript
    }

    fun createExecutedScriptsTableIfNotExists() {
        val tableCreationStatement = connection.prepareStatement("""
            CREATE TABLE IF NOT EXISTS $EXECUTED_SCRIPTS_TABLE (
                id VARCHAR(255) NOT NULL,
                name VARCHAR(255) NOT NULL,
                checksum VARCHAR(255) NOT NULL,
                identifier VARCHAR(255) NOT NULL,
                executionStatus VARCHAR(255) NOT NULL,
                action VARCHAR(255) NOT NULL,
                executionDurationInMillis INT,
                executionOutput VARCHAR(2047),
                PRIMARY KEY ( id )
            )""")
        tableCreationStatement.execute()
    }

    override fun overrideScript(executedScript: ExecutedScript): ExecutedScript {
        connection.prepareStatement("""
            UPDATE $EXECUTED_SCRIPTS_TABLE SET
            action = '${ScriptAction.OVERRIDE_EXECUTED.name}',
            checksum = '${executedScript.checksum}',
            executionStatus = '${ExecutionStatus.OK.name}'
            WHERE name = '${executedScript.name}'
        """).execute()

        executedScript.action = ScriptAction.OVERRIDE_EXECUTED
        return executedScript
    }

    fun ResultSet.toExecutedScript() = ExecutedScript(
            name = this.getString("name"),
            checksum = this.getString("checksum"),
            executionDurationInMillis = this.getLong("executionDurationInMillis"),
            executionOutput = this.getString("executionOutput"),
            executionStatus = ExecutionStatus.valueOf(this.getString("executionStatus")),
            identifier = this.getString("identifier"),
            action = ScriptAction.valueOf(this.getString("action"))
    )
}
