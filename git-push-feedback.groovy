import javax.swing.*

String FEEDBACK_DIR = "feedback"
String TEACHER_EMAIL = "6153201+mladoniczky@users.noreply.github.com"
String REPO_URL = "https://github.com/Interes-Group/semestralny-projekt-2-"
Boolean ALL = true
def confirm = JOptionPane.&showConfirmDialog

def runCommand = { File project, List<String> commands ->
    commands.add(0, "cmd")
    commands.add(1, "/c")
    def b = new ProcessBuilder(commands)
    b.directory(project)
    b.inheritIO()
    def p = b.start()
    p.waitFor()
}

def pushFolder = { File project, boolean push ->
    File feedback = new File(project.absolutePath + File.separator + FEEDBACK_DIR)
    if (!feedback || !feedback.exists()) return
    String repo = REPO_URL + project.name
    println "Gitops for ${project.name}"
    runCommand(project, ['git', 'pull', 'origin'])
    if (push) {
        runCommand(project, ['git', 'remote', 'set-url', 'origin', repo])
        runCommand(project, ['git', 'config', 'user.email', TEACHER_EMAIL])
        runCommand(project, ['git', 'add', FEEDBACK_DIR])
        runCommand(project, ['git', 'commit', '-a', '-m', "'Update feedback from tests'"])
        runCommand(project, ['git', 'push', 'origin'])
    }
    println "----------------------------------\n \n"
}


File cwd = new File('./')
JFrame frame = new JFrame("VSA Project 2 Batch gitops")
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
def alsoPush = confirm(frame, "Do you want to git push with the update?", "VSA Project 2 Batch testing", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION

if (ALL) {
    cwd.listFiles().each {
        pushFolder(new File(it.toPath().normalize().toFile().absolutePath), alsoPush)
    }
} else {
    pushFolder(new File(cwd.absolutePath).parentFile, alsoPush)
}
