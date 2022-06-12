import groovy.json.JsonSlurper
import groovy.sql.Sql
@GrabConfig(systemClassLoader = true)
@Grab(group = 'mysql', module = 'mysql-connector-java', version = '5.1.49')

import groovy.xml.XmlUtil

import javax.swing.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.time.LocalDateTime

/**
 * Test steps:
 * cd to student's project folder
 * make project compatible with testing environment
 * build a package from the project via maven
 * start web server on localhost:8080
 * test if server is listening on the right address and port
 * run test collection via newman
 * aggregate tests result
 * evaluate tests result
 * push feedback to student's repo
 */

// CONSTANTS
Boolean ALL = false
String CWD = new File("..").absolutePath
String TEST_PROJECT = new File(CWD + File.separator + "b-vsa-ls22-project2").absolutePath
String STUDENT_GROUP = "a"
String FEEDBACK_DIR = "feedback"
def DB = [url : "jdbc:mysql://localhost:3306/VSA_PR2?useUnicode=true&characterEncoding=UTF-8",
          user: 'vsa', password: 'vsa', driver: 'com.mysql.jdbc.Driver']
def CSV_HEADER = ['AISID', 'Name', 'Email', 'GitHub', 'Tests Run', 'Succeeded', 'Failures', 'Errors', 'Skipped', 'Points',
                  'Bonus Tests Run', 'Bonus Succeeded', 'Bonus Failures', 'Bonus Errors', 'Bonus Skipped', 'Bonus Points', 'Total Points', 'Notes']
String CSV_DELIMITER = ';'
String MAVEN_OUTPUT = 'maven-output.txt'
String MAVEN_ERRORS = 'maven-error-output.txt'
String NEWMAN_OUTPUT = 'postman-test-outputs.txt'
String NEWMAN_ERRORS = 'postman-test-errors.txt'
String NEWMAN_RESULTS = 'postman-tests-report.json'
String WS_OUTPUT = 'web-server-output.txt'
String WS_ERROR = 'web-server-error.txt'
Integer MAX_POINTS = 20
Integer MAX_BONUS_POINTS = 5
String POSTMAN_ENV_FILE = new File(CWD + File.separator + 'VSA_env.postman_environment.json').absolutePath
String TEST_DIR = CWD + File.separator + 'tests' + File.separator + 'required'
String TEST_BONUS_DIR = CWD + File.separator + 'tests' + File.separator + 'bonus'
String TEST_WS_URL = "http://localhost:8080/users"

def input = JOptionPane.&showInputDialog
def confirm = JOptionPane.&showConfirmDialog


// CLASSES
class Student {
    String aisId
    String name
    String repo
    String email

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, aisId, name, email, repo)
    }

    Node toXml() {
        return new NodeBuilder().student {
            'aisId'(aisId)
            'name'(name)
            'email'(email)
        } as Node
    }
}

class TestsRun {
    Integer totalRun = 0
    Integer success = 0
    Integer failure = 0
    Integer skip = 0
    Integer points = 0

    void calcPoints(Double maxPoints = 20.0) {
        if (success == 0)
            calcSuccess()
        points = Math.ceil((maxPoints / (totalRun.doubleValue() - skip.doubleValue())) * success.doubleValue()).intValue()
    }

    void calcSuccess() {
        success = totalRun - (failure + skip)
    }

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, [
                totalRun as String,
                success as String,
                failure as String,
                skip as String,
                points as String
        ])
    }

    Node toXml() {
        return new NodeBuilder().testsRun {
            'totalRun'(totalRun)
            'success'(success)
            'failure'(failure)
            'skip'(skip)
            'points'(points)
        } as Node
    }
}

class Evaluation {
    String exam = 'B-VSA 21/22 Semestrálny projekt 2'
    Student student = new Student()
    String github = ''
    TestsRun required = new TestsRun()
    TestsRun bonus = new TestsRun()
    Integer totalPoints = 0
    String notes = ''
    Duration testDuration

    TestsRun getTotalTestsRun() {
        TestsRun total = new TestsRun(
                totalRun: required.totalRun,
                success: required.success,
                failure: required.failure,
                skip: required.skip,
                points: required.points
        )
        total.totalRun += bonus.totalRun
        total.success += bonus.success
        total.failure += bonus.failure
        total.skip += bonus.skip
        total.points += bonus.points
        return total
    }

