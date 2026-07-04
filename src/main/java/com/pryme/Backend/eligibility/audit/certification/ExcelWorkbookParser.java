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

    public List<WorkbookModels.HlLtvRow> parseHlLtvWorkbook(InputStream is) throws Exception {
        List<WorkbookModels.HlLtvRow> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            String currentPropertyType = null;
            for (int r = 0; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                
                Cell cell0 = row.getCell(0);
                if (cell0 != null && cell0.getCellType() == CellType.STRING) {
                    String val = cell0.getStringCellValue().trim();
                    if (val.equalsIgnoreCase("Ready Built Property") || val.equalsIgnoreCase("Plot")) {
                        currentPropertyType = val;
                        // Skip header row right below it
                        r++;
                        continue;
                    }
                }
                
                if (currentPropertyType != null) {
                    BigDecimal minVal = getBigDecimalValueFromCell(row.getCell(0));
                    BigDecimal maxVal = getBigDecimalValueFromCell(row.getCell(1));
                    BigDecimal ltvVal = getBigDecimalValueFromCell(row.getCell(2));
                    if (minVal != null || maxVal != null || ltvVal != null) {
                        list.add(new WorkbookModels.HlLtvRow(currentPropertyType, minVal, maxVal, ltvVal));
                    }
                }
            }
        }
        return list;
    }

    public List<WorkbookModels.LapLtvRow> parseLapLtvWorkbook(InputStream is) throws Exception {
        List<WorkbookModels.LapLtvRow> list = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(is)) {
            Sheet sheet = wb.getSheetAt(0);
            Row catRow = sheet.getRow(0);
            Row subRow = sheet.getRow(1);
            if (catRow == null || subRow == null) return list;
            
            int lastCellNum = subRow.getLastCellNum();
            String currentCat = null;
            Map<Integer, String[]> colMap = new HashMap<>();
            for (int c = 1; c < lastCellNum; c++) {
                Cell catCell = catRow.getCell(c);
                if (catCell != null && catCell.getCellType() == CellType.STRING && !catCell.getStringCellValue().isBlank()) {
                    currentCat = catCell.getStringCellValue().trim();
                }
                Cell subCell = subRow.getCell(c);
                if (subCell != null && subCell.getCellType() == CellType.STRING && !subCell.getStringCellValue().isBlank()) {
                    colMap.put(c, new String[]{currentCat, subCell.getStringCellValue().trim()});
                }
            }
            
            int totalRows = sheet.getLastRowNum();
            for (int r = 2; r <= totalRows; r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;
                Cell lenderCell = row.getCell(0);
                if (lenderCell == null || lenderCell.getCellType() == CellType.BLANK) continue;
                String lenderName = lenderCell.getStringCellValue().trim();
                
                for (int c = 1; c < lastCellNum; c++) {
                    if (!colMap.containsKey(c)) continue;
                    String[] catSub = colMap.get(c);
                    Cell valCell = row.getCell(c);
                    String ltvVal = null;
                    if (valCell != null) {
                        if (valCell.getCellType() == CellType.NUMERIC) {
                            ltvVal = String.valueOf(valCell.getNumericCellValue());
                        } else if (valCell.getCellType() == CellType.STRING) {
                            ltvVal = valCell.getStringCellValue().trim();
                        }
                    }
                    if (ltvVal != null) {
                        list.add(new WorkbookModels.LapLtvRow(lenderName, catSub[0], catSub[1], ltvVal));
                    }
                }
            }
        }
        return list;
    }

    private BigDecimal getBigDecimalValueFromCell(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) return null;
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        if (cell.getCellType() == CellType.STRING) {
            String val = cell.getStringCellValue().trim();
            if (val.equalsIgnoreCase("NA") || val.equalsIgnoreCase("No Limit") || val.equalsIgnoreCase("No limit")) return null;
            val = val.replaceAll("[^0-9.\\-]", "");
            if (val.isEmpty()) return null;
            try {
                return new BigDecimal(val);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
