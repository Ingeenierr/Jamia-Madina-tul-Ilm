package com.jamia.madinatulilm.utils

import android.content.Context
import android.net.Uri
import com.jamia.madinatulilm.data.Donation
import com.jamia.madinatulilm.data.Student
import com.jamia.madinatulilm.data.Teacher
import com.jamia.madinatulilm.data.finance.DonationRecord
import com.jamia.madinatulilm.data.finance.PayrollRecord
import com.jamia.madinatulilm.data.finance.Transaction
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.InputStream
import java.io.OutputStream

object ExcelExporter {

    fun exportStudentsToExcel(context: Context, students: List<Student>, uri: Uri) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Students")

        // Header row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Full Name")
        headerRow.createCell(2).setCellValue("Age")
        headerRow.createCell(3).setCellValue("Guardian's Name")
        headerRow.createCell(4).setCellValue("Contact Number")
        headerRow.createCell(5).setCellValue("CNIC")
        headerRow.createCell(6).setCellValue("Address")
        headerRow.createCell(7).setCellValue("Date of Birth")

        // Data rows
        students.forEachIndexed { index, student ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(student.id)
            row.createCell(1).setCellValue(student.fullName)
            row.createCell(2).setCellValue(student.age.toDouble())
            row.createCell(3).setCellValue(student.guardianName)
            row.createCell(4).setCellValue(student.contactNumber)
            row.createCell(5).setCellValue(student.cnic)
            row.createCell(6).setCellValue(student.address)
            row.createCell(7).setCellValue(student.dob)
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
    }