    Integer calcTotalPoints(int max, int bon) {
        required.calcPoints(max.doubleValue())
        bonus.calcPoints(bon.doubleValue())
        totalPoints = required.points + bonus.points
        return totalPoints
    }

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, [
                student.toString(delimiter),
                github,
                required.toString(delimiter),
                bonus.toString(delimiter),
                totalPoints as String,
                notes])
    }

    Node toXml() {
        Node xml = new NodeBuilder().evaluation {
            'exam'(exam)
            'student'('Dummy student')
            'github'(github)
            'testsRuns' {
                'required' {
                    'tests'('Tests placeholder')
                }
                'bonus' {
                    'tests'('Tests placeholder')
                }
            }
            'totalPoints'(totalPoints)
            'notes'(notes)
            'testDuration'(testDuration.toString())
        }
        (xml.student[0] as Node).replaceNode(student.toXml())
        (xml.testsRuns.required.tests[0] as Node).replaceNode(required.toXml())
        (xml.testsRuns.bonus.tests[0] as Node).replaceNode(bonus.toXml())
        return xml
    }
}

List<Evaluation> results = []

void removeFrom(File file) {
    if (file.isFile()) {
        file.delete()
        return
    }
    File[] files = file.listFiles()
    for (File f : files) {
        if (f.isDirectory()) {
            removeFrom(f)
            f.deleteDir()
            continue
        }
        f.delete()
    }
}

def copyDir = { File from, File to ->
    to.mkdirs()
    File[] files = from.listFiles()
    for (File file : files) {
        if (file.isDirectory()) continue
        File target = new File(to.absolutePath + File.separator + file.getName())
        Files.copy(
                file.toPath(),
                target.toPath(),
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING
        )
        target.text = target.text.replace('sk.stuba.fei.uim.vsa.pr1', 'sk.stuba.fei.uim.vsa.pr1' + STUDENT_GROUP)
    }
}

def createDeps = { Node pom, String group, String artifact, String versionValue, Boolean testScope ->
    Node dep = pom.dependencies.dependency.find { it.groupId[0].text() == group && it.artifactId[0].text() == artifact } as Node
    Node newDep = new NodeBuilder().dependency() {
        groupId(group)
        artifactId(artifact)
        version(versionValue)
        if (testScope) {
            scope('test')
        }
    }
    if (dep) {
        dep.replaceNode(newDep)
    } else {
        pom.dependencies[0].append(newDep)
    }
}

def editPom = { File project ->
    def pomFile = new File(project.absolutePath + File.separator + "pom.xml")
    if (!pomFile.exists()) throw new RuntimeException("Cannot find pom.xml file")
    def pom = new XmlParser().parse(pomFile)

    createDeps(pom, 'mysql', 'mysql-connector-java', '5.1.49', false)

    def build = new NodeBuilder().build() {
        plugins() {
            plugin() {
                groupId('org.apache.maven.plugins')
                artifactId('maven-assembly-plugin')
                executions() {
                    execution() {
                        phase('package')
                        goals() {
                            goal('single')
                        }
                        configuration() {
                            archive() {
                                manifest() {
                                    mainClass('sk.stuba.fei.uim.vsa.pr2.Project2')
                                }
                            }
                            descriptorRefs() {
                                descriptorRef('jar-with-dependencies')
                            }
                        }
                    }
                }
            }
        }
    }
    if (pom.build.size() > 0) {
        pom.build[0].replaceNode(build)
    } else {
        pom.append(build)
    }

    def xmlString = XmlUtil.serialize(pom)
    pomFile.text = xmlString

    if (pom.developers.developer.size() > 0) {
        Node dev = pom.developers.developer[0]
        String[] parts = project.name.split('-')
        return new Student(
                'aisId': dev.id?.text()?.trim()?.replace('\n', '')?.replace('\t', ''),
                'name': dev.name?.text()?.trim()?.replace('\n', '')?.replace('\t', ''),
                'email': dev.email?.text()?.trim()?.replace('\n', '')?.replace('\t', ''),
                'repo': parts[parts.length - 1]
        )
    }
    return null
}

def editProperty = { Node properties, String name, String value ->
    def prop = properties.property.find { it.'@name' == name }
    if (prop) {
        prop.'@value' = value
    } else {
        properties.append(new NodeBuilder().property(name: name, value: value) {})
    }
}

