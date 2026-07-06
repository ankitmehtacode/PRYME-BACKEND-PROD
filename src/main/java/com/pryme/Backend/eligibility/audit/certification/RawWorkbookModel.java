package com.pryme.Backend.eligibility.audit.certification;

import java.util.List;
import java.util.Map;

public record RawWorkbookModel(
    List<WorkbookModels.EligibilityRow> eligibilityRows,
    List<WorkbookModels.FoirRow> foirRows,
    List<WorkbookModels.PfRow> pfRows,
    List<WorkbookModels.LoginFeeRow> loginFeeRows,
    List<WorkbookModels.HlLtvRow> hlLtvRows,
    List<WorkbookModels.LapLtvRow> lapLtvRows,
    String combinedHash,
    Map<String, String> individualHashes
) {}
