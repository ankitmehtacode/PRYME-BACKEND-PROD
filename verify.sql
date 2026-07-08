SELECT product_code, employment_type, surrogate, count(*)
FROM eligibility_conditions
GROUP BY 1,2,3 HAVING count(*) > 1;

SELECT conname FROM pg_constraint WHERE conname = 'uq_elig_cond_product_emp_surrogate';

SELECT surrogate, count(*) FROM eligibility_conditions GROUP BY surrogate ORDER BY 2 DESC;

SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
