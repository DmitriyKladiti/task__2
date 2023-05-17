import org.kohsuke.args4j.Argument
import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import org.kohsuke.args4j.spi.BooleanOptionHandler
import java.io.File
import java.text.SimpleDateFormat

// класс для разбора аргументов командной стоки
class CommandLineArgsParser(args: Array<String>) {
    @Option(name = "-l", usage = "List files in long format")
    var listFilesInLongFormat: Boolean = false

    @Option(name = "-h", usage = "List files in human-readable format")
    var listFilesInHumanReadableFormat: Boolean = false

    @Option(name = "-r", usage = "Change file sort order")
    var reverseSortOrder: Boolean = false

    @Option(name = "-o", usage = "Write list of files to an output file")
    var outputFileName: String? = null

    @Argument
    var dir: String? = null

    init {
        val parser = CmdLineParser(this)
        parser.parseArgument(*args)
    }
}

// -l -h -r -o output.txt
fun main(args: Array<String>) {
    val cmdArgs = CommandLineArgsParser(args)

    if (cmdArgs.dir == null) {
        // если позиционный параметр 'dir' не задан, работаем с текущей директорией
        cmdArgs.dir = "."
    }

    val files = File(cmdArgs.dir).listFiles()
    val res = mutableMapOf<String, String>() // список файлов, ключ - имя файла, значение - аттрибуты файла в виде строки
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    if (files != null) {
        for (file in files) {
            if (cmdArgs.listFilesInLongFormat) {
                val canRead = if (file.canRead()) 1 else 0
                val canWrite = if (file.canWrite()) 1 else 0
                val canExecute = if (file.canExecute()) 1 else 0

                val lastModified = file.lastModified()
                val lastModifiedDate = dateFormat.format(lastModified)

                var perms = "${canRead}${canWrite}${canExecute}"
                if (cmdArgs.listFilesInHumanReadableFormat) {
                    // приводим права файла к человекочитаемому виду (111 -> rwx)
                    perms = (if (canRead == 1) "r" else "-") +
                            (if (canWrite == 1) "w" else "-") +
                            (if (canExecute == 1) "x" else "-")
                }
                res[file.name] = "${perms}\t${lastModifiedDate}\t${file.length()}"
            } else {
                res[file.name] = file.name
            }
        }
    }

    // сортировка имен файлов в прямом или обратном порядке
    val sortedKeys = if (!cmdArgs.reverseSortOrder) res.keys.sorted() else res.keys.sortedDescending()
    val resLines = mutableListOf<String>()
    for (f in sortedKeys) {
        if (cmdArgs.listFilesInLongFormat) {
            resLines.add("${res[f]}\t${f}")
        } else {
            resLines.add("${f}")
        }
    }

    if (cmdArgs.outputFileName == null) {
        // печать на экран
        for (line in resLines) println(line)
    } else {
        // вывод в файл
        val writer = File(cmdArgs.outputFileName).printWriter()
        for (line in resLines) writer.println(line)
        writer.close()
    }
}