def editPersistence = { File project ->
    def persistFile = new File(project.absolutePath + File.separator + String.join(File.separator, ['src', 'main', 'resources', 'META-INF', 'persistence.xml']))
    if (!persistFile.exists()) throw new RuntimeException("Cannot find persistence.xml file")
    def persist = new XmlParser().parse(persistFile)
    def punit = persist?.'persistence-unit'[0] as Node
    if (!punit) throw new RuntimeException("persistence.xml file is setup incorrectly! persistence-unit was not found.")
    def props = punit?.'properties'[0]
    if (!props) {
        println "\tProperties of persistence.xml was not found!"
        props = new NodeBuilder().'properties'() {}
        punit.append(props)
    }
    editProperty(props as Node, 'javax.persistence.jdbc.driver', 'com.mysql.jdbc.Driver')
    editProperty(props as Node, 'javax.persistence.jdbc.url', 'jdbc:mysql://localhost:3306/VSA_PR2?serverTimezone=UTC')
    editProperty(props as Node, 'javax.persistence.jdbc.user', 'vsa')
    editProperty(props as Node, 'javax.persistence.jdbc.password', 'vsa')
    editProperty(props as Node, 'javax.persistence.schema-generation.database.action', 'drop-and-create')
    editProperty(props as Node, 'eclipselink.target-database', 'MySQL')
    editProperty(props as Node, 'eclipselink.logging.level.sql', 'FINE')
    editProperty(props as Node, 'eclipselink.logging.parameters', 'true')
    def xmlString = XmlUtil.serialize(persist)
    persistFile.text = xmlString
}

def clearDatabase = {
    def sql = Sql.newInstance(DB.url, DB.user, DB.password, DB.driver)
    if (!sql)
        throw new RuntimeException("Cannot connect to the MySQL DB")
    sql.execute 'SET FOREIGN_KEY_CHECKS = 0'
    sql.execute 'SET GROUP_CONCAT_MAX_LEN = 32768'
    sql.execute 'SET @tables = NULL'
    sql.execute '''
        SELECT GROUP_CONCAT('`', table_name, '`') INTO @tables
        FROM information_schema.tables
        WHERE table_schema = (SELECT DATABASE())
    '''
    sql.execute 'SELECT IFNULL(@tables,\'dummy\') INTO @tables'
    sql.execute 'SET @tables = CONCAT(\'DROP TABLE IF EXISTS \', @tables)'
    sql.execute 'PREPARE stmt FROM @tables'
    sql.execute 'EXECUTE stmt'
    sql.execute 'DEALLOCATE PREPARE stmt'
    sql.execute 'SET FOREIGN_KEY_CHECKS = 1'
    sql.close()
}

def buildProject = { File project, File output, File errors ->
    def args = ['cmd', '/c', 'mvn', '-DskipTests=true', 'clean', 'compile', 'package']
    def builder = new ProcessBuilder(args)
    builder.directory(project)
    builder.redirectOutput(output)
    builder.redirectError(errors)
    def process = builder.start()
    process.waitFor()
}

def runProject = { File project, File output, File error ->
    def targetDir = new File(project.absolutePath + File.separator + 'target')
    if (!targetDir.exists())
        throw new RuntimeException('No target folder in project')
    File jarFile = Arrays.asList(targetDir.listFiles()).find { it.name.contains('jar-with-dependencies.jar') }
    if (!jarFile || !jarFile.exists())
        throw new RuntimeException('No executable jar file has been found!')
    println "Executing ${jarFile.name}"
    def args = ['java', '-jar', jarFile.name]
    def builder = new ProcessBuilder(args)
    builder.directory(targetDir)
    builder.redirectOutput(output)
    builder.redirectError(error)
    def process = builder.start()
    Thread.sleep(3000)
    if (!process.isAlive())
        throw new RuntimeException('Web server process is not running!')
    return process
}

def stopServer = { Process process ->
    println "Stopping project's web server"
    process.destroy()
    process.waitForOrKill(1000)
    process.destroyForcibly()
    process.waitFor()
}

