package com.pryme.Backend.eligibility.audit.certification;

import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

@Service
public class ExcelWorkbookParser {

    public List<WorkbookModels.EligibilityRow> parseEligibilityWorkbook(InputStream is) throws Exception {
        List<WorkbookModels.EligibilityRow> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheet("Loan_Product_Master");
            if (sheet == null) {
                sheet = wb.getSheetAt(0);
            }
            
            Map<String, Integer> headerMap = getHeaderMap(sheet);
            int totalRows = sheet.getLastRowNum();
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                
                list.add(new WorkbookModels.EligibilityRow(
                    getStringValue(row, headerMap, "Product_Name"),
                    getStringValue(row, headerMap, "Loan_Type"),
                    getStringValue(row, headerMap, "Lender_Name"),
                    getStringValue(row, headerMap, "Interest_Type"),
                    getStringValue(row, headerMap, "Property_Type"),
                    getStringValue(row, headerMap, "Negative_Property"),
                    getStringValue(row, headerMap, "Employment_Type"),
                    getStringValue(row, headerMap, "Self Employed Professional"),
                    getStringValue(row, headerMap, "Surrogate"),
                    getStringValue(row, headerMap, "Margin (by occupation)"),
                    getStringValue(row, headerMap, "LTV"),
                    getStringValue(row, headerMap, "Formulae"),
                    getStringValue(row, headerMap, "Conditions"),
                    getIntegerValue(row, headerMap, "MIN_CIBIL"),
                    getIntegerValue(row, headerMap, "Min_Tenure (Months)"),
                    getIntegerValue(row, headerMap, "Max_Tenure (Months)"),
                    getBigDecimalValue(row, headerMap, "Min_LoanAmount"),
                    getBigDecimalValue(row, headerMap, "Max_LoanAmount"),
                    getStringValue(row, headerMap, "Negative_Employer_Type"),
                    getBigDecimalValue(row, headerMap, "Min_Income"),
                    getIntegerValue(row, headerMap, "Min_Age"),
                    getIntegerValue(row, headerMap, "Max_Age"),
                    getStringValue(row, headerMap, "Negative_Profile"),
                    getStringValue(row, headerMap, "Vintage"),
                    getStringValue(row, headerMap, "ITR_Required"),
                    getStringValue(row, headerMap, "Provident_Fund_Mandatory"),
                    getStringValue(row, headerMap, "Negative_Mode_Salary"),
                    getStringValue(row, headerMap, "Bank_Statement"),
                    getStringValue(row, headerMap, "Salary_Slip_months"),
                    getStringValue(row, headerMap, "GST_Required_months"),
                    getStringValue(row, headerMap, "EMI_not_Obligated"),
                    getStringValue(row, headerMap, "Admin Fee"),
                    getStringValue(row, headerMap, "Insurance Charges"),
                    getStringValue(row, headerMap, "Legal and Technical Charges"),
                    getStringValue(row, headerMap, "Other_Expense"),
                    getStringValue(row, headerMap, "Stamp Duty"),
                    getStringValue(row, headerMap, "Prepayment Charges"),
                    getStringValue(row, headerMap, "Foreclosure Charges"),
                    getStringValue(row, headerMap, "Notes")
                ));
            }
        }
        return list;
    }

    public List<WorkbookModels.FoirRow> parseFoirWorkbook(InputStream is) throws Exception {
        List<WorkbookModels.FoirRow> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> headerMap = getHeaderMap(sheet);
            int totalRows = sheet.getLastRowNum();
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                
                list.add(new WorkbookModels.FoirRow(
                    getStringValue(row, headerMap, "Product_Name"),
                    getStringValue(row, headerMap, "Loan_Type"),
                    getStringValue(row, headerMap, "Lender_Name"),
                    getStringValue(row, headerMap, "Surrogate"),
                    getStringValue(row, headerMap, "Employement_Type"),
                    getBigDecimalValue(row, headerMap, "Lower_Salary"),
                    getBigDecimalValue(row, headerMap, "Upper_Salary"),
                    getBigDecimalValue(row, headerMap, "FOIR (%)"),
                    getStringValue(row, headerMap, "Deviation")
                ));
            }
        }
        return list;
    }

    public List<WorkbookModels.PfRow> parsePfWorkbook(InputStream is) throws Exception {
        List<WorkbookModels.PfRow> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> headerMap = getHeaderMap(sheet);
            int totalRows = sheet.getLastRowNum();
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                
                list.add(new WorkbookModels.PfRow(
                    getStringValue(row, headerMap, "Product_Name"),
                    getStringValue(row, headerMap, "Loan_Type"),
                    getStringValue(row, headerMap, "Lender_Name"),
                    getStringValue(row, headerMap, "Employment_Type"),
                    getBigDecimalValue(row, headerMap, "Min_Loan_Amount"),
                    getBigDecimalValue(row, headerMap, "Max_Loan_Amount"),
                    getBigDecimalValue(row, headerMap, "PF"),
                    getBigDecimalValue(row, headerMap, "Tax"),
                    getStringValue(row, headerMap, "Notes")
                ));
            }
        }
        return list;
    }

    public List<WorkbookModels.LoginFeeRow> parseLoginFeeWorkbook(InputStream is) throws Exception {
        List<WorkbookModels.LoginFeeRow> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Map<String, Integer> headerMap = getHeaderMap(sheet);
            int totalRows = sheet.getLastRowNum();
            for (int r = 1; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                
                list.add(new WorkbookModels.LoginFeeRow(
                    getStringValue(row, headerMap, "Product_Name"),
                    getStringValue(row, headerMap, "Loan_Type"),
                    getStringValue(row, headerMap, "Lender_Name"),
                    getStringValue(row, headerMap, "Employment_Type"),
                    getBigDecimalValue(row, headerMap, "Min_Loan_Amount"),
                    getBigDecimalValue(row, headerMap, "Max_Loan_Amount"),
                    getBigDecimalValue(row, headerMap, "Login Fees")
                ));
            }
        }
        return list;
    }

    private Map<String, Integer> getHeaderMap(Sheet sheet) {
        Map<String, Integer> map = new HashMap<>();
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            for (Cell cell : headerRow) {
                if (cell != null && cell.getCellType() == CellType.STRING) {
                    map.put(cell.getStringCellValue().trim(), cell.getColumnIndex());
                }
            }
        }
        return map;
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getStringValue(Row row, Map<String, Integer> headerMap, String columnName) {
        Integer colIndex = headerMap.get(columnName);
        if (colIndex == null) return null;
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            double num = cell.getNumericCellValue();
            if (num == (long) num) {
                return String.valueOf((long) num);
            }
            return String.valueOf(num);
        }
        if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return cell.getStringCellValue().trim();
    }

    private Integer getIntegerValue(Row row, Map<String, Integer> headerMap, String columnName) {
        String val = getStringValue(row, headerMap, columnName);
        if (val == null || val.equalsIgnoreCase("NA") || val.equalsIgnoreCase("No Limit")) return null;
        try {
            return (int) Double.parseDouble(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal getBigDecimalValue(Row row, Map<String, Integer> headerMap, String columnName) {
        String val = getStringValue(row, headerMap, columnName);
        if (val == null || val.equalsIgnoreCase("NA") || val.equalsIgnoreCase("No Limit")) return null;
        // Strip commas and currency symbols
        val = val.replaceAll("[^0-9.\\-]", "");
        if (val.isEmpty()) return null;
        try {
            return new BigDecimal(val);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
