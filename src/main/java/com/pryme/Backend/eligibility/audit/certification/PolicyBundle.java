package com.pryme.Backend.eligibility.audit.certification;

import java.util.List;

public record PolicyBundle(
    List<WorkbookModels.EligibilityRow> eligibilityRows,
    List<WorkbookModels.FoirRow> foirRows,
    List<WorkbookModels.PfRow> pfRows,
    List<WorkbookModels.LoginFeeRow> loginFeeRows,
    List<WorkbookModels.HlLtvRow> hlLtvRows,
    List<WorkbookModels.LapLtvRow> lapLtvRows,
    String workbookHash
) {}
