import org.apache.poi.xssf.usermodel.*

fun main() {
    val filePath = "src/main/resources/Automatic_and_systems.xlsx"
    val excelTable = XSSFWorkbook(filePath)
    val controller = Controller(excelTable,0)
    controller.printInfo()
}