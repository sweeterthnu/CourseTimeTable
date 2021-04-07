import org.apache.poi.xssf.usermodel.*

fun main() {
    val filePath = "src/main/resources/Automatic_and_systems.xlsx"
    val excelTable = XSSFWorkbook(filePath)
    val controller = Controller(excelTable,0)

    val groupPredicate: Predicate<Lesson> = { lesson -> lesson.meta.contains("19а") }
    val teacherPredicate: Predicate<Lesson> = { lesson -> lesson.teacher.name.contains("ст.пр. Александров А.В.") }
    val classroomPredicate: Predicate<Lesson> =  { lesson -> lesson.meta.contains("а.1-326") }
    val lessonNamePredicate: Predicate<Lesson> = { lesson -> lesson.name.contains("лаб.адинистрирование ИС") }

    controller.writeScheduleIntoFile(WeekType.odd, classroomPredicate)
}