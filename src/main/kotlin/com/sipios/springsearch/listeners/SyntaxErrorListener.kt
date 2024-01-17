import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer

class SyntaxErrorListener internal constructor() : BaseErrorListener() {
    private val messages: MutableList<String> = ArrayList()

    override fun syntaxError(
        recognizer: Recognizer<*, *>?,
        offendingSymbol: Any,
        line: Int,
        charPositionInLine: Int,
        msg: String,
        e: RecognitionException?
    ) {
        messages.add("line $line:$charPositionInLine $msg")
    }

    override fun toString(): String {
        return messages.toString()
    }
}