    fun importStudentsFromExcel(context: Context, uri: Uri): List<Student> {
        val students = mutableListOf<Student>()
        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) { // Start from 1 to skip header
                val row = sheet.getRow(i)
                val id = row.getCell(0)?.stringCellValue ?: ""
                val fullName = row.getCell(1)?.stringCellValue ?: ""
                val age = row.getCell(2)?.numericCellValue?.toInt() ?: 0
                val guardianName = row.getCell(3)?.stringCellValue ?: ""
                val contactNumber = row.getCell(4)?.stringCellValue ?: ""
                val cnic = row.getCell(5)?.stringCellValue ?: ""
                val address = row.getCell(6)?.stringCellValue ?: ""
                val dob = row.getCell(7)?.stringCellValue ?: ""

                if (fullName.isNotBlank()) {
                    students.add(
                        Student(
                            id = id,
                            fullName = fullName,
                            age = age,
                            guardianName = guardianName,
                            contactNumber = contactNumber,
                            cnic = cnic,
                            address = address,
                            dob = dob
                        )
                    )
                }
            }
            workbook.close()
        }
        return students
    }

    fun exportTeachersToExcel(context: Context, teachers: List<Teacher>, uri: Uri) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Teachers")

        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Name")
        headerRow.createCell(2).setCellValue("Qualifications")
        headerRow.createCell(3).setCellValue("Contact Info")
        headerRow.createCell(4).setCellValue("CNIC")
        headerRow.createCell(5).setCellValue("Address")
        headerRow.createCell(6).setCellValue("Date of Birth")
        headerRow.createCell(7).setCellValue("Salary")

        teachers.forEachIndexed { index, teacher ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(teacher.id)
            row.createCell(1).setCellValue(teacher.name)
            row.createCell(2).setCellValue(teacher.qualifications)
            row.createCell(3).setCellValue(teacher.contactInfo)
            row.createCell(4).setCellValue(teacher.cnic)
            row.createCell(5).setCellValue(teacher.address)
            row.createCell(6).setCellValue(teacher.dob)
            row.createCell(7).setCellValue(teacher.salary)
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
    }

    fun importTeachersFromExcel(context: Context, uri: Uri): List<Teacher> {
        val teachers = mutableListOf<Teacher>()
        context.contentResolver.openInputStream(uri)?.use { inputStream: InputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)

            for (i in 1..sheet.lastRowNum) { // Start from 1 to skip header
                val row = sheet.getRow(i)
                val id = row.getCell(0)?.stringCellValue ?: ""
                val name = row.getCell(1)?.stringCellValue ?: ""
                val qualifications = row.getCell(2)?.stringCellValue ?: ""
                val contactInfo = row.getCell(3)?.stringCellValue ?: ""
                val cnic = row.getCell(4)?.stringCellValue ?: ""
                val address = row.getCell(5)?.stringCellValue ?: ""
                val dob = row.getCell(6)?.stringCellValue ?: ""
                val salary = row.getCell(7)?.numericCellValue ?: 0.0

                if (name.isNotBlank()) {
                    teachers.add(
                        Teacher(
                            id = id,
                            name = name,
                            qualifications = qualifications,
                            contactInfo = contactInfo,
                            cnic = cnic,
                            address = address,
                            dob = dob,
                            salary = salary
                        )
                    )
                }
            }
            workbook.close()
        }
        return teachers
    }

    fun exportDonationsToExcel(context: Context, donations: List<Donation>, uri: Uri) {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("Donations")

        // Header row
        val headerRow = sheet.createRow(0)
        headerRow.createCell(0).setCellValue("ID")
        headerRow.createCell(1).setCellValue("Student ID")
        headerRow.createCell(2).setCellValue("Amount")
        headerRow.createCell(3).setCellValue("Date")
        headerRow.createCell(4).setCellValue("Type")

        // Data rows
        donations.forEachIndexed { index, donation ->
            val row = sheet.createRow(index + 1)
            row.createCell(0).setCellValue(donation.id)
            row.createCell(1).setCellValue(donation.studentId)
            row.createCell(2).setCellValue(donation.amount)
            row.createCell(3).setCellValue(donation.date)
            row.createCell(4).setCellValue(donation.type)
        }

        context.contentResolver.openOutputStream(uri)?.use { outputStream: OutputStream ->
            workbook.write(outputStream)
        }
        workbook.close()
    }

    fun exportFinanceReportToExcel(
        context: Context,
        month: String,
        donations: List<DonationRecord>,
        payroll: List<PayrollRecord>,
        transactions: List<Transaction>,
        uri: Uri
    ) {
        val workbook = XSSFWorkbook()
        
        // 1. Overview Sheet
        val summarySheet = workbook.createSheet("Monthly Overview")
        val h1 = summarySheet.createRow(0)
        h1.createCell(0).setCellValue("Report Month: $month")
        
        val h2 = summarySheet.createRow(2)
        h2.createCell(0).setCellValue("Category")
        h2.createCell(1).setCellValue("Total (Rs.)")
        
        val dTotal = donations.sumOf { it.amount }
        val pTotal = payroll.filter { it.isPaid }.sumOf { it.amount }
        val tProfit = transactions.sumOf { it.profit }
        
        val r1 = summarySheet.createRow(3)
        r1.createCell(0).setCellValue("Total Donations")
        r1.createCell(1).setCellValue(dTotal)
        
        val r2 = summarySheet.createRow(4)
        r2.createCell(0).setCellValue("Total Payroll Paid")
        r2.createCell(1).setCellValue(pTotal)
        
        val r3 = summarySheet.createRow(5)
        r3.createCell(0).setCellValue("Shop Profit")
        r3.createCell(1).setCellValue(tProfit)
        
        val r4 = summarySheet.createRow(7)
        r4.createCell(0).setCellValue("NET BALANCE")
        r4.createCell(1).setCellValue(dTotal + tProfit - pTotal)

        // 2. Details Sheet
        val donationSheet = workbook.createSheet("Donations Detail")
        val dh = donationSheet.createRow(0)
        dh.createCell(0).setCellValue("Donor")
        dh.createCell(1).setCellValue("Amount")
        dh.createCell(2).setCellValue("Type")
        donations.forEachIndexed { i, d ->
            val row = donationSheet.createRow(i + 1)
            row.createCell(0).setCellValue(d.donorName)
            row.createCell(1).setCellValue(d.amount)
            row.createCell(2).setCellValue(d.type)
        }

        context.contentResolver.openOutputStream(uri)?.use { workbook.write(it) }
        workbook.close()
    }
}