def testWebServerReadiness = {
    def get = new URL(TEST_WS_URL).openConnection()
    get.requestMethod = 'GET'
    get.addRequestProperty("Accept", "application/json")
    get.addRequestProperty('Authorization', "Basic ${new String(Base64.getEncoder().encode("admin@vsa.sk:1".bytes))}")
    get.setConnectTimeout(5000)
    get.setReadTimeout(5000)
    get.doOutput = true
    println("Request response code: ${get.responseCode}")
    def response = get.getInputStream().getText()
    println("Test request to $TEST_WS_URL returned: $response")
    if (!response || response.isEmpty())
        throw new RuntimeException("Test request to $TEST_WS_URL has returned nothing!")
}

def runPostmanTest = { File collection, File output, File errors, String jsonResults ->
    println "Running postman collection ${collection.name}"
    def args = ['cmd', '/c', 'newman', 'run', collection.absolutePath, '-k', '-e', POSTMAN_ENV_FILE,
                '-r', 'cli,json', '--reporter-json-export', jsonResults, '--reporter-cli-no-banner',
                '--reporter-cli-show-timestamps']
    def builder = new ProcessBuilder(args)
    builder.redirectOutput(output)
    builder.redirectError(errors)
    def process = builder.start()
    process.waitFor()
}

def evaluateTests = { File postmanResults, boolean bonusTests ->
    TestsRun run = new TestsRun()
    def report = new JsonSlurper().parse(postmanResults)
    report.each { test ->
        test.run.executions.each { exec ->
            run.totalRun++
            if (exec.assertions.any { it.error != null }) {
                run.failure++
            }
        }
    }
    int max = bonusTests ? MAX_BONUS_POINTS : MAX_POINTS
    run.calcSuccess()
    run.calcPoints(max.doubleValue())
    return run
}

def buildSummaryFile = { Evaluation eval, File project ->
    def summaryFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'summary.xml')
    summaryFile.text = XmlUtil.serialize(eval.toXml())
}

