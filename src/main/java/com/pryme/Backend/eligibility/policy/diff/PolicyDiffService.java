package com.pryme.Backend.eligibility.policy.diff;

import com.pryme.Backend.eligibility.policy.model.BundleManifest;
import com.pryme.Backend.eligibility.policy.model.PolicyBundle;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class PolicyDiffService {

    public Map<String, Object> diff(PolicyBundle oldBundle, PolicyBundle newBundle) {
        Map<String, Object> report = new LinkedHashMap<>();
        if (oldBundle == null || newBundle == null) {
            report.put("message", "Cannot compute diff: one or both bundles are null.");
            return report;
        }

        BundleManifest oldManifest = oldBundle.manifest();
        BundleManifest newManifest = newBundle.manifest();

        report.put("oldVersion", oldManifest.version());
        report.put("newVersion", newManifest.version());
        report.put("oldCombinedHash", oldManifest.policyBundleHash());
        report.put("newCombinedHash", newManifest.policyBundleHash());
        report.put("hashChanged", !oldManifest.policyBundleHash().equals(newManifest.policyBundleHash()));

        // Workbook Diff
        List<String> individualChanges = new ArrayList<>();
        if (oldManifest.individualHashes() != null && newManifest.individualHashes() != null) {
            for (String file : oldManifest.individualHashes().keySet()) {
                String oldHash = oldManifest.individualHashes().get(file);
                String newHash = newManifest.individualHashes().get(file);
                if (newHash == null) {
                    individualChanges.add(file + " was removed.");
                } else if (!oldHash.equals(newHash)) {
                    individualChanges.add(file + " hash changed from " + oldHash + " to " + newHash);
                }
            }
            for (String file : newManifest.individualHashes().keySet()) {
                if (!oldManifest.individualHashes().containsKey(file)) {
                    individualChanges.add(file + " was added.");
                }
            }
        }
        report.put("workbookChanges", individualChanges);

        // Raw row counts diff
        Map<String, String> counts = new LinkedHashMap<>();
        counts.put("eligibilityRows", (oldBundle.eligibilityRules() != null ? oldBundle.eligibilityRules().size() : 0)
                + " -> " + (newBundle.eligibilityRules() != null ? newBundle.eligibilityRules().size() : 0));
        counts.put("foirRows", (oldBundle.foirRules() != null ? oldBundle.foirRules().size() : 0)
                + " -> " + (newBundle.foirRules() != null ? newBundle.foirRules().size() : 0));
        counts.put("pfRows", (oldBundle.pfRules() != null ? oldBundle.pfRules().size() : 0)
                + " -> " + (newBundle.pfRules() != null ? newBundle.pfRules().size() : 0));
        counts.put("loginFeeRows", (oldBundle.loginFeeRules() != null ? oldBundle.loginFeeRules().size() : 0)
                + " -> " + (newBundle.loginFeeRules() != null ? newBundle.loginFeeRules().size() : 0));
        counts.put("lowLtvRows", (oldBundle.lowLtvRules() != null ? oldBundle.lowLtvRules().size() : 0)
                + " -> " + (newBundle.lowLtvRules() != null ? newBundle.lowLtvRules().size() : 0));
        counts.put("roiRows", (oldBundle.roiRules() != null ? oldBundle.roiRules().size() : 0)
                + " -> " + (newBundle.roiRules() != null ? newBundle.roiRules().size() : 0));
        report.put("rowCountDiff", counts);

        return report;
    }
}
