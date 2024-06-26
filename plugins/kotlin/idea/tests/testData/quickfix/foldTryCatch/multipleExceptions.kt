// "Lift assignment out of 'try' expression" "true"
// WITH_STDLIB

fun foo() {
    val x: Int
    try {
        x = 1
    } catch (e: RuntimeException) {
        <caret>x = 2
    } catch (e: Exception) {
        x = 3
    } catch (e: Throwable) {
        x = 4
    }
}
// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.quickfix.LiftAssignmentOutOfTryFix
// FUS_K2_QUICKFIX_NAME: org.jetbrains.kotlin.idea.k2.codeinsight.fixes.LiftAssignmentOutOfTryFixFactory$LiftAssignmentOutOfTryFix