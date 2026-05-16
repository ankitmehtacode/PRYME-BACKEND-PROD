import urllib.request
import json
import ssl

# Bypass SSL verification if needed
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

url = "https://api.gopryme.tech/api/v1/public/eligibility/evaluate"

def get_payload(cibil):
    return {
      "loanType": "HL",
      "cibilScore": cibil,
      "applicantAge": 30,
      "employmentType": "Salaried",
      "propertyType": "Residential",
      "cityTier": "Tier 1",
      "loanAmount": 2500000.00,
      "propertyValue": 5000000.00,
      "requestedTenureMonths": 240,
      "monthlyIncome": 100000.00,
      "existingEmiTotal": 5000.00,
      "businessAgeYears": 5,
      "workExpYears": 5,
      "incomeComputationInput": {
        "surrogateType": "STANDARD",
        "baseIncome": 100000.00
      },
      "idempotencyKey": f"test-roi-{cibil}"
    }

print("=========================================")
print(" ROI MATRIX VALIDATION: BAJAJ NEAR PRIME ")
print("=========================================")

for cibil, expected_roi in [(800, 0.0925), (710, 0.0975), (660, 0.1050)]:
    payload = get_payload(cibil)
    req = urllib.request.Request(url, data=json.dumps(payload).encode('utf-8'), headers={'Content-Type': 'application/json'})
    
    try:
        response = urllib.request.urlopen(req, context=ctx)
        data = json.loads(response.read().decode('utf-8'))
        
        offer = next((item for item in data if item.get("productCode") == "BAJAJ-NP-HL"), None)
        
        if offer:
            if offer.get("eligible"):
                actual_roi = offer.get("roi")
                match = "✅" if actual_roi == expected_roi else "❌"
                print(f"CIBIL {cibil}: Expected ROI {expected_roi*100:.2f}% | Actual ROI {actual_roi*100:.2f}% {match}")
            else:
                print(f"CIBIL {cibil}: REJECTED - {offer.get('rejectionReasons')}")
        else:
            print(f"CIBIL {cibil}: BAJAJ-NP-HL not found in response!")
            
    except Exception as e:
        print(f"Error: {e}")

