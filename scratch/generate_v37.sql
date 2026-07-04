ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS min_tenure INTEGER;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS max_tenure INTEGER;
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS min_loan_amount NUMERIC(15,2);
ALTER TABLE eligibility_conditions ADD COLUMN IF NOT EXISTS max_loan_amount NUMERIC(15,2);

UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 23, max_age = 60, 
    min_tenure = 36, max_tenure = 360, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = 0.75 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = 0.75 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = 0.75 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 62, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 60, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 62, 
    min_tenure = 36, max_tenure = 300, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 62, 
    min_tenure = 36, max_tenure = 300, min_loan_amount = 3500000, max_loan_amount = 35000000, ltv_allowed = 0.50 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 35000000, ltv_allowed = 0.50 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 35000000, ltv_allowed = 0.50 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 10000, min_age = 21, max_age = 60, 
    min_tenure = 36, max_tenure = 360, min_loan_amount = 500000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'Bank of Baroda' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 10000, min_age = 21, max_age = 70, 
    min_tenure = 36, max_tenure = 360, min_loan_amount = 500000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'Bank of Baroda' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 10000, min_age = 21, max_age = 70, 
    min_tenure = 36, max_tenure = 360, min_loan_amount = 500000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'Bank of Baroda' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 550, min_income = 25000, min_age = 20, max_age = 65, 
    min_tenure = 24, max_tenure = 360, min_loan_amount = 1000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'State Bank of India' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 550, min_income = 25000, min_age = 20, max_age = 70, 
    min_tenure = 24, max_tenure = 360, min_loan_amount = 1000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'State Bank of India' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 550, min_income = 25000, min_age = 20, max_age = 70, 
    min_tenure = 24, max_tenure = 360, min_loan_amount = 1000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'State Bank of India' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 384, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 65, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.45 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.45 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 21, max_age = 65, 
    min_tenure = 60, max_tenure = 360, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 21, max_age = 65, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 21, max_age = 65, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 750, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2100000, max_loan_amount = 20000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 750, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 2100000, max_loan_amount = 20000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 62, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 62, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 3000000, max_loan_amount = 30000000, ltv_allowed = 0.50 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 30000000, ltv_allowed = 0.50 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 30000000, ltv_allowed = 0.50 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 3000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 40000, min_age = 22, max_age = 65, 
    min_tenure = 60, max_tenure = 360, min_loan_amount = 5000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'IDBI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'IDBI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'IDBI Bank' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 62, 
    min_tenure = 60, max_tenure = 360, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 62, 
    min_tenure = 60, max_tenure = 360, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 62, 
    min_tenure = 60, max_tenure = 360, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.40 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.40 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.40 
WHERE bank_name = 'TATA Capital' AND loan_type = 'HOME LOAN' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 23, max_age = 60, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = 0.75 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = 0.75 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = 0.75 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 25000, min_age = 25, max_age = 70, 
    min_tenure = 36, max_tenure = 240, min_loan_amount = 2000000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'L&T Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 62, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'ICICI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 60, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 15000, min_age = 21, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 200000, max_loan_amount = 80000000, ltv_allowed = NULL 
WHERE bank_name = 'Bandhan Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 62, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 62, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 35000000, ltv_allowed = 0.40 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 35000000, ltv_allowed = 0.40 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 35000000, ltv_allowed = 0.40 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 675, min_income = 30000, min_age = 22, max_age = 80, 
    min_tenure = 36, max_tenure = 180, min_loan_amount = 3500000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'Aditya Birla Finance Limited' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 10000, min_age = 21, max_age = 60, 
    min_tenure = 36, max_tenure = 144, min_loan_amount = 500000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'Bank of Baroda' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 10000, min_age = 21, max_age = 65, 
    min_tenure = 36, max_tenure = 144, min_loan_amount = 500000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'Bank of Baroda' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 10000, min_age = 21, max_age = 65, 
    min_tenure = 36, max_tenure = 144, min_loan_amount = 500000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'Bank of Baroda' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = 25000, min_age = 20, max_age = 65, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'State Bank of India' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = 25000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'State Bank of India' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = 25000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'State Bank of India' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.65 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 62, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 30000, min_age = 23, max_age = 70, 
    min_tenure = 120, max_tenure = 240, min_loan_amount = 3500000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'Bajaj Prime' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 65, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 65, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.45 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 65, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.40 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 65, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.30 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.45 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.45 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.40 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.40 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.30 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 15000000, ltv_allowed = 0.30 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 150000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 680, min_income = 40000, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 2100000, max_loan_amount = 50000000, ltv_allowed = NULL 
WHERE bank_name = 'Yes Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 20, max_age = 62, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1100000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'HDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1100000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'HDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1100000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'HDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1100000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1100000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'HDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1100000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'HDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 30000, min_age = 20, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1100000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'HDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 62, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 62, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 15000000, ltv_allowed = 0.40 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 15000000, ltv_allowed = 0.40 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 15000000, ltv_allowed = 0.40 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 30000000, ltv_allowed = NULL 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 600, min_income = NULL, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 300, min_loan_amount = 5000000, max_loan_amount = 30000000, ltv_allowed = NULL 
WHERE bank_name = 'IDFC Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 60, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 60, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 30000000, ltv_allowed = 0.50 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 30000000, ltv_allowed = 0.50 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 30000000, ltv_allowed = 0.50 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 60, 
    min_tenure = 60, max_tenure = 144, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 144, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 144, min_loan_amount = 3000000, max_loan_amount = 500000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 75000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'BANKING';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = NULL, min_age = 23, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 3000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'JIO Finance' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'GST';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 40000, min_age = 22, max_age = 65, 
    min_tenure = 60, max_tenure = 240, min_loan_amount = 5000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'IDBI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 5000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'IDBI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 700, min_income = 40000, min_age = 22, max_age = 75, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 5000000, max_loan_amount = NULL, ltv_allowed = NULL 
WHERE bank_name = 'IDBI Bank' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 62, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'NIP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = NULL 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'SEP';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 62, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.50 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 62, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.40 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Salaried' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.40 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Professional' AND surrogate = 'LOW_LTV';
UPDATE eligibility_conditions 
SET cibil_min = 650, min_income = 40000, min_age = 23, max_age = 70, 
    min_tenure = 60, max_tenure = 180, min_loan_amount = 1000000, max_loan_amount = 100000000, ltv_allowed = 0.40 
WHERE bank_name = 'TATA Capital' AND loan_type = 'LOAN AGAINST PROPERTY' AND employment_type = 'Self Employed Non Professional' AND surrogate = 'LOW_LTV';
