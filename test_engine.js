const http = require('http');

const runTest = (name, payload) => {
  return new Promise((resolve) => {
    const data = JSON.stringify(payload);
    
    const options = {
      hostname: 'localhost',
      port: 8080,
      path: '/api/v1/public/eligibility/evaluate',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': data.length,
        'X-Forwarded-For': '203.0.113.' + Math.floor(Math.random() * 255)
      }
    };
    
    const req = http.request(options, (res) => {
      let body = '';
      res.on('data', chunk => body += chunk);
      res.on('end', () => {
        try {
          const results = JSON.parse(body);
          console.log(`\n=== Test Case: ${name} ===`);
          console.log(`Total Rules Evaluated: ${results.length}`);
          
          const eligible = results.filter(r => r.eligible);
          console.log(`Eligible Offers: ${eligible.length}`);
          eligible.forEach(e => {
            console.log(` ✅ ${e.lenderName || ''} ${e.productCode} (${e.productName})`);
            console.log(`    Max Amount: ₹${e.maxEligibleAmount}, ROI: ${e.roi}%`);
          });
          
          const rejected = results.filter(r => !r.eligible);
          console.log(`Rejected Offers: ${rejected.length}`);
          if (rejected.length > 0) {
            // Print first 2 rejections just to see why
            rejected.slice(0, 3).forEach(e => {
              console.log(` ❌ ${e.lenderName || ''} ${e.productCode} (${e.productName})`);
              console.log(`    Reasons: ${e.rejectionReasons.join(' | ')}`);
            });
            if (rejected.length > 3) console.log(`    ... and ${rejected.length - 3} more`);
          }
          resolve();
        } catch(e) {
          console.log(`Error parsing JSON for ${name}:`, e.message, "\nRaw:", body);
          resolve();
        }
      });
    });
    
    req.on('error', error => {
      console.error(`Error in test ${name}:`, error.message);
      resolve();
    });
    
    req.write(data);
    req.end();
  });
};

const basePayload = {
  loanType: "HL",
  cibilScore: 750,
  applicantAge: 30,
  employmentType: "Salaried",
  propertyType: "Ready Possession",
  cityTier: "Tier 1",
  loanAmount: 5000000,
  propertyValue: 8000000,
  requestedTenureMonths: 240,
  monthlyIncome: 100000,
  existingEmiTotal: 10000,
  businessAgeYears: 0,
  workExpYears: 5,
  incomeComputationInput: {
    programName: "NIP",
    pat: 0,
    depreciation: 0,
    interestExpense: 0,
    averageBankBalance: 0,
    bankBalanceSamples: [],
    gstrTurnover12Months: 0,
    businessType: "",
    grossReceipts: 0,
    profession: "",
    lenderName: "",
    loanType: ""
  },
  idempotencyKey: "test-" + Date.now()
};

async function main() {
  console.log("Starting Engine Tests...\n");
  
  // await runTest("1. Ideal Salaried Home Loan", {
  //   ...basePayload,
  //   loanType: "HL",
  //   employmentType: "Salaried"
  // });
  
  // await runTest("2. Ideal Salaried LAP", {
  //   ...basePayload,
  //   loanType: "LAP",
  //   employmentType: "Salaried"
  // });

  await runTest("3. SEP Doctor Home Loan", {
    ...basePayload,
    loanType: "HL",
    employmentType: "Self Employed Professional",
    businessAgeYears: 5,
    incomeComputationInput: {
      ...basePayload.incomeComputationInput,
      programName: "SEP",
      profession: "Doctor",
      grossReceipts: 5000000
    }
  });
  
  await runTest("4. SENP Home Loan", {
    ...basePayload,
    loanType: "HL",
    employmentType: "Self Employed Non Professional",
    businessAgeYears: 5,
    incomeComputationInput: {
      ...basePayload.incomeComputationInput,
      programName: "SENP",
      grossReceipts: 5000000
    }
  });
  
  // await runTest("5. Low CIBIL Rejection", {
  //   ...basePayload,
  //   cibilScore: 600
  // });

  // await runTest("6. High LTV (Loan vs Property Value) Rejection", {
  //   ...basePayload,
  //   loanAmount: 7500000, // 75L
  //   propertyValue: 8000000 // 80L -> 93.75% LTV
  // });
}

main();
