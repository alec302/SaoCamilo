package com.pisc.project.util

import com.lowagie.text.Document
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfPTable
import com.lowagie.text.pdf.PdfWriter
import com.pisc.project.data.local.SweatRateEntity
import java.io.FileOutputStream
import java.io.File
import java.text.DecimalFormat

actual fun exportHistoryToPdf(
    history: List<SweatRateEntity>, 
    userEmail: String, 
    onSuccess: (String) -> Unit, 
    onError: (String) -> Unit
) {
    try {
        val userHome = System.getProperty("user.home")
        val desktopPath = "$userHome/Desktop"
        val cleanEmail = userEmail.substringBefore("@")
        val file = File(desktopPath, "Relatorio_SaoCamilo_$cleanEmail.pdf")
        
        val document = Document()
        PdfWriter.getInstance(document, FileOutputStream(file))
        
        document.open()
        
        document.add(Paragraph("Relatório de Performance - São Camilo"))
        document.add(Paragraph("Atleta: $userEmail"))
        document.add(Paragraph(" "))
        
        val table = PdfPTable(5)
        table.widthPercentage = 100f
        
        table.addCell("Treino")
        table.addCell("Clima")
        table.addCell("Duração (min)")
        table.addCell("Variação Peso")
        table.addCell("Taxa (L/h)")
        
        val df = DecimalFormat("#.##")
        history.forEach { session ->
            table.addCell(session.trainingType.ifEmpty { "N/A" })
            table.addCell(session.climate.ifEmpty { "N/A" })
            table.addCell(session.durationMin.toString())
            val weightChange = session.initialWeight - session.finalWeight
            table.addCell("${df.format(weightChange)} kg")
            table.addCell("${df.format(session.hourlyRateL)} L/h")
        }
        
        document.add(table)
        document.close()
        
        onSuccess(file.absolutePath)
    } catch (e: Exception) {
        onError("Erro ao gerar PDF: ${e.message}")
    }
}