def testStudent = { File project ->
    if (!project.isDirectory()) return null
    LocalDateTime startOfTest = LocalDateTime.now()
    println "Starting evaluation of student ${project.getName()}"
    Evaluation student = new Evaluation()
    student.github = project.getName()
    Process webServer = null
    println "Creating feedback folder"
    new File(project.absolutePath + File.separator + FEEDBACK_DIR).mkdirs()
    File mvnOutFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + MAVEN_OUTPUT)
    File mvnErrFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + MAVEN_ERRORS)
    File newmanOutput = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + NEWMAN_OUTPUT)
    File newmanErrors = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + NEWMAN_ERRORS)
    File bonusNewmanOutput = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'bonus-' + NEWMAN_OUTPUT)
    File bonusNewmanErrors = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'bonus-' + NEWMAN_ERRORS)
    File newmanJsonFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + NEWMAN_RESULTS)
    File serverOutput = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + WS_OUTPUT)
    File serverError = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + WS_ERROR)
    mvnOutFile.text = ''
    mvnErrFile.text = ''
    newmanOutput.text = ''
    newmanErrors.text = ''
    bonusNewmanOutput.text = ''
    bonusNewmanErrors.text = ''
    newmanJsonFile.text = '['
    serverOutput.text = ''
    serverError.text = ''

    try {
        println "Editing pom.xml"
        Student studentId = editPom(project)
        if (!studentId) {
            println "\t Cannot parse student from pom.xml"
            studentId = new Student('name': project.getName())
        }
        student.student = studentId
        println "\t Detected student credentials: ${student.student}"

        println "Editing persistence.xml"
        editPersistence(project)

        println "Clearing database before tests"
        clearDatabase()

        println "Building project"
        buildProject(project, mvnOutFile, mvnErrFile)

        println "Starting project's web server"
        webServer = runProject(project, serverOutput, serverError)

//        println "Test if webserver is running"
//        testWebServerReadiness()

        println "Required Tests Run"
        File tmpOutput = new File(TEST_DIR + File.separator + '..' + File.separator + 'tmp-output.txt')
        File tmpError = new File(TEST_DIR + File.separator + '..' + File.separator + 'tmp-error.txt')
        File tmpJson = new File(TEST_DIR + File.separator + '..' + File.separator + 'tmp-report.json')
        if (!webServer.isAlive())
            throw new RuntimeException('Web server is not alive to test')
        def testDir = new File(TEST_DIR).listFiles()
        def testDirSize = testDir.length
        testDir.eachWithIndex { file, index ->
            if (!file.name.endsWith('json')) return
            tmpOutput.text = ''
            tmpError.text = ''
            tmpJson.text = ''
            runPostmanTest(file, tmpOutput, tmpError, tmpJson.absolutePath)
            newmanOutput << tmpOutput.text
            newmanOutput << '\n-----------------------\n\n'
            if (!tmpError.text.isEmpty()) {
                newmanErrors << tmpError.text
                newmanErrors << '\n-----------------------\n\n'
            }
            if (!tmpJson.text.isEmpty()) {
                newmanJsonFile << tmpJson.text
                if ((index + 1) != testDirSize) {
                    newmanJsonFile << ','
                }
            }
        }
        newmanJsonFile << ']'

        student.required = evaluateTests(newmanJsonFile, false)
        println "Required tests result: ${student.required}"

        // TODO group endpoints tests

        println "Bonus Tests Run"
        if (!webServer.isAlive())
            throw new RuntimeException('Web server is not alive to test')
        new File(TEST_BONUS_DIR).listFiles().each {
            if (!it.name.endsWith('json')) return
            tmpOutput.text = ''
            tmpError.text = ''
            runPostmanTest(it, tmpOutput, tmpError, tmpJson.absolutePath)
            bonusNewmanOutput << tmpOutput.text
            bonusNewmanOutput << '\n-----------------------\n\n'
            if (!tmpError.text.isEmpty()) {
                bonusNewmanErrors << tmpError.text
                bonusNewmanErrors << '\n-----------------------\n\n'
            }
        }

        stopServer(webServer)

        println "Finalizing results"
        student.calcTotalPoints(MAX_POINTS, MAX_BONUS_POINTS)
        TestsRun totalTests = student.getTotalTestsRun()
        println "Results: Run: ${totalTests.totalRun}, Success: ${totalTests.success}, Failure: ${totalTests.failure}, Skip: ${totalTests.skip}, Points: ${totalTests.points}"
    } catch (Exception ex) {
        if (webServer) {
            stopServer(webServer)
        }
        ex.printStackTrace()
        mvnErrFile << "\n"
        mvnErrFile << ex.message
        student.notes += ex.message
    }
    if (mvnOutFile.text.contains('BUILD FAILURE')) {
        if (!student.notes.isEmpty()) student.notes += ';'
        if (mvnOutFile.text.contains('COMPILATION ERROR'))
            student.notes += 'Maven Compilation Error'
        else if (mvnOutFile.text.contains('T E S T S'))
            student.notes += 'Maven Tests failed'
    }
    student.testDuration = Duration.between(startOfTest, LocalDateTime.now())
    buildSummaryFile(student, project)
    println "Test took ${student.testDuration.toString()}"
    println "-----------------------------\n"
    if (webServer) {
        stopServer(webServer)
    }
    return student
}

LocalDateTime startOfScript = LocalDateTime.now()
JFrame frame = new JFrame("VSA Project 2 Batch testing")
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
def confirmation = confirm frame, "Do you want to test all projects?", "VSA Project 2 Batch testing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE

if (confirmation == JOptionPane.YES_OPTION) {
    println "Evaluating students projects"
    println "\n"
    File[] files = new File("$CWD${File.separator}projects").listFiles()
    for (File project : files) {
        Evaluation e = testStudent(project)
        results << e
    }

//    def resultFile = new File(CWD + File.separator + "results-${STUDENT_GROUP}.csv")
//    resultFile.text = String.join(CSV_DELIMITER, CSV_HEADER) + '\n'
//    results.each {
//        resultFile.append(it.toString(CSV_DELIMITER))
//        resultFile.append("\n")
//    }
} else {
    def projectDir = input "Enter students folder to test"
    if (!projectDir || projectDir.isEmpty())
        throw new RuntimeException("No student's folder has been entered")
    File projectFile = new File(CWD + File.separator + 'projects' + File.separator + projectDir)
    if (!projectFile.exists())
        throw new RuntimeException("No student's folder has been found")
    Evaluation ev = testStudent(projectFile)
    println ev
}

println "Tests run for all tests took ${Duration.between(startOfScript, LocalDateTime.now()).toString()}"
System.exit(0)